package net.zerocontact.item.helmet;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.client.renderer.HelmetRender;
import net.zerocontact.item.armor.forge.BaseArmorGeoImpl;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.zerocontact.ZeroContact.MOD_ID;

public class Bastion extends BaseGeoHelmet {
    private static final ResourceLocation  texture= new ResourceLocation(MOD_ID,"textures/models/helmet/helmet_bastion_black.png") ;
    private static final ResourceLocation  model= new ResourceLocation(MOD_ID,"geo/helmet_bastion_black.geo.json") ;
    private static final ResourceLocation  animation= null;
    public Bastion(int absorb,int durability) {
        super(absorb,durability,texture,model,animation);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HelmetRender.HelmetArmorRender<BaseArmorGeoImpl> render;
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if(render==null){
                    this.render = new HelmetRender.HelmetArmorRender<>(new GenerateModel<>(texture, model, animation));
                }
                render.prepForRender(livingEntity,itemStack,equipmentSlot,original);
                return render;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new HelmetRender.HelmetItemRender<>(new GenerateModel<>(texture, model, animation));
            }
        });
    }
}
