package devices;

import getProp.*;
import gui.Settings;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import data.*;

/**
 * Scaning laser module
 */

public class SLM implements Runnable {

  // const
  final static byte DATA_BEGIN = '$'; // Data point begin symbol
  final static byte NEWLINE = 0x0A; // Command/point end symbol
  final static byte CMD_NONE = 0; // No cmd answer waited
  final static byte CMD_BATTERY = '['; // Reports the battery voltage
  final static byte CMD_DEVICE_IDENTIFY = '?'; // Causes unit to return simple
  // string
  final static byte CMD_ENABLE_FIRING = 'A'; // Enables firing of laser, when
  // hrad rotated
  final static byte CMD_DISABLE_FIRING = 'B'; // disables firing of laser
  final static byte CMD_CURRENT_ANGLE = 'C'; // Current angle of the laser (or
  // eye safety register when
  // followed Cx)
  final static byte CMD_CONFIG = 'F'; // Selects angle increment & more
  final static byte CMD_DO_REPLIES = 'H'; // Toggles output of replies to
  // commands
  final static byte CMD_SELECT_SPEED = 'I'; // Selects speed of rotation
  final static byte CMD_VER_DATE = 'J'; // Outputs version and date of software
  final static byte CMD_ORIGIN = 'O'; // Sets/displays reference zero for
  // encoder
  final static byte CMD_POINTER_ON = 'P'; // Turns pointer on
  final static byte CMD_POINTER_OFF = 'Q'; // Turns pointer off
  final static byte CMD_START_SCAN = 'S'; // Starts motor
  final static byte CMD_STOP_SCAN = 'T'; // Stops motor
  final static byte CMD_NETWORK = 'V'; // Configures network parameters
  final static byte CMD_ASCII = 'W'; // Scan data output in ASCII
  final static byte CMD_BINARY = 'X'; // Scan data output in binary
  final static byte CMD_READ_STATUS = 'Y'; // SLM outputs status information
  final static byte CMD_RESET_MAC = 'Z'; // Set MAC address

  final static int MAX_RANGE = 65535;
  final static short MAX_SIGNAL = 4095;
  final static int MAX_ANGLE = 36000;
  final static short MAX_SPEED = 10;
  final static short MIN_SPEED = 1;

  final static byte BINARY_POINT_SIZE = 8; // for binary mode $rrssaa<lf>
  final static byte ASCII_POINT_SIZE = 18; // for ascii mode
  // $rrrrr,ssss,aaaaa<lf>

  public final static int MAX_DATAGRAM_SIZE = 1500;
  final static int SOCKET_TIMEOUT = 1000; // packet waiting interval, msec
  final static int MAX_WRONG_PACKETS = 5; // count of readed packets, after

  // command sended, before disconnect
  // status

  public enum State {
    disconnected, connecting, initializing, startscan, scaning, stopscan, cmdrun, idle
  }

  // exceptions

  /** Throwed when method can't be runned, becose SLM in wrong state */
  public class WrongState extends Exception {
  };

  // properties
  protected Logger logger = Logger.getLogger("svolume.SLM");
  protected boolean stopThread = false;
  protected InetAddress slmaddr;
  protected int slmport;
  protected DatagramSocket socket;

  /** command what answer waiter or NO_CMD if none */
  protected byte activeCmd = ' ';
  /** wrong packets received after command send */
  protected int wrongPackets = 0;
  /** SLM state */
  protected State state = State.disconnected;
  /** Object points buffer */
  protected ArrayList<SLMPoint> points = new ArrayList<SLMPoint>(200);
  protected boolean binaryMode = false;
  protected int scanSpeed = 0;
  protected double batteryVoltage = 0;
  protected int currentAngle = 0;
  protected int eyeSafetyRegister = 0;
  protected int versionMajor = 0;
  protected int versionMinor = 0;
  protected Date versionDate = null;
  protected boolean useHundrAngles = false;
  public int lastAngle = 0;

