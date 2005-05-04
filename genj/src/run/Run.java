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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Manifest;

/**
 * A program starter that reads a classpath and runnable type from manifest-file. The
 * comma-separated classpath is expanded (each entry that is a directory is scanned
 * for jar-files) and the runnable invoked with the resulting classpath.
 * 
 * An applicable manifest file should roughly look like this example: META-INF/MANIFEST.MF
 * 
 * <pre>
 *   Main-Class: Run
 *   Run-Classpath: ./lib
 *   Run-Runnable: some.package.and.Runnable </pre>
 *   
 * The manifest entry Main-Class makes sure that Run can be started via
 * <pre>
 *   java -jar thejar.jar
 * </pre>
 */
public class Run {

  public final static String 
    MANIFEST = "META-INF/MANIFEST.MF",
    CLASSPATH = "Run-Classpath",
    RUNNABLE = "Run-Runnable";
  
  /**
   * Startup runnable with dynamically constructed classpath
   */
  public static void main(String[] args) {
    
    try {
      
      // init grabbing our meta-inf configuration
      Manifest mf = getManifest();
      
      // lookup classpath
      URL[] classpath = getClasspath(mf);
      
      // lookup runnable
      String runnable = getRunnable(mf);
      
      // instantiate classloader and run GenJ
      ClassLoader cl  = new URLClassLoader(classpath);
      Thread.currentThread().setContextClassLoader(cl);
      Runnable app = (Runnable)cl.loadClass(runnable).newInstance();
      app.run();
      
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    }

    // nothing more to do here
  }

  /**
   * Get runnable from manifest file information
   */
  private static String getRunnable(Manifest mf) {
    String runnable = mf.getMainAttributes().getValue(RUNNABLE);
    if (runnable==null||runnable.length()==0)
      throw new Error("No "+RUNNABLE+" defined in "+MANIFEST);
    return runnable;
  }
  
  /**
   * Assemble classpath from manifest file information (optional)
   */
  private static URL[] getClasspath(Manifest mf) throws MalformedURLException {

    String classpath = mf.getMainAttributes().getValue(CLASSPATH);
    if (classpath==null)
      classpath = "";
    
    // collect a list of classloader URLs
    StringTokenizer tokens = new StringTokenizer(classpath, ",", false);
    List result = new ArrayList();
    while (tokens.hasMoreTokens()) {
      File file = new File(tokens.nextToken());
      if (!file.exists())
        continue;
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (int i=0;i<files.length;i++) 
          result.add(files[i].toURL());
      } else {
        result.add(file.toURL());
      }
      // next token
    }

    // done
    return (URL[])result.toArray(new URL[result.size()]);
  }
  
  /**
   * Get our manifest file. Normally all (parent) classloaders of a class do provide
   * resources and the enumeration returned on lookup of manifest.mf will start
   * with the topmost classloader's resources. 
   * We're inverting that order to make sure we're consulting the manifest file in 
   * the same jar as this class if available.
   */
  private static Manifest getManifest() throws IOException {

    // find all manifest files
    Stack manifests = new Stack();
    for (Enumeration e = Run.class.getClassLoader().getResources(MANIFEST); e.hasMoreElements(); )
      manifests.add(e.nextElement());
    
    // it has to have the runnable attribute
    while (!manifests.isEmpty()) {
      URL url = (URL)manifests.pop();
      InputStream in = url.openStream();
      Manifest mf = new Manifest(in);
      in.close();
      // careful with key here since Attributes.Name are used internally by Manifest file
      if (mf.getMainAttributes().getValue(RUNNABLE)!=null)
        return mf;
    }
      
    // not found
    throw new Error("No "+MANIFEST+" with "+RUNNABLE+" found");
  }

} //Run
