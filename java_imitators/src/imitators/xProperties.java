/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class xProperties extends Properties {

  private static final long serialVersionUID = 1L;

  Imitator imitator;
  String prop_Filename;

  public xProperties(Imitator imitator, String prop_Filename) {
    this.prop_Filename = prop_Filename;
    this.imitator = imitator;
    try {
      load(new FileInputStream(prop_Filename));
      imitator.logger.info("Loaded properties file \"" + prop_Filename + "\" - "
          + Utils.lastModifiedDateTime(prop_Filename));
    } catch (FileNotFoundException ex) {
      imitator.logger.info("Properties file \"" + prop_Filename + "\" not found."
          + "Current directory = " + (new File("")).getAbsolutePath());
    } catch (IOException ex) {
      imitator.logger.warning("Properties file read error");
    }
  }

  public boolean getBoolean(String propName, boolean defaultPropValue) {
    try {
      return Boolean.parseBoolean(getProperty(propName));
    } catch (NullPointerException e) {
      imitator.logger.warning("Property \"" + propName + "\" not found in \"" + prop_Filename
          + "\"! " + propName + " = " + defaultPropValue);
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
}
