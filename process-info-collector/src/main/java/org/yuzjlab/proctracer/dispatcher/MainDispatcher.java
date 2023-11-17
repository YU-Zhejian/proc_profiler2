package org.yuzjlab.proctracer.dispatcher;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;

import java.util.Collections;
import java.util.Map;

public class MainDispatcher extends BaseDispatcher{
  ProcessSupervisorThreadInterface psst;


  public MainDispatcher(TracerOpts topt, ProcessSupervisorThreadInterface psst) {
    super(topt.getConfig());
    this.psst = psst;
    psst.start();
    topt.validate();
  }

  @Override
  protected void probe() {
    if(psst.getPid() == -1){
      this.terminate();
    }
  }

  /**
   * Defunct. Do not use.
   * @return An empty map.
   */
  @Override
  protected Map<String, String> frontendFetch() {
    return Collections.emptyMap();
  }

  @Override
  public Configuration getDefaultConfig() throws ConfigurationException {
    var className = TracerOpts.class.getCanonicalName();
    var defConfig = new BasicConfigurationBuilder<>(PropertiesConfiguration.class).getConfiguration();
    defConfig.setProperty("%s.interval".formatted(className), 0.01);
    return defConfig;
  }


}
