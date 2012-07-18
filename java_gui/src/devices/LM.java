package devices;

import java.io.*;
import java.util.logging.*;

public class LM extends AbstractLM implements Runnable {

  protected String path = "";
  private final int MAX_DISTANCE = 9999999;
  private final Logger logger = Logger.getLogger("svolume.LM");

  // if dist = 0 then read from file
  public LM(int dist, String string) {
    path = string;
    BufferedReader in = null;
    if (dist == 0) {
      try {
        // System.out.println("Abs path "+(new File(path).getAbsolutePath()));
        in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String data;
        if (in.ready()) {
          data = in.readLine();
          double distance = Double.parseDouble(data.substring(0, data.length() - 1));
          for (int i = 0; i < DistantListSize; i++) {
            DistanceList[i] = new LMPoint((int) (distance * 100));
          }
          avgDistanse = CurrentAvgDist = distance;
        }
      } catch (IOException e) {
        logger.log(Level.WARNING, "Read Error, file finished");
      } catch (NumberFormatException ex) {
        logger.log(Level.WARNING, "Wrong distance value!!!");
      }
    } else {
      for (int i = 0; i < DistantListSize; i++) {
        DistanceList[i] = new LMPoint((dist * 100));
      }
      avgDistanse = CurrentAvgDist = dist;
    }
  }

  public void avgDist() {
    int summ = 0;
    for (int i = 0; i < DistantListSize; i++) {
      summ += DistanceList[i].distance;
    }
    avgDistanse = summ / ((double) (DistantListSize) * 100);
  }

  public void readData() {
    BufferedReader in = null;
    int summ = 0;
    try {
      in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
      String data;
      int count = 0;
      while (true) {
        while (in.ready()) {
          data = in.readLine();
          double distance = Double.parseDouble(data.substring(0, data.length() - 1));
          if (((int) distance * 100) == MAX_DISTANCE) {
            distance = 0; // no-hit
          }
          DistanceList[count].distance = (int) (distance * 100);
          CurrentAvgDist = distance;
          if (count == DistantListSize - 1) {
            count = 0;
          } else {
            count++;
          }
          // avgDist();
          summ = 0;
          for (int i = 0; i < DistantListSize; i++) {
            summ += DistanceList[i].distance;
          }
          avgDistanse = summ / ((double) (DistantListSize) * 100);

          /*
           * try { Thread.sleep(100); } catch (InterruptedException e) { // TODO
           * Auto-generated catch block e.printStackTrace(); }
           */

        }
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Read Error, file finished");
    } catch (NumberFormatException ex) {
      logger.log(Level.WARNING, "Wrong distance value!!!");
    }
  }

  public void run() {
    readData();
  }

}
