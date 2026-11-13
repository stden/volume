package devices;

/**
 * Implements runnable LMInterface, realize getPoint, pointAvailable, getBattery
 */
public abstract class AbstractLM implements LMInterface {

  // properties

  /** Safe thread stop flag */
  protected boolean stopThread = false;
  // protected LinkedList<LMPoint> DistanceList = new LinkedList<LMPoint>;
  public final int DistantListSize = 50;
  protected LMPoint DistanceList[] = new LMPoint[DistantListSize];
  // protected ArrayList<LMPoint> DistanceList = new
  // ArrayList<LMPoint>(DistantListSize);
  protected double batteryVoltage = 0;
  public double avgDistanse = 0;
  public double CurrentAvgDist = 0;

  // public methods

  /** Creates a new instance of AbstractLM */
  public AbstractLM() {}

  /** Safe thread stop */
  public void stopThread() {
    stopThread = true;
  }

  public double getBatteryVoltage() {
    return batteryVoltage;
  }

  /*
   * public LMPoint getPoint(){ return DistanceList.poll(); }
   */
  public LMPoint getPoint(int position) {
    // return storage.poll();
    return DistanceList[position];
  }

  public double getavgDistance() {
    // TODO Auto-generated method stub
    return this.avgDistanse;
  }

  public int pointsAvailable() {
    return DistanceList.length;
  }

  public void start() {
  // TODO Auto-generated method stub

  }

  public void stop() {
  // TODO Auto-generated method stub

  }

  public void updateBatteryVoltage() {
  // TODO Auto-generated method stub

  }
}
