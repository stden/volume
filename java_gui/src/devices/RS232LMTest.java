/*
 * RS232LMTest.java
 * 
 * (c) 2007 <<OKB Karat>> ltd.* 
 */

package devices;

import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;

public class RS232LMTest {
    RS232LM lm;
    Logger logger = Logger.getLogger(RS232LMTest.class.getName());
    
    RS232LMTest(){
        try {
            lm = new RS232LM();
            lm.connect("/dev/tty_dgrp_ps_0", "/dev/tty_dgrp_ps_1");
            logger.info("Start lm");
            lm.start();
            //lm.updateBatteryVoltage();
            while(System.in.available() <= 0){
                while(lm.pointsAvailable() > 0){
                    System.out.println("point: "+lm.getPoint(3)/*+Integer.toString(lm.getPoint().distance)*/);
                    //System.out.println("voltage: "+Double.toString(lm.getBatteryVoltage()));
                }
                Thread.sleep(10);
            }
            System.out.println("voltage: "+Double.toString(lm.getBatteryVoltage()));
            logger.info("Stop lm");
            lm.stop();
        } catch (InterruptedException ex) {
            Logger.getLogger(RS232LMTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPortException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (PortInUseException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (TooManyListenersException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (UnsupportedCommOperationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }        
    }
    
    public static void main(String[] args){
        RS232LMTest test = new RS232LMTest();  
        System.exit(0);
    }
}
