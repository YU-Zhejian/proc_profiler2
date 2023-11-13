package org.yuzjlab.proctracer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.opts.TracerOutFmt;
import org.yuzjlab.proctracer.psst.BaseProcessSupervisorThread;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadFactory;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;

class FEOpts {
    public final Options options;
    public static final String DEVNULL = "/dev/null";

    FEOpts() {
        var streamDesc = "File path of the standard %s stream to be redirected to the target process. " +
                "Only valid if [CMDS] are present. Default to `/dev/null`.";
        this.options = new Options();
        this.options
                .addOption(
                        Option
                                .builder("h")
                                .longOpt("help")
                                .desc("Display this help")
                                .required(false)
                                .hasArg(false)
                                .build()
                )
                .addOption(
                        Option
                                .builder("p")
                                .longOpt("pid")
                                .desc("Process ID to trace. If this option is set, the CMDS are ignored.")
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("compress")
                                .desc("Whether to compress the output streams, default to false. Valid choices: [GZ, XZ]")
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("stdin")
                                .desc(streamDesc.formatted("input"))
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("stdout")
                                .desc(streamDesc.formatted("output"))
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("stderr")
                                .desc(streamDesc.formatted("error"))
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("wd")
                                .desc("The working directory for the new process. " +
                                        "Only valid if [CMDS] are present. Default to current working directory.")
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder("o")
                                .longOpt("outdir")
                                .desc("Output directory of the tracer.")
                                .required(true)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("frontend-refresh-freq")
                                .desc("Frequency of refreshing the frontend, in seconds")
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("backend-refresh-freq")
                                .desc("Default frequency of refreshing all the backends, in seconds")
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("supress-frontend")
                                .desc("Supress the frontend. If set, --frontend-refresh-freq will be defunct.")
                                .required(false)
                                .hasArg(false)
                                .build()
                )
        ;
    }

    public void printHelp() {
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
                this.options,
                "END OF HELP MESSAGE"
        );
    }
}

public class Main {
    public static void main(String[] args) {
        var lh = LoggerFactory.getLogger(Main.class.getCanonicalName());
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

        var parser = new DefaultParser();
        var feopts = new FEOpts();
        try {
            var cmd = parser.parse(feopts.options, argsBeforeCmdLineArr);
            if (cmd.hasOption("h")) {
                feopts.printHelp();
                return;
            }
            ProcessSupervisorThreadInterface psst;
            if (!cmd.hasOption("p") && cmdLine.isEmpty()) {
                throw new ParseException("At least one of the -p [PID] or [CMDS] needs to be specified!");
            }
            else if (cmd.hasOption("p") &&! cmdLine.isEmpty()) {
                throw new ParseException("Only one of the -p [PID] or [CMDS] needs to be specified!");
            }else if(cmd.hasOption("p")){
                var pidStr = cmd.getOptionValue("p");
                var pid = -1L;
                try{
                    pid = Long.parseLong(pidStr);
                } catch (NumberFormatException numberFormatException){
                    throw new ParseException("Specified pid '%s' cannot be parsed!".formatted(pidStr));
                }
                psst = ProcessSupervisorThreadFactory.create(pid);
            } else{
                var stderr = cmd.getOptionValue("stderr");
                if (stderr == null){
                    stderr = FEOpts.DEVNULL;
                }
                var stdout = cmd.getOptionValue("stdout");
                if (stdout == null){
                    stdout = FEOpts.DEVNULL;
                }
                var stdin = cmd.getOptionValue("stdin");
                if (stdin == null){
                    stdin = FEOpts.DEVNULL;
                }
                var wd = cmd.getOptionValue("wd");
                if (wd == null){
                    wd = new File ( "." ).getAbsolutePath();
                }
                psst = ProcessSupervisorThreadFactory.create(
                        cmdLineArr,
                        new File(stdin),
                        new File(stdout),
                        new File(stderr),
                        new File(wd)
                );
            }
            TracerOutFmt topt;
            var compressOptVal = cmd.getOptionValue("compress");
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
            TracerOpts tracerOpts;
            psst.start();
            try{
                tracerOpts = new TracerOpts(psst.getPid(), new File(cmd.getOptionValue("outdir")), topt);
            }
            catch (IOException ioException){
                lh.error("Failed to validate TracerOptions  Reason: {}", ioException.getMessage());
            }

        } catch (ParseException parseException) {
            lh.error("Parsing failed.  Reason: {}", parseException.getMessage());
            feopts.printHelp();
            System.exit(1);
        }
    }
}
