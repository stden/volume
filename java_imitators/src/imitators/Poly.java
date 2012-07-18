/**
 * http://www.progsystema.ru 
 */

package imitators;

public class Poly extends Func {

  public static final double NEGATIVE_INFINITY = -1000000000.0;
  private static final double POSITIVE_INFINITY = 1000000000.0;
  public final double[] c;
  public final int deg;

  public Poly(double... coef) {
    deg = coef.length - 1;
    c = new double[deg + 1];
    for (int i = 0; i < coef.length; i++)
      c[deg - i] = coef[i];
  }

  public Poly(int deg) {
    this.deg = deg;
    c = new double[deg + 1];
  }

  public Poly derive() {
    Poly res = new Poly(deg - 1);
    for (int i = 1; i <= deg; i++)
      res.c[i - 1] = i * c[i];
    return res;
  }

  public double[] findRoots(double eps) throws Exception {
    switch (deg) {
      case 0:
        return new double[0];
      case 1:
        return new double[] { -c[0] / c[1] };
      default:
        double extr[] = derive().findRoots(eps);
        double roots[] = new double[deg];
        roots[0] = findRoot(NEGATIVE_INFINITY, extr[0], eps);
        for (int i = 1; i < deg - 1; i++)
          roots[i] = findRoot(extr[i - 1], extr[i], eps);
        roots[deg - 1] = findRoot(extr[deg - 2], POSITIVE_INFINITY, eps);
        return roots;
    }
  }

  public Poly mul(Poly poly) {
    Poly res = new Poly(deg + poly.deg);
    for (int i = 0; i <= deg; i++)
      for (int j = 0; j <= poly.deg; j++)
        res.c[i + j] += c[i] * poly.c[j];
    return res;
  }

  public void testRoots(double[] roots, double eps) throws Exception {
    for (double root : roots)
      if (Math.abs(Value(root)) > eps)
        throw new Exception("Value (" + root + ") = " + Value(root));
  }

  @Override
  double Value(double x) {
    double res = 0;
    for (int i = deg; i >= 0; i--)
      res = res * x + c[i];
    return res;
  }
}
