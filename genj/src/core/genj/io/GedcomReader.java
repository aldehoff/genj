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

import genj.crypto.Enigma;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Submitter;
import genj.util.Debug;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.Trackable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Type that knows how to read GEDCOM-data from InputStream
 */
public class GedcomReader implements Trackable {

  private final static Resources resources = Resources.get("genj.io");

  /** estimated average byte size of one entity */
  private final static int ENTITY_AVG_SIZE = 150;
  
  /** stati the reader goes through */
  private final static int READHEADER = 0, READENTITIES = 1, LINKING = 2;

  /** lots of state we keep during reading */
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
  private List warnings;
  private boolean cancel=false;
  private Thread worker;
  private Object lock = new Object();
  private Enigma enigma;

  /**
   * Constructor
   * @param initOrg the origin to initialize reader from
   */
  public GedcomReader(Origin org) throws IOException {
    
    Debug.log(Debug.INFO, this, "Initializing reader for "+org);
    
    // open origin
    InputStream oin = org.open();

    // prepare sniffer
    InputStreamSniffer sniffer = new InputStreamSniffer(oin);
    
    // get reader
    Reader reader;
    try {
      reader = sniffer.getReader();
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, this, "Failed to create reader: "+t.getMessage());
      reader = new InputStreamReader(sniffer);
    }

    // init some data
    in       = new BufferedReader(reader);
    line     = 0;
    origin   = org;
    length   = oin.available();
    level    = 0;
    read     = 0;
    warnings = new ArrayList(128);
    gedcom   = new Gedcom(origin);
    gedcom.setEncoding(sniffer.getEncoding());
    
