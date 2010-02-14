package genj.plugin.sosa;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;

import java.util.Arrays;

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
  
  /**
   * Prepare index root
   * @param set either root to remember or null for clearing all local information
   */
  public void setRoot(Indi set) {
    root = set;
    // reindex is called elsewhere
  }
  
  /**
   * Implementation dependent - create index for current root
   * It's assume all previously set indexes are cleared already
   */
  public abstract void reindex();
  
  /**
   * Removes all index properties from all individuals
   */
  public void remove(Gedcom gedcom) {
    /* we need to search for all existing _SOSA properties to delete them */
    for (Entity entity : gedcom.getEntities(Gedcom.INDI)) {
      Indi indi = (Indi)entity;
      
      /* we delete all _SOSA properties of INDI */
      for (Property prop : Arrays.asList(indi.getProperties(getTag())))
        indi.delProperty(prop);
    }
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
