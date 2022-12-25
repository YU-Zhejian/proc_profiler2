package org.yuzjlab.procfs.files;

import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatTest {

    @Test
    void assertRead() throws ProcessBaseException, IOException {
        var statPath = new TestHelper().resolveResources("/stat.txt");
        var stat = new Stat(statPath);
        var fileStr = new String(Files.readAllBytes(statPath));
        assertEquals(fileStr, stat.toString());
    }
}
