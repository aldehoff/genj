/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Class which provides Components regarding to environment.
 */
public class ComponentProvider {

  private static final int
    BUTTON = 0,
    SCROLLBAR = 1,
    COMBOBOX  = 2,
    CONTAINER = 3,
    LABEL = 4,
    NUM_ABSTRACTS = 5;

  private final static Class[] cachedAbstractClasses = new Class[NUM_ABSTRACTS];

  private final static String[] abstractNames = {
    "Button", "Scrollbar", "Combobox", "Container", "Label"
  };

  public final static int
    UNKNOWN     = -1,
    HEAVYWEIGHT = 0,
    LIGHTWEIGHT = 1;

  public final static int
    BOTH       = 0,
    IMAGE_ONLY = 1,
    TEXT_ONLY  = 2;

  private static int
    mode = UNKNOWN;

  private final static String[] modeNames = {
    "Heavyweight","Lightweight"
  };

  /**
   * An interface to an AbstractScrollbar
   */
  interface AbstractScrollbar {

    public void setOrientation(int orientation);

    // EOC
  }

  /**
   * A Heavyweight Scrollbar
   */
  static class HeavyweightScrollbar extends Scrollbar implements AbstractScrollbar {

    public void setValue(int value) {
      super.setValue(value);
      AdjustmentEvent ae = new AdjustmentEvent(this,AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,AdjustmentEvent.UNIT_INCREMENT,0);
      processAdjustmentEvent(ae);
    }

    // EOC
  }

  /**
   * A Lightweight Scrollbar
   */
  static class LightweightScrollbar extends javax.swing.JScrollBar implements AbstractScrollbar {

    // EOC
  }

