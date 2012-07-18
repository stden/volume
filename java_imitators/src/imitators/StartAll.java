/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.net.BindException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartAll {
  public static void main(String[] args) throws Exception {
    try {
      IM im = Imitator.Start(new IM());
      im.listeners.add(new IM_DebugToConsole(im.properties));
      Imitator.Start(new SLM());
      System.out.println("Press 'Enter' to start/stop moving.");
      im.moving = true;
      while (true) {
        System.in.read(); // wait for Enter key pressed
        System.in.skip(System.in.available());
        im.moving = !im.moving;
        System.out.println(im.moving ? "Started." : "Stopped.");
      }
    } catch (BindException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Can't bind port. Check rights.", ex);
    } catch (SocketException ex) {
      Logger.getLogger(SLM.class.getName()).log(Level.SEVERE, "Socket Exception", ex);
    }
  }
}
