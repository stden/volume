/**
 * imitators/SLM.java
 * (c) 2008 RTI Systems 
 */

package imitators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.io.FileInputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/* 
 * TODO:
 *  1. Use state pattern
 *  2. BINARY/ASCII answers list
 *  3. imitate signal level
 */

/**
 * Imitates work of SLM MDA072B
 * Note: Does't imitate signal level, onyl range
 */
public class SLM implements Runnable{
    
    // *** consts ***
    final String PROPERTIES_FILE = "E:\\Dudko\\SLM_java\\src\\imitators\\slm_im.properties";   
    
    final byte DATA_BEGIN =            '$'; // Data point begin symbol
    final byte NEWLINE =               0x0A; // Command/point end symbol
    final byte LMNEWLINE =             0x0A; //lm points delimeter
    //final byte CMD_NONE =              0;    //No cmd answer waited
    final byte CMD_DEVICE_IDENTIFY =   '?'; // Causes unit to return simple string
    final byte CMD_ENABLE_FIRING =     'A'; // Enables firing of laser, when hrad rotated
    final byte CMD_DISABLE_FIRING =    'B'; // disables firing of laser
    final byte CMD_CURRENT_ANGLE =     'C'; // Current angle of the laser (or eye safety register when followed Cx)    
    final byte CMD_CONFIG =            'F'; // Selects angle increment & more
    final byte CMD_DO_REPLIES =        'H'; // Toggles output of replies to commands
    final byte CMD_SELECT_SPEED =      'I'; // Selects speed of rotation 
    final byte CMD_VER_DATE =          'J'; // Outputs version and date of software
    final byte CMD_ORIGIN =            'O'; // Sets/displays reference zero for encoder
    final byte CMD_POINTER_ON =        'P'; // Turns pointer on
    final byte CMD_POINTER_OFF =       'Q'; // Turns pointer off
    final byte CMD_START_SCAN =        'S'; // Starts motor
    final byte CMD_STOP_SCAN =         'T'; // Stops motor
    final byte CMD_NETWORK =           'V'; // Configures network parameters
    final byte CMD_ASCII =             'W'; // Scan data output in ASCII
    final byte CMD_BINARY =            'X'; // Scan data output in binary
    final byte CMD_READ_STATUS =       'Y'; // SLM outputs status information
    final byte CMD_RESET_MAC =         'Z'; // Set MAC address
    final byte CMD_DO_ANGLES =         'U'; // Togles output of r,s,a or angles only
    final byte CMD_RANGE =             'G'; // Show current range and signal strange
    
    final short MAX_SPEED = 10;
    final short MIN_SPEED = 0;
    final int MAX_RANGE = 99999;
    final int MAX_ANGLE = 36000;
    final int MAX_SIGNAL = 9999;

    final byte BINARY_POINT_SIZE = 8; //for binary mode $rrssaa<lf>
    final byte ASCII_POINT_SIZE = 18; //for ascii mode $rrrrr,ssss,aaaaa<lf>
    
    public final int MAX_DATAGRAM_SIZE = 1500 - 42; //MTU - (Ethernet header + ip header + udp header)
    
    
    // *** properties ***
    //receive commands to slm
    private DatagramSocket slmsocket;
    private InetAddress clientAddress;
    private int clientPort;
    //receive lm points (for surface generate synchronization)
    private DatagramSocket lmsocket;
    private Properties properties;
    private Logger logger = Logger.getLogger(SLM.class.getName());
        
    //device parameters
    private byte speed = 1;
    private boolean onlyAngles = false;
    private boolean binaryMode = false;
    private boolean replies = false;
    private boolean hundr = false;
    private boolean dot = false;
    private boolean firing = false;
    private boolean scaning = false;
    private int origin = 31075;
    private int angle = 0;
    private int range = 0;
    private int signal = 0;
    
    //imitation parameters
    private double distance = 0;
    Surface surface = null;
    /** last points generation time, nanoseconds */
    private long lastGenTime = 0;
    /** nanoseconds count between point generation */
    private long nsecPerPoint = 0;
    /** points buffer (in ascii or binary fmt), created when scan started */
    private byte[] pointsBuf = null;
    /** last filled buffer position, when it greater pointsBuf size, buffer will sended */
    private int lastBufPos = -1;
    
    private Random zpcRand;
    private Random errorRand;
    
    /* accidental error of range parameters */
    private double errorMean = 0;
    private double errorVariance = 1;
    private double zeroPerCircle = 3;
        
