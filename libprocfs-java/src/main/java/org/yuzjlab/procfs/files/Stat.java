package org.yuzjlab.procfs.files;

import org.yuzjlab.procfs.ProcfsInternalUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.exception.ProcessUnknownException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * <code>/proc/[pid]/stat</code>
 */
public final class Stat {

    private final long pid;
    private final String comm;
    private final char state;
    private final long ppid;
    private final long pgrp;
    private final long session;
    private final long ttyNr;
    private final long tpgid;
    private final int flags;
//    private final long minflt;
//    private final long cminflt;
//    private final long majflt;
//    private final long cmajflt;
//    private final long utime;
//    private final long stime;
//    private final long cutime;
//    private final long cstime;
//    private final int priority;
//    private final int nice;
//    private final int numThreads;
//    private final int itrealvalue;
//    private final int starttime;
//    private final int vsize;
//    private final int rss;
//    private final long rsslim;
//    private final long startcode;
//    private final long endcode;
//    private final long startstack;
//    private final long kstkesp;
//    private final long kstkeip;
//    private final long signal;
//    private final long blocked;
//    private final long sigignore;
//    private final long sigcatch;
//    private final long wchan;
//    private final long nswap;
//    private final long cnswap;
//    private final int exitSignal;
//    private final int processor;
//    private final int rtPriority;
//    private final int policy;
//    private final long delayacctBlkioTicks;
//    private final long guestTime;
//    private final long cguestTime;
//    private final long startData;
//    private final long endData;
//    private final long startBrk;
//    private final long argStart;
//    private final long argEnd;
//    private final long envStart;
//    private final long envEnd;
//    private final long exitCode;


    public Stat(Path pathToStats) throws ProcessBaseException {
        Scanner scn;
        try {
            scn = new Scanner(new FileInputStream(pathToStats.toFile()));
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
        }
        try {
            this.pid = scn.nextLong();
            this.comm = scn.next();
            this.state = scn.next().charAt(0);
            this.ppid = scn.nextLong();
            this.pgrp = scn.nextLong();
            this.session = scn.nextLong();
            this.ttyNr = scn.nextLong();
            this.tpgid = scn.nextLong();
            this.flags = scn.nextInt();
        } catch (NoSuchElementException e) {
            throw new ProcessUnknownException(e);
        }
        scn.close();
    }

    public long getPid() {
        return pid;
    }

    public String getComm() {
        return comm;
    }

    public char getState() {
        return state;
    }

    public long getPpid() {
        return ppid;
    }

    public long getPgrp() {
        return pgrp;
    }

    public long getSession() {
        return session;
    }

    @Override
    public String toString() {
        return String.valueOf(pid) + ' ' +
                comm + ' ' +
                state + ' ' +
                ppid + ' ' +
                pgrp + ' ' +
                session + ' ' +
                ttyNr + ' ' +
                tpgid + ' ' +
                flags + ' ';
    }
}
