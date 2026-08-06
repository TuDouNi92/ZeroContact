package net.zerocontact.lua;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.ZCLuaApi;
import net.zerocontact.caliber.HookContext;
import net.zerocontact.caliber.HookEventTrigger;
import net.zerocontact.caliber.LuaHookContext;
import net.zerocontact.datagen.AmmoDataPOJO;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ZCLuaEngine {
    private static final ZcLuaInstance INSTANCE = new ZcLuaInstance();
    private static final long MAX_INSTRUCTIONS = 50_000;
    private static final long MAX_EXECUTION_NANOS = 10_000_000; //10ms

    private static final class BudgetDebugLib extends DebugLib {
        private static final int TIME_CHECK_INTERVAL = 256;
        private long remainInstructions;
        private long executedInstructions;
        private long startedNanos;
        private long endedNanos;
        private long deadlineNanos;
        private int instructionUntilTimeCheck;
        private boolean armed;
        private String failReason;

        void arm() {
            if (ZCLuaEngine.MAX_INSTRUCTIONS <= 0 || ZCLuaEngine.MAX_EXECUTION_NANOS <= 0) {
                throw new IllegalArgumentException("Lua budget must be positive");
            }
            remainInstructions = ZCLuaEngine.MAX_INSTRUCTIONS;
            executedInstructions = 0;
            /*
             * 时间预算从第一条 Lua 指令开始，而不是从 Java 调用
             * function.invoke() 前开始，避免把首次类加载和 JIT 计入脚本。
             */
            startedNanos = 0;
            endedNanos = 0;
            deadlineNanos = 0;
            instructionUntilTimeCheck = TIME_CHECK_INTERVAL;
            failReason = null;
            armed = true;
        }

        void disarm() {
            if (armed) {
                endedNanos = System.nanoTime();
                if (startedNanos == 0) {
                    startedNanos = endedNanos;
                }
                armed = false;
            }
        }

        void checkTimeBudget() {
            if (armed
                    && startedNanos != 0
                    && System.nanoTime() - deadlineNanos >= 0) {
                fail("Lua execution time budget exceeded");
            }
        }

        long executedInstructions() {
            return executedInstructions;
        }

        long elapsedNanos() {
            long end = armed ? System.nanoTime() : endedNanos;
            return Math.max(0, end - startedNanos);
        }

        String failReason() {
            return failReason;
        }

        @Override
        public void onInstruction(int pc, Varargs v, int top) {
            super.onInstruction(pc, v, top);
            if (!armed) {
                return;
            }
            if (failReason != null) {
                throw new LuaError(failReason);
            }
            executedInstructions++;
            if (--remainInstructions < 0) {
                fail("Lua instruction budget exceeded");
            }

            if (startedNanos == 0) {
                startedNanos = System.nanoTime();
                deadlineNanos = startedNanos
                        + ZCLuaEngine.MAX_EXECUTION_NANOS;
            } else if (--instructionUntilTimeCheck <= 0) {
                instructionUntilTimeCheck = TIME_CHECK_INTERVAL;
                checkTimeBudget();
            }
        }

        private void fail(String reason) {
            failReason = reason;
            throw new LuaError(reason);
        }
    }

    private ZCLuaEngine() {
    }

    public static ZcLuaInstance getInstance() {
        return INSTANCE;
    }

    public static final class ZcLuaInstance {
        private final Map<ResourceLocation, Prototype> scripts =
                new ConcurrentHashMap<>();

        /*
         * 在 loadScripts 阶段调用，加载lua文件, 不要在 invoke 中编译。
         */
        public void load(
                ResourceLocation scriptId,
                Reader source
        ) throws IOException {
            Globals compiler = createGlobals();

            Prototype prototype = compiler.compilePrototype(
                    source,
                    "@" + scriptId
            );

            scripts.put(scriptId, prototype);
        }

        //挂载点方法，供Dispatcher挂到指定位置
        public void invoke(
                AmmoDataPOJO.LuaHookData data,
                HookEventTrigger trigger,
                HookContext hookContext
        ) {
            ResourceLocation scriptId = ResourceLocation.tryParse(data.script());

            if (scriptId == null) {
                ZeroContactLogger.LOG.error(
                        "Invalid Lua script id: {}",
                        data.script()
                );
                return;
            }

            Prototype prototype = scripts.get(scriptId);

            if (prototype == null) {
                ZeroContactLogger.LOG.error(
                        "Lua script is not loaded: {}",
                        scriptId
                );
                return;
            }

            if (!hookContext.level().getServer().isSameThread()) {
                ZeroContactLogger.LOG.error(
                        "Lua hook {} was invoked outside the server thread",
                        scriptId
                );
                return;
            }

            LuaHookContext executionContext = new LuaHookContext(
                    scriptId,
                    data.function(),
                    trigger,
                    hookContext,
                    data.arguments()
            );

            try {
                Globals globals = createGlobals();

                BudgetDebugLib budget = (BudgetDebugLib) globals.debuglib;

                // helper 会捕获当前 executionContext
                globals.set(
                        "zc",
                        ZCLuaApi.create(executionContext)
                );

                LuaValue chunk = globals.loader.load(
                        prototype,
                        "@" + scriptId,
                        globals
                );

                /*
                 * Lua 文件应当 return 一个 table。
                 */
                LuaValue module = invokeWithBudget(
                        budget,
                        scriptId,
                        "<module>",
                        trigger,
                        chunk,
                        LuaValue.NONE
                );

                if (!module.istable()) {
                    throw new LuaError(
                            "Script must return a table: " + scriptId
                    );
                }

                LuaValue function = module.get(data.function());

                if (!function.isfunction()) {
                    throw new LuaError(
                            "Function '" + data.function()
                                    + "' not found in " + scriptId
                    );
                }

                LuaTable luaContext = createContextTable(executionContext);
                LuaValue luaArguments = toLuaValue(data.arguments());

                invokeWithBudget(
                        budget,
                        scriptId,
                        data.function(),
                        trigger,
                        function,
                        LuaValue.varargsOf(new LuaValue[]{
                                luaContext,
                                luaArguments
                        })
                );
            } catch (LuaError exception) {
                ZeroContactLogger.LOG.error(
                        "Lua hook failed: script={}, function={}, trigger={}",
                        scriptId,
                        data.function(),
                        trigger,
                        exception
                );
            } catch (Exception exception) {
                ZeroContactLogger.LOG.error(
                        "Unexpected Lua hook failure: script={}, function={}",
                        scriptId,
                        data.function(),
                        exception
                );
            }
        }

        private static LuaValue invokeWithBudget(
                BudgetDebugLib budget,
                ResourceLocation scriptId,
                String functionName,
                HookEventTrigger trigger,
                LuaValue function,
                Varargs arguments
        ) {
            boolean succeeded = false;
            budget.arm();

            try {
                LuaValue result = function.invoke(arguments).arg1();
                /*
                 * 短函数可能不足 256 条指令，因此返回时再检查一次。
                 */
                budget.checkTimeBudget();
                succeeded = true;
                return result;
            } finally {
                budget.disarm();

                String status = budget.failReason() != null
                        ? "budget_exceeded"
                        : succeeded ? "ok" : "error";

                ZeroContactLogger.LOG.debug(
                        "Lua budget: script={}, function={}, trigger={}, "
                                + "instructions={}/{}, time={}us/{}us, status={}",
                        scriptId,
                        functionName,
                        trigger,
                        budget.executedInstructions(),
                        MAX_INSTRUCTIONS,
                        budget.elapsedNanos() / 1_000.0,
                        MAX_EXECUTION_NANOS / 1_000.0,
                        status
                );
            }
        }

        //lua全局环境变量
        private static Globals createGlobals() {
            Globals globals = new Globals();

            globals.load(new BaseLib());
            globals.load(new TableLib());
            globals.load(new StringLib());
            globals.load(new MathLib());

            globals.load(new BudgetDebugLib());

            LoadState.install(globals);
            LuaC.install(globals);

            // 禁止文件、系统和 Java 访问
            globals.set("dofile", LuaValue.NIL);
            globals.set("loadfile", LuaValue.NIL);
            globals.set("load", LuaValue.NIL);
            globals.set("loadstring", LuaValue.NIL);
            globals.set("require", LuaValue.NIL);
            globals.set("package", LuaValue.NIL);
            globals.set("io", LuaValue.NIL);
            globals.set("os", LuaValue.NIL);
            globals.set("luajava", LuaValue.NIL);
            globals.set("debug", LuaValue.NIL);

            return globals;
        }

        //映射Java->Lua上下文，提供第一个参数ctx供访问
        private static LuaTable createContextTable(
                LuaHookContext context
        ) {
            LuaTable table = new LuaTable();

            table.set("script", context.scriptId().toString());
            table.set("function", context.function());
            table.set("trigger", context.trigger().name());

            table.set("position", vectorToLua(context.position()));

            Vec3 previous = context.prevPosition();
            table.set(
                    "previous_position",
                    previous == null
                            ? LuaValue.NIL
                            : vectorToLua(previous)
            );

            /*
             * 暂时只公开 UUID，不公开原生 LivingEntity。
             * 实际实体操作通过 zc.entity 等 helper 完成。
             */
            table.set(
                    "shooter",
                    context.shooter() == null
                            ? LuaValue.NIL
                            : LuaValue.valueOf(
                            context.shooter().getUUID().toString()
                    )
            );

            table.set(
                    "victim",
                    context.victim() == null
                            ? LuaValue.NIL
                            : LuaValue.valueOf(
                            context.victim().getUUID().toString()
                    )
            );

            return table;
        }

        private static LuaTable vectorToLua(Vec3 vector) {
            LuaTable table = new LuaTable();
            table.set("x", vector.x);
            table.set("y", vector.y);
            table.set("z", vector.z);
            return table;
        }

        private static LuaValue toLuaValue(
                Map<String, JsonElement> arguments
        ) {
            LuaTable table = new LuaTable();

            arguments.forEach(
                    (key, value) -> table.set(key, toLuaValue(value))
            );

            return table;
        }

        private static LuaValue toLuaValue(JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return LuaValue.NIL;
            }

            if (element.isJsonObject()) {
                LuaTable table = new LuaTable();

                for (Map.Entry<String, JsonElement> entry
                        : element.getAsJsonObject().entrySet()) {
                    table.set(
                            entry.getKey(),
                            toLuaValue(entry.getValue())
                    );
                }

                return table;
            }

            if (element.isJsonArray()) {
                LuaTable table = new LuaTable();
                int index = 1;

                for (JsonElement value : element.getAsJsonArray()) {
                    table.set(index++, toLuaValue(value));
                }

                return table;
            }

            if (element.isJsonPrimitive()) {
                var primitive = element.getAsJsonPrimitive();

                if (primitive.isBoolean()) {
                    return LuaValue.valueOf(primitive.getAsBoolean());
                }

                if (primitive.isNumber()) {
                    return LuaValue.valueOf(primitive.getAsDouble());
                }

                return LuaValue.valueOf(primitive.getAsString());
            }

            return LuaValue.NIL;
        }
    }
}
