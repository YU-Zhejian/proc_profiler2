package org.yuzjlab.proctracer.opts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;
import org.yuzjlab.proctracer.utils.ConfigurationManager;

@SuppressWarnings("unused")
public class TracerOpts {
    public static final String DEVNULL = "/dev/null";
    protected String frontendImplOptVal;

    public static final Map<String, Object> DEFAULT_CONFIG =
            Map.of(
                    "compressFmt",
                    "PLAIN",
                    "frontendRefreshFreq",
                    0.5,
                    "backendRefreshFreq",
                    0.01,
                    "frontendImpl",
                    "SIMPLE");

    protected static final CSVFormat yReacerCSVFormat =
            CSVFormat.Builder.create()
                    .setDelimiter('\t')
                    .setQuote('\'')
                    .setRecordSeparator('\n')
                    .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
                    .build();
    protected CompressFmt compressFmt;
    protected long tracePID;

    public long getTracePID() {
        return tracePID;
    }

    public File getOutDirPath() {
        return outDirPath;
    }

    protected File outDirPath;
    protected Configuration config;

    public Configuration getConfig() {
        return this.config;
    }

    public TracerOpts(Configuration config) throws ConfigurationException {
        var className = TracerOpts.class.getCanonicalName();
        this.config = config;
        this.setCompressFmt(config.getString("%s.compressFmt".formatted(className), null));
    }

    public static TracerOpts load(File configPath) throws ConfigurationException, IOException {
        var config = new PropertiesConfiguration();
        config.read(new FileReader(configPath));
        return new TracerOpts(config);
    }

    public static Configuration getDefaultConfig() {
        var mc = new MapConfiguration(new HashMap<>());

        mc.append(ConfigurationManager.getDefaultConfig(TracerOpts.class));
        for (var dispatcherClassName : ConfigurationManager.ALL_CONFIGURABLE) {
            try {
                mc.append(
                        ConfigurationManager.getDefaultConfig(Class.forName(dispatcherClassName)));
            } catch (ClassNotFoundException ignored) {
            }
        }
        return mc;
    }

    public void validate() throws IOException {
        this.outDirPath.mkdirs();
        if (this.outDirPath.exists() && !this.outDirPath.isDirectory()) {
            throw new IOException("Failed to mkdir -p '%s'".formatted(this.outDirPath.toString()));
        }
        if (this.compressFmt == null) {
            throw new NullPointerException();
        }
    }

    public void setTracePID(long tracePID) {
        this.tracePID = tracePID;
    }

    public void setOutDirPath(File outDirPath) {
        this.outDirPath = outDirPath;
    }

    public void setCompressFmt(String compressOptVal) throws ConfigurationException {
        if (compressOptVal != null) {
            switch (compressOptVal) {
                case "GZ" -> this.compressFmt = CompressFmt.GZ;
                case "XZ" -> this.compressFmt = CompressFmt.XZ;
                case "PLAIN" -> this.compressFmt = CompressFmt.PLAIN;
                default -> throw new ConfigurationException(
                        "compressFmt should be one of [GZ, XZ] or unspecified.");
            }
        } else {
            this.compressFmt = CompressFmt.PLAIN;
        }
    }

    public CSVPrinter createCSVPrinter(File name) throws IOException {
        OutputStream ios;
        FileOutputStream fStream;
        if (this.compressFmt == CompressFmt.XZ) {
            fStream = new FileOutputStream(name + ".tsv.xz");
            ios = new XZOutputStream(fStream, new LZMA2Options(9));
        } else if (this.compressFmt == CompressFmt.GZ) {
            fStream = new FileOutputStream(name + ".tsv.gz");
            ios = new GZIPOutputStream(fStream);
        } else {
            fStream = new FileOutputStream(name + ".tsv");
            ios = new BufferedOutputStream(fStream);
        }
        var appender = new OutputStreamWriter(ios);
        return new CSVPrinter(appender, yReacerCSVFormat);
    }

    public void setFrontEnd(String frontendImplOptVal) throws ConfigurationException {
        var validFrontendImpl = Set.of("NOP", "SIMPLE", "LOG");
        if (frontendImplOptVal == null || validFrontendImpl.contains(frontendImplOptVal)) {
            this.frontendImplOptVal = frontendImplOptVal;
            return;
        }
        throw new ConfigurationException(
                "frontendImpl should be one of [NOP, SIMPLE, LOG] or unspecified.");
    }

    public String getFrontendImplOptVal() {
        return this.frontendImplOptVal;
    }
}
