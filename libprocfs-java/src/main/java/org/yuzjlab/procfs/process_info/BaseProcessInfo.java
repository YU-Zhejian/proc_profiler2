package org.yuzjlab.procfs.process_info;

import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class BaseProcessInfo implements ProcessInfoInterface {

    /**
     * pid -- Process ID
     */
    protected final long pid;
    /**
     * Start time in clock ticks.
     */
    protected final long startTime;

    /**
     * A String of path in procfs. For example, <code>/proc/1</code>
     */
    protected final Path pathInProcfs;

    protected BaseProcessInfo(long pid) throws ProcessBaseException {
        this.pid = pid;
        this.pathInProcfs = Path.of(ProcessUtils.getProcfsPath(), String.valueOf(pid));
        this.startTime = this.getStat().starttime;
    }

    @Override
    public long getPid() {
        return pid;
    }

    @Override
    public boolean isAlive() {
        return this.pathInProcfs.toFile().exists();
    }

    /**
     * Resolves procfs filename to real path without link
     */
    protected Path getItemRealPath(String name) throws ProcessBaseException {
        try {
            return Path.of(this.pathInProcfs.toString(), name).toRealPath();
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pid, this.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EagerEvaluatedProcessInfo eagerEvaluatedProcessInfo = (EagerEvaluatedProcessInfo) o;

        return (this.pid == eagerEvaluatedProcessInfo.pid) &&
          (this.startTime == eagerEvaluatedProcessInfo.startTime);
    }

    @Override
    public String toString() {
        return "Process %d".formatted(this.pid);
    }
}
