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
package genj.tree;

import java.util.*;
import java.awt.*;

import genj.gedcom.*;

/**
 * The model underlying a tree
 */
public class TreeModel implements GedcomListener, Cloneable {

  /**
   * Tree settings
   */
  private Gedcom             gedcom;
  private Entity             rootEntity;
  private Link               rootLink;
  private Link               actualLink;

  private Dimension          sizeOfIndis, sizeOfFams;

  private int                spaceBetweenPartners,
                             spaceBetweenSiblings,
                             spaceBetweenAncestors,
                             spaceBetweenGenerations;

  private Dimension          size = new Dimension(0,0);

  private boolean            isStickToRoot;
  private boolean            isVertical   ;

  private int                distOfGenerations,
                             distOfPartners,
                             distOfSiblings,
                             distOfAncestors,
                             distOfEntities,
                             distOfIndiPlusMinus,
                             distOfFamPlusMinus;

  private Font               font;

  private Proxy[]            indisProxies, famsProxies;

  /**
   * Handling of Links
   */
  private SetOfLinks         links   = new SetOfLinks();
  private Vector             stoppers= new Vector();

  /**
   * Meta Information
   */
  private Vector             listeners = new Vector();

  /**
   * Default TagPaths for Indi's proxies
   */
  private static final String[] defIndisPaths = {
    "INDI:NAME",
    "INDI:SEX" ,
    "INDI:BIRT:DATE" ,
    "INDI:BIRT:PLAC" ,
    "INDI:OBJE:FILE"
  };

  /**
   * Default Rectangles for Indis proxies
   */
  private static final Rectangle[] defIndisBoxes = {
    new Rectangle( 8, 8,128,18),
    new Rectangle( 8,28,128,18),
    new Rectangle( 8,48,128,18),
    new Rectangle( 8,68,128,18),
    new Rectangle(99, 8, 60,80)
  };

  /**
   * Default TagPaths for Fam's proxies
   */
  private static final String   [] defFamsPaths = {
    "FAM:MARR:DATE"
  };

  /**
   * Default Rectangle for Fam's proxies
   */
  private static final Rectangle[] defFamsBoxes = {
    new Rectangle( 1, 1,80,14)
  };

  /**
   * Default size of Indis
   */
  private static final Dimension defSizeOfIndis = new Dimension(160,90);

  /**
   * Default size of Fams
   */
  private static final Dimension defSizeOfFams  = new Dimension(80,20);

  /**
   * Interface for Action of links
   */
  public interface Action {

  /**
   * Does action on given link
   * @param on link to do action on
   * @return true to continue iteration
   */
  public boolean doAction(Link on);
  }

  /**
   * Constructor
   * @param gedcom Gedcom to display tree of
   */
  public TreeModel(Gedcom setGedcom, String setRootId, String[] setStoppersId, Proxy[] setIndisProxies, Proxy[] setFamsProxies, Dimension setSizeOfIndis, Dimension setSizeOfFams, boolean setVertical, Font setFont) {

    // Setup initial values
    gedcom = setGedcom;

    if (setIndisProxies==null)
      indisProxies = generateProxies(defIndisPaths,defIndisBoxes);
    else
      indisProxies = setIndisProxies;

    if (setFamsProxies==null)
      famsProxies  = generateProxies(defFamsPaths ,defFamsBoxes );
    else
      famsProxies  = setFamsProxies ;

    if (setSizeOfIndis==null)
      sizeOfIndis  = defSizeOfIndis ;
    else
      sizeOfIndis  = setSizeOfIndis ;

    if (setSizeOfFams ==null)
      sizeOfFams   = defSizeOfFams  ;
    else
      sizeOfFams   = setSizeOfFams  ;

    isVertical     = setVertical    ;

    if (setRootId!=null) {
      try {
      rootEntity =gedcom.getEntityFromId(setRootId);
      } catch (DuplicateIDException die) {
      }
    }
    if ((rootEntity==null)&&(gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()>0)) {
      rootEntity = gedcom.getIndi(0);
    }

    if (setStoppersId==null)
      stoppers     = new Vector(10);
    else {
      stoppers     = new Vector(setStoppersId.length);

      Entity e;
      for (int i=0;i<setStoppersId.length;i++) {
      try {
        e = gedcom.getEntityFromId(setStoppersId[i]);
        if (e!=null)
        stoppers.addElement(e);
      } catch (DuplicateIDException die) {
      }
      }
    }

    font = setFont;

    // Gather tree
    gatherTree();

    // Done
  }

  /**
   * Add listener
   */
  public void addTreeModelListener(TreeModelListener listener) {

    // Attach to gedcom ?
    if (listeners.size()==0) {
      gedcom.addListener(this);
      gatherTree();
    }

    // Remember
    listeners.addElement(listener);
  }

  /**
   * Clones this model
   */
  public Object clone() {

    // Clone it
    TreeModel m;
    try {
      m = (TreeModel)super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen because Object is Cloneable
      throw new InternalError();
    }

    // Fill data

    // Done
    return m;
  }

