package getProp;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class xProperties extends Properties {

  private static final long serialVersionUID = 1L;

  public String prop_Filename;
  protected Logger logger = Logger.getLogger("--");

  public xProperties(String prop_Filename) {
    this.prop_Filename = prop_Filename;
    try {
      load(new FileInputStream(prop_Filename));
    } catch (FileNotFoundException ex) {
      logger.info("Properties file \"" + prop_Filename + "\" not found");
    } catch (IOException ex) {
      logger.warning("Properties file read error");
    }
  }

  public boolean getBoolean(String propName, boolean defaultPropValue) {
    try {
      return Boolean.parseBoolean(getProperty(propName));
    } catch (NullPointerException e) {
      logger.warning("Property \"" + propName + "\" not found in \"" + prop_Filename + "\"! "
          + propName + " = " + defaultPropValue);
      return defaultPropValue;
    }
  }

  public double getDouble(String propName, double defaultValue) {
    return Double.parseDouble(getProperty(propName, Double.toString(defaultValue)));
  }

  public int getInt(String propName, int defaultValue) {
    return Integer.parseInt(getProperty(propName, Integer.toString(defaultValue)));
  }

  public long getLong(String propName, long defaultValue) {
    return Long.parseLong(getProperty(propName, Long.toString(defaultValue)));
  }

  public String getString(String propName, String defaultValue) {
    return getProperty(propName, defaultValue);
  }

}
