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

import genj.gedcom.Gedcom;
import java.io.*;
import javax.swing.*;

/**
 * Interface of a user definable GenjJ Report
 */
public interface Report {

  /**
   * Returns the author of this script
   */
  public String getAuthor();

  /**
   * Returns the version of this script
   */
  public String getVersion();

  /**
   * Returns information about this report. A report
   * has to override this method to return a <code>String</code>
   * containing information about the author, version
   * and copyright of the report.
   * @return a string containing information about the author, version
   * and copyright of the report
   */
  public String getInfo();

  /**
   * Returns the name of this report - should be localized.
   */
  public String getName();

  /**
   * Tells wether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly();

  /**
   * Returns true if this report uses STDOUT
   */
  public boolean usesStandardOut();

  /**
   * Called by GenJ to start this report's execution - has to be
   * overriden by a user defined report.
   * @param bridge a bridge to GenJ
   * @param gedcom gedcom object on which the report is supposed
   * @exception InterruptedException in case running Thread is interrupted
   */
  public boolean start(ReportBridge bridge, Gedcom gedcom);

}
