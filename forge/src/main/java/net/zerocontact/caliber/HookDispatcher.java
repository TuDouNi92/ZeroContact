package net.zerocontact.caliber;

import net.zerocontact.datagen.AmmoDataPOJO;
import net.zerocontact.lua.ZCLuaEngine;

public final class HookDispatcher {
    public static void fire(HookEventTrigger trigger, HookContext context) {
        if (context.level().isClientSide()) {
            return;
        }

        for (AmmoDataPOJO.EventHook hook : context.caliber().hooks()) {
            if (hook.trigger() != trigger) {
                continue;
            }

            for (AmmoDataPOJO.HookActionData action : hook.actions()) {
                HookActionExecutor.execute(action, context);
            }
            for (AmmoDataPOJO.LuaHookData luaScript : hook.scripts()) {
                ZCLuaEngine.getInstance().invoke(
                        luaScript,
                        trigger,
                        context
                );
            }
        }
    }
}