/**
 * http://www.progsystema.ru 
 */

package imitators;

import static java.lang.Math.*;

/**
 * Imitates surface as set of parabolic functions with walls and floor.
 */
public class ParabolicSurface extends Surface {

  static double[] findRoots(double A, double B, double C) {
    if (A != 0) {
      double D = B * B - 4 * A * C;
      if (D < 0) return new double[0];
      if (D == 0) return new double[] { -B / (2 * A) };
      return new double[] { (-B + sqrt(D)) / (2 * A), (-B - sqrt(D)) / (2 * A) };
    } else { // A==0
      if (B == 0) return new double[0];
      return new double[] { -C / B };
    }
  }

  private ParabolicSurfaceParams P[];

  public ParabolicSurface() {}

  public ParabolicSurface(ParabolicSurfaceParams P[], double leftwall, double rightwall,
      double floor) throws Exception {
    this.P = P;
    this.leftwall = leftwall;
    this.rightwall = rightwall;
    this.floor = floor;
    precalcBorders();
  }

  public ParabolicSurface(xProperties properties) throws Exception {
    super(properties);
    int pfcount = properties.getInt("surface.parabolic.n", 0);
    P = new ParabolicSurfaceParams[pfcount];
    for (int i = 0; i < pfcount; i++) {
      P[i] = new ParabolicSurfaceParams();
      P[i].A = properties.getDouble(String.format("surface.parabolic.a%d", i + 1), 0);
      P[i].B = properties.getDouble(String.format("surface.parabolic.b%d", i + 1), 0);
      P[i].C = properties.getDouble(String.format("surface.parabolic.c%d", i + 1), 0);
      P[i].W = properties.getDouble(String.format("surface.parabolic.W%d", i + 1), 0);
    }
    precalcBorders();
  }

  /** Return minimal R for N parabolic function */
  private double calcRN(ParabolicSurfaceParams p, double distance, double phi) throws Exception {
    double R = Double.POSITIVE_INFINITY;
    if (abs(phi - 3.0 / 2.0 * PI) < 0.01) return Double.POSITIVE_INFINITY;
    if (abs(phi - 1.0 / 2.0 * PI) < 0.01) return Double.POSITIVE_INFINITY;
    for (double x : findRoots(p.A, p.B - tan(phi), p.C)) {
      if (x >= p.LeftBorder && x <= p.RightBorder) R = min(R, abs(x / cos(phi)));
      if (R == 0) throw new Exception("R==0 check parametrs!");
    }
    return R;
  }

  @Override
  public double getRange(double distance, double phi) throws Exception {
    double R = testWallsFloor(phi);
    phi += PI / 2.0; // !!!!
    // test parabolic functions
    for (ParabolicSurfaceParams p : P)
      R = min(R, calcRN(p, distance, phi));
    return R;
  }

  private void precalcBorders() {
    double curBorder = leftwall;
    for (int i = 0; i < P.length; i++) {
      P[i].LeftBorder = curBorder;
      curBorder += P[i].W;
      P[i].RightBorder = curBorder;
    }
  }
}
