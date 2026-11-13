/**
 * http://www.progsystema.ru 
 */

package imitators;

import junit.framework.TestCase;

public class TestAll extends TestCase {
  // Sample surface for tests
  public class TestSurface implements ISurface {
    public double y(double x) {
      return 1 + Math.sin(x);
    }
  }

  public void testCalc() throws Exception {
    imitators.Calc c = new imitators.Calc();
    // Setup box params
    c.xmin = 0;
    c.xmax = 10;
    c.ymin = 0;
    c.ymax = 5;
    // Tube center coordinates
    c.xp = 5;
    c.yp = 4;
    c.setAlphaGrag(50);
    // Check convert Rad <-> Grad
    double eps = 0.001;
    assertEquals(50.0, c.getAlphaGrad());
    assertEquals(0.873, c.getAlphaRad(), eps);
    assertEquals(-0.195, c.ylx(0), eps);
    ISurface s = new TestSurface();
    assertEquals(1.0, s.y(0.0));
    double x0 = c.findRoot(s, 0.0, 5.0, eps);
    assertEquals(2.307, x0, eps);
    assertEquals(1.741, s.y(x0), eps);
  }

  public void testImitator() {
    Imitator im = new Imitator() {

      @Override
      public void oneStep() {
      // TODO Auto-generated method stub

      }

      @Override
      public void unbind() {}
    };
    im.needFreq = 10; // 10 Hz
    assertEquals(100, im.nextTickAt());
    assertEquals(Double.NaN, im.averageSpeed());
    im.stepsCounter++;
    assertEquals(200, im.nextTickAt()); // 100 ms
    im.curTime = 100;
    assertEquals(10.0, im.averageSpeed());
  }

  public void testIntegrate() {
    Parabolic3DSurface P3D = new Parabolic3DSurface();
    double cx = 1.23, cy = 2.33;
    P3D.c[2][1] = cx * cy; // x^2 * y
    double res[][] = P3D.Integral();
    // Integral( x^a * y^b ) = (x^(a+1)*y^(b+1))/((a+1)*(b+1))
    for (int a = 0; a < 3; a++)
      for (int b = 0; b < 3; b++)
        assertEquals(P3D.c[a][b] / ((double) (a + 1) * (b + 1)), res[a + 1][b + 1]);
  }

  public void testLM() throws Exception {
    IM im = new IM();
    assertEquals(9, im.POINTS_PER_SECOND);
    assertEquals(im.maxDistance, im.position);
    im.maxDistance = 10.1;
    im.minDistance = 9.1;
    im.errorVariance = 0.0;
    im.errorMean = 0.0;
    im.position = im.maxDistance;
    im.speed = 1.0;
    im.moving = true;
    assertEquals("00010.10m\n", im.genDistance());
    int i = 0;
    while (true) {
      im.doStep();
      i++;
      if (im.position <= im.minDistance) break;
      assertEquals(im.maxDistance - i * im.speed / im.POINTS_PER_SECOND, im.position, 0.00000001);
    }
    assertEquals(false, im.moving);
    assertEquals(im.minDistance, im.position);
    // --- check maxDistance ---
    im.position = im.maxDistance - 0.0001;
    im.speed = -1.0;
    im.moving = true;
    im.doStep();
    assertEquals(false, im.moving);
    assertEquals(im.maxDistance, im.position);
    im.unbind();
    im = new IM();
    im.unbind();
  }

  public void testParabolic3DSurface() throws Exception {
    Parabolic3DSurface P3D = new Parabolic3DSurface();
    P3D.c[2][0] = 1.0; // x^2
    P3D.c[0][2] = 2.0; // y^2
    P3D.c[0][0] = 10.0; // C = 10
    double x = 2.0, y = 1.1;
    assertEquals(1.0 * x * x + 2.0 * y * y + 10.0, P3D.getHeight(x, y));
    // --- x^2 * y^2
    P3D = new Parabolic3DSurface();
    P3D.c[2][2] = 1.0; // x^2 * y^2
    assertEquals(x * x * y * y, P3D.getHeight(x, y));
  }

