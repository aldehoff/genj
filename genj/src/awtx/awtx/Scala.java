/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * A lightweight choice for choosing values of a scala from 0% to 100%
 */
public class Scala extends Container {

  public final static int
    PREVIEW = 0,
    BAR     = 1;

  private Color[] colors = { Color.white, Color.black };
  private float preview;
  private float value = 0.5F;
  private int gap = 2;
  private String command="SCALA";
  private String prefix = null;
  private Vector listeners = new Vector();
  private boolean isEditable = true;

  /**
   * Our inner class for mouse handling
   */
  private class MouseHandler extends MouseAdapter implements MouseMotionListener {

    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {
      if (!isEditable) return;
      preview = getRatioFor(e.getX());
      repaint();
    }
    public void mouseEntered(MouseEvent e) {
      if (!isEditable) return;
      preview = value;
    }
    public void mouseExited(MouseEvent e) {
      if (!isEditable) return;
      preview = -1;
      repaint();
    }
    public void mouseClicked(MouseEvent e) {
      // NM 19990722 Check wether this event came from first mouse button
      // Maybe that helps Francois who reported a change in FTree zoom-scala
      // without a mouse click but moving above control
      if ( (!isEditable) || ((e.getModifiers()&e.BUTTON1_MASK)==0) )
      return;
      preview = -1;
      if (e.getClickCount()>1)
      value = 1F;
      else
      value = getRatioFor(e.getX());
      repaint();
      fireActionPerformed();
    }
    // EOC
  }

  /**
   * Constructor
   */
  public Scala() {
    // Some listening
    MouseHandler mhandler = new MouseHandler();
    addMouseMotionListener(mhandler);
    addMouseListener(mhandler);
    // Done
  }

  /**
   * Adds another ActionListener to this scala
   */
  public void addActionListener(ActionListener listener) {
    listeners.addElement(listener);
  }

  /**
   * Fires a ActionPerformed to all attached listeners
   */
  protected void fireActionPerformed() {
    ActionEvent ev = new ActionEvent(this,ActionEvent.ACTION_LAST+400,command);
    Enumeration e = listeners.elements();
    while (e.hasMoreElements()) {
      ((ActionListener)e.nextElement()).actionPerformed(ev);
    }
  }

  /**
   * Our minimum Size
   */
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * Our preferred Size
   */
  public Dimension getPreferredSize() {

    FontMetrics fm = getFontMetrics(getFont());

    return new Dimension(
      fm.charWidth('x')*7,
      fm.getHeight()+3+3
    );
  }

  /**
   * Translates given pixel position in %
   */
  private float getRatioFor(int pixel) {

    return Math.min(1,Math.max(0,
      pixel / ((float)getSize().width-gap-gap-2)
    ));

  }

  /**
   * A getter for the actual value
   */
  public float getValue() {
    return value;
  }

  /**
   * Painting
   */
  public void paint(Graphics g) {

    // Prepare some parms
    Dimension size = getSize();

    int w = size.width,
      h = size.height;

    FontMetrics fm = g.getFontMetrics();

    // Paint background
    g.setColor(Color.lightGray);
    g.fillRect(0,0,w+1,h+1);

    // Paint border
    g.setColor(Color.white);

    g.drawLine(gap,h-gap,w-gap,h-gap);
    g.drawLine(w-gap,gap,w-gap,h-gap);

    g.setColor(Color.darkGray);

    g.drawLine(gap,gap,w-gap,gap);
    g.drawLine(gap,gap,gap,h-gap);

    // Paint text
    float value = (preview>0) ? preview : this.value;

    g.setColor(Color.black);

    String s = ((int)(value*100))+"%";
    if (prefix!=null)
      s = prefix+" "+s;

    g.drawString(
      s,
      (size.width-fm.stringWidth(s))/2,
      (size.height+fm.getHeight())/2+-fm.getMaxDescent()
    );

    // Paint BAR
    if (preview>0) {
      g.setColor(colors[PREVIEW]);
      g.setXORMode(Color.lightGray);
    } else {
      g.setColor(colors[BAR]);
      g.setXORMode(Color.lightGray);
    }

    g.fillRect(
      gap+2,
      gap+2,
      (int)(value*(size.width-gap-2-gap-2))+1,
      h-gap-2-gap-2+1
    );

    // Done
  }

  /**
   * removes one of the ActionListeners from this scala
   */
  public void removeActionListener(ActionListener listener) {
    listeners.removeElement(listener);
  }

  /**
   * Sets the actionCommand for this component
   */
  public void setActionCommand(String command) {
    this.command = command;
  }

  /**
   * Sets a color
   */
  public void setColor(int which, Color color) {
    colors[which]=color;
  }

  /**
   * Sets wether editing is allowed
   */
  public void setEditable(boolean set) {
    isEditable=set;
  }

  /**
   * Sets the prefix to be shown in front of value
   */
  public void setPrefix(String set) {
    prefix=set;
    repaint();
  }

  /**
   * A setter for the actual value
   */
  public void setValue(float newValue) {
    value = Math.min(1,Math.max(0,newValue));
    repaint();
  }

}

