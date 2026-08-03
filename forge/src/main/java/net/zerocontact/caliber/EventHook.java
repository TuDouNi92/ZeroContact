package net.zerocontact.caliber;

import java.util.List;

public record EventHook(
        HookEventTrigger trigger,
        List<HookActionData> actions
) {
}
