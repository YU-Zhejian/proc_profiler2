package org.yuzjlab.proctracer.dispatcher;

import java.util.Map;
import org.apache.commons.configuration2.Configuration;

public abstract class BaseDispatcher extends Thread implements DispatcherInterface {
  protected Configuration config;
  protected boolean shouldStop;

  private BaseDispatcher() {}

  protected BaseDispatcher(Configuration allConfig) {
    var probableConfig = allConfig.subset(this.getClass().getCanonicalName());
    if (probableConfig == null || probableConfig.isEmpty()) {
      this.config = this.getDefaultConfig();
    } else {
      this.config = probableConfig;
    }
  }

  public void terminate() {
    this.shouldStop = true;
  }

  protected abstract void probe();
  protected void setUp(){}
  protected void tearDown(){  }

  protected abstract Map<String, String> frontendFetch();

  public void run() {
    setUp();
    var sleepTime = (int) (this.config.getFloat("interval") * 1000);
    while (!this.shouldStop) {
      this.probe();
      try {
        synchronized (this) {
          this.wait(sleepTime);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    tearDown();
  }
}