    // Done
  }
  
  /**
   * Set password to use
   */
  public void setPassword(String password) {
    
    // valid argument?
    if (password==null)
      throw new IllegalArgumentException("Password can't be NULL");
      
    // set it on Gedcom
    gedcom.setPassword(password); 
    
    // done
  }

  /**
   * Cancels operation (async ok)
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
        return resources.getString("progress.read.header");
      case READENTITIES :default:
        return resources.getString("progress.read.entities", new String[]{ ""+line, ""+entity} );
      case LINKING      :
        return resources.getString("progress.read.linking");
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

    // "0 [@xref@] value" expected - xref can be missing for custom records
    if (!readLine()||level!=0) {
      String msg = "Expected 0 @XREF@ INDI|FAM|OBJE|NOTE|REPO|SOUR|SUBM";
      // at least still level identifyable?
      if (level==0) {
        // skip record
        skipEntity(msg);
        // continue
        return;
      }
      throw new GedcomFormatException(msg,line);
    }
    
    if (xref.length()==0)
      addWarning(line, "Entity/record "+tag+" without valid @xref@");

    // Create entity and read its properties
    try {
      
      Entity ent = gedcom.createEntity(tag, xref);
      
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
      addWarning(start, "Skipped "+(line-start)+" lines - "+msg);
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
    try {
      gedcom.startTransaction().setTrackChanges(false);
      readGedcom();
      gedcom.endTransaction();
      return gedcom;
    } catch (GedcomIOException gex) {
      throw gex;
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

    // nothing happening here
  }
  
  /**
   * Read Gedcom as a whole
   *
   */
  private void readGedcom() throws GedcomIOException, GedcomFormatException {


    // Create Gedcom
    int expected = Math.max((int)length/ENTITY_AVG_SIZE,100);
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
        Submitter sub = (Submitter)gedcom.getEntity(Gedcom.SUBM, submitter.replace('@',' ').trim());
        gedcom.setSubmitter(sub);
      } catch (Throwable t) {
        addWarning(line, "Submitter "+submitter+" couldn't be resolved");
      }
    }

    // Link references
    for (int i=0,j=xrefs.size();i<j;i++) {
      XRef xref = (XRef)xrefs.get(i);
      try {
        xref.prop.link();

        progress = Math.min(100,(int)(i*(100*2)/j));  // 100*2 because Links are probably backref'd

      } catch (GedcomException ex) {
        addWarning(xref.line, "Property "+xref.prop.getTag()+" - "+ ex.getMessage());
      }
    }

    // Done
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
      
      // Read
      do {

        line++;
        gedcomLine = in.readLine();
        
        if (gedcomLine==null) {
          gedcomLine="";
          break;
        }
        
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

    // Parse gedcom-line 
    // 20040322 use space and also \t for delim in case someone used tabs in file
    StringTokenizer tokens = new StringTokenizer(gedcomLine," \t");

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
        if (!tag.endsWith("@")||tag.length()<=2)
          throw new GedcomFormatException("Expected X @XREF@ TAG [VALUE]",line);
 
        // .. indeed, xref !
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
        // to end of line including contained spaces 
        value = tokens.nextToken("\n");
        // 20030609 strip leading space that forms delimiter to tag/xref
        // (this was trim() once but identified as too greedy)
        if (value.startsWith(" "))
          value = value.substring(1);
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

    MultiLineProperty.Collector collector =  of instanceof MultiLineProperty ? ((MultiLineProperty)of).getLineCollector() : null;
  
    // Get properties of property
    Property prop;
    do {
  
      // try to get next property
      if (!readLine()) 
        throw new GedcomFormatException("Unexpected end of record",line);
  
      // end of property ?
      if (level<currentlevel) 
        break;
        
      // can we continue with current?
      if (collector!=null&&collector.append(level-currentlevel+1, tag, value)) 
          continue;
        
      // skip if level>currentLevel?
      if (level>currentlevel) {
        addWarning(line, "Skipping "+tag+" because level "+level+" was expected "+currentlevel);
        continue;
      }
  
      // get meta property for child
      MetaProperty child = meta.get(tag, true);
  
      // create property instance
      prop = child.create(value);
      
      // and add to prop
      // 20040513 since properties are placed in order by default
      // we pass false here - on load we're accepting what's incoming
      of.addProperty(prop, false);
  
      // a reference ? Remember !
      if (prop instanceof PropertyXRef)
        xrefs.add(new XRef(line,(PropertyXRef)prop));
  
      // recurse into its properties
      readProperties(prop, child, currentlevel+1);
      
      // next property
    } while (true);

    // commit collected value if available
    String value;
    if (collector!=null) {
      value = collector.getValue();
      of.setValue(value);
    } else {
      value = of.getValue();
    }

    // decrypt value
    decryptLazy(of, value);
       
    // restore what we haven't consumed
    undoLine();
  }
  
  /**
   * Decrypt a value if necessary
   */
  private void decryptLazy(Property prop, String value) throws GedcomEncryptionException {
    
    // no need to do anything if not encrypted value 
    if (!Enigma.isEncrypted(value))
      return;
      
    // set property private
    prop.setPrivate(true, false);
      
    // no need to do anything for unknown password
    String password = gedcom.getPassword();
    if (password==Gedcom.PASSWORD_UNKNOWN) {
      addWarning(line, resources.getString("crypt.password.unknown"));
      return;
    }
      
    // not set password with encrypted value is error
    if (password==Gedcom.PASSWORD_NOT_SET) 
      throw new GedcomEncryptionException(resources.getString("crypt.password.required"), line);
    
    // try to init decryption
    if (enigma==null) {
      enigma = Enigma.getInstance(password);
      if (enigma==null) {
        addWarning(line, resources.getString("crypt.password.mismatch"));
        gedcom.setPassword(Gedcom.PASSWORD_UNKNOWN);
        return;
      }
    }

    // try to decrypt    
    try {
      // set decrypted value
      prop.setValue(enigma.decrypt(value));
    } catch (IOException e) {
      throw new GedcomEncryptionException(resources.getString("crypt.password.invalid"), line);
    }
      
    // done
  }

  /**
   * Put back gedcom-line
   */
  private void undoLine() {
    undoLine = gedcomLine;
  }
  
  /**
   * Add a warning
   */
  private void addWarning(int wline, String txt) {
    String warning = "Line "+wline+": "+txt;
    warnings.add(warning);
    Debug.log(Debug.INFO, this, warning);
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
    
    private final byte[]
      BOM_UTF8    = { (byte)0xEF, (byte)0xBB, (byte)0xBF },
      BOM_UTF16BE = { (byte)0xFE, (byte)0xFF },
      BOM_UTF16LE = { (byte)0xFF, (byte)0xFE };
      
    private Reader reader;
    private String encoding;

    /**
     * Constructor
     */
    private InputStreamSniffer(InputStream in) throws IOException {
      super(in, 4096);

      // fill buffer
      super.mark(4096); 
      super.read();
      super.reset();
      
      // BOM present?
      if (matchPrefix(BOM_UTF8)) {
        Debug.log(Debug.INFO, this, "Found BOM_UTF8 - trying encoding UTF-8");
        reader = new InputStreamReader(this, "");
        encoding = Gedcom.UNICODE;
        return;
      }
      if (matchPrefix(BOM_UTF16BE)) {
        Debug.log(Debug.INFO, this, "Found BOM_UTF16BE - trying encoding UTF-16BE");
        encoding = Gedcom.UNICODE;
        return;
      }
      if (matchPrefix(BOM_UTF16LE)) {
        Debug.log(Debug.INFO, this, "Found BOM_UTF16LE - trying encoding UTF-16LE");
        reader = new InputStreamReader(this, "UTF-16LE");
        encoding = Gedcom.UNICODE;
        return;
      }
      
      // sniff gedcom header
      String header = new String(super.buf, super.pos, super.count);
      
      // tests
      if (matchHeader(header,Gedcom.UNICODE)) {
        Debug.log(Debug.INFO, this, "Found "+Gedcom.UNICODE+" - trying encoding UTF-8");
        reader = new InputStreamReader(this, "UTF-8");
        encoding = Gedcom.UNICODE;
        return;
      } 
      if (matchHeader(header,Gedcom.ASCII)) {
        Debug.log(Debug.INFO, this, "Found "+Gedcom.ASCII+" - trying encoding ASCII");
        reader = new InputStreamReader(this, "ASCII");
        encoding = Gedcom.ASCII;
        return;
      } 
      if (matchHeader(header,Gedcom.ANSEL)) {
        Debug.log(Debug.INFO, this, "Found "+Gedcom.ANSEL+" - trying encoding ANSEL");
        reader = new AnselReader(this);
        encoding = Gedcom.ANSEL;
        return;
      } 
      if (matchHeader(header,Gedcom.ANSI)) {
        Debug.log(Debug.INFO, this, "Found "+Gedcom.ANSI+" - trying encoding Windows-1252");
        reader = new InputStreamReader(this, "Windows-1252"); 
        encoding = Gedcom.ANSI;
        return;
      } 
      if (matchHeader(header,Gedcom.LATIN1)||matchHeader(header,"IBMPC")) { // legacy - old style ISO-8859-1/latin1
        Debug.log(Debug.INFO, this, "Found "+Gedcom.LATIN1+" or IBMPC - trying encoding ISO-8859-1");
        reader = new InputStreamReader(this, "ISO-8859-1"); 
        encoding = Gedcom.LATIN1;
        return;
      } 

      // no clue - will default to Ansel
      Debug.log(Debug.INFO, this, "Could not sniff encoding - trying ANSEL");
      reader = new AnselReader(this);
      encoding = Gedcom.ANSEL;
    }
    
    /**
     * Match a header encoding
     */
    private boolean matchHeader(String header, String encoding) {
      return header.indexOf("1 CHAR "+encoding)>0;
    }
    
    /**
     * Match a prefix byte sequence
     */
    private boolean matchPrefix(byte[] prefix) throws IOException {
      // too match to match?
      if (super.count<prefix.length)
        return false;
      // try it
      for (int i=0;i<prefix.length;i++) {
        if (super.buf[pos+i]!=prefix[i])
          return false;
      }
      // skip match
      super.skip(prefix.length);
      // matched!
      return true;
    }
          
    /**
     * result - reader
     */
    /*result*/ Reader getReader() {
      return reader;
    }
    
    /**
     * result - encoding
     */
    /*result*/ String getEncoding() {
      return encoding;
    }
    
  } //InputStreamSniffer
  
} //GedcomReader
