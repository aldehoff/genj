/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.shell;

import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.model.MutableGraph;
import gj.shell.swing.UnifiedAction;
import gj.shell.util.ReflectHelper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

/**
 * A widget that describes a Layout
 */
public class LayoutWidget extends JPanel {
  
  /** the layouts we've instantiated */
  private Layout[] layouts = new Layout[0];
  
  /** the graph we're looking at */
  private MutableGraph graph;
  
  /** the combo of layouts */
  private JComboBox comboLayouts;

  /** the layout's properties */
  private PropertyWidget widgetProperties;
  
  /** the button */
  private JButton buttonExecute;
  
  /** the actions we keep */
  private Action 
    actionExecute = new ActionExecute(),
    actionSelect = new ActionSelect();
    
  /** listeners */
  private List alisteners = new ArrayList(); 

  /**
   * Constructor
   */  
  public LayoutWidget() {

    // prepare this
    setLayout(new BorderLayout());
    
    // create widgets
    comboLayouts = new JComboBox();
    widgetProperties = new PropertyWidget();
    buttonExecute = new JButton();
    
    add(comboLayouts,BorderLayout.NORTH);
    add(widgetProperties, BorderLayout.CENTER);
    add(buttonExecute, BorderLayout.SOUTH);

    // ready to listen
    widgetProperties.addActionListener(actionExecute);
    buttonExecute.setAction(actionExecute);
    
    // listening
    comboLayouts.setAction(actionSelect);
    
    // done
  }

  /**
   * Accessor - the graph
   */
  public void setGraph(MutableGraph set) {
    graph = set;
    actionExecute.setEnabled(graph!=null);
  }

  /**
   * Accessor - the default button
   */
  public JButton getDefaultButton() {
    return buttonExecute;
  }

  
  /**
   * Accessor - the layouts
   */
  public Layout[] getLayouts() {
    return layouts;
  }

  /**
   * Accessor - the layouts
   */
  public void setLayouts(Layout[] layouts) {
    this.layouts=layouts;
    comboLayouts.setModel(new DefaultComboBoxModel(layouts));
    if (layouts.length>0) comboLayouts.setSelectedItem(layouts[0]);
  }

  /**
   * Accessor - current layout
   */
  public void setSelectedLayout(Layout layout) {
    comboLayouts.setSelectedItem(layout);
  }
  
  /**
   * Accessor - current layout
   */
  public Layout getSelectedLayout() {
    return (Layout)comboLayouts.getSelectedItem();
  }
  
  /** 
   * Adds an execute listener
   */
  public void addActionListener(ActionListener listener) {
    alisteners.add(listener);
  }
  
  /**
   * How to handle - Run the layout
   */
  protected class ActionExecute extends UnifiedAction {
    protected ActionExecute() { super("Execute"); setEnabled(false); }
    public void execute() throws LayoutException {
      if (getSelectedLayout()==null) return;
      widgetProperties.commit();
      Iterator it = alisteners.iterator();
      while (it.hasNext()) {
        ((ActionListener)it.next()).actionPerformed(null);
      }
      widgetProperties.refresh();
    }
  }
  
  /**
   * How to handle - Select a layout
   */
  protected class ActionSelect extends UnifiedAction {
    protected ActionSelect() { super("Select"); }
    public void execute() {
      // get the selected layout
      Object layout = comboLayouts.getModel().getSelectedItem();
      if (layout==null) return;
      Iterator it = alisteners.iterator();
      while (it.hasNext()) {
        ((ActionListener)it.next()).actionPerformed(null);
      }
      // show its properties
      widgetProperties.setInstance(layout);
    }
  }

  
}
