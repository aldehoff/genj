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
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.time.PointInTime;
import genj.util.Debug;
import genj.util.Trackable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
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
  private String password;
  private String encoding;

  /**
   * Constructor
   * @param ged data to write
   * @param name the logical name (header value)
   * @param enc either IBMPC, ASCII, UNICODE or ANSEL
   * @param stream the stream to write to
   */
  public GedcomWriter(Gedcom ged, String name, String enc, OutputStream stream) {
    
    Calendar now = Calendar.getInstance();

    // init data
    gedcom = ged;
    password = gedcom.getPassword();
    encoding = enc;
    file = name;
    level = 0;
    line = 1;
    date = PointInTime.getNow().getValue();
    time = new SimpleDateFormat("HH:mm:ss").format(now.getTime());

    out = new BufferedWriter(new OutputStreamWriter(stream, getCharset(enc)));

    // Done
  }

  /**
   * Create the charset we're using for out
   */
  private Charset getCharset(String encoding) {

    // Attempt encoding
    try {
      // Unicode
      if (Gedcom.UNICODE.equals(encoding))
        return Charset.forName("UTF-8");
      // ASCII
      if (Gedcom.ASCII.equals(encoding))
        return Charset.forName("ASCII");
      // Latin1 (ISO-8859-1)
      if (Gedcom.LATIN1.equals(encoding))
        return Charset.forName("ISO-8859-1");
      // ANSI (Windows-1252)
      if (Gedcom.ANSI.equals(encoding))
        return Charset.forName("Windows-1252");
    } catch (UnsupportedCharsetException e) {
    }

    // ANSEL (in any case)
    if (!Gedcom.ANSEL.equals(encoding)) {
      encoding = null;
      Debug.log(Debug.WARNING, this, "Couldn't resolve charset for encoding " + encoding);
    }
    return new AnselCharset();
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
   * Sets password to use
   */
  public void setPassword(String pwd) {
    password = pwd;
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

    // Entity line (might be missing xref in case of custom records)
    String xref = ent.getId();
    if (xref.length()>0) 
      xref = "@" + xref + "@ ";
    
    // .. writing it and its subs
    writeProperty(xref, ent);

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

    } catch (Exception ex) {
      throw new GedcomIOException("Error while writing / "+ex.getMessage(), line);
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
    line( 1, "FILE", file);
    line( 1, "GEDC", "");
    line( 2, "VERS", "5.5");
    line( 2, "FORM", "Lineage-Linked");
    line( 1, "CHAR", encoding);
    line( 1, "LANG", gedcom.getLanguage());
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
        lines.setValue(encrypt(prop.getValue()));
        
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
    
    // Make sure enigma is setup
    if (enigma==null) {

      // no need if password is unknown (data is already/still encrypted)
      if (password==Gedcom.PASSWORD_UNKNOWN)
        return value;
        
      // no need if password empty
      if (password.length()==0)
        return value;

      // error if password isn't set    
      if (password==Gedcom.PASSWORD_NOT_SET)
        throw new IOException("Password not set - needed for encryption");
        
      // error if can't encrypt
      enigma = Enigma.getInstance(password);
      if (enigma==null) 
        throw new IOException("Encryption not available");
        
    }
    
    // encrypt and done
    return enigma.encrypt(value);
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
