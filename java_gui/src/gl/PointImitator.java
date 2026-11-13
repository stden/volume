package gl;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import data.Collector;

public class PointImitator implements Runnable {

  public static class Pointd {
    public double distance = 0;
    public double range = 0;
    public double signal = 0;
    public double angle = 0;

    public Pointd(double d, double r, double s, double a) {
      distance = d;
      range = r;
      signal = s;
      angle = a;
    }
  }

  Random f = new Random();
  Collector.Point point;
  PointImitator.Pointd pointd;
  int length = 100000;
  double x, y, z, a, r, d, v, fi;
  double dist, range, angle, rad;
  boolean vp;

  // ConcurrentLinkedQueue<Collector.Point> list = new
  // ConcurrentLinkedQueue<Collector.Point> ();
  // ConcurrentLinkedQueue<Collector.Point> aList = new
  // ConcurrentLinkedQueue<Collector.Point> ();
  ConcurrentLinkedQueue<PointImitator.Pointd> list =
      new ConcurrentLinkedQueue<PointImitator.Pointd>();

  /* dist,range,signal,angle */
  public void run() {
  /*
   * x = 0; y = 0; d = 0; v = 5; angle = 0; vp = true; a = 0; while (true){ if (
   * a <= 720){ if (list.size() > length) list.poll();
   * 
   * /*if (a >= 360) a = 0; else a = a + 0.1;
   */
  /*
   * a = a + 0.1; rad = Math.toRadians(a); fi = Math.cos(rad); range =
   * Math.sqrt(Math.pow(rad,2) + Math.pow(fi,2)); angle =
   * Math.toDegrees(Math.atan(Math.cos(rad)/rad));
   * 
   * //point = new Collector.Point((int) 0,(int) range,(short)
   * f.nextInt(2),(short) angle); pointd = new
   * PointImitator.Pointd(d,range,0,angle); list.add(pointd);
   *  // point = new Collector.Point(i%4000,j%4100,(short)
   * f.nextInt(500),(short) a ); //aList.add(point);
   * 
   * try{ Thread.sleep(100); } catch (InterruptedException ex) {} } else { a =
   * 0; d = d + 0.1; } }
   */
  }

  /*
   * public Collection<Collector.Point> getLastList(){ return
   * Collections.unmodifiableCollection(list); }
   * 
   * public Collection<Collector.Point> getAList(){ return
   * Collections.unmodifiableCollection(aList); }
   */

  public Collection<PointImitator.Pointd> getLastList() {
    return Collections.unmodifiableCollection(list);
  }
}
