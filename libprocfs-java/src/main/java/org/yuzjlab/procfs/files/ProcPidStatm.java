package org.yuzjlab.procfs.files;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.SystemInfo;
import org.yuzjlab.procfs.exception.ProcessBaseException;

/**
 * Provides information about memory usage, measured in pages.
 */
public class ProcPidStatm {

  /** total program size (same as VmSize in /proc/pid/status) */
  public final long size;

  public long getSizeBytes(){
    return size * SystemInfo.PAGE_SIZE;
  }

  /** resident set size (inaccurate; same as VmRSS in /proc/pid/status) */
  public final long resident;

  public long getResidentBytes(){
    return resident * SystemInfo.PAGE_SIZE;
  }

  /**
   * number of resident shared pages (i.e., backed by a file) (inaccurate; same as RssFile+RssShmem
   * in /proc/pid/status)
   */
  public final long shared;

  public long getSharedBytes(){
    return shared * SystemInfo.PAGE_SIZE;
  }

  /** text (code) */
  public final long text;

  public long getTextBytes(){
    return text * SystemInfo.PAGE_SIZE;
  }


  /** data + stack */
  public final long data;

  public long getDataBytes(){
    return data * SystemInfo.PAGE_SIZE;
  }


  public ProcPidStatm(Path pathToStatm) throws ProcessBaseException {
    try (var scn = new Scanner(new FileReader(String.valueOf(pathToStatm)))) {
      this.size = scn.nextLong();
      this.resident = scn.nextLong();
      this.shared = scn.nextLong();
      this.text = scn.nextLong();
      scn.nextLong(); // library (unused since Linux 2.6; always 0)
      this.data = scn.nextLong();
      scn.nextLong(); // dirty pages (unused since Linux 2.6; always 0)
    } catch (IOException e) {
      throw ProcessUtils.resolveIOException(e);
    } catch (NumberFormatException e) {
      throw ProcessUtils.resolveIOException(new IOException(e));
    }
  }
}
