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

import genj.view.ApplyResetSupport;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;


/**
 * The settings editor for the ResumeView
 */
public class ResumeViewSettings extends JPanel implements ApplyResetSupport {
  
  /** a drop-down for available entities */
  private JComboBox dropEntities = new JComboBox();
  
  /** a text-area for html */
  private JTextArea textHtml = new JTextArea();
  
  /**
   * Constructor
   */
  public ResumeViewSettings(ResumeView resumeView) {
    super(new BorderLayout());

    // prepare the drop-down with entity types    
    add(dropEntities, BorderLayout.NORTH);

    // and the html
    add(textHtml, BorderLayout.CENTER);    
    
    // done
  }

  /**
   * @see genj.app.ViewSettingsWidget#apply()
   */
  public void apply() {
  }

  /**
   * @see genj.app.ViewSettingsWidget#reset()
   */
  public void reset() {
  }

}
