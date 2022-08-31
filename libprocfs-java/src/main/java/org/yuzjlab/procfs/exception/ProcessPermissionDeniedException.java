package org.yuzjlab.procfs.exception;

import java.nio.file.AccessDeniedException;

public class ProcessPermissionDeniedException extends ProcessBaseException {
    public ProcessPermissionDeniedException(AccessDeniedException e) {
    }
}
