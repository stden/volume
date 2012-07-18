package data;

import getProp.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * Prepare data for drawing and calc volume.
 */
public class Processor {

  String PROPERTIES_FILE = Utils.PROPERTIES_DIR + "slm_im.properties";
  xProperties properties = new xProperties(PROPERTIES_FILE);

  boolean flag = false;

  /**
   * Determines average list fill mechanizm
   */
  public enum AvgType {
    /** found average value of signal, angle, range and distance */
    avg,
    /** simple take midle point */
    takeMidle
  }

  public class RDPoint {
    public double range;
    public double distance;

    public RDPoint(double range, double distance) {
      this.range = range;
      this.distance = distance;
    }
  }

  /** Save points of two scan cycles and return them by cycle */
  public class VolumeCalcPoints {
    public int pointsPerCycle;
    /** Save points from prev and this cycle */
    public RDPoint oldpoints[];
    /** Save points from this cycle */
    public RDPoint nowpoints[];
    // boolean flag = false;
    /** Determines what cycle points go first */
    private final boolean prevFirst = false;
    private final int addedPoints = 0;
    public int lastAngel = 0;

    public VolumeCalcPoints(int pointsPerCycle) {
      this.pointsPerCycle = pointsPerCycle;
      //points = new RDPoint[pointsPerCycle*2]; 
      /*oldpoints = new RDPoint[pointsPerCycle]; 
      nowpoints = new RDPoint[pointsPerCycle];
      for(int i=0; i<pointsPerCycle; i++){
      	oldpoints[i] = new RDPoint(0,0);
      	nowpoints[i] = new RDPoint(0,0);
      }*/
    }

    public RDPoint getOld(int idx) {
      return oldpoints[idx];
    }

    public RDPoint getNow(int idx) {
      return nowpoints[idx];
    }

    /** @return Point from this cycle */
    public RDPoint get(int idx) {
      int pBegin = (prevFirst) ? pointsPerCycle : 0;
      return oldpoints[pBegin + idx];
    }

    /** @return Point from this previous cycle */
    public RDPoint getPrev(int idx) {
      int pBegin = (prevFirst) ? 0 : pointsPerCycle;
      return oldpoints[pBegin + idx];
    }

    /**
     * Add point to this cycle points. If this cycle points = pointsPerCycle
     * switch cycle
     */
    /*public void addPoint(RDPoint point){
         int pBegin = (prevFirst)?pointsPerCycle:0;
         oldpoints[pBegin+addedPoints] = point;
         addedPoints++;
         if (addedPoints == pointsPerCycle) prevFirst = !prevFirst;
     }*/

    // flag -- true  - oldpoints, false -- nowpoints

    /*public void addPoint(Collector.Point point, int idx){
    	if (lastAngel > point.angle) {
    		flag = !flag;
    	}
    	if (!flag) {
    		oldpoints[idx].range = point.range;
    		oldpoints[idx].distance = point.distance;
    		System.out.println("******Range " + oldpoints[idx].range);
    		System.out.println("******distance " + oldpoints[idx].distance);
    		System.out.println("******Angel " + point.angle/100);
    }else{
    nowpoints[idx].range = point.range;
    		nowpoints[idx].distance = point.distance;
    }
    	lastAngel = point.angle;
    }*/
    public void addPoint(int angle, int lastAngle, int range, double distance) {
      int indx = (angle - vparams.minAngle);
      if (vparams.step > 0.01) {
        indx *= vparams.step;
      }
      if (lastAngle > angle) {
        vcpoints.nowpoints[indx].range = range;
        vcpoints.nowpoints[indx].distance = distance;
      } else {
        vcpoints.oldpoints[indx].range = range;
        vcpoints.oldpoints[indx].distance = distance;
      }
    }

  } //End VolumeCalcPoints

  //properties
  /**
   * Determines count of points stored in last list. Set when object constructed
   * only.
   */
  private int lastListLength = 0;
  /** Volume of air before store content */
  private double volume;
  protected Logger logger = Logger.getLogger("svolume.Processor");
  private AvgType avgType = AvgType.takeMidle;
  private final StoreParams sparams;
  private int avgCount = 100;
  List<Collector.Point> lastList = Collections.synchronizedList(new LinkedList<Collector.Point>());
  List<Collector.Point> averageList =
      Collections.synchronizedList(new LinkedList<Collector.Point>());

  private final VolumeParams vparams;
  private final VolumeCalcPoints vcpoints;

  //public methods

  /** Clear all buffers and states */
  public void clear() {
    averageList.clear();
    lastList.clear();
    volume = 0;
  }

  //---------------------------------------------

