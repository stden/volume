package devices;

//import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * Represents laser module connected to rs232 serial port with voltmeter
 * connected to other rs232 serial port.
 */
public class RS232LM extends AbstractLM implements SerialPortEventListener {
  // const
  private final int PORT_OPEN_TIMEOUT = 500;
  private final int PACKET_LEN = 10;
  private final byte CMD_CR = 0xA;
  private final int MAX_DISTANCE = 9999999;

  // exceptions
  // class VM;

  // classes

  /** Works with LM battery voltmeter */
  class Voltmeter implements SerialPortEventListener {
    private final byte CMD_VOLTAGE = 'y';
    private final byte CMD_CR = 0xA;
    private final byte PACKET_LEN = 15;

    private SerialPort port;
    private OutputStream ostream;
    private InputStream istream;

    private double voltage = 0.;
    /** Store accepted bytes from port */
    private String data;

    /**
     * Create Volmeter instance
     * 
     * @param vmSerialPort
     *          voltmeter communication port
     */
    public Voltmeter(SerialPort vmSerialPort, Logger logger) throws TooManyListenersException,
        UnsupportedCommOperationException, IOException {
      port = vmSerialPort;
      port.addEventListener(this);
      port.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);
      port.notifyOnDataAvailable(true);
      port.notifyOnFramingError(true);
      port.notifyOnParityError(true);
      port.notifyOnOverrunError(true);
      istream = port.getInputStream();
      ostream = port.getOutputStream();
    }

    public void serialEvent(SerialPortEvent ev) {
      switch (ev.getEventType()) {
        case SerialPortEvent.FE:
          logger.warning("frame error");
          break;
        case SerialPortEvent.PE:
          logger.warning("parity error");
          break;
        case SerialPortEvent.OE:
          logger.warning("overrun error");
          break;
        case SerialPortEvent.DATA_AVAILABLE:
          readData();
          break;
      }
    }

    /**
     * Rreads data from port when CMD_CR symbol appear, process voltage
     */
    public void readData() {
      int readed, crpos = 0, lastpos = 0;
      byte[] buf = new byte[10];
      try {
        while (istream.available() > 0) {
          readed = istream.read(buf, 0, PACKET_LEN);

          lastpos = 0;
          while (lastpos < readed) {
            crpos = -1;
            for (int i = lastpos; i < readed; i++) {
              if (buf[i] == CMD_CR) {
                crpos = i;
                break;
              }
            }
            if (crpos < 0) {
              // CR not found
              int diff = data.length() + readed - PACKET_LEN;
              if (diff < 0) {
                data += new String(buf);
              } else {
                // drop data bigger then PACKET_LEN - 1
                data = data.substring(diff + 1) + new String(buf);
              }
              break;
            } else {
              // CR found, add buf part to data and process
              lastpos = crpos;
              data += new String(buf, 0, crpos + 1);
              processData(data);
              data = "";
            }
          }
        }
      } catch (IOException ex) {
        logger.warning("IOException");
      }
    }

    /** Try to get voltage from stored data */
    public void processData(String data) {
      if (data.length() == PACKET_LEN && data.substring(0, 3).equals("Batt")) {
        try {
          voltage = Double.parseDouble(data.substring(5, 8));
        } catch (NumberFormatException ex) {
          logger.log(Level.WARNING, "Wrong battery voltage value: {0}", data.substring(5, 8));
        }
      } else {
        logger.log(Level.WARNING, "Wrong battery voltage reply: {0}", data);
      }
    }

    /** Send command to voltmeter to send voltage */
    public void update() {
      try {
        ostream.write(CMD_VOLTAGE + CMD_CR);
      } catch (IOException ex) {
        logger.warning("IOException");
      }
    }

