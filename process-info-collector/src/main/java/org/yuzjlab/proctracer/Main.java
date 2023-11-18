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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.proctracer.dispatcher.MainDispatcher;
import org.yuzjlab.proctracer.frontend.FrontendInterface;
import org.yuzjlab.proctracer.frontend.LogFrontend;
import org.yuzjlab.proctracer.frontend.NOPFrontend;
import org.yuzjlab.proctracer.frontend.SimpleFrontend;
import org.yuzjlab.proctracer.opts.FEOpts;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadFactory;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;
import org.yuzjlab.proctracer.utils.TestMain;

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
                TestMain.testMain();
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

        try {
            tracerOpts.setFrontEnd(cmd.getOptionValue(FEOpts.frontendImplOption));
        } catch (ConfigurationException e) {
            throw new ParseException(
                    "--frontendImpl should be one of [NOP, SIMPLE, LOG] or unspecified.");
        }

        return Pair.of(psst, tracerOpts);
    }

    public static FrontendInterface frontendFactory(
            TracerOpts topt, MainDispatcher mainDispatcher) {
        var frontendImplOptVal = topt.getFrontendImplOptVal();
        if (frontendImplOptVal == null) {
            frontendImplOptVal = "SIMPLE";
        }
        FrontendInterface fe;
        switch (frontendImplOptVal) {
            case "NOP" -> fe = new NOPFrontend(topt, mainDispatcher);
            case "SIMPLE" -> fe = new SimpleFrontend(topt, mainDispatcher);
            case "LOG" -> fe = new LogFrontend(topt, mainDispatcher);
            default -> fe = new NOPFrontend(topt, mainDispatcher);
        }
        return fe;
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
        FrontendInterface frontend = frontendFactory(topt, mainDispatcher);
        var frontendThread = new Thread(frontend);
        var mainDispatcherThread = new Thread(mainDispatcher, mainDispatcher.toString());
        mainDispatcherThread.start();
        frontendThread.start();
        try {
            mainDispatcherThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        frontend.setShouldStop();
        try {
            frontendThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
