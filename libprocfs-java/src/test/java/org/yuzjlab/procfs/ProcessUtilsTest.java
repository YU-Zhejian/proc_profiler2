package org.yuzjlab.procfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.management.ManagementFactory;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcessUtilsTest {

  @Test
  void getCurrentPid() throws ProcessBaseException {
    var runtime = ManagementFactory.getRuntimeMXBean();
    long rtPid = runtime.getPid();
    assertEquals(ProcessUtils.getCurrentPid(), rtPid);
  }
}
