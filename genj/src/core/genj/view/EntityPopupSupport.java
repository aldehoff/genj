package genj.view;

import genj.gedcom.Entity;
import javax.swing.JComponent;

public interface EntityPopupSupport {

  public JComponent getEntityPopupContainer();
  public Entity getEntityAt(int x, int y);

}
