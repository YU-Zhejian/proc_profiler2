package org.yuzjlab.proctracer.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

public class ConfigurationManager {
    protected final String className;
    public static final Set<String> ALL_CONFIGURABLE =
            Set.of(
                    "org.yuzjlab.proctracer.dispatcher.sys.SystemMemoryTracer",
                    "org.yuzjlab.proctracer.dispatcher.MainDispatcher",
                    "org.yuzjlab.proctracer.dispatcher.proc.ProcessMainDispatcher",
                    "org.yuzjlab.proctracer.dispatcher.sys.SystemMainDispatcher",
                    "org.yuzjlab.proctracer.frontend.BaseFrontend");

    public static Configuration getDefaultConfig(Class<?> cls) {
        var className = cls.getCanonicalName();
        try {
            var retHM = new HashMap<String, Object>();
            var gdc = cls.getField("DEFAULT_CONFIG");
            Object defConfObj = gdc.get(null);
            assert defConfObj instanceof Map;
            var mc = new HashMap<>((Map<String, Object>) defConfObj);
            for (var kv : mc.entrySet()) {
                retHM.put("%s.%s".formatted(className, kv.getKey()), kv.getValue());
            }
            return new MapConfiguration(retHM);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to get default configuration from class %s"
                            .formatted(cls.getCanonicalName()));
        }
    }

    public ConfigurationManager(Configuration config, Class<?> cls) {
        this.config = config;
        this.defaultConfig = getDefaultConfig(cls);
        this.className = cls.getCanonicalName();
    }

    protected Configuration config;
    private final Configuration defaultConfig;

    public <T> T getConfigWithDefaults(Class<T> cls, String localKey) {
        var canonicalKey = "%s.%s".formatted(this.className, localKey);
        if (!this.defaultConfig.containsKey(canonicalKey)) {
            throw new RuntimeException(
                    "Key %s not contained in default config of class %s!"
                            .formatted(canonicalKey, this.className));
        }
        return this.config.get(cls, canonicalKey, this.defaultConfig.get(cls, canonicalKey));
    }

    public <T> List<T> getListConfigWithDefaults(Class<T> cls, String localKey) {
        var canonicalKey = "%s.%s".formatted(this.className, localKey);
        if (!this.defaultConfig.containsKey(canonicalKey)) {
            throw new RuntimeException(
                    "Key %s not contained in default config of class %s!"
                            .formatted(canonicalKey, this.className));
        }
        return this.config.getList(cls, canonicalKey, defaultConfig.getList(cls, canonicalKey));
    }
}
