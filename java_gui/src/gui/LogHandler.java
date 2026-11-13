package gui;

import java.util.logging.*;

import javax.swing.JTextArea;

public class LogHandler extends StreamHandler {
  private static JTextArea jLogTextArea;

  @Override
  public void publish(LogRecord rec) {
    String msg = getFormatter().format(rec);
    jLogTextArea.append(msg + "\n");
  }

  public static void setArea(JTextArea jLogTextArea) {
    LogHandler.jLogTextArea = jLogTextArea;
  }
}
