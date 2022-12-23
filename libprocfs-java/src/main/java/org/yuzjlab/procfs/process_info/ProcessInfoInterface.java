package org.yuzjlab.procfs.process_info;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.Stat;

import java.nio.file.Path;
import java.util.HashMap;

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
     * Get all environment variables
     */
    HashMap<String, String> getEnvironmentVariables() throws ProcessBaseException;

    /**
     * Get PID of parent process
     */
    long getPPID() throws ProcessBaseException;

    /**
     * Get process name
     */
    String getName() throws ProcessBaseException;

    void getMemoryMap();

    Iterable<Integer> getChildPIDs();

    int getNumChildProcess();

    int getOnWhichCPU();

    float getCPUTime();

    float getCPUPercent(float waitNSeconds);

    HashMap<Integer, String> getFileDescriptors();

    void getIO();

    void getMemoryInformation();

    Stat getStat() throws ProcessBaseException;

    void getStatus();

    @Override
    int hashCode();

    @Override
    boolean equals(Object o);

    @Override
    String toString();
}