  /**
   * Fire actual change
   */
  private void fireActualChanged(Link oldActual,Link newActual,boolean delegateToGedcom) {

    Enumeration ls = listeners.elements();
    while (ls.hasMoreElements()) {
      ((TreeModelListener)ls.nextElement()).handleActualChanged(oldActual,newActual);
    }

    // Signal actual change to gedcom
    if (delegateToGedcom) {
      gedcom.fireEntitySelected(this,actualLink.getEntity(),false);
    }

  }

  /**
   * Fire data changed - data has changed so that repaint may be necessary
   */
  private void fireDataChanged() {

    Enumeration ls = listeners.elements();
    while (ls.hasMoreElements()) {
      ((TreeModelListener)ls.nextElement()).handleDataChanged();
    }
  }

  /**
   * Fire notification because of changed entities
   */
  private void fireEntitiesChanged(Vector changed) {

    Enumeration ls = listeners.elements();
    while (ls.hasMoreElements()) {
      ((TreeModelListener)ls.nextElement()).handleEntitiesChanged(changed);
    }
  }

  /**
   * Fire layout changed event
   */
  private void fireStructureChanged() {

    Enumeration ls = listeners.elements();
    while (ls.hasMoreElements()) {
      ((TreeModelListener)ls.nextElement()).handleStructureChanged();
    }
  }

  /**
   * Flips stopping property of entity in tree
   */
  public boolean flipStopper(Entity entity) {

    boolean result;

    if (isStopper(entity)) {
      stoppers.removeElement(entity);
      result = false;
    } else {
      stoppers.addElement(entity);
      result = true;
    }

    gatherTree();

    return result;
  }

  /**
   * Calculate a tree of individuals, a root fam on bottom and all its ancestors
   * above by creating links to persons for all visible persons - fills a set of
   * links with persons on the left/right of the tree for all generations
   */
  private Link gatherAncestorsOf(Fam root,SetOfLinks linksOnTheLeft,SetOfLinks linksOnTheRight) {

    // Initialize some VARs
    SetOfLinks retLinksOnTheLeft,retLinksOnTheRight;
    Link       link;
    LinkToFam  linkToFam=null;
    int                      indexOfFather,indexOfMother,indexOfAncestors;
    int                      distance;

    indexOfAncestors = links.getSize()+1;

    // Gather father in root
    gatherAncestorsOf(root.getHusband(),linksOnTheLeft,linksOnTheRight);
    indexOfFather = links.getSize();

    // ... and mother in root
    retLinksOnTheLeft =new SetOfLinks();
    retLinksOnTheRight=new SetOfLinks();
    gatherAncestorsOf(root.getWife(),retLinksOnTheLeft,retLinksOnTheRight);
    indexOfMother = links.getSize();

    // ... calculate how near partners can be positioned
    distance=0;
    for (int i=1;;i++) {
      if ((retLinksOnTheLeft.getSize()<i)||(linksOnTheRight.getSize()<i)) break;
      distance = java.lang.Math.max( distance
                     , linksOnTheRight.getLink(i).getPosInGen()
                     - retLinksOnTheLeft.getLink(i).getPosInGen()
                     + distOfAncestors
      );
    }

    // ... move mother's beside father's tree
    for (int i=indexOfFather+1; i<=links.getSize(); i++) {
      links.getLink(i).moveBy(0,distance);
    }

    // ... update left&right links of root-tree
    for (int i=1;i<=retLinksOnTheRight.getSize();i++) {
      linksOnTheRight.changeLink(i,retLinksOnTheRight.getLink(i));
    }

    for (int i=linksOnTheLeft.getSize()+1;i<=retLinksOnTheLeft.getSize();i++) {
      linksOnTheLeft.changeLink(i,retLinksOnTheLeft.getLink(i));
    }

    // ... position mother's and father's tree centered above 0
    distance = ( linksOnTheRight.getLink(1).getPosInGen()
                -linksOnTheLeft .getLink(1).getPosInGen() ) / 2;
    for (int i=indexOfAncestors;i<=links.getSize();i++) {
      links.getLink(i).moveBy(-distOfEntities,-distance);
    }

    // ... Position Parents
    links.getLink(indexOfFather).setPosInGen(-distOfPartners/2);
    links.getLink(indexOfMother).setPosInGen(distOfPartners/2);

    // ... add a link for the family
    linkToFam = new LinkToFam(0,0,root);
    links.addLink(linkToFam);

    // Done
    return linkToFam;
  }

