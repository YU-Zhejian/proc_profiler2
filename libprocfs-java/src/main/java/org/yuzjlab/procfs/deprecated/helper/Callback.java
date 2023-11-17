package org.yuzjlab.procfs.deprecated.helper;

import org.yuzjlab.procfs.exception.ProcessBaseException;

public interface Callback<T> {
    T call() throws ProcessBaseException;
}
