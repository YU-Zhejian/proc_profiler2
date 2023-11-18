package org.yuzjlab.proctracer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.SystemInfo;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.process_info.EagerEvaluatedProcessInfo;
import org.yuzjlab.proctracer.dispatcher.MainDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadFactory;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;

class FEOpts {
    public static final Option helpOption =
            Option.builder("h")
                    .longOpt("help")
                    .desc("Display this help")
                    .required(false)
                    .hasArg(false)
                    .build();
    public static final Option testMainOption =
            Option.builder()
                    .longOpt("test-main")
                    .desc("Run several bundled test cases")
                    .required(false)
                    .hasArg(false)
                    .build();
    public static final Option writeDefaultConfigOption =
            Option.builder()
                    .longOpt("write-default-config")
                    .desc(
                            "Write default config and exit."
                                    + "If --config is specified, will write to that file."
                                    + "Otherwise, will write to standard output.")
                    .required(false)
                    .hasArg(false)
                    .build();
    public static final Option pidOption =
            Option.builder("p")
                    .longOpt("pid")
                    .desc("Process ID to trace. If this option is set, the CMDS are ignored.")
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option compressFmtOption =
            Option.builder()
                    .longOpt("compress-fmt")
                    .desc(
                            "Whether to compress the output streams, default to false. Valid choices: [GZ, XZ]")
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option wdOption =
            Option.builder()
                    .longOpt("wd")
                    .desc(
                            "The working directory for the new process. "
                                    + "Only valid if [CMDS] are present. Default to current working directory.")
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option outdirOption =
            Option.builder("o")
                    .longOpt("outdir")
                    .desc("Output directory of the tracer.")
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option frontendRefreshFreqOption =
            Option.builder()
                    .longOpt("frontend-refresh-freq")
                    .desc("Frequency of refreshing the frontend, in seconds")
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option backendRefreshFreqOption =
            Option.builder()
                    .longOpt("backend-refresh-freq")
                    .desc("Default frequency of refreshing all the backends, in seconds")
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option suppressFrontendOption =
            Option.builder()
                    .longOpt("suppress-frontend")
                    .desc("Suppress the frontend. If set, --frontend-refresh-freq will be defunct.")
                    .required(false)
                    .hasArg(false)
                    .build();
    public static final Option configOption =
            Option.builder("c")
                    .longOpt("config")
                    .desc("Load default values from a configuration (.properties) file")
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option envOption =
            Option.builder()
                    .longOpt("env")
                    .desc(
                            "Path to a environment file where environment name and value separated using '='. "
                                    + "Only valid if [CMDS] are present. "
                                    + "Default to current environment.")
                    .required(false)
                    .hasArg(true)
                    .build();
    protected static final String STREAM_DESC =
            "File path of the standard %s stream to be redirected to the target process. "
                    + "Only valid if [CMDS] are present. Default to `/dev/null`.";
    public static final Option stdinOption =
            Option.builder()
                    .longOpt("stdin")
                    .desc(STREAM_DESC.formatted("input"))
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option stdoutOption =
            Option.builder()
                    .longOpt("stdout")
                    .desc(STREAM_DESC.formatted("output"))
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Option stderrOption =
            Option.builder()
                    .longOpt("stderr")
                    .desc(STREAM_DESC.formatted("error"))
                    .required(false)
                    .hasArg(true)
                    .build();
    public static final Options options =
            new Options()
                    .addOption(helpOption)
                    .addOption(writeDefaultConfigOption)
                    .addOption(pidOption)
                    .addOption(compressFmtOption)
                    .addOption(stdinOption)
                    .addOption(stdoutOption)
                    .addOption(stderrOption)
                    .addOption(envOption)
                    .addOption(wdOption)
                    .addOption(outdirOption)
                    .addOption(frontendRefreshFreqOption)
                    .addOption(backendRefreshFreqOption)
                    .addOption(suppressFrontendOption)
                    .addOption(configOption)
                    .addOption(testMainOption);

