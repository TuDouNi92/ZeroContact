package net.zerocontact.item.forge;

import net.minecraft.world.item.ArmorMaterial;
import net.zerocontact.api.DurabilityLossProvider;
import net.zerocontact.api.EntityHurtProvider;
import net.zerocontact.api.PlateInfoProvider;
import net.zerocontact.item.Ceramic;
import org.jetbrains.annotations.NotNull;

public class CeramicImpl extends Ceramic implements EntityHurtProvider, DurabilityLossProvider, PlateInfoProvider {
    private final int defense;
    private final int absorb;
    private final float mass;
    private final Type type;
    private final ArmorMaterial material;

    public CeramicImpl(ArmorMaterial material, Type type, Properties properties, int defense, int absorb, float mass) {
        super(material, type, properties);
        this.defense = defense;
        this.absorb = absorb;
        this.mass = mass;
        this.type = type;
        this.material = material;
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public @NotNull ArmorMaterial getMaterial() {
        return material;
    }

    @Override
    public int getDefense() {
        return this.defense;
    }

    public static Ceramic create(ArmorMaterial material, Type type, Properties properties, int defense, int absorb, float mass) {
        return new CeramicImpl(material, type, properties, defense, absorb, mass);
    }

    @Override
    public int generateLoss(float damageAmount, float durabilityLossFactor, int hits) {
        return (int) Math.round(0.4 * Math.pow(damageAmount * durabilityLossFactor, 1.5) * (1 + hits * 0.5f));
    }

    @Override
    public float generatePenetrated() {
        return 0.5f;
    }

    @Override
    public float generateBlunt() {
        return 0.5f;
    }

    public float getMass() {
        return this.mass;
    }

    @Override
    public int getAbsorb() {
        return this.absorb;
    }
}
