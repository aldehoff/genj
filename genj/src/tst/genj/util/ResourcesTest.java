/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

/**
 * Test language resources for completeness
 */
public class ResourcesTest extends TestCase {
  
  private final static File dir = new File("./language");

  /** check english resources against translations */
  public void testCompleteness() {
    
    try {
      diff("en", "de");
    } catch (IOException e) {
      fail("ioex during file operations");
    }
    
    // done
  }
  
  /**
   * Run externally
   */
  public static void main(String[] args) {
    String translation = Locale.getDefault().getLanguage();
    if (translation.equals("en")) {
      System.out.println("Set language to diff with -Duser.language=xx");
      return;
    }
    System.out.println("Diffing en against "+translation);
    try  {
      new ResourcesTest().diff("en", translation);
    } catch (IOException e) {
      System.out.println("IOException during diff: "+e.getMessage());
    }
  }
  
  /**
   * Diff directories
   */
  private void diff(String original, String translation) throws IOException {
    
    // diff it
    int diffs = diffResources(loadResources(original), loadResources(translation));

    if (diffs==0)
      System.out.println("No differences found - Good Job!");
    else
      System.out.println(diffs+" differences found");
    // done
  }
  
  /** 
   * Diff resources of the original vs the translation
   */
  private int diffResources(Map originals, Map translations) {
    
    int diffs = 0;

    // go package by package
    for (Iterator packages = originals.keySet().iterator(); packages.hasNext(); ) {
      // grab package info and resources in original and translation
      String pckg = (String)packages.next();
      Resources original = (Resources)originals.get(pckg);
      Resources translation = (Resources)translations.get(pckg);
      if (translation==null)
        System.out.println(pckg+",*,not translated");
      else
        diffs += diffResource(pckg, original, translation);
    }
    
    // done
    return diffs;
  }
  
  private int diffResource(String pckg, Resources original, Resources translation) {
    int diffs = 0;
    // go key bey key
    for (Iterator keys = original.getKeys().iterator(); keys.hasNext(); ) {
      // grab key, original value and translated value
      String key = (String)keys.next();
      String val1 = (String)original.getString(key);
      String val2 = (String)translation.getString(key, false);
      // any uppercase in it and we assume it doesn't need to be translated
      if (!key.toLowerCase().equals(key))
        continue;
      // check translation
      if (val2==null) {
        System.out.println(pckg+","+key+",not translated");
        diffs++;
      } else {
        try {
          int fs1 = Resources.getMessageFormat(val1).getFormats().length;
          int fs2 = Resources.getMessageFormat(val2).getFormats().length;
          if (fs1!=fs2) {
            System.out.println(pckg+","+key+",wrong # of {n}s");
            diffs++;
          }
        } catch (IllegalArgumentException e) {
          // some values contain e.g. '{n}' which doesn't go with MessageFormat - ignored
        }
      }
      // next key
    }
    // check for unnecessary keys in translation
    for (Iterator keys = translation.getKeys().iterator(); keys.hasNext(); ) {
      // grab key and check against original
      String key = (String)keys.next();
      // any uppercase in it and we assume it's something special
      if (!key.toLowerCase().equals(key))
        continue;
      // compare!
      if (!original.contains(key)) {
        System.out.println(pckg+","+key+",translated but not in original");
        diffs++;
      }
    }
    // done
    return diffs;
  }
  
  /**
   * Load language resources for given language
   * @return mapping of package to Resources
   */
  private Map loadResources(String language)  throws IOException {
    return loadResources(new File(dir, language),  "", new TreeMap());
  }
  
  private Map loadResources(File dir, String pckg, Map result) throws IOException {
    
    // check files in dir
    File[] resources = dir.listFiles();
    for (int i=0;i<resources.length;i++) {
      File resource = resources[i];
      if (resource.isDirectory())
        loadResources(resource, pckg+"/"+resource.getName(), result);
      else if (resource.getName().endsWith(".properties"))
        result.put(pckg, new Resources(new FileInputStream(resource)));
    }
    
    // done
    return result;
  }
  
}
