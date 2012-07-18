/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class Utils {

  final static byte NEWLINE = 0x0A; // Command/point end symbol

  /*
   * Directory for all properties files
   */
  public static String getBaseDir() {
    return RemoveEnds(getCurrentDirectory(), new String[] { "\\src" }) + "\\";
  }

  private static String getCurrentDirectory() {
    return new File("").getAbsolutePath();
  }

  public static String getInstallPath() {
    return RemoveEnds(getCurrentDirectory(), new String[] { "\\src", "\\java_imitators" });
  }

  static String lastModifiedDateTime(final String FileName) {
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
    Date date = new Date((new File(FileName)).lastModified());
    return df.format(date);
  }

  private static String RemoveEnds(String path, String[] del) {
    for (String p : del)
      if (path.endsWith(p)) path = path.replace(p, "");
    return path;
  }
}
