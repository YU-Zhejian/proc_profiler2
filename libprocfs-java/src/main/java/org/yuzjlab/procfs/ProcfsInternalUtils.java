package org.yuzjlab.procfs;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.exception.ProcessNotExistException;
import org.yuzjlab.procfs.exception.ProcessPermissionDeniedException;
import org.yuzjlab.procfs.exception.ProcessUnknownException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

public class ProcfsInternalUtils {

    private ProcfsInternalUtils() {
    }

    public static ProcessBaseException resolveIOException(IOException e) {
        if (e instanceof FileNotFoundException fileNotFoundException) {
            return new ProcessNotExistException(fileNotFoundException);
        }
        if (e instanceof NoSuchFileException noSuchFileException) {
            return new ProcessNotExistException(noSuchFileException);
        }

        if (e instanceof AccessDeniedException accessDeniedException) {
            return new ProcessPermissionDeniedException(accessDeniedException);
        }
        return new ProcessUnknownException(e);
    }
}
