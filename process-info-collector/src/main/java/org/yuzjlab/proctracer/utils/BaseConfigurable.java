package org.yuzjlab.proctracer.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

public abstract class BaseConfigurable {
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
            throw new RuntimeException(e);
        }
    }

    protected BaseConfigurable(Configuration config) {
        this.config = config;
        this.defaultConfig = getDefaultConfig(this.getClass());
    }

    protected Configuration config;
    private final Configuration defaultConfig;

    protected <T> T getConfigWithDefaults(Class<T> cls, String localKey) {
        var canonicalKey = "%s.%s".formatted(this.getClass().getCanonicalName(), localKey);
        return this.config.get(cls, canonicalKey, defaultConfig.get(cls, canonicalKey));
    }

    protected <T> List<T> getListConfigWithDefaults(Class<T> cls, String localKey) {
        var canonicalKey = "%s.%s".formatted(this.getClass().getCanonicalName(), localKey);
        return this.config.getList(cls, canonicalKey, defaultConfig.getList(cls, canonicalKey));
    }
}
