package org.yuzjlab.proctracer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.LoggerFactory;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadFactory;

class MainTest {

    protected static File which(String executableName) throws FileNotFoundException {
        var path = System.getenv("PATH");
        if (path == null) {
            throw new FileNotFoundException("File %s not found in PATH!".formatted(executableName));
        }
        for (var possiblePath : path.split(File.pathSeparator)) {
            var possibleFile = Path.of(possiblePath, executableName).toFile();
            if (possibleFile.exists()) {
                return possibleFile;
            }
        }
        throw new FileNotFoundException("File %s not found in PATH!".formatted(executableName));
    }

    protected static void mainTest(String interpreterPath, String scriptPath)
            throws ConfigurationException {
        TracerOpts topt = new TracerOpts(TracerOpts.getDefaultConfig());
        var psst =
                ProcessSupervisorThreadFactory.create(
                        new String[] {interpreterPath, scriptPath},
                        new File(TracerOpts.DEVNULL),
                        new File(TracerOpts.DEVNULL),
                        new File(TracerOpts.DEVNULL),
                        new File("."),
                        System.getenv());
        topt.setCompressFmt("PLAIN");
        topt.setFrontEndImpl("NOP");
        topt.setOutDirPath(new File(scriptPath + ".out.d"));
        Main.performTrace(topt, psst);
    }

    public static void main(String[] args) throws IOException, ConfigurationException {
        var lh = LoggerFactory.getLogger(MainTest.class);
        var pyInterpreter = which("python3");
        var bashInterpreter = which("bash");
        lh.info("Received python interpreter {}", pyInterpreter);
        if (args.length == 0) {
            lh.error("Need at least one test file!");
            System.exit(1);
        }
        for (var argVal : args) {
            var fn = new File(argVal);
            if (!fn.isFile()) {
                continue;
            }
            lh.info("Testing {}...", fn);
            if (fn.toString().endsWith(".py")) {
                MainTest.mainTest(pyInterpreter.toString(), fn.toString());
            } else if (fn.toString().endsWith(".sh")) {
                MainTest.mainTest(bashInterpreter.toString(), fn.toString());
            }
        }
    }
}
