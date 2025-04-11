package net.zerocontact.registries;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ModSoundEventsReg {
    private static final DeferredRegister<SoundEvent> MOD_SOUND_REG = DeferredRegister.create(MOD_ID, Registries.SOUND_EVENT);
    public static final SoundEvent ARMOR_EQUIP_PLATE = SoundEvent.createFixedRangeEvent(new ResourceLocation(MOD_ID,"armor/plate_equip"),8f);
    public static final SoundEvent ARMOR_HIT_PLATE = SoundEvent.createFixedRangeEvent(new ResourceLocation(MOD_ID,"armor/plate_hit"),22f);
    public static final SoundEvent ARMOR_BROKEN_PLATE = SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID,"armor/plate_broke"));
    public static void register(){
        MOD_SOUND_REG.register();
    }
}
