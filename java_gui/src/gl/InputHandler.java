package gl;

import java.awt.event.*;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

class InputHandler extends MouseInputAdapter {
  private final Renderer renderer;

  public InputHandler(Renderer renderer) {
    this.renderer = renderer;
  }

  @Override
  public void mouseClicked(MouseEvent evt) {
    if (SwingUtilities.isRightMouseButton(evt)) {
      renderer.reset();
    }
  }

  @Override
  public void mousePressed(MouseEvent evt) {
    if (SwingUtilities.isLeftMouseButton(evt)) {
      renderer.startDrag(evt.getPoint());
    }
  }

  @Override
  public void mouseDragged(MouseEvent evt) {
    if (SwingUtilities.isLeftMouseButton(evt)) {
      renderer.drag(evt.getPoint());
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent evt) {
    if (evt.getWheelRotation() < 0) {
      renderer.zoomOut();
    } else if (evt.getWheelRotation() > 0) {
      renderer.zoomIn();
    }
  }

}
