package genj.util.swing;

import javax.swing.DefaultComboBoxModel;

import java.util.Arrays;
import java.util.Vector;
import java.util.Iterator;


public class SortedComboBoxModel extends DefaultComboBoxModel {
    
    public SortedComboBoxModel() {
        super();
    }

    public SortedComboBoxModel(final Object items[]) {
        super();
        for (int i = 0; i < items.length;i++) addElement(items[i]);
    }

    public SortedComboBoxModel(Vector v) {
    	Iterator i = v.iterator();
        while(i.hasNext()) addElement(i.next());
    }

    /**
     * Adds and element in the correct position (sorted)
     */
    public void addElement(Object anObject) {
    	
        // Binary search for where to insert - assumes sorted array
        int placeToInsert = Arrays.binarySearch(getContents(), anObject);
        
        if(placeToInsert >= 0) return; //already in list
        
        placeToInsert = -(placeToInsert + 1);
        if(placeToInsert >= getSize()) super.addElement(anObject); // add to end
        else insertElementAt(anObject,placeToInsert);
    }
    
    private Object[] getContents() {
    	Object[] result = new Object[getSize()];
    	for(int i = 0; i < getSize(); i++) result[i] = getElementAt(i);
    	return result;
    }
}
