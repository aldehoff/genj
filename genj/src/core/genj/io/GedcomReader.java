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
import java.util.zip.*;
import java.net.*;

import genj.gedcom.*;
import genj.util.*;

/**
 * Type that knows how to read GEDCOM-data from InputStream
 */
public class GedcomReader implements Trackable {

  private Gedcom         gedcom;
  private BufferedReader in;

  private int progress;
  private int level;
  private int line;
  private int entity;
  private int read;
  private int state;
  private long length;
  private String xref;
  private String tag;
  private String value;
  private String undoLine,gedcomLine;
  private Origin origin;
  private Vector pxrefs;
  private StringBuffer warnings;
  private boolean cancel=false;
  private Thread worker;
  private Object lock = new Object();
  private boolean fixDuplicateIDs = false;

  private final static int ENTITY_AVG_SIZE = 150;
  private final static int READHEADER = 0, READENTITIES = 1, LINKING = 2;

  /**
   * Constructor
   * @param in BufferedReader to read from
   */
  public GedcomReader(InputStream in, Origin origin, long length) {

    // Remember some data
    this.in    = new BufferedReader(new InputStreamReader(in));
    this.line  =0 ;
    this.origin=origin;
    this.length=length;

    // Init some data
    level=0;
    read=0;
    warnings=new StringBuffer(512);

    // Done
  }

  /**
   * Backdoor switch that triggers automatic fixing of duplicate IDs
   */
  public void setFixDuplicatedIDs(boolean value) {
    fixDuplicateIDs = value;
  }

  /**
   * Cancels operation
   */
  public void cancel() {

    // Stop it as soon as possible
    cancel=true;
    synchronized (lock) {
      if (worker!=null) {
      try {
        in.close();
      } catch (IOException e) {
      }
      worker.interrupt();
      }
    }
    // Done
  }

  /**
   * Returns progress of save in %
   */
  public int getProgress() {
    return progress;
  }

  /**
   * Returns state as explanatory string
   */
  public String getState() {
    switch (state) {
      case READHEADER :
      return "Reading header";
      default:
      case READENTITIES :
      //return "Loading data";//line+" Lines & "+entity+" Entities";
      return "L:"+line+" & E:"+entity;
      case LINKING      :
      return "Linking entities";
    }
  }

  /**
   * Returns warnings of operation
   * @return the warning as String
   */
  public String getWarnings() {
    return warnings.toString();
  }

