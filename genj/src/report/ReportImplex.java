/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2003
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

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * GenJ - ReportImplex
 *
 * This report computes the implex factor.
 */
public class ReportImplex extends Report {
  private static final String AUTHOR = "Thierry Hardy";
  private static final String VERSION = "0.2";
  private static final String FIELD_SEPARATOR =  " - ";
  private static final String LINE_SEPARATOR =  "---------------------------------------------------------------------------";
  private final int MAX_LEVEL = 100;

  private int[] iBasicCount = new int[MAX_LEVEL];
  private int[] iDiffCount = new int[MAX_LEVEL];
  private HashSet setIndi = new HashSet();
  private HashSet setSharedAncestor = new HashSet();

  /**
   * Returns the author of this report.
   */
  public String getAuthor() {
    return AUTHOR;
  }

  /**
   * Returns the version of this report.
   */
  public String getVersion() {
    return VERSION;
  }

  /**
   * Returns the name of this report.
   */
  public String getName() {
    return i18n("name");
  }

  /**
   * Returns information about this report.
   */
  public String getInfo() {
    return i18n("info");
  }


  /**
   * @see genj.report.Report#accepts(java.lang.Object)
   */
  public String accepts(Object context) {
    // we accept GEDCOM or Individuals
    return context instanceof Indi || context instanceof Gedcom ? getName() : null;
  }

  /**
   * This method actually starts this report
   */
  public void start(Object context) {
    Indi indi;

    // The script is started with a right click on an individual with the contextual submenu
    if (context instanceof Indi) {
      indi = (Indi)context;
    } else {
    // No one has been given, we ask the user to select someone in the tree for analysis
      Gedcom gedcom=(Gedcom)context;
      indi = (Indi)getEntityFromUser (
         i18n("select_individual"),     // msg in resource file
         gedcom,
         Gedcom.INDIVIDUALS,
         "INDI:NAME"
         );
    }

    if (indi == null)
      return;

    // Initialize statistics if the report is executed several times
    clearStats();

    // Print header
    println(i18n("info"));
    println();
    println(i18n("root_individual"));
    println(LINE_SEPARATOR);
    println(getIndiDescription(indi));
    println();
    println(i18n("header_shared_ancestors"));
    println(LINE_SEPARATOR);

    // Initialize the first generation with the selected individual
    List listIndi = new ArrayList();
    listIndi.add(indi);

    // Compute statistics by generations
    int iLevel = 1;
    while (!listIndi.isEmpty() && iLevel < MAX_LEVEL) {
      List listParent = new ArrayList();
      computeGeneration(iLevel, listIndi, listParent);
      listIndi = listParent;
      iLevel++;
    }

    // Print statistics
    printStats(iLevel);
  }

  /**
   * Initialize statistics.
   */
  private void clearStats() {
    for (int i = 0; i < iBasicCount.length; i++) {
      iBasicCount[i] = 0;
      iDiffCount[i] = 0;
    }

    setIndi.clear();
    setSharedAncestor.clear();
  }

  /**
   * Computes statistics for the specified generation.
   * @param iLevel       Current generation level.   *
   * @param listIndi     Individuals of a generation.
   * @param listParent   [return] Individuals of the next generation.
   */
  private void computeGeneration(int iLevel, List listIndi, List listParent) {
    // Scan individual of the generation
    Iterator itr = listIndi.iterator();
    while (itr.hasNext()) {
      Indi indi = (Indi) itr.next();

      // Get ancestor ID and search it in the list
      String strID = indi.getId();
      if (setIndi.contains(strID)) {
        // This is a shared ancestor
        printSharedAncestor(indi);
      }
      else {
        // This is a new ancestor
        setIndi.add(strID);
        iDiffCount[iLevel]++;
      }

      // Count this ancestor in all case
      iBasicCount[iLevel]++;

      // Get parents
      Fam famc = indi.getFamc();
      if (famc != null) {
        // Get mother
        Indi indiWife = famc.getWife();
        if (indiWife != null)
          listParent.add(indiWife);

        // Get father
        Indi indiHusband = famc.getHusband();
        if (indiHusband != null)
          listParent.add(indiHusband);
      }
    }
  }

  /**
   * Returns an individual description.
   */
  private String getIndiDescription(Indi indi) {
    StringBuffer str = new StringBuffer();
    str.append(indi.getId() + FIELD_SEPARATOR + indi.getName());

    String strBirth = indi.getBirthAsString();
    if (strBirth.length() != 0)
      str.append(FIELD_SEPARATOR + i18n("prefix_born") + strBirth);

    String strDeath = indi.getDeathAsString();
    if (strDeath.length() != 0)
      str.append(FIELD_SEPARATOR + i18n("prefix_death") + strDeath);

    return str.toString();
  }

  /**
   * Print a shared ancestor.
   */
  private void printSharedAncestor(Indi indi) {
    // Check if this indivual has already been listed
    String strID = indi.getId();
    if (setSharedAncestor.contains(strID))
      return;

    // Print individual description
    println(getIndiDescription(indi));

    // Add individual and its ancestors to the list
    addSharedAncestor(indi);
  }

  /**
   * Add an individual and all its ancestors in the shared ancestor list.
   * @param indi   Shared ancestor.
   */
  private void addSharedAncestor(Indi indi) {
    if (indi == null)
      return;

    // Add individual to the list
    String strID = indi.getId();
    setSharedAncestor.add(strID);

    // Add parents to the list
    Fam famc = indi.getFamc();
    if (famc != null) {
      addSharedAncestor(famc.getWife());
      addSharedAncestor(famc.getHusband());
    }
  }

  /**
   * Print statistics by generations.
   */
  private void printStats(int iLevel) {
    int iBasicCumul = 0;
    int iDiffCumul = 0;
    int iPossibleCumul = 0;

    // Print header
    println();
    println(i18n("header_implex_stats"));
    println(LINE_SEPARATOR);

    // Iteration on levels
    for (int i = 1; i < iLevel; i++) {
      // Compute possible
      int iPossibleCount = (int) Math.pow(2.0f, i - 1);

      // Compute cumuls
      iPossibleCumul += iPossibleCount;
      iBasicCumul += iBasicCount[i];
      iDiffCumul += iDiffCount[i];

      // Compute coverage
      double dCoverage = Math.round(10000 * iBasicCount[i] / iPossibleCount) / 100;
      double dAllCoverage = Math.round(10000 * iBasicCumul / iPossibleCumul) / 100;

      // Compute implex
      double dImplex = 0;
      if (iBasicCumul != 0)
        dImplex = Math.round(10000 * (iBasicCumul - iDiffCumul) / iBasicCumul) / 100;

        // Display line
      println(align(i, 6) +
              align(iPossibleCount, 11) +
              align(iBasicCount[i], 11) +
              align(dCoverage + "%", 11) +
              align(iBasicCumul, 11) +
              align(dAllCoverage + "%", 11) +
              align(iDiffCount[i], 11) +
              align(dImplex + "%", 11));
    }
  }
}
