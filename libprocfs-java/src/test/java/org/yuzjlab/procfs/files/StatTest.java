package org.yuzjlab.procfs.files;

import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatTest {

    @Test
    void assertRead() throws ProcessBaseException, IOException {
        var tmpfile = File.createTempFile("temp", ".stat");
        var outStream = new FileOutputStream(tmpfile.getPath());
        outStream.write(
                ("11775 (bash) S 11754 11775 11775 34816 11793 4194304 934 7863 0 0 1 0 23 2 20 0 1 0 " +
                        "211367 9023488 1368 18446744073709551615 94266710634496 94266711547661 140724170495168 " +
                        "0 0 0 65536 3686404 1266761467 1 0 0 17 5 0 0 0 0 0 94266711792304 94266711840336 " +
                        "94266720636928 140724170500228 140724170500233 140724170500233 140724170502126 0\n"
                ).getBytes()
        );
        outStream.close();
        var stat = new Stat(tmpfile.toPath());
        var fileStr = new String(Files.readAllBytes(tmpfile.toPath()));
        assertEquals(fileStr, stat.toString());
        if (!tmpfile.delete()) {
            throw new IOException();
        }
    }
}
