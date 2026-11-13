/**
 * http://www.progsystema.ru 
 */

package VolumeLaunch;

import java.util.EventObject;

import javax.swing.JOptionPane;

import org.jdesktop.application.Application.ExitListener;

public class ApplicationExitListener implements ExitListener {

  private final LaunchView launchView;

  public ApplicationExitListener(LaunchView launchView) {
    this.launchView = launchView;
  }

  @Override
  public boolean canExit(EventObject arg0) {
    return JOptionPane.showConfirmDialog(null, "Exit now?", "Question",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
  }

  @Override
  public void willExit(EventObject arg0) {
    launchView.closeAll();
  }

}
