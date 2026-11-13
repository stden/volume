/*
 * VolumeParams.java
 *
 * (c) 2007 <<OKB Karat>> ltd.
 *
 */

package data;

/**
 * Represents volume calculating parameters
 */
public class VolumeParams {
    
    /** Points with angle less then minAngle will skip, degree*/
    public int minAngle = 9000; //0
    /** Points with angle greater then maxAngle will skip, degree*/
    public int maxAngle = 27000; //270
    /** Points with angle between steps will skip
     * 0.1 - no skip points
     * 0.5 - skip 4 points
     */
    public double step = 0.1;
    /** How much points not skipped per one cycle scan */
    public int pointsPerCycle = (int) (((maxAngle - minAngle)/100)/step)+1;
    
    public double stepSin = Math.sin ( Math.toRadians(step) );
    public double stepCos = Math.cos ( Math.toRadians(step) );
    
    
    /** Creates a new instance of VolumeParams */
    public VolumeParams(int minAngle, int maxAngle, double step) {
        if( (minAngle>=0) && (maxAngle>=0) && (maxAngle>minAngle) && (step>=0.1) && (step<=10)){
            this.minAngle = minAngle*100;
            this.maxAngle = maxAngle*100;
            this.step = step;
            this.pointsPerCycle = (int)(((maxAngle - minAngle)/step))+1;
            this.stepSin = Math.sin ( Math.toRadians(step) );
            this.stepCos = Math.cos ( Math.toRadians(step) );
        } else {
            throw new IllegalArgumentException();
        }
    }
    
  /*  public int getMinAngle() {
        return minAngle;
    };
    
    public int getMaxAngle() {
        return maxAngle;
    };
    
    public double getStep() {
        return step;
    };
    
    public int getPointsPerCycle() {
        return pointsPerCycle;
    };
    
    public double getStepSin(){
        return stepSin;
    }

    public double getStepCos(){
        return stepCos;
    }
*/
}
