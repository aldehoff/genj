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
import genj.crypto.Enigma;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.PointInTime;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.Debug;
import genj.util.Trackable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 * Type that knows how to write GEDCOM-data to InputStream
 */
public class GedcomWriter implements Trackable {

  /** lots of state */
  private Gedcom gedcom;
  private BufferedWriter out;
  private String file;
  private String date;
  private String time;
  private int level;
  private int total, progress;
  private int line;
  private int entity;
  private boolean cancel = false;
  private Filter[] filters = new Filter[0];
  private Enigma enigma = null;

  /**
   * Constructor
   * @param ged data to write
   * @param name the logical name (header value)
   * @param stream the stream to write to
   * @param encoding either IBMPC, ASCII, UNICODE or ANSEL
   */
  public GedcomWriter(Gedcom ged, String name, OutputStream stream) {
    
    Calendar now = Calendar.getInstance();

    // init data
    gedcom = ged;
    file = name;
    level = 0;
    line = 1;
    date = PointInTime.getNow().toGedcomString();
    time = new SimpleDateFormat("HH:mm:ss").format(now.getTime());

    out = new BufferedWriter(createWriter(stream, ged.getEncoding()));

    // Done
  }

  /**
   * Initialize the writer we're using
   */
  private Writer createWriter(OutputStream stream, String encoding) {
    // Attempt encoding
    try {
      // Unicode
      if (Gedcom.UNICODE.equals(encoding))
        return new OutputStreamWriter(stream, "UTF-8");
      // ASCII
      if (Gedcom.ASCII.equals(encoding))
        return new OutputStreamWriter(stream, "ASCII");
      // ISO-8859-1
      if (Gedcom.IBMPC.equals(encoding))
        return new OutputStreamWriter(stream, "ISO-8859-1");
      // ANSEL
      if (Gedcom.ANSEL.equals(encoding))
        return new AnselWriter(stream);
    } catch (UnsupportedEncodingException e) {
    }
    // not supported
    encoding = null;
    Debug.log(Debug.WARNING, this, "Couldn't create writer for encoding " + encoding);
    return new OutputStreamWriter(stream);
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
   * Sets filters to use
   */
  public void setFilters(Filter[] fs) {
    if (fs == null)
      fs = new Filter[0];
    filters = fs;
  }

  /**
   * Helper for writing Gedcom-line
   * @exception IOException
   * @exception GedcomIOException
   */
  private void line(int ldelta, String tag, String value) throws IOException, GedcomIOException {
    level += ldelta;
    line(tag, value);
    level -= ldelta;
  }

  /**
   * Helper for writing Gedcom-line
   * @exception IOException
   * @exception GedcomIOException
   */
  private void line(String tag, String value) throws IOException, GedcomIOException {

    // Still Operation ?
    if (cancel) {
      throw new GedcomIOException("Operation cancelled", line);
    }
    
    // Level+Tag+Value
    String l = "" + level;
    out.write(l, 0, l.length());
    out.write(' ');
    out.write(tag, 0, tag.length());
    
    // 20030715 only write separating ' ' if value.length()>0 
    if (value!=null&&value.length()>0) {
      out.write(' ');
      out.write(value, 0, value.length());
    }
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
  private void writeEntities(Collection ents) throws IOException, GedcomIOException {

    // Loop through entities
    for (Iterator it=ents.iterator();it.hasNext();) {
      writeEntity((Entity)it.next());
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
    writeProperty("@" + ent.getId() + "@ ", ent);

    // Done
    entity++;
  }

  /**
   * Do the writing
   * @exception GedcomIOException
   */
  public boolean writeGedcom() throws GedcomIOException {

    Collection ents = gedcom.getEntities(); 
    total = ents.size();

    // Out operation
    try {

      // Data
      writeHeader();
      writeEntities(ents);
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
    line( 1, "SOUR", "GENJ");
    line( 2, "VERS", Version.getInstance().toString());
    line( 2, "NAME", "GenealogyJ");
    line( 2, "CORP", "Nils Meier");
    line( 3, "ADDR", "http://genj.sourceforge.net");
    line( 1, "DEST", "ANY");
    line( 1, "DATE", date);
    line( 2, "TIME", time);
    if (gedcom.getSubmitter()!=null)
      line( 1, "SUBM", '@'+gedcom.getSubmitter().getId()+'@');
    line( 1, "GEDC", "");
    line( 2, "VERS", "5.5");
    line( 2, "FORM", "Lineage-Linked");
    line( 1, "CHAR", gedcom.getEncoding());
    line( 1, "FILE", file);
    // done
  }

  /**
   * Write Property
   * @exception IOException
   * @exception GedcomIOException
   */
  private void writeProperty(String prefix, Property prop) throws IOException, GedcomIOException {

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
    boolean encrypt = prop.isPrivate();
    
    if (prop instanceof MultiLineProperty) {
      
      MultiLineProperty multi = (MultiLineProperty)prop;

      // prep an iterator to loop through lines in this property
      MultiLineProperty.Iterator lines = multi.getLineIterator();
      
      // encrypt lines value?
      if (encrypt)
        lines.setValue(encrypt(multi.getLinesValue()));
        
      // loop for write
      line(lines.getIndent(), prefix + lines.getTag(), lines.getValue());
      while (lines.next())
        line(lines.getIndent(), lines.getTag(), lines.getValue());


    } else {

      // encrypt value?
      String value = prop.getValue();
      if (encrypt)
        value = encrypt(value);
        
      
      // .. just a single line
      line(prefix + prop.getTag(), value);
      
    }

    // Sub Properties
    level++;

    int num = prop.getNoOfProperties();
    for (int i = 0; i < num; i++) {
      Property pi = prop.getProperty(i);
      if (!pi.isTransient()) {
        writeProperty("", pi);
      }
    }

    level--;
    
    // done
  }
  
  /**
   * encrypt a value
   */
  private String encrypt(String value) throws IOException {
    return value;
// FIXME encrypt call here    
//    // Make sure enigma is setup
//    if (enigma==null) {
//      String pwd = gedcom.getPassword();
//      if (pwd==null)
//        throw new IOException("Unknown Password - needed for encryption");
//      enigma = Enigma.getInstance(gedcom.getPassword());
//      if (enigma==null) 
//        throw new IOException("Encryption not available");
//    }
//    // encrypt and done
//    return enigma.encrypt(value);
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
  
  
} //GedcomWriter
