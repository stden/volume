/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.util.logging.Logger;

abstract public class Imitator extends SmartTimer {
  public static <T extends Imitator> T Start(T imitator) throws Exception {
    imitator.thread = new Thread(imitator); // create separate thread
    imitator.thread.start(); // start thread
    return imitator;
  }

  Logger logger = Logger.getLogger(SLM.class.getName());
  xProperties properties;
  Thread thread;

  @Override
  public void run() {
    super.run();
    unbind();
  }

  public abstract void unbind();
}