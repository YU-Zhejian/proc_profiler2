package org.yuzjlab.procfs.files;

import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.exception.ProcessFileParsingException;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * <code>/proc/[pid]/stat</code>
 * <p>
 * Descriptions from <a href="https://man7.org/linux/man-pages/man5/proc.5.html">manual pages</a>.
 * <p>
 * <i>/proc/[pid]/stat</i>
 * Status information about the process.  This is used by <code>ps(1)</code>.
 * It is defined in the kernel source file <i>fs/proc/array.c</i>.
 * <p>
 * The fields, in order, with their proper <code>scanf(3)</code> format specifiers, are listed below.
 * Whether or not certain of these fields display valid information is governed by a ptrace access mode
 * <b>PTRACE_MODE_READ_FSCREDS </b>|<b>PTRACE_MODE_NOAUDIT</b>chec
 * (refer to <code>ptrace(2)</code>).
 * If the check denies access, then the field value is displayed as 0.
 * The affected fields are indicated with the marking
 * [PT].
 * <p>
 */
public final class ProcPidStat {

    /**
     * The process ID.
     */
    public final long pid;
    /**
     * The filename of the executable, in parentheses.
     * Strings longer than <b>TASK_COMM_LEN </b>(16) characters (including the terminating null byte) are silently truncated.
     * This is visible whether or not the executable is swapped out.
     */
    public final String comm;

