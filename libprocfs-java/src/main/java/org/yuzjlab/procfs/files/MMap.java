package org.yuzjlab.procfs.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.TreeSet;

import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

public class MMap {
    private MMap(){}
    public static Iterable<String> parseMMap(Path pathOfMMap)throws ProcessBaseException{
        var fns = new TreeSet<String>();
        try (var fs = Files.newDirectoryStream(pathOfMMap)) {
            for (var mmapItemPath : fs) {
                var rpath = ProcessUtils.resolveRealPath(mmapItemPath);
                if (!rpath.equals(mmapItemPath.toString())) {
                  fns.add(rpath);
                }
            }
            return fns;
        } catch (IOException e) {
            throw ProcessUtils.resolveIOException(e);
        }
    }
}
