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

import genj.app.App;
import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Selection;
import genj.gedcom.GedcomListener;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A rendering component showing a resume of the currently
 * selected entity
 */
public class ResumeView extends JComponent {
  
  /**
   * Constructor
   */
  public ResumeView(Gedcom useGedcom, Registry useRegistry, Frame useFrame) {
    // done    
    useGedcom.addListener(new GedcomConnector());
  }
  
  /**
   * @see javax.swing.JComponent#paintComponent(Graphics)
   */
  protected void paintComponent(Graphics g) {
    String txt = "Coming Soon!";
    Dimension d = getSize();
    int 
      h = g.getFontMetrics().getHeight(),
      w = g.getFontMetrics().stringWidth(txt);
    g.drawString(txt, (d.width-w)/2, (d.height-h)/2);
  }
  
  /** 
   * Our connection to the Gedcom
   */
  private class GedcomConnector implements GedcomListener {
      /**
     * @see genj.gedcom.GedcomListener#handleChange(Change)
     */
    public void handleChange(Change change) {
      repaint();
    }
    /**
     * @see genj.gedcom.GedcomListener#handleClose(Gedcom)
     */
    public void handleClose(Gedcom which) {
    }
    /**
     * @see genj.gedcom.GedcomListener#handleSelection(Selection)
     */
    public void handleSelection(Selection selection) {
      Entity e = selection.getEntity();
      Debug.log(Debug.INFO, ResumeView.this, "Switching resume to "+e);
    }
  } //GedcomConnector

} //ResumeView
