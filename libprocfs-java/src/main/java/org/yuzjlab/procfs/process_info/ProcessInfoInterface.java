package org.yuzjlab.procfs.process_info;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.ProcPidIo;
import org.yuzjlab.procfs.files.ProcPidStat;

import java.nio.file.Path;
import java.util.Map;

public interface ProcessInfoInterface {
    /**
     * @return Whether the process still exists in /proc
     */
    boolean isAlive();

    /**
     * Get path to the executable
     */
    Path getExePath() throws ProcessBaseException;

    /**
     * Get Current Working Directory (CWD) of a process
     */
    Path getCwdPath() throws ProcessBaseException;

    /**
     * Get Process ID (PID)
     */
    long getPid();

    /**
     * Get Commandline arguments
     */
    String[] getCmdLine() throws ProcessBaseException;

    /**
     * Get all environment variables in Map,
     * with environment variable name as key and environment variable value as value.
     * <p>
     * WARNING: This map would only represent the state when the process is started.
     * It would <i>not</i> be changed if the executable inside changed its environment by system calls.
     */
    Map<String, String> getEnvironmentVariables() throws ProcessBaseException;

    /**
     * Get PID of parent process
     */
    long getPPID() throws ProcessBaseException;

    /**
     * Get process name.
     * <p>
     * The process name should be the executable name trimmed to 16 characters.
     */
    String getName() throws ProcessBaseException;

    Iterable<String> getMemoryMap() throws ProcessBaseException;

    Iterable<Integer> getChildPIDs() throws ProcessBaseException;

    long getNumChildProcess() throws ProcessBaseException;

    int getOnWhichCPU() throws ProcessBaseException;

    float getCPUTime() throws ProcessBaseException;

    float getCPUPercent(float waitNSeconds) throws ProcessBaseException;

    Map<Integer, String> getFileDescriptors() throws ProcessBaseException;

    long getNumberOfFileDescriptors() throws ProcessBaseException;

    ProcPidIo getIO() throws ProcessBaseException;

    void getMemoryInformation();

    ProcPidStat getStat() throws ProcessBaseException;

    char getState() throws ProcessBaseException;
}
