package net.zerocontact.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.zerocontact.armor.modular.module.nvg.event.ThermalRenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void zerocontact$captureThermal(EntityRenderer<?> renderer, Entity entity,
            float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, Operation<Void> original) {
        original.call(renderer, entity, yaw, partialTick, poseStack,
                ThermalRenderHandler.captureEntity(entity, buffers), packedLight);
    }
}
