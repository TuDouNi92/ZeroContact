package net.zerocontact.armor.modular.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.zerocontact.mixin.minecraft.CompositeRenderTypeAccessor;
import net.zerocontact.mixin.minecraft.CompositeStateAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Reuses vanilla's alpha-tested, unlit silhouette shader without its outline target.
 */
final class ThermalRenderType extends RenderType {
    private static final Map<RenderType, RenderType> TYPES = new WeakHashMap<>();
    private static final OutputStateShard CURRENT_TARGET =
            new OutputStateShard("thermal_current_target", () -> {
            }, () -> {
            });

    private ThermalRenderType() {
        super("thermal", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
                256, false, false, () -> {
                }, () -> {
                });
    }

    @Nullable
    static RenderType from(RenderType original) {
        if (!(original instanceof CompositeRenderTypeAccessor accessor)
                || original.mode() != VertexFormat.Mode.QUADS) return null;
        CompositeStateAccessor state = (CompositeStateAccessor) (Object) accessor.zerocontact$getState();
        // Outline eligibility selects ordinary model geometry and excludes text,
        // shadows, glint, debug lines and other auxiliary passes. Armor is also a
        // surface, but vanilla deliberately disables its outline eligibility.
        if (state != null && original.outline().isEmpty()
                && state.zerocontact$getShaderState() != RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER) return null;
        return TYPES.computeIfAbsent(original, ignored -> {
            if (state != null) {
                return create(
                        "zerocontact_thermal", DefaultVertexFormat.POSITION_COLOR_TEX,
                        VertexFormat.Mode.QUADS, 256, false, false,
                        CompositeState.builder()
                                .setShaderState(RENDERTYPE_OUTLINE_SHADER)
                                .setTextureState(state.zerocontact$getTextureState())
                                .setCullState(state.zerocontact$getCullState())
                                .setDepthTestState(LEQUAL_DEPTH_TEST)
                                .setWriteMaskState(COLOR_WRITE)
                                .setTransparencyState(NO_TRANSPARENCY)
                                .setOutputState(CURRENT_TARGET)
                                .createCompositeState(false));
            }
            return null;
        });
    }
}
