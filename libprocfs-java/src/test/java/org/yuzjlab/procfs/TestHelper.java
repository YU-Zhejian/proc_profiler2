package org.yuzjlab.procfs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class TestHelper {

    @Test
    void testMySelf() {
        var f =
                new File(
                        Objects.requireNonNull(
                                        this.getClass().getClassLoader().getResource("empty.txt"))
                                .getFile());
        assertTrue(f.exists());
    }

    public Path resolveResources(String name) throws FileNotFoundException {
        var resource = this.getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new FileNotFoundException();
        }
        return new File(resource.getFile()).toPath();
    }
}
