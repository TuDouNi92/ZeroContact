package net.zerocontact.client.renderer;

import net.minecraft.world.item.ItemStack;
import net.zerocontact.curios.CuriosConstants;
import net.zerocontact.entity.ArmedRaider;
import net.zerocontact.events.EventUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;

public class ArmedRaiderBackpackLayer extends ArmedRaiderArmorLayer {
    public ArmedRaiderBackpackLayer(GeoRenderer<ArmedRaider> geoRenderer) {
        super(geoRenderer);
    }

    @Override
    protected @Nullable ItemStack getArmorItemForBone(GeoBone bone, ArmedRaider animatable) {
        if (bone.getName().equals(ARMOR_BONE)) {
            ItemStack stack = EventUtil.getCuriosStackFirst(animatable, CuriosConstants.BACKPACK);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return super.getArmorItemForBone(bone, animatable);
    }
}
