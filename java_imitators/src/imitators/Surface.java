/**
 * http://www.progsystema.ru 
 */

package imitators;

import static java.lang.Math.*;

/**
 * Abstract surface, used by imitators/SLM to imitate store surface.
 */
public abstract class Surface {

  double floor;
  double leftwall;
  double rightwall;

  public Surface() {}

  public Surface(xProperties properties) {
    leftwall = -abs(properties.getDouble("surface.leftwall", Double.POSITIVE_INFINITY));
    rightwall = abs(properties.getDouble("surface.rightwall", Double.POSITIVE_INFINITY));
    floor = abs(properties.getDouble("surface.floor", Double.POSITIVE_INFINITY));
  }

  /**
   * Abstract method, must be implemented in subclasses. Determine function of 2
   * arguments, that generate surface.
   * 
   * @param distance
   *          LM distance, m
   * @param phi
   *          angles, rad
   * @return range range, m
   */
  public abstract double getRange(double distance, double phi) throws Exception;

  protected double testWallsFloor(double phi) throws Exception {
    double sin = -sin(phi);
    double walls = sin > 0 ? rightwall / sin : leftwall / sin;
    double cos = cos(PI - phi);
    double floor = cos > 0 ? this.floor / cos : Double.POSITIVE_INFINITY;
    if (walls == 0) throw new Exception("walls == 0");
    if (floor == 0) throw new Exception("floor == 0");
    return min(walls, floor);
  }

}
