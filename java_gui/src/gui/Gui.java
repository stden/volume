package gui;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

import javax.swing.*;

import data.StoreParams;

public class Gui extends javax.swing.JFrame {

  /** Creates new form Gui */
  public Gui() {
    initComponents();
    guiPointer = this;

    logger = Logger.getLogger(Gui.class.getName());
    LogHandler.setArea(this.jLogTextArea);

    File f = new File("settings.dat");
    if (f.exists()) {
      try {
        Settings s = Settings.read();
        restoreFields(s);
        settings = s;
        manager = new Manager(guiPointer, settings);
        logger.info("Settings read successfull");
      } catch (Exception ex) {
        logger.severe("Can't read settings file");
      }
    }
  }

  private double getDouble(JTextField textField) {
    return Double.valueOf(textField.getText());
  }

  private int getInteger(JTextField textField) {
    return Integer.decode(textField.getText());
  }

  private InetAddress getInetAddress(JTextField textField) throws UnknownHostException {
    return InetAddress.getByName(textField.getText());
  }

  private Settings.CalcModel getAvgType(JComboBox comboBox) {
    if (comboBox.getSelectedIndex() == 0)
      return Settings.CalcModel.avg;
    else
      return Settings.CalcModel.takeMidle;
  }

  private StoreParams getStore(JTextField LeftWall, JTextField RightWall, JTextField Start,
      JTextField Stop, JTextField Height) {
    double lwall = Double.valueOf(LeftWall.getText());
    double rwall = Double.valueOf(RightWall.getText());
    double startpos = Double.valueOf(Start.getText());
    double stoppos = Double.valueOf(Stop.getText());
    double h = Double.valueOf(Height.getText());
    return new StoreParams(lwall, rwall, startpos, stoppos, h);
  }

  private void setDouble(JTextField textField, double d) {
    String s = String.valueOf(d);
    textField.setText(s);
  }

  private void setInteger(JTextField textField, int i) {
    String s = String.valueOf(i);
    textField.setText(s);
  }

  public void setAvgType(JComboBox comboBox, Settings.CalcModel model) {
    if (model == Settings.CalcModel.avg)
      comboBox.setSelectedIndex(0);
    else
      comboBox.setSelectedIndex(1);
  }

  public void setInetAddress(JTextField textField, InetAddress adr) {
    textField.setText(adr.getHostAddress());
  }

  public void setStore(JTextField LeftWall, JTextField RightWall, JTextField Start,
      JTextField Stop, JTextField Height, StoreParams st) {
    LeftWall.setText(String.valueOf(st.left));
    RightWall.setText(String.valueOf(st.right));
    Start.setText(String.valueOf(st.start));
    Stop.setText(String.valueOf(st.stop));
    Height.setText(String.valueOf(st.height));
  }

  private void restoreFields(Settings settings) {
    setDouble(jVoltageLMTextField, settings.getVoltage());
    setDouble(jStepSLMTextField, settings.getAngStep());
    setInteger(jFreqSLMTextField, settings.getRotFreq());
    setAvgType(jMethodComboBox, settings.getModel());
    setInteger(jMaxAngleTextField, settings.getMaxAngle());
    setInteger(jMinAngleTextField, settings.getMinAngle());
    setInetAddress(jIpSLMTextField, settings.getSlmIp());
    setInteger(jPortLMBatteryTextField, settings.getLmPortBattery());
    setInteger(jPortLMTextField, settings.getLmPort());
    // setInteger(jPortSLMTextField,settings.getSlmPort());
    setStore(jLeftWallTextField, jRightWallTextField, jStartTextField, jStopTextField,
        jHeightTextField, settings.getStore());
  }

  public void setCharge(double charge) {
    jChargeLabel.setText(String.valueOf(charge));
  }

  public void setVolume(double volume) {
    jVolumeLabel.setText(String.format("%02.03f", volume));
  }

  public void setDistance(double distance) {
    jDistanceLabel.setText(String.format("%02.02f", distance));
  }

  public void setRange(double range) {
    jRLabel.setText(String.format("%02.02f", range));
  }

  public void setAngle(double angle) {
    jALabel.setText(String.format("%02.02f", angle));
  }

  public void setStoreHouse(double volume) {
    jStHouseLabel.setText(String.format("%02.02f", volume));
  }

  public void setElemVolume(double volume) {
    jElemVolumeLabel.setText(String.format("%02.04f", volume));
  }

  public void setFreeSpace(double volume) {
    jFreeSpaceLabel.setText(String.format("%02.02f", volume));
  }

  public void setMissedAngle(int number) {
    jMissedALabel.setText(String.valueOf(number));
  }

  public JPanel getRangePanel() {
    return jRangeViewPanel;
  }

  public JPanel getAbovePanel() {
    return jAboveViewPanel;
  }

  public JPanel get3DPanel() {
    return j3DViewPanel;
  }

