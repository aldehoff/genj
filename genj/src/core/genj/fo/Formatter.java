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
package genj.fo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import sun.misc.Service;

/**
 * A document formatter
 */
public abstract class Formatter {
  
  /** available formatters */
  private static Formatter[] formatters;
  
  /** default formatter */
  public static Formatter DEFAULT = new DocBookFormatter();

  /** this format */
  private String format;

  /** file extension */
  private String extension;
  
  /** whether this format requires externalization of referenced files (imagedata) */
  private boolean isExternalizedFiles;
  
  /** caching for xsl 2 templates */
  private Map xsl2templates = new HashMap();

  /** 
   * Constructor 
   */
  protected Formatter(String format, String extension, boolean isExternalizedFiles) {
    this.format = format;
    this.extension = extension;
    this.isExternalizedFiles = isExternalizedFiles;

    // setup saxon xslt
    System.setProperty("javax.xml.transform.TransformerFactory", "com.icl.saxon.TransformerFactoryImpl");
  }
  
  /**
   * Valid file extension without dot (e.g. xml, pdf, html, fo) 
   */
  public String getFileExtension() {
    return extension;
  }
  
  /**
   * Format Name 
   */
  public String getFormat() {
    return format;
  }
  
  /**
   * Text representation
   */
  public String toString() {
    return format;
  }
  
  /**
   * equals
   */
  public boolean equals(Object other) {
    Formatter that = (Formatter)other;
    return this.format.equals(that.format);
  }
  
  /**
   * Externalize files resolving imagedata references
   */
  private void externalizeFiles(Document doc, File file) throws IOException {
    
    // grab image directory
    File imageDir = new File(file.getParentFile(), file.getName()+".images");
    imageDir.mkdirs();
    
    // copy all images so their local to the generated document
    if (imageDir.exists()) {
      File[] images = doc.getImageFiles();
      for (int i = 0; i < images.length; i++) {
        File oldFile = images[i];
        File newFile = new File(imageDir, oldFile.getName());
        FileChannel from = new FileInputStream(oldFile).getChannel();
        FileChannel to = new FileOutputStream(newFile).getChannel();
        from.transferTo(0, from.size(), to);
        from.close();
        to.close();      
        doc.setImageFileRef(oldFile, imageDir.getName() + "/" + newFile.getName());
      }
    }
    
    // done
    
  }
  
  /**
   * Format a document
   */
  public void format(Document doc, File file) throws IOException {
    
    // try to create output stream
    FileOutputStream out = new FileOutputStream(file);
    
    // chance to externalize files if applicable
    if (isExternalizedFiles)
      externalizeFiles(doc, file);
    
    // continue
    format(doc, out);
  }
  
  /**
   * Format a document
   */
  public void format(Document doc, OutputStream out) throws IOException {
    try {
      formatImpl(doc, out);
    } catch (Throwable t) {
      if (t instanceof IOException) throw (IOException)t;
      throw new IOException(t.getMessage());
    } finally {
      try { out.close(); } catch (Throwable t) {}
    }
  }
  
  /**
   * Format implementation
   */
  protected abstract void formatImpl(Document doc, OutputStream out) throws Throwable;
  
  /**
   * Get a cached transformation template
   */
  protected Templates getTemplates(File xsl) throws IOException {

    try {
      // known?
      Templates templates = (Templates)xsl2templates.get(xsl);
      if (templates==null) {
        TransformerFactory factory = TransformerFactory.newInstance();
        templates = factory.newTemplates(new StreamSource(xsl));
        xsl2templates.put(xsl, templates);
      }
      // done
      return templates;
    } catch (TransformerConfigurationException e) {
      throw new IOException(e.getMessage());
    }
    
  }
  
  /**
   * Return formatter by key
   */
  public static Formatter getFormatter(String key) {
    Formatter[] fs = getFormatters();
    for (int i = 0; i < fs.length; i++) {
      if (fs[i].getClass().getName().equals(key))
        return fs[i];
    }
    return DEFAULT;
  }
  
  /**
   * Return a lookup key
   */
  public String getKey() {
    return getClass().getName();
  }
  
  /**
   * Resolve available formatters
   */
  public static Formatter[] getFormatters() {
    
    // known?
    if (formatters!=null)
      return formatters;
    
    // look 'em up
    List list = new ArrayList(10);
    list.add(DEFAULT);
    
    Iterator it = Service.providers(Formatter.class);
    while (it.hasNext()) {
      Formatter f = (Formatter)it.next();
      if (!list.contains(f)) list.add(f);
    }

    // keep 'em
    formatters = (Formatter[])list.toArray(new Formatter[list.size()]);
    
    // done
    return formatters;
  }
  
}
