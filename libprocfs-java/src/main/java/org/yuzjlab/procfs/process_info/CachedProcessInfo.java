package org.yuzjlab.procfs.process_info;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.Stat;
import org.yuzjlab.procfs.helper.Field;

import java.nio.file.Path;
import java.util.Map;

public class CachedProcessInfo extends BaseProcessInfo {
    protected final EagerEvaluatedProcessInfo eepi;
    protected Path exePathCache;
    protected float expireTime;
    protected String[] cmdLineCache;
    protected Path cwdPathCache;
    protected Field<Stat> statCache;
    protected Field<Map<Integer, String>> fileDescriptorsCache;
    protected Field<Long> numberOfFileDescriptorCache;
    protected Field<Map<String, String>> environmentVariableCache;

    protected CachedProcessInfo(long pid, float expireTime) {
        super(pid);
        this.eepi = new EagerEvaluatedProcessInfo(pid);
        this.expireTime = expireTime;
    }

    @Override
    public Path getExePath() throws ProcessBaseException {
        if (this.exePathCache == null) {
            this.exePathCache = eepi.getExePath();
        }
        return this.exePathCache;
    }

    @Override
    public Path getCwdPath() throws ProcessBaseException {
        if (this.cwdPathCache == null) {
            this.cwdPathCache = eepi.getCwdPath();
        }
        return this.cwdPathCache;
    }

    @Override
    public String[] getCmdLine() throws ProcessBaseException {
        if (this.cmdLineCache == null) {
            this.cmdLineCache = this.eepi.getCmdLine();
        }
        return this.cmdLineCache;
    }

    @Override
    public Map<String, String> getEnvironmentVariables() throws ProcessBaseException {
        if (this.environmentVariableCache == null) {
            this.environmentVariableCache = new Field<>(
                    this.eepi::getEnvironmentVariables, this.expireTime
            );
        }
        return this.environmentVariableCache.get();
    }

    @Override
    public long getPPID() throws ProcessBaseException {
        return 0;// TODO
    }

    @Override
    public String getName() throws ProcessBaseException {
        return this.getStat().comm;
    }

    @Override
    public void getMemoryMap() {
        // TODO
    }

    @Override
    public Iterable<Integer> getChildPIDs() {
        return null;// TODO
    }

    @Override
    public int getNumChildProcess() {
        return 0; // TODO
    }

    @Override
    public int getOnWhichCPU() throws ProcessBaseException {
        return this.getStat().processor;
    }

    @Override
    public float getCPUTime() {
        return 0;// TODO
    }

    @Override
    public float getCPUPercent(float waitNSeconds) {
        return 0;// TODO
    }

    @Override
    public Map<Integer, String> getFileDescriptors() throws ProcessBaseException {
        if (this.fileDescriptorsCache == null) {
            this.fileDescriptorsCache = new Field<>(
                    this.eepi::getFileDescriptors, this.expireTime
            );
        }
        return this.fileDescriptorsCache.get();
    }

    @Override
    public long getNumberOfFileDescriptors() throws ProcessBaseException {
        if (this.numberOfFileDescriptorCache == null) {
            this.numberOfFileDescriptorCache = new Field<>(
                    this.eepi::getNumberOfFileDescriptors, this.expireTime
            );
        }
        return this.numberOfFileDescriptorCache.get();
    }

    @Override
    public void getIO() {
// TODO
    }

    @Override
    public void getMemoryInformation() {
// TODO
    }

    @Override
    public Stat getStat() throws ProcessBaseException {
        if (this.statCache == null) {
            this.statCache = new Field<>(
                    this.eepi::getStat, this.expireTime
            );
        }
        return this.statCache.get();
    }

    @Override
    public char getState() throws ProcessBaseException {
        return this.getStat().state;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
