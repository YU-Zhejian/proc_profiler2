package org.yuzjlab.proctracer.opts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;

public class TracerOpts {
    public static final String DEVNULL = "/dev/null";
    protected static final CSVFormat yReacerCSVFormat =
            CSVFormat.Builder.create()
                    .setDelimiter('\t')
                    .setQuote('\'')
                    .setRecordSeparator('\n')
                    .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
                    .build();
    protected CompressFmt compressFmt;
    protected long tracePID;
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
        var className = TracerOpts.class.getCanonicalName();

        var mc = new MapConfiguration(
                Stream.of(
                                new AbstractMap.SimpleEntry<>("%s.compressFmt".formatted(className), "PLAIN"),
                                new AbstractMap.SimpleEntry<>("%s.frontendRefreshFreq".formatted(className), 0.5),
                                new AbstractMap.SimpleEntry<>("%s.backendRefreshFreq".formatted(className), 0.01),
                                new AbstractMap.SimpleEntry<>("%s.suppressFrontend".formatted(className), false)
                        )
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                );
        for(var dispatcherClassName : BaseDispatcher.ALL_KNOWN_DISPATCHER_NAMES){
            try{
                Class<?> di = Class.forName(dispatcherClassName);
                var gdc = di.getMethod("getDefaultConfig");
                var result = (Configuration) gdc.invoke(null);
                mc.append(result);
            } catch (Exception ignored){
                throw new RuntimeException(ignored);
            }

        }
        return mc;
    }

    public void validate() {
        this.outDirPath.mkdirs();
        if (this.outDirPath.exists() && !this.outDirPath.isDirectory()) {
            throw new RuntimeException(
                    "Failed to mkdir -p '%s'".formatted(this.outDirPath.toString()));
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
        var fStream = new FileOutputStream(name);
        if (this.compressFmt == CompressFmt.XZ) {
            ios = new XZOutputStream(fStream, new LZMA2Options(9));
        } else if (this.compressFmt == CompressFmt.GZ) {
            ios = new GZIPOutputStream(fStream);
        } else {
            ios = new BufferedOutputStream(fStream);
        }
        var appender = new OutputStreamWriter(ios);
        return new CSVPrinter(appender, yReacerCSVFormat);
    }
}
