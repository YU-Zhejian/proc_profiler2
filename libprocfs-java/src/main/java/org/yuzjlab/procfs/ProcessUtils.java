package org.yuzjlab.procfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.exception.ProcessNotExistException;
import org.yuzjlab.procfs.exception.ProcessPermissionDeniedException;
import org.yuzjlab.procfs.exception.ProcessUnknownException;

@SuppressWarnings("squid:S1075") // Supress URIs should not be hardcoded
public class ProcessUtils {
    // SonarLint: Suppress
    protected static String resolvedProcfsPath = null;

    private ProcessUtils() {}

    public static String getProcfsPath() throws ProcessBaseException {
        if (resolvedProcfsPath != null) {
            return resolvedProcfsPath;
        }
        var procfsPaths = new ArrayList<String>();
        procfsPaths.add(System.getenv("LIBPROCFS_PROCFS_PATH"));
        procfsPaths.add("/compat/linux/proc/");
        procfsPaths.add("/proc/");
        var lh = LoggerFactory.getLogger(ProcessUtils.class.getCanonicalName());
        for (var procfsPath : procfsPaths) {
            if (procfsPath == null) {
                continue;
            } else if (Files.exists(Path.of(procfsPath))) {
                resolvedProcfsPath = procfsPath;
                return resolvedProcfsPath;
            } else {
                lh.debug("Tried {} failed", procfsPath);
            }
        }
        throw resolveIOException(
                new FileNotFoundException("PROCFS not exist! See log for tried locations."));
    }

    public static long getCurrentPid() throws ProcessBaseException {
        Path selfProcPath = null;
        if (Path.of(getProcfsPath(), "self").toFile().exists()) {
            selfProcPath = Path.of(getProcfsPath(), "self");
        } else if (Path.of(getProcfsPath(), "curproc").toFile().exists()) {
            selfProcPath = Path.of(getProcfsPath(), "curproc");
        }
        try {
            return Long.parseLong(selfProcPath.toRealPath().getFileName().toString());
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
     *
     * <p>This method does <i>not</i> deal with special symbolic links like sockets, pipes, etc.
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
