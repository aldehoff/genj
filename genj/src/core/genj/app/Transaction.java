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
package genj.app;

import java.awt.*;
import javax.swing.*;

import genj.util.ImgIcon;

/**
 * Transaction - a transactional controller for a TransactionPanel
 */
public interface Transaction {

  public static final int
    NEXT   = 1,
    PREV   = 2,
    CANCEL = 4,
    OK     = 8,
    DONE   =16;

  /**
   * Ends this transaction - this action can't be ignored
   */
  public void cancel();

  /**
   * Ends this transaction - this action can't be ignored by returning false
   */
  public boolean done();

  /**
   * Returns the currently available actions for this transaction
   * at current state
   */
  public int getActions();

  /**
   * Returns an image that represents this transaction's state
   */
  public ImgIcon getImage();

  /**
   * Returns the current visualization for this transaction
   */
  public JPanel getPanel();

  /**
   * Moves one further in transaction - this action can be ignored by returning false
   */
  public boolean next();

  /**
   * Initiates last step of this transaction - this action can be ignored by returning false
   */
  public boolean ok();

  /**
   * Moves one back in transaction - this action can be ignored by returning false
   */
  public boolean prev();

  /**
   * Set the TransactionPanel to use
   */
  public void setPanel(TransactionPanel set);

  /**
   * Starts the transaction
   */
  public void start();

}
