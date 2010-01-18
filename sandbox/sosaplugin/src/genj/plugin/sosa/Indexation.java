package genj.plugin.sosa;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;

/**
 * The engine keeping track of and maintaining index information 
 */
public abstract class Indexation implements GedcomMetaListener {
  
  private Indi root = null;
  
  private boolean isEnabled = false;
  
  public boolean isEnabled() {
    return isEnabled;
  }
  
  public void setEnabled(boolean set) {
    isEnabled = set;
  }

  
  public abstract String getName();

  public Indi getRoot() {
    return root;
  }
  
  protected abstract String getTag();

  public void gedcomAfterUnitOfWork(Gedcom gedcom) {
  }

  public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
  }

  public void gedcomHeaderChanged(Gedcom gedcom) {
  }

  public void gedcomWriteLockAcquired(Gedcom gedcom) {
  }

  public void gedcomWriteLockReleased(Gedcom gedcom) {
  }

  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
  }

  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
  }

  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
  }

  public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
  }

  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
  }

}
