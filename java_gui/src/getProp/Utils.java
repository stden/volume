package getProp;

import java.io.File;
import java.net.*;
import java.text.DateFormat;
import java.util.Date;

public class Utils {
  /*
   * Directory for all properties files
   */

  public final static String PROPERTIES_DIR = getCurrentDirectory() + "\\";

  static String lastModifiedDateTime(final String FileName) {
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
    Date date = new Date((new File(FileName)).lastModified());
    return df.format(date);
  }

  public static void setSocketAlmostUnblocked(DatagramSocket socket) throws SocketException {
    // set socket "almost unblocked" FIXME find other decision
    socket.setSoTimeout(1);
  }

  public static String getCurrentDirectory() {
    return new File("").getAbsolutePath();
  }

  public static String getBaseDir() {
    return RemoveEnds(getCurrentDirectory(), new String[] { "\\src" }) + "\\";
  }

  public static String getInstallPath() {
    return RemoveEnds(getCurrentDirectory(), new String[] { "\\src", "\\java_imitators" });
  }

  private static String RemoveEnds(String path, String[] del) {
    for (String p : del)
      if (path.endsWith(p)) path = path.replace(p, "");
    return path;
  }

}
