package org.yuzjlab.procfs.files;

import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>/proc/[pid]/stat</code>
 * <p>
 * Descriptions from <a href="https://man7.org/linux/man-pages/man5/proc.5.html">manual pages</a>.
 * <p>
 * This file contains the initial environment
 * that was set when the currently executing program was started via <code>execve(2)</code>.
 * The entries are separated by null bytes ('\0'), and there may be a null byte at the end.
 * Thus, to print out the environment of process 1, you would do:
 * <p>
 * $ <b>cat /proc/1/environ | tr '\000' '\n'</b>
 * <p>
 * If, after an <code>execve(2)</code>,
 * the process modifies its environment (e.g., by calling functions such as <code>putenv(3)</code>
 * or modifying the <code>environ(7)</code> variable directly), this file will <i>not</i> reflect those changes.
 * <p>
 * Furthermore,
 * a process may change the memory location that this file refers via <code>prctl(2)</code> operations such as
 * <b>PR_SET_MM_ENV_START</b>.
 * <p>
 * Permission to access this file is governed by a ptrace access mode <b>PTRACE_MODE_READ_FSCREDS </b>check; see <code>ptrace(2)</code>.
 */
public final class Environ {
    private Environ() {
    }

    /**
     * Parse <code>/proc/[pid]/environ</code> file.
     *
     * @param pathToEnviron Path to the file that will be parsed
     * @return Parsed environment file as a Map,
     * with environment variable name as key and environment variable value as value.
     */
    public static Map<String, String> parseEnviron(Path pathToEnviron) throws ProcessBaseException {
        var environMap = new HashMap<String, String>();
        try {
            var environLine = Files.readString(pathToEnviron);
            for (var environKeyValue : environLine.split("\\u0000")) {
                var sepIdx = environKeyValue.indexOf('=');
                var envKey = environKeyValue.substring(0, sepIdx);
                var envValue = environKeyValue.substring(sepIdx + 1);
                environMap.put(envKey, envValue);
            }
            return environMap;
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
    }
}
