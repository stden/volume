package gui;

import getProp.*;
import gl.Drawer;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.swing.JPanel;

import data.*;
import devices.*;

public class Manager {

  private Gui gui;
  private SLM slm;
  // private RS232LM lm;
  private LM lm;
  private Processor prc;
  private Collector collector;
  private Settings settings;
  private VolumeParams vp;
  private StoreParams sp;
  private final Drawer drawer;
  private final JPanel jRangeViewPanel;
  private final JPanel jAboveViewPanel;
  private final JPanel j3DViewPanel;
  Logger logger = Logger.getLogger(Manager.class.getName());

  public Manager(Gui gui, Settings settings) {
    jRangeViewPanel = gui.getRangePanel();
    jAboveViewPanel = gui.getAbovePanel();
    j3DViewPanel = gui.get3DPanel();
    drawer = new Drawer(jRangeViewPanel, jAboveViewPanel, j3DViewPanel);
    try {
      this.gui = gui;
      this.settings = settings;

      String PROPERTIES_FILE = Utils.PROPERTIES_DIR + "svolume.properties";
      xProperties properties = new xProperties(PROPERTIES_FILE);

      String NameFileImitLM = null;
      properties.getString("NameFileImitLM", NameFileImitLM);
      // "E:/dudko/FinishC++SLM/Finish_SLM_JAVA_C++/java_imitators/lmpoints.txt"
      lm = new LM(0, properties.getString("NameFileImitLM", NameFileImitLM));
      // System.out.println(lm.avgDistanse);
      new Thread(lm, "Thread_lm").start();

      vp = new VolumeParams(settings.getMinAngle(), settings.getMaxAngle(), settings.getAngStep()); // установка
      // min/maxangle
      // и
      // step
      sp = settings.getStore(); // параметры склада
      gui.setStoreHouse(sp.volume);
      gui.setDistance(lm.avgDistanse);
      slm = new SLM(lm, vp, sp, settings);
      new Thread(slm, "Thread_slm").start();
      // slm.connect(InetAddress.getByName("127.0.0.1"), 30);
      slm.connect(settings.getSlmIp(), settings.getLmPort());

    } catch (SocketException ex) {
      // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null,
      // ex);
      // JOptionPane.showMessageDialog(gui,ex,"Error",JOptionPane.OK_OPTION);
    } catch (SLM.WrongState ex) {
      // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null,
      // ex);
      // JOptionPane.showMessageDialog(gui,ex,"Error",JOptionPane.OK_OPTION);
    } catch (IOException ex) {
      // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null,
      // ex);
      // JOptionPane.showMessageDialog(gui,ex,"Error",JOptionPane.OK_OPTION);
    } catch (Exception ex) {
      // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null,
      // ex);
      // JOptionPane.showMessageDialog(gui,ex,"Error",JOptionPane.OK_OPTION);
    }
  }

  public void startDevice() {
    // drawer.startGL();
    try {
      System.out.println("–аботаю в потоке" + Thread.currentThread());
      slm.setRedDot(false);
      while (slm.getState() != SLM.State.idle) {
        Thread.sleep(10);
      }
      slm.start();
      while (slm.getState() != SLM.State.scaning) {
        Thread.sleep(10);
      }
      SLMPoint buf;
      while (System.in.available() <= 0) {
        while (slm.pointsAvailable() != 0) {
          buf = slm.getPoint();
          // System.out.format("--SLMPoint angle "+ buf.angle+", range
          // "+ buf.range+", signal "+ buf.signal+"\n");
          // System.out.format("SLMPoint r "+ buf.angle+", s "+
          // buf.range+", a "+ buf.signal+"\n");
        }
        gui.setDistance(lm.avgDistanse);
        gui.setRange((double) (slm.range) / 100);
        gui.setAngle((double) (slm.angle) / 100);
        gui.setElemVolume(sp.ElVolume);
        gui.setFreeSpace(sp.FreeVolume);
        gui.setVolume(sp.volume);
        gui.setMissedAngle(slm.missingAngle);
        Thread.sleep(100);
      }
      /*
       * lm.start(); gui.setCharge(lm.getBatteryVoltage());
       * while(System.in.available() <= 0){ while(lm.pointsAvailable() >
       * 0){ System.out.println("point:
       * "+Integer.toString((int)lm.avgDistanse));
       * //lm.getPoint().distance gui.setDistance((int)lm.avgDistanse); //
       * lm.getPoint().distance //System.out.println("voltage:
       * "+Double.toString(lm.getBatteryVoltage())); } Thread.sleep(10); }
       */
      // System.out.println("voltage:
      // "+Double.toString(lm.getBatteryVoltage()));
      // collector.start();
    } catch (SLM.WrongState ex) {
      // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null,
      // ex);
      // JOptionPane.showMessageDialog(gui,"Exception on
      // start","Error",JOptionPane.OK_OPTION);
    } catch (Exception ex) {
      // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null,
      // ex);
      // JOptionPane.showMessageDialog(gui,"Other ex
      // "+ex,"Error",JOptionPane.OK_OPTION);
    }
  }

  public void stopDevice() {
    // drawer.stopGL();

    (new Thread(new Runnable() {
      @Override
      public void run() {
        logger.info("Wait for SLM.State.idle...");
        try {
          slm.stop();
          while (slm.getState() != SLM.State.idle) {
            Thread.sleep(10);
          }
          // logger.info("Stop lm");
          // lm.stop();
          // collector.stop();
        } catch (SLM.WrongState ex) {
          // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
          // Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null,
          // ex);
        }
        logger.info("End of waiting SLM.State.idle!");
      }
    })).start();
  }

  public void resizeGLPanel() {
    java.awt.Dimension rangeSize = jRangeViewPanel.getSize();
    java.awt.Dimension aboveSize = jAboveViewPanel.getSize();
    java.awt.Dimension dSize = j3DViewPanel.getSize();
    drawer.resizeGLPanel(rangeSize, aboveSize, dSize);
  }

  public void getCharge() {}

  public void getDistance() {

  }

  public void getVolume() {

  }

  public boolean isConnect() {
    return slm.isConnected();
  }
}
