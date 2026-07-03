package net.zerocontact.client.renderer;

import net.minecraft.world.item.ItemStack;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.events.EventUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;

public class ArmedRaiderBackpackLayer extends ArmedRaiderArmorLayer {
    protected static final String BACKPACK_CURIO = "backpack";

    public ArmedRaiderBackpackLayer(GeoRenderer<ArmedRaider> geoRenderer) {
        super(geoRenderer);
    }

    @Override
    protected @Nullable ItemStack getArmorItemForBone(GeoBone bone, ArmedRaider animatable) {
        if (bone.getName().equals(ARMOR_BONE)) {
            ItemStack stack = EventUtil.getCuriosStackFirst(animatable, BACKPACK_CURIO);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return super.getArmorItemForBone(bone, animatable);
    }
}
