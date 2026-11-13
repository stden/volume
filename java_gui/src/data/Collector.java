package data;

import getProp.*;

import java.util.*;
import java.util.logging.Logger;

import devices.*;
import devices.SLM.State;

/**
 * Collects and store data from SLM and LM
 * 
 * Data stored as four numbers: 
 * int distanse -- LM distance * 100 
 * int range -- SLM range 
 * short signal -- SLM signal strength 
 * short angle -- SLM scan angle * 10
 */
public class Collector implements Runnable {

  // consts

  /** sleep time in ms, between slm points availability check */
  final int POINTS_CHECK_TIME = 5;
  String PROPERTIES_FILE = Utils.PROPERTIES_DIR + "slm_im.properties";
  xProperties properties = new xProperties(PROPERTIES_FILE);
  int lstart = -1;
  int lstop = -1;

  public static class Point {
    public double distance = 0; // average distance
    public int range = 0;
    public short angle = 0;
    public short signal = 0;

    public Point(double d, int r, short a, short s) {
      distance = d;
      range = r;
      angle = a;
      signal = s;
    }
  }

  // properties

  /** flag for safe thead stop */
  boolean stopThread = false;
  Logger logger = Logger.getLogger("svolume.Collector");
  /** Scan state */
  boolean started = false;
  SLM slm = null;
  LM lm = null;
  // LMInterface lm = null;
  /** Data processor fetch received points */
  Processor processor;
  /** Points storage */
  ArrayList<Point> storage = new ArrayList<Point>();
  double VOLUME = 0;
  StoreParams sp;

  // methods
  /** Creates a new instance of Collector */
  public Collector(LM lm, SLM slm, Processor prc, StoreParams sp) {
    this.slm = slm;
    this.lm = lm;
    this.processor = prc;
    this.sp = sp;
    storage.clear();
  }

  /**
   * start points collecting (NOTE: drops previous collected points)
   */
  public void start() throws SLM.WrongState {
    if (started) {
      logger.warning("Collector already started. Skip");
      return;
    }

    storage.clear();

    /*
     * try { slm.start(); } catch (SLM.WrongState ex) { logger.warning("SLM
     * not in right state. Skip stop."); throw ex; // return false; }
     */

    // lm.start();
    if (slm.getState() == State.startscan) {
      started = true;
    }
    return;
  }

  /**
   * stop points collecting
   */
  public void stop() throws SLM.WrongState {
    if (!started) {
      logger.warning("Collector not started. Skip stop.");
      return;
    }

    try {
      slm.stop();
    } catch (SLM.WrongState ex) {
      logger.warning("SLM not in right state. Skip stop.");
      throw ex;
      // return false;
    }
    // lm.stop();
    if (slm.getState() == State.startscan) {
      started = false;
    }
    return;
  }

  /** safe stop thread */
  public void stopThread() {
    stopThread = true;
  }

  /** @return part (no copy) of storage, safety to read? */
  public synchronized List<Point> getPoints() {
    return storage.subList(0, storage.size());
  }

  /** Thread main method */

  public void run() {

  }
}
/*
 * double stepSin=Math.sin ( Math.toRadians(0.1) ); boolean Error = false;
 * while(!stopThread){ if(started){ try{ Error = false; /* if
 * (slm.StartCalculation){ if(slm.pnow[slm.indx -
 * 1].distance>properties.getDouble("lstart",
 * lstart)||slm.pnow[slm.indx].distance>properties.getDouble("lstart",
 * lstart)|| slm.pold[slm.indx-1].distance>properties.getDouble("lstart",
 * lstart)||slm.pold[slm.indx].distance>properties.getDouble("lstart",
 * lstart)) { System.out.println(properties.getDouble("lstart", lstart));
 * System.out.println("Error Lstart"); Error = !Error; }
 * if(slm.pnow[slm.indx - 1].distance<properties.getDouble("lstop",
 * lstop)||slm.pnow[slm.indx].distance<properties.getDouble("lstop",
 * lstop)|| slm.pold[slm.indx-1].distance<properties.getDouble("lstop",
 * lstop)||slm.pold[slm.indx].distance<properties.getDouble("lstop",
 * lstop)) { System.out.println("Error Lstop"); Error = !Error; }
 */
// while have slm points
/*
 * double S1,S2, S; boolean f = true;
 */
/*
 * if ((!Error)&&(slm.indx-1)>0 && (lm.avgDistanse >=
 * (double)5.03) && (lm.avgDistanse <= (double)10)){ if(f) { //
 * сам алгоритм расчета либо один либо другой double R1 = 0.5 *
 * (slm.pnow[slm.indx-1].range + slm.pold[slm.indx-1].range);
 * double R2 = 0.5 * (slm.pnow[slm.indx].range +
 * slm.pold[slm.indx].range); double Rmin, Rmax; if(R1>R2) {
 * Rmax=R1; Rmin=R2;} else { Rmax=R2; Rmin=R1;}
 * 
 */
// S = 0.5*Rmin*Rmax*stepSin;
/*
 * S1=0.5*Rmin*Rmin*vparams.getStepSin()*vparams.getStepCos();
 * S2=0.5*Rmin*vparams.getStepSin()*(Rmax-Rmin*vparams.getStepCos());
 * S=S1+Math.abs(S2);
 */
/*
 * } else { /*
 * S1=slm.pold[slm.indx-1].range*slm.pold[slm.indx].range*stepSin/2.;
 * S2=slm.pnow[slm.indx].range*slm.pold[slm.indx].range*stepSin/2.;
 * S=(S1+S2)/2.;
 */
// }
/*
 * double
 * Lt=0.5*(slm.pnow[slm.indx-1].distance+slm.pnow[slm.indx].distance);
 * double
 * Lt0=0.5*(slm.pold[slm.indx-1].distance+slm.pold[slm.indx].distance);
 * double vol = Math.abs(Lt0-Lt)*S/100; VOLUME +=vol; sp.volume -=
 * vol; System.out.println("voll_current= " + vol+ ", VOLUME_free= " +
 * VOLUME+ ", FinishVolum= " + sp.volume+ "\n");
 */

/*
 * } //System.out.println("Wating........"); } }catch
 * (ArrayIndexOutOfBoundsException e) { // System.out.println("error
 * index= " + slm.indx); // System.out.println("error index= " +
 * slm.indx); }
 */

/*
 * while(slm.pointsAvailable()>0){ if(lm.getavgDistance()!= 0){
 * //lm.pointsAvailable()>0 SLMPoint slmp = slm.getPoint(); //
 * System.out.format("------------ angel "+ slmp.angle+", range "+
 * slmp.range+", signal "+ slmp.signal+"\n"); Collector.Point point =
 * new Collector.Point(lm.getavgDistance(), slmp.range, (short)
 * slmp.angle, (short) slmp.signal); synchronized(this){ /* Now we
 * don't need this, may be useful in future projects
 * storage.add(point);
 */
// send point to processor
// processor.processPoint(point);
/*
 * } } else { logger.severe("LM points not available, when SLM
 * point available. Skip point"); } }
 */
/*
 * try { Thread.sleep(POINTS_CHECK_TIME); } catch (InterruptedException
 * ex) { logger.warning("Thread was interrupted"); stopThread = true; // } /*
 * }else{ if(slm.getState() == State.startscan){ started = true; } }
 */
// }
