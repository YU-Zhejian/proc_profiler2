package org.yuzjlab.procfs.files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

/**
       /proc/pid/io (since Linux 2.6.20)
              This file contains I/O statistics for the process, for
              example:

                  # cat /proc/3828/io
                  rchar: 323934931
                  wchar: 323929600
                  syscr: 632687
                  syscw: 632675
                  read_bytes: 0
                  write_bytes: 323932160
                  cancelled_write_bytes: 0

              The fields are as follows:

              rchar: characters read
                     The number of bytes which this task has caused to
                     be read from storage.  This is simply the sum of
                     bytes which this process passed to read(2) and
                     similar system calls.  It includes things such as
                     terminal I/O and is unaffected by whether or not
                     actual physical disk I/O was required (the read
                     might have been satisfied from pagecache).

              wchar: characters written
                     The number of bytes which this task has caused, or
                     shall cause to be written to disk.  Similar caveats
                     apply here as with rchar.

              syscr: read syscalls
                     Attempt to count the number of read I/O operations
                     that is, system calls such as read(2) and pread(2).

              syscw: write syscalls
                     Attempt to count the number of write I/O
                     operations—that is, system calls such as write(2)
                     and pwrite(2).

              read_bytes: bytes read
                     Attempt to count the number of bytes which this
                     process really did cause to be fetched from the
                     storage layer.  This is accurate for block-backed
                     filesystems.

              write_bytes: bytes written
                     Attempt to count the number of bytes which this
                     process caused to be sent to the storage layer.

              cancelled_write_bytes:
                     The big inaccuracy here is truncate.  If a process
                     writes 1 MB to a file and then deletes the file, it
                     will in fact perform no writeout.  But it will have
                     been accounted as having caused 1 MB of write.  In
                     other words: this field represents the number of
                     bytes which this process caused to not happen, by
                     truncating pagecache.  A task can cause "negative"
                     I/O too.  If this task truncates some dirty
                     pagecache, some I/O which another task has been
                     accounted for (in its write_bytes) will not be
                     happening.

              Note: In the current implementation, things are a bit racy
              on 32-bit systems: if process A reads process B's
              /proc/pid/io while process B is updating one of these
              64-bit counters, process A could see an intermediate
              result.

              Permission to access this file is governed by a ptrace
              access mode PTRACE_MODE_READ_FSCREDS check; see ptrace(2).

 */
public class ProcPidIo {
    public final long readChars;
    public final long writeChars;
    public final long readSyscalls;
    public final long writeSyscalls;
    public final long readBytes;
    public final long writeBytes;
    public final long cancelledWriteBytes;

    public ProcPidIo(Path pathOfIO) throws ProcessBaseException {
        try(var br = new BufferedReader(new FileReader((pathOfIO.toFile())))) {
            long[] ios = new long[7];
            for (var i = 0; i < ios.length; i++){
                var l = Long.parseLong(br.readLine().split(" ")[1]);
                ios[i] = l;
            }
            this.readChars = ios[0];
            this.writeChars = ios[1];
            this.readSyscalls = ios[2];
            this.writeSyscalls = ios[3];
            this.readBytes = ios[4];
            this.writeBytes = ios[5];
            this.cancelledWriteBytes = ios[6];
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        } catch (NumberFormatException e){
            throw ProcessUtils.resolveIOException(new IOException(e));
        }
    }
}
