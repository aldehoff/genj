/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.*;
import genj.report.*;
import java.io.*;

/**
 * GenJ - Report
 * MigrateToMedias
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportMigrateToMedias implements Report {

  /** the tags to look for while migrating */
  private static final String tagsToMigrate[] = { "PHOT" };

  /** this report's version */
  public static final String VERSION = "0.1";

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    return VERSION;
  }
  
  /**
   * Returns the name of this report - should be localized.
   */
  public String getName() {
    return "Migrate to Medias";
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report migrates an existing Gedcom file to use "+
           "multimedia properties for referencing external files "+
           "like pictures. Currently only property PHOT is substituted "+
           "by the corresponding OBJE|FILE solution";
  }

  /**
   * Indication of how this reports shows information
   * to the user. Standard Out here only.
   */
  public boolean usesStandardOut() {
    return true;
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Nils Meier <nils@meiers.net>";
  }

  /**
   * Tells whether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Helper that scans entity for properties to migrate
   */
  private void analyseEntity(Entity entity, ReportBridge bridge) {

    // Get properties of entity
    if (entity.getProperty().getNoOfProperties()==0) {
      return;
    }

    // Look for properties to migrate
    for (int i=entity.getProperty().getNoOfProperties()-1;i>=0;i--) {
      analyseProperty(entity.getProperty().getProperty(i),bridge);
    }

    // Done
  }

  /**
   * Helper that checks property as candidate to migrate
   */
  private void analyseProperty(Property prop, ReportBridge bridge) {

    // Compare to tags to migrate
    for (int i=0;i<tagsToMigrate.length;i++) {
      if (prop.getTag().equals(tagsToMigrate[i])) {
        migrateProperty(prop,bridge);
      }
    }

    // Done
  }

  /**
   * Helper that migrates property
   */
  private void migrateProperty(Property prop, ReportBridge bridge) {

    // Properties with sub-properties are not migrated
    if (prop.getNoOfProperties()>0) {
      bridge.println("Skipping "+prop.getTag()+" of entity "+prop.getEntity().getId());
      return;
    }

    // Migrate it
    String value = prop.getValue();
    bridge.println("Migrating "+prop.getTag()+" of entity "+prop.getEntity().getId()+" pointing to "+value);

    // .. delete old
    Property parent = prop.getParent();
    parent.delProperty(prop);

    // .. add as new OBJE|FILE
    Property pobje = new PropertyMedia(null);
    pobje .addProperty(new PropertyFile(value));
    parent.addProperty(pobje);

    // Done
  }

  /**
   * This method actually starts this report
   */
  public boolean start(ReportBridge bridge, Gedcom gedcom) {

    // Looking for individuals and families
    bridge.println("***Looking for individuals");
    EntityList indis = gedcom.getEntities(Gedcom.INDIVIDUALS);
    if (indis.getSize()==0)
            bridge.println("- none found");
    else {
            for (int i=0;i<indis.getSize();i++)
                    analyseEntity(indis.getIndi(i),bridge);
    }
    bridge.println();

    bridge.println("***Looking for families");
    EntityList fams = gedcom.getEntities(Gedcom.FAMILIES);
    if (fams.getSize()==0)
            bridge.println("- none found");
    else {
            for (int i=0;i<fams.getSize();i++)
                    analyseEntity(fams.getFam(i),bridge);
    }
    bridge.println();

    bridge.println("***Done");

    // Done
    return true;
  }

}