  FileWriter outputFile = null;
  File of = new File("E:\\dudko\\SLM_java\\SlmAngle.txt");

  // ****** my variable
  public class RDPoint {
    public double range;
    public double distance;

    public RDPoint(double range, double distance) {
      this.range = range;
      this.distance = distance;
    }
  }

  /** Save points from this cycle */
  public RDPoint[] pnow;
  /** Save points from prev and this cycle */
  public RDPoint[] pold;

  public static RDPoint rd1[];
  public static RDPoint rd2[];
  int rangeOld = 0;

  data.VolumeParams vparams;
  data.StoreParams sparams;

  LM lm = null;
  boolean StartWriteMassive = false;
  Settings settings;
  boolean flag = false;
  // public int indx = -1;
  public boolean StartCalculation = false;
  String PROPERTIES_FILE = Utils.PROPERTIES_DIR + "svolume.properties";
  xProperties properties = new xProperties(PROPERTIES_FILE);
  public int range = 0, signal = 0, angle = 0;
  public int missingAngle = 0;

  // methods
  /**
   * sends data to SLM
   * 
   * @param buf
   *          data to send
   */
  protected void sendData(byte[] buf) {
    DatagramPacket packet = new DatagramPacket(buf, buf.length, slmaddr, slmport);
    wrongPackets = 0;
    try {
      socket.send(packet);
    } catch (PortUnreachableException ex) {
      logger.severe("sendCmd. Port unreachable exception");
      state = State.disconnected;
      activeCmd = CMD_NONE;
    } catch (IOException ex) {
      logger.severe("sendCmd. IO exception");
      state = State.disconnected;
      activeCmd = CMD_NONE;
    }
  }

  /** send cmd without params and sets slm state */
  protected void sendCmd(byte cmd) {
    byte[] buf = { cmd, NEWLINE };
    activeCmd = cmd;
    wrongPackets = 0;
    sendData(buf);
    System.out.println("Send " + (char) cmd);
  }

  /** send cmd with one integer parametr (only unsigned supported) */
  protected void sendCmdInt(byte cmd, int param) {
    if (param > 0) {
      byte[] bParam = Integer.toString(param).getBytes();
      byte[] buf = new byte[2 + bParam.length];
      buf[0] = cmd;
      for (int i = 0; i < bParam.length; i++) {
        buf[i + 1] = bParam[i];
      }
      buf[buf.length - 1] = NEWLINE;
      activeCmd = cmd;
      wrongPackets = 0;
      sendData(buf);
    } else {
      logger.warning("sendCmdInt. param is negative. ignored");
    }
  }

  /** send cmd with one symbol param */
  protected void sendCmd(byte cmd, byte param) {
    byte[] buf = { cmd, param, NEWLINE };
    activeCmd = cmd;
    wrongPackets = 0;
    sendData(buf);
  }

  /** sends config (set angle hundredth or tenth) command to SLM */
  protected void sendConfig() {
    byte param = (byte) 'T';
    // hunderedth angle work only with 1Hz scan
    if ((scanSpeed == 1) && (useHundrAngles)) {
      param = (byte) 'H';
    }
    sendCmd(CMD_CONFIG, param);
  }

