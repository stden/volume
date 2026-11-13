package devices;

/**
 * LMPoint representation
 */
public class LMPoint {
  public int distance; // distance in meters*100

  public LMPoint() {}

  /** Creates a new instance of LMPoint */
  public LMPoint(int distance) { // distance must distance*100
    this.distance = distance;
  }

}
