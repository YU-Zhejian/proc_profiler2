package org.yuzjlab.procfs;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.exception.ProcessNotExistException;
import org.yuzjlab.procfs.exception.ProcessPermissionDeniedException;
import org.yuzjlab.procfs.exception.ProcessUnknownException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@SuppressWarnings("squid:S1075") // Supress URIs should not be hardcoded
public class ProcessUtils {
    // SonarLint: Suppress
    protected static final String DEFAULT_PROCFS_PATH = "/proc/";

    private ProcessUtils() {

    }

    public static String getProcfsPath () throws ProcessBaseException{
        var procfsPath = System.getenv("LIBPROCFS_PROCFS_PATH");
        if (procfsPath == null){
            procfsPath = DEFAULT_PROCFS_PATH;
        }
        if (!Files.exists(Path.of(procfsPath))){
            throw resolveIOException(new FileNotFoundException("PROCFS not exist at '%s'!".formatted(procfsPath)));
        }
        return procfsPath;
    }

    public static long getCurrentPid() throws ProcessBaseException {
        try {
            return Long.parseLong(Path.of(getProcfsPath(), "self").toRealPath().getFileName().toString());
        } catch (NumberFormatException e) {
            throw new ProcessUnknownException(e);
        } catch (IOException e) {
            throw resolveIOException(e);
        }
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

    /**
     * Try resolve symbolic link to actual file. If failed, return non-resolved filename.
     * <p>
     * This method does <i>not</i> deal with special symbolic links like sockets, pipes, etc.
     */
    public static String resolveRealPath(Path path) {
        String realPath;
        try {
            realPath = path.toFile().toPath().toRealPath().toString();
        } catch (IOException e) {
            realPath = path.toString();
        }
        return realPath;
    }
}
