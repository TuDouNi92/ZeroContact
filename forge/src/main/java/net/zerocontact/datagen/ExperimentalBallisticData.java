package net.zerocontact.datagen;

import com.google.gson.annotations.SerializedName;

public class ExperimentalBallisticData {
    @SerializedName("ammo_id")
    //The identifier of a type of ammo that has been registered in TACZ gun pack;
    public String ammoId;
    @SerializedName("base_damage_factor")
    //Indicates damage balancing factor on guns with same caliber
    public int baseDamageFactor;
    @SerializedName("penetration_class")
    /*Penetration Class value ranges [10,60], the tens digit indicates the penetration class,
    * while the ones digit indicates the possibility of penetrating next armor class.
    * */
    public int penetrationClass =10;
    @SerializedName("flesh_damage")
    //Flesh damage indicates the damage being produced through direct hit on penetrated armor, or direct hit on bodies.
    public int fleshDamage =4;
}
