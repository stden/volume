/*
 * LMImitator.java
 *
 * Created on 16 Ноябрь 2007 г., 15:39
 *
 * (c) 2007 <<OKB Karat>> ltd.
 *
 */

package SLM_LM_Imitator;

import java.io.IOException;
import java.util.ArrayList;
import devices.LMPoint;

/**
 * Imitates LM works inside and generate points with 9 Hz frenzy
 */
public class LMObjImitator implements Runnable{
    final int MIN_DISTANCE = 0;
    final int MAX_DISTANCE = 1000;
    final int DISTANCE_STEP = 1;
    
    protected int distance = MIN_DISTANCE;
    protected boolean started = false;
    volatile protected boolean stopThread = false; 
    protected ArrayList<LMPoint> points = new ArrayList<LMPoint>(200);
    
    /** Creates a new instance of LMImitator */
    public LMObjImitator() {
    }

    /** Stops LMObjImitator thread */
    public void stopThread(){ 
        stopThread = true; 
    }
    
    /** Start data collecting */
    public void start(){
        points.clear();
        started = true;
    }
    
    /** Stop data collecting */
    public void stop(){
        started = false;
    }
    
    /** How much points available */
    public synchronized int pointsAvailable(){
        return points.size();
    }
    
    /** Get oldest point and delete them */
    public  synchronized LMPoint getPoint(){
        if(pointsAvailable()!=0)
            return points.remove(0);
        else
            return null;
    }

    /** Thread function. Gets point then sleep 1/9 of second.*/
    public void run(){
        while(!stopThread){
            if(started)
            {
                distance += DISTANCE_STEP;
                points.add(new LMPoint(distance));
                if(distance>=MAX_DISTANCE) distance = MAX_DISTANCE;
            }
            try {
                    Thread.sleep(110);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                stopThread = true;
            }
        }
    }
    
        /** simple imitator test*/
    public static void main(String[] args) throws IOException {
        LMObjImitator lmji = new LMObjImitator();
        new Thread(lmji).start(); //slm imitator thread creation
        
        lmji.start();
        while(System.in.available()==0)
        {
            for(int i=0;i<lmji.pointsAvailable();i++)
                System.out.println(lmji.getPoint().distance);
        }
        lmji.stopThread();
    }
 
}
