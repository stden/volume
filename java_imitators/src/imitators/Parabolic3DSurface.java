/**
 * http://www.progsystema.ru 
 */

package imitators;

public class Parabolic3DSurface extends Surface {
  double c[][] = new double[3][3]; // Coeficients

  public Parabolic3DSurface() {}

  public Parabolic3DSurface(xProperties properties) {
    super(properties);
    for (int x_deg = 0; x_deg <= 2; x_deg++)
      for (int y_deg = 0; y_deg <= 2; y_deg++)
        c[x_deg][y_deg] =
            properties.getDouble(String.format("surface.parabolic3D.c%d%d", x_deg, y_deg), 0);
  }

  public double getHeight(double x, double y) {
    double res = 0, xx = 1;
    for (int x_deg = 0; x_deg <= 2; x_deg++, xx *= x) {
      double yy = 1;
      for (int y_deg = 0; y_deg <= 2; y_deg++, yy *= y)
        res += c[x_deg][y_deg] * xx * yy;
    }
    return res;
  }

  @Override
  public double getRange(double distance, double phi) throws Exception {
    double sc[] = new double[3];
    for (int x_deg = 0; x_deg <= 2; x_deg++) {
      sc[x_deg] = 0;
      double yy = 1;
      for (int y_deg = 0; y_deg <= 2; y_deg++, yy *= distance)
        sc[x_deg] += c[x_deg][y_deg] * distance;
    }
    double R = Double.POSITIVE_INFINITY;
    for (double x : ParabolicSurface.findRoots(sc[2], sc[1] - Math.tan(phi), sc[0]))
      R = Math.min(R, Math.abs(x / Math.cos(phi)));
    return R;
  }

  public double[][] Integral() {
    double res[][] = new double[c.length + 1][c[0].length + 1];
    for (int a = 0; a <= 2; a++)
      for (int b = 0; b <= 2; b++)
        res[a + 1][b + 1] = c[a][b] / ((double) (a + 1) * (b + 1));
    return res;
  }
}
