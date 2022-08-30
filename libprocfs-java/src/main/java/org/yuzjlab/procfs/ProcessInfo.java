package org.yuzjlab.procfs;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * = The Process Class
 *
 * This class represents a user-level process which retrieves information from procfs.
 */
public class ProcessInfo {

    /**
     * pid -- Process ID
     */
    protected final long pid;
    protected final String pathInProcfs;

    protected final File fileInProcfs;

    public ProcessInfo(long pid) {
        this.pid = pid;
        this.pathInProcfs = String.format("/proc/%d", pid);
        this.fileInProcfs = new File(this.pathInProcfs);
    }

    public boolean isAlive(){
        return this.fileInProcfs.exists();
    }

    public Path getExePath() throws IOException {
        return Path.of(String.format("%s/%s", this.pathInProcfs, "exe")).toRealPath();
    }

    public Path getCwdPath() throws IOException {
        return Path.of(String.format("%s/%s", this.pathInProcfs, "cwd")).toRealPath();
    }

    public long getPid() {
        return pid;
    }

    public String[] getCmdLine() throws IOException{
        var cmdLineStr = Files.readString(Path.of(String.format("%s/%s", this.pathInProcfs, "cmdline")));
        return cmdLineStr.split("\\u0000");
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

    public static void main(String[] args) throws IOException {
        var p = new ProcessInfo(ProcessUtils.getCurrentPid());
        System.out.println(p.getPid());
        System.out.println(Arrays.toString(p.getCmdLine()));
        System.out.println(p.getExePath());
    }
}
