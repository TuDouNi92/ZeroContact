package net.zerocontact.armor.modular.module.nvg.client;

/** Dependency-free regression checks. Run main with NvgExposureState on the classpath. */
public final class NvgExposureStateTest {
    private static final float NIGHT = 0.2F;
    private static final float DAY = 1.0F;
    private static int checks;

    public static void main(String[] args) {
        // Open sky can have raw SKY light 15 in both scenarios. Only the live sky factor differs.
        var openNight = sample(0.0F, true, NIGHT);
        var openDay = sample(0.0F, true, DAY);
        check(openNight.gain(1) == 12.0F, "outdoor night without block light has its own maximum gain of 12");
        check(openDay.gain(1) > 0.0F && openDay.gain(1) < openNight.gain(1),
                "clear outdoor daylight still suppresses gain");
        check(openNight.gain(0) == openNight.gain(1), "initialization has no stale interpolation");

        var nightLamp = sample(15.0F, true, NIGHT);
        var indoorLamp = sample(15.0F, false, NIGHT);
        check(nightLamp.gain(1) < openNight.gain(1) && nightLamp.gain(1) >= 6.0F,
                "full block light outdoors at night reduces gain but retains useful amplification");
        check(indoorLamp.gain(1) < nightLamp.gain(1),
                "fully lit interiors can reduce gain further than outdoor-night lamps");
        var nearbyNightLamp = sample(6.0F, true, NIGHT);
        check(nearbyNightLamp.gain(1) > nightLamp.gain(1)
                        && nearbyNightLamp.gain(1) < openNight.gain(1),
                "light near a lamp reduces outdoor-night gain proportionally");

        var darkRoomDay = sample(0.0F, false, DAY);
        var darkRoomNight = sample(0.0F, false, NIGHT);
        check(darkRoomDay.gain(1) == 48.0F && darkRoomDay.gain(1) > openNight.gain(1)
                        && darkRoomDay.gain(1) == darkRoomNight.gain(1),
                "dark interiors receive high gain regardless of time");
        check(sample(5.0F, false, DAY).gain(1) == sample(5.0F, false, NIGHT).gain(1),
                "indoor gain depends on block light, not the outdoor sky factor");

        check(sample(2, true, NIGHT).gain(1) > openNight.gain(1) * 0.95F,
                "very weak block light retains most outdoor-night gain");
        float outdoorDrop = sample(4, true, NIGHT).gain(1) - sample(6, true, NIGHT).gain(1);
        float indoorDrop = sample(4, false, NIGHT).gain(1) - sample(6, false, NIGHT).gain(1);
        check(outdoorDrop > (openNight.gain(1) - nightLamp.gain(1)) * 0.5F,
                "outdoor gain loses over half its range near block-light level five");
        check(indoorDrop > (darkRoomNight.gain(1) - indoorLamp.gain(1)) * 0.5F,
                "indoor gain also falls rapidly near block-light level five");
        check(sample(8, false, NIGHT).gain(1) - indoorLamp.gain(1) < 0.1F,
                "indoor gain is already near minimum above the transition range");

        var leavingRoom = sample(0, false, NIGHT);
        leavingRoom.tick(0, true, NIGHT);
        check(leavingRoom.gain(0) == darkRoomNight.gain(1)
                        && leavingRoom.gain(1) > openNight.gain(1)
                        && leavingRoom.gain(1) < darkRoomNight.gain(1),
                "leaving a dark room preserves the last exposure and approaches the outdoor target");
        check(leavingRoom.gain(0.5F) < leavingRoom.gain(0)
                        && leavingRoom.gain(0.5F) > leavingRoom.gain(1),
                "doorway transition is interpolated without an instantaneous clamp");
        for (int i = 1; i < 20; i++) leavingRoom.tick(0, true, NIGHT);
        check(leavingRoom.gain(1) > openNight.gain(1)
                        && leavingRoom.gain(1) < darkRoomNight.gain(1),
                "outdoor adaptation progresses over time instead of snapping to its limit");
        float doorwayGain = leavingRoom.gain(1);
        leavingRoom.tick(0, false, NIGHT);
        check(leavingRoom.gain(0) == doorwayGain && leavingRoom.gain(1) > doorwayGain
                        && leavingRoom.gain(1) < darkRoomNight.gain(1),
                "returning indoors mid-transition continues from the current gain");
        for (int i = 0; i < 200; i++) leavingRoom.tick(0, true, NIGHT);
        check(Math.abs(leavingRoom.gain(1) - openNight.gain(1)) < 0.01F,
                "outdoor adaptation eventually converges to the independent gain limit");

        float previousOutdoor = openNight.gain(1);
        float previousIndoor = darkRoomDay.gain(1);
        float previousDaylight = openNight.gain(1);
        for (int i = 0; i <= 100; i++) {
            float brightness = i / 100.0F;
            float outdoor = sample(brightness * 15.0F, true, NIGHT).gain(1);
            float indoor = sample(brightness * 15.0F, false, DAY).gain(1);
            float daylight = sample(0, true, NIGHT + (DAY - NIGHT) * brightness).gain(1);
            check(outdoor <= previousOutdoor && outdoor >= nightLamp.gain(1),
                    "more block light monotonically reduces outdoor-night gain without over-suppression");
            check(indoor <= previousIndoor && indoor >= indoorLamp.gain(1),
                    "more block light monotonically reduces indoor gain");
            check(daylight <= previousDaylight && daylight >= openDay.gain(1),
                    "dawn transitions continuously from night gain to daylight gain");
            previousOutdoor = outdoor;
            previousIndoor = indoor;
            previousDaylight = daylight;
        }

        var leavingDark = sample(0, true, NIGHT);
        var enteringDark = sample(15, true, NIGHT);
        leavingDark.tick(15, true, NIGHT);
        enteringDark.tick(0, true, NIGHT);
        check(leavingDark.gain(1) < openNight.gain(1) && leavingDark.gain(1) > nightLamp.gain(1)
                        && enteringDark.gain(1) > nightLamp.gain(1) && enteringDark.gain(1) < openNight.gain(1),
                "gain reduction and recovery both approach their targets smoothly");
        check(leavingDark.gain(0) == openNight.gain(1)
                        && leavingDark.gain(0.5F) > leavingDark.gain(1)
                        && leavingDark.gain(0.5F) < leavingDark.gain(0),
                "render interpolation smoothly bridges exposure ticks");
        check(leavingDark.gain(-1) == leavingDark.gain(0)
                        && leavingDark.gain(2) == leavingDark.gain(1),
                "partial ticks are clamped");
        for (int i = 0; i < 200; i++) {
            leavingDark.tick(15, true, NIGHT);
            enteringDark.tick(0, true, NIGHT);
        }
        check(Math.abs(leavingDark.gain(1) - nightLamp.gain(1)) < 0.001F
                        && Math.abs(enteringDark.gain(1) - openNight.gain(1)) < 0.01F,
                "adaptation converges to both outdoor-night targets");

        var sunset = sample(0, true, DAY);
        for (int i = 0; i < 200; i++) sunset.tick(0, true, NIGHT);
        check(Math.abs(sunset.gain(1) - openNight.gain(1)) < 0.01F,
                "nightfall restores high gain without moving or changing raw sky light");
        for (int i = 0; i < 200; i++) sunset.tick(0, true, DAY);
        check(Math.abs(sunset.gain(1) - openDay.gain(1)) < 0.05F,
                "daylight adaptation converges to its target");

        sunset.reset();
        sunset.tick(0, false, DAY);
        check(sunset.gain(0) == darkRoomDay.gain(1) && sunset.gain(1) == darkRoomDay.gain(1),
                "reset initializes directly from the new environment");
        sunset.tick(Float.NaN, true, NIGHT);
        sunset.tick(0, true, Float.POSITIVE_INFINITY);
        check(sunset.gain(0.5F) == darkRoomDay.gain(1), "invalid samples do not poison exposure");
        check(sample(-1, true, -1).gain(1) == openNight.gain(1)
                        && sample(20, true, 2).gain(1) == openDay.gain(1),
                "out-of-range light input is clamped");
        System.out.println("NvgExposureState: " + checks + " regression checks passed");
    }

    private static NvgExposureState sample(float block, boolean outdoors, float sky) {
        var exposure = new NvgExposureState();
        exposure.tick(block, outdoors, sky);
        return exposure;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
