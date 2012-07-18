/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.logging.*;
import static java.lang.Math.*;

/**
 * Imitates work of SLM MDA072B Note: Does't imitate signal level, only range
 */
public class SLM extends Imitator {

  public interface IListener {
    void update_SLM(SLM slm);
  }

  public final static int MAX_DATAGRAM_SIZE = 1500 - 42; // MTU - (Ethernet

  // header + ip header
  // + udp header)

  public static void main(String[] args) throws Exception {
    try {
      SLM slm = Imitator.Start(new SLM());
      slm.thread.join();
    } catch (InterruptedException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Imitator thread interrupted.", ex);
    } catch (BindException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Can't bind port. Check rights.", ex);
    } catch (SocketException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Socket Exception", ex);
    }
  }

  public int angle = 0, range = 0, signal = 0;

  final byte ASCII_POINT_SIZE = 18; // for ascii mode $rrrrr,ssss,aaaaa<lf>
  final byte BINARY_POINT_SIZE = 8; // for binary mode $rrssaa<lf>
  boolean binaryMode = false;
  InetAddress clientAddress;
  int clientPort;
  final byte CMD_ASCII = 'W'; // Scan data output in ASCII
  final byte CMD_BINARY = 'X'; // Scan data output in binary safety register
  // when followed Cx)
  final byte CMD_CONFIG = 'F'; // Selects angle increment & more
  final byte CMD_CURRENT_ANGLE = 'C'; // Current angle of the laser (or eye
  final byte CMD_DEVICE_IDENTIFY = '?'; // Causes unit to return simple rotated
  final byte CMD_DISABLE_FIRING = 'B'; // disables firing of laser
  final byte CMD_DO_ANGLES = 'U'; // Togles output of r,s,a or angles only
  final byte CMD_DO_REPLIES = 'H'; // Toggles output of replies to commands
  // string
  final byte CMD_ENABLE_FIRING = 'A'; // Enables firing of laser, when hrad
  final byte CMD_NETWORK = 'V'; // Configures network parameters
  final byte CMD_NONE = 0; // No cmd answer waited
  final byte CMD_ORIGIN = 'O'; // Sets/displays reference zero for encoder
  final byte CMD_POINTER_OFF = 'Q'; // Turns pointer off
  final byte CMD_POINTER_ON = 'P'; // Turns pointer on
  final byte CMD_RANGE = 'G'; // Show current range and signal strange

  final byte CMD_READ_STATUS = 'Y'; // SLM outputs status information
  final byte CMD_RESET_MAC = 'Z'; // Set MAC address

  final byte CMD_SELECT_SPEED = 'I'; // Selects speed of rotation
  final byte CMD_START_SCAN = 'S'; // Starts motor

  final byte CMD_STOP_SCAN = 'T'; // Stops motor
  final byte CMD_VER_DATE = 'J'; // Outputs version and date of software
  final byte DATA_BEGIN = '$'; // Data point begin symbol

  public double distance = 0;

  private boolean dot = false;

  /* accidental error of range parameters */
  double errorMean = 0, errorVariance = 0;

  int errors = 0;
  private boolean firing = false;
  boolean hundr = false;

  DatagramSocket IM_socket; // receive LM points (for surface generate
  // synchronization)

  /**
   * last filled buffer position, when it greater pointsBuf size, buffer will
   * sended
   */
  int lastBufPos = -1;
  public IListener listener = null;
  private String lmip = "127.0.0.1";
  final byte LMNEWLINE = 0x0A; // lm points delimeter
  private int lmport = 3095;
  final int MAX_ANGLE = 36000 - 1;
  int MAX_RANGE = 14999;
  final int MAX_SIGNAL = 9999;
  final short MAX_SPEED = 10;
  final short MIN_SPEED = 0;

  private boolean onlyAngles = false;
  private final int origin = 31075;

  /** points buffer (in ascii or binary fmt), created when scan started */
  byte[] pointsBuf = null;

  private long pointsCnt = 0;
  final String PROPERTIES_FILE = Utils.getBaseDir() + "slm_im.properties";
  private final Random random = new Random();
  private boolean replies = false;
  private boolean scaning = false;
  public int SendedPackets = 0;

  // receive commands to SLM
  DatagramSocket SLM_socket;
  int slmport = 30;

