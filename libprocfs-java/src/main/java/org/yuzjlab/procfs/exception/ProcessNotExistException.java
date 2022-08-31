package org.yuzjlab.procfs.exception;

import java.io.FileNotFoundException;

public class ProcessNotExistException extends ProcessBaseException {
    public ProcessNotExistException(FileNotFoundException e) {
    }
}
