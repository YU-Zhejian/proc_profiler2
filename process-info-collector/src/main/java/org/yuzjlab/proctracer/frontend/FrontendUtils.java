package org.yuzjlab.proctracer.frontend;

import java.text.DecimalFormat;

public class FrontendUtils {

    private static final long KB = 1024;
    private static final long MB = KB * KB;
    private static final long GB = KB * MB;
    private static final long TB = KB * GB;
    private static final DecimalFormat DF = new DecimalFormat("###.##");

    public static String toHumanReadable(long n) {
        return toHumanReadable(n, "Bytes");
    }

    public static String toHumanReadable(long n, String suffix) {
        if (n > TB) {
            return DF.format(n * 1.0 / TB) + "T" + suffix;
        } else if (n > GB) {
            return DF.format(n * 1.0 / GB) + "G" + suffix;
        } else if (n > MB) {
            return DF.format(n * 1.0 / MB) + "M" + suffix;
        } else if (n > KB) {
            return DF.format(n * 1.0 / KB) + "K" + suffix;
        } else {
            return DF.format(n * 1.0) + suffix;
        }
    }
}
