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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;


/**
 * GenJ - ReportTree
 *
 * $Header:
 * @author Tom Morris
 * @version 1.01
 */
public class ReportTrees extends Report {
    
    /**
     * interface when available
     */
    public int minGroupSize = 2;  // Don't print groups with size less than this
    
    /**
     * This method actually starts this report
     */
    public void start(Gedcom gedcom) {
        
        // Get a list of the individuals 
        Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        println(i18n("fileheader",gedcom.getName()));
        
        // Step through all the Individuals we haven't seen yet
        println(i18n("indicount",indis.length)+"\n");
        
        HashSet unvisited = new HashSet(Arrays.asList(indis));
        List trees = new ArrayList();
        while (!unvisited.isEmpty()) {
          Indi indi = (Indi)unvisited.iterator().next();
          
          // start a new sub-tree
          Tree tree = new Tree();
          
          // indi has been visited now
          unvisited.remove(indi);
          
          // collect all relatives
          iterate(indi, tree, unvisited);
          
          // remember
          trees.add(tree);
        }
        
        // Report about groups
        if (!trees.isEmpty()) {
          
          // Sort in descending order by count
          Collections.sort(trees);
            
          // Print sorted list of groups
          println(align(i18n("count"),7, Report.ALIGN_RIGHT)+"  "+i18n("name"));
          println("-------  ----------------------------------------------");
            
            int grandtotal=0;
            int loners=0;
            for (int i=0; i<trees.size(); i++) {
              
              Tree tree = (Tree)trees.get(i);
              
              // sort group entities by birth date
              grandtotal += tree.size();
              if (tree.size()<minGroupSize) 
                loners +=tree.size();
              else
                println(align(""+tree.size(),7, Report.ALIGN_RIGHT)+"  "+tree );
            }
            
            println("");
            println(i18n("grandtotal",grandtotal));
            
            if (loners>0) {
                Object[] msgargs = {new Integer(loners), new Integer(minGroupSize)};
                println("\n"+i18n("loners",msgargs));
            }
            
        }
        println("");
        println(i18n("endreport"));
        
        // Done
        return;
    }

    /**
     * Iterate over an individual who's part of a sub-tree
     */
    private void iterate(Indi indi, Tree tree, Set unvisited) {

      // individuals we need to check
      Stack todos  = new Stack();
      todos.add(indi);

      // loop 
      while (!todos.isEmpty()) {

        Indi todo = (Indi)todos.pop();

        // belongs to group
        tree.add(todo);
        
        // check the ancestors
        Fam famc = todo.getFamilyWhereBiologicalChild();
        if (famc!=null)  {
          Indi mother = famc.getWife();
          if (mother!=null&&unvisited.remove(mother))
            todos.push(mother);
    
          Indi father = famc.getHusband();
          if (father!=null&&unvisited.remove(father))
            todos.push(father);
        }
      
        // check descendants 
        Fam[] fams = todo.getFamiliesWhereSpouse();
        for (int f=0;f<fams.length;f++) {
            
            // Get the family & process the spouse
            Fam fam = fams[f];
            Indi spouse = fam.getOtherSpouse(todo);
            if (spouse!=null&&unvisited.remove(spouse))
              todos.push(spouse);
            
            // .. and all the kids
            Indi[] children = fam.getChildren();
            for (int c = 0; c < children.length; c++) {
              if (unvisited.remove(children[c]))
                todos.push(children[c]);
            }
            
            // next family
        }

        // continue with to-dos
      }
        
      // done
    }
    
    /**
     * A sub-tree of people related to each other
     */
    private class Tree extends HashSet implements Comparable {
      
      private Indi oldestIndividual;
      
      public int compareTo(Object that) {
        return ((Tree)that).size()-((Tree)this).size();
      }
      
      public String toString() {
        return oldestIndividual.getId()+" "+oldestIndividual.getName()+" "+oldestIndividual.getBirthAsString();
      }
      
      public boolean add(Object o) {
        // Individuals expected
        Indi indi = (Indi)o;
        // check if oldest
        if (isOldest(indi))
          oldestIndividual = indi;
        // continue
        return super.add(o);
      }
      
      private boolean isOldest(Indi indi) {
        long jd;
        try {
          jd = oldestIndividual.getBirthDate().getStart().getJulianDay();
        } catch (Throwable t) {
          return true;
        }
        try {
          return indi.getBirthDate().getStart().getJulianDay() < jd;
        } catch (Throwable t) {
          return false;
        }
        
      }
      
    } //Tree
    
} //ReportTrees
