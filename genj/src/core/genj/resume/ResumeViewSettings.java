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
package genj.resume;

import genj.gedcom.Gedcom;
import genj.view.ApplyResetSupport;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * The settings editor for the ResumeView
 */
public class ResumeViewSettings extends JPanel implements ApplyResetSupport {
  
  /** a drop-down for available entities */
  private JComboBox dropEntities = new JComboBox();
  
  /** a text-area for html */
  private JTextArea textHtml = new JTextArea();
  
  /** the resume */
  private ResumeView resumeView; 
  
  /** the current entity type */
  private int entityType = Gedcom.INDIVIDUALS;
  
  /**
   * Constructor
   */
  public ResumeViewSettings(ResumeView view) {
    
    // keep the view
    resumeView = view;
    
    // get entities
    for (int i=Gedcom.FIRST_ETYPE;i<=Gedcom.LAST_ETYPE;i++) {
      dropEntities.addItem(Gedcom.getNameFor(i,true));
    }
    
    // do the layout
    setLayout(new BorderLayout());
    add(dropEntities, BorderLayout.NORTH);
    add(new JScrollPane(textHtml), BorderLayout.CENTER);    
    
    // done
  }

  /**
   * @see genj.app.ViewSettingsWidget#apply()
   */
  public void apply() {
    resumeView.setHtml(entityType, textHtml.getText());
  }

  /**
   * @see genj.app.ViewSettingsWidget#reset()
   */
  public void reset() {
    textHtml.setText(resumeView.getHtml(entityType));
  }

}
