package org.yuzjlab.procfs;


import org.yuzjlab.procfs._internal.ProcfsInternalUtils;
import org.yuzjlab.procfs.exception.ProcessNotExistException;
import org.yuzjlab.procfs.exception.ProcessPermissionDeniedException;
import org.yuzjlab.procfs.exception.ProcessUnknownException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
    protected final String pathStrInProcfs;

    protected final File fileInProcfs;

    public ProcessInfo(long pid) {
        this.pid = pid;
        this.pathStrInProcfs = String.format("/proc/%d", pid);
        this.fileInProcfs = new File(this.pathStrInProcfs);
    }

    public static void main(String[] args) throws IOException, ProcessPermissionDeniedException, ProcessUnknownException, ProcessNotExistException {
        var p = new ProcessInfo(ProcessUtils.getCurrentPid());
        System.out.println(p.getPid());
        System.out.println(Arrays.toString(p.getCmdLine()));
        System.out.println(p.getExePath());
    }

    public boolean isAlive() {
        return this.fileInProcfs.exists();
    }

    private Path getItemRealPath(String name) throws ProcessNotExistException, ProcessPermissionDeniedException, ProcessUnknownException {
        try {
            return Path.of(String.format("%s/%s", this.pathStrInProcfs, name)).toRealPath();
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
        }

    }

    public Path getExePath() throws ProcessNotExistException, ProcessPermissionDeniedException, ProcessUnknownException {
        return getItemRealPath("exe");
    }

    public Path getCwdPath() throws ProcessNotExistException, ProcessPermissionDeniedException, ProcessUnknownException {
        return getItemRealPath("cwd");
    }

    public long getPid() {
        return pid;
    }

    public String[] getCmdLine() throws IOException {
        var cmdLineStr = Files.readString(Path.of(String.format("%s/%s", this.pathStrInProcfs, "cmdline")));
        return cmdLineStr.split("\\u0000");
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
