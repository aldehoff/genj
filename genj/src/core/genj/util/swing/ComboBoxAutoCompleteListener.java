package genj.util.swing;

import genj.util.swing.ChoiceWidget.Editor;

import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ComboBoxAutoCompleteListener implements ActionListener {

    // the combox to autocomplete
    JComboBox theComboBox;

    /**
     * @param aComboBox   ComboBox to autocomplete. 
     */
    public ComboBoxAutoCompleteListener(JComboBox aComboBox) {
        theComboBox = aComboBox;

        // Find the ChoiceWidgets editor
        Editor editor = (Editor) theComboBox.getEditor().getEditorComponent();
        editor.addActionListener(this);

        // Register a listener to repond to additions to the choiceWidget
        editor.getDocument().addDocumentListener(new AdditionListener());
    }


    public void actionPerformed(ActionEvent ae) {
        JTextField editor = (JTextField) theComboBox.getEditor().getEditorComponent();
        String s = editor.getText();
        editor.getCaret().setDot(editor.getText().length());

        for(int i = 0; i < theComboBox.getItemCount(); i++) {
            Object o = theComboBox.getItemAt(i);
            String temp = o.toString();

            if(temp.equals(s)) return;
        }

       SortedComboBoxModel model = (SortedComboBoxModel) theComboBox.getModel();
       model.addElement(s);
       model.setSelectedItem(s);
    }

    /**
     * function to actually calculate the text
     * will add the remainding text (selected) to the end of a
     * given string if any matching text exists
     */
    private void calcAutoCompleteText(String s) {
    	//check each object to see if it matches
        for(int i = 0; i < theComboBox.getItemCount(); i++) {
            Object o = theComboBox.getItemAt(i);
            String temp = o.toString();

            if(temp.equals(s)) return; // text exactly matches

            if(temp.startsWith(s)) {
                theComboBox.setSelectedIndex(i);
                JTextComponent jtc = (JTextComponent) theComboBox.getEditor().getEditorComponent();

                // update the choicewidgets editors textcomponent to show all the text
                jtc.setText(temp);

                // and select the text that has been added
                // ie from the current edit position to the end of the text
                Caret c = jtc.getCaret();
                c.setDot(temp.length());
                c.moveDot(s.length());
                return;
            }
        }
    }
 
    
    /**
     * on additions this listner generates the autocomplete text
     */
    private class AdditionListener implements DocumentListener {
        public void insertUpdate(DocumentEvent event)
        {
            // must update the list model after this response thread 
            // or else there will be an exception trying to update the list model
            // within a response function.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    calcAutoCompleteText(((Editor) theComboBox.getEditor().getEditorComponent()).getText());
                }
            }
            );
        }
        
        public void changedUpdate(DocumentEvent ignore){
        }   // do nothing
        
        public void removeUpdate(DocumentEvent ignore){
        }   // do nothing

    }
}