    FEOpts() {}

    public static void printHelp() {
        var formatter = new HelpFormatter();
        formatter.printHelp(
                "See below",
                """
                        YuZJ ProcTracer -- Process Tracer using libprocfs-java
                        Trace a PID: [EXE] -p pid [OPTS]
                        Trace a command: [EXE] [OPTS] -- [CMDS]

                        For example:
                        Trace process with PID 1000: [EXE] -p 1000
                        Trace new process `sleep 10`: [EXE] -- sleep 10
                        """,
                options,
                "END OF HELP MESSAGE");
    }

    public static String getOptionValueWithDefaults(CommandLine cmd, Option opt, String defaults) {
        var retv = cmd.getOptionValue(opt);
        if (retv == null) {
            return defaults;
        }
        return retv;
    }
}

public class Main {

    private static Map<String, String> parseEnv(File envPath) throws IOException {
        var envMap = new HashMap<String, String>();
        var lineNo = 0;
        for (var environKeyValue : Files.readAllLines(envPath.toPath())) {
            var sepIdx = environKeyValue.indexOf('=');
            if (sepIdx == -1) {
                throw new IOException(
                        "Line %d of File %s should have at least one '='"
                                .formatted(lineNo, envPath.toString()));
            }
            var envKey = environKeyValue.substring(0, sepIdx);
            var envValue = environKeyValue.substring(sepIdx + 1);
            envMap.put(envKey, envValue);
            lineNo += 1;
        }
        return envMap;
    }

    private static Pair<String[], String[]> splitArgsBeforeAfterCmd(String[] args) {
        ArrayList<String> argsBeforeCmdLine = new ArrayList<>();
        ArrayList<String> cmdLine = new ArrayList<>();
        boolean hadCmdLine = false;
        for (var arg : args) {
            if (arg.equals("--")) {
                hadCmdLine = true;
            } else if (hadCmdLine) {
                cmdLine.add(arg);
            } else {
                argsBeforeCmdLine.add(arg);
            }
        }
        String[] argsBeforeCmdLineArr = new String[argsBeforeCmdLine.size()];
        argsBeforeCmdLineArr = argsBeforeCmdLine.toArray(argsBeforeCmdLineArr);

        String[] cmdLineArr = new String[cmdLine.size()];
        cmdLineArr = cmdLine.toArray(cmdLineArr);
        return Pair.of(argsBeforeCmdLineArr, cmdLineArr);
    }

    @SuppressWarnings("java:S106") // I need to write something to stdout
    private static void writeDefaultConfig(CommandLine cmd, Logger lh) {
        String outPath = "null";
        try {
            var pconfig = new PropertiesConfiguration();
            pconfig.copy(TracerOpts.getDefaultConfig());
            Writer defConfOutWriter;
            if (cmd.hasOption(FEOpts.configOption)) {
                outPath = cmd.getOptionValue(FEOpts.configOption);
                defConfOutWriter = new FileWriter(outPath);
            } else {
                outPath = "stdout";
                defConfOutWriter = new OutputStreamWriter(System.out);
            }
            pconfig.write(defConfOutWriter);
        } catch (IOException | ConfigurationException e) {
            lh.error("Exception detected: %s".formatted(e.getMessage()));
            lh.error("Failed to write default configuration to '%s'!".formatted(outPath));
            lh.error(
                    "Suggested: Examine whether the directory containing destination file exists or whether you have enough permission.");
            System.exit(1);
        }
        System.exit(0);
    }

