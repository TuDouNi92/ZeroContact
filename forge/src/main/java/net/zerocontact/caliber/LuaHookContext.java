package net.zerocontact.caliber;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public record LuaHookContext(
        ResourceLocation scriptId,
        String function,
        HookEventTrigger trigger,
        HookContext hookContext,
        Map<String, JsonElement> arguments
) {
    public LuaHookContext {
        Objects.requireNonNull(scriptId, "scriptId");
        Objects.requireNonNull(function, "function");
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(hookContext, "hookContext");
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }

    public ServerLevel level() {
        return hookContext.level();
    }

    public @Nullable LivingEntity shooter() {
        return hookContext.shooter();
    }

    public @Nullable LivingEntity victim() {
        return hookContext.victim();
    }

    public Vec3 position() {
        return hookContext.positon();
    }

    public @Nullable Vec3 prevPosition() {
        return hookContext.prevPos();
    }

    public boolean booleanArgument(String name, boolean fallback) {
        JsonElement value = arguments.get(name);
        return value == null || !value.isJsonPrimitive()
                ? fallback
                : value.getAsBoolean();
    }

    public int intArgument(String name, int fallback) {
        JsonElement value = arguments.get(name);
        return value == null || !value.isJsonPrimitive()
                ? fallback
                : value.getAsInt();
    }

    public float floatArgument(String name, float fallback) {
        JsonElement value = arguments.get(name);
        return value == null || !value.isJsonPrimitive()
                ? fallback
                : value.getAsFloat();
    }

    public String stringArgument(String name, String fallback) {
        JsonElement value = arguments.get(name);
        return value == null || !value.isJsonPrimitive()
                ? fallback
                : value.getAsString();
    }
}