    /**
     * One of the following characters, indicating process state:
     * <p>
     * R  Running
     * <p>
     * S  Sleeping in an interruptible wait
     * <p>
     * D  Waiting in uninterruptible disk sleep
     * <p>
     * Z  Zombie
     * <p>
     * T  Stopped (on a signal) or (before Linux 2.6.33)
     * trace stopped
     * <p>
     * t  Tracing stop (Linux 2.6.33 onward)
     * <p>
     * W  Paging (only before Linux 2.6.0)
     * <p>
     * X  Dead (from Linux 2.6.0 onward)
     * <p>
     * x  Dead (Linux 2.6.33 to 3.13 only)
     * <p>
     * K  Wakekill (Linux 2.6.33 to 3.13 only)
     * <p>
     * W  Waking (Linux 2.6.33 to 3.13 only)
     * <p>
     * P  Parked (Linux 3.9 to 3.13 only)
     */
    public final char state;
    /**
     * The PID of the parent of this process.
     */
    public final long ppid;
    /**
     * The process group ID of the process.
     */
    public final long pgrp;
    /**
     * The session ID of the process.
     */
    public final long session;
    /**
     * The controlling terminal of the process.
     * (The minor device number is contained in the combination of bits 31 to 20 and 7 to 0;
     * the major device number is in bits 15 to 8.)
     */
    public final long ttyNr;
    /**
     * The ID of the foreground process group of the controlling terminal of the process.
     */
    public final long tpgid;
    /**
     * The kernel flags word of the process.
     * For bit meanings, see the PF_* defines in the Linux kernel source file <i>include/linux/sched.h</i>.
     * Details depend on the kernel version.
     */
    public final int flags;
    /**
     * The number of minor faults the process has made which have not required loading a memory page from disk.
     */
    public final long minflt;
    /**
     * The number of minor faults that the process's waited-for children have made
     */
    public final long cminflt;
    /**
     * The number of major faults the process has made which have required loading a memory page from disk.
     */
    public final long majflt;
    /**
     * The number of major faults that the process's waited-for children have made.
     */
    public final long cmajflt;
    /**
     * Amount of time that this process has been scheduled in user mode,
     * measured in clock ticks (divide by <i>sysconf(_SC_CLK_TCK)</i>).
     * This includes guest time, <i>guest_time</i> (time spent running a virtual CPU, see below),
     * so that applications that are not aware of the guest time field do not lose that time from their calculations.
     */
    public final long utime;
    /**
     * Amount of time that this process has been scheduled in kernel mode,
     * measured in clock ticks (divide by <i>sysconf(_SC_CLK_TCK)</i>).
     */
    public final long stime;
    /**
     * Amount of time that this process's waited-for children have been scheduled in user mode,
     * measured in clock ticks (divide by <i>sysconf(_SC_CLK_TCK)</i>). (See also <code>times(2)</code>.)
     * This includes guest time, <i>cguest_time</i> (time spent running a virtual CPU, see below).
     */
    public final long cutime;
    /**
     * Amount of time that this process's waited-for children have been scheduled in kernel mode,
     * measured in clock ticks (divide by <i>sysconf(_SC_CLK_TCK)</i>).
     */
    public final long cstime;
    /**
     * (Explanation for Linux 2.6)
     * For processes running a real-time scheduling policy (<i>policy</i> below;
     * see <code>sched_setscheduler(2)</code>),
     * this is the negated scheduling priority, minus one;
     * that is, a number in the range -2 to -100, corresponding to real-time priorities 1 to 99.
     * For processes running under a non-real-time scheduling policy,
     * this is the raw nice value (<code>setpriority(2)</code>) as represented in the kernel.
     * The kernel stores nice values as numbers in the range 0 (high) to 39 (low),
     * corresponding to the user-visible nice range of -20 to 19.
     * <p>
     * Before Linux 2.6, this was a scaled value based on the scheduler weighting given to this process.
     */
    public final int priority;
    /**
     * The nice value (see <code>setpriority(2)</code>), a value in the range 19 (low priority) to -20 (high priority).
     */
    public final int nice;
    /**
     * Number of threads in this process (since Linux 2.6).
     * Before kernel 2.6, this field was hard coded to 0 as a placeholder for an earlier removed field.
     */
    public final int numThreads;
    /**
     * The time in jiffies before the next <b>SIGALRM </b>is sent to the process due to an interval timer.
     * Since kernel 2.6.17, this field is no longer maintained, and is hard coded as 0.
     */
    public final int itrealvalue;
    /**
     * The time the process started after system boot.
     * In kernels before Linux 2.6, this value was expressed  in jiffies.
     * Since Linux 2.6, the value is expressed in clock ticks (divide by <i>sysconf(_SC_CLK_TCK)</i>).
     */
    public final int starttime;
    /**
     * Virtual memory size in bytes.
     */
    public final BigInteger vsize;
    /**
     * Resident Set Size: number of pages the process has in real memory.
     * This is just the pages which count toward text, data, or stack space.
     * This does not include pages which have not been demand-loaded in, or which are swapped out.
     * This value is inaccurate; see <i>/proc/[pid]/statm</i> below.
     */
    public final BigInteger rss;
    /**
     * Current soft limit in bytes on the rss of the process;
     * see the description of <b>RLIMIT_RSS </b>in <code>getrlimit(2)</code>.
     */
    public final BigInteger rsslim;
    /**
     * The address above which program text can run.
     */
    public final BigInteger startcode;
    /**
     * The address below which program text can run.
     */
    public final BigInteger endcode;
    /**
     * The address of the start (i.e., bottom) of the stack.
     */
    public final BigInteger startstack;
    /**
     * The current value of ESP (stack pointer), as found in the kernel stack page for the process.
     */
    public final long kstkesp;
    /**
     * The current EIP (instruction pointer).
     */
    public final long kstkeip;
    /**
     * The bitmap of pending signals, displayed as a decimal number.
     * Obsolete, because it does not provide information on real-time signals;
     * use <i>/proc/[pid]/status</i> instead.
     */
    public final long signal;
    /**
     * The bitmap of blocked signals, displayed as a decimal number.
     * Obsolete, because it does not provide information on real-time signals; use
     * <i>/proc/[pid]/status</i> instead.
     */
    public final long blocked;
    /**
     * The bitmap of ignored signals, displayed as a decimal number.
     * Obsolete, because it does not provide information on real-time signals;
     * use <i>/proc/[pid]/status</i> instead.
     */
    public final long sigignore;
    /**
     * The bitmap of caught signals, displayed as a decimal number.
     * Obsolete, because it does not provide information on real-time signals;
     * use <i>/proc/[pid]/status</i> instead.
     */
    public final BigInteger sigcatch;
    /**
     * This is the "channel" in which the process is waiting.
     * It is the address of a location in the kernel where the process is sleeping.
     * The corresponding symbolic name can be found in <i>/proc/[pid]/wchan</i>.
     */
    public final long wchan;
    /**
     * Number of pages swapped (not maintained).
     */
    public final long nswap;
    /**
     * Cumulative <i>nswap</i> for child processes (not maintained).
     */
    public final long cnswap;
    /**
     * Signal to be sent to parent when we die.
     */
    public final int exitSignal;
    /**
     * CPU number last executed on.
     */
    public final int processor;
    /**
     * Real-time scheduling priority, a number in the range 1 to 99 for processes scheduled under a real-time policy,
     * or 0, for non-real-time processes (see <code>sched_setscheduler(2)</code>).
     */
    public final int rtPriority;
    /**
     * Scheduling policy (see <code>sched_setscheduler(2)</code>).
     * Decode using the SCHED_* constants in <i>linux/sched.h</i>.
     */
    public final int policy;
    /**
     * Aggregated block I/O delays, measured in clock ticks (centiseconds).
     */
    public final long delayacctBlkioTicks;
    /**
     * Guest time of the process (time spent running a virtual CPU for a guest operating system),
     * measured in clock ticks (divide by <i>sysconf(_SC_CLK_TCK)</i>).
     */
    public final long guestTime;
    /**
     * Guest time of the process's children, measured in clock ticks (divide by <i>sysconf(_SC_CLK_TCK)</i>).
     */
    public final long cguestTime;
    /**
     * Address above which program initialized and uninitialized (BSS) data are placed.
     */
    public final BigInteger startData;
    /**
     * Address below which program initialized and uninitialized (BSS) data are placed.
     */
    public final BigInteger endData;
    /**
     * Address above which program heap can be expanded  with <code>brk(2)</code>.
     */
    public final BigInteger startBrk;
    /**
     * Address above which program command-line arguments (<i>argv</i>) are placed.
     */
    public final BigInteger argStart;
    /**
     * Address below program command-line arguments (<i>argv</i>) are placed.
     */
    public final BigInteger argEnd;
    /**
     * Address above which program environment is placed.
     */
    public final BigInteger envStart;
    /**
     * Address below which program environment is placed.
     */
    public final BigInteger envEnd;
    /**
     * The thread's exit status in the form reported by <code>waitpid(2)</code>.
     */
    public final long exitCode;