    private static ProcessSupervisorThreadInterface createPSST(
            CommandLine cmd, Logger lh, String[] cmdLineArr) {
        ProcessSupervisorThreadInterface psst = null;
        if (!cmd.hasOption(FEOpts.pidOption) && cmdLineArr.length == 0) {
            lh.error("At least one of the -p [PID] or [CMDS] needs to be specified!");
            System.exit(1);
        } else if (cmd.hasOption(FEOpts.pidOption) && cmdLineArr.length != 0) {
            lh.error("Only one of the -p [PID] or [CMDS] needs to be specified!");
            System.exit(1);
        } else if (cmd.hasOption(FEOpts.pidOption)) {
            var pidStr = cmd.getOptionValue(FEOpts.pidOption);
            var pid = -1L;

            try {
                pid = Long.parseLong(pidStr);
            } catch (NumberFormatException numberFormatException) {
                lh.error("Specified pid '{}' cannot be parsed!", pidStr);
                System.exit(1);
            }
            lh.info("Creating PSST from PID {}", pid);
            psst = ProcessSupervisorThreadFactory.create(pid);
        } else {
            Map<String, String> env = null;
            var envOptVal = cmd.getOptionValue(FEOpts.envOption);
            if (envOptVal != null) {
                try {
                    env = parseEnv(new File(envOptVal));
                } catch (IOException e) {
                    lh.error("--env file parse failed.");
                    System.exit(1);
                }
            } else {
                env = System.getenv();
                lh.info("--env not set; default current environment");
            }

            psst =
                    ProcessSupervisorThreadFactory.create(
                            cmdLineArr,
                            new File(
                                    FEOpts.getOptionValueWithDefaults(
                                            cmd, FEOpts.stdinOption, TracerOpts.DEVNULL)),
                            new File(
                                    FEOpts.getOptionValueWithDefaults(
                                            cmd, FEOpts.stdoutOption, TracerOpts.DEVNULL)),
                            new File(
                                    FEOpts.getOptionValueWithDefaults(
                                            cmd, FEOpts.stderrOption, TracerOpts.DEVNULL)),
                            new File(
                                    FEOpts.getOptionValueWithDefaults(
                                            cmd,
                                            FEOpts.stdinOption,
                                            new File(".").getAbsolutePath())),
                            env);
        }
        return psst;
    }

