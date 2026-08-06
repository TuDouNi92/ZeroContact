package net.zerocontact.lua;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.ZeroContact;
import net.zerocontact.ZeroContactLogger;
import net.zerocontact.api.ZCLuaApi;
import net.zerocontact.caliber.HookActionExecutor;
import net.zerocontact.caliber.TargetSelector;
import net.zerocontact.datagen.AmmoDataPOJO;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class ZcLuaApiHelpers {
    private static boolean registered;

    private ZcLuaApiHelpers() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
        registerLog();
        registerEffect();
        registerEntity();
    }

    private static void registerLog() {
        ZCLuaApi.register(
                new ResourceLocation(ZeroContact.MOD_ID, "log"),
                (api, context) -> {
                    api.set("info", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue value) {
                            String message = value.checkjstring();

                            // 防止脚本一次写入过长日志
                            if (message.length() > 1_000) {
                                message = message.substring(0, 1_000);
                            }

                            ZeroContactLogger.LOG.info(
                                    "[Lua {}] {}",
                                    context.scriptId(),
                                    message
                            );

                            return LuaValue.NONE;
                        }
                    });
                }
        );
    }

    private static void registerEffect() {
        ZCLuaApi.register(
                new ResourceLocation(ZeroContact.MOD_ID, "effect"),
                (api, context) -> {
                    api.set("apply", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs args) {
                            TargetSelector target = parseTarget(
                                    args.checkjstring(1)
                            );

                            String effectId = args.checkjstring(2);

                            int duration = Mth.clamp(
                                    args.checkint(3),
                                    1,
                                    20 * 60 * 60
                            );

                            int amplifier = Mth.clamp(
                                    args.optint(4, 0),
                                    0,
                                    255
                            );

                            float radius = Mth.clamp(
                                    (float) args.optdouble(5, 0),
                                    0,
                                    32
                            );

                            AmmoDataPOJO.HookActionData action =
                                    new AmmoDataPOJO.HookActionData(
                                            target,
                                            effectId,
                                            duration,
                                            amplifier,
                                            1.0F,
                                            radius
                                    );

                            HookActionExecutor.execute(
                                    action,
                                    context.hookContext()
                            );

                            return LuaValue.NONE;
                        }
                    });
                }
        );
    }

    private static void registerEntity() {
        ZCLuaApi.register(
                new ResourceLocation(ZeroContact.MOD_ID, "entity"),
                (api, context) -> {

                    api.set("health", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String target = arg.checkjstring();
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.getHealth())).orElse(LuaValue.NIL);
                        }
                    });

                    api.set("max_health", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String target = arg.checkjstring();
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.getMaxHealth())).orElse(LuaValue.NIL);
                        }
                    });

                    api.set("has_effect", new TwoArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg, LuaValue arg2) {
                            String target = arg.checkjstring();
                            String effect = arg2.checkjstring();
                            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect));
                            if (mobEffect == null) return LuaValue.FALSE;
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.hasEffect(mobEffect))).orElse(LuaValue.FALSE);
                        }
                    });

                    api.set("remove_effect", new TwoArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg, LuaValue arg2) {
                            String target = arg.checkjstring();
                            String effect = arg2.checkjstring();
                            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect));
                            if (mobEffect == null) return LuaValue.FALSE;
                            TargetSelector selector = parseTarget(target);
                            return selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .map(lv -> (LuaValue) LuaValue.valueOf(lv.removeEffect(mobEffect))).orElse(LuaValue.FALSE);
                        }
                    });

                    api.set("extinguish", new OneArgFunction() {
                        @Override
                        public LuaValue call(LuaValue arg) {
                            String target = arg.checkjstring();
                            TargetSelector selector = parseTarget(target);
                            selector.resolve(context.hookContext(), 0)
                                    .findFirst()
                                    .ifPresent(lv -> lv.setRemainingFireTicks(0));
                            return LuaValue.NONE;
                        }
                    });

                    api.set("heal", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs varargs) {
                            String target = varargs.checkjstring(1);
                            TargetSelector targetSelector = parseTarget(target);
                            int amount = varargs.optint(2, 1);
                            float radius = (float) varargs.optdouble(3, 1);
                            targetSelector.resolve(context.hookContext(), radius).forEach(
                                    lv -> lv.heal(amount)
                            );
                            return LuaValue.NONE;
                        }
                    });


                    api.set("ignite", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs varargs) {
                            String target = varargs.checkjstring(1);
                            TargetSelector targetSelector = parseTarget(target);
                            int seconds = varargs.optint(2, 1);
                            float radius = (float) varargs.optdouble(3, 1);
                            targetSelector.resolve(context.hookContext(), radius).forEach(
                                    lv -> lv.setSecondsOnFire(seconds)
                            );
                            return LuaValue.NONE;
                        }
                    });


                }
        );
    }

    private static TargetSelector parseTarget(String value) {
        try {
            return TargetSelector.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new LuaError(
                    "Unknown target '" + value
                            + "', expected SHOOTER, VICTIM or NEARBY"
            );
        }
    }
}
