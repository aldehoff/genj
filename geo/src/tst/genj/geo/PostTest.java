/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Test posting to our server side query script
 */
public class PostTest {

  private static Charset UTF8 = Charset.forName("UTF8");
  
  /**
   * our main
   */
  public static void main(String[] args) {
    try {
      post();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  /**
   * posting
   */
  private static void post() throws Throwable {

    System.setProperty("http.proxyHost","torisaw01.prod.quest.corp");
    System.setProperty("http.proxyPort","8080");

    URL url = new URL("http://genj.sourceforge.net/geoq.php");
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("POST");
    con.setDoOutput(true);
    con.setDoInput(true);
    
    Writer out = new OutputStreamWriter(con.getOutputStream(), UTF8);
    out.write("GEOQ\n");
    out.write("Lohmar\n");
    out.write("Siegburg;Nordrhein-Westfalen\n");
    out.write("Köln;;de\n");
    out.write("Rendsburg\n");
    out.write("Celle\n");
    out.write("Celle;Niedersachsen;de\n");
    out.write("Ham*\n");
    out.close();

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), UTF8));
    while (true) {
      String line = in.readLine();
      if (line==null) break;
      System.out.println(line);
    }
    in.close();
    
  }

}
