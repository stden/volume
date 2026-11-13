/**
 * http://www.progsystema.ru 
 */

package VolumeLaunch;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/** The main class of the application */
public class LaunchApplication extends SingleFrameApplication {

  /**
   * A convenient static getter for the application instance.
   * 
   * @return the instance of LaunchApplication
   */
  public static LaunchApplication getApplication() {
    return Application.getInstance(LaunchApplication.class);
  }

  /** Main method launching the application */
  public static void main(String[] args) {
    launch(LaunchApplication.class, args);
  }

  /**
   * This method is to initialize the specified window by injecting resources.
   * Windows shown in our application come fully initialized from the GUI
   * builder, so this additional configuration is not needed.
   */
  @Override
  protected void configureWindow(java.awt.Window root) {}

  /** At startup create and show the main frame of the application */
  @Override
  protected void startup() {
    show(new LaunchView(this));
  }
}
