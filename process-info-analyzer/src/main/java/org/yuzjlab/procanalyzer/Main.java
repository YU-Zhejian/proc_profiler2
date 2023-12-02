package org.yuzjlab.procanalyzer;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.slf4j.LoggerFactory;

public class Main {
    public static DataType getDT(String schemaName) {
        DataType dt;
        switch (schemaName) {
            case "Time:MilliSecSinceEpoch" -> dt = DataTypes.LongType;
            case "Long" -> dt = DataTypes.LongType;
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
        sdf.printSchema();
        var startOffset = 1700489717705L;
        var interval = 1000L; // Interval 1s
        sdf =
                sdf.withColumn("TIME_WITHOUT_OFFSET", functions.col("TIME").minus(startOffset))
                        .withColumn(
                                "TIME_GRP_ID",
                                functions
                                        .col("TIME_WITHOUT_OFFSET")
                                        .divide(interval)
                                        .cast(DataTypes.LongType));
        var expr = new ArrayList<Column>();
        for (var name : confHeader) {
            if (name.equals("TIME")) {
                continue;
            }
            expr.add(functions.mean(name).as(name));
        }
        var exprs = expr.toArray(new Column[0]);
        sdf =
                sdf.groupBy("TIME_GRP_ID")
                        .agg(
                                functions
                                        .col("TIME_GRP_ID")
                                        .multiply(interval)
                                        .plus(startOffset)
                                        .as("TIME"),
                                exprs);
        sdf = sdf.drop("TIME_WITHOUT_OFFSET", "TIME_GRP_ID").sort("TIME");
        sdf.show();
        spark.stop();
    }
}