  /**
   * Calculate a tree of individuals, root on bottom and all its ancestors above
   * by creating links to persons for all visible persons - fills a set of links
   * with persons on the left/right of the tree for all generations
   */
  private Link gatherAncestorsOf(Indi root,SetOfLinks linksOnTheLeft,SetOfLinks linksOnTheRight) {
    // Initialize some VARs
    SetOfLinks retLinksOnTheLeft,retLinksOnTheRight;
    Link       link;
    LinkToFam  linkToFam=null;
    int        indexOfFather,indexOfMother,indexOfAncestors;
    int        distance;

    // Is root an indi or a missing person (=null) ?
    if (root != null) {
      // Gather parents of root
      Fam fam = root.getFamc();
      if ( (fam != null) && (!isStopper(root)) ){

      // ... remember new links
      indexOfAncestors=links.getSize()+1;

      // ... build a tree for father
      gatherAncestorsOf(fam.getHusband(),linksOnTheLeft,linksOnTheRight);
      indexOfFather = links.getSize();

      // ... build a tree for mother
      retLinksOnTheLeft =new SetOfLinks();
      retLinksOnTheRight=new SetOfLinks();
      gatherAncestorsOf(fam.getWife(),retLinksOnTheLeft,retLinksOnTheRight);
      indexOfMother = links.getSize();

      // ... calculate how near partners can be positioned
      distance=0;
      for (int i=1;;i++) {
        if ((retLinksOnTheLeft.getSize()<i)||(linksOnTheRight.getSize()<i)) break;
        distance = java.lang.Math.max( distance
                       ,  linksOnTheRight.getLink(i).getPosInGen()
                      - retLinksOnTheLeft.getLink(i).getPosInGen()
                      + distOfAncestors);
      }

      // ... move mother's beside father's tree
      for (int i=indexOfFather+1; i<=links.getSize(); i++)
        links.getLink(i).moveBy(0,distance);

      // ... update left&right links of root-tree
      for (int i=1;i<=retLinksOnTheRight.getSize();i++)
        linksOnTheRight.changeLink(i,retLinksOnTheRight.getLink(i));
      for (int i=linksOnTheLeft.getSize()+1;i<=retLinksOnTheLeft.getSize();i++)
        linksOnTheLeft.changeLink(i,retLinksOnTheLeft.getLink(i));

      // ... position mother's and father's tree centered above 0
      distance = ( linksOnTheRight.getLink(1).getPosInGen()
            -linksOnTheLeft .getLink(1).getPosInGen() ) / 2;
      for (int i=indexOfAncestors;i<=links.getSize();i++)
        links.getLink(i).moveBy(-distOfGenerations,-distance);

      // ... Position Parents
      links.getLink(indexOfFather).setPosInGen(-distOfPartners/2);
      links.getLink(indexOfMother).setPosInGen(distOfPartners/2);

      // ... add a link for the family
      linkToFam = new LinkToFam(-distOfGenerations+distOfEntities,0,fam);
      links.addLink(linkToFam);

      // ... done with parents
      }

      // ... make a link for a plus of that fam
      if (root.getFamc()!=null) {
      if (isStopper(root))
        link=new PlusMinus(this,-distOfIndiPlusMinus,0,root,false);
      else
        link=new PlusMinus(this,-distOfGenerations+distOfEntities+distOfFamPlusMinus,0,root,true);
      links.addLink(link);
      }

      // Done with parental family
    }

    // Add root of tree (depthInTree=0;positionInGen=0)
    link=new LinkToIndi(0,0,root);
    links.addLink(link);

    if (linkToFam!=null)
      linkToFam.addLinkToChild(link);

    // Add entries in list of left/right links of tree for top generation
    linksOnTheLeft.addLinkAt(1,link);
    linksOnTheRight.addLinkAt(1,link);

    // Done
    return link;
  }

  /**
   * Calculate a tree of individuals, fam as root on top and all its descendants below
   * by creating links to persons for all visible persons - fills a set of links with
   * persons on the left/right of the tree for all generations
   */
  private Link gatherDescendantsOf(Fam root,SetOfLinks linksOnTheLeft,SetOfLinks linksOnTheRight,LinkToFam presetRoot) {

    // Initialize some VARs
    LinkToFam  linkToRoot=null;
    SetOfLinks retLinksOnTheLeft,
           retLinksOnTheRight,
           children= new SetOfLinks();
    Link       link,linkOfChild;
    int        indexOfFirstChild,indexOfChildren;
    int        distance;

    // Gather all children of root family
    if (!isStopper(root)) {

      indexOfFirstChild = links.getSize()+1;
      for (int c=1;c<=root.getNoOfChildren();c++) {

      indexOfChildren=links.getSize()+1;

      // ... build a tree for every child (child will be the last in the visible vector)
      retLinksOnTheLeft =new SetOfLinks();
      retLinksOnTheRight=new SetOfLinks();
      linkOfChild = gatherDescendantsOf(root.getChild(c-1),retLinksOnTheLeft,retLinksOnTheRight,null);

      // ... remember child
      children.addLink(linkOfChild);

      // ... calculate how near children can be positioned
      distance=0;
      if (c>1) {

        for (int i=1;;i++) {
        if ((retLinksOnTheLeft.getSize()<i)||(linksOnTheRight.getSize()<i)) break;
        distance = java.lang.Math.max( distance
                       , linksOnTheRight.getLink(i).getPosInGen()
                       - retLinksOnTheLeft.getLink(i).getPosInGen()
                       + distOfSiblings );
        }

        // ... move child beside others
        for (;indexOfChildren<=links.getSize();indexOfChildren++) {
        link=links.getLink(indexOfChildren);
        link.moveBy(0,distance);
        }

      }

      // ... update left&right links of root-tree
      for (int i=1;i<=retLinksOnTheRight.getSize();i++)
        linksOnTheRight.changeLink(i,retLinksOnTheRight.getLink(i));
      for (int i=linksOnTheLeft.getSize()+1;i<=retLinksOnTheLeft.getSize();i++)
        linksOnTheLeft.changeLink(i,retLinksOnTheLeft.getLink(i));

      // ... next child
      }

      // Position Children centered under distOfGenerations-distOfEntities,distOfPartners/2
      if (linksOnTheRight.getSize()>0) {
      distance = ( linksOnTheRight.getLink(1).getPosInGen()
            -linksOnTheLeft .getLink(1).getPosInGen()
            ) / 2;
            // - distOfPartners ) / 2;

      for (int i=indexOfFirstChild;i<=links.getSize();i++) {
        link=links.getLink(i);
        link.moveBy(distOfGenerations-distOfEntities,-distance);
      }
      }

      // ... done with children
    }

    // Add root of tree (depthInTree=0;positionInGen=0)
    if (presetRoot==null) {
      linkToRoot=new LinkToFam(0,0,root);
      links.addLink(linkToRoot);
    } else {
      linkToRoot=presetRoot;
    }

    // ... make a link for a plus/minus stopper of root fam
    if (root.getNoOfChildren()>0)
      links.addLink(new PlusMinus(this,distOfFamPlusMinus,0,root,true));

    // Add chilren to root fam of tree
    for (int i=1;i<=children.getSize();i++)
      linkToRoot.addLinkToChild(children.getLink(i));

    // Done
    return linkToRoot;
  }