  public void testParabolicSurface() throws Exception {
    double C = 10.0;
    double leftwall = -16.0, rightwall = 16.1, floor = 12.0;

    ParabolicSurfaceParams[] P = new ParabolicSurfaceParams[1];
    P[0] = new ParabolicSurfaceParams();
    P[0].A = 0;
    P[0].B = 0;
    P[0].C = C;
    P[0].W = 100;

    ParabolicSurface surface = new ParabolicSurface(P, leftwall, rightwall, floor);

    assertEquals(-16.0, surface.leftwall);
    assertEquals(16.1, surface.rightwall);
    assertEquals(12.0, surface.floor);

    double eps = 0.00001;

    // testLeftWall
    assertEquals(Math.abs(surface.leftwall), surface.testWallsFloor(Math.PI / 2), eps);
    assertEquals(Math.abs(surface.leftwall), surface.getRange(0, Math.PI / 2));

    // testRightWall
    assertEquals(Math.abs(surface.rightwall), surface.testWallsFloor(-Math.PI / 2), eps);
    assertEquals(Math.abs(surface.rightwall), surface.getRange(0, -Math.PI / 2));

    // testFloor
    assertEquals(Double.POSITIVE_INFINITY, surface.testWallsFloor(0), eps);
    assertEquals(surface.floor, surface.testWallsFloor(Math.PI), eps);
    assertEquals(surface.floor * Math.sqrt(2), surface.testWallsFloor(Math.PI - Math.PI / 4), eps);
    assertEquals(surface.floor * Math.sqrt(2), surface.testWallsFloor(Math.PI + Math.PI / 4), eps);
    assertEquals(C * Math.sqrt(2), surface.getRange(0, Math.PI / 4), eps);
    double save = surface.floor;
    surface.floor = 9.12;
    assertEquals(surface.floor, surface.testWallsFloor(Math.PI));
    assertEquals(surface.floor, surface.getRange(0, Math.PI));
    surface.floor = save;
    assertEquals(C, surface.getRange(0, 0));

    assertEquals(surface.floor * Math.sqrt(2), surface.testWallsFloor(Math.PI / 2 + Math.PI / 4),
        eps);

    assertEquals(C * Math.sqrt(2), surface.getRange(0, -Math.PI / 4), eps);
    for (double phi = -Math.PI / 4 + Math.PI; phi < Math.PI / 4 + Math.PI; phi += 0.1)
      assertEquals(C, surface.getRange(0, phi) * -Math.cos(phi), eps);
  }

  public void testPoly_PY() {
    Poly_XY p = new Poly_XY(2, 0); // a*x^2+b*x+c
    p.c[2][0] = 1.0; // p(x,y) = x^2
    assertEquals(p.c[2][0], 1.0);
    assertEquals(p.c[1][0], 0.0);
    assertEquals(p.c[0][0], 0.0);
    assertEquals(4.0, p.Value(2.0, 0));
    assertEquals(4.875, p.Volume(1.0, 2.5, 0.0, 1.0));
    // example 2 - x^2 * y^2
    p = new Poly_XY(2, 2);
    p.c[2][2] = 1.3;
    assertEquals(1.0172331, p.Volume(1.1, 1.2, 2.1, 3.0), 0.000001);
    // example 3 - 1
    p = new Poly_XY(0, 0);
    p.c[0][0] = 1.0;
    assertEquals(1.4, p.Volume(1.0, 2.4, 0.0, 1.0), 0.001);
    // example 4 - x^2 + x + 1
    p = new Poly_XY(2, 0);
    assertEquals(2, p.deg_x);
    assertEquals(0, p.deg_y);
    p.c[2][0] = 1.0;
    p.c[1][0] = 1.0;
    p.c[0][0] = 1.0;
    assertEquals(4.833, p.Volume(1.0, 2.0, 0.0, 1.0), 0.001);
    // example 5 -
    p = new Poly_XY(2, 2);
    p.c[2] = new double[] { 1.0, 2.0, 3.0 };
    p.c[1] = new double[] { 4.0, 5.0, 6.0 };
    p.c[0] = new double[] { 7.0, 8.0, 9.0 };
    p.c[0][0] = 7.0;
    assertEquals(10.888506, p.Volume(2.1, 3.0, 1.1, 1.2), 0.001);
  }

