package org.yuzjlab.procfs;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.exception.ProcessExceptionFactory;
import org.yuzjlab.procfs.exception.ProcessUnknownException;

import java.io.IOException;
import java.nio.file.Path;

public class ProcessUtils {

    private ProcessUtils() {
    }

    public static long getCurrentPid() throws ProcessBaseException {
        try {
            return Long.parseLong(Path.of("/proc/self").toRealPath().getFileName().toString());
        } catch (NumberFormatException e) {
            throw new ProcessUnknownException(e);
        } catch (IOException e) {
            throw ProcessExceptionFactory.convertIOException(e);
        }
    }
}
