package gl;

import java.awt.Dimension;
import java.util.Collection;

import javax.media.opengl.GLJPanel;
import javax.swing.JPanel;

public class Drawer implements Runnable {
  private final JPanel jPanel1, jPanel2, jPanel3;
  private final GLJPanel glPanel1 = new GLJPanel();
  private final GLJPanel glPanel2 = new GLJPanel();
  private final GLJPanel glPanel3 = new GLJPanel();
  private boolean enable = false;
  private final PointImitator t;

  // private Collection<Collector.Point> list;
  Collection<PointImitator.Pointd> list;

  /** Creates a new instance of myThread */
  public Drawer(JPanel jPanel1, JPanel jPanel2, JPanel jPanel3) {
    t = new PointImitator();
    new Thread(t, "Thread_Draw...").start();

    this.jPanel1 = jPanel1;
    this.jPanel2 = jPanel2;
    this.jPanel3 = jPanel3;

  }

  public void startGL() {
    addMyPanel(jPanel1, glPanel1);
    list = t.getLastList();
    initGLPanel(glPanel1, list);

    /*
     * addMyPanel(jPanel3,glPanel3); list = t.getAList();
     * initGLPanel(glPanel3,list);
     */

    new Thread(this).start();
  }

  public void stopGL() {
    enable = false;
    jPanel1.remove(glPanel1);
    jPanel1.repaint();
    /*
     * jPanel3.remove(glPanel3); jPanel3.repaint();
     */
  }

  public void run() {
    while (enable) {
      glPanel1.display();
      // glPanel3.display();
    }
  }

  public void resizeGLPanel(Dimension size1, Dimension size2, Dimension size3) {
    glPanel1.setSize((size1.width - 14), (size1.height - 27));
    glPanel2.setSize((size2.width - 14), (size2.height - 27));
    glPanel3.setSize((size3.width - 14), (size3.height - 27));
  }

  private void addMyPanel(JPanel jPanel, GLJPanel glPanel) {
    jPanel.add(glPanel);
    Dimension size = jPanel.getSize();
    glPanel.setSize((size.width - 14), (size.height - 27));
    glPanel.setLocation(7, 17);
  }

  /*
   * private void initGLPanel(GLJPanel glPanel,Collection<Collector.Point>
   * list){ Renderer render = new Renderer(list); InputHandler inputHandler =
   * new InputHandler(render); glPanel.addGLEventListener(render);
   * glPanel.addMouseListener(inputHandler);
   * glPanel.addMouseMotionListener(inputHandler);
   * glPanel.addMouseWheelListener(inputHandler); enable = true; }
   */
  private void initGLPanel(GLJPanel glPanel, Collection<PointImitator.Pointd> list) {
    Renderer render = new Renderer(list);
    InputHandler inputHandler = new InputHandler(render);
    glPanel.addGLEventListener(render);
    glPanel.addMouseListener(inputHandler);
    glPanel.addMouseMotionListener(inputHandler);
    glPanel.addMouseWheelListener(inputHandler);
    enable = true;
  }
}
