package org.yuzjlab.proctracer.dispatcher.sys;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;

public class SystemMemoryDispatcher extends BaseDispatcher {
  public SystemMemoryDispatcher(Configuration allConfig) {
    super(allConfig);
  }

  @Override
  public Configuration getDefaultConfig() {
    return new MapConfiguration(
        Stream.of(new AbstractMap.SimpleEntry<>("interval", 0.1))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  @Override
  protected void probe() {}

  @Override
  protected Map<String, String> frontendFetch() {
    return null;
  }
}