    /** Returns battery voltage from buffer */
    public double getVoltage() {
      return voltage;
    }
  }

  // properties
  private boolean collecting = false;
  private SerialPort lmPort;
  private SerialPort vmPort;
  private InputStream istream;
  private Voltmeter voltmeter;
  private final Logger logger = Logger.getLogger("svolume.RS232LM");
  private FileOutputStream fos;
  private String data = "";

  // public methods

  public RS232LM() {

  }

  /**
   * Creates a new instance of RS232LM
   * 
   * @param lmPortName
   *          LM communication port name
   * @param vmPortName
   *          Voltmeter communication port name
   */
  public void connect(String lmPortName, String vmPortName) throws NoSuchPortException,
      PortInUseException, TooManyListenersException, UnsupportedCommOperationException, IOException {
    lmPort =
        (SerialPort) CommPortIdentifier.getPortIdentifier(lmPortName).open("RS232LM",
            PORT_OPEN_TIMEOUT);
    lmPort.addEventListener(this);
    lmPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
        SerialPort.PARITY_NONE);
    lmPort.notifyOnDataAvailable(true);
    lmPort.notifyOnFramingError(true);
    lmPort.notifyOnParityError(true);
    lmPort.notifyOnOverrunError(true);
    istream = lmPort.getInputStream();

    fos = new FileOutputStream(new File("lmraw.log"));
    // vmPort = (SerialPort)
    // CommPortIdentifier.getPortIdentifier(vmPortName).open("RS232LM",
    // PORT_OPEN_TIMEOUT);
    // voltmeter = new Voltmeter(vmPort, logger);
  }

  /** Closes the communication ports */
  public void disconnect() {
    collecting = false;
    lmPort.close();
    vmPort.close();
  }

  @Override
  public void start() {
    // DistanceList.clear();
    collecting = true;
  }

  @Override
  public void stop() {
    collecting = false;
  }

  @Override
  public void updateBatteryVoltage() {
    voltmeter.update();
  }

  @Override
  public double getBatteryVoltage() {
    return voltmeter.getVoltage();
  }

  /** process serial ports events */
  public void serialEvent(SerialPortEvent ev) {
    switch (ev.getEventType()) {
      case SerialPortEvent.FE:
        logger.warning("Frame error");
        break;
      case SerialPortEvent.PE:
        logger.warning("Parity error");
        break;
      case SerialPortEvent.OE:
        logger.warning("Overrun error");
        break;
      case SerialPortEvent.DATA_AVAILABLE:
        readData();
        break;
    }
  }

  public void readData() {
    int readed, crpos = 0, lastpos = 0;
    byte[] buf = new byte[10];
    try {
      while (istream.available() > 0) {
        if (!collecting) {
          // don't started drop data
          istream.skip(istream.available());
        }

        readed = istream.read(buf, 0, PACKET_LEN);
        fos.write(buf);

        // System.out.println(new String(buf));
        lastpos = 0;
        while (lastpos < readed) {
          crpos = -1;
          for (int i = lastpos; i < readed; i++) {
            if (buf[i] == CMD_CR) {
              crpos = i;
              break;
            }
          }
          logger.log(Level.INFO, "crpos {0}", crpos);
          if (crpos < 0) {
            // CR not found
            data += new String(buf);
            if (data.length() > PACKET_LEN * 2) data = data.substring(PACKET_LEN);

            // int diff = data.length() + readed - PACKET_LEN;
            // logger.log(Level.INFO, "diff {0}", diff);
            // logger.log(Level.INFO, "data.length {0}", data.length());
            // if( diff < 0 ){
            // data += new String(buf);
            // } else if (diff == 0) {
            // //drop data bigger then PACKET_LEN - 1
            // data = data.substring(diff + 1) + new String(buf);
            // }
            break;
          } else {
            // CR found, add buf part to data and process
            lastpos = crpos + 1;
            data += new String(buf, 0, crpos + 1);
            processData(data);
            data = "";
          }
        }
      }
    } catch (IOException ex) {
      logger.warning("IOException");
    }
  }

  /** Get distance from data and store LMPoint */
  public void processData(String data) {
    logger.log(Level.INFO, "processdata: {0}", data);
    if (data.length() == PACKET_LEN) {
      try {
        double distance = Double.parseDouble(data.substring(0, PACKET_LEN - 2));
        if (((int) distance * 100) == MAX_DISTANCE) distance = 0; // no-hit
        // DistanceList.add(new LMPoint((int)(distance*100)) );
      } catch (NumberFormatException ex) {
        logger.log(Level.WARNING, "Wrong distance value: {0}", data.substring(5, 8));
      }
    } else {
      logger.log(Level.WARNING, "Wrong distance reply: {0}", data);
    }
  }

  /** Returns enumeration of available ports */
  public Enumeration availablePorts() {
    return CommPortIdentifier.getPortIdentifiers();
  }

}
