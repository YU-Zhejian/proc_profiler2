package org.yuzjlab.procfs.files;

import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcPidEnvironTest {

    @Test
    void parseEnviron() throws FileNotFoundException, ProcessBaseException {
        var environPath = new TestHelper().resolveResources("/environ.txt");
        var parsedEnviron = ProcPidEnviron.parseEnviron(environPath);
        assertEquals(
                "zh_CN.UTF-8",
                parsedEnviron.get("LC_MEASUREMENT")
        );
        assertEquals(
                "/home/yuzj",
                parsedEnviron.get("HOME")
        );
    }
}
