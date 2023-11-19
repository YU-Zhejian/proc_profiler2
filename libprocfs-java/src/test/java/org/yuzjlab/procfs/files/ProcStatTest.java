package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcStatTest {

    @Test
    void assertRead() throws FileNotFoundException, ProcessBaseException {
        var statPath = new TestHelper().resolveResources("proc_stat.txt");
        var stat = new ProcStat(statPath);
        assertEquals(378312L, stat.cpuUser);
        assertEquals(1431L, stat.cpuNice);
        assertEquals(102527L, stat.cpuSystem);
        assertEquals(12358934L, stat.cpuIdle);
        assertEquals(2158L, stat.cpuIOWait);
        assertEquals(0L, stat.cpuIrq);
        assertEquals(8000L, stat.cpuSoftIrq);
        assertEquals(0L, stat.cpuSteal);
        assertEquals(0L, stat.cpuGuest);
        assertEquals(0L, stat.cpuGuestNice);
        assertEquals(59483848L, stat.ctxt);
        assertEquals(33812L, stat.process);
        assertEquals(0L, stat.procsBlocked);
        assertEquals(1L, stat.procsRunning);
        assertEquals(1699882876L, stat.btime);
    }
}
