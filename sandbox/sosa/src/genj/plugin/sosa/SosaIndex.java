/**
 * This source file is part of a GenJ Plugin and copyright of the respective authors.
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genj.plugin.sosa;

import genj.gedcom.Indi;

import java.util.logging.Logger;

/**
 * A sample plugin that manages Sosa Indexation of individuals
 */
public class SosaIndex extends Index {
  
  private final static Logger LOG = Logger.getLogger("genj.plugin.sosa");

  public static final String TAG = "_SOSA";
  public static final String SEPARATOR = ";";

///* we initialise enum constants */
//BIOLOGICAL_BROTHER_AND_SISTER("(  +)"), BIOLOGICAL_BROTHER_AND_SISTER_SPOUSE("( ++)"), OTHER_BROTHER_AND_SISTER("( ~+)"), OTHER_BROTHER_AND_SISTER_SPOUSE("(~++)");

  @Override
  public String getName() {
    return "Sosa";
  }
  
  @Override
  protected String getTag() {
    return TAG;
  }

  /**
   * This method sets sosa indexation starting from root individual
   * Assuming old index is cleared already.
   */
  public void reindex() {
    
    Indi root = getRoot();
    if (root==null)
      throw new IllegalArgumentException("reindex requires root");
    
    /* we set Sosa root value */
    int sosaIndex = 1;
    
  }


}
