
package net.zerocontact.animation_data;

import org.jetbrains.annotations.Nullable;

public class AnimateData {
    static public class VisorAnimateData {
        public String animationName;
        public boolean isPlaying;
        public double animLength;
        public VisorAnimateData(String animationName,double animLength, boolean isPlaying) {
            this.animationName = animationName;
            this.isPlaying = isPlaying;
            this.animLength = animLength;
        }

        public static VisorAnimateData create(@Nullable String animationName,double animLength, boolean isPlaying) {
            if (animationName == null) return new VisorAnimateData("empty",  0,false);
            return new VisorAnimateData(animationName, animLength, isPlaying);
        }

        public void set(String animationName,double animLength, boolean isPlaying) {
            this.animationName = animationName;
            this.isPlaying = isPlaying;
            this.animLength = animLength;
        }
    }

}
