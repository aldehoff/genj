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
package genj.tree;

import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.URL;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import genj.*;
import genj.gedcom.*;
import genj.util.*;
import genj.print.*;
import genj.option.*;

/**
 * An implementation for a PrintRenderer for the TreeView
 */
public class TreeViewPrintRenderer implements PrintRenderer {

  private TreeModel       model;
  private JPanel          panelProperties;
  private float           zoom = 1.0F;
  private int             valignment = CENTER, halignment = CENTER;
  private Printer         printer;

  final static int CENTER=0, TOP=1, BOTTOM=2, LEFT=1, RIGHT=2;
  final static String valignments[] = { "renderer.vert.center", "renderer.vert.top" , "renderer.vert.bottom"};
  final static String halignments[] = { "renderer.hori.center", "renderer.hori.left", "renderer.hori.right" };

  /**
   * Constructor
   */
  public TreeViewPrintRenderer(TreeView view) {
    model = (TreeModel)view.getModel().clone();
  }

  /**
   * Returns a panel for editing this renderers properties
   */
  public JPanel getEditor(Resources resources) {

    // Already present ?
    if (panelProperties!=null) {
      return panelProperties;
    }

    // Create editable fields
    final JTextField tZoom = new JTextField(5);
    JButton bZoom = new JButton(resources.getString("renderer.zoom"));

    final JComboBox cVertical   = new JComboBox(resources.getStrings(valignments));
    cVertical  .setSelectedIndex(valignment);
    final JComboBox cHorizontal = new JComboBox(resources.getStrings(halignments));
    cHorizontal.setSelectedIndex(halignment);

    readFromUser(tZoom,cVertical,cHorizontal);

    // Prepare ActionListener
    ActionListener alistener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        readFromUser(tZoom,cVertical,cHorizontal);
      }
    };
    tZoom      .addActionListener(alistener);
    bZoom      .addActionListener(alistener);
    cVertical  .addActionListener(alistener);
    cHorizontal.addActionListener(alistener);

    // Create panel
    panelProperties = new JPanel();
    panelProperties.add(bZoom);
    panelProperties.add(tZoom);
    panelProperties.add(new JLabel(resources.getString("renderer.vert")));
    panelProperties.add(cVertical);
    panelProperties.add(new JLabel(resources.getString("renderer.hori")));
    panelProperties.add(cHorizontal);

    // Done
    return panelProperties;
  }

  /**
   * Returns size of rendering object in pixels
   */
  public Dimension getSize() {
    return new Dimension(
      (int)(model.getSize().width *zoom),
      (int)(model.getSize().height*zoom)
    );
  }

  /**
   * Helper that reads from user input components
   */
  private void readFromUser(JTextField tZoom, JComboBox cVertical, JComboBox cHorizontal) {

    // Parse Zoom
    String sZoom = tZoom.getText();
    if (!sZoom.endsWith("%")) {
      sZoom += "%";
    }

    NumberFormat format = NumberFormat.getPercentInstance();
    Number newZoom;
    try {
      newZoom = format.parse(sZoom);
      zoom = Math.max(0.05F, newZoom.floatValue());
    } catch (ParseException e) {
    }
    tZoom.setText(format.format(zoom));

    // Parse Vertical alignment
    valignment = cVertical.getSelectedIndex();

    // Parse Horizontal alignment
    halignment = cHorizontal.getSelectedIndex();

    // Tell to printer
    if (printer!=null) {
      printer.rendererChanged();
    }

    // Done
  }

  /**
   * Renders print data for page
   * @param g Graphics to render on
   * @param dimPages Dimension of all pages printed
   */
  public void renderPage(Graphics g, Dimension dimPages) {

    // Calculate offset for alignment
    int xoffset, yoffset;

    switch (halignment) {
      case TOP   :
        xoffset = 0;
        break;
      default:
        case CENTER:
        xoffset = (int)( dimPages.width /2 - getSize().width/2);
      break;
        case BOTTOM:
        xoffset = (int)( dimPages.width    - getSize().width  );
      break;
    }
    switch (valignment) {
      case LEFT  :
        yoffset = 0;
        break;
      default:
      case CENTER:
        yoffset = (int)( dimPages.height/2 - getSize().height/2);
        break;
      case RIGHT :
        yoffset = (int)( dimPages.height   - getSize().height  );
        break;
    }

    // Create Graphics for model drawing
    g.translate(xoffset,yoffset);

    final TreeGraphics tg = new TreeGraphics(
      g,
      model,
      zoom,
      false,
      false,
      false,
      true,
      false
    );

    Enumeration links = model.getLinks();
    while (links.hasMoreElements()) {
      ((Link)links.nextElement()).paint(tg,false,true);
    }

    // Done
  }

  /**
   * Rendering of preview
   */
  public void renderPreview(Graphics g, Dimension dimPreview, float zoomPreview) {

    // Calculate offset for alignment
    int xoffset, yoffset;

    switch (halignment) {
      case TOP   :
        xoffset = 0;
        break;
      default:
      case CENTER:
        xoffset = (int)( dimPreview.width /2 - (getSize().width *zoomPreview)/2);
        break;
      case BOTTOM:
        xoffset = (int)( dimPreview.width    - (getSize().width *zoomPreview)  );
        break;
    }
    switch (valignment) {
      case LEFT  :
        yoffset = 0;
        break;
      default:
      case CENTER:
        yoffset = (int)( dimPreview.height/2 - (getSize().height*zoomPreview)/2);
        break;
      case RIGHT :
        yoffset = (int)( dimPreview.height   - (getSize().height*zoomPreview)  );
        break;
    }

  // Create Graphics for model drawing
  g.translate(xoffset,yoffset);

  final TreeGraphics tg = new TreeGraphics(
    g,
    model,
    zoom * zoomPreview,
    false,
    false,
    false,
    false,
    false
  );

  Enumeration links = model.getLinks();
  while (links.hasMoreElements()) {
    ((Link)links.nextElement()).paint(tg,false,false);
  }

  // Done

  }
  /**
   * Sets the printer which is using this renderer
   */
  public void setPrinter(Printer printer) {
   this.printer = printer;
  }
}
