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
package genj.window;

import java.awt.Dimension;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;

/**
 * The manager for window creation/handling
 */
public interface WindowManager {
  
  public void openFrame(String key, String title, ImageIcon image, Dimension dimension, JComponent content, JMenuBar menu, Runnable onClosing, Runnable onClose);

  public void closeFrame(String key);

  public void closeAllFrames();
  
  public List getRootComponents();
  
  public JComponent getRootComponent(String key);
  
  public boolean isFrame(String key);

} //WindowManager