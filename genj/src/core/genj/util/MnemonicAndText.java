/**
 * 
 */
package genj.util;


/**
 * A text and mnemonic wrapper
 */
public class MnemonicAndText {
  
  private char mnemonic = 0;
  private String text;

  public MnemonicAndText(String label) {
    
    // safety check
    if (label==null)
      label = "";
    text = label;
    
    // look for mnemonic
    int i = label.indexOf('~');    
    if (i<0) {
      mnemonic = text.length()>0 ? text.charAt(0) : 0;
      return;
    }
    if (i==label.length()-1) {
      mnemonic = text.length()>0 ? text.charAt(0) : 0;
      text = text.substring(0, label.length()-1);
      return;
    }
    
    // splice it
    mnemonic = text.charAt(i+1);
    text = text.substring(0,i)+text.substring(i+1);
      
    // done
  }
  
  /** mnemonic representation */
  public char getMnemonic() {
    return mnemonic;
  }
  
  /** text representation */
  public String getText(String markup) {
    if (mnemonic==0)
      return getText();
    return text + " ["+markup+mnemonic+"]";
  }

  /** text representation */
  public String getText() {
    return text;
  }

  /** string representation */
  public String toString() {
    return mnemonic==0 ? text : text+"["+mnemonic+"]";
  }
  
}