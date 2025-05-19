package net.zerocontact.api;

public interface EntityHurtProvider {
    default float generateRicochet(){
        return 0.05f;
    }
    default float generatePenetrated(){
        return 0.7f;
    }
    default float generateBlunt(){
        return 0.1f;
    }
}
