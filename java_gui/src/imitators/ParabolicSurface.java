/**
 * imitators/ParabolicSurface.java
 * (c) 2008 RTI Systems 
 */

package imitators;

/**
 * Imitates surface as set of 0..10 parabolic functions
 * with walls and floor.
 */
public class ParabolicSurface extends Surface{
    private double pfkoefs[][];
    private double leftwall = -1;
    private double rightwall = -1;
    private double floor = -1;
    
    /**
     * Return minimal of two R for N parabolic function
     */
    private double calcMinRN(int n, double distance, double angle){
        double tana = Math.tan(angle);
        double d2 = Math.pow(pfkoefs[n][1] - Math.tan(tana), 2) - 2*pfkoefs[n][0]*pfkoefs[n][2]*distance;
        if(d2<0) return -1;
        double d = Math.sqrt(d2);
        double x = Math.min(Math.abs((tana-pfkoefs[n][1]+d/2/pfkoefs[n][0])), Math.abs((tana-pfkoefs[n][1]-d/2/pfkoefs[n][0])));
        return Math.sqrt(1+tana*tana) * x;
    }
    
    ParabolicSurface(double leftwall, double rightwall, double floor, double pfkoefs[][]){
        this.leftwall = leftwall;
        this.rightwall = rightwall;
        this.floor = floor;
        this.pfkoefs = pfkoefs;
    }
    
    
    @Override
    public double getRange(double distance, double angle) {
        
        //test parabolic functions
        double minr = 1000000;
        double rbuf;
        for(int i=0;i<pfkoefs.length;i++){
            rbuf = calcMinRN(i, distance, angle);
            if( (rbuf!=-1) && (rbuf<minr)  ){
                minr = rbuf;
            }
        }
        
        //test walls and floor
        double buf = Math.sqrt(1+Math.tan(angle));
        if(leftwall!=-1) minr = Math.min(minr, leftwall*buf);
        if(rightwall!=-1) minr = Math.min(minr, rightwall*buf);
        if(floor!=-1) minr = Math.min(minr, floor*buf);
        
        return minr;
    }
}
