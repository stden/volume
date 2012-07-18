package devices;

/**
 * Laser Module interface
 */
public interface LMInterface {

  /** Start point collectioning */
  public void start();

  /** Stop point collectioning */
  public void stop();

  /** @returns available points count */
  public int pointsAvailable();

  /**
   * Returns and remove oldest point
   * 
   * @return oldest point or null if no points available
   */
  public LMPoint getPoint(int position);

  public double getavgDistance();

  /** @returns battery voltage from buffer */
  public double getBatteryVoltage();

  /** Query battery voltage */
  public void updateBatteryVoltage();
}
