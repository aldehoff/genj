/**
 * 
 */
package genj.option;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * A custom option with custom UI
 */
public abstract class CustomOption extends Option {

  /** reference widget */
  protected OptionsWidget widget;

  /** our ui */
  private UI ui = new UI();
  
  /** callback - ui access */
  public OptionUI getUI(OptionsWidget widget) {
    this.widget = widget;
    return ui;
  }
  
  /** 
   * implementation requirement - edit visually 
   */
  protected abstract void edit();
    
  /** 
   * Custom UI is a button only
   */
  private class UI implements OptionUI, ActionListener {
    
    /** callback - text representation = none */
    public String getTextRepresentation() {
      return null;
    }

    /** callback - component representation = button */
    public JComponent getComponentRepresentation() {
      JButton result = new JButton("...");
      result.setFocusable(false);
      result.addActionListener(this);
      return result;
    }

    /** commit - noop */    
    public void endRepresentation() {
    }
    
    /** callback - button pressed */
    public void actionPerformed(ActionEvent e) {
      edit();
    }
  
  } //UI

} //CustomOption
 