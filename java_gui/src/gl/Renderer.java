package gl;

import java.awt.Point;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

public class Renderer implements GLEventListener {
  private static final GLU glu = new GLU();
  private GLUquadric quadric;
  private final ArcBall arcBall = new ArcBall(640.0f, 480.0f);

  private final Matrix4f LastRot = new Matrix4f();
  private final Matrix4f ThisRot = new Matrix4f();
  private final Object matrixLock = new Object();
  private final float[] matrix = new float[16];

  private final double ANGLE = 60;
  private final int WIDTH = 1024;
  private final int HEIGHT = 768;
  private final double koef = 1; // metr
  private float zoom;

  // private Collection<Collector.Point> lastList;
  // private Collector.Point point;
  private final Collection<PointImitator.Pointd> lastList;
  private PointImitator.Pointd point;
  private double x, y, z;

  private boolean zoomIn, zoomOut;

  /*
   * public Renderer(Collection<Collector.Point> lastList){ this.lastList =
   * lastList; }
   */

  public Renderer(Collection<PointImitator.Pointd> lastList) {
    this.lastList = lastList;
  }

  void reset() {
    synchronized (matrixLock) {
      LastRot.setIdentity();
      ThisRot.setIdentity();
    }
  }

  public void startDrag(Point MousePt) {
    synchronized (matrixLock) {
      LastRot.set(ThisRot);
    }
    arcBall.click(MousePt);
  }

  public void drag(Point MousePt) {
    Quat ThisQuat = new Quat();

    arcBall.drag(MousePt, ThisQuat);
    synchronized (matrixLock) {
      ThisRot.setRotation(ThisQuat);
      ThisRot.mul(ThisRot, LastRot);
    }
  }

  public void zoomIn() {
    zoom += 1.0f;
  }

  public void zoomOut() {
    zoom -= 1.0f;
  }

  private void drawAxis(GL gl, double len, double mesh, double rad) {
    int i, j;
    /**
     * Drawing axis
     */
    gl.glPushMatrix();
    gl.glBegin(GL.GL_LINES);
    gl.glColor3f(1.0f, 0.0f, 0.0f);
    gl.glVertex3d(0, 0, 0);
    gl.glVertex3d(len, 0, 0);
    gl.glColor3f(0.0f, 1.0f, 0.0f);
    gl.glVertex3d(0, 0, 0);
    gl.glVertex3d(0, len, 0);
    gl.glColor3f(0.0f, 0.0f, 1.0f);
    gl.glVertex3d(0, 0, 0);
    gl.glVertex3d(0, 0, len);
    gl.glEnd();
    /**
     * Drawing sphere on axis
     */
    if (rad > 0 && mesh > 0) {
      quadric = glu.gluNewQuadric();
      glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
      glu.gluQuadricNormals(quadric, GLU.GLU_NONE);
      for (j = 0; j < 3; j++) {
        gl.glPushMatrix();
        switch (j) {
          case 0:
            gl.glColor3f(1.0f, 0.0f, 0.0f);
            for (i = 0; i < len / mesh; i++) {
              gl.glTranslated(mesh, 0, 0);
              glu.gluSphere(quadric, rad, 16, 16);
            }
            break;
          case 1:
            gl.glColor3f(0.0f, 1.0f, 0.0f);
            for (i = 0; i < len / mesh; i++) {
              gl.glTranslated(0, mesh, 0);
              glu.gluSphere(quadric, rad, 16, 16);
            }
            break;
          case 2:
            gl.glColor3f(0.0f, 0.0f, 1.0f);
            for (i = 0; i < len / mesh; i++) {
              gl.glTranslated(0, 0, mesh);
              glu.gluSphere(quadric, rad, 16, 16);
            }
            break;
        }

        gl.glPopMatrix();
      }
      glu.gluDeleteQuadric(quadric);
    }
    gl.glPopMatrix();
  }

  public void display(GLAutoDrawable gLDrawable) {
    synchronized (matrixLock) {
      ThisRot.get(matrix);
    }

    final GL gl = gLDrawable.getGL();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    gl.glLoadIdentity();
    gl.glTranslatef(-5.0f, 0.0f, zoom);

    gl.glPushMatrix();
    gl.glMultMatrixf(matrix, 0);
    drawAxis(gl, 5., 1., 0.05);
    gl.glPopMatrix();

    gl.glPushMatrix();
    gl.glMultMatrixf(matrix, 0);
    // Iterator<Collector.Point> iter = lastList.iterator();
    Iterator<PointImitator.Pointd> iter = lastList.iterator();
    while (iter.hasNext()) {
      point = iter.next();
      x = point.range * Math.cos(Math.toRadians(point.angle));
      y = point.range * Math.sin(Math.toRadians(point.angle));

      z = point.distance;

      gl.glPointSize(1.0f);
      gl.glBegin(GL.GL_POINTS);
      gl.glColor3f(1.0f, 1.0f, 0.0f);
      gl.glVertex3d(x, y, z);
      gl.glEnd();

    }
    gl.glPopMatrix();

  }

  public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
    init(gLDrawable);
  }

  public void init(GLAutoDrawable gLDrawable) {
    final GL gl = gLDrawable.getGL();
    gl.glShadeModel(GL.GL_SMOOTH);
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LEQUAL);
    gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST);

    LastRot.setIdentity(); // Reset Rotation
    ThisRot.setIdentity(); // Reset Rotation
    ThisRot.get(matrix);
    zoom = -20.0f;
    x = 0.0;

    gl.glLoadIdentity();
    glu.gluPerspective(ANGLE / WIDTH * HEIGHT, (float) WIDTH / (float) HEIGHT, 0.001, 1000.0);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glEnable(GL.GL_BLEND);// включает прозрачность
    gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_DST_COLOR);// функция
    // работы
    // прозрачности
    gl.glEnable(GL.GL_LIGHT0);
    // gl.glEnable(GL.GL_LIGHTING);
    gl.glEnable(GL.GL_NORMALIZE);// для освещенности
    gl.glEnable(GL.GL_COLOR_MATERIAL);// тоже для
    // освещенности
  }

  public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
    final GL gl = gLDrawable.getGL();

    height = (height == 0) ? 1 : height;

    final float h = (float) width / (float) height;
    gl.glViewport(0, 0, width, height); // Reset The Current
    // Viewport
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluPerspective(45.0f, h, 0.001, 1000.0);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();

    arcBall.setBounds(width, height);
  }

}
