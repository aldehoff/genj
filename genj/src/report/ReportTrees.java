/**
 * ReportTree
 *
 * Copyright (c) 2003 Tom Morris
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;


/**
 * GenJ - ReportTree
 *
 * $Header:
 * @author Tom Morris
 * @version 1.0
 */
public class ReportTrees extends Report {
    
    /**
     * FIXME The parameter below should be set by a configuration
     * interface when available
     */
    public int minTreeSize = 2;  // Don't print trees with count less than this
    
    /** this report's version */
    static final String VERSION = "1.0";
    
    /**
     * Returns the version of this script
     */
    public String getVersion() {
        return VERSION;
    }
    
    /**
     * Returns the name of this report - should be localized.
     */
    public String getName() {
        return i18n("reportname");
    }
    
    /**
     * Author
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
    
    private static class Statistics{
        int numTrees = 0;
        AncTree[] trees;
        Hashtable seen;
        
        Statistics(int maxsize) {
            trees = new AncTree[maxsize];
            seen = new Hashtable(maxsize);
        }
    }
    
    private static class AncTree {
        int count;    // total number of connected people
        String name;  // name of first person encountered
    }
    
    /**
     * This method actually starts this report
     */
    public void start(Object context) {
        Gedcom gedcom = (Gedcom)context;
        
        // Get a list of the individuals and create a stati
        Entity[] indis = gedcom.getEntities(gedcom.INDI, "INDI:NAME");
        Statistics stats = new Statistics(indis.length);
        
        println(i18n("fileheader",gedcom.getName()));
        
        // Step through all the Individuals
        println(i18n("indicount",indis.length)+"\n");
        
        for (int e=0;e<indis.length;e++) {
            Indi indi = (Indi)indis[e];
            // If we haven't seen them yet, it's the beginning of a new tree
            if ( !stats.seen.containsKey(indi.getId()) ) {
                int curTree = stats.numTrees++;
                stats.trees[curTree] = new AncTree();
                stats.trees[curTree].name = "@"+indi.getId()+"@ "+indi.getName();
                stats.trees[curTree].count = analyzeIndividual(indi, stats);
                if( stats.trees[curTree].count > minTreeSize ) {
                    Object[] msgargs = {new Integer(curTree),
                    stats.trees[curTree].name,
                    new Integer(stats.trees[curTree].count)};
                    // println(i18n("treecount", msgargs));
                }
            }
        }
        
        if(stats.trees.length>0) {
            // Sort in descending order by count
            Arrays.sort(stats.trees, 0, stats.numTrees-1, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int c1 = ((AncTree)o1).count;
                    int c2 = ((AncTree)o2).count;
                    return c2-c1;
                }
            });
            
            // Print sorted list
            println(align(i18n("count"),7)+"  "+i18n("name"));
            println("-------  ----------------------------------------------");
            
            int grandtotal=0;
            int loners=0;
            for (int i=0; i<stats.numTrees; i++) {
                int count=stats.trees[i].count;
                if (count<minTreeSize) {
                    loners +=count;
                } else {
                    grandtotal+=count;
                    println(align(count,7)+"  "+stats.trees[i].name);
                }
            }
            
            println("");
            println(i18n("grandtotal",grandtotal));
            
            if (loners>0) {
                Object[] msgargs = {new Integer(loners), new Integer(minTreeSize)};
                println("\n"+i18n("loners",msgargs));
            }
        }
        println("");
        println(i18n("endreport"));
        
        // Done
        return;
    }
    
    /**
     * Analyze an individuals ancestor & descendants
     * keeping track of who we've seen already
     */
    private int analyzeIndividual(Indi indi, Statistics stats) {
        if ( stats.seen.containsKey(indi.getId()) ) {
            return 0;
        }
        else {
            // insert in hash
            stats.seen.put(indi.getId(),indi);
            
            // count relatives (including self)
            return (1 +
            analyzeIndividualAncestors(indi, stats) +
            analyzeIndividualDescendants(indi, stats)
            );
        }
    }
    
    /**
     * Analyzes an Individual's Ancestors
     */
    private int analyzeIndividualAncestors(Indi indi, Statistics stats) {
        int count = 0;
        
        // Get family that we are a child of
        Fam famc = indi.getFamc();
        if (famc==null) {
            return 0;
        }
        
        if (famc.getWife()!=null) {
            count += analyzeIndividual(famc.getWife(), stats);
        }
        if (famc.getHusband()!=null) {
            count += analyzeIndividual(famc.getHusband(), stats);
        }
        
        return count;
        
    }
    
    /**
     * Analyzes an Individual's Descendants
     */
    private int analyzeIndividualDescendants(Indi indi, Statistics stats) {
        int count = 0;
        
        // loop through all families
        int fcount = indi.getNoOfFams();
        for (int f=0;f<fcount;f++) {
            
            // Get the family & process the spouse
            Fam fam = indi.getFam(f);
            if (fam.getOtherSpouse(indi)!=null) {
                count += analyzeIndividual(fam.getOtherSpouse(indi), stats);
            }
            
            // .. and all the kids
            Indi[] children = fam.getChildren();
            for (int c = 0; c < children.length; c++) {
                count += analyzeIndividual(children[c], stats);
            }
            
            // .. next family
        }
        
        return count;
    }
    
}
