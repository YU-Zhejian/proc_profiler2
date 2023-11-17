package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcPidEnvironTest {

    @Test
    void parseEnviron() throws FileNotFoundException, ProcessBaseException {
        var environPath = new TestHelper().resolveResources("/proc_pid_environ.txt");
        var parsedEnviron = ProcPidEnviron.parseEnviron(environPath);
        assertEquals("zh_CN.UTF-8", parsedEnviron.get("LC_MEASUREMENT"));
        assertEquals("/home/yuzj", parsedEnviron.get("HOME"));
    }
}