  /**
   * Calculate a tree of individuals, root on top and all its descendants below
   * by creating links to persons for all visible persons - fills a set of links
   * with persons on the left/right of the tree for all generations
   */
  private Link gatherDescendantsOf(Indi root,SetOfLinks linksOnTheLeft,SetOfLinks linksOnTheRight,LinkToIndi presetRoot) {

    // Initialize some VARs
    LinkToFam  linkToFam;
    SetOfLinks retLinksOnTheLeft,retLinksOnTheRight,fams;
    Link       link,linkOfChild,linkOfRoot=null;
    int        indexOfFirstChild,indexOfChildren,indexOfLastPartner=0,indexOfFirstPartner=0;
    int        distance;

    // Gather all children of root
    indexOfFirstChild = links.getSize()+1;
    fams=new SetOfLinks();

    for (int f=1;f<=root.getNoOfFams();f++) {

      // ... calculate current family
      Fam family=root.getFam(f-1);

      // ... make a link for that fam
      linkToFam = new LinkToFam(distOfEntities
                   ,f*distOfPartners-distOfPartners/2
                   ,family);
      fams.addLink(linkToFam);

      // ... make a link for a plus/minus stopper of that fam
      if (family.getNoOfChildren()>0)
      fams.addLink(new PlusMinus(this,
                     distOfEntities+distOfFamPlusMinus,
                     f*distOfPartners-distOfPartners/2,
                     family,true));

      // ... go through children
      if (!isStopper(family))
      for (int c=1;c<=family.getNoOfChildren();c++) {

      indexOfChildren=links.getSize()+1;

      // ... build a tree for every child (child will be the last in the visible vector)
      retLinksOnTheLeft =new SetOfLinks();
      retLinksOnTheRight=new SetOfLinks();
      linkOfChild = gatherDescendantsOf(family.getChild(c-1),retLinksOnTheLeft,retLinksOnTheRight,null);

      // ... add the childlink to the family link
      linkToFam.addLinkToChild(linkOfChild);

      // ... calculate how near children can be positioned
      distance=0;
      for (int i=1;;i++) {
        if ((retLinksOnTheLeft.getSize()<i)||(linksOnTheRight.getSize()<i)) break;
        distance = java.lang.Math.max( distance
                       , linksOnTheRight.getLink(i).getPosInGen()
                       - retLinksOnTheLeft.getLink(i).getPosInGen()
                       + distOfSiblings );
      }

      // ... move child beside others
      for (;indexOfChildren<=links.getSize();indexOfChildren++) {
        link=links.getLink(indexOfChildren);
        link.moveBy(0,distance);
      }

      // ... update left&right links of root-tree
      for (int i=1;i<=retLinksOnTheRight.getSize();i++)
        linksOnTheRight.changeLink(i,retLinksOnTheRight.getLink(i));
      for (int i=linksOnTheLeft.getSize()+1;i<=retLinksOnTheLeft.getSize();i++)
        linksOnTheLeft.changeLink(i,retLinksOnTheLeft.getLink(i));

      // ... next child
      }

      // ... next family
    }

    // Position Children centered under distOfGenerations,distOfPartners/2
    if (linksOnTheRight.getSize()>0) {
      distance = ( linksOnTheRight.getLink(1).getPosInGen()
            -linksOnTheLeft .getLink(1).getPosInGen()
            - distOfPartners ) / 2;
      for (int i=indexOfFirstChild;i<=links.getSize();i++) {
      link=links.getLink(i);
      link.moveBy(distOfGenerations,-distance);
      }
    }

    // Add links of families
    links.addLinksFrom(fams);

    // Gather all partners of root
    indexOfFirstPartner=links.getSize()+1;

    for (int f=1;f<=root.getNoOfFams();f++) {

      // ... calculate current family
      Fam family=root.getFam(f-1);

      // ... make a link for a "More" stopper of that fam
      if ( (family.getOtherSpouse(root)!=null) && (family.getOtherSpouse(root).getFamc()!=null) ) {
      link = new PlusMinus(this,
                 -distOfIndiPlusMinus,
                 f*distOfPartners,
                 null,
                 false
      );
      links.addLink(link);
      }

      // ... add a link to partner
      link=new LinkToIndi(0                  // depth in tree = 0
               ,f*distOfPartners // position in generation = beside root
               ,family.getOtherSpouse(root));
      links.addLink(link);
      indexOfLastPartner=links.getSize();
    }

    // Add root of tree (depthInTree=0;positionInGen=0)
    if (presetRoot==null) {
      linkOfRoot=new LinkToIndi(0,0,root);
      links.addLink(linkOfRoot);
    } else {
      linkOfRoot=presetRoot;
    }

    // Add entries in list of left/right links of tree for top generation
    linksOnTheLeft.addLinkAt(1,linkOfRoot);
    if (indexOfLastPartner==0)
      linksOnTheRight.addLinkAt(1,linkOfRoot);
    else
      linksOnTheRight.addLinkAt(1,links.getLink(indexOfLastPartner));

    // Done
    return linkOfRoot;
  }

