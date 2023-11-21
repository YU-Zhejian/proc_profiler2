package org.yuzjlab.proctracer.opts;

import java.util.Objects;

public enum CompressFmt {
    /**
     * Compress the output streams using Lempel-Ziv Markov Chain version 2 (LZMA2) algorithm. Have
     * the smallest size the but slowest speed.
     */
    XZ("XZ"),
    /**
     * Compress the output streams using Lempel-Ziv 77 (LZ77) algorithm. Have moderate size and
     * speed.
     */
    GZ("GZ"),
    /** No compression. Fast but cannot reduce output size. */
    PLAIN("PLAIN");

    private final String name;

    CompressFmt(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static CompressFmt fromString(String name) throws ClassNotFoundException {
        if (name == null) {
            return PLAIN;
        }
        for (var v : values()) {
            if (Objects.equals(v.name, name)) {
                return v;
            }
        }
        throw new ClassNotFoundException("compressFmt should be one of [GZ, XZ] or unspecified.");
    }
}
