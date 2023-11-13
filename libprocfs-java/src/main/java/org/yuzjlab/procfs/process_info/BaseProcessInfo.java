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
     * A String of path in procfs. For example, <code>/proc/1</code>
     */
    protected final Path pathInProcfs;

    protected BaseProcessInfo(long pid) throws ProcessBaseException {
        this.pid = pid;
        this.pathInProcfs = Path.of(ProcessUtils.getProcfsPath(), String.valueOf(pid));
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
        return Objects.hash(pid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EagerEvaluatedProcessInfo eagerEvaluatedProcessInfo = (EagerEvaluatedProcessInfo) o;

        return pid == eagerEvaluatedProcessInfo.pid;
    }

    @Override
    public String toString() {
        return "Process{" +
                "pid=" + pid +
                '}';
    }
}
