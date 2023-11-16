package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcPidMapsTest {

  @Test
  void parseMaps() throws FileNotFoundException, ProcessBaseException {
    var mapsPath = new TestHelper().resolveResources("/proc_pid_maps.txt");
    var parsedMMap = new TreeSet<String>();
    parsedMMap.add("/bin/sh");
    parsedMMap.add("/lib/libedit.so.8");
    parsedMMap.add("/lib/libc.so.7");
    parsedMMap.add("/lib/libncursesw.so.9");
    parsedMMap.add("/libexec/ld-elf.so.1");
    for (var returnedPath : ProcPidMaps.parseMaps(mapsPath)) {
      parsedMMap.remove(returnedPath);
    }
    assertTrue(parsedMMap.isEmpty());
  }
}
