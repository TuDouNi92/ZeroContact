package net.zerocontact.caliber;

public final class HookDispatcher {
    public static void fire(HookEventTrigger trigger, HookContext context) {
        if (context.level().isClientSide()) {
            return;
        }

        for (EventHook hook : context.caliber().hooks()) {
            if (hook.trigger() != trigger) {
                continue;
            }

            for (HookActionData action : hook.actions()) {
                HookActionExecutor.execute(action, context);
            }
        }
    }
}