  /**
   * Calculates the visible tree from the data in mankind
   */
  private void gatherTree() {

    // Reset vector of visible persons/families
    links      = new SetOfLinks(gedcom.getEntities(Gedcom.INDIVIDUALS).getSize() );
    actualLink = null;

    // Calculate tree
    int indiWidth,indiHeight,famWidth,famHeight;

    if (isVertical()) {
      indiWidth            = sizeOfIndis.width ;
      indiHeight           = sizeOfIndis.height;
      famWidth             = sizeOfFams .width ;
      famHeight            = sizeOfFams .height;
    } else {
      indiWidth            = sizeOfIndis.height;
      indiHeight           = sizeOfIndis.width ;
      famWidth             = sizeOfFams .height;
      famHeight            = sizeOfFams .width;
    }
    spaceBetweenPartners   = 2;
    spaceBetweenSiblings   = 20;
    spaceBetweenAncestors  = 20;
    spaceBetweenGenerations= famHeight+40;

    distOfEntities         = 1+indiHeight/2+famHeight/2+1;
    distOfGenerations      = spaceBetweenGenerations+indiHeight;
    distOfAncestors        = spaceBetweenAncestors  +indiWidth ;
    distOfPartners         = spaceBetweenPartners   +indiWidth ;
    distOfSiblings         = spaceBetweenSiblings   +indiWidth ;
    distOfIndiPlusMinus    = indiHeight/2;
    distOfFamPlusMinus     = famHeight /2;

    // Do the tree
    if (rootEntity!=null) {
      if (rootEntity instanceof Indi)
      gatherTreeOf((Indi)rootEntity);
      else
      gatherTreeOf((Fam )rootEntity);
    }

    // Preset actual to root
    actualLink = rootLink;

    // Signal layout change
    fireStructureChanged();

    // Done
  }

  /**
   * Build links to related entities for a given family
   */
  private void gatherTreeOf(Fam fam) {

    // Some VARs
    int  heightAboveRoot
      ,heightBelowRoot
      ,widthLeftOfRoot
      ,widthRightOfRoot;
    // Do the ancestors
    SetOfLinks minLinks=new SetOfLinks();
    SetOfLinks maxLinks=new SetOfLinks();

    rootLink = gatherAncestorsOf(fam,minLinks,maxLinks);

    // Calculate Tree size
    heightAboveRoot  = java.lang.Math.max( minLinks.getSize()-1 , maxLinks.getSize()-1 )
             * distOfGenerations + distOfEntities;
    widthLeftOfRoot  = 0;
    widthRightOfRoot = 0;

    for (int i=1; (i<=minLinks.getSize()) || (i<=maxLinks.getSize()) ;i++) {
      if (i<=minLinks.getSize())
      widthLeftOfRoot = java.lang.Math.max( widthLeftOfRoot, -minLinks.getLink(i).getPosInGen() );
      if (i<=maxLinks.getSize())
      widthRightOfRoot= java.lang.Math.max( widthRightOfRoot, maxLinks.getLink(i).getPosInGen() );
    }

    // Do the descendants
    minLinks=new SetOfLinks();
    maxLinks=new SetOfLinks();
    gatherDescendantsOf(fam,
              minLinks,
              maxLinks,
              (LinkToFam)rootLink);

    // Calculate Tree size
    heightBelowRoot = java.lang.Math.max( minLinks.getSize()-1 , maxLinks.getSize()-1 )
            * distOfGenerations + (distOfGenerations-distOfEntities);

    for (int i=1; (i<=minLinks.getSize()) || (i<=maxLinks.getSize()) ;i++) {
      if (i<=minLinks.getSize()) {
        widthLeftOfRoot = java.lang.Math.max( widthLeftOfRoot, -minLinks.getLink(i).getPosInGen() );
      }
      if (i<=maxLinks.getSize()) {
        widthRightOfRoot= java.lang.Math.max( widthRightOfRoot, maxLinks.getLink(i).getPosInGen() );
      }
    }

    widthLeftOfRoot += distOfSiblings;
    widthRightOfRoot+= distOfSiblings;
    heightAboveRoot += distOfGenerations;
    heightBelowRoot += distOfGenerations;

    if (isVertical()) {
      size = new Dimension(
        widthLeftOfRoot + widthRightOfRoot,
        heightAboveRoot + heightBelowRoot
      );
    } else {
      size = new Dimension(
        heightAboveRoot + heightBelowRoot,
        widthLeftOfRoot + widthRightOfRoot
      );
    }

    // Re-align to Point(0,0)
    for (int i=1;i<=links.getSize();i++)
      links.getLink(i).moveBy(heightAboveRoot,widthLeftOfRoot);

    // Done
    return ;
  }

