package genj.almanac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Wikipedia History Import
 */
public class Wikipedia {
  
  /** disclaimer */
  private static final String[] 
    DISCLAIMER = {
      "#                                                                       ",
      "# GenJ Almanac (C) 2004 Nils Meier <nils@meiers.net>                    ",
      "#                                                                       ",
      "# This document is licensed under the GNU Free Documentation License. It",
      "# uses material from the Wikipedia article \"{1}\" to \"{2}\" available at",
      "# http://{0}.wikipedia.org/wiki/{1} (and following).                    ",
      "#                                                                       ",
      "# Permission is granted to copy, distribute and/or modify this document ",
      "# under the terms of the GNU Free Documentation License, Version 1.2 or ",
      "# any later version published by the Free Software Foundation; with no  ",
      "# Invariant Sections, with no Front-Cover Texts, and with no Back-Cover ",
      "# Texts.                                                                ",
      "#                                                                       "
    };
  
  /** utf8 */
  private static final Charset UTF8 = Charset.forName("UTF-8");
  
  /** regexp patterns we use */
  private static Pattern 
  
    // match "== something ==="
    PATTERN_GROUP = Pattern.compile(" *(=+) *([^=]*?)[ =]*"),
    
    // match "* [[6. August]] - Event description"
    PATTERN_EVENT = Pattern.compile("\\*+[ ]*\\[\\[(.+?)\\]\\] *[-:] *(.*)"),
    
    // match "...[[link|text]]..."
    PATTERN_LINK  = Pattern.compile("\\[\\[([^\\[]*\\|)?([^\\[]*?)\\]\\]"),
    
    // match "January 1", "13.Maerz", "3 fevrier"
    PATTERN_MONTH = Pattern.compile("[^\\d \\.]+"),
    PATTERN_DAY   = Pattern.compile("[\\d]+");
  
  private static Object[] PATTERNSUBS = {
      // "'''"
      Pattern.compile("'''(.*?)'''"  ), "<b>$1</b>",
      // "''" 
      Pattern.compile("''(.*?)''"  ), "<em>$1</em>",
  };

  /** wikipedia url */
  private final static String 
    URL = "http://{0}.wikipedia.org/wiki/Special:Export/{1}";
  
  /** current group (===) */
  private String group = null;
  
  /** month names */
  private static String[] months;
  
  /** events imported */
  private int imported = 0;

  /**
   * main - call with arguments
   */
  public static void main(String[] args) {
    
    // check args
    String lang, cmd;
    PrintWriter out = null;
    int first, last;
    try {
      
      // 'read'
      cmd   = args[0];
      if (!"read".equals(cmd))
        throw new IllegalArgumentException("unknown command "+cmd);

      // en|fr|de
      lang  = args[1];
      months = new DateFormatSymbols(new Locale(lang)).getMonths();
      if (months==null||months.length<12)
        throw new IllegalArgumentException("no month name information for "+lang);
      
      // years
      first = Integer.parseInt(args[2]);
      last  = Integer.parseInt(args[3]);

      // out
      if (args.length>4) 
        out   = getOut(first, last, args[4] , lang);

    } catch (Throwable t) {
      log(true, "java genj.almanac.WikipediaImport read LANGUAGE FIRSTYEAR LASTYEAR [DIROUT]");
      log(false," ("+t.getMessage()+")");
      System.exit(1);
      return;
    }
    
    // run import
    new Wikipedia().read(lang, first, last, out);
    
    // close out
    if (out!=null) {
      out.flush();
      out.close();
    }
    
    // done
  }
  
  /**
   * Log information
   */
  private static void log(boolean system, String msg) {
    if (system)
      System.out.print("*** ");
    System.out.println(msg);
  }
  

  /**
   * Import
   */
  private void read(String lang, int first, int last, PrintWriter out) {

    // put out disclaimer
    if (out!=null) {
      String[] args = new String[]{ lang, ""+first, ""+last };
      for (int i=0;i<DISCLAIMER.length;i++) {
        out.println(new MessageFormat(DISCLAIMER[i]).format(args));
      }
    }
    
    // loop over years
    for (int year=first;year<=last;year++) {
      
      String yyyy = getYYYY(year);
      if (yyyy==null)
        throw new IllegalArgumentException("can't create yyyy from "+year);
      
      try {
        // build URL
        String url = new MessageFormat(URL).format(new String[]{ lang, ""+year});
        
        // try to read exports
        readURL(yyyy, new URL(url), out);
        
        // next
      } catch (IOException e) {
        log(true, "IO error on reading "+year);
      }
      
    }
    
    // report
    log(true, "Read "+imported+" events for '"+lang+"' between "+first+" and "+last);
    
    // done
  }
  
  /**
   * Get Output from dir arg
   */
  private static PrintWriter getOut(int first, int last, String dir, String lang) throws IOException {

    // no dir no out
    if (dir==null)
      return null;
    
    // create appropriate zip-file in dir
    File file = new File(dir, lang+".wikipedia.zip");
    if (!file.exists())
      file.createNewFile();
    
    if (!file.canWrite())
      throw new IllegalArgumentException("can't write "+file);
    
    log(true, "Writing Wikipedia events into "+file);
    
    // prepare unix style line delimiters
    System.setProperty("line.separator", "\n");
    
    // open zip file
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
    
    // create one zip entry
    out.putNextEntry(new ZipEntry(lang+".wikipedia"));
    
    // done
    return new PrintWriter(new OutputStreamWriter(out, UTF8));
  }
  
