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

import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Reads gedcom lines into properties
 */
public class PropertyReader {

  private final static Resources RESOURCES = Resources.get("genj.io");

  private boolean useIndents = false;
  protected int lines = 0;
  private String line = null;
  
  /** variables read line by line */
  protected int level;
  protected String tag;
  protected String xref;
  protected String value;
  
  /** input */
  private BufferedReader in;
  
  /** constructor */
  public PropertyReader(Reader in, boolean useIndents) {
    this(new BufferedReader(in), useIndents);
  }
  
  /** constructor */
  public PropertyReader(BufferedReader in, boolean useIndents) {
    this.in = in;
    this.useIndents = useIndents;
  }
  
  /** lines read */
  public int getLines() {
    return lines;
  }
  
  /**
   * read into property
   */
  public void read(Property prop) throws IOException {
    read(prop, -1);
  }
  
  /**
   * read into property
   */
  public void read(Property prop, int index) throws IOException {
    // do the recursive read
    readProperties(prop, 0, index);
    // put back a pending line? this works only if the constructor received a buffered reader
    if (line!=null) {
      line = null;
      in.reset();
    }
    // done
  }
  
  /**
   * read recursively while lines available
   */
  protected void readProperties(Property prop, int currentLevel, int pos) throws IOException {

    // try to read some multilines first?
    if (prop instanceof MultiLineProperty) {
      // run through collector
      MultiLineProperty.Collector collector = ((MultiLineProperty)prop).getLineCollector();
      while (true) {
        // check next line
        if (!readLine(false))
          break;
        // collect as far as we can
        if (level<currentLevel+1 || !collector.append(level-currentLevel, tag, value))
          break;
        // consume it
        line = null;
        // next line
      } 
      // commit collected value
      prop.setValue(collector.getValue());
    }
  
    // loop over subs
    while (true) {
      
      // read next &parse
      if (!readLine(false))
        return;
      
      // wrong level now?
      if (level<currentLevel+1) 
        return;
      
      // consume it
      line = null;
      
      // check for wrong level value
      //  0 INDI
      //  1 BIRT
      //  3 DATE
      // we simply spit out a warning and continue as if nothing happened
      if (level>currentLevel+1) {
        trackBadLevel(level);
        while (level-1>currentLevel++) 
          prop = prop.addProperty("_TAG", "");
      }
    
      // add sub property
      Property child = addProperty(prop, tag, value, pos<0 ? -1 : pos++);
      
      // a reference?
      if (child instanceof PropertyXRef)
        link((PropertyXRef)child);
        
      // continue with child
      readProperties(child, currentLevel+1, 0);
        
      // next line
    }
    
    // done
  }

  /** add a child property **/
  protected Property addProperty(Property prop, String tag, String value, int pos) {
    return pos<0 ? prop.addProperty(tag, value, true) : prop.addProperty(tag, value, pos);
  }
  
  /**
   * read a line
   */
  protected boolean readLine(boolean consume) throws IOException {
    
    // need a line?
    if (line==null) {
      
      // mark current position in reader
      in.mark(256);
      
      // grab it
      line = in.readLine();
      if (line==null)
        return false;
      lines ++;
    
      // 20040322 use space and also \t for delim in case someone used tabs in file
      StringTokenizer tokens = new StringTokenizer(line," \t");
  
      try {
        
        // .. caclulate level by looking at spaces or parsing a number
        try {
          if (useIndents) {
            level = 0;
            while (line.charAt(level)==' ') level++;
            level++;
          } else {
            level = Integer.parseInt(tokens.nextToken(),10);
          }
        } catch (StringIndexOutOfBoundsException sioobe) {
          throw new GedcomFormatException(RESOURCES.getString("read.error.emptyline"), lines);
        } catch (NumberFormatException nfe) {
          throw new GedcomFormatException(RESOURCES.getString("read.error.nonumber"), lines);
        }
  
        // .. tag (?)
        if (tokens.hasMoreTokens()) 
          tag = tokens.nextToken();
        else {
          tag = "_TAG";
        }
          
        // .. xref ?
        if (tag.startsWith("@")) {
  
          // .. valid ?
          if (!tag.endsWith("@")||tag.length()<=2)
            throw new GedcomFormatException(RESOURCES.getString("read.error.invalidid"), lines);
   
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
        throw new GedcomFormatException(RESOURCES.getString("read.error.cantparse"), lines);
      }
      
      // TUNING: for tags we expect a lot of repeating strings (limited numbe of tags) so
      // we build the intern representation of tag here - this makes us share string 
      // instances for an upfront cost
      tag = tag.intern();
          
    }
    
    // consume it already?
    if (consume)
      line = null;
      
    // we're ready
    return true;
  }
  
  /** link a reference - default tries to link and ignores errors */
  protected void link(PropertyXRef xref) {
    try {
      xref.link();
    } catch (Throwable t) {
      // ignored
    }
  }
  
  /** track a bad level - default noop */
  protected void trackBadLevel(int level) {
  }
  
} //PropertyDecoder