  // <editor-fold defaultstate="collapsed" desc="Generated
  // Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jSettingsDialog = new javax.swing.JDialog();
    jPanel1 = new javax.swing.JPanel();
    jVoltageLMLabel = new javax.swing.JLabel();
    jVoltageLMTextField = new javax.swing.JTextField();
    jPanel5 = new javax.swing.JPanel();
    // jLengthLabel = new javax.swing.JLabel();
    jStartLabel = new javax.swing.JLabel();
    jStopLabel = new javax.swing.JLabel();
    // jWidthLabel = new javax.swing.JLabel();
    jLeftWallLabel = new javax.swing.JLabel();
    jRightWallLabel = new javax.swing.JLabel();
    jHeightLabel = new javax.swing.JLabel();
    // jLengthTextField = new javax.swing.JTextField();
    jStartTextField = new javax.swing.JTextField();
    jStopTextField = new javax.swing.JTextField();
    // jWidthTextField = new javax.swing.JTextField();
    jLeftWallTextField = new javax.swing.JTextField();
    jRightWallTextField = new javax.swing.JTextField();
    jHeightTextField = new javax.swing.JTextField();
    jPanel3 = new javax.swing.JPanel();
    jPortLMBatteryLabel = new javax.swing.JLabel();
    // jPortLMLabel = new javax.swing.JLabel();
    jIpSLMLabel = new javax.swing.JLabel();
    jPortLMLabel = new javax.swing.JLabel();
    jPortLMBatteryTextField = new javax.swing.JTextField();
    jIpSLMTextField = new javax.swing.JTextField();
    jPortLMTextField = new javax.swing.JTextField();
    jPortLMTextField = new javax.swing.JTextField();
    jPanel2 = new javax.swing.JPanel();
    // jLengthZoneLabel = new javax.swing.JLabel();
    jStepSLMLabel = new javax.swing.JLabel();
    jFreqSLMLabel = new javax.swing.JLabel();
    jMethodLabel = new javax.swing.JLabel();
    // jCountLabel = new javax.swing.JLabel();
    jMaxAngleLabel = new javax.swing.JLabel();
    jMinAngleLabel = new javax.swing.JLabel();
    // jLengthZoneTextField = new javax.swing.JTextField();
    jStepSLMTextField = new javax.swing.JTextField();
    jFreqSLMTextField = new javax.swing.JTextField();
    // jCountTextField = new javax.swing.JTextField();
    jMaxAngleTextField = new javax.swing.JTextField();
    jMinAngleTextField = new javax.swing.JTextField();
    jMethodComboBox = new javax.swing.JComboBox();
    jPanel6 = new javax.swing.JPanel();
    jCancelButton = new javax.swing.JButton();
    jOkButton = new javax.swing.JButton();
    jLogDialog = new javax.swing.JDialog();
    jLogDialog.setMinimumSize(new java.awt.Dimension(400, 300));
    jScrollPane1 = new javax.swing.JScrollPane();
    jLogTextArea = new javax.swing.JTextArea();
    jPanel8 = new javax.swing.JPanel();
    jToolBar1 = new javax.swing.JToolBar();
    jStartToggleButton = new javax.swing.JToggleButton();
    jRangeViewToggleButton = new javax.swing.JToggleButton();
    jAboveViewToggleButton = new javax.swing.JToggleButton();
    j3DViewToggleButton = new javax.swing.JToggleButton();
    jInfoToggleButton = new javax.swing.JToggleButton();
    jPanel7 = new javax.swing.JPanel();
    jInfoPanel = new javax.swing.JPanel();
    jLabel4 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    jLabelR = new javax.swing.JLabel();
    jLabelA = new javax.swing.JLabel();
    jLabelStHouse = new javax.swing.JLabel();
    jLabelElemVolume = new javax.swing.JLabel();
    jLabelFreeSpace = new javax.swing.JLabel();
    jLabelMissedA = new javax.swing.JLabel();

    jDistanceLabel = new javax.swing.JLabel();
    jRLabel = new javax.swing.JLabel();
    jALabel = new javax.swing.JLabel();
    jStHouseLabel = new javax.swing.JLabel();
    jFreeSpaceLabel = new javax.swing.JLabel();
    jMissedALabel = new javax.swing.JLabel();
    jElemVolumeLabel = new javax.swing.JLabel();

    jVolumeLabel = new javax.swing.JLabel();
    jLabel9 = new javax.swing.JLabel();
    jChargeLabel = new javax.swing.JLabel();
    jSplitPane1 = new javax.swing.JSplitPane();
    jSplitPane2 = new javax.swing.JSplitPane();
    jRangeViewPanel = new javax.swing.JPanel();
    jAboveViewPanel = new javax.swing.JPanel();
    j3DViewPanel = new javax.swing.JPanel();
    jMenuBar1 = new javax.swing.JMenuBar();
    jFileMenu = new javax.swing.JMenu();
    jMenuItem1 = new javax.swing.JMenuItem();
    jMenuItem2 = new javax.swing.JMenuItem();
    jMenuItem3 = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JSeparator();
    jSettingsMenuItem = new javax.swing.JMenuItem();
    jSeparator2 = new javax.swing.JSeparator();
    jExitMenuItem = new javax.swing.JMenuItem();
    jViewMenu = new javax.swing.JMenu();
    jRangeViewCheckBox = new javax.swing.JCheckBoxMenuItem();
    jAboveViewCheckBox = new javax.swing.JCheckBoxMenuItem();
    j3DViewCheckBox = new javax.swing.JCheckBoxMenuItem();
    jInfoCheckBox = new javax.swing.JCheckBoxMenuItem();
    jSeparator3 = new javax.swing.JSeparator();
    jLogCheckBox = new javax.swing.JCheckBoxMenuItem();
    jHelpMenu = new javax.swing.JMenu();