  // device parameters
  private byte speed = 1; // in Hz
  Surface surface = null;
  double zeroPerCircle = 0;

  /**
   * creates SLM imitator instance
   * 
   * @throws Exception
   */
  public SLM() throws Exception {
    properties = new xProperties(this, PROPERTIES_FILE);

    MAX_RANGE = properties.getInt("MAX_RANGE", MAX_RANGE);

    slmport = properties.getInt("slmport", slmport);
    SLM_socket = new DatagramSocket(slmport);
    readProcessCommand();

    lmport = properties.getInt("lmport", lmport);
    lmip = properties.getProperty("lmip", lmip);
    String LM_host = lmip + ":" + lmport;

    try {
      IM_socket = new DatagramSocket();
      IM_socket.connect(InetAddress.getByName(lmip), lmport);
      // send command to LM imitator, it adds us to points receivers
      String hello = IM_SendUDP.HELLO;
      IM_socket.send(new DatagramPacket(hello.getBytes(), hello.length()));
      logger.info("Connected to LM host: " + LM_host);
      readProcessDistanceFromIM();
    } catch (UnknownHostException ex) {
      logger.warning("LM host " + LM_host + " unknown. Works without.");
      IM_socket = null;
    } catch (IOException ex) {
      logger.log(Level.WARNING, "IOError occured. Works without LM imitator.", ex);
      IM_socket = null;
    }

    errorMean = Double.parseDouble(properties.getProperty("errorMean"));
    errorVariance = Double.parseDouble(properties.getProperty("errorVariance"));
    zeroPerCircle = Double.parseDouble(properties.getProperty("zeroPerCircle"));

    if (properties.getProperty("surface").equals("parabolic")) {
      logger.info("parabolic surface");
      surface = new ParabolicSurface(properties);
    } else if (properties.getProperty("surface").equals("parabolic3D")) {
      logger.info("parabolic3D");
      surface = new Parabolic3DSurface(properties);
    } else if (properties.getProperty("surface").equals("const")) {
      logger.info("const surface");
      surface = new ConstSurface(properties);
    } else {
      logger.warning("unknown surface - " + properties.getProperty("surface"));
      surface = null;
    }

    needFreq = 10 * pointsPerTime(1) / pointsPerPacket();
    logger.info("needFreq = " + needFreq);
  }

  /** Switches to ASCII mode */
  private void cmdAscii() {
    binaryMode = false;
    if (replies) sendAnswer("ASCII OUTPUT ENABLED");
  }

  /** Switches to BINARY mode */
  private void cmdBinary() {
    binaryMode = true;
    if (replies) sendAnswer("*BINARY");
  }

  /** Change angle accuracy */
  private void cmdConfig(String option) {
    if (option.charAt(0) == 'T') {
      hundr = false;
      if (replies) sendAnswer(binaryMode ? "*Tenth" : "1/10th selected");
    } else if (option.charAt(0) == 'H') {
      // If speed != 1, imitator do not set hundr and not send answer
      // TODO: determine what do device, in that case
      if (speed == 1) {
        hundr = true;
        if (replies) sendAnswer(binaryMode ? "*Hundr" : "1/100th selected");
      } else
        logger.warning("Not select 1/100th, becose speed not equal to one.");
    } else if (option.charAt(0) == 'E') {
      // i don't now what is that (in doc. nothing about). Added for
      // compartibility with london software
      // FIXME: actualize actions
      if (replies)
        sendAnswer(String.format((binaryMode ? "*F%05d" : "eye safety set to %5d"), Integer
            .parseInt(option.substring(1))));
    } else
      logger.log(Level.WARNING, "Unknown config option: {0}. Skipped.", option);
  }

  private void cmdCurrentAngle() {
    if (binaryMode)
      sendAnswer(String.format("*A%05d", angle));
    else
      sendAnswer(String.format("Angle- %5d", angle));
  }

  /** Send device id */
  private void cmdDeviceIdentify() {
    sendAnswer(binaryMode ? "*MDA072" : "MDA072B");
  }

  private void cmdDoAngles() {
    onlyAngles = !onlyAngles;
    // Same answer in BINARY and ASCII mode
    if (replies) sendAnswer(onlyAngles ? "ANGLES ENABLED" : "ANGLES DISABLED");
  }