  /**
   * Build links to related entities for a given individual
   */
  private void gatherTreeOf(Indi indi) {

    // Some VARs
    int  heightAboveRoot
        ,heightBelowRoot
        ,widthLeftOfRoot
        ,widthRightOfRoot;

    // Do the ancestors
    SetOfLinks minLinks=new SetOfLinks();
    SetOfLinks maxLinks=new SetOfLinks();
    rootLink = gatherAncestorsOf(indi,minLinks,maxLinks);

    // Calculate Tree size
    heightAboveRoot  = java.lang.Math.max( minLinks.getSize()-1 , maxLinks.getSize()-1 )
               * distOfGenerations;
    widthLeftOfRoot  = 0;
    widthRightOfRoot = 0;

    for (int i=1; (i<=minLinks.getSize()) || (i<=maxLinks.getSize()) ;i++) {
      if (i<=minLinks.getSize()) {
        widthLeftOfRoot = java.lang.Math.max( widthLeftOfRoot, -minLinks.getLink(i).getPosInGen() );
      }
      if (i<=maxLinks.getSize()) {
        widthRightOfRoot= java.lang.Math.max( widthRightOfRoot, maxLinks.getLink(i).getPosInGen() );
      }
    }

    // Do the descendants
    minLinks=new SetOfLinks();
    maxLinks=new SetOfLinks();
    gatherDescendantsOf(
      indi,
      minLinks,
      maxLinks,
      (LinkToIndi)rootLink
    );

    // Calculate Tree size
    heightBelowRoot = java.lang.Math.max( minLinks.getSize()-1 , maxLinks.getSize()-1 )
            * distOfGenerations;

    for (int i=1; (i<=minLinks.getSize()) || (i<=maxLinks.getSize()) ;i++) {
      if (i<=minLinks.getSize()) {
        widthLeftOfRoot = java.lang.Math.max( widthLeftOfRoot, -minLinks.getLink(i).getPosInGen() );
      }
      if (i<=maxLinks.getSize()) {
        widthRightOfRoot= java.lang.Math.max( widthRightOfRoot, maxLinks.getLink(i).getPosInGen() );
      }
    }

    widthLeftOfRoot += distOfSiblings;
    widthRightOfRoot+= distOfSiblings;
    heightAboveRoot += distOfGenerations;
    heightBelowRoot += distOfGenerations;

    if (isVertical()) {
      size = new Dimension(
        widthLeftOfRoot + widthRightOfRoot,
        heightAboveRoot + heightBelowRoot
      );
    } else {
      size = new Dimension(
        heightAboveRoot + heightBelowRoot,
        widthLeftOfRoot + widthRightOfRoot
      );
    }

    // Re-align to Point(0,0)
    for (int i=1;i<=links.getSize();i++) {
      links.getLink(i).moveBy(heightAboveRoot,widthLeftOfRoot);
    }
    // Done
  }

  /**
   * Generates Proxies for given TagPaths
   */
  public static Proxy[] generateProxies(TagPath[] paths, Rectangle[] boxes) {

    Proxy[] result = new Proxy[paths.length];

    for (int i=0;i<result.length;i++) {
      result[i] = Proxy.generate(paths[i],boxes[i]);
    }

    return result;
  }

  /**
   * Helper that creates array of proxies for given path/rectangle
   * @param paths array of paths to use
   * @param boxes array of boxes to use
   * @return resulting array of Proxies
   */
  public static Proxy[] generateProxies(String[] paths, Rectangle[] boxes)  {

    try {
      TagPath[] tpaths = new TagPath[paths.length];
      for (int i=0;i<tpaths.length;i++) {
        tpaths[i] = new TagPath(paths[i]);
      }
      return generateProxies(tpaths,boxes);
    } catch (IllegalArgumentException x) {
      return new Proxy[0];
    }
  }