    // *** private methods ***
    private Properties getDefaultProperties(){
        Properties prop = new Properties();
        prop.setProperty("slmport", "30");
        prop.setProperty("lmip", "127.0.0.1");
        prop.setProperty("lmport", "3095");
        prop.setProperty("errorMean", "0");
        prop.setProperty("errorVariance", "1");
        prop.setProperty("zeroPerCircle","3");    
        prop.setProperty("surface","const");    
        prop.setProperty("surface.leftwall","-1");
        prop.setProperty("surface.rightwall","-1");
        prop.setProperty("surface.floor","-1");
        prop.setProperty("surface.const.r","1.");            
        
        prop.setProperty("surface.parabolic.n", "1");
        prop.setProperty("surface.parabolic.a1", "1.");
        prop.setProperty("surface.parabolic.b1", "1.");
        prop.setProperty("surface.parabolic.c1", "1.");
        
        return prop;
    }
            
    // *** public ***
    
    /** creates slm imitator instance*/
    public SLM() throws SocketException, BindException{
        properties = new Properties(getDefaultProperties());
        try {
        	properties.load(new FileInputStream(PROPERTIES_FILE)); 
        } catch (FileNotFoundException ex) {
            logger.info("Properties file not found");
        } catch (IOException ex) {
            logger.warning("Properties file read error");
        }

        //bind slmport for command receiving
        slmsocket = new DatagramSocket(Integer.parseInt(properties.getProperty("slmport")));
        //set socket "almost unblocked" FIXME find other decision
        slmsocket.setSoTimeout(1); 
        
        //connects to lm
        try{
            lmsocket = new DatagramSocket();        
            //set socket "almost unblocked" FIXME find other decision
            lmsocket.setSoTimeout(1); 
            
            lmsocket.connect(InetAddress.getByName(properties.getProperty("lmip")), Integer.parseInt(properties.getProperty("lmport")));
            //send command to lm imitator, it adds us to points receivers
            String hello = "123456"; //"points";
            DatagramPacket packet = new DatagramPacket(hello.getBytes(), hello.length());
            lmsocket.send(packet);
        } catch (UnknownHostException ex) {
            logger.warning("LM host unknown. Works without.");
            lmsocket = null;
        } catch (IOException ex) {
            logger.log(Level.WARNING, "IOError occured. Works without LM imitator.", ex);
            lmsocket = null;
        }
            
        
        errorMean = Double.parseDouble(properties.getProperty("errorMean"));
        errorVariance = Double.parseDouble(properties.getProperty("errorVariance"));
        zeroPerCircle = Double.parseDouble(properties.getProperty("zeroPerCircle"));
        zpcRand = new Random();
        errorRand = new Random();
        
        if( properties.getProperty("surface").equals("parabolic") ){
            logger.info("parabolic surface");
            
            int pfcount = Integer.parseInt(properties.getProperty("surface.parabolic.n"));
            double pfkoefs[][] = new double[pfcount][3];
            
            for(int i=0;i<pfkoefs.length;i++){
                pfkoefs[i][0] = Double.parseDouble(properties.getProperty(String.format("surface.parabolic.a%d", i+1)));
                pfkoefs[i][1] = Double.parseDouble(properties.getProperty(String.format("surface.parabolic.b%d", i+1)));
                pfkoefs[i][2] = Double.parseDouble(properties.getProperty(String.format("surface.parabolic.c%d", i+1)));
            }
            
            surface = new ParabolicSurface(
                    Double.parseDouble(properties.getProperty("surface.leftwall")),                    
                    Double.parseDouble(properties.getProperty("surface.rightwall")),
                    Double.parseDouble(properties.getProperty("surface.floor")),
                    pfkoefs
                    );
        } else if( properties.getProperty("surface").equals("const") ) { 
            logger.info("const surface");
            surface = new ConstSurface(
                        Double.parseDouble(properties.getProperty("surface.const.r"))
                    );
        } else {
            logger.warning("unknown surface");
            surface = null;
        }
    }
        
    //** reads and process command packet */
    public void readPacket(){
            byte[] buf = new byte[MAX_DATAGRAM_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, MAX_DATAGRAM_SIZE);
        
        try {
            slmsocket.receive(packet);
            
            //save first packet sender ip and port (if not connected) for send data
            if (clientAddress == null){
                clientAddress = packet.getAddress();
                clientPort = packet.getPort();
            //change port if packet sended from saved ip
            } else if(clientAddress.equals(packet.getAddress()) && (clientPort!=packet.getPort())){
                clientPort = packet.getPort();
            }
            
            //process buffer, find line end and give command before to processCmd
            String strbuf = new String(buf);
            int lfidx = strbuf.indexOf(NEWLINE);
            if(lfidx == -1){
                logger.log(Level.WARNING,"In input buffer <lf> not found, skipped. Buffer: {0}",strbuf);
            } else if(lfidx == 0) {
                logger.log(Level.WARNING,"In input buffer <lf> is first symbol, skipped. Buffer: {0}",strbuf);
            } else {
                processCommand(new String(buf).substring(0, lfidx));
            }                
        } catch (SocketTimeoutException ex) {
            //do nothing
        } catch (IOException ex) {
            logger.log(Level.WARNING, "IOException occured.", ex);
        }
        
    }

