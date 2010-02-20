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
package genj.help;

import genj.util.Resources;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

/**
 * A bridge to javax Help System
 */
class HelpWidget extends JPanel {

  private final static Logger LOG = Logger.getLogger("genj.help");
  private final static Resources RESOURCES = Resources.get(HelpWidget.class);
  
  private JEditorPane content;

  /**
   * Constructor
   */
  public HelpWidget() {
    
    // setup
    HTMLDocument doc = new HTMLDocument();
    doc.setAsynchronousLoadPriority(1);
    
    content = new JEditorPane();
    content.setBackground(Color.WHITE);
    content.setEditable(false);
    content.setEditorKit(new Kit());
    content.setDocument(doc);
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(content));

    // load
    try {
      content.setPage("http://genj.sourceforge.net/wiki/en/manual/overview?do=export_xhtmlbody");
    } catch (Throwable t) {
      
    }
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,480);
  }
  
  /**
   * our editor kit w/custom factory
   */
  private static class Kit extends HTMLEditorKit {
    
    private static Factory factory = new Factory();
    
    @Override
    public ViewFactory getViewFactory() {
      return factory;
    }
  
    private static class Factory extends HTMLFactory {
      @Override
      public View create(Element elem) {
        Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
          // patch img border=0
          if (o==HTML.Tag.IMG) {
            MutableAttributeSet atts = (MutableAttributeSet)elem.getAttributes();
            atts.addAttribute(HTML.Attribute.BORDER, "0");
            ImageView img = new ImageView(elem);
            return img;
          }
        }
        // fallback
        return super.create(elem);
      }
    }
  }
    
} //HelpWidget