  /**
   * Helper to get gedcom-line
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private boolean peekLine() throws GedcomIOException, GedcomFormatException {
    boolean rc = readLine();
    undoLine();
    return rc;
  }

  /**
   * Read entity
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private void readEntity() throws GedcomIOException, GedcomFormatException {

    // L XXXX
    if (!readLine()||(level!=0)||(xref.length()==0)) {
      throw new GedcomFormatException("Expected 0 @XREF@ INDI|FAM|OBJE|NOTE|REPO|SOUR|SUBN|SUBM",line);
    }

    // Create entity
    Entity ent=null;
    Property prop=null;
    try {
      // Fix duplicates?
      if ((fixDuplicateIDs)&&(gedcom.getEntityFromId(xref)!=null)) {
        ent=gedcom.createEntity(tag, null);
      } else {
        ent=gedcom.createEntity(tag,xref);
      }
      prop=ent.getProperty();
    } catch (GedcomException ex) {
      //  throw new GedcomFormatException("Unknown entity with tag:"+tag,line);
      warnings.append("Line "+line+": Dropped entity "+tag+"\n");
      do {
        readLine();
      } while (level!=0);
      undoLine();
    }

    // Read entity's properties till end of record
    // NM 19990720 Have to make sure that prop is not null
    if (prop!=null) {
      readProperties(prop,1);
    }

    // Done
    entity++;
  }

  /**
   * Read Gedcom data
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  public Gedcom readGedcom() throws GedcomIOException, GedcomFormatException {

    // Remember working thread
    synchronized (lock) {
      worker=Thread.currentThread();
    }

    // Create Gedcom
    int expected = Math.max((int)length/ENTITY_AVG_SIZE,100);
    gedcom = new Gedcom(origin,expected);
    pxrefs = new Vector(expected);

    // Read the Header
    readHeader();

    // Next state
    state++;

    // Read records
    do {
      // .. still there ?
      if (!peekLine()||(level!=0)) {
        throw new GedcomFormatException("Expected 0 TAG or 0 TRLR",line);
      }

      // .. end ?
      if (tag.equals("TRLR")) {
        break;
      }

      // .. entity to parse
      readEntity();

      // .. next
    } while (true);


    // Read Tail
    if (!readLine()||(level!=0)) {
      throw new GedcomFormatException("Expected 0 TRLR",line);
    }

    // Next state
    state++;

    // Link references
    PropertyXRef pxref;
    int xcount = pxrefs.size();
    for (int i=0;i<xcount;i++) {
      pxref = (PropertyXRef)pxrefs.elementAt(i);
      try {
        pxref.link();

        progress = Math.min(100,(int)(i*(100*2)/xcount));  // 100*2 because Links are probably backref'd

      } catch (GedcomException ex) {
        warnings.append("Line "+line+": Property "+pxref.getTag()+" - "+
                 ex.getMessage()+"\n");
      }
    }

    // Forget working thread
    synchronized (lock) {
      worker=null;
    }

    // Done
    return gedcom;
  }

  /**
   * Read Header
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private boolean readHeader() throws GedcomIOException, GedcomFormatException {

    // 0 HEAD
    // 1 SOUR
    // 2 VERS
    // 2 NAME
    // 1 DATE
    if (!readLine()||(level!=0)||(!tag.equals("HEAD"))) {
      throw new GedcomFormatException("Expected 0 HEAD",line);
    }

    do {
      if (!readLine()) {
        throw new GedcomFormatException("Unexpected end of header",line);
      }
      if (level==0) {
        break;
      }
    } while (true);

    // Last still to be used
    undoLine();

    // Done
    return true;
  }

  /**
   * Helper to get gedcom-line
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private boolean readLine() throws GedcomIOException, GedcomFormatException {

    // Still running ?
    if (cancel) {
      throw new GedcomIOException("Operation cancelled",line);
    }

    // Still undo ?
    if (undoLine!=null) {

      // .. use that
      gedcomLine=undoLine;
      undoLine=null;

      // .. done
      return true;
    }

    // .. get new
    try {
      
      //Thread.currentThread().sleep(100);

      // Read
      do {

        line++;
        gedcomLine = in.readLine().trim();

        // .. update statistics
        read+=gedcomLine.length()+2;
        if (length>0) {
          progress = Math.min(100,(int)(read*100/length));
        }

      } while ( gedcomLine.length()==0 );

    } catch (Exception ex) {

      // .. cancel
      if (cancel) {
        throw new GedcomIOException("Operation cancelled",line);
      }

      // .. file erro
      throw new GedcomIOException("Error reading file "+ex.getMessage(),line);
    }

    // Parse gecom-line
    StringTokenizer tokens = new StringTokenizer(gedcomLine," ",false);

    try {

      // .. level
      try {
        level = Integer.parseInt(tokens.nextToken(),10);
      } catch (NumberFormatException ex) {
        throw new GedcomFormatException("Expected X [@XREF@] TAG [VALUE] - x integer",line);
      }

      // .. tag (?)
      tag = tokens.nextToken();

      // .. xref ?
      if (tag.startsWith("@")) {

        // .. valid ?
        if (!tag.endsWith("@")) {
          throw new GedcomFormatException("Expected X @XREF@ TAG [VALUE]",line);
        }

        // .. indeed, valid xref !
        xref = tag.substring(1,tag.length()-1);

        // .. tag is the next token
        tag = tokens.nextToken();

      } else {

        // .. no reference in line !
        xref = "";
      }

      // .. value
      if (tokens.hasMoreElements()) {
        value = tokens.nextToken("\n").trim();
      } else {
        value = "";
      }

    } catch (NoSuchElementException ex) {
      // .. not enough tokens
      throw new GedcomFormatException("Expected X [@XREF@] TAG [VALUE]",line);
    }

    // Done
    return true;
  }

  /**
   * Read propertiees of property
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private void readProperties(Property of,int currentlevel) throws GedcomIOException, GedcomFormatException {

    // Property with multiLine?
    int ml = of.isMultiLine();
    if (ml!=Property.NO_MULTI) {

      StringBuffer bvalue = new StringBuffer(value);
      while (peekLine()) {

        // .. CONT ?
        // NM 20010710 allows CONT & CONC now
        if ( (!(tag.equals("CONT")||tag.equals("CONC"))) || (level!=currentlevel)) {
          break;
        }

        // .. Take it
        readLine();

        // .. remember another value line
        if (ml==Property.MULTI_NEWLINE) {
          bvalue.append("\n");
        }

        bvalue.append(value);
      }

      // Change value
      of.setValue(bvalue.toString());
    }

    // Get properties of property
    Property prop;
    do {

      // .. try to get next property
      if (!readLine()) {
        throw new GedcomFormatException("Unexpected end of record",line);
      }

      // .. end of property ?
      if (level<currentlevel) {
        break;
      }

      // .. here's the property
      prop = Property.createInstance(tag,value);
      of.addProperty(prop);

      // .. a reference ? Remember !
      if (prop instanceof PropertyXRef) {
        pxrefs.addElement(prop);
      }

      // .. read its properties
      readProperties(prop,currentlevel+1);

      // .. next property
    } while (true);

    undoLine();
  }

  /**
   * Put back gedcom-line
   */
  private void undoLine() {
    undoLine = gedcomLine;
  }
}
