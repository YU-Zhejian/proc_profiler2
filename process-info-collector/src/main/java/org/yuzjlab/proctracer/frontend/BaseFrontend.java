package org.yuzjlab.proctracer.frontend;

import java.util.Map;
import org.yuzjlab.proctracer.dispatcher.DispatcherInterface;
import org.yuzjlab.proctracer.dispatcher.MainDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.utils.BaseCanStop;
import org.yuzjlab.proctracer.utils.ConfigurationManager;
import org.yuzjlab.proctracer.utils.LogManager;

public abstract class BaseFrontend extends BaseCanStop implements FrontendInterface {

    public static final Map<String, Object> DEFAULT_CONFIG = Map.of("interval", 1.0);
    protected Map<String, String> frontendStatus;
    protected final ConfigurationManager configurationManager;
    protected final DispatcherInterface mainDispatcher;
    protected final LogManager logManager;

    public BaseFrontend(TracerOpts topt, MainDispatcher mainDispatcher) {
        this.mainDispatcher = mainDispatcher;
        this.configurationManager = new ConfigurationManager(topt.getConfig(), BaseFrontend.class);
        this.logManager = new LogManager(this);
    }

    protected abstract void flush();

    protected void probe() {
        this.frontendStatus = this.mainDispatcher.recursiveFrontendFetch();
    }

    public void run() {
        var interval =
                (int)
                        (this.configurationManager.getConfigWithDefaults(Float.class, "interval")
                                * 1000);
        this.logManager.lh.info("Starting frontend");
        while (!this.shouldStop) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.probe();
            this.flush();
        }
        this.logManager.lh.info("Terminating frontend");
    }
}
