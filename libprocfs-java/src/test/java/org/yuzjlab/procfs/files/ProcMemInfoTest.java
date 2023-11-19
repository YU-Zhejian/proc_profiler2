package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcMemInfoTest {
    @Test
    public void assertRead() throws FileNotFoundException, ProcessBaseException {
        var statPath = new TestHelper().resolveResources("proc_meminfo.txt");
        var stat = new ProcMemInfo(statPath);
        assertEquals(27560236L, stat.memAvailiableKBytes);
        assertEquals(5679124L, stat.activeKBytes);
        assertEquals(487748L, stat.buffersKBytes);
        assertEquals(24041916L, stat.memFreeKBytes);
        assertEquals(32479800L, stat.memTotalKBytes);
        assertEquals(27560236L, stat.memAvailiableKBytes);
        assertEquals(3357384L, stat.cachedKBytes);
        assertEquals(0L, stat.swapCachedKBytes);
        assertEquals(0L, stat.swapFreeKBytes);
        assertEquals(0L, stat.swapTotalKBytes);
        assertEquals(1860052L, stat.inactiveKBytes);
    }
}
