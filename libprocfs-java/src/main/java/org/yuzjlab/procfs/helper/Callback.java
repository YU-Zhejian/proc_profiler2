package org.yuzjlab.procfs.helper;


import org.yuzjlab.procfs.exception.ProcessBaseException;

public interface Callback<T> {
    T call() throws ProcessBaseException;
}
