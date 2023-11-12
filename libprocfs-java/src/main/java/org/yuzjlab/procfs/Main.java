package org.yuzjlab.procfs;

import org.slf4j.LoggerFactory;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.process_info.EagerEvaluatedProcessInfo;

import java.util.Objects;
import java.util.stream.StreamSupport;

public class Main {
    public static void main(String[] args) throws ProcessBaseException {
        System.err.println("Running libprocfs-java testing code ...");
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

        var p = new EagerEvaluatedProcessInfo(ProcessUtils.getCurrentPid());
        lh.info("PID: {}", p.getPid());
        lh.info("CMDLINE: {}", String.join(" ", p.getCmdLine()));
        lh.info("EXEPATH: {}", p.getExePath());
        lh.info(
                "CHILDREN ({}): {}",
                p.getNumChildProcess(),
                String.join(
                        " ",
                        StreamSupport
                                .stream(p.getChildPIDs().spliterator(), false)
                                .map(String::valueOf)
                                .toList()
                )
        );
        System.err.println("Running libprocfs-java testing code FINISHED");
    }
}
