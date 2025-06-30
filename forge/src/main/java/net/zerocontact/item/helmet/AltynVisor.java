package net.zerocontact.item.helmet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.zerocontact.api.Togglable;
import org.jetbrains.annotations.Nullable;

import static net.zerocontact.ZeroContact.MOD_ID;

public class AltynVisor {
    public static class WithVisor extends BaseGeoHelmet implements Togglable {
        private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_altyn_visor_olive.png");
        private static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/helmet_altyn_enabled_visor_olive.geo.json");
        private static final ResourceLocation animation = null;

        public WithVisor(int absorb, int defaultDurability) {
            super(absorb, defaultDurability, texture, model, animation);
        }

        @Override
        public Item getToggleBrother() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(MOD_ID, "helmet_altyn"));
        }
    }

    public static class WithoutVisor extends BaseGeoHelmet implements Togglable {
        private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/models/helmet/helmet_altyn_olive.png");
        private static final ResourceLocation model = new ResourceLocation(MOD_ID, "geo/helmet_altyn_disabled_visor_olive.geo.json");
        private static final ResourceLocation animation = null;

        public WithoutVisor(int absorb, int defaultDurability) {
            super(absorb, defaultDurability, texture, model, animation);
        }

        @Override
        public Item getToggleBrother() {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(MOD_ID, "helmet_altyn_visor"));
        }
    }
}
