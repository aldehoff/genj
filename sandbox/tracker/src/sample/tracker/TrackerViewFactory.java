package sample.tracker;

import genj.util.swing.ImageIcon;
import genj.view.View;
import genj.view.ViewFactory;

public class TrackerViewFactory implements ViewFactory {

  public View createView() {
    return new TrackerView();
  }

  public ImageIcon getImage() {
    return TrackerPlugin.IMG;
  }

  public String getTitle() {
    return "Tracker";
  }

  
}
