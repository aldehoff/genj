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
package genj.edit.actions;

import genj.gedcom.Gedcom;
import genj.gedcom.Submission;
import genj.util.ActionDelegate;

/**
 * Set the submission record of a gedcom file
 */
public class SetSubmission extends ActionDelegate {

    /** the submission */
    private Submission submission;
    
    /**
     * Constructor
     */
    public SetSubmission(Submission sub) {
      submission = sub;
      Gedcom ged = submission.getGedcom();
      setImage(Gedcom.getImage(Gedcom.SUBMISSIONS));
      setText(AbstractChange.resources.getString("submission", ged.getName()));
      if (ged.getSubmission()==submission) setEnabled(false);
    }
    
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      submission.getGedcom().setSubmission(submission);
    }

} //SetSubmission

