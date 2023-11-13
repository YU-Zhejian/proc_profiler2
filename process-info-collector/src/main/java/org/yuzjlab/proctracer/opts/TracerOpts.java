package org.yuzjlab.proctracer.opts;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class TracerOpts {
    protected static final CSVFormat yReacerCSVFormat = CSVFormat
            .Builder
            .create()
            .setDelimiter('\t')
            .setQuote('\'')
            .setRecordSeparator('\n')
            .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
            .build();
    protected TracerOutFmt tracerOutFmt;
    protected long tracePID;
    protected File outDirPath;


    public TracerOpts(long tracePID, File outDirPath, TracerOutFmt tracerOutFmt) throws IOException {
        this.tracePID = tracePID;
        this.outDirPath = outDirPath;
        this.tracerOutFmt = tracerOutFmt;
        this.outDirPath.mkdirs();
        if (this.outDirPath.exists() && !this.outDirPath.isDirectory()) {
            throw new IOException("Failed to mkdir -p '%s'".formatted(this.outDirPath.toString()));
        }
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
