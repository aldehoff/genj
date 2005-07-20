/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportTinyTafel extends Report {
  
  /**
   * Overriden image - we're using the provided FO image 
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  /**
   * While we generate information on stdout it's not really
   * necessary because we're returning a Document
   * that is handled by the UI anyways
   */
  public boolean usesStandardOut() {
    return true;
  }

  /**
   * One of the report's entry point
   */
  public void start(Gedcom gedcom) {
    start(gedcom, gedcom.getEntities(Gedcom.INDI));
  }

  /**
   * One of the report's entry point
   */
  public void start(Indi[] indis) {
    start(indis[0].getGedcom(), Arrays.asList(indis));
  }

  /**
   * Our main logic
   */
  private void start(Gedcom gedcom, Collection indis) {
    
    System.out.println("This is not implemented yet");
    //PropertyPlace.getAllJurisdictions()
    
    // done
  }
  
  /**
   * Iterate over individuals
   */
  private void iterate(Collection indis) {
    for (Iterator it = indis.iterator(); it.hasNext();) {
      Indi indi = (Indi) it.next();
      System.out.println(indi);
    }
  }

} //ReportHTMLSheets
