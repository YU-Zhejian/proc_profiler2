package org.yuzjlab.proctracer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogManager {

    public final Logger lh;

    public LogManager(Object target) {
        this.lh = LoggerFactory.getLogger(target.toString());
    }

    public void logError(Throwable e) {
        this.lh.debug("Failed because of {}", e.toString());
    }
}
