package org.yuzjlab.procfs.files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

/**
 * This file reports statistics about memory usage on the system. It is used by free(1) to report
 * the amount of free and used memory (both physical and swap) on the system as well as the shared
 * memory and buffers used by the kernel. Each line of the file consists of a parameter name,
 * followed by a colon, the value of the parameter, and an option unit of measurement (e.g.,
 * "KBytes"). The list below describes the parameter names and the format specifier required to read
 * the field value. Except as noted below, all of the fields have been present since at least Linux
 * 2.6.0. Some fields are displayed only if the kernel was configured with various options; those
 * dependencies are noted in the list.
 */
public class ProcMemInfo {
  /** Total usable RAM (i.e., physical RAM minus a few reserved bits and the kernel binary code). */
  protected final long memTotalKBytes;

  /** The sum of LowFree+HighFree. Highmem is all memory above ~860 MB of physical memory. */
  protected final long memFreeKBytes;

  /**
   * An estimate of how much memory is available for starting new applications, without swapping.
   */
  protected final long memAvailiableKBytes;

  /**
   * Relatively temporary storage for raw disk blocks that shouldn't get tremendously large (20 MB
   * or so).
   */
  protected final long buffersKBytes;

  /** In-memory cache for files read from the disk (the page cache). Doesn't include SwapCached. */
  protected final long cachedKBytes;

  /**
   * Memory that once was swapped out, is swapped back in but still also is in the swap file. (If
   * memory pressure is high, these pages don't need to be swapped out again because they are
   * already in the swap file. This saves I/O.)
   */
  protected final long swapCachedKBytes;

  /**
   * Memory that has been used more recently and usually not reclaimed unless absolutely necessary
   */
  protected final long activeKBytes;

  /**
   * Memory which has been less recently used. It is more eligible to be reclaimed for other
   * purposes.
   */
  protected final long inactiveKBytes;

  /** Total amount of swap space available. */
  protected final long swapTotalKBytes;

  /** Amount of swap space that is currently unused. */
  protected final long swapFreeKBytes;

  public ProcMemInfo(Path pathToMeminfo) throws ProcessBaseException {
    var minfoHM = new HashMap<String, Long>();
    try (var br = new BufferedReader(new FileReader((pathToMeminfo.toFile())))) {
      for (Iterator<String> it = br.lines().iterator(); it.hasNext(); ) {
        var line = it.next();
        var lb = line.strip().split(":");
        var key = lb[0];
        var value = Long.parseLong(lb[1].strip().split(" ")[0]);
        minfoHM.put(key, value);
      }
    } catch (IOException e) {
      throw ProcessUtils.resolveIOException(e);
    } catch (NumberFormatException e) {
      throw ProcessUtils.resolveIOException(new IOException(e));
    }
    this.activeKBytes = minfoHM.getOrDefault("Active", -1L);
    this.buffersKBytes = minfoHM.getOrDefault("Buffers", -1L);
    this.memFreeKBytes = minfoHM.getOrDefault("MemFree", -1L);
    this.memTotalKBytes = minfoHM.getOrDefault("MemTotal", -1L);
    this.memAvailiableKBytes = minfoHM.getOrDefault("MemAvailable", -1L);
    this.cachedKBytes = minfoHM.getOrDefault("Cached", -1L);
    this.swapCachedKBytes = minfoHM.getOrDefault("SwapCached", -1L);
    this.swapFreeKBytes = minfoHM.getOrDefault("SwapFree", -1L);
    this.swapTotalKBytes = minfoHM.getOrDefault("SwapTotal", -1L);
    this.inactiveKBytes = minfoHM.getOrDefault("Inactive", -1L);
  }
}