  /**
   * Returns the entity pointed to by the actual link
   */
  public Entity getActualEntity() {
    if (actualLink!=null) {
      return actualLink.getEntity();
    }
    return null;
  }

  /**
   * Returns the link currently actual
   */
  public Link getActualLink() {
    return actualLink;
  }

  /**
   * Returns this model's font
   */
  public Font getFont() {
    return font;
  }

  /**
   * Returns the gedcom object this tree depends on
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * Returns the link for given entity
   */
  Link getLink(Entity entity) {
    Enumeration ls = links.elements();
    while (ls.hasMoreElements()) {
      Link link = (Link)ls.nextElement();
      if ((link.getEntity()==entity)&&(link instanceof LinkToIndi)) {
        return link;
      }
    }
    return null;
  }

  /**
   * Returns an enumeration of links
   */
  public Enumeration getLinks() {
    return links.elements();
  }

  /**
   * Returns offset of plusminus regarding to entity
   */
  public int getPlusMinusOffset(int of) {
    switch (of) {
    case Gedcom.INDIVIDUALS:
      return distOfIndiPlusMinus;
    case Gedcom.FAMILIES:
      return distOfFamPlusMinus;
    }
    throw new IllegalArgumentException();
  }

  /**
   * Returns the current proxies for individuals/families
   * @param which either Gedcom.INDIVIDUALS or Gedcom.FAMILIES
   * @return array of proxies
   */
  public Proxy[] getProxies(int which) {
    switch (which) {
      case Gedcom.INDIVIDUALS :
        return indisProxies;
      case Gedcom.FAMILIES :
        return famsProxies;
    }
    throw new IllegalArgumentException("Only Individuals and Families are supported");
  }

  /**
   * Returns this tree's root entity
   */
  public Entity getRoot() {
    return rootEntity;
  }

  /**
   * Returns this tree's root link
   */
  public Link getRootLink() {
    return rootLink;
  }

  /**
   * Returns the relative position of root in dimension of tree
   */
  public float getRootToHeight() {

    // No root ?
    if (rootLink==null) {
      return 0.5F;
    }

    // Calc position
    return rootLink.getDepthInTree() / (float)size.height;
  }

  /**
   * Returns the relative position of root in dimension of tree
   */
  public float getRootToWidth() {

    // No root ?
    if (rootLink==null) {
      return 0.5F;
    }

    // Calc position
    return rootLink.getPosInGen() / (float)size.width;
  }

  /**
   * Returns pixel dimension of tree
   */
  public Dimension getSize() {
    return size;
  }

  /**
   * Returns dimension of entity
   */
  public Dimension getSize(int which) {
    switch (which) {
      case Gedcom.INDIVIDUALS:
        return sizeOfIndis;
      case Gedcom.FAMILIES:
        return sizeOfFams;
    }
    throw new IllegalArgumentException();
  }

  /**
   * Returns stoppers (Entities) in tree
   */
  public Vector getStoppers() {
    return stoppers;
  }

  /**
   * Returns the current tag paths for individuals/families
   * @param which either Gedcom.INDIVIDUALS or Gedcom.FAMILIES
   * @return array of TagPaths
   */
  public TagPath[] getTagPaths(int which) {

    Proxy proxies[];
    switch (which) {
      case Gedcom.INDIVIDUALS :
        proxies = indisProxies;
        break;
      case Gedcom.FAMILIES :
        proxies = famsProxies;
        break;
      default :
        throw new IllegalArgumentException("Only Individuals and Families are supported");
    }

    TagPath paths[] = new TagPath[proxies.length];
    for (int i=0;i<paths.length;i++) {
      paths[i] = proxies[i].getPath();
    }

    return paths;
  }

  /**
   * Notification that a change in a Gedcom-object took place.
   */
  public synchronized void handleChange(final Change change) {

    // Added/Deleted entities ?
    if (  (change.isChanged(Change.EADD))
       || (change.isChanged(Change.EDEL)) ) {

      // Root deleted ?
      Vector deleted = change.getEntities(Change.EDEL);
      if (deleted.contains(rootEntity)) {
        if (gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()>0) {
          // BUG this was '1' ... hmmm .. and there was no check for size
          rootEntity = gedcom.getIndi(0);
        } else {
          rootEntity=null;
        }
      }

      // Could be new root
      while (rootEntity==null) {
      if (gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()>0) {
        rootEntity=gedcom.getIndi(0);
        break;
      }
      if (gedcom.getEntities(Gedcom.FAMILIES).getSize()>0) {
        rootEntity=gedcom.getFam(0);
        break;
      }
      break;
      }

      // Rebuild tree
      gatherTree();
      return;
    }

    // Added/Deleted/Modified properties
    if (! (change.isChanged(Change.PADD)
          ||change.isChanged(Change.PDEL)
          ||change.isChanged(Change.PMOD)) )
      return;

    if (  change.getProperties(Change.PMOD).contains(PropertyXRef.class)
      ||change.getProperties(Change.PDEL).contains(PropertyXRef.class)
      ||change.getProperties(Change.PADD).contains(PropertyXRef.class) ) {

      gatherTree();
      return;
    }

    // Some Entities changed
    fireEntitiesChanged(change.getEntities(Change.EMOD));

    // Done
  }

