package devices;

/**
 * SLMPoint representation
 */
public class SLMPoint {

  public int range;
  public short signal;
  public int angle;

  /** Creates a new instance of SLMPoint */
  public SLMPoint() {}

  /** Creates a new instance of SLMPoint, and accepts data */
  public SLMPoint(int range, short signal, int angle) {
    this.range = range;
    this.angle = angle;
    this.signal = signal;
  }

}
