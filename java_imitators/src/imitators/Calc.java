/**
 * http://www.progsystema.ru 
 */

package imitators;

public class Calc {
  public int xmin, xmax, ymin, ymax, xp, yp;

  private double alpha;

  public double findRoot(ISurface s, double left, double right, double eps) throws Exception {
    while (right - left > eps) {
      double LeftValue = value(s, left), RightValue = value(s, right);
      if (LeftValue * RightValue > 0) throw new Exception("Signs in ends must be different");
      double middle = (left + right) / 2.0;
      if (value(s, middle) * LeftValue > 0)
        left = middle;
      else
        right = middle;
    }
    return right;
  }

  public double getAlphaGrad() {
    return alpha * 180.0 / Math.PI;
  }

  public double getAlphaRad() {
    return alpha;
  }

  public void setAlphaGrag(double angle) {
    alpha = angle * Math.PI / 180.0;
  }

  public void setAlphaRad(double angle) {
    alpha = angle;
  }

  private double value(ISurface s, double x) {
    return ylx(x) - s.y(x);
  }

  public double ylx(double x) {
    return yp + Math.cos(alpha) * (x - xp) / Math.sin(alpha);
  }

}