  /**
   * try to read packet from SLM and process them
   */
  protected void readPacket() {
    byte[] buf = new byte[MAX_DATAGRAM_SIZE];
    DatagramPacket packet = new DatagramPacket(buf, MAX_DATAGRAM_SIZE);
    try {
      socket.receive(packet);
      // logger.warning(new String(packet.getData(),0,packet.getLength()));
      // System.out.println(new String(packet.getData(),0,packet.getLength()));
    } catch (SocketTimeoutException ex) {
      state = State.disconnected;
      logger.warning("Socket timeout exception. Disconnected");
      return;
    } catch (PortUnreachableException ex) {
      state = State.disconnected;
      logger.warning("Port unreachable exception. Disconnected");
      return;
    } catch (IOException ex) {
      state = State.disconnected;
      logger.warning("IO exception. Disconnected");
      return;
    }

    int pointSize = (binaryMode) ? BINARY_POINT_SIZE : ASCII_POINT_SIZE;

    if (buf[0] != DATA_BEGIN) {
      // replie recieved
      String replie = new String(buf);
      System.out.println("Receive " + replie);
      int nlIdx = replie.indexOf(0);// NEWLINE
      if ((nlIdx > 0) && (nlIdx < replie.length() - 1)) {
        replie = replie.substring(0, nlIdx);
      }

      Matcher m;
      switch (activeCmd) {
        case CMD_DO_REPLIES:
          if (replie.equals("*REPLON") || replie.equals("REPLIES ON")) {
            state = State.initializing;
            if (binaryMode) {
              sendCmd(CMD_BINARY);
            } else {
              sendCmd(CMD_ASCII);
            }
          }
          break;

        case CMD_ASCII:
          if (replie.equals("ASCII OUTPUT ENABLED")) {
            sendCmd(CMD_DEVICE_IDENTIFY);
          }
          break;

        case CMD_BINARY:
          if (replie.equals("*BINARY")) {
            sendCmd(CMD_DEVICE_IDENTIFY);
          }
          break;

        case CMD_DEVICE_IDENTIFY:
          if (replie.equals(binaryMode ? "*MDA072" : "MDA072B")) {
            // FIXME: ver date format: v3.1d - Oct 12 2007
            // sendCmd(CMD_VER_DATE);
            sendCmd(CMD_CURRENT_ANGLE);
          }
          break;

        case CMD_VER_DATE:
          m = Pattern.compile("^v(\\d)\\.(\\d)\\s-\\s(\\d{2}/\\d{2}/\\d{2})$").matcher(replie);
          if (m.matches()) {
            try {
              versionMajor = Integer.decode(m.group(1));
              versionMinor = Integer.decode(m.group(2));
              SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
              versionDate = df.parse(m.group(3));
            } catch (ParseException ex) {
              logger.warning("Sofware revision date not parsed. Replie skipped");
              break;
            } catch (NumberFormatException ex) {
              logger.warning("Version not parsed. Replie skipped");
              break;
            }
            sendCmd(CMD_BATTERY);
          }
          break;

        case CMD_BATTERY:
          if (replie.equals(binaryMode ? "*MDA072" : "MDA072B")) {
            // FIXME: answer on battery voltage is device id
            // if(replie.matches("^\\d\\d\\.\\d$")){
            try {
              // batteryVoltage = Double.valueOf(replie);
            } catch (NumberFormatException ex) {
              logger.warning("Battery voltage not parsed. Replie skipped");
              break;
            }
            if (state == State.initializing)
              sendCmd(CMD_CURRENT_ANGLE);
            else if (state == State.startscan)
              sendCmd(CMD_START_SCAN);
            else if (state == State.stopscan)
              sendCmd(CMD_CURRENT_ANGLE);
            else
              activeCmd = ' ';
          }
          break;

        case CMD_CURRENT_ANGLE:
          m =
              Pattern.compile((binaryMode) ? "^\\*A(\\d{5})$" : "^Angle- (\\d{5})$")
                  .matcher(replie);
          Matcher mEyeSafe =
              Pattern.compile((binaryMode) ? "^*E(\\d{5})$" : "^Eye safe - (\\d{5})$").matcher(
                  replie);
          if (m.matches()) {
            try {
              currentAngle = Integer.valueOf(m.group(1));
            } catch (NumberFormatException ex) {
              logger.warning("Current angle not parsed. Replie skipped");
              break;
            }
            if (state == State.initializing)
              sendCmdInt(CMD_SELECT_SPEED, scanSpeed);
            else if (state == State.stopscan) {
              state = State.idle;
              activeCmd = CMD_NONE;
            } else if (state == State.idle) activeCmd = CMD_NONE;
            break;
          } else if (mEyeSafe.matches()) {
            try {
              eyeSafetyRegister = Integer.valueOf(m.group(1));
            } catch (NumberFormatException ex) {
              logger.warning("Eye safe register not parsed. Replie skipped");
              break;
            }
            activeCmd = CMD_NONE;
          }
          break;

        case CMD_SELECT_SPEED:
          m =
              Pattern.compile((binaryMode) ? "^\\*D000(\\d{2})$" : "^Duty cycle set to (\\d{2})$")
                  .matcher(replie);
          if (m.matches()) {
            try {
              if (Integer.valueOf(m.group(1)) == scanSpeed) {
                sendConfig();
              } else {
                logger.warning("Select speed not equal to asked. Replie skipped");
                break;
              }
            } catch (NumberFormatException ex) {
              logger.warning("Select speed not parsed. Replie skipped");
              break;
            }
          }
          break;

        case CMD_CONFIG:
          if (replie.equals((binaryMode) ? "*Hundr" : "1/100th selected")
              || replie.equals((binaryMode) ? "*Tenth" : "1/10th selected")) {
            state = State.idle;
            activeCmd = CMD_NONE;
            logger.info("SLM connected");
          }
          break;

        case CMD_START_SCAN:
          if (replie.equals((binaryMode) ? "*STARTD" : "Scan started")) {
            sendCmd(CMD_ENABLE_FIRING);
          }
          break;

        case CMD_ENABLE_FIRING:
          if (replie.equals((binaryMode) ? "*Lasron" : "Laser on")) {
            state = State.scaning;
          }
          break;

        case CMD_DISABLE_FIRING:
          if (replie.equals((binaryMode) ? "*Lasoff" : "Laser off")) {
            sendCmd(CMD_STOP_SCAN);
          }
          break;

        case CMD_STOP_SCAN:
          if (replie.equals((binaryMode) ? "*STOPED" : "Scan stopped")) {
            sendCmd(CMD_CURRENT_ANGLE);
          }
          break;

        case CMD_POINTER_ON:
          if (replie.equals((binaryMode) ? "*DotOn" : "Red dot on")) {
            activeCmd = CMD_NONE;
          }
          break;

        case CMD_POINTER_OFF:
          if (replie.equals((binaryMode) ? "*DotOff" : "Red dot off")) {
            activeCmd = CMD_NONE;
          }
          break;

        default:
          logger.warning("Not waited/wrong replie recieved.");
      }
    } else if ((buf[0] == DATA_BEGIN) && (packet.getLength() % pointSize == 0)) {
      if (state != State.scaning) {
        logger.warning("Get point, but not in scanning state, skipped. Try stop.");
        state = State.stopscan;
        activeCmd = CMD_STOP_SCAN;
        sendCmd(CMD_STOP_SCAN);
        return;
      }

      // points packet recieved, split them
      int firstByte = 0;

      for (int i = 0; i < packet.getLength() / pointSize; i++) {
        firstByte = i * pointSize;
        if (buf[firstByte] != DATA_BEGIN || buf[firstByte + pointSize - 1] != NEWLINE) {
          // wrong point
          logger.log(Level.WARNING, "Wrong point in packet, stop processing. First byte num: {0}",
              firstByte);
          break;
        } else {
          range = 0;
          signal = 0;
          angle = 0;
          if (binaryMode) {
            // all right collect point
            range = ((buf[firstByte + 1] << 8) & 0xFF00) + (buf[firstByte + 2] & 0xFF);
            signal = ((buf[firstByte + 3] << 8) & 0xFF00) + (buf[firstByte + 4] & 0xFF);
            angle = ((buf[firstByte + 5] << 8) & 0xFF00) + (buf[firstByte + 6] & 0xFF);

            try {
              if (of.createNewFile()) outputFile = new FileWriter(of);
            } catch (IOException ex) {
              logger.log(Level.WARNING, "IOException while creating output file", ex);
            }
            if (((lastAngle + 10) != angle) && (lastAngle != 35990) && (angle != 0)) {
              String str = String.valueOf("Angle= " + angle + ", LastAngle = " + lastAngle + "\n");
              missingAngle++;
              try {
                outputFile.write(str);
                outputFile.flush();
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }

          } else {
            String parseString = new String(buf, firstByte + 1, pointSize - 2);
            int fDel = parseString.indexOf(',');
            int sDel = parseString.lastIndexOf(',');

            try {
              range = Integer.parseInt(parseString.substring(0, fDel - 1));
              signal = Integer.parseInt(parseString.substring(fDel + 1, sDel - 1));
              angle = Integer.parseInt(parseString.substring(sDel + 1));
            } catch (NumberFormatException ex) {
              logger.log(Level.WARNING, "ASCII point not decoded, stop processing. Point: {0}",
                  parseString);
            }
          }

          /*
           * OnPointDetected(lastAngle, angle, range, lm.avgDistanse); lastAngle =
           * angle;
           */

          // if(range==MAX_RANGE) range = 0;//no-hit
          if ((range >= 0) && (range <= MAX_RANGE)) {
            if ((signal >= 0) && (signal <= MAX_SIGNAL)) {
              if ((angle >= 0) && (angle <= MAX_ANGLE)) {

                /*
                 * OnPointDetected(lastAngle, angle, range,
                 * lm.avgDistanse);//lm.avgDistanse lastAngle = angle;
                 */

                // if (angle == 0) StartCalculation = true;
                // if (StartCalculation)
                if ((lastAngle != angle) && (angle >= vparams.minAngle)
                    && (angle <= vparams.maxAngle)) OnPointDetected(angle, range, lm.avgDistanse);

                if (lm.avgDistanse <= 5.01) {
                  sparams.volume = 940.78;
                  sparams.FreeVolume = 859.22;
                }

                if (lastAngle > angle) {
                  if (pnow == rd1) {
                    pnow = rd2;
                    pold = rd1;
                  } else {
                    pnow = rd1;
                    pold = rd2;
                  }
                }

                lastAngle = angle;

                // StartCalculation = false;
                // lastAngle = angle;
                /*
                 * if (angle == 0){ StartWriteMassive = true; } if(lastAngle >
                 * angle) { if(pnow!=rd1) { pnow=rd1; pold=rd2; } else {
                 * pnow=rd2; pold=rd1; } }
                 * 
                 * if ((StartWriteMassive)&&(angle >= vparams.minAngle) &&
                 * (angle <= vparams.maxAngle)){ indx = (int) (angle -
                 * vparams.minAngle); if (vparams.step > 0.01){ indx *=
                 * vparams.step; } pnow[indx].range = range; pnow[indx].distance =
                 * lm.avgDistanse; this.StartCalculation = true; }else{
                 * this.StartCalculation = false; }
                 */
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // lastAngle = angle;!!!!!!!!!!!!!!!!!!!!!!!!
              } else {
                logger.log(Level.WARNING, "Angle is out of range ({0}).", angle);
              }
            } else {
              logger.log(Level.WARNING, "Signal is out of range ({0}).", signal);
            }
          } else {
            logger.log(Level.WARNING, "Range is out of range ({0}).", range);
          }
          // lastAngle = angle;
          // }
          // }
        }
      }
    } else {
      // unknown packet
      logger.warning("unknown packet fetched");
    }
  }

  // public methods

  /**
   * Creates a new instance of SLM
   */
  public SLM(LM lm, VolumeParams vp, StoreParams sp, Settings settings) throws SocketException {
    this.socket = new DatagramSocket();
    this.settings = settings;
    this.lm = lm;
    this.vparams = vp;
    this.sparams = sp;
    binaryMode = true;
    state = State.disconnected;
    // scanSpeed = 9;
    scanSpeed = settings.getRotFreq();// max=3, poterya
    useHundrAngles = false;
    logger.info("SLM created");
    rd1 = new RDPoint[vparams.pointsPerCycle];
    rd2 = new RDPoint[vparams.pointsPerCycle];
    for (int i = 0; i < vparams.pointsPerCycle; i++) {
      rd1[i] = new RDPoint(-1, -1);
      rd2[i] = new RDPoint(-1, -1);
    }
    this.pold = rd2;// 1
    this.pnow = rd1;// 2
  }

  /**
   * Try to connect to SLM and initialize them.
   * <p>
   * Working sheme: <br>
   * state = connecting -> REPLIES_ON -> state = initialazing -> BINARY|ASCII ->
   * DEVICE_IDENTIFY -> -> VER_DATE -> BATTERY -> CURRENT_ANGLE -> SELECT_SPEED ->
   * CONFIG -> state = idle
   * 
   * @param addr
   *          SLM Address
   * @param port
   *          SLM port
   */
  public void connect(InetAddress addr, int port) throws WrongState {
    if (state == State.disconnected) {
      slmport = port;
      slmaddr = addr;
      state = State.connecting;
      socket.connect(slmaddr, slmport);
      sendCmd(CMD_DO_REPLIES);
    } else {
      logger.severe("Can't connect to SLM, not disconnected");
      throw new WrongState();
    }
  }

  public boolean isConnected() {
    return (state != State.disconnected) && (state != State.connecting);
  }

  /**
   * Disconnect from slm
   */
  public void disconnect() throws WrongState {
    if (getState() == State.idle) {
      state = State.disconnected;
    } else {
      logger.severe("Can't disconnect from SLM, wrong state");
      throw new WrongState();
    }
  }

  public State getState() {
    if ((state == State.idle) && (activeCmd != CMD_NONE))
      return State.cmdrun;
    else
      return state;
  }

  /**
   * If SLM is not scanning stops SLM thread.
   */
  public void stopThread() throws WrongState {
    if (state != State.scaning && state != State.startscan && state != State.stopscan) {
      stopThread = true;
    } else {
      logger.severe("Cant stop thread. Still scanning.");
      throw new WrongState();
    }
  }

  /**
   * Start firing and points collecting
   * <p>
   * Working sheme: <br>
   * state = startscan -> BATTERY -> SCAN_START -> ENABLE_FIRING -> state =
   * scanning
   */
  public void start() throws WrongState {
    if ((state == State.idle) && (activeCmd == CMD_NONE)) {
      points.clear();
      state = State.startscan;
      sendCmd(CMD_START_SCAN);
    } else {
      logger.warning("Can't start scanning SLM not idle");
      throw new WrongState();
    }
  }

  /**
   * Stop firing and points collecting
   * <p>
   * Working sheme: <br>
   * state = stopscan -> DISABLE_FIRING -> SCAN_STOP -> BATTERY -> CURRENT_ANGLE ->
   * state = idle
   */
  public void stop() throws WrongState {
    if (state == State.scaning) {
      state = State.stopscan;
      sendCmd(CMD_STOP_SCAN);
      sendCmd(CMD_DISABLE_FIRING);
    } else {
      logger.warning("Can't stop scanning, not scannin.");
      throw new WrongState();
    }
  }

  /**
   * Power off or on red dot.
   * 
   * @param on
   *          Needed dot state true &mdash; on, false &mdash off.
   */
  public void setRedDot(boolean on) throws WrongState {
    if (state == State.idle && activeCmd == CMD_NONE) {
      sendCmd((on) ? CMD_POINTER_ON : CMD_POINTER_OFF);
    } else {
      throw new WrongState();
    }
  }

  /**
   * Send command to update battery voltage object buffer
   * 
   * @see #getBatteryVoltage()
   */
  public void updateBatteryVoltage() throws WrongState {
    if (state == State.idle && activeCmd == CMD_NONE) {
      sendCmd(CMD_BATTERY);
    } else {
      throw new WrongState();
    }
  }

  /**
   * @return Battery voltage from object buffer
   * @see #updateBatteryVoltage()
   */
  public double getBatteryVoltage() {
    return batteryVoltage;
  }

  /**
   * @return Current SLM angle.
   */
  public double getCurrentAngle() {
    return currentAngle;
  }

  /** return collected points count */
  public synchronized int pointsAvailable() {
    return points.size();
  }

  /** return and delete oldest point */
  public synchronized SLMPoint getPoint() {
    if (pointsAvailable() != 0)
      return points.remove(points.size() - 1);
    else
      return null;
  }

  public void OnPointDetected(int angle, int range, double distance) {
    int rmin = 10, rmax = 15000;

    if ((range < properties.getInt("rmin", rmin)) || (range > properties.getInt("rmax", rmax))) {
      range = rangeOld;
    } else {
      rangeOld = range;
    }

    int deltaprev = (int) (vparams.step * 100);
    int angprev = angle - deltaprev;
    int indx = (angle - vparams.minAngle);
    if (vparams.step > 0.01) {
      indx *= vparams.step;
    }
    pnow[indx].range = range / 100;
    pnow[indx].distance = distance;
    // /!!!!!!!!!!!!!!! Wall !!!!!!!!!!!!!!!! вставить!!!!!!!!!!!
    if ((angle > vparams.minAngle) && (angprev >= vparams.minAngle)) {
      CalcVolume(indx);
    }
  }

  public void CalcVolume(int indx) {
    if (pnow[indx - 1].distance > sparams.start || pnow[indx].distance > sparams.start
        || pold[indx - 1].distance > sparams.start || pold[indx].distance > sparams.start) {
      // System.out.println("Error Lstart");
      return;
    }

    if (pnow[indx - 1].distance < sparams.stop || pnow[indx].distance < sparams.stop
        || pold[indx - 1].distance < sparams.stop || pold[indx].distance < sparams.stop) {
      // System.out.println("Error Lstop");
      return;
    }

    double S1, S2, S;
    // усеченная пирамида
    /*
     * S1=pold[indx-1].range*pold[indx].range*vparams.stepSin/2.;
     * S2=pnow[indx-1].range*pold[indx].range*vparams.stepSin/2.;
     */

    if (settings.getModel().name().equals("avg")) {
      double R1 = 0.5 * (pnow[indx - 1].range + pold[indx - 1].range);
      double R2 = 0.5 * (pnow[indx].range + pold[indx].range);
      S = 0.5 * R1 * R2 * vparams.stepSin;
    } else {
      S1 = pold[indx - 1].range * pold[indx].range * vparams.stepSin / 2.;
      S2 = pnow[indx - 1].range * pold[indx].range * vparams.stepSin / 2.;
      S = (S1 + S2) / 2.;
    }

    double Lt = 0.5 * (pnow[indx - 1].distance + pnow[indx].distance);
    double Lt0 = 0.5 * (pold[indx - 1].distance + pold[indx].distance);
    double vol = 0;
    if (Lt0 > Lt) vol = Math.abs(Lt0 - Lt) * S;
    // /усеченная пирамида
    // vol = (S1 + Math.sqrt(S1*S2) + S2)*Math.abs(Lt0-Lt)/3;
    sparams.FreeVolume += vol;
    sparams.volume -= vol;
    sparams.ElVolume = vol;
    // System.out.println("voll_current= " + vol+ ", VOLUME_free= " +
    // sparams.FreeVolume+ ", FinishVolum= " + sparams.volume+ "\n");
  }

  /** SLM thread main method */
  public void run() {
    of.delete();
    while (!stopThread) {
      if ((state != State.disconnected) && !(state == State.idle && activeCmd == CMD_NONE)) {
        readPacket();
        // wrongPackets++;
        if (wrongPackets > MAX_WRONG_PACKETS && activeCmd != CMD_NONE && state != State.scaning) {
          logger.warning("MAX_WRONG_PACKETS reached, disconnect");
          state = State.disconnected;
          activeCmd = CMD_NONE;
          wrongPackets = 0;
        }
        if ((wrongPackets == MAX_WRONG_PACKETS) && (state == State.stopscan)) {
          state = State.idle;
          activeCmd = CMD_NONE;
        }
      }
    }
  }
}
