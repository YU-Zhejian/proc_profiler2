package org.yuzjlab.procfs;


import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.Stat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

/**
 * <h1>The Process Class</h1>
 * <p>
 * This class represents a user-level process which retrieves information from procfs.
 */
public class ProcessInfo {

    /**
     * pid -- Process ID
     */
    protected final long pid;

    /**
     * A String of path in procfs. For example, <code>/proc/1</code>
     */
    protected final Path pathInProcfs;

    public ProcessInfo(long pid) {
        this.pid = pid;
        this.pathInProcfs = Path.of("/", "proc", String.valueOf(pid));
    }

    /**
     * @return Whether the process still exists in /proc
     */
    public boolean isAlive() {
        return this.pathInProcfs.toFile().exists();
    }

    /**
     * Resolves procfs filename to real path without link
     */
    private Path getItemRealPath(String name) throws ProcessBaseException {
        try {
            return Path.of(this.pathInProcfs.toString(), name).toRealPath();
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
        }
    }

    /**
     * Get path to the executable
     */
    public Path getExePath() throws ProcessBaseException {
        return getItemRealPath("exe");
    }

    /**
     * Get Current Working Directory (CWD) of a process
     */
    public Path getCwdPath() throws ProcessBaseException {
        return getItemRealPath("cwd");
    }

    /**
     * Get Process ID (PID)
     */
    public long getPid() {
        return pid;
    }

    /**
     * Get Commandline arguments
     */
    public String[] getCmdLine() throws ProcessBaseException {
        try {
            var cmdLineStr = Files.readString(Path.of(this.pathInProcfs.toString(), "cmdline"));
            return cmdLineStr.split("\\u0000");
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
        }
    }

    /**
     * Get all environment variables
     */
    public HashMap<String, String> getEnvironmentVariables() throws ProcessBaseException {
        return null; // TODO
    }

    /**
     * Get PID of parent process
     */
    public long getPPID() {
        return 0; // TODO
    }

    /**
     * Get process name
     */
    public String getName() {
        return null; // TODO
    }

    public void getMemoryMap() {
        // TODO
    }

    public Iterable<Integer> getChildPIDs() {
        return null; // TODO
    }

    public int getNumChildProcess() {
        return 0; //TODO
    }

    public int getOnWhichCPU() {
        return 0; // TODO
    }

    public float getCPUTime() {
        return 0; // TODO
    }

    public float getCPUPercent(float waitNSeconds) {
        return 0; // TODO
    }

    public HashMap<Integer, String> getFileDescriptors() {
        return null; // TODO
    }

    public void getIO() {
        // TODO
    }

    public void getMemoryInformation() {
        // TODO
    }

    public Stat getStat() throws ProcessBaseException {
        return new Stat(Path.of(this.pathInProcfs.toString(), "stat"));
    }

    public void getStatus() {
        // TODO
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessInfo processInfo = (ProcessInfo) o;

        return pid == processInfo.pid;
    }

    @Override
    public String toString() {
        return "Process{" +
                "pid=" + pid +
                '}';
    }
}