    //** reads and process packet from lm (00000.00m<lf>)*/
    private void readLMPacket() {
        byte[] buf = new byte[MAX_DATAGRAM_SIZE];
        DatagramPacket packet = new DatagramPacket(buf,MAX_DATAGRAM_SIZE);
        try{
            lmsocket.receive(packet);
            
            distance = Double.parseDouble(new String(buf).replaceAll("[^0-9.]",""));
        } catch (PortUnreachableException ex) {
            logger.warning("LM imitator unreachable");
            lmsocket = null;
        } catch (SocketTimeoutException ex) {
            //do nothing
        } catch (IOException ex) {
            logger.warning("IOException occured.");
        }            
    }
    
    /** Sends answer to client 
     * @param answer Answer string
     */
    private void sendAnswer(String answer){
        logger.fine("OUT: "+answer);
        try {
            DatagramPacket packet = new DatagramPacket(answer.getBytes(), answer.length(), clientAddress, clientPort);
            slmsocket.send(packet);
        } catch (PortUnreachableException ex) {
            logger.warning("Port unreachable.");
        } catch (IOException ex) {
            logger.warning("IOException occured.");
        }
    }
           
            
    //** Process cmd and run needed function */
    private void processCommand(String cmd){
        cmd = cmd.toUpperCase();
        logger.fine("IN: "+cmd);
        switch(cmd.charAt(0)){
            case CMD_ASCII:
                if(!(firing&&scaning)) cmdAscii();
                break;
            case CMD_DEVICE_IDENTIFY:
                cmdDeviceIdentify();
                break;              
            case CMD_BINARY:
                if(!(firing&&scaning)) cmdBinary();
                break;
            case CMD_CURRENT_ANGLE:
                cmdCurrentAngle();
                break;
            case CMD_DO_REPLIES:
                cmdDoReplies();
                break;
            case CMD_VER_DATE:
                cmdVerDate();
                break;
            case CMD_DO_ANGLES:
                cmdDoAngles();
                break;
            case CMD_CONFIG:
                if(cmd.length()>=2){
                    cmdConfig(cmd.substring(1));
                } else {
                    logger.warning("Config without param. Skipped.");
                }
                break;
            case CMD_POINTER_ON:
                cmdPointer(true);
                break;
            case CMD_POINTER_OFF:
                cmdPointer(false);
                break;
            case CMD_ORIGIN:
                cmdOrigin();
                break;
            case CMD_SELECT_SPEED:
                if(cmd.length()>=2){
                    try{
                        cmdSelectSpeed(Integer.valueOf(cmd.substring(1)));
                    } catch (NumberFormatException ex) {
                        logger.warning("Select speed wrong param. Skipped.");
                    }
                } else {
                    logger.warning("Select speed without param. Skipped.");
                }
                break;
            case CMD_RANGE:
                cmdRange();
                break;
            case CMD_READ_STATUS:
                cmdReadStatus();
                break;
            case CMD_ENABLE_FIRING:
                cmdFiring(true);
                break;
            case CMD_DISABLE_FIRING:
                cmdFiring(false);
                break;
            case CMD_START_SCAN:
                cmdScan(true);
                break;
            case CMD_STOP_SCAN:
                cmdScan(false);
                break;

            default:
                logger.log(Level.WARNING, "Unknown command: {0}",cmd.charAt(0));
        }
    }
    
    /** Switches to ASCII mode */
    private void cmdAscii(){
        binaryMode = false;
        if(replies) sendAnswer("ASCII OUTPUT ENABLED");
    }

    /** Switches to BINARY mode */
    private void cmdBinary(){
        binaryMode = true;
        if(replies) sendAnswer("*BINARY");
    }
    
    /** Send device id*/
    private void cmdDeviceIdentify(){
        sendAnswer((binaryMode)?"*MDA072":"MDA072B");
    }

    private void cmdCurrentAngle() {
        if(binaryMode){
            sendAnswer(String.format("*A%05d", angle));
        } else {
            sendAnswer(String.format("Angle- %5d",angle));
        }
    }

