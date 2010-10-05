package com.github.luuuis.myzone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class holds information about the plugin, such as the current version.
 */
public class BuildConstants
{
    /**
     * The key for the property containing the version.
     */
    private static final String VERSION = "version";

    /**
     * The name of the properties file to read.
     */
    private static final String PROPERTIES_FILENAME = BuildConstants.class.getSimpleName() + ".properties";

    /**
     * Logger for this BuildConstants.
     */
    private final Logger log = LoggerFactory.getLogger(BuildConstants.class);

    /**
     * The encapsulated properties.
     */
    private final Properties props = new Properties();

    /**
     * Creates a new BuildConstants instance.
     */
    public BuildConstants() {
        InputStream in = BuildConstants.class.getResourceAsStream(PROPERTIES_FILENAME);
        if (in != null)
        {
            try
            {
                props.load(in);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error reading properties from: " + in, e);
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    log.warn("Error closing: "+ in, e);
                }
            }
        }
    }

    /**
     * Returns the version.
     *
     * @return a String containing the version
     */
    public String getVersion()
    {
        return props.getProperty(VERSION);
    }
}
