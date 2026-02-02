
package net.zerocontact.animation_data;

import org.jetbrains.annotations.Nullable;

public class AnimateData {
    static public class VisorAnimateData {
        public String animationName;
        public double tick;
        public boolean isPlaying;
        public double animLength;
        public VisorAnimateData(String animationName, double tick,double animLength, boolean isPlaying) {
            this.animationName = animationName;
            this.tick = tick;
            this.isPlaying = isPlaying;
            this.animLength = animLength;
        }

        public static VisorAnimateData create(@Nullable String animationName, double tick,double animLength, boolean isPlaying) {
            if (animationName == null) return new VisorAnimateData("null", 0, 0,false);
            return new VisorAnimateData(animationName, tick,animLength, isPlaying);
        }

        public void set(String animationName, double tick, boolean isPlaying) {
            this.animationName = animationName;
            this.tick = tick;
            this.isPlaying = isPlaying;
        }

        ;
    }

}
