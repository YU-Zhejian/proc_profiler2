package org.yuzjlab.proctracer.opts;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class TracerOpts {
    protected final static CSVFormat yReacerCSVFormat = CSVFormat
            .Builder
            .create()
            .setDelimiter('\t')
            .setQuote('\'')
            .setRecordSeparator('\n')
            .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
            .build();
    protected TracerOutFmt tracerOutFmt;
    protected int tracePID;
    protected File outDirPath;


    public TracerOpts(int tracePID, File outDirPath, TracerOutFmt tracerOutFmt) {
        this.tracePID = tracePID;
        this.outDirPath = outDirPath;
        this.tracerOutFmt = tracerOutFmt;
        this.outDirPath.mkdirs();
        if (this.outDirPath.exists() && !this.outDirPath.isDirectory()) {
            // throw new IOException("message");
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
