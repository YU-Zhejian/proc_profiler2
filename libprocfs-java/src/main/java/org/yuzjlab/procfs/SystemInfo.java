package org.yuzjlab.procfs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class SystemInfo {

    public Iterator<Integer> iterAllPids() throws IOException {
        return new AllPidIterator();
    }

    public int getNumberOfProcesses(){
        return 0;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("INIT");
        try (var it = new AllPidIterator()) {
            while (it.hasNext()) {
                int pid = it.next();
                System.out.println(pid);
            }
        }
        System.out.println("FIN");
    }

}

class AllPidIterator implements Iterator<Integer>, AutoCloseable {

    private final Iterator<Integer> iterator;
    private final DirectoryStream<Path> dstream;

    public AllPidIterator() throws IOException {
        var dir = Path.of("/proc/");
        this.dstream = Files.newDirectoryStream(dir);
        this.iterator = StreamSupport.stream(dstream.spliterator(), true)
                .filter((Path p) -> (p.toFile().isDirectory()))
                .map((Path p) -> {
                    int i = -1;
                    try {
                        i = Integer.parseInt(p.getFileName().toString());
                    } catch (NumberFormatException ignored) {}
                    return i;
                }).
                filter((Integer i) -> (i != -1))
                .iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Integer next() {
        return iterator.next();
    }

    @Override
    public void forEachRemaining(Consumer<? super Integer> action) {
        this.iterator.forEachRemaining(action);
    }

    @Override
    public void close() throws IOException {
        this.dstream.close();
    }
}
