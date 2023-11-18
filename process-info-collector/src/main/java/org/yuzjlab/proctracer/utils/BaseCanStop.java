package org.yuzjlab.proctracer.utils;

public class BaseCanStop implements CanStopInterface {

    protected boolean shouldStop;

    public boolean getShouldStop() {
        return this.shouldStop;
    }

    public void setShouldStop() {
        this.shouldStop = true;
    }
}
