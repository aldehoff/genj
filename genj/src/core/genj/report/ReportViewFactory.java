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
package genj.report;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.view.ActionSupport;
import genj.view.ViewFactory;
import genj.view.ViewManager;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * The factory for the TableView
 */
public class ReportViewFactory implements ViewFactory, ActionSupport {

  /**
   * @see genj.view.ViewFactory#createView(String, Gedcom, Registry, ViewManager)
   */
  public JComponent createView(String title, Gedcom gedcom, Registry registry, ViewManager manager) {
    return new ReportView(title,gedcom,registry,manager);
  }
  
  /**
   * @see genj.view.ViewFactory#getImage()
   */
  public ImageIcon getImage() {
    return new ImageIcon(this, "View.gif");
  }
  
  /**
   * @see genj.view.ViewFactory#getName(boolean)
   */
  public String getTitle(boolean abbreviate) {
    return Resources.get(this).getString("title" + (abbreviate?".short":""));
  }

  /**
   * Reports ew offer to run on an entity
   * @see genj.view.ActionSupport#createActions(genj.gedcom.Entity, genj.view.ViewManager)
   */
  public List createActions(Entity entity, ViewManager manager) {
    return getActions(entity, manager);
  }

  /**
   * Reports we offer to be run on a gedcom file
   * @see genj.view.ActionSupport#createActions(genj.gedcom.Gedcom, genj.view.ViewManager)
   */
  public List createActions(Gedcom gedcom, ViewManager manager) {
    return getActions(gedcom, manager);
  }

  /**
   * Report we offer to be run on a property
   * @see genj.view.ActionSupport#createActions(genj.gedcom.Property, genj.view.ViewManager)
   */
  public List createActions(Property property, ViewManager manager) {
    return getActions(property, manager);
  }

  /**
   * collects actions for reports valid for given context
   */
  private List getActions(Object context, ViewManager manager) {
    List result = new ArrayList(10);
    // Look through reports
    Report[] reports = ReportLoader.getInstance().getReports();
    for (int r=0;r<reports.length;r++) {
      Report report = reports[r];
      if (report.accepts(context))
        result.add(new ActionRun(context, report));
    }
    // done
    return result;
  }

  /**
   * Run a report
   */
  private class ActionRun extends ActionDelegate {
    /** constructor */
    private ActionRun(Object context, Report report) {
      setImage(getImage());
      setText(report.getName());
    }
    /** callback */
    protected void execute() {
    }
  } //ActionRun

} //ReportViewFactory
