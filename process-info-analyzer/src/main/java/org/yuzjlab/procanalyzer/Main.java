package org.yuzjlab.procanalyzer;

import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.slf4j.LoggerFactory;

public class Main {
    public static DataType getDT(String schemaName) {
        DataType dt;
        switch (schemaName) {
            case "Time:MilliSecSinceEpoch", "Long" -> dt = DataTypes.LongType;
            default -> throw new RuntimeException("Type mismatch!");
        }
        return dt;
    }

    public static void main(String[] args) throws IOException, ConfigurationException {
        var lh = LoggerFactory.getLogger("YUZJLab.ProcAnalyzer");
        var spark =
                SparkSession.builder()
                        .appName("YuZJLab Proc Analyzer")
                        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                        .getOrCreate();
        lh.info("Using spark ver. {}", spark.version());

        var conf = new PropertiesConfiguration();
        conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        conf.read(new FileReader("test.out.d/test_sleep/csvPolicies.properties"));
        var confSchema = conf.getList(String.class, "SystemMemoryTracer.schema");
        var confHeader = conf.getList(String.class, "SystemMemoryTracer.header");
        var sparkSchema = new StructType();
        for (int i = 0; i < confHeader.size(); i++) {
            sparkSchema = sparkSchema.add(confHeader.get(i), getDT(confSchema.get(i)));
        }
        var sdf =
                spark.read()
                        .format("csv")
                        .schema(sparkSchema)
                        .option("delimiter", "\t")
                        .option("header", true)
                        .option("encoding", "UTF-8")
                        .option("inferSchema", true)
                        .option("enforceSchema", true)
                        .option("mode", "DROPMALFORMED")
                        .option("locale", "en-US")
                        .load("test.out.d/test_sleep/SystemMemoryTracer-0.tsv");
        sdf.show();
        spark.stop();
    }
}
