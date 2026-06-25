package net.zerocontact.compat;

import com.tacz.guns.entity.EntityKineticBullet;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.events.EventUtil;
import org.jetbrains.annotations.Nullable;
import ru.ranazy.tacz_firstaid_compat.compat.firstaid.BodypartHitbox;
import ru.ranazy.tacz_firstaid_compat.compat.firstaid.CoordinateTransform;

public class FirstAidCompatCompat {

    private static @Nullable EnumPlayerPart getPart(DamageSource source, ServerPlayer player) {
        EntityKineticBullet.@Nullable EntityResult hitResult = EventUtil.getHitResult(source);
        if (hitResult == null) return null;
        Vec3 localHit = CoordinateTransform.worldToLocal(hitResult.getHitPos(), player);
        EnumPlayerPart part = BodypartHitbox.getHitPart(localHit);
        if (part == null) {
            part = BodypartHitbox.getClosestPart(localHit);
        }
        return part;
    }

    public static boolean shouldBypassTorsoDamage(DamageSource source, ServerPlayer player) {
        EnumPlayerPart part = getPart(source, player);
        if (part == null) return false;
        return part != EnumPlayerPart.BODY && part != EnumPlayerPart.HEAD;
    }

    public static boolean shouldInterceptHeadDamage(DamageSource source, ServerPlayer player) {
        EnumPlayerPart part = getPart(source, player);
        if (part == null) return false;
        return part == EnumPlayerPart.HEAD;
    }

}
