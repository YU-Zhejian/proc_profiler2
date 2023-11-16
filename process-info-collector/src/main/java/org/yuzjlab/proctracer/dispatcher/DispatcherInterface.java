package org.yuzjlab.proctracer.dispatcher;

import org.apache.commons.configuration2.Configuration;
import org.yuzjlab.proctracer.utils.ThreadInterface;

public interface DispatcherInterface extends ThreadInterface {
  Configuration getDefaultConfig();
}
