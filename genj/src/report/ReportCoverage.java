/**
 * ReportCoverage - 
 *
 * Copyright (c) 2003 Tom Morris
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

/**
 * GenJ -  ReportCoverage
 * @author Tom Morris
 * @version 1.0
 */
public class ReportCoverage extends Report {

  /** this report's version (not localized)*/
  public static final String VERSION = "1.0";
  
  /**
   * Returns the version of this script
   */
  public String getVersion() {
    return VERSION;
  }
  
  /**
   * Returns the name of this report 
   */
  public String getName() {
    return i18n("reportname");
  }  

  /**
   * Author - this string doesn't get localized
   */
  public String getAuthor() {
    return "Tom Morris";
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return i18n("description");
  }

  /**
   * @see genj.report.Report#accepts(java.lang.Object)
   */
  public String accepts(Object context) {
    // we accept GEDCOM or Individuals 
    return context instanceof Indi || context instanceof Gedcom ? getName() : null;  
  }
  

	final int maxdepth = 100;
	int[] count;

  /**
   * This method actually starts this report
   */
  public void start(Object context) {
		int depth;
		Indi indi;

		// If we were passed a person to start at, use that
    if (context instanceof Indi) {
      indi = (Indi)context;
    } else {
    // Otherwise, ask the user select the root of the tree for analysis
			Gedcom gedcom=(Gedcom)context;
			indi = (Indi)getEntityFromUser (
				 i18n("select"), // msg
				 gedcom,                        // our gedcom instance
				 Gedcom.INDIVIDUALS,            // type INDIVIDUALS
				 "INDI:NAME"                    // sort by name
				 );
		}

    if (indi==null) {
      return;
    }
    
		// Allocate and initialize our count of ancestors at each generation
		count = new int[maxdepth];
		for (int i=0; i<count.length; i++) {
			count[i]=0;
		}

		println(i18n("root",indi.getName()));

    // Count the ancestors (recursively)
    depth = parent(indi, 1);

		if (depth>maxdepth) {
			println(i18n("error_maxdepth",count.length+""));
			return;
		}
		else {
	
			// Print the results
			// FIXME: this should use locale specific formatting for numbers & %s
			println(justify(i18n("gen"),5)+
										 justify(i18n("possible"),14)+
										 justify(i18n("count"),10)+
										 justify(i18n("percent"),6)+
										 justify(i18n("cumulative"),10)+
										 justify(i18n("percent"),6)
										 );

			int cum_count = 0;
			int cum_poss = 0;
			for (int i=1; i<depth+1; i++) {
				int poss = pow2(i);
				cum_count += count[i];
				cum_poss += poss;
				println(justify(i,5)+
											 justify(poss,14)+
											 justify(count[i],10)+
											 justify((count[i]*100/poss),5)+"%"+
											 justify(cum_count,10)+
											 justify((cum_count*100/cum_poss),5)+"%"
											 );
			}
			
			return;
		}
	}
  
  /**
   * parent - collects information about one parent and then recurses
   */
  private int parent(Indi indi, int level) {

    int maxdepth = level;
		int depth;

		count[level]++;

    Fam famc = indi.getFamc();

    if (famc!=null) {
			if (famc.getWife()!=null) {
        depth = parent(famc.getWife(), level+1);
				if (depth>maxdepth) {
					maxdepth = depth;
				}
			}
			if (famc.getHusband()!=null) {
        depth = parent(famc.getHusband(), level+1);
				if (depth>maxdepth) {
					maxdepth = depth;
				}
			}
		}

		return maxdepth;
  }
  
  /**
   * Helper that returns a power of 2
   */
  private int pow2(int level) {
    int result=1;
    while (--level>0) {
	result=result*2;
      }
      return result;
  }

}