  private void cmdDoReplies() {
    replies = !replies;
    if (replies) sendAnswer(binaryMode ? "*REPLON" : "REPLIES ON");
  }

  /** Enables / disables firing */
  private void cmdFiring(boolean enable) {
    firing = enable;
    scanOrFiringChange();
    if (replies) if (enable)
      sendAnswer(binaryMode ? "*Lasron" : "Laser on");
    else
      sendAnswer(binaryMode ? "*Lasoff" : "Laser off");
  }

  /** Send current origin */
  private void cmdOrigin() {
    sendAnswer(String.format(binaryMode ? "*O%05d" : "Origin = %5d", origin));
  }

  private void cmdPointer(boolean on) {
    dot = on;

    if (replies) if (on)
      sendAnswer(binaryMode ? "*DotOn " : "Red Dot on");
    else
      sendAnswer(binaryMode ? "*DotOff" : "Red Dot off");
  }

  private void cmdRange() {
    sendAnswer(String.format(binaryMode ? "*R%5d\n*S%05d" : "Range= %5d\nSigstren= %5d", range,
        signal));
  }

  private void cmdReadStatus() {
    if (binaryMode)
      // FIXME i'dont know what all this mean
      sendAnswer("*FLGS940070d1\n*A4294935566\n*WRD000\n*Binary");
    else
      // FIXME works partially, realize all functionality
      sendAnswer(String.format("FIFO empty\n" + "Enable low\n" + "Zero not found\n"
          + "Red dot %s\n" + "1/100th. %s\n" + "Not up to speed\n" + "Angle - %05d\n"
          + "Words left = 000\n" + "ASCII mode", dot ? "on" : "off", hundr ? "on" : "off", angle));
  }

