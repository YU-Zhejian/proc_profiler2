package org.yuzjlab.proctracer.opts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

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

  public ImmutableConfiguration getConfig(){
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

  public static Configuration getDefaultConfig() throws ConfigurationException {
    var className = TracerOpts.class.getCanonicalName();
    var defConfig = new BasicConfigurationBuilder<>(PropertiesConfiguration.class).getConfiguration();
    defConfig.setProperty("%s.compressFmt".formatted(className), null);
    defConfig.setProperty("%s.frontendRefreshFreq".formatted(className), 0.5);
    defConfig.setProperty("%s.backendRefreshFreq".formatted(className), 0.01);
    defConfig.setProperty("%s.suppressFrontend".formatted(className), false);
    return defConfig;
  }

  public static TracerOpts defaults() throws ConfigurationException {
    return new TracerOpts(TracerOpts.getDefaultConfig());
  }

  public void validate() throws IOException {
    this.outDirPath.mkdirs();
    if (this.outDirPath.exists() && !this.outDirPath.isDirectory()) {
      throw new IOException("Failed to mkdir -p '%s'".formatted(this.outDirPath.toString()));
    }
    if (this.compressFmt == null){
      throw new NullPointerException();
    }
  }

  public void setTracePID(long tracePID) {
    this.tracePID = tracePID;
  }

  public void setOutDirPath(File outDirPath) {
    this.outDirPath = outDirPath;
  }


  public void setCompressFmt(String compressOptVal) throws ConfigurationException{
    if (compressOptVal != null) {
      if (compressOptVal.equals("GZ")) {
        this.compressFmt = CompressFmt.GZ;
      } else if (compressOptVal.equals("XZ")) {
        this.compressFmt = CompressFmt.XZ;
      } else {
        throw new ConfigurationException("compressFmt should be one of [GZ, XZ] or unspecified.");
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
