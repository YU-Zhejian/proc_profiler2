package org.yuzjlab.proctracer.opts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CmdlineOpts {
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
    public static final Option frontendImplOption =
            Option.builder()
                    .longOpt("frontend-impl")
                    .desc("The implementation of frontend. Valid choices: [NOP, SIMPLE, LOG]")
                    .required(false)
                    .hasArg(true)
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
                    .addOption(frontendImplOption)
                    .addOption(configOption)
                    .addOption(testMainOption);

    CmdlineOpts() {}

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
