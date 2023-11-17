package org.yuzjlab.proctracer.psst;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CMDBasedProcessSupervisorThread extends BaseProcessSupervisorThread {
    protected final String[] cmds;
    protected Process p;
    protected final File stdin;
    protected final File stdout;
    protected final File stderr;
    protected final File wd;
    protected final Map<String, String> env;

    public CMDBasedProcessSupervisorThread(
            String[] cmds, File stdin, File stdout, File stderr, File wd, Map<String, String> env) {
        this.cmds = cmds;
        this.p = null;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.wd = wd;
        this.env = env;
    }

    @Override
    public void run() {
        var pb =
                new ProcessBuilder(cmds)
                        .redirectError(this.stderr)
                        .redirectInput(this.stdin)
                        .redirectOutput(this.stdout)
                        .directory(this.wd);
        pb.environment().clear();
        pb.environment().putAll(this.env);
        try {
            this.p = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public long getPid() {
        if (this.p == null) {
            return -1;
        }
        return this.p.pid();
    }

    public int getExitValue() {
        if (this.p == null) {
            return -1;
        }
        return this.p.exitValue();
    }
}