    /**
     * Default initializer.
     *
     * @param pathToStat Path to the stat file that would be parsed.
     */
    public ProcPidStat(Path pathToStat) throws ProcessBaseException {
        try (Scanner scn = new Scanner(new FileInputStream(pathToStat.toFile()))) {
            this.pid = scn.nextLong();
            this.comm = scn.next().replace("(", "").replace(")", "");
            
            this.state = scn.next().charAt(0);
            this.ppid = scn.nextLong();
            this.pgrp = scn.nextLong();
            this.session = scn.nextLong();
            this.ttyNr = scn.nextLong();
            this.tpgid = scn.nextLong();
            this.flags = scn.nextInt();
            this.minflt = scn.nextInt();
            this.cminflt = scn.nextInt();
            this.majflt = scn.nextInt();
            this.cmajflt = scn.nextInt();
            this.utime = scn.nextLong();
            this.stime = scn.nextLong();
            this.cutime = scn.nextLong();
            this.cstime = scn.nextLong();
            this.priority = scn.nextInt();
            this.nice = scn.nextInt();
            this.numThreads = scn.nextInt();
            this.itrealvalue = scn.nextInt();
            this.starttime = scn.nextInt();
            this.vsize = scn.nextBigInteger();
            this.rss = scn.nextBigInteger();
            this.rsslim = scn.nextBigInteger();
            this.startcode = scn.nextBigInteger();
            this.endcode = scn.nextBigInteger();
            this.startstack = scn.nextBigInteger();
            this.kstkesp = scn.nextLong();
            this.kstkeip = scn.nextLong();
            this.signal = scn.nextLong();
            this.blocked = scn.nextLong();
            this.sigignore = scn.nextLong();
            this.sigcatch = scn.nextBigInteger();
            this.wchan = scn.nextLong();
            this.nswap = scn.nextLong();
            this.cnswap = scn.nextLong();
            this.exitSignal = scn.nextInt();
            this.processor = scn.nextInt();
            this.rtPriority = scn.nextInt();
            this.policy = scn.nextInt();
            if(scn.hasNextLong()) {
            	this.delayacctBlkioTicks = scn.nextLong();
                this.guestTime = scn.nextLong();
                this.cguestTime = scn.nextLong();
                this.startData = scn.nextBigInteger();
                this.endData = scn.nextBigInteger();
                this.startBrk = scn.nextBigInteger();
                this.argStart = scn.nextBigInteger();
                this.argEnd = scn.nextBigInteger();
                this.envStart = scn.nextBigInteger();
                this.envEnd = scn.nextBigInteger();
                this.exitCode = scn.nextLong();
            }
            else{
            	// These things happens in FreeBSD.
            	this.delayacctBlkioTicks = -1L;
                this.guestTime = -1L;
                this.cguestTime = -1L;
                this.startData = BigInteger.valueOf(-1L);
                this.endData = BigInteger.valueOf(-1L);
                this.startBrk = BigInteger.valueOf(-1L);
                this.argStart = BigInteger.valueOf(-1L);
                this.argEnd = BigInteger.valueOf(-1L);
                this.envStart = BigInteger.valueOf(-1L);
                this.envEnd = BigInteger.valueOf(-1L);
                this.exitCode = -1L;
            }

        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        } catch (NoSuchElementException e) {
            throw new ProcessFileParsingException(e);
        }
    }

