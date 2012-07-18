/**
 * http://www.progsystema.ru 
 */

package imitators;

public class IM_DebugToConsole implements IM.IListener {

  private boolean outputToConsole = false;
  private long lastLogFile = System.currentTimeMillis();
  private long logSpeedPeriod = 2000;

  public IM_DebugToConsole(xProperties properties) {
    outputToConsole = properties.getBoolean("outputToConsole", outputToConsole);
    logSpeedPeriod = properties.getLong("logSpeedPeriod", logSpeedPeriod);
  }

  @Override
  public void close() {}

  @Override
  public void sendDistance(String distance, IM im) {
    if (outputToConsole) System.out.print("Send #" + im.stepsCounter + "  " + distance);
    if (System.currentTimeMillis() - lastLogFile > logSpeedPeriod) {
      im.logger.info(String.format(
          "sendedPoints = %d  distance = %s  average speed = %.1f points/sec", im.stepsCounter,
          distance.replace((char) Utils.NEWLINE, ' '), im.averageSpeed()));
      lastLogFile = System.currentTimeMillis();
    }
  }

}
