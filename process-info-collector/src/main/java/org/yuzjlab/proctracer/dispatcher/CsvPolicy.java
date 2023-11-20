package org.yuzjlab.proctracer.dispatcher;

import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.csv.CSVPrinter;

public class CsvPolicy {
    protected final String name;
    protected final String[] header;
    protected final String[] schema;
    protected final Boolean[] isAccumulative;

    public CsvPolicy(Class<?> cls, String[] header, String[] schema, Boolean[] isAccumulative) {
        this.name = cls.getSimpleName();
        this.header = header;
        this.schema = schema;
        this.isAccumulative = isAccumulative;
    }

    public void writeHeader(CSVPrinter csvPrinter) throws IOException {
        csvPrinter.printRecord((Object[]) this.header);
    }

    @Override
    public String toString() {
        return "CsvPolicy<%s>(%s)".formatted(this.name, Arrays.toString(this.header));
    }
}
