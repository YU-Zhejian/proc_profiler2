package org.yuzjlab.proctracer.dispatcher;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.yuzjlab.proctracer.utils.ThreadInterface;

public interface DispatcherInterface extends ThreadInterface {
  Configuration getDefaultConfig() throws ConfigurationException;
}
