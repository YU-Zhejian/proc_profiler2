package org.yuzjlab.procfs.exception;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

public class ProcessExceptionFactory {
    public static ProcessBaseException convertIOException(FileNotFoundException e) {
        return new ProcessNotExistException(e);
    }

    public static ProcessBaseException convertIOException(AccessDeniedException e) {
        return new ProcessPermissionDeniedException(e);
    }

    public static ProcessBaseException convertIOException(IOException e) {
        return new ProcessUnknownException(e);
    }

    public static void resolveIOException(IOException e) throws ProcessBaseException {
        throw ProcessExceptionFactory.convertIOException(e);
    }
}
