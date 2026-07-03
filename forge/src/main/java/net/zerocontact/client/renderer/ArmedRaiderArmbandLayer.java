package net.zerocontact.client.renderer;

import net.minecraft.world.item.ItemStack;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.events.EventUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;

public class ArmedRaiderArmbandLayer extends ArmedRaiderArmorLayer{
    public ArmedRaiderArmbandLayer(GeoRenderer<ArmedRaider> geoRenderer) {
        super(geoRenderer);
    }

    @Override
    protected @Nullable ItemStack getArmorItemForBone(GeoBone bone, ArmedRaider animatable) {
        if (bone.getName().equals(ARMOR_BONE)) {
            ItemStack stack = EventUtil.getCuriosStackFirst(animatable, ARMBAND_CURIO);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return super.getArmorItemForBone(bone, animatable);
    }
}
