package sample.tracker;

import genj.util.swing.ImageIcon;
import genj.view.View;
import genj.view.ViewFactory;

public class TrackerViewFactory implements ViewFactory {

  @Override
  public View createView() {
    return new TrackerView();
  }

  @Override
  public ImageIcon getImage() {
    return TrackerPlugin.IMG;
  }

  @Override
  public String getTitle() {
    return "Tracker";
  }

  
}
