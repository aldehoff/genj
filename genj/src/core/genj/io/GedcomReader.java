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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.MultiLineSupport;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Submitter;
import genj.gedcom.Submission;
import genj.util.Debug;
import genj.util.Origin;
import genj.util.Trackable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Type that knows how to read GEDCOM-data from InputStream
 */
public class GedcomReader implements Trackable {

  private Gedcom              gedcom;
  private BufferedReader      in;

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
  private List xrefs;
  private String submitter;
  private String submission;
  private List warnings;
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
  public GedcomReader(Origin org) throws IOException {
    
    // open origin
    InputStream oin = org.open();
    
    // init some data
    in       = new BufferedReader(createReader(oin));
    line     = 0;
    origin   = org;
    length   = oin.available();
    level    = 0;
    read     = 0;
    warnings = new ArrayList(128);
    
    // Done
  }

  /**
   * Initialize the reader we're using
   */
  private Reader createReader(InputStream stream) throws IOException {
    
    // prepare sniffer
    InputStreamSniffer sniffer = new InputStreamSniffer(stream);
    String encoding = sniffer.getEncoding();
    Debug.log(Debug.INFO, this, "Trying encoding "+encoding);
    
    // attempt it
    try {
      // Unicode
      if (GedcomWriter.UNICODE.equals(encoding)) return new InputStreamReader(sniffer, "UTF-8");
      // ASCII
      if (GedcomWriter.ASCII.equals(encoding)) return new InputStreamReader(sniffer, "ASCII");
      // ISO-8859-1
      if (GedcomWriter.IBMPC.equals(encoding)) return new InputStreamReader(sniffer, "ISO-8859-1");
      // ANSEL
      if (GedcomWriter.ANSEL.equals(encoding)) return new AnselReader(sniffer);
    } catch (UnsupportedEncodingException e) {
    }
    
    // default
    Debug.log(Debug.WARNING, this, "Failed to create reader for encoding "+encoding);
    
    return new InputStreamReader(sniffer);
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
      if (worker!=null)
        worker.interrupt();
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
  public List getWarnings() {
    return warnings;
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

    // "L XXXX" expected
    if (!readLine()||(level!=0)||(xref.length()==0)) {
      String msg = "Expected 0 @XREF@ INDI|FAM|OBJE|NOTE|REPO|SOUR|SUBN|SUBM";
      // at least still level identifyable?
      if (level==0) {
        // skip record
        skipEntity(msg);
        // continue
        return;
      }
      throw new GedcomFormatException(msg,line);
    }

    // Create entity
    Entity ent=null;
    try {
      // Fix duplicates?
      if (fixDuplicateIDs&&gedcom.getEntity(xref)!=null) {
        ent=gedcom.createEntity(tag, null);
      } else {
        ent=gedcom.createEntity(tag, xref);
      }
      
      // preserve value for those who care
      ent.setValue(value);
      
      // Read entity's properties till end of record
      readProperties(ent, MetaProperty.get(ent), 1);

    } catch (GedcomException ex) {
      skipEntity(ex.getMessage());
    }

    // Done
    entity++;
  }
  
  /**
   * Skip entity
   */
  private void skipEntity(String msg) throws GedcomFormatException, GedcomIOException {
    //  track it
    int start = line;
    try {
      do {
        readLine();
      } while (level!=0);
      undoLine();
    } finally {
      warnings.add("Line "+start+": Skipping "+(line-start)+" lines - "+msg);
    }
  }

  /**
   * Read Gedcom data
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  public Gedcom read() throws GedcomIOException, GedcomFormatException {

    // Remember working thread
    synchronized (lock) {
      worker=Thread.currentThread();
    }
    
    // try it
    Gedcom result;
    
    try {
      result = readGedcom();
    } catch (Throwable t) {
      // 20030530 what abbout OutOfMemoryError
      throw new GedcomIOException(t.toString(), line);
    } finally  {
      // close in
      try { in.close(); } catch (Throwable t) {};
      // forget working thread
      synchronized (lock) {
        worker=null;
      }
    }

    // done
    return result;
  }
  
  /**
   * Read Gedcom as a whole
   *
   */
  private Gedcom readGedcom() throws GedcomIOException, GedcomFormatException {


    // Create Gedcom
    int expected = Math.max((int)length/ENTITY_AVG_SIZE,100);
    gedcom = new Gedcom(origin,expected);
    xrefs = new ArrayList(expected);

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
      if (tag.equals("TRLR")) break;

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

    // Prepare submitter
    if (submitter!=null) {
      try {
        Submitter sub = (Submitter)gedcom.getEntity(submitter.replace('@',' ').trim(), Gedcom.SUBMITTERS);
        gedcom.setSubmitter(sub);
      } catch (Throwable t) {
        warnings.add("Submitter "+submitter+" couldn't be resolved");
      }
    }

    /*
    // Prepare submission record
    if (submission!=null) {
      try {
        Submission sub = (Submission)gedcom.getEntity(submission.replace('@',' ').trim(), Gedcom.SUBMISSIONS);
        gedcom.setSubmission(sub);
      } catch (Throwable t) {
        warnings.add("Submission record "+submission+" couldn't be resolved");
      }
    }
    */

    // Link references
    for (int i=0,j=xrefs.size();i<j;i++) {
      XRef xref = (XRef)xrefs.get(i);
      try {
        xref.prop.link();

        progress = Math.min(100,(int)(i*(100*2)/j));  // 100*2 because Links are probably backref'd

      } catch (GedcomException ex) {
        warnings.add("Line "+xref.line+": Property "+xref.prop.getTag()+" - "+
                 ex.getMessage());
      }
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

    //  0 "HEAD", ""
    //  1 "SOUR", "GENJ"
    //  2 "VERS", Version.getInstance().toString()
    //  2 "NAME", "GenealogyJ"
    //  2 "CORP", "Nils Meier"
    //  3 "ADDR", "http://genj.sourceforge.net"
    //  1 "DEST", "ANY"
    //  1 "DATE", date
    //  2 "TIME", time
    //  1 "SUBM", '@'+gedcom.getSubmitter().getId()+'@'
    //  1 "SUBN", '@'+gedcom.getSubmission().getId()+'@'
    //  1 "GEDC", ""
    //  2 "VERS", "5.5"
    //  2 "FORM", "Lineage-Linked"
    //  1 "CHAR", encoding
    //  1 "FILE", file
    if (!readLine()||(level!=0)||(!tag.equals("HEAD"))) {
      throw new GedcomFormatException("Expected 0 HEAD",line);
    }

    do {
      // read until end of header
      if (!readLine()) {
        throw new GedcomFormatException("Unexpected end of header",line);
      }
      if (level==0) {
        break;
      }
      // check for submitter
      if (level==1&&"SUBM".equals(tag)) {
        submitter = value; 
      }
      // check for submission record
      if (level==1&&"SUBN".equals(tag)) {
        submission = value; 
      }
      // done
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
    if (cancel)
      throw new GedcomIOException("Operation cancelled",line);

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
        gedcomLine = in.readLine();
        
        if (gedcomLine==null) {
          gedcomLine="";
          break;
        }
        
        // 20030530 I think we can forget about this call
        //gedcomLine.trim();

        // .. update statistics
        read+=gedcomLine.length()+2;
        if (length>0) {
          progress = Math.min(100,(int)(read*100/length));
        }

      } while ( gedcomLine.length()==0 );

    } catch (Exception ex) {

      // .. cancel
      if (cancel) 
        throw new GedcomIOException("Operation cancelled",line);

      // .. file erro
      throw new GedcomIOException("Error reading file "+ex.getMessage(),line);
    }

    // Parse gecom-line
    // 20030530 I think we can use the simple constructor tokenizing " \t\n\r\f"
    //StringTokenizer tokens = new StringTokenizer(gedcomLine," ",false);
    StringTokenizer tokens = new StringTokenizer(gedcomLine);

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
        // 20030530 o.k. gotta switch to delim "\n" because we want everything 
        // to end of line including contained spaces but without leading/trailing
        // spaces
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
  private void readProperties(Property of, MetaProperty meta, int currentlevel) throws GedcomIOException, GedcomFormatException {

    MultiLineSupport multi = (of instanceof MultiLineSupport) ? (MultiLineSupport)of : null;
  
    // Get properties of property
    Property prop;
    do {
  
      // try to get next property
      if (!readLine()) 
        throw new GedcomFormatException("Unexpected end of record",line);
  
      // end of property ?
      if (level<currentlevel) 
        break;
        
      // can 'of' consume it?
      if (multi!=null&&multi.append(level-currentlevel+1, tag, value)) 
        continue;
        
      // skip if level>currentLevel?
      if (level>currentlevel) {
        warnings.add("Line "+line+": Skipping "+tag+" because of level "+level+" - expected "+currentlevel);
        continue;
      }
  
      // get meta property for child
      MetaProperty child = meta.get(tag, true);
  
      // create property instance
      prop = child.create(value);
      
      // and add to prop
      of.addProperty(prop);
  
      // a reference ? Remember !
      if (prop instanceof PropertyXRef)
        xrefs.add(new XRef(line,(PropertyXRef)prop));
  
      // recurse into its properties
      readProperties(prop, child, currentlevel+1);
      
      // next property
    } while (true);
  
    // restore what we haven't consumed
    undoLine();
  }

  /**
   * Put back gedcom-line
   */
  private void undoLine() {
    undoLine = gedcomLine;
  }
  
  /**
   * Keeping track of XRefs
   */
  private static class XRef {
    /** attributes */
    int line;
    PropertyXRef prop;
    /** constructor */
    XRef(int l, PropertyXRef p) {
      line = l;
      prop = p;
    }
  } //XRef
  
  /**
   * EncodingInputStream
   */
  private static class InputStreamSniffer extends BufferedInputStream {
    
    /**
     * Constructor
     */
    private InputStreamSniffer(InputStream in) {
      super(in, 4096);
      
      // done
    }
    
    /**
     * Sniff encoding
     */
    public String getEncoding() throws IOException {
      // fill buffer
      super.mark(1); 
      super.read();
      super.reset();
      // sniff
      String s = new String(super.buf, super.pos, super.count);
      // tests
      if (s.indexOf("1 CHAR UNICODE")>0) return GedcomWriter.UNICODE;
      if (s.indexOf("1 CHAR ASCII")>0) return GedcomWriter.ASCII;
      if (s.indexOf("1 CHAR ANSEL")>0) return GedcomWriter.ANSEL;
      if (s.indexOf("1 CHAR IBMPC")>0) return GedcomWriter.IBMPC;
      // no clue
      return null;
    }
          
  } //InputStreamSniffer
  
} //GedcomReader
