package export;

import genj.gedcom.Entity;
import genj.gedcom.Property;

public interface ExportFilter {
  
  public String name();
  
  public boolean veto(Property property);

  public boolean veto(Entity entity);
  
}
