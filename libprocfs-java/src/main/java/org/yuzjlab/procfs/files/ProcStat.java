package org.yuzjlab.procfs.files;

import jnr.constants.platform.Sysconf;
import jnr.posix.POSIXFactory;
import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

/**
 * NOTE: CPU time , measured in units of USER_HZ (1/100ths of a second on most architectures,
 * use sysconf(_SC_CLK_TCK) to obtain the right value),
 * that the system ("cpu" line) or the specific CPU ("cpuN" line) spent in various states:
 */
public class ProcStat {

    public final long clockTick = POSIXFactory.getPOSIX().sysconf(Sysconf._SC_CLK_TCK);

    /**
     * Time spent in user mode.
     */
    public final long cpuUser;

    /**
     * Time spent in user mode with low priority (nice).
     */
    public final long cpuNice;

    /**
     * Time spent in system mode.
     */
    public final long cpuSystem;

    /**
     * Time spent in the idle task.
     * This value should be USER_HZ times the second entry in  the /proc/uptime pseudo-file.
     */
    public final long cpuIdle;

    /**
     * Time waiting for I/O to complete.  This value is not reliable, for the following reasons:
     * <p>
     * The CPU will not wait for I/O to complete;
     * iowait is the time that a task is waiting for I/O to complete.
     * When CPU goes into idle state for outstanding task I/O,
     * another task will be scheduled on this CPU.
     * <p>
     * On a multi-core CPU, the task waiting for I/O to complete is not running on any CPU,
     * so the iowait of each CPU is difficult to calculate.
     * <p>
     * The value in this field may decrease in certain conditions.
     */
    public final long cpuIOWait;

    /**
     * Time servicing interrupts.
     */
    public final long cpuIrq;

    /**
     * Time servicing softirqs.
     */
    public final long cpuSoftIrq;

    /**
     * Stolen time, which is the time spent in other operating systems when running in a virtualized environment
     */
    public final long cpuSteal;

    /**
     * Time spent running a virtual CPU for guest operating systems under the control of the Linux kernel.
     */
    public final long cpuGuest;

    /**
     * Time spent running a niced guest (virtual CPU for guest operating systems under the control of the Linux kernel).
     */
    public final long cpuGuestNice;

    /**
     * The number of context switches that the system underwent.
     */
    public final long ctxt;

    /**
     * boot time, in seconds since the Epoch, 1970-01-01 00:00:00 +0000 (UTC).
     */
    public final long btime;

    /**
     * Number of forks since boot.
     */
    public final long process;

    /**
     * Number of processes in runnable state.
     */
    public final long procsRunning;

    /**
     * Number of processes blocked waiting for I/O to complete.
     */
    public final long procsBlocked;

    protected static long getTrailingLong(List<String> lines, String prefix) throws ProcessBaseException{
        String reqLine = null;
        var pline = prefix+" ";
        for (var line : lines) {
            if (line.startsWith(pline)) {
                reqLine = line;
                break;
            }
        }
        if (reqLine == null) {
            throw new ProcessBaseException(new RuntimeException("proc/stat file contains no %s line!".formatted(prefix)));
        }
        var reqNum = reqLine.strip().split(" ")[1];
        try{
            return Long.parseLong(reqNum);
        } catch (NumberFormatException e){
            throw new ProcessBaseException(
                    new RuntimeException("proc/stat file contains illegal %s line ('%s')!"
                            .formatted(prefix, reqNum))
            );
        }
    }

    public float getCpuGuestSeconds() {
        return (float) this.cpuGuest /this.clockTick;
    }
    public float getCpuUserSeconds() {
        return (float) this.cpuUser /this.clockTick;
    }

    public float getCpuStealSeconds() {
        return (float) this.cpuSteal /this.clockTick;
    }
    public float getCpuIrqSeconds() {
        return (float) this.cpuIrq /this.clockTick;
    }
    public float getCpuSoftIrqSeconds() {
        return (float) this.cpuSoftIrq /this.clockTick;
    }
    public float getCpuIOWaitSeconds() {
        return (float) this.cpuIOWait /this.clockTick;
    }
    public float getCpuIdleSeconds() {
        return (float) this.cpuIdle /this.clockTick;
    }
    public float getCpuSystemSeconds() {
        return (float) this.cpuSystem /this.clockTick;
    }
    public float getCpuNiceSeconds() {
        return (float) this.cpuNice /this.clockTick;
    }
    public float getCpuGuestNiceSeconds() {
        return (float) this.cpuGuestNice /this.clockTick;
    }

    public Instant getUpInstant(){
        return Instant.ofEpochSecond(this.btime);
    }

    public long getUpTimeSeconds(){
        return Instant.now().getEpochSecond() - this.btime;
    }

    public ProcStat(Path pathToStat) throws ProcessBaseException {
        List<String> lines;
        try {
            lines = new BufferedReader(new FileReader(pathToStat.toFile())).lines().toList();
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }

        String cpuLine = null;
        for (var line : lines) {
            if (line.startsWith("cpu ")) {
                cpuLine = line;
                break;
            }
        }
        if (cpuLine == null) {
            throw new ProcessBaseException(new RuntimeException("proc/stat file contains no cpu line!"));
        }

        Scanner scn = new Scanner(cpuLine);
        scn.next();
        this.cpuUser = scn.nextLong();
        this.cpuNice = scn.nextLong();
        this.cpuSystem = scn.nextLong();
        this.cpuIdle = scn.nextLong();
        this.cpuIOWait = scn.nextLong();
        this.cpuIrq = scn.nextLong();
        this.cpuSoftIrq = scn.nextLong();
        this.cpuSteal = scn.nextLong();
        this.cpuGuest = scn.nextLong();
        this.cpuGuestNice = scn.nextLong();
        this.ctxt = getTrailingLong(lines, "ctxt");
        this.process = getTrailingLong(lines, "processes");
        this.procsBlocked = getTrailingLong(lines, "procs_blocked");
        this.procsRunning = getTrailingLong(lines, "procs_running");
        this.btime = getTrailingLong(lines, "btime");
    }
}
