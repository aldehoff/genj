/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.util.swing;

import genj.util.GridBagHelper;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * Until we're going for JDK 1.4 there's no JSpinner - this is the
 * substitute
 * 
 * @author nmeier
 */
public class SpinnerWidget extends JPanel {
  
  /** text */
  private TextFieldWidget tfield;

  /** buttons */
  private AB anorth, asouth;

  /** model */
  private Model model;
  
  /** change listener */
  private ChangeListener modelListener;
  
  /** format */
  private Format format = null;

  /**
   * Constructor 
   */
  public SpinnerWidget(String txt, int setCols, Model setModel) {
    
    // setup
    setAlignmentX(0);
    
    tfield = new TextFieldWidget("", setCols);
    tfield.setHorizontalAlignment(SwingConstants.RIGHT);
    
    anorth = new AB(SwingConstants.NORTH);
    asouth = new AB(SwingConstants.SOUTH);
    
    GridBagHelper gbh = new GridBagHelper(this);
    gbh.add(tfield, 1, 0, 1, 2);
    gbh.add(anorth, 2, 0); 
    gbh.add(asouth, 2, 1);
    if (txt.length()>0)
      gbh.add(new JLabel(txt), 3, 0, 1, 2);
    
    // listening
    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource()==anorth) setNext();
        else setPrevious();
      }
    };
    anorth.addActionListener(al);
    asouth.addActionListener(al);
    
    modelListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        tfield.setText(format(model.getValue()));
      }
    };
    
    // remember model
    setModel(setModel);
    
    // done 
  }
  
  /**
   * Add change listener
   */
  public void addChangeListener(ChangeListener l) {
    tfield.addChangeListener(l);
  }
  
  /**
   * Remove change listener
   */
  public void removeChangeListener(ChangeListener l) {
    tfield.removeChangeListener(l);
  }
  
  /**
   * @see javax.swing.JComponent#getMaximumSize()
   */
  public Dimension getMaximumSize() {
    return new Dimension(getPreferredSize().width,tfield.getPreferredSize().height);
  }

  /**
   * Helper formatting a value
   */
  private String format(Object value) {
    return format==null ? value.toString() : format.format(value);
  }
  
  /**
   * Set formatter to use
   */
  public void setFormat(Format setFormat) {
    format = setFormat;
  }

  /**
   * Set model
   */
  public void setModel(Model setModel) {
    // stop?
    if (model!=null) 
      model.removeChangeListener(modelListener);
    // new!
    model = setModel;
    // start
    model.addChangeListener(modelListener);
    // show
    tfield.setText(format(model.getValue()));
    tfield.selectAll();
    // done
  }
  
  /** 
   * Get model
   */
  public Model getModel() {
    return model;
  }
  
  /**
   * Set to next value
   */
  public void setNext() {
    // propagate next
    model.setValue(model.getNextValue());
    // select
    tfield.selectAll();
    // done
  }
  
  /**
   * Set to previous value
   */
  public void setPrevious() {
    // propagate prev
    model.setValue(model.getPreviousValue());
  }
  
  
  /**
   * Our arrow button
   */
  private class AB extends BasicArrowButton {
    /** constructor */
    private AB(int dir) { super(dir); }
    /** patched size */
    public Dimension getPreferredSize() {
      int i = tfield.getPreferredSize().height/2;
      return new Dimension(i,i);
    }
    /** patched size */
    public Dimension getMinimumSize() {
      return getPreferredSize();
    }
  } //AB
  
  /** 
   * Model
   */
  public interface Model {
    
    /**
     * Accessor - value
     */
    public Object getValue();
    
    /**
     * Accessor - value
     */
    public void setValue(Object val) throws IllegalArgumentException ;
    
    /**
     * Accessor - next value
     */
    public Object getNextValue();
    
    /**
     * Accessor - prev value
     */
    public Object getPreviousValue();
    
    /**
     * Listener - add
     */
    public void addChangeListener(ChangeListener l);
    
    /**
     * Listener - remove
     */
    public void removeChangeListener(ChangeListener l);
    
  } //Model
  
  /**
   * Abstract Model
   */
  public abstract static class AbstractModel implements Model {
    
    /** listeners */
    private List listeners = new ArrayList();
    
    /**
     * @see genj.util.swing.SpinnerWidget.Model#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener l) {
      listeners.add(l);
    }
    
    /**
     * @see genj.util.swing.SpinnerWidget.Model#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener(ChangeListener l) {
      listeners.remove(l);
    }

    /**
     * fire state change
     */
    protected void fireStateChanged() {
      ChangeEvent e = new ChangeEvent(this);
      ChangeListener[] ls = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
      for (int i = 0; i < ls.length; i++) {
      	ls[i].stateChanged(e);
      }
    }

  } //AbstractModel
  
  /**
   * Integer based Model
   */
  public static class IntegerModel extends AbstractModel {
    
    /** value */
    private int value;
    
    /** max, min */
    private int maximum, minimum;
    
    /**
     * Constructor
     */
    public IntegerModel(int val, int min, int max) {
      if (max<min) throw new IllegalArgumentException("max<min");
      value = Math.min(Math.max(min,val), max);
      maximum = max;
      minimum = min;
    }
    
    /**
     * @see genj.util.swing.SpinnerWidget.Model#getNextValue()
     */
    public Object getNextValue() {
      return new Integer(value==maximum ? minimum : value+1);
    }
    
    /**
     * @see genj.util.swing.SpinnerWidget.Model#getPreviousValue()
     */
    public Object getPreviousValue() {
      return new Integer(value==minimum ? maximum : value-1);
    }
    
    /**
     * @see genj.util.swing.SpinnerWidget.Model#getValue()
     */
    public Object getValue() {
      return new Integer(value);
    }
    
    /**
     * @see genj.util.swing.SpinnerWidget.Model#setValue(java.lang.Object)
     */
    public void setValue(Object val) throws IllegalArgumentException {
      try {
        int i = Integer.parseInt(val.toString());
        if (i<minimum||i>maximum)
          throw new IllegalArgumentException("val>max || val<min");
        value = i;
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Integer.parseInt() failed");
      }
      fireStateChanged();
    }
    
  } //IntegerModel

  /**
   * Model for values with fixed number of values with fractions
   */
  public static class FractionModel extends AbstractModel {
    
    /** value */
    private int value, minimum, maximum;
    
    /** the base 1 or 0.1 or 0.01 */
    private int base;
    
    /**
     * Constructor
     */
    public FractionModel(double min, double max, int fractionDigits) {

      base = 1;
      while (fractionDigits-->0) base *= 10; 

      minimum = (int)(min*base);
      maximum = (int)(max*base);
      value = (int)(min*base);
      
    }
    /**
     * @see genj.util.swing.SpinnerWidget.Model#getNextValue()
     */
    public Object getNextValue() {
      int next = value+1;
      if (next>maximum) next = minimum;
      return new Double(next/(double)base);
    }
    /**
     * @see genj.util.swing.SpinnerWidget.Model#getPreviousValue()
     */
    public Object getPreviousValue() {
      int prev = value-1;
      if (prev<minimum) prev = maximum;
      return new Double(prev/(double)base);
    }
    /**
     * @see genj.util.swing.SpinnerWidget.Model#getValue()
     */
    public Object getValue() {
      return new Double(value/(double)base);
    }
    /**
     * @see genj.util.swing.SpinnerWidget.Model#setValue(java.lang.Object)
     */
    public void setValue(Object val) throws IllegalArgumentException {
      try {
        setDoubleValue(Double.parseDouble(val.toString()));
      } catch (IllegalArgumentException e) {
      }
    }
    /**
     * Access the underlying value
     */
    public void setDoubleValue(double val) {
      value = Math.max(minimum, Math.min(maximum, (int)(val*base)));
      fireStateChanged();
    }
    /**
     * Access the underlying value
     */
    public double getDoubleValue() {
      return value/(double)base;
    }
  } //CmModel

} //SpinnerWidget
