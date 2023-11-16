package org.yuzjlab.procfs.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.TreeSet;
import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

public class ProcPidMaps {
  private ProcPidMaps() {}

  public static Iterable<String> parseMMap(Path pathOfMMap) throws ProcessBaseException {
    var fns = new TreeSet<String>();
    try (var fs = Files.newDirectoryStream(pathOfMMap)) {
      for (var mmapItemPath : fs) {
        var rpath = ProcessUtils.resolveRealPath(mmapItemPath);
        if (!rpath.equals(mmapItemPath.toString())) {
          fns.add(rpath);
        }
      }
      return fns;
    } catch (IOException e) {
      throw ProcessUtils.resolveIOException(e);
    }
  }

  private static String getLastToken(String line) {
    Scanner scn = new Scanner(line);
    String last = null;
    while (scn.hasNext()) {
      last = scn.next();
    }
    return last;
  }

  public static Iterable<String> parseMaps(Path pathOfMaps) throws ProcessBaseException {
    try {
      return new TreeSet<>(
          Files.readAllLines(pathOfMaps).stream()
              .map(ProcPidMaps::getLastToken)
              .filter((String s) -> !s.equals("0"))
              .toList());
    } catch (IOException e) {
      throw ProcessUtils.resolveIOException(e);
    }
  }
}
