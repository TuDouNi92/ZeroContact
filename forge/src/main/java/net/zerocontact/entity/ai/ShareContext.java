package net.zerocontact.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.zerocontact.entity.ai.controller.GlobalStateController;

import java.util.HashSet;
import java.util.Set;

public class ShareContext {
    public final Set<GlobalStateController.SignalPhase> signalPhases;
    public boolean isHurt;
    public ShareContext(){
        this.signalPhases = new HashSet<>();
    }
}
