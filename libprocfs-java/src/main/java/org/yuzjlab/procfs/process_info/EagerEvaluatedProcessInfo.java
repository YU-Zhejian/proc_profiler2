package org.yuzjlab.procfs.process_info;


import org.yuzjlab.procfs.ProcfsInternalUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.Environ;
import org.yuzjlab.procfs.files.FD;
import org.yuzjlab.procfs.files.Stat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
    public Iterable<Integer> getChildPIDs() {
        return null; // TODO
    }

    @Override
    public int getNumChildProcess() {
        return 0; //TODO
    }

    @Override
    public int getOnWhichCPU() throws ProcessBaseException {
        return this.getStat().processor;
    }

    @Override
    public float getCPUTime() {
        return 0; // TODO
    }

    @Override
    public float getCPUPercent(float waitNSeconds) {
        return 0; // TODO
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
