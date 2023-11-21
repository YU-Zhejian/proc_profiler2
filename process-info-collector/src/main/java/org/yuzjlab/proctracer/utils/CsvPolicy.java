package org.yuzjlab.proctracer.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;
import org.yuzjlab.proctracer.dispatcher.DispatcherInterface;
import org.yuzjlab.proctracer.opts.CompressFmt;

public class CsvPolicy {
    public final String name;
    public final String[] header;
    public final String[] schema;
    public final Boolean[] isAccumulative;
    protected CSVPrinter csvPrinter;
    protected Writer csvWriter;

    public CsvPolicy(Class<?> cls, String[] header, String[] schema, Boolean[] isAccumulative) {
        this.name = cls.getSimpleName();
        this.header = header;
        this.schema = schema;
        this.isAccumulative = isAccumulative;
    }

    protected static final CSVFormat yReacerCSVFormat =
            CSVFormat.Builder.create()
                    .setDelimiter('\t')
                    .setQuote('\'')
                    .setRecordSeparator('\n')
                    .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
                    .build();

    public CSVPrinter createCSVPrinter(String fileName, CompressFmt compressFmt) throws IOException {
        OutputStream ios;
        FileOutputStream fStream;
        fileName = fileName + ".tsv";
        if (compressFmt == CompressFmt.XZ) {
            fStream = new FileOutputStream(fileName + ".xz");
            ios = new XZOutputStream(fStream, new LZMA2Options(9));
        } else if (compressFmt == CompressFmt.GZ) {
            fStream = new FileOutputStream(fileName + ".gz");
            ios = new GZIPOutputStream(fStream);
        } else {
            fStream = new FileOutputStream(fileName);
            ios = new BufferedOutputStream(fStream);
        }
        this.csvWriter = new OutputStreamWriter(ios);
        return new CSVPrinter(this.csvWriter, yReacerCSVFormat);
    }

    public void setUp(DispatcherInterface dispatcher) throws IOException {
        var topt = dispatcher.getTracerOpts();
        this.csvPrinter =
                this.createCSVPrinter(
                        Path.of(topt.getOutDirPath().toString(), dispatcher.toString()).toString(),
                        topt.getCompressFmt());
        this.csvPrinter.printRecord((Object[]) this.header);
    }

    public void tearDown() throws IOException {
        this.csvPrinter.close();
        this.csvWriter.close();
    }

    @Override
    public String toString() {
        return "CsvPolicy<%s>(%s)".formatted(this.name, Arrays.toString(this.header));
    }

    public void printRecord(Object... values) throws IOException {
        this.csvPrinter.printRecord(values);
    }
}
