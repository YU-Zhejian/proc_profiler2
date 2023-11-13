package org.yuzjlab.procfs.files;

import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class ProcStat {

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
     * Time waiting for I/O to complete.  This
     * value is not reliable, for the following
     * reasons:
     * <p>
     * The CPU will not wait for I/O to
     * complete; iowait is the time that a task
     * is waiting for I/O to complete.  When a
     * CPU goes into idle state for outstanding
     * task I/O, another task will be scheduled
     * on this CPU.
     * <p>
     * On a multi-core CPU, the task waiting for
     * I/O to complete is not running on any
     * CPU, so the iowait of each CPU is
     * difficult to calculate.
     * <p>
     * The value in this field may decrease in
     * certain conditions.
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
     * Stolen time, which is the time spent in
     * other operating systems when running in a
     * virtualized environment
     */
    public final long cpuSteal;

    /**
     * Time spent running a virtual CPU for
     * guest operating systems under the control of
     * the Linux kernel.
     */
    public final long cpuGuest;

    /**
     * Time spent running a niced guest
     * (virtual CPU for guest operating systems
     * under the control of the Linux kernel).
     */
    public final long cpuGuestNice;

    /**
     * The number of context switches that the system
     * underwent.
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
        for (var line : lines) {
            if (line.startsWith("cpu ")) {
                reqLine = line;
                break;
            }
        }
        if (reqLine == null) {
            throw new ProcessBaseException(new RuntimeException("proc/stat file contains no %s line!".formatted(prefix)));
        }
        try{
            return Long.parseLong(reqLine.strip().split(" ")[1]);
        } catch (NumberFormatException e){
            throw new ProcessBaseException(new RuntimeException("proc/stat file contains illegal %s line!".formatted(prefix)));
        }
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
        this.process = getTrailingLong(lines, "process");
        this.procsBlocked = getTrailingLong(lines, "process_blocked");
        this.procsRunning = getTrailingLong(lines, "process_running");
        this.btime = getTrailingLong(lines, "btime");
    }

}
