package net.zerocontact.armor.modular.module.headset.item;

import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;
import net.zerocontact.armor.modular.module.headset.model.AudioProfile;
import net.zerocontact.armor.modular.module.headset.model.EqualizerBand;
import net.zerocontact.armor.modular.registry.ModuleRegistry;

import java.util.List;

public class Amp extends Headset {
    private static final ResourceLocation model = new ResourceLocation(ZeroContact.MOD_ID, "geo/headset/amp.geo.json");
    private static final ResourceLocation animation = new ResourceLocation("");

    public enum Color {
        TAN(new ResourceLocation(ZeroContact.MOD_ID, "textures/models/headset/amp.png")),
        BLACK(new ResourceLocation(ZeroContact.MOD_ID, "textures/models/headset/amp_black.png"));
        private final ResourceLocation texture;

        Color(ResourceLocation texture) {
            this.texture = texture;
        }
    }

    public Amp(String id, Color color) {
        super("headset_amp", 0, color.texture, model, animation, new AudioProfile(
                10.0f,
                -6.0f,
                -12.0f,
                20f,
                List.of(
                        new EqualizerBand(125, -3, 1),
                        new EqualizerBand(500, -3, 1),
                        new EqualizerBand(2000, -5, 1),
                        new EqualizerBand(4000, -2, 1),
                        new EqualizerBand(8000, 5, 1)
                )
        ));
        ModuleRegistry.registerCategory(new ResourceLocation(ZeroContact.MOD_ID, id), MountCategory.HEADSET);
    }
}
