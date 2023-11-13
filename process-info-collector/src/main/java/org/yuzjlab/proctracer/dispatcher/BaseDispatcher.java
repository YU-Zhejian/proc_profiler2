package org.yuzjlab.proctracer.dispatcher;

import org.apache.commons.configuration2.Configuration;

import java.util.Map;

public abstract class BaseDispatcher extends Thread implements DispatcherInterface {
    protected Configuration config;
    protected boolean shouldStop;
    private BaseDispatcher(){}
    public BaseDispatcher(Configuration allConfig){
        var probableConfig = allConfig.subset(this.getClass().getCanonicalName());
        if (probableConfig == null || probableConfig.isEmpty()){
            this.config = this.getDefaultConfig();
        }
        else{
            this.config = probableConfig;
        }
    }

    public void terminate() {
        this.shouldStop = true;
    }

    abstract protected void probe();
    abstract protected Map<String, String> frontendFetch();

    public void run(){
        var sleepTime = (int)(this.config.getFloat("interval") * 1000);
        while (! this.shouldStop){
            this.probe();
            try{
                synchronized (this){
                    this.wait(sleepTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
