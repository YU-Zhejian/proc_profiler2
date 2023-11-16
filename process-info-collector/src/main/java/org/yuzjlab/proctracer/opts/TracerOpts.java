package org.yuzjlab.proctracer.opts;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class TracerOpts {
  protected static final CSVFormat yReacerCSVFormat =
      CSVFormat.Builder.create()
          .setDelimiter('\t')
          .setQuote('\'')
          .setRecordSeparator('\n')
          .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
          .build();
  protected TracerOutFmt tracerOutFmt;
  protected long tracePID;
  protected File outDirPath;
  protected Configuration config;

  public TracerOpts(Configuration config) {
    this.config = config;
  }

  public static TracerOpts load(File configPath) throws ConfigurationException {
    var params = new Parameters();
    var builder =
        new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure(params.fileBased().setFile(configPath));
    return new TracerOpts(builder.getConfiguration());
  }

  public static TracerOpts defaults() {
    return null; // TODO
  }

  public void validate() throws IOException {
    this.outDirPath.mkdirs();
    if (this.outDirPath.exists() && !this.outDirPath.isDirectory()) {
      throw new IOException("Failed to mkdir -p '%s'".formatted(this.outDirPath.toString()));
    }
  }

  public void setTracePID(long tracePID) {
    this.tracePID = tracePID;
  }

  public void setOutDirPath(File outDirPath) {
    this.outDirPath = outDirPath;
  }

  public CSVPrinter createCSVPrinter(File name) throws IOException {
    OutputStream ios;
    var fStream = new FileOutputStream(name);
    if (this.tracerOutFmt == TracerOutFmt.XZ) {
      ios = new XZOutputStream(fStream, new LZMA2Options(9));
    } else if (this.tracerOutFmt == TracerOutFmt.GZ) {
      ios = new GZIPOutputStream(fStream);
    } else {
      ios = new BufferedOutputStream(fStream);
    }
    var appender = new OutputStreamWriter(ios);
    return new CSVPrinter(appender, yReacerCSVFormat);
  }
}
