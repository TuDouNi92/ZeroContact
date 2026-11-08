package net.zerocontact.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

public class EventUtil {

    public static boolean isLookAtTargetBack(ServerPlayer player, @Nullable LivingEntity target) {
        if (target == null) return false;
        Vec3 look = player.position().subtract(target.position()).normalize();
        float yRot = target.getYRot();
        Vec3 targetForward = Vec3.directionFromRotation(0, yRot).normalize();
        Vec3 targetBack = targetForward.scale(-1);
        double dot = look.dot(targetBack);
        return dot > 0;
    }

    public static ItemStack getCuriosStackFirst(LivingEntity player, String id) {
        ItemStack[] stacks = {ItemStack.EMPTY};
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.getStacksHandler(id).ifPresent(iCurioStacksHandler -> {
            ItemStack stack = iCurioStacksHandler.getStacks().getStackInSlot(0);
            stacks[0] = stack;
        }));
        return stacks[0];
    }
}
