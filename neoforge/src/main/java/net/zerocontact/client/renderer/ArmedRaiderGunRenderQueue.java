package net.zerocontact.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ArmedRaiderGunRenderQueue {
    private final List<GunRenderTask> tasks = new ArrayList<>();

    public void submit(ItemStack stack, PoseStack poseStack, ItemDisplayContext displayContext, int packedLight, int packedOverlay) {
        Matrix4f pose = new Matrix4f(poseStack.last().pose());
        Matrix3f normal = new Matrix3f(poseStack.last().normal());
        ItemStack copiedStack = stack.copy();

        tasks.add(new GunRenderTask(copiedStack, pose, normal, displayContext, packedLight, packedOverlay));
    }

    public void flush(MultiBufferSource bufferSource) {
        if (tasks.isEmpty()) {
            return;
        }

        List<GunRenderTask> copiedTasks = new ArrayList<>(tasks);
        tasks.clear();

        for (GunRenderTask task : copiedTasks) {
            task.render(bufferSource);
        }
    }

    public void clear() {
        tasks.clear();
    }

    private record GunRenderTask(ItemStack stack, Matrix4f pose, Matrix3f normal, ItemDisplayContext displayContext,
                                 int packedLight, int packedOverlay) {

        private void render(MultiBufferSource bufferSource) {
                if (stack.isEmpty()) {
                    return;
                }

                if (!(IClientItemExtensions.of(stack).getCustomRenderer() instanceof GunItemRendererWrapper renderer)) {
                    return;
                }

                PoseStack copiedPoseStack = new PoseStack();
                copiedPoseStack.last().pose().set(pose);
                copiedPoseStack.last().normal().set(normal);

                renderer.renderByItem(
                        stack,
                        displayContext,
                        copiedPoseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay
                );
            }
        }
}