  /**
   * A heavyweight implementation of our Combobox
   */
  static class HeavyweightCombobox extends Combobox implements ItemListener {
    private Choice _choice = (Choice)getDependant();
    private Vector _listeners=new Vector();
    private String _acommand="";
    HeavyweightCombobox() {
      _choice.addItemListener(this);
    }
    public void setActionCommand(String acommand) {
      _acommand=acommand;
    }
    public void itemStateChanged(ItemEvent ie) {
      ActionEvent ae = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,_acommand);
      Enumeration e = _listeners.elements();
      while (e.hasMoreElements()) {
        ((ActionListener)e.nextElement()).actionPerformed(ae);
      }
    }
    public void setElements(Object[] elements) {
      _choice.removeAll();
      for (int i=0;i<elements.length;i++) {
        _choice.add(elements[i].toString());
      }
    }
    public void setSelectedIndex(int which) {
      _choice.select(which);
    }
    public int getSelectedIndex() {
      return _choice.getSelectedIndex();
    }
    protected Component getDependant() {
      if (_choice==null) {
        _choice = new Choice();
      }
      return _choice;
    }
    public void addActionListener(ActionListener listener) {
      _listeners.addElement(listener);
    }
    public void removeActionListener(ActionListener listener) {
      _listeners.removeElement(listener);
    }
    // EOC
  }

  /**
   * A lightweight implementation of our Combobox
   */
  static class LightweightCombobox extends Combobox {
    private javax.swing.JComboBox _combo = (javax.swing.JComboBox)getDependant();
    public void setActionCommand(String acommand) {
      _combo.setActionCommand(acommand);
    }
    public void setElements(Object[] elements) {
      _combo.setModel(new javax.swing.DefaultComboBoxModel(elements));
    }
    public void setSelectedIndex(int which) {
      _combo.setSelectedIndex(which);
    }
    public int getSelectedIndex() {
      return _combo.getSelectedIndex();
    }
    protected Component getDependant() {
      if (_combo==null) {
        _combo = new javax.swing.JComboBox();
      }
      return _combo;
    }
    public void addActionListener(ActionListener listener) {
      _combo.addActionListener(listener);
    }
    public void removeActionListener(ActionListener listener) {
      _combo.removeActionListener(listener);
    }
    // EOC
  }

  /**
   * An interface to an AbstractButton
   */
  interface AbstractButton {
    public void setText(String text, int policy);
    public void setImage(Image image, int policy);
    public void setActionCommand(String action);
    public void addActionListener(ActionListener listener);
    public void setToolTipText(String tip);
    public void setMargin(Insets insets);
    // EOC
  }

  /**
   * A Heavyweight Button
   */
  static class HeavyweightButton extends Button implements AbstractButton {
    public void setText(String text,int policy) {
      super.setLabel(text);
    }
    public void setImage(Image image, int policy) {}
    //public void setActionCommand(String action);
    //public void addActionListener(ActionListener listener);
    public void setToolTipText(String tip) {}
    public void setMargin(Insets insets) {}
    // EOC
  }

  /**
   * A Lightweight Button
   */
  static class LightweightButton extends javax.swing.JButton implements AbstractButton {
    LightweightButton() {
      setRequestFocusEnabled(false);
    }
    public void setText(String text, int policy) {
      if (policy!=IMAGE_ONLY)
      super.setText(text);
    }
    public void setImage(Image image, int policy) {
      if (policy!=TEXT_ONLY)
      super.setIcon(new javax.swing.ImageIcon(image));
    }
    //public void setActionCommand(String action);
    //public void addActionListener(ActionListener listener);
    //public void setToolTipText(String tip);
    //public void setMargin();
    // EOC
  }


  /**
   * A Heavyweight Container
   */
  static class HeavyweightContainer extends Container {
    public HeavyweightContainer() {
      setLayout(new FlowLayout());
    }
    // EOC
  }

  /**
   * A Lightweight Container
   */
  static class LightweightContainer extends javax.swing.JComponent {
    public LightweightContainer() {
      setLayout(new FlowLayout());
    }
    // EOC
  }

  /**
   * An interface to an AbstractButton
   */
  interface AbstractLabel {
    public void setText(String text);
    // EOC
  }

  /**
   * A Heavyweight Label
   */
  static class HeavyweightLabel extends Label implements AbstractLabel {
    // EOC
  }

  /**
   * A Lightweight Label
   */
  static class LightweightLabel extends javax.swing.JLabel implements AbstractLabel {
    // EOC
  }

  /**
   * Check for the type of components to create in current environment
   */
  private static void checkMode() {

    // Done?
    if (mode!=UNKNOWN) {
      return;
    }

    // A user defined mode?
    try {
      // .. some browsers might not allow us to read this
      if (System.getProperty("awtx.lightweight")!=null) {
        mode = LIGHTWEIGHT;
      } else {
        if (System.getProperty("awtx.heavyweight")!=null) {
          mode = HEAVYWEIGHT;
        }
      }
    } catch (Throwable throwable) {
    }

    // Last chance to find out which to use
    if (mode==UNKNOWN) {

      // ... try to find out
      try {
        Class c = Class.forName("javax.swing.JComponent");
        mode = LIGHTWEIGHT;
      } catch (ClassNotFoundException ex) {
        mode = HEAVYWEIGHT;
      }
    }

    // Done
    //System.out.println("[Debug]Creating "+modeNames[mode]+" components from now on");
  }

  /**
   * Static method for generating an Adjustable Component
   */
  public static Component createAdjustable(int orientation) {

    // Get Abstract Component
    AbstractScrollbar s;
    try {
      s = (AbstractScrollbar)getClass(SCROLLBAR).newInstance();
    } catch (Exception e) {
      return null;
    }

    s.setOrientation(orientation);

    // Done
    return (Component)s;
  }

  /**
   * Static method for generating a Button
   * @param image the image to put on the button
   * @param text the text to put on the button
   * @param action the action command to be set on the button
   * @param listener the action listener to attach to the button
   * @param policy for showing text & image BOTH, IMAGE_ONLY, TEXT_ONLY
   */
  public static Component createButton(Image image, String text, String tip, String action, ActionListener listener) {
    return createButton(image,text,tip,action,listener,BOTH,null);
  }

  public static Component createButton(Image image, String text, String tip, String action, ActionListener listener, int policy) {
    return createButton(image,text,tip,action,listener,policy,null);
  }

  public static Component createButton(Image image, String text, String tip, String action, ActionListener listener, int policy, Insets margin) {

    // Get Abstract Component
    AbstractButton b;
    try {
      b = (AbstractButton)getClass(BUTTON).newInstance();
    } catch (Exception e) {
      return null;
    }

    if (text!=null)
      b.setText(text,policy);
    if (image!=null)
      b.setImage(image,policy);
    if (tip!=null)
      b.setToolTipText(tip);
    if (action!=null)
      b.setActionCommand(action);
    if (listener!=null)
      b.addActionListener(listener);
    if (margin!=null)
      b.setMargin(margin);

    // Done
    return (Component)b;
  }

  /**
   * Create a Combobox component
   */
  public static Combobox createCombobox() {
    // Get Abstract Component
    Combobox c;
    try {
      c = (Combobox)getClass(COMBOBOX).newInstance();
    } catch (Exception e) {
      return null;
    }
    // Done
    return c;
  }

  /**
   * Create a Container component
   */
  public static Container createContainer() {

    // Get Abstract Component
    try {
      return (Container)getClass(CONTAINER).newInstance();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Creates a Dialog
   */
  public static Window createDialog(Frame parent, String title, final Component content, final Runnable runClosed) {

    final Window result;

    if (parent!=null) {
      result = new Dialog(parent,title);
    } else {
      result = new Frame(title);
    }

    result.add("Center",content);

    WindowAdapter wadapter = new WindowAdapter() {
      public void windowClosed(WindowEvent e) {
        if (runClosed!=null) {
          runClosed.run();
        }
      }
      public void windowClosing(WindowEvent e) {
        result.dispose();
      }
    };
    result.addWindowListener(wadapter);

    // Done
    return result;
  }

  /**
   * Create a Label component
   */
  public static Component createLabel(String text) {

    // Get Abstract Component
    try {
      Component result = (Component)getClass(LABEL).newInstance();
      AbstractLabel label = (AbstractLabel)result;
      label.setText(text);
      return result;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Helper which calculates a class for a lightweight/heavyweight component
   * with given postfix
   */
  private static Class getClass(int abstractType) {

    // Make sure we know what we're doing
    checkMode();

    // Known?
    Class c = cachedAbstractClasses[abstractType];
    if (c!=null)
      return c;

    // Resolve Class
    String name = ComponentProvider.class.getName()
           +"$"+modeNames[mode]+abstractNames[abstractType];

    try {
      c = Class.forName(name);
    } catch (Exception e) {
      return null;
    }

    // Remember
    cachedAbstractClasses[abstractType]=c;

    // Done
    return c;
  }

}
