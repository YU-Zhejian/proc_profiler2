package org.yuzjlab.procfs.exception;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;

public class ProcessNotExistException extends ProcessBaseException {
    public ProcessNotExistException(FileNotFoundException e) {
        super(e);
    }

    public ProcessNotExistException(NoSuchFileException e) {
        super(e);
    }
}
