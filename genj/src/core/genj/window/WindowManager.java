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


import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

/**
 * The manager for window creation/handling
 */
public interface WindowManager {
  
  /** message types*/
  public static final int  
    ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE,
    INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE,
    WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE,
    QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE,
    PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;
  
  /**
   * Opens a frame
   * @param key a unique key 
   * @param title text for titlebar
   * @param image image for titlebar
   * @param content component to be shown in frame
   * @param menu menubar to be shown in frame
   * @param close code to run on closing (frame won't close automatically if != null)
   * @return key 
   */
  public String openFrame(String key, String title, ImageIcon image, JComponent content, JMenuBar menu, Runnable close);

  /**
   * Opens a frame with one close button
   * @param key a unique key 
   * @param title text for titlebar
   * @param image image for titlebar
   * @param content component to be shown in frame
   * @param menu menubar to be shown in frame
   * @param action a single action to close the frame
   * @return key 
   */
  public String openFrame(String key, String title, ImageIcon image, JComponent content, Action action);

  /**
   * Opens a dialog containing a custom component
   * @param key a unique key 
   * @param title text for titlebar
   * @param messageType
   * @param content component to be shown in dialog
   * @param actions text labels for buttons that will close dialog
   * @param owner the 'owning' component
   * @return index of actions choosen or -1 
   */
  public int openDialog(String key, String title, int messageType, JComponent content, Action[] actions, Component owner);

  /**
   * Opens a dialog containing several stacked custom components
   * @param key a unique key 
   * @param title text for titlebar
   * @param image image for titlebar
   * @param content components to be shown in a vertical box
   * @param actions text labels for buttons that will close dialog
   * @param owner the 'owning' component
   * @return index of actions choosen or -1 
   */
  public int openDialog(String key, String title, int messageType, JComponent[] content, Action[] actions, Component owner);

  /**
   * Opens a dialog with a simple text message
   * @param key a unique key 
   * @param title text for titlebar
   * @param image image for titlebar
   * @param txt text to show in a scroll text area
   * @param actions resoved into option buttons closing dialog
   * @param owner the 'owning' component
   * @return index of actions choosen or -1 
   */
  public int openDialog(String key, String title,  int messageType, String txt, Action[] actions, Component owner);
  
  /**
   * Opens a dialog prompting the user for a simple text value
   * @param key a unique key 
   * @param title text for titlebar
   * @param image image for titlebar
   * @param txt text to show in a scroll text area
   * @param value initial value to present
   * @param owner the 'owning' component
   * @return entered text or null
   */
  public String openDialog(String key, String title,  int messageType, String txt, String value, Component owner);
  
  /**
   * Opens a non-modal dialog 
   * @param key a unique key 
   * @param title text for titlebar
   * @param image image for titlebar
   * @param content component to be shown in dialog
   * @param owner the 'owning' component
   * @return key 
   */
  public String openNonModalDialog(String key, String title,  int messageType, JComponent content, Action[] actions, Component owner);
  
  /**
   * Close dialog/frame 
   * @param key the dialog/frame's key
   */
  public void close(String key);

  /**
   * Close all dialogs/frames 
   */
  public void closeAll();
  
  /**
   * Return root components of all heavyweight dialogs/frames
   */
  public List getRootComponents();
  
  /**
   * Return the content of a dialog/frame 
   * @param key the dialog/frame's key 
   */
  public JComponent getContent(String key);
  
  /**
   * Makes sure the dialog/frame is visible
   * @param key the dialog/frame's key 
   * @return success or no valid key supplied
   */
  public boolean show(String key);
  
 
} //WindowManager