  /**
   * Notification that the gedcom is being closed
   */
  public void handleClose(Gedcom which) {
  // I won't do anything
  }

  /**
   * Notification that an entity has been selected.
   */
  public void handleSelection(Selection selection) {

    // Entity that's interesting for us ?
    if ( (!(selection.getEntity() instanceof Indi)) && (!(selection.getEntity() instanceof Fam)) ) {
      return;
    }

    // Double-Click -> new origin (when not stickToRoot)
    if ((!isStickToRoot)&&selection.isDoubleClick()) {
      setRoot(selection.getEntity());
      return;
    }

    // Single Click -> change actual in model if link exists
    Entity entity = selection.getEntity();
    Link newActual = null;

    for (int i=1;i<=links.getSize();i++) {
      if ((links.getLink(i).getEntity()==entity)&&(!(links.getLink(i) instanceof PlusMinus))) {
        newActual = links.getLink(i);
        break;
      }
    }
    if ((newActual==null)||(newActual==actualLink))
      return;

    Link oldActual=actualLink;
    actualLink=newActual;

    fireActualChanged(oldActual,newActual,false);

  // Done
  }

  /**
   * Returns wether given link is actual
   */
  public boolean isActual(Link link) {
    return actualLink==link;
  }

  /**
   * Returns wether this tree shows itself horizontally
   */
  public boolean isHorizontal() {
    return !isVertical;
  }

  /**
   * Returns this tree behaviour in case a selection indicates
   * root-change through double-click.
   */
  public boolean isStickToRoot() {
    return isStickToRoot;
  }

  /**
   * Returns true if entity is a stopper in tree
   */
  public boolean isStopper(Entity entity) {
    if (entity==null) {
      return false;
    }
    return stoppers.contains(entity);
  }

  /**
   * Returns wether this tree shows itself vertically
   */
  public boolean isVertical() {
    return isVertical;
  }

  /**
   * Remove listener
   */
  public void removeTreeModelListener(TreeModelListener listener) {

    // Forget
    listeners.removeElement(listener);

    // Disconnect from gedcom ?
    if (listeners.size()==0) {
      gedcom.removeListener(this);
    }
  }

  /**
   * Changes the actual selected entity's box
   * @param newActual Link to become actual
   */
  public void setActual(Link newActual) {

    // Nothing new ?
    if (newActual==actualLink) {
      return;
    }

    // Change actual
    Link oldActual=actualLink;
    actualLink=newActual;

    // Signal actual change
    fireActualChanged(oldActual,newActual,true);

    // Done
  }

  /**
   * Sets the current proxies for individuals/families
   * @param which either Gedcom.INDIVIDUALS or Gedcom.FAMILIES
   * @param proxies array of proxies for given type
   */
  public void setProxiesOf(int which,Proxy[] proxies) {
    switch (which) {
    case Gedcom.INDIVIDUALS :
      indisProxies = proxies;
      break;
    case Gedcom.FAMILIES :
      famsProxies = proxies;
      break;
    default:
      throw new IllegalArgumentException("Only Individuals and Families are supported");
    }
    fireDataChanged();
  }

  /**
   * Changes root of tree
   * @param root entity to be new root
   */
  public void setRoot(Entity root) {

    // Change root
    rootEntity=root;
    actualLink=null;

    // Re-Calc links
    gatherTree();
  }

  /**
   * Sets the current size or individuals/families
   * @param which either Gedcom.INDIVIDUALS or Gedcom.FAMILIES
   * @param size Dimension to set
   */
  public void setSizeOf(int which,Dimension size) {
    switch (which) {
    case Gedcom.INDIVIDUALS :
      if (sizeOfIndis.equals(size))
      return;
      sizeOfIndis = size;
      break;
    case Gedcom.FAMILIES :
      if (sizeOfFams.equals(size))
      return;
      sizeOfFams  = size;
      break;
    default:
      throw new IllegalArgumentException("Only Individuals and Families are supported");
    }

    gatherTree();
  }

  /**
   * Sets this tree behaviour in case a selection indicates
   * root-change through double-click.
   * @param stickToRoot selects wether tree should stick to
   * current root
   */
  public void setStickToRoot(boolean stickToRoot) {
    isStickToRoot=stickToRoot;
  }

  /**
   * Sets this tree's direction property
   * @param set true for vertical, false for horizontal
   */
  public void setVertical(boolean set) {

    // Real change ?
    if (isVertical==set) {
      return;
    }

    // Remember
    isVertical=set;

    // Re-do tree
    gatherTree();

    // Done
  }

  /**
   * Sets this model's font
   */
  public void setFont(Font theFont) {
    font = theFont;
  }

}
