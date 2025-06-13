package net.zerocontact.registries;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Random;
import java.util.Set;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ModSoundEventsReg {
    public static final SoundEvent ARMOR_EQUIP_PLATE = SoundEvent.createFixedRangeEvent(new ResourceLocation(MOD_ID, "armor/plate_equip"), 8f);
    public static final SoundEvent ARMOR_HIT_PLATE = SoundEvent.createFixedRangeEvent(new ResourceLocation(MOD_ID, "armor/plate_hit"), 22f);
    public static final SoundEvent ARMOR_BROKEN_PLATE = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "armor/plate_broke"));
    public static final SoundEvent RAIDER_CONTACT = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "usec1_enemy_contact_01_n_01"));
    public static final SoundEvent RAIDER_RELOAD = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "usec1_weap_reload_01_n_01"));
    public static final SoundEvent BULLET_SOUND_1 = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "bullet_1"));
    public static final SoundEvent BULLET_SOUND_2 = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "bullet_2"));
    public static final SoundEvent BULLET_SOUND_3 = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "bullet_3"));
    public static final SoundEvent BULLET_SOUND_4 = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "bullet_4"));
    public static final SoundEvent BULLET_SOUND_5 = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "bullet_5"));
    public static final SoundEvent BULLET_SOUND_6 = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "bullet_6"));
    public static final Random random = new Random();
    public static SoundEvent randomBulletSound() {
        Set<SoundEvent> BULLET_SOUNDS =
                Set.of(BULLET_SOUND_1, BULLET_SOUND_2, BULLET_SOUND_3, BULLET_SOUND_4, BULLET_SOUND_5, BULLET_SOUND_6);
        return BULLET_SOUNDS.stream().toList().get(random.nextInt(BULLET_SOUNDS.size()));
    }
}
