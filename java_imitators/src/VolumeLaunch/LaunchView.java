/**
 * http://www.progsystema.ru 
 */

package VolumeLaunch;

import imitators.IM;
import imitators.Imitator;
import imitators.SLM;
import imitators.Utils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * The application's main frame.
 */
public class LaunchView extends FrameView implements IM.IListener, SLM.IListener {
  public void ShowError(Exception ex){
        JOptionPane.showMessageDialog(this.mainPanel, ex.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);        
        ex.printStackTrace();
  }
  
  private class StartAllTask extends org.jdesktop.application.Task<Object, Void> {

    private final Color DARK_GREEN = new Color(0, 100, 0);
    private final LaunchView view;

    StartAllTask(org.jdesktop.application.Application app, LaunchView view) {
      super(app);
      this.view = view;
      try {
        view.im = Imitator.Start(new IM());
        view.slm = Imitator.Start(new SLM());
        IM_OutputFilename.setText(im.outputFileName);
        im.listeners.add(view);
        slm.listener = view;
        String volumeExe = InstallPath.getText() + "\\volume.exe";
        VolumeExe.setText(volumeExe);
        if (StartVolumeExe.isSelected()) {
          view.volumeProcess =
              Runtime.getRuntime().exec(new String[]{ volumeExe }, null, new File(InstallPath.getText()));
          VolumeStatus.setText("Running...");
          VolumeStatus.setForeground(DARK_GREEN);
        } else {
          volumeProcess = null;
          stopAllSignal = false;
        }
        view.refreshControlsState();
      } catch (Exception ex) {
        ShowError(ex);
        volumeProcess = null;
      }
    }

    @Override
    protected Object doInBackground() {
      try {
        if (volumeProcess == null)
          while (!view.stopAllSignal)
            Thread.sleep(400);
        else {
          volumeProcess.waitFor();
          VolumeStatus.setText("Closed");
          VolumeStatus.setForeground(Color.RED);
        }
      } catch (InterruptedException ex) {
        ShowError(ex);
      }
      return null;
    }

    @Override
    protected void succeeded(Object result) {
      // Runs on the EDT. Update the GUI based on
      // the result computed by doInBackground().
      if (im != null) im.terminateSignal = true;
      im = null;
      if (slm != null) slm.terminateSignal = true;
      slm = null;
      refreshControlsState();
    }
  }

  private int busyIconIndex = 0;
  private final Icon[] busyIcons = new Icon[15];

  private final Timer busyIconTimer;

  private final Icon idleIcon;
  IM im = null;

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel IM_Distance;
  private javax.swing.JLabel IM_Header;
  private javax.swing.JLabel IM_OutputFilename;
  private javax.swing.JLabel IM_OutputFilenameLabel;
  private javax.swing.JPanel IM_Panel;
  private javax.swing.JLabel IM_SendedPoints;
  private javax.swing.JLabel InstallPath;
  private javax.swing.JLabel InstallPathLabel;
  private javax.swing.JLabel SLM_Angle;
  private javax.swing.JLabel SLM_Distance;
  private javax.swing.JLabel SLM_Header;
  private javax.swing.JLabel SLM_NumberOfPackets;
  private javax.swing.JPanel SLM_Panel;
  private javax.swing.JButton StartAllButton;
  private javax.swing.JButton StartStopMovingButton;
  private javax.swing.JCheckBox StartVolumeExe;
  private javax.swing.JButton StopAllButton;
  private javax.swing.JLabel VolumeExe;
  private javax.swing.JLabel VolumeStatus;
  private javax.swing.JLabel Volume_Header;
  private javax.swing.JPanel Volume_Panel;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JLabel statusAnimationLabel;
  private javax.swing.JLabel statusMessageLabel;
  private javax.swing.JPanel statusPanel;
  // End of variables declaration//GEN-END:variables
  private final Timer messageTimer;
  SLM slm = null;
  private LaunchView.StartAllTask startAllTask;
  public boolean stopAllSignal;
  private Process volumeProcess;
  