  /**
   * read from Wikipedia URL
   */
  private void readURL(String yyyy, URL url, PrintWriter out) throws IOException {
    
    // open pipe
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    
    InputStream in = con.getInputStream();
    
    // check what we're getting
    if (con.getResponseCode()!=HttpURLConnection.HTTP_OK) 
      throw new IOException(con.getResponseMessage());
    
    // read
    readPage(yyyy, in, out);
    
    // close it
    con.disconnect();
      
    // done
  }
  
  /**
   * Read Wikipedia history page (UTF-8) from input stream
   */
  private void readPage(String yyyy, InputStream xml, PrintWriter out) throws IOException {
    // we're starting without event section
    group = null;
    // read it line by line
    BufferedReader in = new BufferedReader(new InputStreamReader(xml, UTF8));
    while (true) {
      String line = in.readLine();
      if (line==null||!readLine(yyyy, line, out)) 
        break;
    }
    // done
  }
  
  /**
   * Read wikipedia line of text - we're looking for 
   * the end of the header, groupings and event lines
   */
  private boolean readLine(String yyyy, String line, PrintWriter out) {
    
    // match "== 2ND_LEVEL_GROUP =="
    Matcher matcher = PATTERN_GROUP.matcher(line);
    if (matcher.matches()) {
      int    lineLevel = matcher.group(1).length();
      String lineGroup = unformat(matcher.group(2));
      // are we not in a group yet? 
      if (group==null) {
        if (lineLevel==2)
          group = lineGroup;
        return true;
      }
      // don't go into another level-2 group - stop here
      if (lineLevel==2)
        return false;
      // change group for level-3 groups that are not months
      if (lineLevel==3&&!containsMonth(lineGroup))
        group = unlinkify(lineGroup);
    }
    
    // are we in a group yet?
    if (group==null)
      return true;
    
    // matching an event "* [[15. Juli]] - [[Manitoba]] wird kanadische Provinz"
    Matcher event = PATTERN_EVENT.matcher(line);
    if (event.matches())
      readEvent(yyyy, event, out);
    
    // continue
    return true;
  }
  
  /**
   * Read event e.g. 
   * "* [[15. Juli]] - [[Manitoba]] wird kanadische Provinz"
   */
  private void readEvent(String yyyy, Matcher matcher, PrintWriter out) {
    
    // grab time
    String yyyymmdd = getYYYYMMDD(yyyy, matcher.group(1));
    if (yyyymmdd==null)
      return;
    
    // grab text
    String text = unformat(unlinkify(matcher.group(2).trim()));
    if (text.length()==0)
      return;
    
    // create an event line
    String event = yyyymmdd+"\\"+(group!=null?group:"-")+"\\"+text;
    if (out!=null)
      out.println(event);
    else
      log(false,event);
      
    imported++;
    
    // continue
    return;
  }
  
  /**
   * Turn a year and month/day string into YYYYMMDD - currently
   * this can handle
   * <pre>
   *  1. Januar
   *  January 1
   *  1 janvier
   * </pre>
   */
  private String getYYYYMMDD(String yyyy, String monthday) {
    
    // try month
    Matcher month = PATTERN_MONTH.matcher(monthday);
    if (!month.find())
      return null;
    String mm = getMM(month.group(0));
    if (mm==null)
      return null;

    // try day
    Matcher day = PATTERN_DAY.matcher(monthday);
    if (!day.find())
      return null;
    String dd = getDD(day.group(0));
    if (dd==null)
      return null;
    
    // done
    return yyyy+mm+dd;
    
  }

  /**
   * Turn month name into MM
   */
  private String getMM(String month) {
    
    for (int i=1;i<=months.length;i++) {
      if (months[i-1].equals(month))
        return i<10 ? "0"+i : ""+i;
    }
    
    return null;
  }
  
  /**
   * Turn day into DD
   */
  private String getDD(String day) {

    try {
      int i = Integer.parseInt(day);
      return i<10 ? "0"+i : ""+i;
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  /**
   * Turn year into YYYY
   */
  private String getYYYY(int year) {
    if (year<10)
      return "000"+year;
    if (year<100)
      return "00"+year;
    if (year<1000)
      return "0"+year;
    if (year<10000)
      return ""+year;
    return null;
  }
  
  /**
   * Replace links in text e.g.
   * "[[Manitoba|Manitoba]]" 
   */
  private String unlinkify(String text) {
    
    if (text.length()>0) {
      // match "...[[link|text]]..."
      Matcher matcher = PATTERN_LINK.matcher(text);
      text = matcher.replaceAll("$2");
    }
    
    // done
    return text;
  }
  
  /**
   * take formatting away from text
   */
  private String unformat(String text) {
    // match all patterns
    for (int i=0;i<PATTERNSUBS.length;) {
      Matcher matcher = ((Pattern)PATTERNSUBS[i++]).matcher(text);
      text = matcher.replaceAll(PATTERNSUBS[i++].toString());
    }
    // done
    return text;
  }
  
  /**
   * checks whether a string contains a month name
   */
  private boolean containsMonth(String text) {
    for (int i=0;i<12;i++) {
      if (text.indexOf(months[i])>=0)
        return true;
    }
    return false;
  }
  
} //WikiImport
