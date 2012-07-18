/**
 * http://www.progsystema.ru 
 */

package imitators;

public abstract class Func {
  public double findRoot(double left, double right, double eps) throws Exception {
    double rv = Value(right), lv = Value(left);
    if (rv * lv >= 0) throw new Exception("Signs in ends must be different!");
    while (right - left > eps) {
      double middle = (right + left) / 2, mv = Value(middle);
      if (mv * lv >= 0) {
        left = middle;
        lv = mv;
      } else {
        right = middle;
        rv = mv;
      }
    }
    return (right + left) / 2;
  }

  abstract double Value(double x);
}
