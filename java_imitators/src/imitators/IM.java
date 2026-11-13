/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Imitates work of LM (Laser Module). Sends data over com port and network.
 * Imitator binds port (def. 3095) and wait UDP packets with word "points", when
 * received, begin send data on packet sender address.
 */
public class IM extends Imitator {

  public interface IListener {
    public void close();

    public void sendDistance(String distance, IM im) throws IOException;
  }

  static String FormatDistance(double distance) {
    return String.format(Locale.US, "%08.02fm", distance) + (char) Utils.NEWLINE;
  }

  public static void main(String args[]) throws Exception {
    try {
      IM im = Start(new IM());
      im.listeners.add(new IM_DebugToConsole(im.properties));
      System.out.println("Press 'Enter' to start/stop moving.");
      while (true) {
        System.in.read(); // Wait for Enter
        System.in.skip(System.in.available());
        im.moving = !im.moving;
        System.out.println(im.moving ? "Started." : "Stopped.");
      }
    } catch (BindException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Can't bind port. Check rights.", ex);
    } catch (SocketException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Socket Exception.", ex);
    } catch (Exception ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Exception.", ex);
    }
  }

  private final Random errRand = new Random();

  public final LinkedList<IListener> listeners = new LinkedList<IListener>();
  // imitation parameters
  public boolean moving = false;
  public String outputFileName = "none";

  final int POINTS_PER_SECOND = 9;
  final String PROPERTIES_FILE = Utils.getBaseDir() + "lm_im.properties";
  double speed, minDistance = 2.24, maxDistance = 47.04, position, errorVariance = 1,
      errorMean = 0;

  public IM() throws Exception {
    properties = new xProperties(this, PROPERTIES_FILE);
    listeners.add(new IM_SendUDP(properties));
    needFreq = POINTS_PER_SECOND;
    speed = properties.getDouble("speed", speed);
    minDistance = properties.getDouble("minDistance", minDistance);
    maxDistance = properties.getDouble("maxDistance", maxDistance);
    errorMean = properties.getDouble("errorMean", errorMean);
    errorVariance = properties.getDouble("errorVariance", errorVariance);
    position = properties.getDouble("startPosition", maxDistance);
    if (!properties.getProperty("outputFile").equals("none"))
      try {
        IM_WriteFile fileSender =
            new IM_WriteFile(Utils.getBaseDir() + properties.getProperty("outputFile"));
        outputFileName = fileSender.of.getAbsolutePath();
        logger.info("Write distanse to file \"" + outputFileName + "\"  Distance: max = "
            + maxDistance + "  min = " + minDistance);
        listeners.add(fileSender);
      } catch (IOException ex) {
        logger.log(Level.WARNING, "IOException while creating output file", ex);
      }
  }

  void doStep() {
    if (moving) {
      position -= speed / POINTS_PER_SECOND;
      if (position < minDistance) {
        moving = false;
        position = minDistance;
        logger.info("MinDistance = " + position + " reached. Stopped.");
      }
      if (position > maxDistance) {
        moving = false;
        position = maxDistance;
        logger.info("MaxDistance = " + position + " reached. Stopped.");
      }
    }
  }

  /** Generate and returns distance in ready to send format */
  String genDistance() {
    return FormatDistance(position + errRand.nextGaussian() * errorVariance + errorMean);
  }

  @Override
  public void oneStep() {
    doStep();
    sendDistanceToAll(genDistance());
  }

  void sendDistanceToAll(String distance) {
    ListIterator<IListener> iterator = listeners.listIterator();
    while (iterator.hasNext())
      try {
        iterator.next().sendDistance(distance, this);
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  @Override
  public void unbind() {
    ListIterator<IListener> iterator = listeners.listIterator();
    while (iterator.hasNext())
      iterator.next().close();
  }

}