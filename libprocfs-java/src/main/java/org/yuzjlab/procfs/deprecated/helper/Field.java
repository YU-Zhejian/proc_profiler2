package org.yuzjlab.procfs.deprecated.helper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.yuzjlab.procfs.exception.ProcessBaseException;

public class Field<T> {
    private final Callback<T> callback;
    private final float expireTime;
    private float lastUpdated;
    private T value;

    public Field(Callback<T> callback, float expireTime) {
        this.callback = callback;
        this.lastUpdated = 0;
        this.expireTime = expireTime;
    }

    public T get() throws ProcessBaseException {
        var now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        if (now - this.lastUpdated < expireTime) {
            this.value = this.callback.call();
            this.lastUpdated = now;
        }
        return this.value;
    }
}
