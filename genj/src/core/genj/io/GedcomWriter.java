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
package genj.io;

import java.io.*;
import java.util.*;

import genj.Version;
import genj.gedcom.*;
import genj.util.Trackable;

/**
 * Type that knows how to write GEDCOM-data to InputStream
 */
public class GedcomWriter implements Trackable {

  private Gedcom gedcom;
  private BufferedWriter out;
  private String file;
  private String date;
  private int level;
  private int total,progress;
  private int line;
  private int entity;
  private boolean cancel=false;

  /**
   * Constructor
   * @param gedcom Gedcom object to write
   * @param out BufferedWriter to write to
   */
  public GedcomWriter(Gedcom gedcom, String file, BufferedWriter out) {

    // Remember some data
    this.gedcom=gedcom;
    this.out   =out   ;
    this.file  =file  ;

    // Initializer calculation values
    level=0;
    line =1;
    date =PropertyDate.getString(Calendar.getInstance());

    // Done
  }

  /**
   * Cancels operation
   */
  public void cancel() {
    cancel = true;
  }

  /**
   * Returns progress of save in %
   */
  public int getProgress() {
    if (progress==0) {
      return 0;
    }
    return progress*100/total;
  }

  /**
   * Returns state as explanatory string
   */
  public String getState() {
    return line+" Lines & "+entity+" Entities";
  }

  /**
   * Returns warnings of operation
   */
  public String getWarnings() {
    return "";
  }

  /**
   * Helper for writing Gedcom-line
   */
  private void line(int ldelta) {
    level+=ldelta;
  }

  /**
   * Helper for writing Gedcom-line
   * @exception IOException
   * @exception GedcomIOException
   */
  private void line(int ldelta, String tag, String value) throws IOException, GedcomIOException {
    level+=ldelta;
    line(tag,value);
  }

  /**
   * Helper for writing Gedcom-line
   * @exception IOException
   * @exception GedcomIOException
   */
  private void line(String tag, String value) throws IOException, GedcomIOException {

    // Still Operation ?
    if (cancel) {
      throw new GedcomIOException("Operation cancelled",line);
    }

    // Indent
    /*
    for (int i=0;i<level;i++)
      out.write(' ');
    */
    // Level+Tag+Value
    String l = ""+level;
    out.write(l    ,0,l    .length());
    out.write(' ');
    out.write(tag  ,0,tag  .length());
    out.write(' ');
    if (value!=null)
      out.write(value,0,value.length());
    out.newLine();

    // next
    line++;

    // Done
  }

  /**
   * Write Entities information
   * @exception IOException
   * @exception GedcomIOException
   */
  private void writeEntities(EntityList[] entities) throws IOException, GedcomIOException {

    // Loop through EntityLists
    for (int i=0;i<entities.length;i++) {
      for (int j=0;j<entities[i].getSize();j++) {
        writeEntity(entities[i].get(j));
      }
    }

    // Done
  }

  /**
   * Write Entity
   * @exception IOException
   * @exception GedcomIOException
   */
  private void writeEntity(Entity ent) throws IOException, GedcomIOException {

    // Entity line
    Property prop  = ent.getProperty();
    writeProperty("@"+ent.getId()+"@ ",prop);

    // Done
    entity++;
  }

  /**
   * Do the writing
   * @exception GedcomIOException
   */
  public boolean writeGedcom() throws GedcomIOException {

    // Prepare
    total = gedcom.getNoOfEntities();
    EntityList[] entities = gedcom.getEntities();

    // Out operation
    try {

      // Data
      writeHeader();
      writeEntities(entities);
      writeTail();

      // Close Output
      out.close();

    } catch (IOException ex) {
      throw new GedcomIOException("Error while writing",line);
    } finally {
      // .. Clear changes in gedcom
      gedcom.setUnsavedChanges(false);
    }

    // Done
    return true;
  }

  /**
   * Write Header information
   * @exception IOException
   * @exception GedcomIOException
   */
  private void writeHeader() throws IOException, GedcomIOException {

    // Header
    line("HEAD","");
    line(+1,"SOUR","GENJ");
    line(+1,"VERS",Version.getInstance().toString());
    line(+0,"NAME","GenealogyJ");
    line(-1);
    line(+0,"DEST","ANY");
    line(+0,"CHAR","IBMPC");
    line(+0,"FILE",file);
    line(+0,"DATE",date);
    line(-1);
  }

  /**
   * Write Property
   * @exception IOException
   * @exception GedcomIOException
   */
  private void writeProperty(String prefix, Property prop) throws IOException, GedcomIOException {

    // This property's value
    if (prop.isMultiLine()==Property.NO_MULTI) {
      // .. just a single line
      line(0,prefix+prop.getTag(),prop.getValue());
    } else {
      // .. more lines from iterator
      Property.LineIterator iterator = prop.getLineIterator();
      // .. just one though?
      if ( !iterator.hasMoreValues() ) {
        line(0,prefix+prop.getTag(),"");
      } else {
        line(0,prefix+prop.getTag(),iterator.getNextValue());
        line(1);
        while (iterator.hasMoreValues()) {
          line (0,"CONT",iterator.getNextValue());
        }
        line(-1);
      }
    }

    // Properties
    line(1);

    int num = prop.getNoOfProperties();
    for (int i=0;i<num;i++) {
      writeProperty("",prop.getProperty(i));
    }

    line(-1);
  }

  /**
   * Write Tail information
   * @exception IOException
   * @exception GedcomIOException
   */
  private void writeTail() throws IOException, GedcomIOException {

    // Tailer
    line("TRLR","");
  }
}