  public LaunchView(SingleFrameApplication app) {
    super(app);

    initComponents();

    // status bar initialization - message timeout, idle icon and busy
    // animation, etc
    ResourceMap resourceMap = getResourceMap();
    int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
    messageTimer = new Timer(messageTimeout, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        statusMessageLabel.setText("");
      }
    });
    messageTimer.setRepeats(false);
    int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
    for (int i = 0; i < busyIcons.length; i++)
      busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
    busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
        statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
      }
    });
    idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
    statusAnimationLabel.setIcon(idleIcon);
    progressBar.setVisible(false);

    // connecting action tasks to status bar via TaskMonitor
    TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
    taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if ("started".equals(propertyName)) {
          if (!busyIconTimer.isRunning()) {
            statusAnimationLabel.setIcon(busyIcons[0]);
            busyIconIndex = 0;
            busyIconTimer.start();
          }
          progressBar.setVisible(true);
          progressBar.setIndeterminate(true);
        } else if ("done".equals(propertyName)) {
          busyIconTimer.stop();
          statusAnimationLabel.setIcon(idleIcon);
          progressBar.setVisible(false);
          progressBar.setValue(0);
        } else if ("message".equals(propertyName)) {
          String text = (String) evt.getNewValue();
          statusMessageLabel.setText(text == null ? "" : text);
          messageTimer.restart();
        } else if ("progress".equals(propertyName)) {
          int value = (Integer) evt.getNewValue();
          progressBar.setVisible(true);
          progressBar.setIndeterminate(false);
          progressBar.setValue(value);
        }
      }
    });

    InstallPath.setText(Utils.getInstallPath());
    refreshControlsState();

    app.addExitListener(new ApplicationExitListener(this));
  }

  public void close() {

  }

  void closeAll() {
    if (volumeProcess != null) {
      volumeProcess.destroy();
      volumeProcess = null;
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mainPanel = new javax.swing.JPanel();
    StopAllButton = new javax.swing.JButton();
    StartAllButton = new javax.swing.JButton();
    InstallPath = new javax.swing.JLabel();
    InstallPathLabel = new javax.swing.JLabel();
    IM_Panel = new javax.swing.JPanel();
    IM_SendedPoints = new javax.swing.JLabel();
    IM_Header = new javax.swing.JLabel();
    IM_Distance = new javax.swing.JLabel();
    StartStopMovingButton = new javax.swing.JButton();
    IM_OutputFilename = new javax.swing.JLabel();
    IM_OutputFilenameLabel = new javax.swing.JLabel();
    SLM_Panel = new javax.swing.JPanel();
    SLM_NumberOfPackets = new javax.swing.JLabel();
    SLM_Header = new javax.swing.JLabel();
    SLM_Distance = new javax.swing.JLabel();
    SLM_Angle = new javax.swing.JLabel();
    Volume_Panel = new javax.swing.JPanel();
    VolumeExe = new javax.swing.JLabel();
    VolumeStatus = new javax.swing.JLabel();
    Volume_Header = new javax.swing.JLabel();
    StartVolumeExe = new javax.swing.JCheckBox();
    statusPanel = new javax.swing.JPanel();
    javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
    statusMessageLabel = new javax.swing.JLabel();
    statusAnimationLabel = new javax.swing.JLabel();
    progressBar = new javax.swing.JProgressBar();

    mainPanel.setName("mainPanel"); // NOI18N
    mainPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
      public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
      }
      public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
      }
      public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
        mainPanelAncestorRemoved(evt);
      }
    });

    javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(VolumeLaunch.LaunchApplication.class).getContext().getActionMap(LaunchView.class, this);
    StopAllButton.setAction(actionMap.get("StopAll")); // NOI18N
    StopAllButton.setName("StopAllButton"); // NOI18N

    StartAllButton.setAction(actionMap.get("StartAll")); // NOI18N
    StartAllButton.setName("StartAllButton"); // NOI18N

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(VolumeLaunch.LaunchApplication.class).getContext().getResourceMap(LaunchView.class);
    InstallPath.setText(resourceMap.getString("InstallPath.text")); // NOI18N
    InstallPath.setName("InstallPath"); // NOI18N
    InstallPath.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        openLink(evt);
      }
    });

    InstallPathLabel.setFont(resourceMap.getFont("InstallPathLabel.font")); // NOI18N
    InstallPathLabel.setText(resourceMap.getString("InstallPathLabel.text")); // NOI18N
    InstallPathLabel.setName("InstallPathLabel"); // NOI18N

    IM_Panel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, resourceMap.getColor("IM_Panel.border.matteColor"))); // NOI18N
    IM_Panel.setName("IM_Panel"); // NOI18N

    IM_SendedPoints.setText(resourceMap.getString("IM_SendedPoints.text")); // NOI18N
    IM_SendedPoints.setName("IM_SendedPoints"); // NOI18N

    IM_Header.setFont(resourceMap.getFont("IM_Header.font")); // NOI18N
    IM_Header.setText(resourceMap.getString("IM_Header.text")); // NOI18N
    IM_Header.setName("IM_Header"); // NOI18N

    IM_Distance.setText(resourceMap.getString("IM_Distance.text")); // NOI18N
    IM_Distance.setName("IM_Distance"); // NOI18N

    StartStopMovingButton.setAction(actionMap.get("StartStop_LM")); // NOI18N
    StartStopMovingButton.setName("StartStopMovingButton"); // NOI18N

    IM_OutputFilename.setText(resourceMap.getString("IM_OutputFilename.text")); // NOI18N
    IM_OutputFilename.setName("IM_OutputFilename"); // NOI18N
    IM_OutputFilename.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseDragged(java.awt.event.MouseEvent evt) {
        IM_OutputFilenameMouseDragged(evt);
      }
    });

    IM_OutputFilenameLabel.setText(resourceMap.getString("IM_OutputFilenameLabel.text")); // NOI18N
    IM_OutputFilenameLabel.setName("IM_OutputFilenameLabel"); // NOI18N

    javax.swing.GroupLayout IM_PanelLayout = new javax.swing.GroupLayout(IM_Panel);
    IM_Panel.setLayout(IM_PanelLayout);
    IM_PanelLayout.setHorizontalGroup(
      IM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(IM_PanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(IM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(IM_PanelLayout.createSequentialGroup()
            .addGroup(IM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addGroup(IM_PanelLayout.createSequentialGroup()
                .addComponent(IM_SendedPoints, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(IM_Distance, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addComponent(IM_Header, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(StartStopMovingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(IM_PanelLayout.createSequentialGroup()
            .addComponent(IM_OutputFilenameLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(IM_OutputFilename, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGap(127, 127, 127))
    );
    IM_PanelLayout.setVerticalGroup(
      IM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(IM_PanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(IM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(IM_PanelLayout.createSequentialGroup()
            .addComponent(IM_Header)
            .addGap(6, 6, 6)
            .addGroup(IM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(IM_SendedPoints)
              .addComponent(IM_Distance)))
          .addComponent(StartStopMovingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(IM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(IM_OutputFilenameLabel)
          .addComponent(IM_OutputFilename))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    SLM_Panel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, resourceMap.getColor("SLM_Panel.border.matteColor"))); // NOI18N
    SLM_Panel.setName("SLM_Panel"); // NOI18N

    SLM_NumberOfPackets.setText(resourceMap.getString("SLM_NumberOfPackets.text")); // NOI18N
    SLM_NumberOfPackets.setMaximumSize(new java.awt.Dimension(120, 14));
    SLM_NumberOfPackets.setName("SLM_NumberOfPackets"); // NOI18N

    SLM_Header.setFont(resourceMap.getFont("SLM_Header.font")); // NOI18N
    SLM_Header.setText(resourceMap.getString("SLM_Header.text")); // NOI18N
    SLM_Header.setName("SLM_Header"); // NOI18N

    SLM_Distance.setText(resourceMap.getString("SLM_Distance.text")); // NOI18N
    SLM_Distance.setName("SLM_Distance"); // NOI18N

    SLM_Angle.setText(resourceMap.getString("SLM_Angle.text")); // NOI18N
    SLM_Angle.setName("SLM_Angle"); // NOI18N

    javax.swing.GroupLayout SLM_PanelLayout = new javax.swing.GroupLayout(SLM_Panel);
    SLM_Panel.setLayout(SLM_PanelLayout);
    SLM_PanelLayout.setHorizontalGroup(
      SLM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(SLM_PanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(SLM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(SLM_Header)
          .addComponent(SLM_NumberOfPackets, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(SLM_Distance, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(SLM_Angle, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    SLM_PanelLayout.setVerticalGroup(
      SLM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(SLM_PanelLayout.createSequentialGroup()
        .addComponent(SLM_Header)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(SLM_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(SLM_NumberOfPackets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(SLM_Distance)
          .addComponent(SLM_Angle))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    Volume_Panel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(51, 102, 255)));
    Volume_Panel.setName("Volume_Panel"); // NOI18N

    VolumeExe.setText(resourceMap.getString("VolumeExe.text")); // NOI18N
    VolumeExe.setMaximumSize(new java.awt.Dimension(560, 14));
    VolumeExe.setName("VolumeExe"); // NOI18N

    VolumeStatus.setText(resourceMap.getString("VolumeStatus.text")); // NOI18N
    VolumeStatus.setName("VolumeStatus"); // NOI18N

    Volume_Header.setFont(resourceMap.getFont("Volume_Header.font")); // NOI18N
    Volume_Header.setText(resourceMap.getString("Volume_Header.text")); // NOI18N
    Volume_Header.setName("Volume_Header"); // NOI18N

    javax.swing.GroupLayout Volume_PanelLayout = new javax.swing.GroupLayout(Volume_Panel);
    Volume_Panel.setLayout(Volume_PanelLayout);
    Volume_PanelLayout.setHorizontalGroup(
      Volume_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(Volume_PanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(Volume_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(Volume_PanelLayout.createSequentialGroup()
            .addComponent(VolumeExe, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(VolumeStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(Volume_Header, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(238, Short.MAX_VALUE))
    );
    Volume_PanelLayout.setVerticalGroup(
      Volume_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Volume_PanelLayout.createSequentialGroup()
        .addComponent(Volume_Header)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(Volume_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(VolumeStatus)
          .addComponent(VolumeExe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );

    VolumeStatus.getAccessibleContext().setAccessibleName(resourceMap.getString("VolumeStatus.AccessibleContext.accessibleName")); // NOI18N

    StartVolumeExe.setText(resourceMap.getString("StartVolumeExe.text")); // NOI18N
    StartVolumeExe.setName("StartVolumeExe"); // NOI18N

    javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
    mainPanel.setLayout(mainPanelLayout);
    mainPanelLayout.setHorizontalGroup(
      mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(mainPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(mainPanelLayout.createSequentialGroup()
            .addComponent(StartVolumeExe)
            .addContainerGap())
          .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
              .addComponent(InstallPathLabel)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(InstallPath, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
              .addGap(60, 60, 60)
              .addComponent(StartAllButton)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(StopAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addGap(116, 116, 116))
            .addGroup(mainPanelLayout.createSequentialGroup()
              .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(IM_Panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                .addComponent(SLM_Panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                .addComponent(Volume_Panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 566, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addContainerGap(12, Short.MAX_VALUE)))))
    );
    mainPanelLayout.setVerticalGroup(
      mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(mainPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(InstallPathLabel)
          .addComponent(InstallPath)
          .addComponent(StartAllButton)
          .addComponent(StopAllButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(StartVolumeExe)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
        .addComponent(IM_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(SLM_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(Volume_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    InstallPath.getAccessibleContext().setAccessibleName(resourceMap.getString("InstallPath.AccessibleContext.accessibleName")); // NOI18N

    statusPanel.setName("statusPanel"); // NOI18N

    statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

    statusMessageLabel.setName("statusMessageLabel"); // NOI18N

    statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

    progressBar.setName("progressBar"); // NOI18N

    javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
    statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(
      statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
      .addGroup(statusPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(statusMessageLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 418, Short.MAX_VALUE)
        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusAnimationLabel)
        .addContainerGap())
    );
    statusPanelLayout.setVerticalGroup(
      statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(statusPanelLayout.createSequentialGroup()
        .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(statusMessageLabel)
          .addComponent(statusAnimationLabel)
          .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(3, 3, 3))
    );

    setComponent(mainPanel);
    setStatusBar(statusPanel);
  }// </editor-fold>//GEN-END:initComponents

  private void IM_OutputFilenameMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IM_OutputFilenameMouseDragged
    // TODO add your handling code here:
  }//GEN-LAST:event_IM_OutputFilenameMouseDragged

  private void mainPanelAncestorRemoved(javax.swing.event.AncestorEvent evt) {// GEN-FIRST:event_mainPanelAncestorRemoved
  }// GEN-LAST:event_mainPanelAncestorRemoved

  private void openLink(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_openLink
  }// GEN-LAST:event_openLink

  public void refreshControlsState() {
    StartAllButton.setEnabled(im == null);
    StopAllButton.setEnabled(im != null);
    StartStopMovingButton.setEnabled(im != null);
    if (im != null) StartStopMovingButton.setText(im.moving ? "Stop moving" : "Start moving");
  }

  public void sendDistance(String distance, IM im) {
    IM_Distance.setText("Distance: " + distance);
    IM_SendedPoints.setText("Sended points: " + im.stepsCounter);
  }

  @Action
  public Task<Object, Void> StartAll() {
    startAllTask = new StartAllTask(getApplication(), this);
    return startAllTask;
  }

  @Action
  public void StartStop_LM() {
    im.moving = !im.moving;
    refreshControlsState();
  }

  @Action
  public void StopAll() {
    if (volumeProcess == null)
      stopAllSignal = true;
    else
      volumeProcess.destroy();
  }

  @Override
  public void update_SLM(SLM slm) {
    SLM_NumberOfPackets.setText("Number of packet: " + slm.SendedPackets);
    SLM_Distance.setText("Distance: " + slm.distance);
    SLM_Angle.setText("Angle: " + slm.angle / 100.0);
  }
}
