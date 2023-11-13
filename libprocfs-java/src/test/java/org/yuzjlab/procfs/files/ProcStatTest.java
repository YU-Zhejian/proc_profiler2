package org.yuzjlab.procfs.files;

import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.FileNotFoundException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ProcStatTest {

    @Test
    void getTrailingLong() throws FileNotFoundException, ProcessBaseException {
        var statPath = new TestHelper().resolveResources("/proc_stat.txt");
        var stat = new ProcStat(statPath);
        assertEquals(stat.cpuUser, 305556L);
        assertEquals(stat.cpuNice, 1431L);
        assertEquals(stat.cpuSystem, 80662L);
    }
}
/**
 *         this.cpuIdle = scn.nextLong();
 *         this.cpuIOWait = scn.nextLong();
 *         this.cpuIrq = scn.nextLong();
 *         this.cpuSoftIrq = scn.nextLong();
 *         this.cpuSteal = scn.nextLong();
 *         this.cpuGuest = scn.nextLong();
 *         this.cpuGuestNice = scn.nextLong();
 *         this.ctxt = getTrailingLong(lines, "ctxt");
 *         this.process = getTrailingLong(lines, "process");
 *         this.procsBlocked = getTrailingLong(lines, "process_blocked");
 *         this.procsRunning = getTrailingLong(lines, "process_running");
 *         this.btime = getTrailingLong(lines, "btime");
 */