package net.zerocontact.api;
@Deprecated
public interface DurabilityLossProvider {
    default int generateLoss(float damageAmount, float durabilityLossFactor, int hits){
        return (int) Math.round(0.4 * Math.pow(damageAmount * durabilityLossFactor, 1.5) * (1 + hits * 0.1f));
    }
}
