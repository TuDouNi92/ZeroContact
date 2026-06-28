package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;

public class ExperimentalBallisticData {
    @SerializedName("ammo_id")
    //The identifier of a type of ammo that has been registered in TACZ gun pack.
    public String ammoId;

    //The variant cartridge of this ammo, for example {hp}, any name you want.
    public String variant;

    @SerializedName("base_damage_factor")
    //Indicates a damage balancing factor on guns with the same caliber.
    public float baseDamageFactor;

    //The penetration against armors.
    @SerializedName("penetration_class")
    public int penetrationClass =10;
    @SerializedName("flesh_damage")
    //Flesh damage indicates the damage being produced through direct hit on penetrated armor, or direct hit on bodies.
    public float fleshDamage =4;

    //The percent of consumed armor durability. Leaving {0} means a default process.
    @SerializedName("armor_damage")
    public float armorDamage = 0;

    @SerializedName("stack_size")
    public int stackSize =30;

    //The color of this bullet tracer.
    @SerializedName("tracer_color")
    public int[] tracerColor = new int[]{255,255,255,255};

}
