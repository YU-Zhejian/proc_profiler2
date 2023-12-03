package org.yuzjlab.procanalyzer;

import java.io.IOException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.spark.sql.SparkSession;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) throws IOException, ConfigurationException {
        var lh = LoggerFactory.getLogger("YUZJLab.ProcAnalyzer");
        var spark =
                SparkSession.builder()
                        .appName("YuZJLab Proc Analyzer")
                        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                        .getOrCreate();
        lh.info("Using spark ver. {}", spark.version());
        var sparkUtils = new SparkDownSampleUtils("test.out.d/test_sleep/csvPolicies.properties");
        sparkUtils.performDownSampling(
                "test.out.d/test_sleep/SystemMemoryTracer-0.tsv.gz",
                null,
                spark,
                1700489717705L,
                1000L);
        spark.stop();
    }
}