    private void cmdDoAngles() {
        onlyAngles = !onlyAngles;
        // Same answer in BINARY and ASCII mode
        if(replies) sendAnswer((onlyAngles)?"ANGLES ENABLED":"ANGLES DISABLED");
    }

    private void cmdDoReplies() {
        replies = !replies;
        if(replies) sendAnswer((binaryMode)?"*REPLON":"REPLIES ON");
    }

    private void cmdVerDate() {
        // Same answer in BINARY and ASCII mode
        sendAnswer("v3.1d - Oct 12 2007");
    }
    
    private void cmdPointer(boolean on){
        dot = on;
        
        if(replies){
            if(on){
                sendAnswer((binaryMode)?"*DotOn ":"Red Dot on");
            } else {
                sendAnswer((binaryMode)?"*DotOff":"Red Dot off");
            }
        }
    }

    /** Change angle accuracy */
    private void cmdConfig(String option) {
        if(option.charAt(0) == 'T'){
            hundr = false;
            if(replies) sendAnswer((binaryMode)?"*Tenth":"1/10th selected");
        } else if (option.charAt(0) == 'H'){
            //If speed != 1, imitator do not set hundr and not send answer
            //TODO: determine what do device, in that case
            if(speed == 1){
                hundr = true;
                if(replies) sendAnswer((binaryMode)?"*Hundr":"1/100th selected");
            } else {
                logger.warning("Not select 1/100th, becose speed not equal to one.");
            }
        } else if (option.charAt(0) == 'E') { 
            //i don't now what is that (in doc. nothing about). Added for compartibility with london software
            //FIXME: actualize actions
            if(replies) sendAnswer(String.format(((binaryMode)?"*F%05d":"eye safety set to %5d"), Integer.parseInt(option.substring(1))));
        } else {
            logger.log(Level.WARNING,"Unknown config option: {0}. Skipped.",option);
        }
    }

    /** Send current origin */
    private void cmdOrigin() {
        sendAnswer(String.format((binaryMode)?"*O%05d":"Origin = %5d",origin));
    }

    /** Change scan speed */
    private void cmdSelectSpeed(int speed) {
        if(speed>=MIN_SPEED && speed<=MAX_SPEED){
            this.speed = (byte) speed;
        } else {
            logger.log(Level.WARNING, "Wrong speed: {0}, set to 1",speed);
            this.speed = 1;
        }
        if(replies) sendAnswer(String.format((binaryMode)?"*D%05d":"Duty cycle set to %2d", this.speed));
    }
   
    private void cmdRange(){
        sendAnswer(String.format((binaryMode)?"*R%5d\n*S%05d":"Range= %5d\nSigstren= %5d",range,signal));
    }
    
    private void cmdReadStatus(){
        if(binaryMode){
            //FIXME i'dont know what all this mean
            sendAnswer("*FLGS940070d1\n*A4294935566\n*WRD000\n*Binary");                   
        } else {
            //FIXME works partially, realize all functionality
            sendAnswer(String.format("FIFO empty\n" +
                    "Enable low\n" +
                    "Zero not found\n" +
                    "Red dot %s\n" +
                    "1/100th. %s\n" +
                    "Not up to speed\n" +
                    "Angle - %05d\n" +
                    "Words left = 000\n" +
                    "ASCII mode",(dot)?"on":"off",(hundr)?"on":"off",angle));
        }
    }
    
    /** Enables / disables firing */
    private void cmdFiring(boolean enable){
        firing = enable;
        scanOrFiringChange();
        if(replies){
            if(enable){
                sendAnswer((binaryMode)?"*Lasron":"Laser on");
            } else {
                sendAnswer((binaryMode)?"*Lasoff":"Laser off");
            }
        }
    }
    
    /** 
     * Enables / disables motor rotating 
     * When rotating angles increments responds to speed.
     * When rotating and firing sends data packets with getted points
     */
    private void cmdScan(boolean start){
        scaning = start;
        scanOrFiringChange();
        if(replies){
            if(start){
                sendAnswer((binaryMode)?"*STARTD":"Scan started");
            } else {
                sendAnswer((binaryMode)?"*STOPED":"Scan stopped");
            }
        }        
    }
   
    /** Determines begin or end of point collection 
     * If collection ended, buffer sends and empty
     * If collection started, pointBuf (ascii or binary) created
     */
    private void scanOrFiringChange(){
        if((firing && scaning)&&(pointsBuf == null)){
            //scan starting
            int pointsPerPacket = MAX_DATAGRAM_SIZE/((binaryMode)?BINARY_POINT_SIZE:ASCII_POINT_SIZE);
            pointsBuf = new byte[pointsPerPacket*((binaryMode)?BINARY_POINT_SIZE:ASCII_POINT_SIZE)];
            lastBufPos = -1;
        } else if(!(firing && scaning) && (pointsBuf != null)){
            //scan stopping
            if(lastBufPos != -1) sendPoints();
            pointsBuf = null;
        }
    }
    