  /**
   * Enables / disables motor rotating When rotating angles increments responds
   * to speed. When rotating and firing sends data packets with getted points
   */
  private void cmdScan(boolean start) {
    try {
      scaning = start;
      scanOrFiringChange();
      if (replies) {
        if (start) {
          sendAnswer(binaryMode ? "*STARTD" : "Scan started");
        } else {
          sendAnswer(binaryMode ? "*STOPED" : "Scan stopped");
        }
      }
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /** Change scan speed */
  private void cmdSelectSpeed(int speed) {
    if (speed >= MIN_SPEED && speed <= MAX_SPEED)
      this.speed = (byte) speed;
    else {
      logger.log(Level.WARNING, "Wrong speed: {0}, set to 1", speed);
      this.speed = 1;
    }
    if (replies)
      sendAnswer(String.format(binaryMode ? "*D%05d" : "Duty cycle set to %2d", this.speed));
  }

  private void cmdVerDate() {
    // Same answer in BINARY and ASCII mode
    sendAnswer("v3.1d - Oct 12 2007");
  }

  void createPointsBuf() {
    pointsBuf = new byte[pointsPerPacket() * pointSize()];
    lastBufPos = -1;
  }

  /**
   * Determine current time and lastGenTime delta, generate point that device
   * generate in that time and fill in pointsBuffer Also this function
   * increments angle.
   * 
   * @throws Exception
   */
  private void generatePoints() throws Exception {
    double timeDiff = (System.currentTimeMillis() - startTime) / 1000.0;
    long PointsNeedCnt = pointsPerTime(timeDiff);
    for (; pointsCnt < PointsNeedCnt; pointsCnt++) {
      // increment angle
      angle += hundr ? 1 : 10;
      angle %= MAX_ANGLE + 1;
      // if firing collect point
      if (firing) {
        // randomly return zero points
        boolean zeroPoint = abs(random.nextDouble()) < zeroPerCircle / 360 / (hundr ? 1 : 10);
        int packetID = SendedPackets;
        if (onlyAngles || surface == null || zeroPoint)
          putPointToBuf(0, packetID, angle);
        else {
          int ran = prepareRange(surface.getRange(distance, toRadians(angle / 100.)));
          putPointToBuf(ran, packetID, angle);
        }
        if (lastBufPos >= pointsBuf.length - 1) sendPoints();
      }
    }
  }

  @Override
  public void oneStep() {
    if (scaning) try {
      generatePoints();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception occured.", e);
      System.exit(1);
    }
    if (listener != null) listener.update_SLM(this);
  }

  /* Point size in bytes */
  byte pointSize() {
    return binaryMode ? BINARY_POINT_SIZE : ASCII_POINT_SIZE;
  }

  private int pointsPerPacket() {
    return MAX_DATAGRAM_SIZE / pointSize();
  }

  /**
   * Calcs how much points must be generated in specified time
   * 
   * @param time
   *          Time in seconds
   * @return Point count.
   */
  private long pointsPerTime(double time) {
    return (long) (360. * speed / (hundr ? 0.01 : 0.1) * time);
  }

  public int prepareRange(double range) {
    // accidental error calculation
    int range_int = (int) (100. * range + errorMean + errorVariance * random.nextGaussian());
    return range_int > MAX_RANGE ? MAX_RANGE : range_int;
  }

  // ** Process cmd and run needed function */
  void processCmd(String cmd) throws IOException {
    cmd = cmd.toUpperCase();
    logger.info("IN: " + cmd);
    switch (cmd.charAt(0)) {
      case CMD_ASCII:
        if (!(firing && scaning)) cmdAscii();
        break;
      case CMD_DEVICE_IDENTIFY:
        cmdDeviceIdentify();
        break;
      case CMD_BINARY:
        if (!(firing && scaning)) cmdBinary();
        break;
      case CMD_CURRENT_ANGLE:
        cmdCurrentAngle();
        break;
      case CMD_DO_REPLIES:
        cmdDoReplies();
        break;
      case CMD_VER_DATE:
        cmdVerDate();
        break;
      case CMD_DO_ANGLES:
        cmdDoAngles();
        break;
      case CMD_CONFIG:
        if (cmd.length() >= 2)
          cmdConfig(cmd.substring(1));
        else
          logger.warning("Config without param. Skipped.");
        break;
      case CMD_POINTER_ON:
        cmdPointer(true);
        break;
      case CMD_POINTER_OFF:
        cmdPointer(false);
        break;
      case CMD_ORIGIN:
        cmdOrigin();
        break;
      case CMD_SELECT_SPEED:
        if (cmd.length() >= 2)
          try {
            cmdSelectSpeed(Integer.valueOf(cmd.substring(1)));
          } catch (NumberFormatException ex) {
            logger.warning("Select speed wrong param. Skipped.");
          }
        else
          logger.warning("Select speed without param. Skipped.");
        break;
      case CMD_RANGE:
        cmdRange();
        break;
      case CMD_READ_STATUS:
        cmdReadStatus();
        break;
      case CMD_ENABLE_FIRING:
        cmdFiring(true);
        break;
      case CMD_DISABLE_FIRING:
        cmdFiring(false);
        break;
      case CMD_START_SCAN:
        cmdScan(true);
        break;
      case CMD_STOP_SCAN:
        cmdScan(false);
        break;

      default:
        logger.warning("Unknown command: " + cmd.charAt(0));
    }
  }

  void putPointToBuf(int range, int signal, int angle) throws Exception {
    if (range <= 0 || range > 0xFFFF || range > MAX_RANGE) throw new Exception("range = " + range);
    if (angle < 0 || angle > MAX_ANGLE) throw new Exception("angle = " + angle);

    if (binaryMode) {
      pointsBuf[lastBufPos + 1] = DATA_BEGIN;

      pointsBuf[lastBufPos + 2] = (byte) ((range & 0xFF00) >> 8);
      pointsBuf[lastBufPos + 3] = (byte) (range & 0x00FF);

      if (pointsBuf[lastBufPos + 2] == 0 && pointsBuf[lastBufPos + 3] == 0)
        System.out.println(">> " + pointsBuf[lastBufPos + 2] + "  " + pointsBuf[lastBufPos + 3]);

      pointsBuf[lastBufPos + 4] = (byte) ((signal & 0xFF00) >> 8);
      pointsBuf[lastBufPos + 5] = (byte) (signal & 0x00FF);

      pointsBuf[lastBufPos + 6] = (byte) ((angle & 0xFF00) >> 8);
      pointsBuf[lastBufPos + 7] = (byte) (angle & 0x00FF);
      lastBufPos += BINARY_POINT_SIZE;

      pointsBuf[lastBufPos] = Utils.NEWLINE;
    } else {
      pointsBuf[lastBufPos + 1] = DATA_BEGIN;

      String strbuf = String.format("%05d,%04d,%05d", range, signal, angle);
      byte[] buf = strbuf.getBytes();
      for (int i = 0; i < ASCII_POINT_SIZE - 2; i++)
        pointsBuf[lastBufPos + 2 + i] = buf[i];
      lastBufPos += ASCII_POINT_SIZE;
      pointsBuf[lastBufPos] = Utils.NEWLINE;
    }
  }

  // ** reads and process command packet */
  private void readProcessCommand() {
    (new Thread(new Runnable() {

      @Override
      public void run() {
        logger.info("Bind slmport = " + slmport + " for command receiving");
        while (true) {
          byte[] buf = new byte[MAX_DATAGRAM_SIZE];
          DatagramPacket packet = new DatagramPacket(buf, MAX_DATAGRAM_SIZE);

          try {
            SLM_socket.receive(packet);

            // save first packet sender ip and port (if not connected) for send
            // data
            if (clientAddress == null) {
              clientAddress = packet.getAddress();
              clientPort = packet.getPort();
              // change port if packet sended from saved ip
            } else if (clientAddress.equals(packet.getAddress()) && clientPort != packet.getPort())
              clientPort = packet.getPort();

            // process buffer, find line end and give command before to
            // processCmd
            String strbuf = new String(buf);
            int LF_idx = strbuf.indexOf(Utils.NEWLINE);
            if (LF_idx == -1)
              logger.log(Level.WARNING, "In input buffer <lf> not found, skipped. Buffer: {0}",
                  strbuf);
            else if (LF_idx == 0)
              logger.log(Level.WARNING,
                  "In input buffer <lf> is first symbol, skipped. Buffer: {0}", strbuf);
            else
              processCmd(new String(buf).substring(0, LF_idx));
          } catch (IOException ex) {
            break;
          }
        }

      }

    })).start();
  }

  private void readProcessDistanceFromIM() {
    (new Thread(new Runnable() {

      @Override
      public void run() {
        while (IM_socket != null) {
          byte[] buf = new byte[MAX_DATAGRAM_SIZE];
          DatagramPacket packet = new DatagramPacket(buf, MAX_DATAGRAM_SIZE);
          try {
            IM_socket.receive(packet);
            distance = Double.parseDouble(new String(buf).replaceAll("[^0-9.]", ""));
          } catch (PortUnreachableException ex) {
            logger.warning("LM imitator unreachable");
            break;
          } catch (IOException ex) {
            break;
          }
        }
      }
    })).start();
  }

  /**
   * Determines begin or end of point collection If collection ended, buffer
   * sends and empty If collection started, pointBuf (ascii or binary) created
   */
  private void scanOrFiringChange() {
    if (firing && scaning && pointsBuf == null)
      // scan starting
      createPointsBuf();
    else if (!(firing && scaning) && pointsBuf != null) {
      // scan stopping
      if (lastBufPos != -1) sendPoints();
      pointsBuf = null;
    }
  }

  /**
   * Sends answer to client
   * 
   * @param answer
   *          Answer string
   */
  private void sendAnswer(String answer) {
    try {
      DatagramPacket packet =
          new DatagramPacket(answer.getBytes(), answer.length(), clientAddress, clientPort);
      SLM_socket.send(packet);
      logger.info("sendAnswer(\"" + answer + "\")");
    } catch (PortUnreachableException ex) {
      logger.warning("Port unreachable.");
    } catch (IOException ex) {
      logger.warning("IOException occured.");
    }
  }

  /** Send points from pointBuffer */
  private void sendPoints() {
    try {
      DatagramPacket packet =
          new DatagramPacket(pointsBuf, lastBufPos + 1, clientAddress, clientPort);
      SLM_socket.send(packet);
      SendedPackets++;
      lastBufPos = -1;
    } catch (PortUnreachableException ex) {
      logger.warning("Port unreachable.");
    } catch (IOException ex) {
      logger.warning("IOException occured.");
    }
  }

  @Override
  public void unbind() {
    if (SLM_socket != null) {
      SLM_socket.close();
      SLM_socket = null;
    }
    if (IM_socket != null) {
      IM_socket.close();
      IM_socket = null;
    }
  }
}
