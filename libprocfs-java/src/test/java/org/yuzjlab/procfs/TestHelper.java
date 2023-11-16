package org.yuzjlab.procfs;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class TestHelper {
  public Path resolveResources(String name) throws FileNotFoundException {
    var resource = this.getClass().getResource(name);
    if (resource == null) {
      throw new FileNotFoundException();
    }
    return Path.of(resource.getFile());
  }
}