    /**
     * Send points from pointBuffer
     */
    private void sendPoints(){
        try{
            DatagramPacket packet = new DatagramPacket(pointsBuf, lastBufPos+1, clientAddress, clientPort);
            slmsocket.send(packet);
            lastBufPos = -1;
        } catch (PortUnreachableException ex) {
            logger.warning("Port unreachable.");
        } catch (IOException ex) {
            logger.warning("IOException occured.");
        }        
    }

    /** calcs how much points must be generated in specified time
     * 
     * @param time Time in nanoseconds
     * @return Point count.
     */
    private int pointsPerTime(long time){
        //FIXME we generate more points then needed, becose use ceil
        return (int) Math.ceil( 
                    360./(double) speed 
                    * ((hundr)?0.01:0.1)
                );
    }

    /**
     * Put points to pointBuf starting at lastBufPos+1 in ASCII or BINARY format
     * and increments lastBufPos
     */
    private void putPointToBuf(int range, int signal, int angle){
        range = range%(MAX_RANGE+1);
        signal = signal%(MAX_SIGNAL+1);
        angle = angle%(MAX_ANGLE+1);
        logger.finest(String.format("Save point r: %d, s: %d, a:%d", range, signal, angle));
        if(binaryMode){
            pointsBuf[lastBufPos+1] = DATA_BEGIN;
            
            pointsBuf[lastBufPos+2] = (byte) ((range & 0xFF00)>>8);
            pointsBuf[lastBufPos+3] = (byte) (range & 0x00FF);            

            pointsBuf[lastBufPos+4] = (byte) ((signal & 0xFF00)>>8);
            pointsBuf[lastBufPos+5] = (byte) (signal & 0x00FF);            
            
            pointsBuf[lastBufPos+6] = (byte) ((angle & 0xFF00)>>8);
            pointsBuf[lastBufPos+7] = (byte) (angle & 0x00FF);
            lastBufPos += BINARY_POINT_SIZE;
            
            pointsBuf[lastBufPos] = NEWLINE;
        } else {
            pointsBuf[lastBufPos+1] = DATA_BEGIN;
            
            String strbuf = String.format("%05d,%04d,%05d", range, signal, angle);
            byte[] buf = strbuf.getBytes();
            for(int i=0;i<ASCII_POINT_SIZE-2;i++)
            {
                pointsBuf[lastBufPos+2+i] = buf[i];
            }           
            lastBufPos += ASCII_POINT_SIZE;
            pointsBuf[lastBufPos] = NEWLINE;
        }
    }
    
    /** Determine current time and lastGenTime delta, generate point that device generate in that time
     *  and fill in pointsBuffer
     * Also this function increments angle.
     */
    private void generatePoints(){
        int pCount = pointsPerTime(lastGenTime - System.nanoTime());
        lastGenTime = System.nanoTime();
        for(int i=0;i<pCount;i++){
            //increment angle
            if((angle + ((hundr)?1:10)) > 36000) angle = 0;
            angle += (hundr)?1:10;
            //if firing collect point
            if(firing){
                //randomly return zero points
                boolean zeroPoint = zpcRand.nextGaussian() < ((double)zeroPerCircle/360/((hundr)?1:10));
                if(onlyAngles || (surface==null) || zeroPoint){
                    putPointToBuf(0,0,angle);
                } else {
                    double rangebuf = (surface.getRange(distance, Math.toRadians((double) angle/100.)) );
                    // accidental error calculation
                    rangebuf = 100.*rangebuf + errorMean + errorVariance*errorRand.nextGaussian();
                    putPointToBuf((int) (rangebuf), 0, angle);
                }
                if( lastBufPos >= (pointsBuf.length-1) ){
                    sendPoints();
                }
            }

        }
        
    }
    
    public void run(){
        while(true){
            readPacket();
            if(lmsocket!=null) readLMPacket();
            if(scaning) generatePoints();
        }
        
    }
    
    public static void main(String[] args){
        try {
            SLM slm = new SLM();
            Thread thrd = new Thread(slm);
            thrd.start();
            thrd.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Imitator thread interrupted.", ex);
        } catch (BindException ex) {
            Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Can't bind port. Check rights.",ex);
        } catch (SocketException ex) {
            Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Socket Exception", ex);
        }
    }
    
}
