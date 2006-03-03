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
package genj.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * An updating widget showing memory consumption
 */
public class HeapStatusWidget extends JProgressBar {
  
  private final static NumberFormat FORMAT = new DecimalFormat("0.0");

  /**
   * constructor
   */
  public HeapStatusWidget() {
    super(0,100);
    setValue(0);
    setBorderPainted(false);
    setStringPainted(true);
    new Timer(3000, new Update()).start();
  }
  
  /** update status */
  private class Update implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      
      Runtime r = Runtime.getRuntime();
      long max = r.maxMemory();
      long used = r.totalMemory()-r.freeMemory();
      int percent = (int)(used*100/max);
      setValue(percent);
      setString(FORMAT.format(used/1000000D)+"MB ("+percent+"%)");

    }
  }
  
}
