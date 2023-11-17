package org.yuzjlab.proctracer.dispatcher;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

import java.util.HashMap;

public interface DispatcherInterface extends Runnable {
    static Configuration getDefaultConfig() {
        return new MapConfiguration(new HashMap<>());
    }
}
