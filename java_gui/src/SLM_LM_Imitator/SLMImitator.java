/*
 * SLMImitator.java
 *
 * Created on 14 Ноябрь 2007 г., 14:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SLM_LM_Imitator;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Imitates SLM data sending by UDP. Circle points returned
 */
public class SLMImitator implements Runnable{
    final double ANGLE_STEP = 0.1;
    final double MAX_ANGLE = 10;//360;
    final double MIN_ANGLE = 0;
    final double MAX_R = 324;
    final double MAX_S = 4000;
    final short MAX_PACKET_SIZE = 1450;
    final short POINT_SIZE=8;
    double currentAngle = 0;
    
    volatile boolean stop = false;   
    
    int udpPort = 30;
    byte[] destIP = {127,0,0,1};
    
    public void stopThread(){
        stop = true;
    }
       
    /** Creates a new instance of SLMImitator */
    public SLMImitator() {
    }
    
    /** returns two-bytes from char*/
    byte[] splitChar(char ch){
        byte[] result = {0,0};
        result[0] = (byte)( (ch >> 8) & 0xFF );
        result[1] = (byte) (ch & 0xFF);
        return result;
    }
    
    /** forms one point and return eight-byte array in format $rrssaa<lf>*/
    byte[] generatePoint(){
        byte[] result = new byte[8];
        byte[] buf = {0,0};
        
        result[0] = '$';
        //range (circle emulation)
        buf = splitChar((char) MAX_R);
        result[1] = buf[0];
        result[2] = buf[1];
        
        //strench (random)
        buf = splitChar((char)(Math.random()*MAX_S));
        result[3] = buf[0];
        result[4] = buf[1];
        
        //angle
        buf = splitChar((char)(currentAngle*100));
        result[5] = buf[0];
        result[6] = buf[1];
        
        //<lf>
        result[7] = 0x0A;
        
        currentAngle += 0.1;
        if(currentAngle>=MAX_ANGLE) 
            currentAngle = MIN_ANGLE;
       
        return result;
    }
    
    /** collects generate points data in one buffer not greater then MAX_PACKET_SIZE*/
    byte[] generatePacketBuf() throws InterruptedException{
        byte packetBuf[] = new byte[(MAX_PACKET_SIZE/POINT_SIZE)*POINT_SIZE];
        byte pointBuf[] = new byte[POINT_SIZE];
        for(int i=0; i<MAX_PACKET_SIZE / POINT_SIZE;i++){
            pointBuf = generatePoint();
            Thread.sleep(110); //emulate 9 Hz point generation
            if(stop) break;
            for(int j=0;j<POINT_SIZE;j++)
                packetBuf[i*POINT_SIZE+j] = pointBuf[j];
        }
        return packetBuf;
    }
    
    void debugPrintPoint(byte[] buf){
        for(int i=0;i<buf.length;i++)
            System.out.print(buf[i]+0+" ");
        System.out.println();
    }
    
        
    /** imitator thread*/
    public void run(){
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            
            while(!stop){                   
                byte[] buf;
                try {
                    buf =  generatePacketBuf();
                    //debugPrintPoint(buf);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByAddress(destIP), udpPort);
                    udpSocket.send(packet);
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                    stop = true;
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                    stop = true;                   
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }              
        } catch (SocketException ex) {
            ex.printStackTrace();
            stop = true;
        }
            
    }
    
    public static void main(String[] args) throws IOException {
        SLMImitator slmi = new SLMImitator();
        new Thread(slmi).start(); //slm imitator thread creation
        while(System.in.available()==0);
        slmi.stopThread();
    }
}
    
