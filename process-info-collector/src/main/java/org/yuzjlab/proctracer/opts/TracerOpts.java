package org.yuzjlab.proctracer.opts;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.yuzjlab.proctracer.utils.ConfigurationManager;

@SuppressWarnings("unused")
public class TracerOpts {
    public static final String DEVNULL = "/dev/null";
    protected String frontendImplOptVal;

    public static final Map<String, Object> DEFAULT_CONFIG =
            Map.of("compressFmt", CompressFmt.PLAIN.toString(), "frontendImpl", "SIMPLE");
    protected CompressFmt compressFmt;
    protected long tracePID;

    public long getTracePID() {
        return tracePID;
    }

    public File getOutDirPath() {
        return outDirPath;
    }

    protected File outDirPath;
    protected Configuration config;

    public Configuration getConfig() {
        return this.config;
    }

    public CompressFmt getCompressFmt() {
        return this.compressFmt;
    }

    public TracerOpts(Configuration config) throws ConfigurationException {
        var className = TracerOpts.class.getCanonicalName();
        this.config = config;
        this.setCompressFmt(config.getString("%s.compressFmt".formatted(className), null));
        this.setFrontEndImpl(config.getString("%s.frontendImpl".formatted(className), null));
    }

    public static TracerOpts load(File configPath) throws ConfigurationException, IOException {
        var config = new PropertiesConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.read(new FileReader(configPath));
        return new TracerOpts(config);
    }

    public void save(Writer writer) throws ConfigurationException, IOException {
        var outConfig = new PropertiesConfiguration();
        var layOut = new PropertiesConfigurationLayout();
        layOut.setForceSingleLine(true);
        outConfig.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        outConfig.setLayout(layOut);
        outConfig.copy(this.config);
        outConfig.setProperty(
                "%s.compressFmt".formatted(this.getClass().getCanonicalName()),
                this.compressFmt.toString());
        outConfig.setProperty(
                "%s.frontendImpl".formatted(this.getClass().getCanonicalName()),
                this.frontendImplOptVal);
        outConfig.write(writer);
    }

    public void save(File configPath) throws ConfigurationException, IOException {
        this.save(new FileWriter(configPath));
    }

    // setSingleLine()

    public static Configuration getDefaultConfig() {
        var mc = new MapConfiguration(new HashMap<>());

        mc.append(ConfigurationManager.getDefaultConfig(TracerOpts.class));
        for (var dispatcherClassName : ConfigurationManager.ALL_CONFIGURABLE) {
            try {
                mc.append(
                        ConfigurationManager.getDefaultConfig(Class.forName(dispatcherClassName)));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return mc;
    }

    public void validate() throws IOException, ConfigurationException {
        FileUtils.deleteQuietly(this.outDirPath);
        FileUtils.forceMkdir(this.outDirPath);
        if (this.outDirPath.exists() && !this.outDirPath.isDirectory()) {
            throw new IOException("Failed to mkdir -p '%s'".formatted(this.outDirPath.toString()));
        }
        if (this.compressFmt == null) {
            throw new NullPointerException();
        }
        this.save(
                new File(Path.of(String.valueOf(this.outDirPath), "config.properties").toString()));
    }

    public void setOutDirPath(File outDirPath) {
        this.outDirPath = outDirPath;
    }

    public void setCompressFmt(String compressOptVal) throws ConfigurationException {
        try {
            this.compressFmt = CompressFmt.fromString(compressOptVal);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }

    public void setFrontEndImpl(String frontendImplOptVal) throws ConfigurationException {
        var validFrontendImpl = Set.of("NOP", "SIMPLE", "LOG");
        if (frontendImplOptVal == null || validFrontendImpl.contains(frontendImplOptVal)) {
            this.frontendImplOptVal = frontendImplOptVal;
            return;
        }
        throw new ConfigurationException(
                "frontendImpl should be one of [NOP, SIMPLE, LOG] or unspecified.");
    }

    public String getFrontendImplOptVal() {
        return this.frontendImplOptVal;
    }
}
