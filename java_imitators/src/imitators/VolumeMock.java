/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VolumeMock {

  private final DatagramSocket SLM_socket;

  public VolumeMock(String SLM_IP, int slmport) throws IOException {
    SLM_socket = new DatagramSocket();
    SLM_socket.connect(InetAddress.getByName(SLM_IP), slmport);
  }

  String Recieve() throws IOException {
    byte[] buf = new byte[SLM.MAX_DATAGRAM_SIZE];
    DatagramPacket packet = new DatagramPacket(buf, SLM.MAX_DATAGRAM_SIZE);
    SLM_socket.receive(packet);
    String res = new String(buf);
    int LF_idx = Math.max(res.indexOf(Utils.NEWLINE), res.indexOf((char) 0));
    return res.substring(0, LF_idx);
  }

  void Send(String message) throws IOException {
    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length());
    SLM_socket.send(packet);
  }

}
