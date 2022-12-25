package org.yuzjlab.procfs;

import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class SystemInfo {

    private SystemInfo() {
    }

    public static Iterable<Integer> iterAllPids() throws ProcessBaseException {
        try {
            return new AllPidIterator();
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
        }
    }

    public static long getNumberOfProcesses() throws ProcessBaseException {
        try (var aps = new AllPidStream()) {
            return aps.getStream().count();
        } catch (IOException e) {
            throw ProcfsInternalUtils.resolveIOException(e);
        }
    }

}

class AllPidStream implements AutoCloseable {
    private final DirectoryStream<Path> dstream;

    public AllPidStream() throws IOException {
        var dir = Path.of("/", "proc");
        this.dstream = Files.newDirectoryStream(dir);
    }

    public Stream<Integer> getStream() {
        return StreamSupport.stream(dstream.spliterator(), true)
                .filter((Path p) -> (p.toFile().isDirectory()))
                .map((Path p) -> {
                    int i = -1;
                    try {
                        i = Integer.parseInt(p.getFileName().toString());
                    } catch (NumberFormatException ignored) {
                        //
                    }
                    return i;
                })
                .filter((Integer i) -> (i != -1));
    }

    @Override
    public void close() throws IOException {
        this.dstream.close();
    }
}


class AllPidIterator implements Iterable<Integer>, AutoCloseable {

    private final Iterator<Integer> iterator;
    private final AllPidStream allPidStream;

    public AllPidIterator() throws IOException {
        this.allPidStream = new AllPidStream();
        this.iterator = this.allPidStream.getStream().iterator();
    }


    @Override
    public void close() throws IOException {
        this.allPidStream.close();
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.iterator;
    }
}