    public static Pair<ProcessSupervisorThreadInterface, TracerOpts> parseArgs(
            String[] argsBeforeCmdLineArr, String[] cmdLineArr) throws ParseException {
        var lh = LoggerFactory.getLogger(Main.class.getCanonicalName());
        var parser = new DefaultParser();
        var cmd = parser.parse(FEOpts.options, argsBeforeCmdLineArr);

        if (cmd.hasOption(FEOpts.helpOption)) {
            FEOpts.printHelp();
            System.exit(0);
        }

        if (cmd.hasOption(FEOpts.testMainOption)) {
            try {
                testMain();
            } catch (ProcessBaseException e) {
                lh.error("ProcessBaseException raised! Details: {}", e.getMessage());
                System.exit(-1);
            }
            System.exit(0);
        }

        if (cmd.hasOption(FEOpts.writeDefaultConfigOption)) {
            writeDefaultConfig(cmd, lh);
        }

        TracerOpts tracerOpts;
        String configPath = "null";
        try {
            if (cmd.hasOption(FEOpts.configOption)) {
                configPath = cmd.getOptionValue(FEOpts.configOption);
                tracerOpts = TracerOpts.load(new File(configPath));
            } else {
                configPath = "defaults";
                tracerOpts = new TracerOpts(TracerOpts.getDefaultConfig());
            }
        } catch (ConfigurationException | IOException e) {
            throw new ParseException(
                    "Failed to parse default configuration from %s!".formatted(configPath));
        }

        ProcessSupervisorThreadInterface psst = createPSST(cmd, lh, cmdLineArr);

        try {
            tracerOpts.setCompressFmt(cmd.getOptionValue(FEOpts.compressFmtOption));
        } catch (ConfigurationException e) {
            throw new ParseException("--compress should be one of [GZ, XZ] or unspecified.");
        }

        var outDirOptVal = cmd.getOptionValue(FEOpts.outdirOption);
        if (outDirOptVal == null) {
            throw new ParseException("--out-dir should be specified.");
        }
        tracerOpts.setOutDirPath(new File(outDirOptVal));
        return Pair.of(psst, tracerOpts);
    }

    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "US"));
        var lh = LoggerFactory.getLogger(Main.class.getCanonicalName());
        var argPair = splitArgsBeforeAfterCmd(args);
        ProcessSupervisorThreadInterface psst = null;
        TracerOpts topt = null;
        try {
            var psstToptPair = parseArgs(argPair.getLeft(), argPair.getRight());
            psst = psstToptPair.getLeft();
            topt = psstToptPair.getRight();
        } catch (ParseException parseException) {
            lh.error("Parsing failed.  Reason: {}", parseException.getMessage());
            FEOpts.printHelp();
            System.exit(1);
        }
        var mainDispatcher = new MainDispatcher(topt, psst);
        var mainDispatcherThread = new Thread(mainDispatcher, mainDispatcher.toString());
        mainDispatcherThread.start();
        try {
            mainDispatcherThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void testMain() throws ProcessBaseException {
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
                systemProperties.get("java.home"));

        var rt = Runtime.getRuntime();
        lh.info(
                "JVM Memory (KB): Free/Total/Max {}/{}/{}",
                rt.freeMemory() / 1024,
                rt.totalMemory() / 1024,
                rt.maxMemory() / 1024);

        try {
            lh.info("procfs identified at {}", ProcessUtils.getProcfsPath());
        } catch (ProcessBaseException processBaseException) {
            lh.error(processBaseException.getMessage());
            System.exit(1);
        }
        lh.info("Start displaying information of current system...");
        lh.info("POSIX Clock tick: {}, Page size: {}", SystemInfo.CLOCK_TICK, SystemInfo.PAGE_SIZE);

        lh.info("Start displaying information of current process...");

        var p = new EagerEvaluatedProcessInfo(ProcessUtils.getCurrentPid());
        lh.info("{} ({}/{})", p.getName(), p.getPid(), p.getState());
        lh.info("PPID: {}", p.getPPID());

        var cmdLine = String.join(" ", p.getCmdLine());
        lh.info("CMDLINE: {}", cmdLine);

        lh.info("EXEPATH: {}", p.getExePath());

        var children =
                String.join(
                        " ",
                        StreamSupport.stream(p.getChildPIDs().spliterator(), false)
                                .map(String::valueOf)
                                .toList());
        lh.info("CHILDREN ({}): {}", p.getNumChildProcess(), children);

        lh.info("MMAP:");
        for (var mmapItem : p.getMemoryMap()) {
            lh.info("\t{}", mmapItem);
        }

        var io = p.getIO();
        lh.info(
                "IO: rchar={}, wchar={}, rsyscall={}, wsyscall={}, rbytes={}, wbytes={}, cwbytes={}",
                io.readChars,
                io.writeChars,
                io.readSyscalls,
                io.writeSyscalls,
                io.readBytes,
                io.writeBytes,
                io.cancelledWriteBytes);

        lh.info("FD:");
        var fd = p.getFileDescriptors();
        for (var fdItem : fd.entrySet()) {
            lh.info("\t{}: {}", fdItem.getKey(), fdItem.getValue());
        }

        lh.info("CPU: {}% on {}", Math.round(p.getCPUPercent(5) * 100), p.getOnWhichCPU());

        var statm = p.getMemoryInformation();
        lh.info(
                "MEM (KB): Virtual={}, Resident={}, Shared={}, Text/Data={}/{}",
                statm.getSizeBytes() / 1024,
                statm.getResidentBytes() / 1024,
                statm.getSharedBytes() / 1024,
                statm.getTextBytes() / 1024,
                statm.getDataBytes() / 1024);

        lh.info("Running libprocfs-java testing code FINISHED");
    }
}