  public void testSLM() throws Exception {
    IM im = new IM();
    SLM slm = new SLM();

    assertEquals(100, slm.prepareRange(1.0));
    assertEquals(slm.MAX_RANGE, slm.prepareRange(Double.POSITIVE_INFINITY));

    boolean save = slm.binaryMode;
    slm.binaryMode = true;
    slm.createPointsBuf();
    assertEquals(-1, slm.lastBufPos);
    for (int range = 0; range <= slm.MAX_RANGE; range++) {
      slm.lastBufPos = -1;
      slm.putPointToBuf(range, 0, 0);
      slm.pointsBuf[2] = (byte) (range % 256);
      assertEquals(range / 256, toInt(slm.pointsBuf[1]));
      assertEquals(range % 256, toInt(slm.pointsBuf[2]));
      int rr = toInt(slm.pointsBuf[1]) * 256 + toInt(slm.pointsBuf[2]);
      assertEquals(range, rr);
    }
    slm.binaryMode = save;
    slm.lastBufPos = -1;

    assertEquals(0.0, slm.errorMean);
    assertEquals(0.0, slm.errorVariance);
    assertEquals(0.0, slm.zeroPerCircle);
    assertEquals(false, slm.hundr);
    im.moving = true;
    for (int i = 0; i < 10; i++) {
      im.doStep();
      String distance = im.genDistance();
      im.sendDistanceToAll(distance);
      Thread.sleep(10);
      assertEquals(distance, IM.FormatDistance(slm.distance));
    }
    VolumeMock volume = new VolumeMock("127.0.0.1", slm.slmport);
    String cmd[][] =
        { { "H", "REPLIES ON" }, { "X", "*BINARY" }, { "?", "*MDA072" },
            { "J", "v3.1d - Oct 12 2007" }, { "C", "*A00000" }, { "I00010", "*D00010" },
            { "FE1543", "*F01543" }, { "FT", "*Tenth" }, { "S", "*STARTD" }, { "A", "*Lasron" } };
    for (int i = 0; i < cmd.length; i++) {
      volume.Send(cmd[i][0] + "\n");
      // slm.readProcessPacket();
      assertEquals(cmd[i][1], volume.Recieve());
    }
  }

  public void testTextGen() throws Exception {
    double AN[] = { 1.0, 2.0, 3.0 };
    // Double AN2[] = TextGen.Pack(AN);
    // for (int i = 0; i < AN1.length; i++)
    // assertEquals(AN1[i], AN2[i]);
    assertEquals("double AN[] = { 1.0, 2.0, 3.0 };", TextGen.InitArray(AN, "AN"));
    // assertEquals("double AN[] = { 1.0, 2.0, 3.0 };", TextGen.InitArray(AN1,
    // "AN"));
    // int T[] = { 1, 2 };
    // assertEquals("int T[] = { 1, 2 };", TextGen.InitArray(TextGen.Pack(T),
    // "T"));
    // long L[] = { 1, 2 };
    // assertEquals("long L[] = { 1, 2 };", TextGen.InitArray(TextGen.Pack(L),
    // "L"));
    // byte B[] = { 2, 3 };
    // assertEquals("byte B[] = { 2, 3 };", TextGen.InitArray(TextGen.Pack(B),
    // "B"));
    // short S[] = { 1, 3 };
    // assertEquals("short S[] = { 1, 3 };", TextGen.InitArray(TextGen.Pack(S),
    // "S"));
    // char C[] = { 'A', 'B' };
    // assertEquals("char C[] = { 'A', 'B' };",
    // TextGen.InitArray(TextGen.Pack(C), "C"));
    // String SS[] = { "aa", "bb" };
    // assertEquals("String SS[] = { \"aa\", \"bb\" };", TextGen.InitArray(SS,
    // "SS"));
  }

  public void testTube() {
    ConstSurface cs = new ConstSurface(10);
    assertEquals(10.0, cs.getRange(10, 100));
  }

  private int toInt(byte b) {
    return b < 0 ? 256 + b : b;
  }
}