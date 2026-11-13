package devices;

import java.io.IOException;
import java.net.*;
import java.util.logging.*;

public class SLMTest {

  SLM slm;
  Logger logger = Logger.getLogger(SLMTest.class.getName());

  SLMTest() throws InterruptedException, SLM.WrongState, IOException {
    try {
      // исправитть!!!!!!!
      // slm = new SLM(lm);
      new Thread(slm).start();
      slm.connect(InetAddress.getByName("127.0.0.1"), 30); // 3095
      while (slm.getState() != SLM.State.idle) {
        Thread.sleep(10);
      }
      slm.setRedDot(false);
      while (slm.getState() != SLM.State.idle) {
        Thread.sleep(10);
      }
      slm.start();
      while (slm.getState() != SLM.State.scaning) {
        Thread.sleep(10);
      }
      // slm.stop();
      SLMPoint buf;
      while (System.in.available() <= 0) {
        while (slm.pointsAvailable() != 0) {
          buf = slm.getPoint();
          System.out.format("SLMPoint angel " + buf.angle + ", range " + buf.range + ", signal "
              + buf.signal + "\n");
          // System.out.format("SLMPoint r "+ buf.angle+", s "+ buf.range+", a
          // "+ buf.signal+"\n");
        }
        Thread.sleep(100);
      }
      slm.stop();
      while (slm.getState() != SLM.State.idle) {
        Thread.sleep(10);
      }
    } catch (UnknownHostException ex) {
      Logger.getLogger(SLMTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SocketException ex) {
      Logger.getLogger(SLMTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public static void main(String[] args) {
    try {
      new SLMTest();
      System.exit(0);
    } catch (InterruptedException ex) {
      Logger.getLogger(SLMTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SLM.WrongState ex) {
      Logger.getLogger(SLMTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(SLMTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
