/**
 * 
 */
package genj.option;

import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;

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
  private class UI extends ActionDelegate implements OptionUI {
    
    /** callback - text representation = none */
    public String getTextRepresentation() {
      return null;
    }

    /** callback - component representation = button */
    public JComponent getComponentRepresentation() {
      setText("...");
      return new ButtonHelper().setFocusable(false).create(this);
    }

    /** commit - noop */    
    public void endRepresentation() {
    }
    
    /** callback - button pressed */
    protected void execute() {
      edit();
    }
  
  } //UI

} //CustomOption
 