package org.yuzjlab.procfs.files;

import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * <code>/proc/[pid]/fd</code>
 * <p>
 * Descriptions from <a href="https://man7.org/linux/man-pages/man5/proc.5.html">manual pages</a>.
 * <p>
 * This is a subdirectory containing one entry for each file which the process has open, named by its file descriptor,
 * and which is a symbolic link to the actual file.
 * Thus, 0 is standard input, 1 standard output, 2 standard error, and so on.
 * <p>
 * For file descriptors for pipes and sockets,
 * the entries will be symbolic links whose content is the file type with the inode.
 * A <code>readlink(2)</code> call on this file returns a string in the format:
 * <p>
 * type:[inode]
 * <p>
 * For example, <i>socket:[2248868]</i> will be a socket and its inode is 2248868.
 * For sockets, that inode can be used to find more information in one of the files under
 * <i>/proc/net/</i>.
 * <p>
 * For file descriptors that have no corresponding inode (e.g., file descriptors produced by <code>bpf(2)</code>,
 * <code>epoll_create(2)</code>, <code>eventfd(2)</code>, <code>inotify_init(2)</code>,
 * <code>perf_event_open(2)</code>, <code>signalfd(2)</code>, <code>timerfd_create(2)</code>,
 * and <code>userfaultfd(2)</code>), the entry will be a symbolic link with contents of the form
 * <p>
 * anon_inode:&lt;file-type&gt;
 * <p>
 * In many cases (but not all), the <i>file-type</i> is surrounded by square brackets.
 * <p>
 * For example, an epoll file descriptor will have a symbolic link whose content is the string <i>anon_inode:[eventpoll]</i>.
 * <p>
 * In a multithreaded process,
 * the contents of this directory are not available if the main thread has already terminated
 * (typically by calling <code>pthread_exit(3)</code>).
 * <p>
 * Programs that take a filename as a command-line argument,
 * but don't take input from standard input if no argument is supplied,
 * and programs that write to a file named as a command-line argument,
 * but don't send their output to standard output if no argument is supplied,
 * can nevertheless be made to use standard input or standard output
 * by using <i>/proc/[pid]/fd</i> files as command-line arguments.
 * For example, assuming that <i>-i</i> is the flag designating an input file and
 * <i>-o</i> is the flag designating
 * an output file:
 * <p>
 * $ <b>foobar -i /proc/self/fd/0 -o /proc/self/fd/1 ...</b>
 * <p>
 * and you have a working filter.
 * <p>
 * <i>/proc/self/fd/N</i> is approximately the same as <i>/dev/fd/N</i> in some UNIX and UNIX-like systems.
 * Most Linux MAKEDEV scripts symbolically link <i>/dev/fd</i> to <i>/proc/self/fd</i>, in fact.
 * <p>
 * Most systems provide symbolic links <i>/dev/stdin</i>, <i>/dev/stdout</i>, and <i>/dev/stderr</i>,
 * which respectively link to the files <i>0</i>, <i>1</i>, and <i>2</i> in <i>/proc/self/fd</i>.
 * Thus the example command above could be written as:
 * <p>
 * $ <b>foobar -i /dev/stdin -o /dev/stdout ...</b>
 * <p>
 * Permission to dereference or read (<code>readlink(2)</code>) the symbolic links in this directory is governed by a
 * ptrace access mode <b>PTRACE_MODE_READ_FSCREDS </b>check; see <code>ptrace(2)</code>.
 * <p>
 * Note that for file descriptors referring to inodes (pipes and sockets, see above),
 * those inodes still have permission bits and ownership information distinct from
 * those of the <i>/proc/[pid]/fd</i> entry,
 * and that the owner may differ from the user and group IDs of the process.
 * An unprivileged process may lack permissions to open them, as in this example:
 * <p>
 * $ <b>echo test | sudo -u nobody cat</b>
 * test
 * $ <b>echo test | sudo -u nobody cat /proc/self/fd/0</b>
 * cat: /proc/self/fd/0: Permission denied
 * <p>
 * File descriptor 0 refers to the pipe created by the shell and owned by that shell's user,
 * which is not <i>nobody</i>,
 * so <b>cat </b>does not have permission to create a new file descriptor to read from that inode,
 * even though it can still read from its existing file descriptor 0.
 */
public final class FD {
    private FD() {
    }

    /**
     * Return actual paths to file descriptors of a process.
     *
     * @return A map whose key is file descriptors and values are real paths to that descriptor.
     */
    public static Map<Integer, String> parseFD(Path pathOfFD) throws ProcessBaseException {
        var parsedFD = new HashMap<Integer, String>();
        try (var dstream = Files.newDirectoryStream(pathOfFD)) {
            for (var path : dstream) {
                var descriptor = Integer.parseInt(String.valueOf(path.getFileName()));
                var realPath = ProcessUtils.resolveRealPath(path);
                parsedFD.put(descriptor, realPath);
            }
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
        return parsedFD;
    }

    /**
     * Get number of opened file descriptors of a process.
     */
    public static long getNumberOfFD(Path pathOfFD) throws ProcessBaseException {
        try (var dstream = Files.newDirectoryStream(pathOfFD)) {
            return StreamSupport.stream(dstream.spliterator(), false).count();
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
    }
}
