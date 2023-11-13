package org.yuzjlab.procfs;

import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

public final class SystemInfo {

    private SystemInfo() {
    }

    public static Iterable<Integer> iterAllPids() throws ProcessBaseException {
        try(var dstream = Files.newDirectoryStream(Path.of(ProcessUtils.getProcfsPath()))){
            return StreamSupport.stream(dstream.spliterator(), true)
                    .filter((Path p) -> (p.toFile().isDirectory()))
                    .map((Path p) -> {
                        int i = -1;
                        try {
                            i = Integer.parseInt(p.getFileName().toString());
                        } catch (NumberFormatException ignored) {
                            // Happens for special directories.
                        }
                        return i;
                    })
                    .filter((Integer i) -> (i != -1))
                    .toList();
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
    }

    public static long getNumberOfProcesses() throws ProcessBaseException {
        return StreamSupport.stream(iterAllPids().spliterator(), false).count();
    }
}
