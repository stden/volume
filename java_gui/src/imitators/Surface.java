/**
 * imitators/Surface.java
 * (c) 2008 RTI Systems 
 */

package imitators;

/**
 * Abstract surface, used by imitators/SLM to imitate store surface.
 */
public abstract class Surface {     
    
    /**
     * Abstract method, must be implemented in subclasses.
     * Determine function of 2 arguments, that generate surface.
     * @param distance lm distance, m
     * @param angle angles, rad
     * @return range range, m
     */
    public abstract double getRange(double distance, double angle);            
}
