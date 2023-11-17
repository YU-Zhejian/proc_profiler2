package org.yuzjlab.procfs.process_info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.ProcPidEnviron;
import org.yuzjlab.procfs.files.ProcPidFd;
import org.yuzjlab.procfs.files.ProcPidIo;
import org.yuzjlab.procfs.files.ProcPidMaps;
import org.yuzjlab.procfs.files.ProcPidStat;
import org.yuzjlab.procfs.files.ProcPidStatm;

/**
 * The Process Class
 *
 * <p>This class represents a user-level process which retrieves information from procfs.
 */
public class EagerEvaluatedProcessInfo extends BaseProcessInfo implements ProcessInfoInterface {

    public EagerEvaluatedProcessInfo(long pid) throws ProcessBaseException {
        super(pid);
    }

    @Override
    public Path getExePath() throws ProcessBaseException {
        return getItemRealPath("exe");
    }

    @Override
    public Path getCwdPath() throws ProcessBaseException {
        return getItemRealPath("cwd");
    }

    @Override
    public String[] getCmdLine() throws ProcessBaseException {
        try {
            var cmdLineStr = Files.readString(Path.of(this.pathInProcfs.toString(), "cmdline"));
            return cmdLineStr.split("\\u0000");
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
    }

    @Override
    public Map<String, String> getEnvironmentVariables() throws ProcessBaseException {
        return ProcPidEnviron.parseEnviron(Path.of(this.pathInProcfs.toString(), "environ"));
    }

    @Override
    public long getPPID() throws ProcessBaseException {
        return this.getStat().ppid;
    }

    @Override
    public String getName() throws ProcessBaseException {
        try {
            return Files.readString(Path.of(this.pathInProcfs.toString(), "comm")).strip();
        } catch (IOException e) {
            return this.getStat().comm;
        }
    }

    @Override
    public Iterable<String> getMemoryMap() throws ProcessBaseException {
        try {
            return ProcPidMaps.parseMMap(Path.of(this.pathInProcfs.toString(), "map_files"));
        } catch (ProcessBaseException e) {
            return ProcPidMaps.parseMaps(Path.of(this.pathInProcfs.toString(), "maps"));
        }
    }

    private List<Integer> getChildPidsFromThreadDir(Path threadDirPath) {
        try {
            return Arrays.stream(
                            Files.readString(Path.of(threadDirPath.toString(), "children"))
                                    .split(" "))
                    .filter((String s) -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
        } catch (IOException | NumberFormatException ignored) {
            return new ArrayList<>();
        }
    }

    @Override
    public Iterable<Integer> getChildPIDs() throws ProcessBaseException {
        HashSet<Integer> pids = new HashSet<>();
        try (var fs = Files.newDirectoryStream(Path.of(this.pathInProcfs.toString(), "task"))) {
            for (var threadDirPath : fs) {
                pids.addAll(getChildPidsFromThreadDir(threadDirPath));
            }
            return pids;
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
    }

    @Override
    public long getNumChildProcess() throws ProcessBaseException {
        return StreamSupport.stream(this.getChildPIDs().spliterator(), false).count();
    }

    @Override
    public int getOnWhichCPU() throws ProcessBaseException {
        return this.getStat().processor;
    }

    @Override
    public float getCPUTime() throws ProcessBaseException {
        var stat = this.getStat();
        return (stat.utime + stat.stime);
    }

    @Override
    @SuppressWarnings("squid:S2274") // I need a wait here
    public float getCPUPercent(float waitNSeconds) throws ProcessBaseException {
        float cpuTimeAtStart = this.getCPUTime();
        try {
            synchronized (this) {
                this.wait((int) (waitNSeconds * 1000));
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        float cpuTimeAtEnd = this.getCPUTime();
        return (cpuTimeAtEnd - cpuTimeAtStart) / waitNSeconds;
    }

    @Override
    public Map<Integer, String> getFileDescriptors() throws ProcessBaseException {
        return ProcPidFd.parseFD(Path.of(this.pathInProcfs.toString(), "fd"));
    }

    @Override
    public long getNumberOfFileDescriptors() throws ProcessBaseException {
        return ProcPidFd.getNumberOfFD(Path.of(this.pathInProcfs.toString(), "fd"));
    }

    @Override
    public ProcPidIo getIO() throws ProcessBaseException {
        return new ProcPidIo(Path.of(this.pathInProcfs.toString(), "io"));
    }

    @Override
    public ProcPidStatm getMemoryInformation() throws ProcessBaseException {
        return new ProcPidStatm(Path.of(this.pathInProcfs.toString(), "statm"));
    }

    @Override
    public ProcPidStat getStat() throws ProcessBaseException {
        return new ProcPidStat(Path.of(this.pathInProcfs.toString(), "stat"));
    }

    @Override
    public char getState() throws ProcessBaseException {
        return this.getStat().state;
    }
}
