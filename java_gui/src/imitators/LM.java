/**
 * imitators/LM.java
 * (c) 2008 RTI Systems 
 */

package imitators;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Imitates work of LM (Laser Module). Sends data over com port and network.
 * Imitator binds port (def. 3095) and wait udp packets with word "points", when
 * received, begin send data on packet sender address.
 */

public class LM implements Runnable {
  // *** consts ***
  final String PROPERTIES_FILE = "E:\\Dudko\\SLM_java\\src\\imitators\\lm_im.properties";
  /** Sended by udp-clients if then wants to recieve points */
  final String HELLO = "points";
  final int POINTS_PER_SECOND = 9;

  final byte NEWLINE = 0xA;

  // *** properties ***
  private final Properties properties;
  private final DatagramSocket socket;
  private FileWriter outputFile;
  private final Logger logger = Logger.getLogger(SLM.class.getName());
  private final LinkedList<InetSocketAddress> clients;
  private final Random errRand;
  private boolean outputToConsole = false;

  // last points generation time, msec
  private long lastGenTime;
  // no generated in last time points (or them parts)
  private double noGeneratedPoints;
  // imitation parameters
  private boolean moving = false;
  private double speed = 0.5;
  private double minDistance = 2.24;
  private double maxDistance = 47.04;
  private double position = 0;
  private double errorVariance = 1;
  private double errorMean = 0;

  // *** private methods ***
  private Properties getDefaultProperties() {
    Properties prop = new Properties();
    prop.setProperty("rs232", "none");
    prop.setProperty("udpport", "3095");
    prop.setProperty("outputFile", "lmpoints.txt");
    prop.setProperty("speed", "0.5");
    prop.setProperty("minDistance", "2.24");
    prop.setProperty("maxDistance", "47.04");
    prop.setProperty("errorVariance", "1");
    prop.setProperty("errorMean", "0");
    prop.setProperty("outputToConsole", "false");
    // prop.setProperty("", "");

    return prop;
  }

  /** Returns distance in ready to send format */
  private String getDistance() {
    double distance =
        (maxDistance - minDistance) - position + errRand.nextGaussian() * errorVariance + errorMean;
    String result =
        String.format("%05d.%02dm", (int) distance, (int) ((distance - (int) distance) * 100));
    logger.fine(result);
    return result + (char) NEWLINE;
  }

  /** Sends current distance to all clients from list */
  private void sendDistanceToClients() {
    ListIterator<InetSocketAddress> iterator = clients.listIterator();
    String distance = getDistance();
    while (iterator.hasNext()) {
      try {
        DatagramPacket packet =
            new DatagramPacket(distance.getBytes(), distance.length(), iterator.next());
        socket.send(packet);
      } catch (PortUnreachableException ex) {
        logger.warning("Port unreachable. Remove client");
        iterator.remove();
      } catch (IOException ex) {
        logger.warning("IOException occured.");
      }
    }
  }

  /** Sends currents distance via rs232 port */
  private void sendDistanceToRS232() {
  // TODO: realize
  }

  /** Writes distance in output file */
  private void sendDistanceToFile() throws IOException {
    outputFile.write(getDistance());
    outputFile.flush();
  }

  // *** public ***

  /** creates lm imitator instance */
  public LM() throws SocketException, BindException {
    properties = new Properties(getDefaultProperties());
    try {
      properties.load(new FileInputStream(PROPERTIES_FILE));
    } catch (FileNotFoundException ex) {
      logger.info("Properties file not found");
    } catch (IOException ex) {
      logger.warning("Properties file read error");
    }

    // bind udpport for hello waiting
    socket = new DatagramSocket(Integer.parseInt(properties.getProperty("udpport")));
    // set socket "almost unblocked" FIXME find other decision
    socket.setSoTimeout(1);

    outputToConsole = Boolean.parseBoolean(properties.getProperty("outputToConsole"));
    outputFile = null;
    if (!properties.getProperty("outputFile").equals("none")) {
      try {
        File of = new File(properties.getProperty("outputFile"));
        of.delete();
        if (of.createNewFile()) {
          outputFile = new FileWriter(of);
        } else {
          logger.warning("Can't create output file.");
        }
      } catch (IOException ex) {
        logger.log(Level.WARNING, "IOException while creating output file", ex);
      }
    }

    speed = Double.parseDouble(properties.getProperty("speed"));
    minDistance = Double.parseDouble(properties.getProperty("minDistance"));
    maxDistance = Double.parseDouble(properties.getProperty("maxDistance"));
    position = 0;

    errorMean = Double.parseDouble(properties.getProperty("errorMean"));
    errorVariance = Double.parseDouble(properties.getProperty("errorVariance"));

    moving = false;

    errRand = new Random();
    clients = new LinkedList<InetSocketAddress>();
    lastGenTime = System.currentTimeMillis();
  }

  public void setMoving(boolean start) {
    moving = start;
  }

  public boolean isMoving() {
    return moving;
  }

  public void run() {
    while (true) {
      try {
        // send points
        long timeDelta = System.currentTimeMillis() - lastGenTime;
        lastGenTime = System.currentTimeMillis();
        noGeneratedPoints += timeDelta / 1000. * POINTS_PER_SECOND;

        while (noGeneratedPoints > 1) {
          if (moving) {
            position += speed / POINTS_PER_SECOND;
            if (position > (maxDistance - minDistance)) {
              position = 0;
              moving = false;
              System.out.println("MaxDistance reached. Stopped.");
            }
          }
          try {
            sendDistanceToRS232();
            sendDistanceToClients();
            if (outputFile != null) {
              sendDistanceToFile();
            }
            if (outputToConsole) {
              System.out.print(getDistance());
            }

          } catch (IOException ex) {
            logger.log(Level.WARNING, "IOException occured, while send points.", ex);
          }
          noGeneratedPoints -= 1;
        }

        // test queires for points by udp
        byte[] buf = new byte[HELLO.length()];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        // hello string received, add sender to clients
        if (HELLO.equals(new String(buf))) {
          logger.info("UDPClient added");
          clients.add((InetSocketAddress) packet.getSocketAddress());
        }
      } catch (SocketTimeoutException ex) {
        // do nothing
      } catch (IOException ex) {
        logger.log(Level.WARNING, "IOException occured, while receive packets.", ex);
      }
    }
  }

  public static void main(String args[]) throws IOException {
    try {
      LM lm = new LM();
      Thread thrd = new Thread(lm);
      thrd.start();
      System.out.println("Press enter to start/stop moving.");
      while (System.in.read() != -1) {
        lm.setMoving(!lm.isMoving());
        System.out.print(lm.isMoving() ? "Started." : "Stopped.");
      }
      thrd.join();
    } catch (InterruptedException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Imitator thread interrupted.", ex);
    } catch (BindException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Can't bind port. Check rights.", ex);
    } catch (SocketException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Socket Exception", ex);
    }
  }
}
