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

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

/**
 * The manager for window creation/handling
 */
public interface WindowManager {
  
  /** predefined images */
  public final static Icon
    IMG_ERROR = UIManager.getIcon("OptionPane.errorIcon"),
    IMG_INFORMATION = UIManager.getIcon("OptionPane.informationIcon"),
    IMG_WARNING = UIManager.getIcon("OptionPane.warningIcon"),
    IMG_QUESTION = UIManager.getIcon("OptionPane.questionIcon");

  /** predefined options */
  public final static String
    OPTION_YES = UIManager.getString("OptionPane.yesButtonText"),
    OPTION_NO  = UIManager.getString("OptionPane.noButtonText" ),
    OPTION_OK  = UIManager.getString("OptionPane.okButtonText" ),
    OPTION_CANCEL = UIManager.getString("OptionPane.cancelButtonText");

  /** predefined option groups */
  public final static String[]
    OPTIONS_YES_NO    = new String[]{ OPTION_YES, OPTION_NO },
    OPTIONS_OK_CANCEL = new String[]{ OPTION_OK, OPTION_CANCEL },
    OPTIONS_OK        = new String[]{ OPTION_OK };
    
  public void openFrame(String key, String title, ImageIcon image, JComponent content, JMenuBar menu, Runnable onClosing, Runnable onClose);
  
  public int openDialog(String key, String title, Icon image, JComponent content, String[] options, JComponent owner, Runnable onClosing, Runnable onClose);

  public int openDialog(String key, String title, Icon image, JComponent content, String[] options, JComponent owner);

  public int openDialog(String key, String title, Icon image, JComponent[] content, String[] options, JComponent owner);

  public int openDialog(String key, String title, Icon img, String txt, String[] options, JComponent owner);
  
  public String openDialog(String key, String title, Icon img, String txt, String value, JComponent owner);
  
  public void close(String key);

  public void closeAll();
  
  public List getRootComponents();
  
  public JComponent getRootComponent(String key);
  
  public boolean isOpen(String key);

} //WindowManager