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

import genj.app.Images;
import genj.util.ActionDelegate;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImgIconConverter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A settings component 
 */
/*package*/ class SettingsWidget extends JPanel {
  
  /** members */
  private JPanel pSettings,pActions;
  private Vector vButtons = new Vector();
  private ViewWidget viewWidget = null;
  private JLabel lIdle;
  
  /**
   * Constructor
   */
  protected SettingsWidget(Resources resources, JFrame frame) {
    
    // Idle case prep
    lIdle = new JLabel(resources.getString("view.choose"),ImgIconConverter.get(Images.imgSettings),JLabel.CENTER);
    lIdle.setHorizontalTextPosition(lIdle.LEADING);
    
    // Panel for ViewSettingsWidget
    pSettings = new JPanel(new BorderLayout());

    // Panel for Actions
    JPanel pActions = new JPanel();

    ButtonHelper bh = new ButtonHelper()
      .setResources(resources)
      .setContainer(pActions)
      .addCollection(vButtons)
      .setEnabled(false);
      
    bh.create(new ActionApply());
    bh.create(new ActionReset());
    bh.removeCollection(vButtons)
      .setEnabled(true)
      .create(new ActionDelegate.ActionDisposeFrame(frame).setText("view.close"));

    // Layout
    setLayout(new BorderLayout());
    add(pSettings,"Center");
    add(pActions ,"South" );
    
    // init
    setViewWidget(null);
  }

  /**
   * Gets the View we're showing settings for
   */
  protected ViewWidget getViewWidget() {
    return viewWidget;
  }

  /**
   * Sets the ViewSettingsWidget to display
   */
  protected void setViewWidget(ViewWidget vw) {
    
    // remember
    viewWidget = vw;

    // enable buttons
    ButtonHelper.setEnabled(vButtons, viewWidget!=null);
    
    // setup content
    pSettings.removeAll();
    if (viewWidget==null) {
      pSettings.add(lIdle, BorderLayout.CENTER);
    } else {
      Component view = viewWidget.getSettings();
      if (view instanceof ApplyResetSupport) ((ApplyResetSupport)view).reset();
      pSettings.add(view, BorderLayout.CENTER);
    }
    pSettings.invalidate();
    pSettings.validate();
    pSettings.repaint();
  }

  /**
   * Applies the changes currently being done
   */
  private class ActionApply extends ActionDelegate {
    protected ActionApply() { super.setText("view.apply"); }
    protected void execute() {
      if (viewWidget.getView() instanceof ApplyResetSupport) 
        ((ApplyResetSupport)viewWidget.getView()).apply();
    }
  }

  /**
   * Resets any change being done
   */
  private class ActionReset extends ActionDelegate {
    protected ActionReset() { super.setText("view.reset"); }
    protected void execute() {
      if (viewWidget.getView() instanceof ApplyResetSupport) 
        ((ApplyResetSupport)viewWidget.getView()).reset();
    }
  }
  
} //SettingsWidget

