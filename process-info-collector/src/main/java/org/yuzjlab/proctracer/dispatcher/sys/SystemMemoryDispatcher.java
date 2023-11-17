package org.yuzjlab.proctracer.dispatcher.sys;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;
import org.yuzjlab.proctracer.dispatcher.MainDispatcher;

public class SystemMemoryDispatcher extends BaseDispatcher {
    public SystemMemoryDispatcher(Configuration allConfig) {
        super(allConfig);
        this.defaultConfig = SystemMemoryDispatcher.getDefaultConfig();
    }

    public static  Configuration getDefaultConfig() {
        return new MapConfiguration(
                Stream.of(new AbstractMap.SimpleEntry<>("%s.interval".formatted(SystemMemoryDispatcher.class.getCanonicalName()), 0.01))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    protected void probe() {}

    @Override
    protected Map<String, String> frontendFetch() {
        return null;
    }
}
