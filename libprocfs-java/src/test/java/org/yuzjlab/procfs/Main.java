package org.yuzjlab.procfs;

import org.slf4j.LoggerFactory;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.process_info.EagerEvaluatedProcessInfo;

import java.util.Objects;
import java.util.stream.StreamSupport;

public class Main {
    public static void main(String[] args) throws ProcessBaseException {
        var systemProperties = System.getProperties();
        var lh = LoggerFactory.getLogger(Main.class.getCanonicalName());

        var osName = systemProperties.get("os.name");
        if (!Objects.equals(osName, "Linux")) {
            lh.warn("Detected operating system '{}', which is not Linux", osName);
        }

        var osArch = systemProperties.get("os.arch");
        if (!Objects.equals(osArch, "amd64")) {
            lh.warn("Detected operating system architecture {}, which is not amd64", osArch);
        }

        var osVer = systemProperties.get("os.version");
        lh.info("OS: '{}' arch. {} ver. '{}'", osName, osArch, osVer);
        lh.info(
                "Java: '{}' ver. '{}' (Spec. ver. {}) by '{}' with JAVAHOME='{}'",
                systemProperties.get("java.runtime.name"),
                systemProperties.get("java.version"),
                systemProperties.get("java.specification.version"),
                systemProperties.get("java.vendor"),
                systemProperties.get("java.home")
        );

        var rt = Runtime.getRuntime();
        lh.info("JVM Memory: Free/Total/Max {}/{}/{}", rt.freeMemory(), rt.totalMemory(), rt.maxMemory());

        try{
            ProcessUtils.getProcfsPath();
        }
        catch (ProcessBaseException processBaseException){
            lh.error(processBaseException.getMessage());
            System.exit(1);
        }

        var p = new EagerEvaluatedProcessInfo(ProcessUtils.getCurrentPid());
        lh.info("{} ({}/{})", p.getName(), p.getPid(), p.getState());
        lh.info("PPID: {}", p.getPPID());

        var cmdLine = String.join(" ", p.getCmdLine());
        lh.info("CMDLINE: {}", cmdLine);

        lh.info("EXEPATH: {}", p.getExePath());

        var children = String.join(
                " ",
                StreamSupport
                        .stream(p.getChildPIDs().spliterator(), false)
                        .map(String::valueOf)
                        .toList()
        );
        lh.info(
                "CHILDREN ({}): {}",
                p.getNumChildProcess(),
                children
        );

        lh.info("MMAP:");
        for (var mmapItem :  p.getMemoryMap()){
            lh.info("\t{}", mmapItem);
        }

        var io = p.getIO();
        lh.info(
                "IO: rchar={}, wchar={}, rsyscall={}, wsyscall={}, rbytes={}, wbytes={}, cwbytes={}",
                io.readChars, io.writeChars,
                io.readSyscalls, io.writeSyscalls,
                io.readBytes, io.writeBytes,
                io.cancelledWriteBytes
        );

        lh.info("FD:");
        var fd = p.getFileDescriptors();
        for (var fdItem :  fd.entrySet()){
            lh.info("\t{}: {}", fdItem.getKey(), fdItem.getValue());
        }

        lh.info("CPU: {}% on {}", Math.round(p.getCPUPercent(5) * 100), p.getOnWhichCPU());

        lh.info("Running libprocfs-java testing code FINISHED");
    }
}
