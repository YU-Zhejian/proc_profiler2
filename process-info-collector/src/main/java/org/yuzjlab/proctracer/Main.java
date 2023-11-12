package org.yuzjlab.proctracer;

import org.apache.commons.cli.*;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.opts.TracerOutFmt;

import java.io.File;
import java.util.ArrayList;


class FEOpts {
    public final Options options;

    FEOpts() {
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
                                .desc("File path of the standard input to be redirected to the target process. " +
                                        "Only valid if [CMDS] are present. Default to `/dev/null`.")
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("stdout")
                                .desc("File path of the standard output to be redirected to the target process. " +
                                        "Only valid if [CMDS] are present. Default to `/dev/null`.")
                                .required(false)
                                .hasArg(true)
                                .build()
                )
                .addOption(
                        Option
                                .builder()
                                .longOpt("stderr")
                                .desc("File path of the standard error to be redirected to the target process. " +
                                        "Only valid if [CMDS] are present. Default to `/dev/null`.")
                                .required(false)
                                .hasArg(true)
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
    public static void main(String[] args) throws ProcessBaseException {
        System.err.println("Running libprocfs-java testing code ...");

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
            if (!cmd.hasOption("p") && cmdLine.isEmpty()) {
                throw new ParseException("At least one of the -p [PID] or [CMDS] needs to be specified!");
            }

            TracerOutFmt topt;
            if (cmd.hasOption("compress")) {
                if (cmd.getOptionValue("compress").equals("GZ")) {
                    topt = TracerOutFmt.GZ;
                } else if (cmd.getOptionValue("compress").equals("XZ")) {
                    topt = TracerOutFmt.XZ;
                } else {
                    throw new ParseException("--compress should be one of [GZ, XZ] or unspecified,");
                }
            } else {
                topt = TracerOutFmt.PLAIN;
            }

            var tracerOpts = new TracerOpts(0 /* TODO */, new File("") /*todo*/, topt);
        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            feopts.printHelp();
            System.exit(1);
        }
    }
}