    @Override
    public String toString() {
        return String.join(
                " ",
                String.valueOf(pid),
                comm,
                String.valueOf(state),
                String.valueOf(ppid),
                String.valueOf(pgrp),
                String.valueOf(session),
                String.valueOf(ttyNr),
                String.valueOf(tpgid),
                String.valueOf(flags),
                String.valueOf(minflt),
                String.valueOf(cminflt),
                String.valueOf(majflt),
                String.valueOf(cmajflt),
                String.valueOf(utime),
                String.valueOf(stime),
                String.valueOf(cutime),
                String.valueOf(cstime),
                String.valueOf(priority),
                String.valueOf(nice),
                String.valueOf(numThreads),
                String.valueOf(itrealvalue),
                String.valueOf(starttime),
                String.valueOf(vsize),
                String.valueOf(rss),
                String.valueOf(rsslim),
                String.valueOf(startcode),
                String.valueOf(endcode),
                String.valueOf(startstack),
                String.valueOf(kstkesp),
                String.valueOf(kstkeip),
                String.valueOf(signal),
                String.valueOf(blocked),
                String.valueOf(sigignore),
                String.valueOf(sigcatch),
                String.valueOf(wchan),
                String.valueOf(nswap),
                String.valueOf(cnswap),
                String.valueOf(exitSignal),
                String.valueOf(processor),
                String.valueOf(rtPriority),
                String.valueOf(policy),
                String.valueOf(delayacctBlkioTicks),
                String.valueOf(guestTime),
                String.valueOf(cguestTime),
                String.valueOf(startData),
                String.valueOf(endData),
                String.valueOf(startBrk),
                String.valueOf(argStart),
                String.valueOf(argEnd),
                String.valueOf(envStart),
                String.valueOf(envEnd),
                String.valueOf(exitCode)
        ) + "\n";
    }
}
