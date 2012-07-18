/**
 * http://www.progsystema.ru 
 */

package imitators;

/**
 * Emulates tube with constant radius R
 */
public class ConstSurface extends Surface {
  private double R = 0.;

  public ConstSurface(double R) {
    this.R = R;
  }

  public ConstSurface(xProperties properties) {
    R = properties.getDouble("surface.const.r", R);
  }

  @Override
  public double getRange(double distance, double angle) {
    return R;
  }

}
