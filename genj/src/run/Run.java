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
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    MAIN_CLASS = "Run-Class";
  
  /**
   * Startup runnable with dynamically constructed classpath
   */
  public static void main(String[] args) {
    
    try {
      
      // cd into 'current' directoruy
      cd(Run.class);
      
      // init grabbing our meta-inf configuration
      Manifest mf = getManifest();
      
      // prepare classpath
      String[] classpath = getClasspath(mf);
      
      // tell everyone about it
      setClasspath(classpath);
      
      // prepare classloader
      ClassLoader cl  = getClassLoader(classpath);

      // instantiate class and run main
      Thread.currentThread().setContextClassLoader(cl);
      Class clazz = cl.loadClass(getClass(mf));
      Method method = clazz.getMethod("main", new Class[]{String[].class});
      method.invoke(null, new Object[]{args});
      
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    }

    // nothing more to do here
  }

  /**
   * Try to change into the directory of jar file that contains given class.
   * @param  clazz  class to get containing jar file for
   * @return success or not 
   */
  private static boolean cd(Class clazz) {
    
    try {         
      
      // jar:file:/C:/Program Files/FooMatic/./lib/foo.jar!/foo/Bar.class
      JarURLConnection jarCon = (JarURLConnection)getClassURL(clazz).openConnection();

      // file:/C:/Program Files/FooMatic/./lib/foo.jar
      URL jarUrl = jarCon.getJarFileURL();
  
      // /C:/Program Files/FooMatic/./lib/foo.jar
      File jarFile = new File(URLDecoder.decode(jarUrl.getPath(), "UTF-8"));   
      
      // /C:/Program Files/FooMatic/./lib
      File jarDir = jarFile.getParentFile();

      // cd C:/Program Files/FooMatic/.
      System.setProperty("user.dir", jarDir.getAbsolutePath());

      // done
      return true;
      
    } catch (Exception ex) {

      // didn't work
      System.err.println("Couldn't cd into directory with jar containing "+clazz);
      ex.printStackTrace(System.err);
      
      return false;
    }

  }
  
  /**
   * Get URL of the given class.
   * 
   * @param  clazz  class to get URL for
   * @return the URL this class was loaded from
   */
  private static URL getClassURL(Class clazz) {
    String resourceName = "/" + clazz.getName().replace('.', '/') + ".class";
    return clazz.getResource(resourceName);
  }
  
 /**
   * Get main class from manifest file information
   */
  private static String getClass(Manifest mf) {
    String clazz = mf.getMainAttributes().getValue(MAIN_CLASS);
    if (clazz == null || clazz.length() == 0) {
      throw new Error("No " + MAIN_CLASS + " defined in " + MANIFEST);
    }
    
    return clazz;
  }  
  
  /**
   * create classloader
   */
  private static ClassLoader getClassLoader(String[] classpath) throws MalformedURLException {
    
    URL[] urls = new URL[classpath.length];
    for (int i = 0; i < urls.length; i++) {
      urls[i] = new File(classpath[i]).toURL();
    }
    
    return new URLClassLoader(urls);
  }
  
  /**
   * Set java.class.path
   */
  private static void setClasspath(String[] classpath) {
    
    String separator = System.getProperty("path.separator");
    
    StringBuffer value = new StringBuffer();
    for (int i = 0; i < classpath.length; i++) {
      if (i>0) value.append(separator);
      value.append(classpath[i]);
    }
    
    System.setProperty("java.class.path", value.toString());
  }
  
  /**
   * Assemble classpath from manifest file information (optional)
   */
  private static String[] getClasspath(Manifest mf) throws MalformedURLException {

    String classpath = expandSystemProperties(mf.getMainAttributes().getValue(CLASSPATH));
    List result = new ArrayList();
    
    // collect a list of classloader URLs
    StringTokenizer tokens = new StringTokenizer(classpath, ",", false);
    while (tokens.hasMoreTokens()) {
      String token = tokens.nextToken().trim();
      File file = new File(token).getAbsoluteFile();
      if (!file.exists()) 
        continue;
      buildClasspath(file, result);
      // next token
    }

    // done
    return (String[])result.toArray(new String[result.size()]);
  }
  
  private static void buildClasspath(File file, List result) throws MalformedURLException {
    
    // a simple file?
    if (!file.isDirectory() && file.getName().endsWith(".jar")) {
      result.add(file.getAbsolutePath());
      return;
    }
    
    // recurse into directory
    File[] files = file.listFiles();
    if (files!=null) for (int i=0;i<files.length;i++) 
      buildClasspath(files[i], result);

    // done
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
      if (mf.getMainAttributes().getValue(MAIN_CLASS)!=null)
        return mf;
    }
      
    // not found
    throw new Error("No "+MANIFEST+" with "+MAIN_CLASS+" found");
  }

  /**
   * A helper for resolving system properties in form of ${key} in a string. The pattern
   * we're looking for is ${[.\w]*}
   */
  private static final Pattern PATTERN_KEY = Pattern.compile("\\$\\{[\\.\\w]*\\}");
  private static String expandSystemProperties(String string) {

    StringBuffer result = new StringBuffer();
    Matcher m = PATTERN_KEY.matcher(string);

    int pos = 0;
    while (m.find()) {
      String prefix = string.substring(pos, m.start());
      String key = string.substring(m.start()+2, m.end()-1);
      
      result.append(prefix);
      result.append(System.getProperty(key));
      
      pos = m.end();
    }
    result.append(string.substring(pos));

    return result.toString();
  }

} //Run
