package sample.tracker;

import genj.view.View;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class TrackerView extends View {

  private JTextArea out = new JTextArea(40,10);
  
  public TrackerView() {
    
    out.setEditable(false);
    
    add(new JScrollPane(out));
  }

  public void add(String text) {
    // log a text message to our output area
    try {
      Document doc = out.getDocument();
      doc.insertString(doc.getLength(), text, null);
      doc.insertString(doc.getLength(), "\n", null);
    } catch (BadLocationException e) {
      // can't happen
    }
  }

}
