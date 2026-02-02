package net.zerocontact.client.animation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zerocontact.animation_data.AnimateData;
import software.bernie.geckolib.core.animation.AnimationState;

@OnlyIn(Dist.CLIENT)
public final class VisorTracker {
    private static final AnimateData.VisorAnimateData DATA = AnimateData.VisorAnimateData.create("",0.0,0,false);
    public static void update(AnimationState<?> state){
        DATA.set(
                state.getController().getCurrentAnimation().animation().name(),
                state.getAnimationTick(),
                state.getController().isPlayingTriggeredAnimation()
        );
    }
    public static void update(AnimateData.VisorAnimateData data){
        DATA.set(
                data.animationName,
                data.tick,
                data.isPlaying
        );
    }
}
