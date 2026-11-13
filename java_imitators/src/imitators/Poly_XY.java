/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.util.Random;

public class Poly_XY {

  static Random rand = new Random();

  static double randomIn(double min, double max) {
    return rand.nextDouble() * (max - min) + min;
  }

  final double[][] c;

  final int deg_x;

  final int deg_y;

  public Poly_XY(int deg_x, int deg_y) {
    this.deg_x = deg_x;
    this.deg_y = deg_y;
    c = new double[deg_x + 1][deg_y + 1];
  }

  public double OnlyPositiveVolume(double xa, double xb, double ya, double yb, int iterations) {
    double sum = 0;
    for (int t = 0; t < iterations; t++) {
      double Value = Value(randomIn(xa, xb), randomIn(ya, yb));
      if (Value > 0) sum += Value;
    }
    double average_height = sum / iterations;
    double footarea = (xb - xa) * (yb - ya);
    return average_height * footarea;
  }

  public double Value(double x, double y) {
    double value = 0, xx = 1;
    for (int xi = 0; xi <= deg_x; xi++, xx *= x) {
      double yy = 1;
      for (int yi = 0; yi <= deg_y; yi++, yy *= y)
        value += c[xi][yi] * xx * yy;
    }
    return value;
  }

  public double Volume(double xa, double xb, double ya, double yb) {
    double volume = 0;
    for (int xi = 0; xi <= deg_x; xi++)
      for (int yi = 0; yi <= deg_y; yi++)
        volume +=
            c[xi][yi] * (Math.pow(xb, xi + 1) - Math.pow(xa, xi + 1))
                * (Math.pow(yb, yi + 1) - Math.pow(ya, yi + 1)) / ((double) (xi + 1) * (yi + 1));
    return volume;
  }
}