    jSettingsDialog.setMinimumSize(new java.awt.Dimension(650, 340)); // 650
    // x
    // 300
    jSettingsDialog.setTitle("Settings");
    // jSettingsDialog.setAlwaysOnTop(true);
    jSettingsDialog.setLocation(210, 250);
    jSettingsDialog.setModal(true);

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Battery"));
    jPanel1.setLayout(new java.awt.GridBagLayout());

    jVoltageLMLabel.setText("Nominal voltage, V");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel1.add(jVoltageLMLabel, gridBagConstraints);

    jVoltageLMTextField.setColumns(3);
    jVoltageLMTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jVoltageLMTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);// 0 0 0
    // 20
    jPanel1.add(jVoltageLMTextField, gridBagConstraints);

    jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Store"));
    jPanel5.setLayout(new java.awt.GridBagLayout());

    jLeftWallLabel.setText("Left wall, m");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel5.add(jLeftWallLabel, gridBagConstraints);

    jRightWallLabel.setText("Right wall, m");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel5.add(jRightWallLabel, gridBagConstraints);

    jHeightLabel.setText("Height, m");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel5.add(jHeightLabel, gridBagConstraints);

    jStartLabel.setText("Position Start, m");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel5.add(jStartLabel, gridBagConstraints);

    jStopLabel.setText("Position Stop, m");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel5.add(jStopLabel, gridBagConstraints);

    jLeftWallTextField.setColumns(3);
    jLeftWallTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jLeftWallTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
    jPanel5.add(jLeftWallTextField, gridBagConstraints);

    jRightWallTextField.setColumns(3);
    jRightWallTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jRightWallTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
    jPanel5.add(jRightWallTextField, gridBagConstraints);

    jHeightTextField.setColumns(3);
    jHeightTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jHeightTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
    jPanel5.add(jHeightTextField, gridBagConstraints);

    jStartTextField.setColumns(3);
    jStartTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jStartTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
    jPanel5.add(jStartTextField, gridBagConstraints);

    jStopTextField.setColumns(3);
    jStopTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jStopTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
    jPanel5.add(jStopTextField, gridBagConstraints);

    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Network"));
    jPanel3.setLayout(new java.awt.GridBagLayout());

    jIpSLMLabel.setText("SLM ip address");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel3.add(jIpSLMLabel, gridBagConstraints);

    jPortLMLabel.setText("IM Virtual COM Port");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel3.add(jPortLMLabel, gridBagConstraints);

    jPortLMBatteryLabel.setText("Battery Port");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel3.add(jPortLMBatteryLabel, gridBagConstraints);

    jPortLMBatteryTextField.setMaximumSize(new java.awt.Dimension(47, 19));
    jPortLMBatteryTextField.setMinimumSize(new java.awt.Dimension(47, 19));
    jPortLMBatteryTextField.setPreferredSize(new java.awt.Dimension(47, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jPanel3.add(jPortLMBatteryTextField, gridBagConstraints);

    jIpSLMTextField.setMaximumSize(new java.awt.Dimension(117, 19));
    jIpSLMTextField.setMinimumSize(new java.awt.Dimension(117, 19));
    jIpSLMTextField.setPreferredSize(new java.awt.Dimension(117, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jPanel3.add(jIpSLMTextField, gridBagConstraints);

    jPortLMTextField.setMaximumSize(new java.awt.Dimension(47, 19));
    jPortLMTextField.setMinimumSize(new java.awt.Dimension(47, 19));
    jPortLMTextField.setPreferredSize(new java.awt.Dimension(47, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jPanel3.add(jPortLMTextField, gridBagConstraints);

    jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Math"));
    jPanel2.setLayout(new java.awt.GridBagLayout());

    jStepSLMLabel.setText("Angular step, deg");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel2.add(jStepSLMLabel, gridBagConstraints);

    jFreqSLMLabel.setText("Rotation frequency, Hz");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel2.add(jFreqSLMLabel, gridBagConstraints);

    jMethodLabel.setText("Method");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel2.add(jMethodLabel, gridBagConstraints);

    jMinAngleLabel.setText("Min angle, deg");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel2.add(jMinAngleLabel, gridBagConstraints);

    jMaxAngleLabel.setText("Max angle, deg");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel2.add(jMaxAngleLabel, gridBagConstraints);

    jStepSLMTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jStepSLMTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    jStepSLMTextField.setPreferredSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 110, 0, 0);// 0 60 0
    // 0
    jPanel2.add(jStepSLMTextField, gridBagConstraints);

    jFreqSLMTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jFreqSLMTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    jFreqSLMTextField.setPreferredSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 110, 0, 0);
    jPanel2.add(jFreqSLMTextField, gridBagConstraints);

    jMethodComboBox
        .setModel(new javax.swing.DefaultComboBoxModel(new String[] { "avg", "middle" }));
    jMethodComboBox.setPreferredSize(new java.awt.Dimension(70, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 110, 0, 0);
    jPanel2.add(jMethodComboBox, gridBagConstraints);

    jMinAngleTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jMinAngleTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    jMinAngleTextField.setPreferredSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 110, 0, 0);
    jPanel2.add(jMinAngleTextField, gridBagConstraints);

    jMaxAngleTextField.setMaximumSize(new java.awt.Dimension(30, 19));
    jMaxAngleTextField.setMinimumSize(new java.awt.Dimension(30, 19));
    jMaxAngleTextField.setPreferredSize(new java.awt.Dimension(30, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 110, 0, 0);
    jPanel2.add(jMaxAngleTextField, gridBagConstraints);

    jCancelButton.setText("Cancel");
    jCancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jCancelButtonActionPerformed(evt);
      }
    });

    jOkButton.setText("OK");
    jOkButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jOkButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        jPanel6Layout.createSequentialGroup().addContainerGap().addComponent(jOkButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19,
                Short.MAX_VALUE).addComponent(jCancelButton)));

    jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {
        jCancelButton, jOkButton });

    jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
            jOkButton).addComponent(jCancelButton)));

    javax.swing.GroupLayout jSettingsDialogLayout =
        new javax.swing.GroupLayout(jSettingsDialog.getContentPane());
    jSettingsDialog.getContentPane().setLayout(jSettingsDialogLayout);

    jSettingsDialogLayout.setHorizontalGroup(jSettingsDialogLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        jSettingsDialogLayout.createSequentialGroup().addContainerGap().addGroup(
            jSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                    jSettingsDialogLayout.createSequentialGroup().addComponent(jPanel1,
                        javax.swing.GroupLayout.PREFERRED_SIZE, 252,
                        javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                        javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel3,
                        javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)).addGroup(
                    jSettingsDialogLayout.createSequentialGroup().addComponent(jPanel5,
                        javax.swing.GroupLayout.PREFERRED_SIZE, 242,
                        javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                        javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel2,
                        javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))// 350
                .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));

    jSettingsDialogLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {
        jPanel1, jPanel5 });

    jSettingsDialogLayout.setVerticalGroup(jSettingsDialogLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        jSettingsDialogLayout.createSequentialGroup().addContainerGap().addGroup(
            jSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 75,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jPanel3,
                    javax.swing.GroupLayout.PREFERRED_SIZE, 88,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
            jSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jPanel2,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
            javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE).addComponent(
            jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
            javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));

    jSettingsDialogLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {
        jPanel2, jPanel5 });

    jSettingsDialogLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {
        jPanel1, jPanel3 });

    jLogDialog.setTitle("Log");
    jLogDialog.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent evt) {
        jLogDialogWindowClosing(evt);
      }
    });

    jLogTextArea.setColumns(20);
    jLogTextArea.setRows(5);
    jScrollPane1.setViewportView(jLogTextArea);

    javax.swing.GroupLayout jLogDialogLayout =
        new javax.swing.GroupLayout(jLogDialog.getContentPane());
    jLogDialog.getContentPane().setLayout(jLogDialogLayout);
    jLogDialogLayout.setHorizontalGroup(jLogDialogLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1,
        javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE));
    jLogDialogLayout.setVerticalGroup(jLogDialogLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1,
        javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE));

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("SVolume");

    jPanel8.setLayout(new java.awt.BorderLayout());

    jToolBar1.setOpaque(false);

    jStartToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
        "/images/player_play.png"))); // NOI18N
    jStartToggleButton.setToolTipText("Start/Stop");
    jStartToggleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    jStartToggleButton.setFocusable(false);
    jStartToggleButton.setMargin(new java.awt.Insets(2, 100, 2, 100));
    jStartToggleButton.setOpaque(false);
    jStartToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource(
        "/images/player_stop.png"))); // NOI18N
    jStartToggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jStartToggleButtonActionPerformed(evt);
      }
    });
    jToolBar1.add(jStartToggleButton);

    viewRange =
        new ViewAction(jRangeViewPanel, jSplitPane2, jSplitPane1, jRangeViewToggleButton,
            jRangeViewCheckBox, true);
    jRangeViewToggleButton.setAction(viewRange);
    jRangeViewToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
        "/images/kimproxyaway.png"))); // NOI18N
    jRangeViewToggleButton.setSelected(true);
    jRangeViewToggleButton.setToolTipText("Range view");
    jRangeViewToggleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    jRangeViewToggleButton.setFocusable(false);
    jRangeViewToggleButton.setOpaque(false);
    jToolBar1.add(jRangeViewToggleButton);

    viewAbove =
        new ViewAction(jAboveViewPanel, jSplitPane2, jSplitPane1, jAboveViewToggleButton,
            jAboveViewCheckBox, true);
    jAboveViewToggleButton.setAction(viewAbove);
    jAboveViewToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
        "/images/kimproxyoffline.png"))); // NOI18N
    jAboveViewToggleButton.setSelected(true);
    jAboveViewToggleButton.setToolTipText("Above view");
    jAboveViewToggleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    jAboveViewToggleButton.setFocusable(false);
    jAboveViewToggleButton.setOpaque(false);
    jToolBar1.add(jAboveViewToggleButton);

    view3D =
        new ViewAction(j3DViewPanel, jSplitPane1, jSplitPane2, j3DViewToggleButton,
            j3DViewCheckBox, true);
    j3DViewToggleButton.setAction(view3D);
    j3DViewToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
        "/images/kimproxyonline.png"))); // NOI18N
    j3DViewToggleButton.setSelected(true);
    j3DViewToggleButton.setToolTipText("3D view");
    j3DViewToggleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    j3DViewToggleButton.setFocusable(false);
    j3DViewToggleButton.setOpaque(false);
    jToolBar1.add(j3DViewToggleButton);

    jInfoToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
        "/images/messagebox_info.png"))); // NOI18N
    jInfoToggleButton.setSelected(true);
    jInfoToggleButton.setToolTipText("information");
    jInfoToggleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    jInfoToggleButton.setFocusable(false);
    jInfoToggleButton.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        jInfoToggleButtonStateChanged(evt);
      }
    });
    jToolBar1.add(jInfoToggleButton);

    jPanel8.add(jToolBar1, java.awt.BorderLayout.CENTER);
    // jPanel8.add(jToolBar1);
    jPanel7.setMinimumSize(new java.awt.Dimension(700, 600));

    jInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));
    jInfoPanel.setLayout(new java.awt.GridBagLayout());

    jLabel4.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabel4.setText("Distance, m");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
    jInfoPanel.add(jLabel4, gridBagConstraints);

    jLabelR.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabelR.setText("Range, m");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
    jInfoPanel.add(jLabelR, gridBagConstraints);

    jLabelA.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabelA.setText("Angle, deg");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
    jInfoPanel.add(jLabelA, gridBagConstraints);

    jLabelStHouse.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabelStHouse.setText("Store house, m^3");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 90, 0, 0);
    jInfoPanel.add(jLabelStHouse, gridBagConstraints);

    jLabelElemVolume.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabelElemVolume.setText("Elementary volume, m^3");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 90, 0, 0);
    jInfoPanel.add(jLabelElemVolume, gridBagConstraints);

    jLabelFreeSpace.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabelFreeSpace.setText("Free space volume, m^3");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 90, 0, 0);
    jInfoPanel.add(jLabelFreeSpace, gridBagConstraints);

    jLabel3.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabel3.setText("LimeVolume, m^3");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 90, 0, 0);
    jInfoPanel.add(jLabel3, gridBagConstraints);

    jLabelMissedA.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabelMissedA.setText("Missed angle reading");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 90, 0, 0);
    jInfoPanel.add(jLabelMissedA, gridBagConstraints);

    jDistanceLabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jDistanceLabel.setText("--");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jDistanceLabel, gridBagConstraints);

    jRLabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jRLabel.setText("--");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jRLabel, gridBagConstraints);

    jALabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jALabel.setText("--");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jALabel, gridBagConstraints);

    jStHouseLabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jStHouseLabel.setText("--");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jStHouseLabel, gridBagConstraints);

    jElemVolumeLabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jElemVolumeLabel.setText("--");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jElemVolumeLabel, gridBagConstraints);

    jFreeSpaceLabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jFreeSpaceLabel.setText("--");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jFreeSpaceLabel, gridBagConstraints);

    jVolumeLabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jVolumeLabel.setText("--");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jVolumeLabel, gridBagConstraints);

    jMissedALabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jMissedALabel.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jMissedALabel, gridBagConstraints);

    jLabel9.setFont(new java.awt.Font("Dialog", 1, 14));
    jLabel9.setText("Energy");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 90, 0, 0);
    jInfoPanel.add(jLabel9, gridBagConstraints);

    jChargeLabel.setFont(new java.awt.Font("Dialog", 1, 14));
    jChargeLabel.setText("58%");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    jInfoPanel.add(jChargeLabel, gridBagConstraints);

    jSplitPane1.setDividerLocation(300);
    jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setResizeWeight(0.5);

    jSplitPane2.setBorder(null);
    jSplitPane2.setDividerLocation(410);
    jSplitPane2.setResizeWeight(0.5);

    jRangeViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Range view"));
    jRangeViewPanel.setDoubleBuffered(false);
    jRangeViewPanel.setMinimumSize(new java.awt.Dimension(40, 30));
    jRangeViewPanel.setPreferredSize(new java.awt.Dimension(40, 30));
    jRangeViewPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent evt) {
        jRangeViewPanelComponentResized(evt);
      }
    });

    javax.swing.GroupLayout jRangeViewPanelLayout = new javax.swing.GroupLayout(jRangeViewPanel);
    jRangeViewPanel.setLayout(jRangeViewPanelLayout);
    jRangeViewPanelLayout.setHorizontalGroup(jRangeViewPanelLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 404, Short.MAX_VALUE));
    jRangeViewPanelLayout.setVerticalGroup(jRangeViewPanelLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 248, Short.MAX_VALUE));

    jSplitPane2.setLeftComponent(jRangeViewPanel);

    jAboveViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Above view"));
    jAboveViewPanel.setDoubleBuffered(false);
    jAboveViewPanel.setMinimumSize(new java.awt.Dimension(40, 30));
    jAboveViewPanel.setPreferredSize(new java.awt.Dimension(40, 30));
    jAboveViewPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent evt) {
        jAboveViewPanelComponentResized(evt);
      }
    });

    javax.swing.GroupLayout jAboveViewPanelLayout = new javax.swing.GroupLayout(jAboveViewPanel);
    jAboveViewPanel.setLayout(jAboveViewPanelLayout);
    jAboveViewPanelLayout.setHorizontalGroup(jAboveViewPanelLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 404, Short.MAX_VALUE));
    jAboveViewPanelLayout.setVerticalGroup(jAboveViewPanelLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 248, Short.MAX_VALUE));

    jSplitPane2.setRightComponent(jAboveViewPanel);

    jSplitPane1.setTopComponent(jSplitPane2);

    j3DViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("3D view"));
    j3DViewPanel.setMinimumSize(new java.awt.Dimension(40, 30));
    j3DViewPanel.setPreferredSize(new java.awt.Dimension(40, 30));
    j3DViewPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent evt) {
        j3DViewPanelComponentResized(evt);
      }
    });

    javax.swing.GroupLayout j3DViewPanelLayout = new javax.swing.GroupLayout(j3DViewPanel);
    j3DViewPanel.setLayout(j3DViewPanelLayout);
    j3DViewPanelLayout.setHorizontalGroup(j3DViewPanelLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 828, Short.MAX_VALUE));
    j3DViewPanelLayout.setVerticalGroup(j3DViewPanelLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 249, Short.MAX_VALUE));

    jSplitPane1.setRightComponent(j3DViewPanel);

    javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
    jPanel7.setLayout(jPanel7Layout);
    jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addComponent(jInfoPanel,
        javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 840,
        Short.MAX_VALUE).addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 840,
        Short.MAX_VALUE));
    jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING).addGroup(
        javax.swing.GroupLayout.Alignment.TRAILING,
        jPanel7Layout.createSequentialGroup().addContainerGap().addComponent(jSplitPane1,
            javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE).addPreferredGap(
            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jInfoPanel,
            javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0))// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!49
        );

    jFileMenu.setMnemonic('F');
    jFileMenu.setText("File");

    jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
        java.awt.event.InputEvent.CTRL_MASK));
    jMenuItem1.setText("New");
    jMenuItem1.setPreferredSize(new java.awt.Dimension(130, 19));
    jFileMenu.add(jMenuItem1);

    jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
        java.awt.event.InputEvent.CTRL_MASK));
    jMenuItem2.setText("Open");
    jFileMenu.add(jMenuItem2);

    jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
        java.awt.event.InputEvent.CTRL_MASK));
    jMenuItem3.setText("Save");
    jFileMenu.add(jMenuItem3);
    jFileMenu.add(jSeparator1);

    jSettingsMenuItem.setMnemonic('s');
    jSettingsMenuItem.setText("Settings");
    jSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jSettingsMenuItemActionPerformed(evt);
      }
    });
    jFileMenu.add(jSettingsMenuItem);
    jFileMenu.add(jSeparator2);

    jExitMenuItem.setMnemonic('x');
    jExitMenuItem.setText("Exit");
    jFileMenu.add(jExitMenuItem);

    jMenuBar1.add(jFileMenu);

    jViewMenu.setMnemonic('V');
    jViewMenu.setText("View");

    jRangeViewCheckBox.setAction(viewRange);
    jRangeViewCheckBox.setSelected(true);
    jRangeViewCheckBox.setText("Range View");
    jRangeViewCheckBox.setPreferredSize(new java.awt.Dimension(130, 19));
    jViewMenu.add(jRangeViewCheckBox);

    jAboveViewCheckBox.setAction(viewAbove);
    jAboveViewCheckBox.setSelected(true);
    jAboveViewCheckBox.setText("Above View");
    jViewMenu.add(jAboveViewCheckBox);

    j3DViewCheckBox.setAction(view3D);
    j3DViewCheckBox.setSelected(true);
    j3DViewCheckBox.setText("3D View");
    jViewMenu.add(j3DViewCheckBox);

    jInfoCheckBox.setSelected(true);
    jInfoCheckBox.setText("Info");
    jInfoCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        jInfoCheckBoxStateChanged(evt);
      }
    });
    jViewMenu.add(jInfoCheckBox);
    jViewMenu.add(jSeparator3);

    jLogCheckBox.setText("Log");
    jLogCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jLogCheckBoxActionPerformed(evt);
      }
    });
    jViewMenu.add(jLogCheckBox);

    jMenuBar1.add(jViewMenu);

    jHelpMenu.setMnemonic('H');
    jHelpMenu.setText("Help");
    jMenuBar1.add(jHelpMenu);

    setJMenuBar(jMenuBar1);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout
        .setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(
                            layout.createSequentialGroup().addGap(12, 12, 12).addComponent(jPanel7,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 852,
                            Short.MAX_VALUE)).addContainerGap()));
    layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(
            layout.createSequentialGroup().addComponent(jPanel8,
                javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel7,
                javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE).addContainerGap()));

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCancelButtonActionPerformed
    if (settings != null) restoreFields(settings);
    jSettingsDialog.setVisible(false);
  }// GEN-LAST:event_jCancelButtonActionPerformed

  private void jOkButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jOkButtonActionPerformed

    if (settings == null) {
      settings = new Settings();
      manager = new Manager(guiPointer, settings);
    }
    try {
      settings.setVoltage(getDouble(jVoltageLMTextField));

      // settings.setlastListLength(getInteger(jLengthZoneTextField));
      settings.setAngStep(getDouble(jStepSLMTextField));
      settings.setRotFreq(getInteger(jFreqSLMTextField));
      settings.setModel(getAvgType(jMethodComboBox));
      // settings.setAvgCount(getInteger(jCountTextField));
      settings.setMaxAngle(getInteger(jMaxAngleTextField));
      settings.setMinAngle(getInteger(jMinAngleTextField));
      settings.setLmPort(getInteger(jPortLMTextField));
      // settings.setSlmPort(getInteger(jPortSLMTextField));

      settings.setStore(getStore(jLeftWallTextField, jRightWallTextField, jStartTextField,
          jStopTextField, jHeightTextField));

      settings.setLmPortBattary(getInteger(jPortLMBatteryTextField));
      settings.setSlmIp(getInetAddress(jIpSLMTextField));

      settings.write();
      jSettingsDialog.setVisible(false);
    } catch (NumberFormatException ex) {
      logger.severe("Wrong data format");
      JOptionPane.showMessageDialog(this, "Wrong data format", "Error", JOptionPane.OK_OPTION);
    } catch (UnknownHostException ex) {
      logger.severe("Wrong IP address");
      JOptionPane.showMessageDialog(this, "Wrong IP address", "Error", JOptionPane.OK_OPTION);
    } catch (IOException ex) {
      logger.severe("Can't write settings");
      JOptionPane.showMessageDialog(this, "Can't write settings", "Error", JOptionPane.OK_OPTION);
    }
  }// GEN-LAST:event_jOkButtonActionPerformed

  private void jSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jSettingsMenuItemActionPerformed
    jSettingsDialog.setVisible(true);
    jSettingsDialog.setLocation(200, 200);
  }// GEN-LAST:event_jSettingsMenuItemActionPerformed

  private void jStartToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jStartToggleButtonActionPerformed
    if (jStartToggleButton.isSelected()) {
      if (settings != null && manager.isConnect()) {
        (new Thread(new Runnable() {
          @Override
          public void run() {
            manager.startDevice();
            logger.info("Start device");
            jSettingsMenuItem.setEnabled(false);
          }
        })).start();
      } else {
        jStartToggleButton.setSelected(false);
        JOptionPane.showMessageDialog(this, "Check Settings", "Error", JOptionPane.OK_OPTION);
      }
    } else {
      manager.stopDevice();
      logger.info("Stop device");
      jSettingsMenuItem.setEnabled(true);
    }
  }// GEN-LAST:event_jStartToggleButtonActionPerformed

  private void jInfoToggleButtonStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_jInfoToggleButtonStateChanged
    jInfoCheckBox.setSelected(jInfoToggleButton.isSelected());
    jInfoPanel.setVisible(jInfoToggleButton.isSelected());
  }// GEN-LAST:event_jInfoToggleButtonStateChanged

  private void jInfoCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_jInfoCheckBoxStateChanged
    jInfoToggleButton.setSelected(jInfoCheckBox.isSelected());
    jInfoPanel.setVisible(jInfoCheckBox.isSelected());
  }// GEN-LAST:event_jInfoCheckBoxStateChanged

  private void jLogCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jLogCheckBoxActionPerformed
    if (jLogCheckBox.isSelected()) {
      jLogDialog.setVisible(true);
      jLogDialog.setLocation(300, 300);
    } else {
      jLogDialog.setVisible(false);
    }
  }// GEN-LAST:event_jLogCheckBoxActionPerformed

  private void jLogDialogWindowClosing(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_jLogDialogWindowClosing
    jLogCheckBox.setSelected(false);
  }// GEN-LAST:event_jLogDialogWindowClosing

  private void jRangeViewPanelComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jRangeViewPanelComponentResized
    manager.resizeGLPanel();
  }// GEN-LAST:event_jRangeViewPanelComponentResized

  private void jAboveViewPanelComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jAboveViewPanelComponentResized
    jRangeViewPanelComponentResized(evt);
  }// GEN-LAST:event_jAboveViewPanelComponentResized

  private void j3DViewPanelComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_j3DViewPanelComponentResized
    jRangeViewPanelComponentResized(evt);
  }// GEN-LAST:event_j3DViewPanelComponentResized

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String args[]) {
    Runnable runnable = new Runnable() {

      @Override
      public void run() {
        Gui gui = new Gui();
        gui.setVisible(true);
        gui.setLocation(100, 100);
      }
    };
    java.awt.EventQueue.invokeLater(runnable);
  }

  private class ViewAction extends AbstractAction {
    private boolean state;

    public ViewAction(JPanel panel, JSplitPane selfSplit, JSplitPane otherSplit,
        JToggleButton button, JCheckBoxMenuItem checkbox, boolean state) {
      putValue("panel", panel);
      putValue("selfSplit", selfSplit);
      putValue("otherSplit", otherSplit);
      putValue("button", button);
      putValue("checkbox", checkbox);
      this.state = state;

    }

    public void actionPerformed(ActionEvent event) {
      JPanel panel = (JPanel) getValue("panel");
      JSplitPane selfSplit = (JSplitPane) getValue("selfSplit");
      JSplitPane otherSplit = (JSplitPane) getValue("otherSplit");
      JToggleButton button = (JToggleButton) getValue("button");
      JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) getValue("checkbox");

      state = !state;

      int self = selfSplit.getDividerSize();
      int other = otherSplit.getDividerSize();

      if (!state) {
        if (self == 0 && other == 0) {
          state = !state;
          button.setSelected(state);
          checkbox.setSelected(state);
          return;
        }
        if (self != 0) {
          selfSplit.setDividerSize(0);
          selfSplit.setDividerLocation(1.0);
        } else {
          otherSplit.setDividerSize(0);
          otherSplit.setDividerLocation(0.0);
        }
      } else {
        if (j3DViewPanel.isVisible() && self == 0 && other == 0) {
          otherSplit.setDividerSize(10);
          otherSplit.setDividerLocation(0.5);

        } else {
          selfSplit.setDividerSize(10);
          selfSplit.setDividerLocation(0.5);
        }
      }
      panel.setVisible(state);
      button.setSelected(state);
      checkbox.setSelected(state);
    }
  }

  private final Logger logger;
  private final Gui guiPointer;
  private Manager manager;
  private Settings settings = null;

  private ViewAction viewRange;
  private ViewAction viewAbove;
  private ViewAction view3D;
  private ViewAction viewInfo;
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBoxMenuItem j3DViewCheckBox;
  private javax.swing.JPanel j3DViewPanel;
  private javax.swing.JToggleButton j3DViewToggleButton;
  private javax.swing.JCheckBoxMenuItem jAboveViewCheckBox;
  private javax.swing.JPanel jAboveViewPanel;
  private javax.swing.JToggleButton jAboveViewToggleButton;
  private javax.swing.JButton jCancelButton;
  private javax.swing.JLabel jChargeLabel;
  // private javax.swing.JLabel jCountLabel;
  private javax.swing.JLabel jMinAngleLabel;
  private javax.swing.JLabel jMaxAngleLabel;
  // private javax.swing.JTextField jCountTextField;
  private javax.swing.JTextField jMinAngleTextField;
  private javax.swing.JTextField jMaxAngleTextField;
  private javax.swing.JLabel jDistanceLabel;

  private javax.swing.JLabel jRLabel;
  private javax.swing.JLabel jALabel;
  private javax.swing.JLabel jStHouseLabel;
  private javax.swing.JLabel jElemVolumeLabel;
  private javax.swing.JLabel jFreeSpaceLabel;
  private javax.swing.JLabel jMissedALabel;

  private javax.swing.JMenuItem jExitMenuItem;
  private javax.swing.JMenu jFileMenu;
  private javax.swing.JLabel jHeightLabel;
  private javax.swing.JTextField jHeightTextField;
  private javax.swing.JMenu jHelpMenu;
  private javax.swing.JCheckBoxMenuItem jInfoCheckBox;
  private javax.swing.JPanel jInfoPanel;
  private javax.swing.JToggleButton jInfoToggleButton;
  private javax.swing.JLabel jPortLMBatteryLabel;
  private javax.swing.JTextField jPortLMBatteryTextField;
  // private javax.swing.JLabel jPortLMLabel;
  // private javax.swing.JTextField jPortLMTextField;
  private javax.swing.JLabel jIpSLMLabel;
  private javax.swing.JTextField jIpSLMTextField;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JLabel jLabelR;
  private javax.swing.JLabel jLabelA;
  private javax.swing.JLabel jLabelStHouse;
  private javax.swing.JLabel jLabelElemVolume;
  private javax.swing.JLabel jLabelFreeSpace;
  private javax.swing.JLabel jLabelMissedA;
  // private javax.swing.JLabel jLengthLabel;
  private javax.swing.JLabel jStartLabel;
  private javax.swing.JLabel jStopLabel;
  // private javax.swing.JTextField jLengthTextField;
  private javax.swing.JTextField jStartTextField;
  private javax.swing.JTextField jStopTextField;
  // private javax.swing.JLabel jLengthZoneLabel;
  private javax.swing.JLabel jStepSLMLabel;
  private javax.swing.JLabel jFreqSLMLabel;
  // private javax.swing.JTextField jLengthZoneTextField;
  private javax.swing.JTextField jStepSLMTextField;
  private javax.swing.JTextField jFreqSLMTextField;
  private javax.swing.JCheckBoxMenuItem jLogCheckBox;
  private javax.swing.JDialog jLogDialog;
  private javax.swing.JTextArea jLogTextArea;
  private javax.swing.JMenuBar jMenuBar1;
  private javax.swing.JMenuItem jMenuItem1;
  private javax.swing.JMenuItem jMenuItem2;
  private javax.swing.JMenuItem jMenuItem3;
  private javax.swing.JComboBox jMethodComboBox;
  private javax.swing.JLabel jMethodLabel;
  private javax.swing.JButton jOkButton;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JPanel jPanel7;
  private javax.swing.JPanel jPanel8;
  private javax.swing.JLabel jPortLMLabel;
  private javax.swing.JTextField jPortLMTextField;
  private javax.swing.JCheckBoxMenuItem jRangeViewCheckBox;
  private javax.swing.JPanel jRangeViewPanel;
  private javax.swing.JToggleButton jRangeViewToggleButton;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JSeparator jSeparator2;
  private javax.swing.JSeparator jSeparator3;
  private javax.swing.JDialog jSettingsDialog;
  private javax.swing.JMenuItem jSettingsMenuItem;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JSplitPane jSplitPane2;
  private javax.swing.JToggleButton jStartToggleButton;
  private javax.swing.JToolBar jToolBar1;
  private javax.swing.JMenu jViewMenu;
  private javax.swing.JLabel jVoltageLMLabel;
  private javax.swing.JTextField jVoltageLMTextField;
  private javax.swing.JLabel jVolumeLabel;
  // private javax.swing.JLabel jWidthLabel;
  private javax.swing.JLabel jLeftWallLabel;
  private javax.swing.JLabel jRightWallLabel;
  // private javax.swing.JTextField jWidthTextField;
  private javax.swing.JTextField jLeftWallTextField;
  private javax.swing.JTextField jRightWallTextField;
  // End of variables declaration//GEN-END:variables

}
