package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

import java.util.List;

public final class EquipmentPlayerPreview {
    private EquipmentPlayerPreview() {}

    public static List<EquipmentAnchorCapture.Anchor> render(GuiGraphics graphics, Player player,
            int centerX, int centerY, float scale, float yaw, float pitch) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Quaternionf oldCamera = new Quaternionf(dispatcher.cameraOrientation());
        float body = player.yBodyRot, oldBody = player.yBodyRotO;
        float head = player.yHeadRot, oldHead = player.yHeadRotO;
        float yRot = player.getYRot(), oldYRot = player.yRotO;
        float xRot = player.getXRot(), oldXRot = player.xRotO;
        graphics.flush();
        graphics.pose().pushPose();
        try (var capture = new EquipmentAnchorCapture(player)) {
            // Rotate around the player's centre, not around the feet or the real world camera.
            Quaternionf orbit = new Quaternionf().rotationX(pitch).rotateY(yaw);
            graphics.pose().translate(centerX, centerY, 100);
            graphics.pose().scale(scale, scale, -scale);
            graphics.pose().mulPose(new Quaternionf().rotationZ((float) Math.PI).mul(orbit));
            graphics.pose().translate(0, -player.getBbHeight() / 2f, 0);
            player.yBodyRot = player.yBodyRotO = 180;
            player.yHeadRot = player.yHeadRotO = 180;
            player.setYRot(180);
            player.yRotO = 180;
            player.setXRot(0);
            player.xRotO = 0;
            Lighting.setupForEntityInInventory();
            dispatcher.overrideCameraOrientation(new Quaternionf(orbit).conjugate());
            dispatcher.setRenderShadow(false);
            RenderSystem.runAsFancy(() -> dispatcher.render(player, 0, 0, 0, 0, 1,
                    graphics.pose(), graphics.bufferSource(), 15728880));
            graphics.flush();
            return capture.anchors();
        } finally {
            player.yBodyRot = body;
            player.yBodyRotO = oldBody;
            player.yHeadRot = head;
            player.yHeadRotO = oldHead;
            player.setYRot(yRot);
            player.yRotO = oldYRot;
            player.setXRot(xRot);
            player.xRotO = oldXRot;
            dispatcher.overrideCameraOrientation(oldCamera);
            dispatcher.setRenderShadow(true);
            graphics.pose().popPose();
            Lighting.setupFor3DItems();
        }
    }
}
