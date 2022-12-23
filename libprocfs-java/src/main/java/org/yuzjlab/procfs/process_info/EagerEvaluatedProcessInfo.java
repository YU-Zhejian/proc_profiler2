package org.yuzjlab.procfs.process_info;


import org.yuzjlab.procfs.ProcfsInternalUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.Stat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

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
    public HashMap<String, String> getEnvironmentVariables() throws ProcessBaseException {
        return new HashMap<>(); // TODO
    }

    @Override
    public long getPPID() throws ProcessBaseException {
        return this.getStat().ppid;
    }

    @Override
    public String getName() throws ProcessBaseException {
        // "/proc/$$/comm"
        return "";
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
    public int getOnWhichCPU() {
        return 0; // TODO
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
    public HashMap<Integer, String> getFileDescriptors() {
        return new HashMap<>(); // TODO
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
    public void getStatus() {
        // TODO
    }

}
