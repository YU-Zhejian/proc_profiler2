package org.yuzjlab.proctracer.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.FileUtils;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;
import org.yuzjlab.proctracer.opts.CompressFmt;

// Currently NOP.
public class RollingFileAppender extends Writer {
    protected boolean closed;
    protected Writer currentFileWriter;
    protected long fileID;
    protected final CompressFmt compressFmt;
    protected final String fileName;

    protected boolean needsShifting() {
        return false;
    }

    protected void createFileWriter() throws IOException {
        OutputStream ios;
        FileOutputStream fStream;
        if (this.compressFmt == CompressFmt.XZ) {
            fStream = new FileOutputStream(this.fileName + ".xz");
            ios = new XZOutputStream(fStream, new LZMA2Options(9));
        } else if (compressFmt == CompressFmt.GZ) {
            fStream = new FileOutputStream(this.fileName + ".gz");
            ios = new GZIPOutputStream(fStream);
        } else {
            fStream = new FileOutputStream(this.fileName);
            ios = new BufferedOutputStream(fStream);
        }
        this.currentFileWriter = new OutputStreamWriter(ios);
    }

    protected void closeFileWriter() throws IOException {
        if (this.currentFileWriter != null) {
            this.currentFileWriter.flush();
            this.currentFileWriter.close();
            FileUtils.moveFile(
                    new File(this.fileName), new File(this.fileName + "." + this.fileID));
            this.fileID += 1;
        }
        this.currentFileWriter = null;
    }

    protected void checkClosed() throws IOException {
        if (this.closed) {
            throw new IOException("Cannot write to a closed writer.");
        }
    }

    public RollingFileAppender(String fileName, CompressFmt compressFmt) throws IOException {
        this.closed = false;
        this.fileID = 0;
        this.compressFmt = compressFmt;
        this.fileName = fileName;
        this.createFileWriter();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        this.checkClosed();
        if (this.needsShifting()) {
            this.closeFileWriter();
            this.createFileWriter();
        }
        this.currentFileWriter.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.checkClosed();
        this.currentFileWriter.flush();
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.flush();
        }
        this.closeFileWriter();
        this.closed = true;
    }
}
