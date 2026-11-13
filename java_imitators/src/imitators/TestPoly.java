/**
 * http://www.progsystema.ru 
 */

package imitators;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class TestPoly {

  private void assertArrayEquals(double[] expected, double[] actual, double eps) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++)
      assertEquals(expected[i], actual[i], eps);
  }

  @Test
  public final void testValue() throws Exception {
    double eps = 0.0001;
    Random r = new Random();
    // const
    Poly p = new Poly(0);
    assertEquals(0, p.deg);
    double C = r.nextDouble();
    p.c[0] = C;
    assertEquals(C, p.Value(r.nextDouble()));
    // x + 2
    p = new Poly(1.0, 2.0);
    assertEquals(1, p.deg);
    assertEquals(1.0, p.c[1]);
    assertEquals(2.0, p.c[0]);
    assertEquals(5.0 + 2.0, p.Value(5));
    assertEquals(-2.0, p.findRoot(-3.0, 4.0, eps), eps);
    assertArrayEquals(new double[] { -2.0 }, p.findRoots(eps), eps);
    Poly d = p.derive();
    assertArrayEquals(new double[] { 1.0 }, d.c, eps);
    // (x+2) * (x+1)
    Poly t = p.mul(new Poly(1.0, 1.0));
    assertEquals(-2.0, t.findRoot(Poly.NEGATIVE_INFINITY, -1.5, eps), eps);
    assertArrayEquals(new double[] { -2.0, -1.0 }, t.findRoots(eps), eps);
    // (x+3) * (x+2) * (x+1)
    t = t.mul(new Poly(1.0, 3.0));
    assertArrayEquals(new double[] { -3.0, -2.0, -1.0 }, t.findRoots(eps), eps);
    // (x+3) * (x+2) * (x+1) * (x-14)
    t = t.mul(new Poly(1.0, -14.0));
    assertArrayEquals(new double[] { -3.0, -2.0, -1.0, 14.0 }, t.findRoots(eps), eps);
  }
}
