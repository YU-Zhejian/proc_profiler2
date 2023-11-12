package org.yuzjlab.procfs.process_info;


import org.yuzjlab.procfs.ProcfsInternalUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.Environ;
import org.yuzjlab.procfs.files.FD;
import org.yuzjlab.procfs.files.Stat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.StreamSupport;


/**
 * The Process Class
 * <p>
 * This class represents a user-level process which retrieves information from procfs.
 */
public class EagerEvaluatedProcessInfo extends BaseProcessInfo {

    public EagerEvaluatedProcessInfo(long pid) {
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
            throw ProcfsInternalUtils.resolveIOException(e);
        }
    }

    @Override
    public Map<String, String> getEnvironmentVariables() throws ProcessBaseException {
        return Environ.parseEnviron(Path.of(this.pathInProcfs.toString(), "environ"));
    }

    @Override
    public long getPPID() throws ProcessBaseException {
        return this.getStat().ppid;
    }

    @Override
    public String getName() throws ProcessBaseException {
        try {
            return Files.readString(Path.of(this.pathInProcfs.toString(), "comm"));
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
        }
    }

    @Override
    public void getMemoryMap() {
        // TODO
    }

    @Override
    public Iterable<Integer> getChildPIDs() throws ProcessBaseException {
        HashSet<Integer> pids = new HashSet<>();
        try (var fs = Files.newDirectoryStream(Path.of(this.pathInProcfs.toString(), "task"))) {
            for (var thread_dir_path : fs) {
                try {
                    pids.addAll(
                            Arrays
                                    .stream(Files.readString(Path.of(thread_dir_path.toString(), "children")).split(" "))
                                    .filter((String _s) -> !_s.isEmpty())
                                    .map(Integer::parseInt)
                                    .toList()
                    );
                } catch (IOException ignored) {
                }
            }
            return pids;
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
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
        return stat.utime + stat.stime;
    }

    @Override
    public float getCPUPercent(float waitNSeconds) throws ProcessBaseException {
        float cpuTimeAtStart = this.getCPUTime();
        try {
            wait((int) (waitNSeconds * 1000));
        } catch (InterruptedException ignored) {
        }
        float cpuTimeAtEnd = this.getCPUTime();
        return (cpuTimeAtEnd - cpuTimeAtStart) / waitNSeconds;
    }

    @Override
    public Map<Integer, String> getFileDescriptors() throws ProcessBaseException {
        return FD.parseFD(Path.of(this.pathInProcfs.toString(), "fd"));
    }

    @Override
    public long getNumberOfFileDescriptors() throws ProcessBaseException {
        return FD.getNumberOfFD(Path.of(this.pathInProcfs.toString(), "fd"));
    }

    @Override
    public void getIO() {
        // TODO
    }

    @Override
    public void getMemoryInformation() {
        // TODO
    }

    @Override
    public Stat getStat() throws ProcessBaseException {
        return new Stat(Path.of(this.pathInProcfs.toString(), "stat"));
    }

    @Override
    public char getState() throws ProcessBaseException {
        return this.getStat().state;
    }

}
