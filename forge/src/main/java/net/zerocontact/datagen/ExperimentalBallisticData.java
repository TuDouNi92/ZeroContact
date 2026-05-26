package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;

public class ExperimentalBallisticData {
    @SerializedName("ammo_id")
    //The identifier of a type of ammo that has been registered in TACZ gun pack;
    public String ammoId;

    //The variant of this ammo, for example {hp}, any name you want
    public String variant;

    @SerializedName("base_damage_factor")
    //Indicates a damage balancing factor on guns with the same caliber
    public float baseDamageFactor;
    @SerializedName("penetration_class")
    /*Penetration Class value ranges [10,60], the tens digit indicates the penetration class,
    * while the one's digit indicates the possibility of penetrating the next armor class.
    * */
    public int penetrationClass =10;
    @SerializedName("flesh_damage")
    //Flesh damage indicates the damage being produced through direct hit on penetrated armor, or direct hit on bodies.
    public float fleshDamage =4;

    @SerializedName("stack_size")
    public int stackSize =30;
}
