package org.yuzjlab.proctracer;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.opts.TracerOutFmt;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadFactory;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;

class FEOpts {
  public static final String DEVNULL = "/dev/null";
  public static final Option helpOption =
      Option.builder("h")
          .longOpt("help")
          .desc("Display this help")
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
  public static final Option compressOption =
      Option.builder()
          .longOpt("compress")
          .desc("Whether to compress the output streams, default to false. Valid choices: [GZ, XZ]")
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
          .required(true)
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
  public static final Option supressFrontendOption =
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
  protected static final String streamDesc =
      "File path of the standard %s stream to be redirected to the target process. "
          + "Only valid if [CMDS] are present. Default to `/dev/null`.";
  public static final Option stdinOption =
      Option.builder()
          .longOpt("stdin")
          .desc(streamDesc.formatted("input"))
          .required(false)
          .hasArg(true)
          .build();
  public static final Option stdoutOption =
      Option.builder()
          .longOpt("stdout")
          .desc(streamDesc.formatted("output"))
          .required(false)
          .hasArg(true)
          .build();
  public static final Option stderrOption =
      Option.builder()
          .longOpt("stderr")
          .desc(streamDesc.formatted("error"))
          .required(false)
          .hasArg(true)
          .build();
  public static final Options options =
      new Options()
          .addOption(helpOption)
          .addOption(pidOption)
          .addOption(compressOption)
          .addOption(stdinOption)
          .addOption(stdoutOption)
          .addOption(stderrOption)
          .addOption(wdOption)
          .addOption(outdirOption)
          .addOption(frontendRefreshFreqOption)
          .addOption(backendRefreshFreqOption)
          .addOption(supressFrontendOption)
          .addOption(configOption);

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
}

public class Main {

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

  public static Pair<ProcessSupervisorThreadInterface, TracerOpts> parseArgs(
      String[] argsBeforeCmdLineArr, String[] cmdLineArr) throws ParseException {
    var lh = LoggerFactory.getLogger(Main.class.getCanonicalName());
    var parser = new DefaultParser();
    var cmd = parser.parse(FEOpts.options, argsBeforeCmdLineArr);
    ProcessSupervisorThreadInterface psst;
    TracerOutFmt topt;
    TracerOpts tracerOpts = null;

    if (cmd.hasOption(FEOpts.helpOption)) {
      FEOpts.printHelp();
      System.exit(0);
    }

    if (cmd.hasOption(FEOpts.configOption)) {
      try {
        tracerOpts = TracerOpts.load(new File(cmd.getOptionValue(FEOpts.configOption)));
      } catch (ConfigurationException e) {
        lh.error("Failed to parse provided config. Error: {}", e.getMessage());
        System.exit(1);
      }
    } else {
      tracerOpts = TracerOpts.defaults();
    }

    if (!cmd.hasOption(FEOpts.pidOption) && cmdLineArr.length == 0) {
      throw new ParseException("At least one of the -p [PID] or [CMDS] needs to be specified!");
    } else if (cmd.hasOption(FEOpts.pidOption) && cmdLineArr.length != 0) {
      throw new ParseException("Only one of the -p [PID] or [CMDS] needs to be specified!");
    } else if (cmd.hasOption(FEOpts.pidOption)) {
      var pidStr = cmd.getOptionValue(FEOpts.pidOption);
      var pid = -1L;
      try {
        pid = Long.parseLong(pidStr);
      } catch (NumberFormatException numberFormatException) {
        throw new ParseException("Specified pid '%s' cannot be parsed!".formatted(pidStr));
      }
      psst = ProcessSupervisorThreadFactory.create(pid);
    } else {
      var stderr = cmd.getOptionValue(FEOpts.stderrOption);
      if (stderr == null) {
        stderr = FEOpts.DEVNULL;
      }
      var stdout = cmd.getOptionValue(FEOpts.stdoutOption);
      if (stdout == null) {
        stdout = FEOpts.DEVNULL;
      }
      var stdin = cmd.getOptionValue(FEOpts.stdinOption);
      if (stdin == null) {
        stdin = FEOpts.DEVNULL;
      }
      var wd = cmd.getOptionValue(FEOpts.wdOption);
      if (wd == null) {
        wd = new File(".").getAbsolutePath();
      }
      psst =
          ProcessSupervisorThreadFactory.create(
              cmdLineArr, new File(stdin), new File(stdout), new File(stderr), new File(wd));
    }

    var compressOptVal = cmd.getOptionValue(FEOpts.compressOption);
    if (compressOptVal != null) {
      if (compressOptVal.equals("GZ")) {
        topt = TracerOutFmt.GZ;
      } else if (compressOptVal.equals("XZ")) {
        topt = TracerOutFmt.XZ;
      } else {
        throw new ParseException("--compress should be one of [GZ, XZ] or unspecified,");
      }
    } else {
      topt = TracerOutFmt.PLAIN;
    }

    tracerOpts.setOutDirPath(new File(cmd.getOptionValue(FEOpts.outdirOption)));
    return Pair.of(psst, tracerOpts);
  }

  public static void main(String[] args) {
    var lh = LoggerFactory.getLogger(Main.class.getCanonicalName());
    var argPair = splitArgsBeforeAfterCmd(args);
    try {
      var psstToptPair = parseArgs(argPair.getLeft(), argPair.getRight());
    } catch (ParseException parseException) {
      lh.error("Parsing failed.  Reason: {}", parseException.getMessage());
      FEOpts.printHelp();
      System.exit(1);
    }
    //
    //        psst.start();
    //        tracerOpts.setTracePID(psst.getPid());
  }
}
