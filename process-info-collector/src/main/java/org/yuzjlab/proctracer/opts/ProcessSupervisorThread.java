package org.yuzjlab.proctracer.opts;

import java.io.File;
import java.io.IOException;

public class ProcessSupervisorThread extends Thread {
    protected String[] cmds;
    protected Process p;
    File stdin;
    File stdout;
    File stderr;
    File wd;

    public ProcessSupervisorThread(String[] cmds, File stdin, File stdout, File stderr, File wd) {
        this.cmds = cmds;
        this.p = null;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.wd = wd;
    }

    @Override
    public void run() {
        var pb = new ProcessBuilder(cmds)
                .redirectError(this.stderr)
                .redirectInput(this.stdin)
                .redirectOutput(this.stdout)
                .directory(this.wd);
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

    public void kill() {
        if (this.p == null) {
            return;
        }
        this.p = this.p.destroyForcibly();
    }

}
