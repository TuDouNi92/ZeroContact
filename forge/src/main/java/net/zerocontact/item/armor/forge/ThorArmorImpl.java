package net.zerocontact.item.armor.forge;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.zerocontact.client.renderer.ArmorRender;
import net.zerocontact.models.GenerateModel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.zerocontact.ZeroContact.MOD_ID;

public class ThorArmorImpl extends BaseArmorGeoImpl {
    private static final ResourceLocation texture = new ResourceLocation(MOD_ID,"textures/models/armor/vest_thor_black.png");
    private static final ResourceLocation model = new ResourceLocation(MOD_ID,"geo/vest_thor_black.geo.json");
    private static final ResourceLocation animation=null;
    public ThorArmorImpl(int defense, int defaultDurability) {
        super(Type.CHESTPLATE, "", defense, defaultDurability, texture, model, animation);
    }
    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ArmorRender<BaseArmorGeoImpl> render;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if(render==null){
                    this.render = new ArmorRender<>(new GenerateModel<>(texture, model, animation));
                }
                render.prepForRender(livingEntity,itemStack,equipmentSlot,original);
                return render;
            }
        });
    }
}
