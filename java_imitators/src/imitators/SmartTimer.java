/**
 * http://www.progsystema.ru 
 */

package imitators;

public abstract class SmartTimer implements Runnable {

  protected long curTime;
  public double needFreq = 1; // in Hz
  protected long startTime = System.currentTimeMillis();
  public int stepsCounter = 0;
  public boolean terminateSignal = false;

  public double averageSpeed() {
    return stepsCounter * 1000.0 / curTime;
  }

  public long nextTickAt() {
    return (long) (1000.0 / needFreq * (stepsCounter + 1));
  }

  public abstract void oneStep();

  @Override
  public void run() {
    while (!terminateSignal) {
      oneStep();
      stepsCounter++;
      curTime = System.currentTimeMillis() - startTime;
      long pause = nextTickAt() - curTime;
      if (pause > 0) try {
        Thread.sleep(pause);
      } catch (InterruptedException e) {
        break;
      }
    }
  }

}