  /** Collector exec this method when point available */
  public void processPoint(/*Point point*/Collector.Point point) {
    //test is angle useful for us
    if (point.angle < vparams.minAngle || point.angle > vparams.maxAngle) {
      logger.info("Point skipped, because not in min-max angle interval.");
      return;
    }

    System.out.println("******Range " + point.range);
    System.out.println("******distance " + point.distance);
    System.out.println("******Angel " + point.angle / 100);

    //int idx = (int) ((point.angle - vparams.getMinAngle()) / vparams.getStep());
    int idx = (point.angle - vparams.minAngle);
    if (vparams.step > 0.01) {
      idx *= vparams.step;
    }
    // vcpoints.addPoint(point, idx);

    if (lastList.size() > lastListLength) ((Queue) lastList).remove();
    lastList.add(point);

    //calc volume
    //int idx = (int) (point.angle / vparams.getStep());
    // if(idx - point.angle / vparams.getStep()>=0.5) idx++;

    double S1, S2, S;
    boolean f = true;
    if (f && flag) { // сам алгоритм расчета либо один  либо другой
      double R1 = 0.5 * (vcpoints.getNow(idx - 1).range + vcpoints.getOld(idx - 1).range);
      double R2 = 0.5 * (vcpoints.getNow(idx).range + vcpoints.getOld(idx).range);
      double Rmin, Rmax;
      if (R1 > R2) {
        Rmax = R1;
        Rmin = R2;
      } else {
        Rmax = R2;
        Rmin = R1;
      }

      S = 0.5 * Rmin * Rmax * vparams.stepSin;
      /*
      S1=0.5*Rmin*Rmin*vparams.getStepSin()*vparams.getStepCos();
      S2=0.5*Rmin*vparams.getStepSin()*(Rmax-Rmin*vparams.getStepCos());
      S=S1+Math.abs(S2);*/

    } else {
      S1 = vcpoints.getOld(idx - 1).range * vcpoints.getOld(idx).range * vparams.stepSin / 2.;
      S2 = vcpoints.getNow(idx - 1).range * vcpoints.getOld(idx).range * vparams.stepSin / 2.;
      S = (S1 + S2) / 2.;
    }

    double Lt = 0.5 * (vcpoints.getNow(idx - 1).distance + vcpoints.getNow(idx).distance);
    double Lt0 = 0.5 * (vcpoints.getOld(idx - 1).distance + vcpoints.getOld(idx).distance);
    double vol = (Lt0 - Lt) * S;

    System.out.println("VOLLL " + vol);

    /* if(idx>0){
     	
     	
     	
         //vcpoints.addPoint(new RDPoint(point.range, point.distance));
         double R1 = 0.5 * (vcpoints.get(idx-1).range + vcpoints.getPrev(idx-1).range);
         double R2 = 0.5 * (vcpoints.get(idx).range + vcpoints.getPrev(idx).range);
         double Rmin, Rmax;
         if(R1>R2) {
             Rmax = R1;
             Rmin = R2;
         } else {
             Rmax = R2;
             Rmin = R1;
         }
         double S1 = 0.5 * Rmin * Rmin * vparams.getStepSin() * vparams.getStepCos();
         double S2 = 0.5 * Rmin * vparams.getStepSin() * (Rmax-Rmin*vparams.getStepCos());
         double Lt = 0.5 * (vcpoints.get(idx-1).distance + vcpoints.get(idx).distance);
         double Lt0 = 0.5 * (vcpoints.getPrev(idx-1).distance + vcpoints.getPrev(idx).distance);
         volume += Math.abs( (Lt - Lt0) * (S2 + S2) );*/
    // }
    //call average list values
    /*   switch(avgType){
           case takeMidle:
               if(lastList.size()>avgCount && lastList.size()%avgCount == 0){
                   averageList.add(lastList.get(lastList.size()-avgCount/2));
               }
               break;             
           
           case avg:
               if(lastList.size()>avgCount && lastList.size()%avgCount == 0){
                   Collector.Point sumPoint = new Collector.Point(0,0,(short)0,(short)0);
                   Collector.Point bufPoint;
                   Iterator<Collector.Point> i = lastList.listIterator(lastList.size()-avgCount);
                   while(i.hasNext()){
                       bufPoint = i.next();
                       sumPoint.distance += bufPoint.distance;
                       sumPoint.range += bufPoint.range;
                       sumPoint.signal += bufPoint.signal;
                       sumPoint.angle += bufPoint.angle;
                   }
                   sumPoint.distance /= avgCount;
                   sumPoint.range /= avgCount;
                   sumPoint.signal /= avgCount;
                   sumPoint.angle /= avgCount;
                   averageList.add(sumPoint);
               }
               
               break;
                   
       }*/
  }

  /**
   * @return unmodificable view of lastList
   */
  public List<Collector.Point> getLastList() {
    return Collections.unmodifiableList(lastList);
  }

  /**
   * @return unmodificable view of avgList
   */
  public List<Collector.Point> getAvgList() {
    return Collections.unmodifiableList(averageList);
  }

  /**
   * @return volume of store content
   */
  public double getVolume() {
    return sparams.volume - volume;
  }

  /**
   * Creates a new instance of Data Processor
   * 
   * @param lastListLength
   *          Length of last list in points
   * @param avgtype
   *          Average list filling mechanizm
   * @param avgcount
   *          Point count used to fill one average list point, must be less or
   *          equal then lastListLength
   */
  public Processor(int lastListLength, AvgType avgtype, int avgcount, StoreParams sp,
      VolumeParams vp) {
    if (avgcount > lastListLength) throw new IllegalArgumentException();
    this.lastListLength = lastListLength;
    this.avgCount = avgcount;
    this.avgType = avgtype;
    this.sparams = sp; // параметры скалада
    this.vparams = vp; // параметры датчика min, max Angel, range
    volume = 0;
    vcpoints = new VolumeCalcPoints(vp.pointsPerCycle);
  }

}
