/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell;

import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.shell.model.EditableGraph;
import gj.shell.swing.Action2;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * A widget that describes a Layout
 */
public class AlgorithmWidget extends JPanel {
  
  /** the algorithms we've instantiated */
  private LayoutAlgorithm[] algorithms = new LayoutAlgorithm[0];
  
  /** the graph we're looking at */
  private EditableGraph graph;
  
  /** the combo of layouts */
  private JComboBox comboAlgorithms;

  /** the layout's properties */
  private PropertyWidget widgetProperties;
  
  /** the button */
  private JButton buttonExecute;
  
  /** the actions we keep */
  private Action 
    actionExecute = new ActionExecute(),
    actionSelect = new ActionSelect();
    
  /** listeners */
  private List<ActionListener> alisteners = new ArrayList<ActionListener>(); 

  /**
   * Constructor
   */  
  public AlgorithmWidget() {

    // prepare this
    setLayout(new BorderLayout());
    
    // create widgets
    comboAlgorithms = new JComboBox();
    widgetProperties = new PropertyWidget();
    buttonExecute = new JButton();
    
    add(comboAlgorithms,BorderLayout.NORTH);
    add(widgetProperties, BorderLayout.CENTER);
    add(buttonExecute, BorderLayout.SOUTH);

    // ready to listen
    widgetProperties.addActionListener(actionExecute);
    buttonExecute.setAction(actionExecute);
    
    // listening
    comboAlgorithms.setAction(actionSelect);
    
    // done
  }

  /**
   * Accessor - the graph
   */
  public void setGraph(EditableGraph set) {
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
   * Accessor - the algorithms
   */
  public LayoutAlgorithm[] getAlgorithms() {
    return algorithms;
  }

  /**
   * Accessor - the algorithms
   */
  public void setAlgorithms(LayoutAlgorithm[] set) {
    algorithms=set;
    comboAlgorithms.setModel(new DefaultComboBoxModel(algorithms));
    if (algorithms.length>0) 
      comboAlgorithms.setSelectedItem(algorithms[0]);
  }

  /**
   * Accessor - current algorithms
   */
  public void setSelectedAlgorithm(LayoutAlgorithm set) {
    comboAlgorithms.setSelectedItem(set);
  }
  
  /**
   * Accessor - current algorithm
   */
  public LayoutAlgorithm getSelectedAlgorithm() {
    return (LayoutAlgorithm)comboAlgorithms.getSelectedItem();
  }
  
  /** 
   * Adds an execute listener
   */
  public void addActionListener(ActionListener listener) {
    alisteners.add(listener);
  }
  
  /**
   * How to handle - Run the algorithm
   */
  protected class ActionExecute extends Action2 {
    protected ActionExecute() { 
      super("Execute"); 
      setEnabled(false); 
    }
    @Override
    public void execute() throws LayoutAlgorithmException {
      if (getSelectedAlgorithm()==null) 
        return;
      widgetProperties.commit();
      for (ActionListener listener : alisteners) 
        listener.actionPerformed(null);
      widgetProperties.refresh();
    }
  }
  
  /**
   * How to handle - Select an algorithm
   */
  protected class ActionSelect extends Action2 {
    protected ActionSelect() { 
      super("Select"); 
    }
    @Override
    public void execute() {
      // get the selected algorithm
      Object algorithm = comboAlgorithms.getModel().getSelectedItem();
      if (algorithm==null) 
        return;
      for (ActionListener listener : alisteners) 
        listener.actionPerformed(null);
      // show its properties
      widgetProperties.setInstance(algorithm);
    }
  }

  
}
