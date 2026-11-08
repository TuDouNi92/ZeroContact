package net.zerocontact.compat;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.zerocontact.events.ResolveHitBodyPartEvent.HitPartEnum;
import org.jetbrains.annotations.Nullable;
import ru.ranazy.tacz_firstaid_compat.compat.firstaid.BodypartHitbox;
import ru.ranazy.tacz_firstaid_compat.compat.firstaid.CoordinateTransform;

public class FirstAidCompatCompat {

    public static @Nullable HitPartEnum resolvePart(Vec3 hitPosition, ServerPlayer player) {
        Vec3 localHit = CoordinateTransform.worldToLocal(hitPosition, player);
        EnumPlayerPart part = BodypartHitbox.getHitPart(localHit);
        if (part == null) {
            part = BodypartHitbox.getClosestPart(localHit);
        }
        if (part == null) return null;
        return toHitPart(part);
    }

    public static HitPartEnum toHitPart(EnumPlayerPart part) {
        return switch (part) {
            case HEAD -> HitPartEnum.HEAD;
            case BODY -> HitPartEnum.TORSO;
            case LEFT_ARM, RIGHT_ARM -> HitPartEnum.ARM;
            case LEFT_LEG, RIGHT_LEG, LEFT_FOOT, RIGHT_FOOT -> HitPartEnum.LEG;
        };
    }

}
