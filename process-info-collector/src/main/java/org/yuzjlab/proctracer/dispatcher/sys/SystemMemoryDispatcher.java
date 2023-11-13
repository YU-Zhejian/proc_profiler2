package org.yuzjlab.proctracer.dispatcher.sys;

import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.jffi.NativeMemoryManager;
import jnr.posix.POSIXFactory;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationMap;
import org.apache.commons.configuration2.MapConfiguration;
import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;

import java.lang.management.MemoryUsage;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemMemoryDispatcher extends BaseDispatcher {
    public SystemMemoryDispatcher(Configuration allConfig) {
        super(allConfig);


    }

    @Override
    public Configuration getDefaultConfig() {
        return new MapConfiguration(Stream.of(
                        new AbstractMap.SimpleEntry<>("interval", 0.1)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    protected void probe() {

    }

    @Override
    protected Map<String, String> frontendFetch() {
        return null;
    }
}
