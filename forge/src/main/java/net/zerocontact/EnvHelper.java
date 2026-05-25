package net.zerocontact;

import java.lang.management.ManagementFactory;

public class EnvHelper {
    public static final boolean DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
}
