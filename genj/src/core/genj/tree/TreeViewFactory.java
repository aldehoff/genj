package genj.tree;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JComponent;


import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import genj.util.Registry;
import genj.view.ViewFactory;

public class TreeViewFactory implements ViewFactory {

  /**
   * @see genj.app.ViewFactory#createPrintRenderer(Component)
   */
  public PrintRenderer createPrintRenderer(Component view) {
    return new TreeViewPrintRenderer((TreeView)view);
  }

  /**
   * @see genj.app.ViewFactory#createSettingsComponent(Component)
   */
  public JComponent createSettingsComponent(Component view) {
    return new TreeViewSettings((TreeView)view);
  }

  /**
   * @see genj.app.ViewFactory#createViewComponent(Gedcom, Registry, Frame)
   */
  public Component createViewComponent(Gedcom gedcom, Registry registry, Frame frame) {
    return new TreeView(gedcom, registry, frame);
  }

}
