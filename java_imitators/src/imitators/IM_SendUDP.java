/**
 * http://www.progsystema.ru 
 */

package imitators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Logger;

public class IM_SendUDP implements IM.IListener {
  /** Sended by udp-clients if then wants to recieve points */
  final static String HELLO = "points";
  final LinkedList<InetSocketAddress> clients = new LinkedList<InetSocketAddress>();
  Logger logger = Logger.getLogger(SLM.class.getName());
  private DatagramSocket socket = null;
  private int udpport = 3095;

  public IM_SendUDP(xProperties properties) throws SocketException {
    udpport = properties.getInt("udpport", udpport);
    if (socket != null) close();
    // bind udpport for hello waiting
    socket = new DatagramSocket(udpport);

    (new Thread(new Runnable() {

      @Override
      public void run() {
        while (socket != null) {
          // test queires for points by udp
          byte[] buf = new byte[HELLO.length()];
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          logger.info("Waiting for SLM...");
          try {
            socket.receive(packet);
          } catch (IOException e) {
            break;
          }
          // hello string received, add sender to clients
          if (HELLO.equals(new String(buf))) {
            logger.info("UDPClient added");
            clients.add((InetSocketAddress) packet.getSocketAddress());
          }
        }
        logger.info("Closing...");
      }
    })).start();
  }

  @Override
  public void close() {
    socket.close();
    socket = null;
  }

  @Override
  /** Sends current distance to all clients from list */
  public void sendDistance(String distance, IM im) {
    ListIterator<InetSocketAddress> iterator = clients.listIterator();
    while (iterator.hasNext())
      try {
        DatagramPacket packet =
            new DatagramPacket(distance.getBytes(), distance.length(), iterator.next());
        socket.send(packet);
      } catch (PortUnreachableException ex) {
        logger.warning("Port unreachable. Remove client");
        iterator.remove();
      } catch (IOException ex) {
        logger.warning("IOException occured.");
      }
  }
}
