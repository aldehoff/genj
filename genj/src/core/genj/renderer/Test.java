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
package genj.renderer;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyAddress;
import genj.gedcom.PropertyCity;
import genj.gedcom.PropertyContinuation;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyPostalCode;
import genj.gedcom.TagPath;
import genj.util.Origin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 */
public class Test {
  
  /**
   * 
   */
  public static void main(String[] args) {
    
    String html = 
      "<p>Individual <font color=blue><b><prop path=INDI></b></font></p>"+
      "<table>"+
       "<tr valign=top><td>"+
       "<table>"+
        "<tr><td>Name &nbsp;&nbsp;&nbsp;</td><td><i><prop path=INDI:NAME></i></td></tr>"+
        "<tr><td>Sex  </td><td><prop path=INDI:SEX img=yes txt=no w=16 h=16></td></tr>"+
        "<tr><td>Birth</td><td><prop path=INDI:BIRT:DATE img=yes>, <u><prop path=INDI:BIRT:PLAC></u></td></tr>"+
        "<tr><td>Addr </td><td><prop path=INDI:ADDR><br><prop path=INDI:RESI:ADDR:CITY><br><prop path=INDI:RESI:POST></u></td></tr>"+
       "</table>"+
       "</td><td>"+
        "<prop path=INDI:FILE>"+
       "</td></tr>"+
      "</table>"+
      "";

    final EntityRenderer renderer = new EntityRenderer(html);

    final Indi nils, leslie;
    try {
      Gedcom gedcom = new Gedcom(Origin.create(new File("../GenJ/gedcom/example.ged").toURL()));
      
      nils = gedcom.createIndi("Meier", "Nils", 1);
      ((PropertyDate)nils.getProperty(new TagPath("INDI:BIRT:DATE"),false)).setValue("25 MAY 1970");
      ((PropertyPlace)nils.getProperty(new TagPath("INDI:BIRT:PLAC"),false)).setValue("Rendsburg");
      nils.getProperty().addProperty(new PropertyFile("meiern.jpg"));
      PropertyAddress addr = (PropertyAddress)nils.getProperty(new TagPath("INDI:ADDR"),false);
      addr.setValue("788 Harrison Street");
      addr.addProperty(new PropertyContinuation("Apt 829")); 
      nils.getProperty().addProperty(addr);
     
      leslie = gedcom.createIndi("Jansson", "Leslie Marie", 2);
      ((PropertyDate)leslie.getProperty(new TagPath("INDI:BIRT:DATE"),false)).setValue("14 MAR 1969");
      ((PropertyPlace)leslie.getProperty(new TagPath("INDI:BIRT:PLAC"),false)).setValue("Campbell River");
      
    } catch (Throwable t) {
      return;
    }
    
    final JFrame frame = new JFrame("html.Test");

    JComponent content = new JComponent() {
      protected void paintComponent(Graphics g) {
        Rectangle bounds = getBounds();
        g.setColor(Color.white);
        g.fillRect(0,0,bounds.width,bounds.height);
        g.setColor(Color.black);
        renderer.setEntity(nils);
        renderer.render(g, new Dimension(bounds.width,bounds.height/2));
        g.translate(0,bounds.height/2);
        renderer.setEntity(leslie);
        renderer.render(g, new Dimension(bounds.width,bounds.height/2));
      }
    };
    
    JPanel p = new JPanel(new BorderLayout());
    p.add(content,"Center");
    
    frame.getContentPane().add(p);
    frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
    frame.setBounds(0,0,512,512);
    frame.setVisible(true);
    
  }
  
} //Test
