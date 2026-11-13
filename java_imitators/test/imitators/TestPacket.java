package imitators;

import java.io.IOException;

public class TestPacket {

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    Imitator.Start(new IM());
    final SLM slm = new SLM();
    Imitator.Start(slm);
    (new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          VolumeMock vm = new VolumeMock("127.0.0.1", slm.slmport);
          vm.Recieve();

        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    })).start();
    System.in.read();
  }
}
