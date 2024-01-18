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
package genj.view;

/**
 * A class wrapping the event of a context selection 
 */
public class ContextSelectionEvent {
  
  private ContextProvider provider;
  private Context context;
  private boolean isActionPerformed = false;
  
  /**
   * Constructor
   */
  public ContextSelectionEvent(Context context, ContextProvider provider) {
    this.context = context;
    this.provider = provider;
  }
  
  /**
   * Constructor
   */
  public ContextSelectionEvent(Context context, ContextProvider provider, boolean isActionPerformed) {
    this(context, provider);
    this.isActionPerformed = isActionPerformed;
  }
  
  /**
   * Read-Only Accessor
   */
  public ContextProvider getProvider() {
    return provider;
  }
  
  /**
   * Read-Only Accessor
   */
  public Context getContext() {
    return context;
  }

  /**
   * Read-Only Accessor
   */
  public boolean isActionPerformed() {
    return isActionPerformed;
  }

}
