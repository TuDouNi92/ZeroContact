package net.zerocontact.events;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.zerocontact.item.dogtag.BaseDogTag;

public class EntityDeathDogTagEvent {
    public static void register() {
        EntityEvent.LIVING_DEATH.register((livingEntity, damageSource) -> {
            if (BaseDogTag.spawnTagOnKillEntity(livingEntity, damageSource)) return EventResult.pass();
            return EventResult.pass();
        });
    }
}
