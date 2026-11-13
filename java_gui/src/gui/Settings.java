package gui;

import java.io.*;
import java.net.InetAddress;

import data.StoreParams;

/**
 * Store all settings
 */

public class Settings implements Serializable {

  /** Battary nominal voltage */
  private double NominalVoltage;

  /** Min Angle, default = 90 */
  private int minAngle = 90;

  /** Max Angle, default = 270 */
  private int maxAngle = 270;

  /** Angular step */
  private double AngStep;

  /** Rotation Frequency */
  private int RotFreq;

  public enum CalcModel {
    /** found average value of signal, angle, range and distance */
    avg,
    /** simple take midle point */
    takeMidle
  }

  CalcModel model;

  // /**Processor settings*/
  // private int lastListLength;
  // private Processor.AvgType avgtype;
  // private int avgcount;

  /** Nets settings */
  private InetAddress slmIp;
  private int lmPort, lmPortBattery;

  /** Store settings */
  private StoreParams Store;

  /** Gradient settings */
  // private int gmethod;
  /** Creates a new instance of Settings */
  public Settings() {}

  /*
   * public Settings(double NominalVoltageLM, double NominalVoltageSLM, int
   * lastListLength, Processor.AvgType avgtype, int avgcount, InetAddress slmIp,
   * int slmPort, InetAddress lmIp, int lmPort, StoreParams Store) {
   * this.NominalVoltageLM = NominalVoltageLM; this.NominalVoltageSLM =
   * NominalVoltageSLM; this.lastListLength = lastListLength; this.avgtype =
   * avgtype; this.avgcount = avgcount; this.slmIp = slmIp; this.lmIp = lmIp;
   * this.slmPort = slmPort; this.lmPort = lmPort; this.Store = Store; }
   */

  /**
   * Write Settings object in file
   */
  public void write() throws IOException {
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("settings.dat"));
    out.writeObject(this);
  }

  /**
   * ReadSettings object from file
   */
  public static Settings read() throws Exception {
    ObjectInputStream in = new ObjectInputStream(new FileInputStream("settings.dat"));
    Settings set = (Settings) in.readObject();
    return set;
  }

  /* Accessors */
  public double getVoltage() {
    return this.NominalVoltage;
  }

  public double getAngStep() {
    return this.AngStep;
  }

  public int getRotFreq() {
    return this.RotFreq;
  }

  public int getMinAngle() {
    return this.minAngle;
  }

  public int getMaxAngle() {
    return this.maxAngle;
  }

  public CalcModel getModel() {
    return this.model;
  }

  /*
   * public Processor.AvgType getAvgType(){ return this.avgtype; }
   */

  /*
   * public int getAvgCount(){ return this.avgcount; }
   */

  public InetAddress getSlmIp() {
    return this.slmIp;
  }

  public int getLmPortBattery() {
    return this.lmPortBattery;
  }

  /*
   * public int getSlmPort(){ return this.slmPort; }
   */

  public int getLmPort() {
    return this.lmPort;
  }

  public StoreParams getStore() {
    return this.Store;
  }

  /* Modificators */
  public void setVoltage(double NominalVoltageLM) {
    this.NominalVoltage = NominalVoltageLM;
  }

  public void setAngStep(double Step) {
    this.AngStep = Step;
  }

  public void setRotFreq(int Freq) {
    this.RotFreq = Freq;
  }

  public void setMinAngle(int angle) {
    this.minAngle = angle;
  }

  public void setMaxAngle(int angle) {
    this.maxAngle = angle;
  }

  /*
   * public void setAvgType(Processor.AvgType avgtype){ this.avgtype = avgtype; }
   */

  /*
   * public void setAvgCount(int avgcount){ this.avgcount = avgcount; }
   */
  public void setModel(CalcModel model) {
    this.model = model;
  }

  public void setSlmIp(InetAddress slmIp) {
    this.slmIp = slmIp;
  }

  public void setLmPortBattary(int lmPortBattery) {
    this.lmPortBattery = lmPortBattery;
  }

  /*
   * public void setSlmPort(int slmPort){ this.slmPort = slmPort; }
   */

  public void setLmPort(int lmPort) {
    this.lmPort = lmPort;
  }

  public void setStore(StoreParams Store) {
    this.Store = Store;
  }

}
