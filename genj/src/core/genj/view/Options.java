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

import genj.option.CustomOption;
import genj.option.OptionProvider;
import genj.option.PropertyOption;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ScreenResolutionScale;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.Point;
import java.awt.Toolkit;
import java.util.List;

/**
 * View options
 */
public class Options extends OptionProvider {

  /** singleton */
  private final static Options instance = new Options();
  
  /** resources */
  private static Resources resources;
  
  /** the current screen resolution */
  private Point dpi = new Point( 
    Toolkit.getDefaultToolkit().getScreenResolution(),
    Toolkit.getDefaultToolkit().getScreenResolution()
  );
  
  /**
   * Instance access
   */
  public static Options getInstance() {
    return instance;
  }
  
  /**
   * Lazy resources
   */
  private Resources getResources() {
    if (resources==null)
      resources = Resources.get(this);
    return resources;
  }
  
  /** 
   * Accessor - DPI
   */
  public Point getDPI() {
    return dpi;
  }

  /** 
   * Provider callback 
   */
  public List getOptions() {
    List result = PropertyOption.introspect(getInstance());
    result.add(new ScreenResolutionOption());
    return result;
  }
  
  /** 
   * Option for Screen Resolution
   */
  private class ScreenResolutionOption extends CustomOption {

    /** callback - user readble name */
    public String getName() {
      return getResources().getString("option.screenresolution");
    }

    /** callback - persist */
    public void persist(Registry registry) {
      registry.put("dpi", dpi);
    }

    /** callback - restore */
    public void restore(Registry registry) {
      Point set = registry.get("dpi", (Point)null);
      if (set!=null)
        dpi = set;
    }

    /** callback - edit option */
    protected void edit() {
      ScreenResolutionScale scale = new ScreenResolutionScale(dpi);
      int rc = widget.getWindowManager().openDialog(null, getName(), WindowManager.IMG_QUESTION, scale, CloseWindow.OKandCANCEL(), widget);
      if (rc==0)
        dpi = scale.getDPI();
    }

  } //ScreenResolutionOption

} //Options
