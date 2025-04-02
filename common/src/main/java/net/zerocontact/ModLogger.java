package net.zerocontact;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModLogger {
    public static final Logger LOG = LogManager.getLogger(ModLogger.class);
    public static void init(){
        LOG.info("初始化");
    }
}
