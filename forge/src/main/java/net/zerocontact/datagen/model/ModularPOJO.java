package net.zerocontact.datagen.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.zerocontact.ZeroContact;
import net.zerocontact.armor.modular.model.MountCategory;

import java.util.Arrays;

public class ModularPOJO extends ItemPOJO {
    public String id;
    @SerializedName("mount_type")
    public String mountCategory;
    @SerializedName("module_trait")
    public String moduleTrait;
    public int durability;
    public String texture = "";
    public String model = "";
    public String animation = "";

    public ResourceLocation getTrait() {
        return new ResourceLocation(ZeroContact.MOD_ID, moduleTrait);
    }

    public MountCategory getMountCategory() {
        return Arrays.stream(MountCategory.values())
                .toList()
                .stream()
                .filter(type -> type.name().equals(mountCategory.toUpperCase()))
                .findFirst()
                .orElse(MountCategory.UNDEFINED);
    }
}
