package org.yuzjlab.procfs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;
import jnr.constants.platform.Sysconf;
import jnr.posix.POSIXFactory;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.ProcMemInfo;
import org.yuzjlab.procfs.files.ProcStat;

public final class SystemInfo {

    public static final long PAGE_SIZE = POSIXFactory.getPOSIX().sysconf(Sysconf._SC_PAGE_SIZE);
    public static final long CLOCK_TICK = POSIXFactory.getPOSIX().sysconf(Sysconf._SC_CLK_TCK);

    public SystemInfo() {}

    public static Iterable<Integer> iterAllPids() throws ProcessBaseException {
        try (var dstream = Files.newDirectoryStream(Path.of(ProcessUtils.getProcfsPath()))) {
            return StreamSupport.stream(dstream.spliterator(), true)
                    .filter((Path p) -> (p.toFile().isDirectory()))
                    .map(
                            (Path p) -> {
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

    public ProcStat getStat() throws ProcessBaseException {
        return new ProcStat(Path.of(ProcessUtils.getProcfsPath(), "stat"));
    }

    public ProcMemInfo getMemInfo() throws ProcessBaseException {
        return new ProcMemInfo(Path.of(ProcessUtils.getProcfsPath(), "meminfo"));
    }
}
