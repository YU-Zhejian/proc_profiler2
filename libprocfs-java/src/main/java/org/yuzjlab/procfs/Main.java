package org.yuzjlab.procfs;

import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        var systemProperties = System.getProperties();
        var lh = LoggerFactory.getLogger("Test");

        var osName = systemProperties.get("os.name");
        if (!Objects.equals(osName, "Linux")) {
            lh.warn("Detected operating system {}, which is not Linux", osName);
        }

        var osArch = systemProperties.get("os.arch");
        if (!Objects.equals(osArch, "amd64")) {
            lh.warn("Detected operating system architecture {}, which is not amd64", osArch);
        }

        var osVer = systemProperties.get("os.version");
        lh.error("OS: {} {} ver. {}", osName, osArch, osVer);
        lh.error(
                "Java: {} ver. {} (Spec. ver. {}) by {} with JAVAHOME={}",
                systemProperties.get("java.runtime.name"),
                systemProperties.get("java.version"),
                systemProperties.get("java.specification.version"),
                systemProperties.get("java.vendor"),
                systemProperties.get("java.home")
        );

        lh.error("ERR!");
    }
}
