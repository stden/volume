/**
 * imitators/ConstSurface.java
 * (c) 2008 RTI Systems 
 */

package imitators;

/**
 * Emulates tube with constant radius
 */
public class ConstSurface extends Surface{
    private double r = 0.;
    
    public ConstSurface(double r){
        this.r  = r;
    }
        
    @Override
    public double getRange(double distance, double angle){
        return r;
    }

}
