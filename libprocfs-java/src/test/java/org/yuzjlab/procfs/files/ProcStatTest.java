package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcStatTest {

    @Test
    void assertRead() throws FileNotFoundException, ProcessBaseException {
        var statPath = new TestHelper().resolveResources("/proc_stat.txt");
        var stat = new ProcStat(statPath);
        assertEquals(stat.cpuUser, 378312L);
        assertEquals(stat.cpuNice, 1431L);
        assertEquals(stat.cpuSystem, 102527L);
        assertEquals(stat.cpuIdle, 12358934L);
        assertEquals(stat.cpuIOWait, 2158L);
        assertEquals(stat.cpuIrq, 0L);
        assertEquals(stat.cpuSoftIrq, 8000L);
        assertEquals(stat.cpuSteal, 0L);
        assertEquals(stat.cpuGuest, 0L);
        assertEquals(stat.cpuGuestNice, 0L);
        assertEquals(stat.ctxt, 59483848L);
        assertEquals(stat.process, 33812L);
        assertEquals(stat.procsBlocked, 0L);
        assertEquals(stat.procsRunning, 1L);
        assertEquals(stat.btime, 1699882876L);
    }
}
