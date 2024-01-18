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

import genj.Version;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.util.Trackable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

/**
 * Type that knows how to write GEDCOM-data to InputStream
 */
public class GedcomWriter implements Trackable {

  public static final String
    UNICODE = "UNICODE",
    ASCII   = "ASCII",
    IBMPC   = "IBMPC",
    ANSEL   = "ANSEL";
    
  public static final String[] ENCODINGS = {
    IBMPC, ANSEL, ASCII, UNICODE
  };

  private Gedcom gedcom;
  private BufferedWriter out;
  private String file;
  private String date;
  private String encoding;
  private int level;
  private int total, progress;
  private int line;
  private int entity;
  private boolean cancel = false;
  private Filter[] filters = new Filter[0];

  /**
   * Constructor
   * @param ged data to write
   * @param name the logical name (header value)
   * @param stream the stream to write to
   * @param encoding either IBMPC, ASCII, UNICODE or ANSEL
   */
  public GedcomWriter(Gedcom ged, String name, OutputStream stream, String enc) throws UnsupportedEncodingException {

    // init data
    gedcom = ged;
    file = name;
    level = 0;
    line = 1;
    date = PropertyDate.getString(Calendar.getInstance());
    encoding = enc;
    out = new BufferedWriter(initOut(stream));

    // Done
  }

  /**
   * Initialize the writer we're using
   */
  private Writer initOut(OutputStream stream) throws UnsupportedEncodingException {
    // Unicode
    if (UNICODE.equals(encoding)) {
      return new OutputStreamWriter(stream, "Unicode");
    }
    // ASCII
    if (ASCII.equals(encoding)) {
      return new OutputStreamWriter(stream, "ASCII");
    }
    // ISO-8859-1
    if (IBMPC.equals(encoding)) {
      return new OutputStreamWriter(stream, "ISO-8859-1");
    }
    // ANSEL
    if (ANSEL.equals(encoding)) {
      return new AnselWriter(stream);
    }
    // not supported
    throw new UnsupportedEncodingException(encoding+" is not a supported encoding");
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
    if (progress == 0) {
      return 0;
    }
    return progress * 100 / total;
  }

  /**
   * Returns state as explanatory string
   */
  public String getState() {
    return line + " Lines & " + entity + " Entities";
  }

  /**
   * Returns warnings of operation
   */
  public String getWarnings() {
    return "";
  }

  /**
   * Sets filters to use
   */
  public void setFilters(Filter[] fs) {
    if (fs == null)
      fs = new Filter[0];
    filters = fs;
  }

  /**
   * Helper for writing Gedcom-line
   */
  private void line(int ldelta) {
    level += ldelta;
  }

  /**
   * Helper for writing Gedcom-line
   * @exception IOException
   * @exception GedcomIOException
   */
  private void line(int ldelta, String tag, String value)
    throws IOException, GedcomIOException {
    level += ldelta;
    line(tag, value);
  }

  /**
   * Helper for writing Gedcom-line
   * @exception IOException
   * @exception GedcomIOException
   */
  private void line(String tag, String value)
    throws IOException, GedcomIOException {

    // Still Operation ?
    if (cancel) {
      throw new GedcomIOException("Operation cancelled", line);
    }

    // Indent
    /*
    for (int i=0;i<level;i++)
      out.write(' ');
    */
    // Level+Tag+Value
    String l = "" + level;
    out.write(l, 0, l.length());
    out.write(' ');
    out.write(tag, 0, tag.length());
    out.write(' ');
    if (value != null)
      out.write(value, 0, value.length());
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
  private void writeEntities(Gedcom ged)
    throws IOException, GedcomIOException {

    // Loop through EntityLists
    for (int i = 0; i < ged.NUM_TYPES; i++) {
      List ents = ged.getEntities(i);
      for (int j = 0; j < ents.size(); j++) {
        writeEntity((Entity) ents.get(j));
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

    // Filters
    for (int f = 0; f < filters.length; f++) {
      if (filters[f].accept(ent) == false)
        return;
    }

    // Entity line
    Property prop = ent.getProperty();
    writeProperty("@" + ent.getId() + "@ ", prop);

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

    // Out operation
    try {

      // Data
      writeHeader();
      writeEntities(gedcom);
      writeTail();

      // Close Output
      out.close();

    } catch (IOException ex) {
      throw new GedcomIOException("Error while writing", line);
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
    line("HEAD", "");
    line(+1, "SOUR", "GENJ");
    line(+1, "VERS", Version.getInstance().toString());
    line(+0, "NAME", "GenealogyJ");
    line(-1);
    line(+0, "DEST", "ANY");
    line(+0, "CHAR", encoding);
    line(+0, "FILE", file);
    line(+0, "DATE", date);
    line(-1);
  }

  /**
   * Write Property
   * @exception IOException
   * @exception GedcomIOException
   */
  private void writeProperty(String prefix, Property prop)
    throws IOException, GedcomIOException {

    // Filters
    Entity target = null;
    if (prop instanceof PropertyXRef) {
      Property p = ((PropertyXRef) prop).getTarget();
      if (p != null)
        target = p.getEntity();
    }
    for (int f = 0; f < filters.length; f++) {
      if (filters[f].accept(prop) == false)
        return;
      if (target != null)
        if (filters[f].accept(target) == false)
          return;
    }

    // This property's value
    if (prop.isMultiLine() == Property.NO_MULTI) {
      // .. just a single line
      line(0, prefix + prop.getTag(), prop.getValue());
    } else {
      // .. more lines from iterator
      Enumeration iterator = prop.getLineIterator();
      // .. just one though?
      if (!iterator.hasMoreElements()) {
        line(0, prefix + prop.getTag(), "");
      } else {
        line(0, prefix + prop.getTag(), iterator.nextElement().toString());
        line(1);
        while (iterator.hasMoreElements()) {
          line(0, "CONT", iterator.nextElement().toString());
        }
        line(-1);
      }
    }

    // Properties
    line(1);

    int num = prop.getNoOfProperties();
    for (int i = 0; i < num; i++) {
      Property pi = prop.getProperty(i);
      if (!pi.isTransient()) {
        writeProperty("", pi);
      }
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
    line("TRLR", "");
  }
}
