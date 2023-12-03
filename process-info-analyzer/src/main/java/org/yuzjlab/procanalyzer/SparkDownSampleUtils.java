package org.yuzjlab.procanalyzer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class SparkDownSampleUtils {
    public static final String TIME_WITHOUT_OFFSET = "TIME_WITHOUT_OFFSET";
    public static final String TIME_GRP_ID = "TIME_GRP_ID";
    public static final String TIME = "TIME";
    public static final Pattern FILE_NAME_PATTERN = Pattern.compile("(\\S+)-(\\d+)\\.tsv");
    protected PropertiesConfiguration conf;

    /**
     * Get the data type based on the given schema name.
     *
     * @param schemaName the name of the schema
     * @return the corresponding data type
     * @throws RuntimeException if there is a type mismatch
     */
    public static DataType getDT(String schemaName) {
        return switch (schemaName) {
            case "Time:MilliSecSinceEpoch", "Long" -> DataTypes.LongType;
            case "Int" -> DataTypes.IntegerType;
            case "Dbl" -> DataTypes.DoubleType;
            default -> throw new RuntimeException("Type mismatch!");
        };
    }

    public SparkDownSampleUtils(String csvPolicyPropertiesPath) throws ConfigurationException, IOException {
        this.conf = new PropertiesConfiguration();
        this.conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        this.conf.read(new FileReader(csvPolicyPropertiesPath));
    }

    public void performDownSampling(
            String inputPath,
            String outputPath,
            SparkSession spark,
            Long startOffset,
            long intervalMiliSec)
            throws IOException {

        var m = FILE_NAME_PATTERN.matcher(FileUtils.getFile(inputPath).getName());
        if (!m.find()) {

            throw new FileNotFoundException("File name does not match the pattern");
        }
        var dispatcherName = m.group(1);

        var confSchema = this.conf.getList(String.class, "%s.schema".formatted(dispatcherName));
        var confHeader = this.conf.getList(String.class, "%s.header".formatted(dispatcherName));
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
                        .load(inputPath);
        sdf.printSchema();
        sdf =
                sdf.withColumn(TIME_WITHOUT_OFFSET, functions.col(TIME).minus(startOffset))
                        .withColumn(
                                TIME_GRP_ID,
                                functions
                                        .col(TIME_WITHOUT_OFFSET)
                                        .divide(intervalMiliSec)
                                        .cast(DataTypes.LongType));
        // Array of Aggregation Transformations
        var expr = new ArrayList<Column>();
        for (var name : confHeader) {
            if (name.equals(TIME)) {
                continue;
            }
            expr.add(functions.mean(name).as(name));
        }
        var exprs = expr.toArray(new Column[0]);
        sdf =
                sdf.groupBy(TIME_GRP_ID)
                        .agg(
                                functions
                                        .col(TIME_GRP_ID)
                                        .multiply(intervalMiliSec)
                                        .plus(startOffset)
                                        .as(TIME),
                                exprs);
        sdf = sdf.drop(TIME_WITHOUT_OFFSET).sort(TIME);
        if (outputPath == null) {
            sdf.show();
        } else {
            sdf.write().mode("overwrite").parquet(outputPath);
        }
    }
}
