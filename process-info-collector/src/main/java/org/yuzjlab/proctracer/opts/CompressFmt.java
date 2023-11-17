package org.yuzjlab.proctracer.opts;

public enum CompressFmt {
    /**
     * Compress the output streams using Lempel-Ziv Markov Chain version 2 (LZMA2) algorithm. Have
     * the smallest size but slowest speed.
     */
    XZ,
    /**
     * Compress the output streams using Lempel-Ziv 77 (LZ77) algorithm. Have moderate size and
     * speed.
     */
    GZ,
    /** No compression. Fast but cannot reduce output size. */
    PLAIN;
}
