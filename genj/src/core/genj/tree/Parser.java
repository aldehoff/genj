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

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import gj.layout.LayoutException;
import gj.layout.tree.Orientation;
import gj.layout.tree.TreeLayout;
import gj.model.Arc;
import gj.model.Node;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Parser
 */
/*package*/ abstract class Parser {
  
  /** the model we're working on*/
  protected Model model;

  /** metrics */
  protected TreeMetrics metrics;
  
  /** shapes */
  protected Shape shapeMarrs, shapeIndis, shapeFams; 

  /** padding (n, e, s, w) */
  protected double[] padMarrs, padIndis; 
  
  /** 
   * gets an instance of a parser   */
  public static Parser getInstance(boolean ancestors, boolean families, Model model, TreeMetrics metrics) {
    if (ancestors) {
      if (families) return new AncestorsWithFams(model, metrics);
      return new AncestorsNoFams(model, metrics);
    } else {
      if (families) return new DescendantsWithFams(model, metrics);
      return new DescendantsNoFams(model, metrics);
    }
  }
  
  /**
   * Constructor   */
  protected Parser(Model mOdel, TreeMetrics mEtrics) {
    
    // keep the model&metrics
    model = mOdel;
    metrics = mEtrics;
    
    // init values
    shapeMarrs = calcMarriageShape();
    shapeIndis = calcIndiShape();
    shapeFams  = calcFamShape();
    
    // .. marrs padding
    if (model.isVertical()) {
      padMarrs = new double[]{  
        (metrics.hIndis+metrics.pad)/2 - metrics.hMarrs/2, 
        -metrics.pad/2, 
        (metrics.hIndis+metrics.pad)/2 - metrics.hMarrs/2,
        -metrics.pad/2 
      };
    } else {
      padMarrs = new double[]{  
        (metrics.wIndis+metrics.pad)/2 - metrics.wMarrs/2,
        -metrics.pad/2, 
        (metrics.wIndis+metrics.pad)/2 - metrics.wMarrs/2,
        -metrics.pad/2
      };
    }
    
    // .. indis
    padIndis  = new double[] { 
      metrics.pad/2, 
      metrics.pad/2, 
      metrics.pad/2, 
      metrics.pad/2
    };

    // done    
  }
   
  /**
   * parses a tree starting from entity
   * @param entity either fam or indi
   */
  public final TreeNode parse(Entity entity, Point2D at) {
    
    // delegate
    TreeNode result;
    if (entity instanceof Indi)
      result = parse((Indi)entity, at);
    else
      result = parse((Fam )entity, at);
      
    // done
    return result;
  }
  
  /**
   * parses a tree starting from an indi
   */
  protected abstract TreeNode parse(Indi indi, Point2D at);
  
  /**
   * parses a tree starting from a family
   */
  protected abstract TreeNode parse(Fam fam, Point2D at);
  
  /**
   * Helper that applies the layout
   */
  protected void layout(TreeNode root, boolean isTopDown) {
    // layout
    try {
      TreeLayout layout = new TreeLayout();
      layout.setTopDown(isTopDown);
      layout.setBendArcs(model.isBendArcs());
      layout.setDebug(false);
      layout.setIgnoreUnreachables(true);
      layout.setBalanceChildren(false);
      layout.setRoot(root);
      layout.setVertical(model.isVertical());
      layout.layout(model);
    } catch (LayoutException e) {
      e.printStackTrace();
    }
    // done
  }
  
  /**
   * Calculates marriage rings
   */
  private Shape calcMarriageShape() {
    Ellipse2D
      a = new Ellipse2D.Double(
        -metrics.wMarrs/2,
        -metrics.hMarrs/2,
        metrics.wMarrs*0.6,
        metrics.hMarrs
      ),
      b = new Ellipse2D.Double(
        metrics.wMarrs/2-metrics.wMarrs*0.6,
        -metrics.hMarrs/2,
        metrics.wMarrs*0.6,
        metrics.hMarrs
      );
    GeneralPath result = new GeneralPath(a);      
    result.append(b,false);
    return result;
  }

  /**
   * Calculates shape for indis 
   */
  private Shape calcIndiShape() {
    return new Rectangle2D.Double(
      -metrics.wIndis/2,
      -metrics.hIndis/2,
       metrics.wIndis,
       metrics.hIndis
     );
  }
  
  /**
   * Calculates shape for fams 
   */
  private Shape calcFamShape() {
    return new Rectangle2D.Double(
      -metrics.wFams/2,
      -metrics.hFams/2,
       metrics.wFams,
       metrics.hFams
     );
  }
    
  /**
   * Parser - Ancestors without Families
   */
  private static class AncestorsNoFams extends Parser {
    /**
     * Constructor     */
    protected AncestorsNoFams(Model model, TreeMetrics metrics) {
      super(model, metrics);
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam, java.awt.geom.Point2D)
     */
    protected TreeNode parse(Fam fam, Point2D at) {
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padIndis));
      Indi wife = fam.getWife();
      Indi husb = fam.getHusband();
      if (wife!=null) model.add(new TreeArc(node, iterate(wife), true));
      if (husb!=null) model.add(new TreeArc(node, iterate(husb), true));
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi, java.awt.geom.Point2D)
     */
    protected TreeNode parse(Indi indi, Point2D at) {
      TreeNode node = iterate(indi);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * parse an individual and its ancestors
     */
    private TreeNode iterate(Indi indi) {
      // node for indi      
      TreeNode node = model.add(new TreeNode(indi, shapeIndis, padIndis));
      // do we have a family we're child in?
      if (indi!=null) {
        Fam famc = indi.getFamc();
        // grab the family's husband/wife and their ancestors
        if (famc!=null) iterate(famc, node);
      } 
      // done
      return node;
    }
    /**
     * parses a family and its ancestors
     */
    private void iterate(Fam fam, TreeNode child) {
      Indi wife = fam.getWife();
      Indi husb = fam.getHusband();
      if (wife!=null) model.add(new TreeArc(child, iterate(wife), true));
      if (husb!=null) model.add(new TreeArc(child, iterate(husb), true));
    }
  } //AncestorsNoFams 
   
  /**
   * Parser - Ancestors with Families
   */
  private static class AncestorsWithFams extends Parser {
    
    private final int
      CENTER = 0,
      LEFT   = 1,
      RIGHT  = 2;

    /** how we pad families */
    private double[] padFams;
      
    /**
     * Constructor
     */
    protected AncestorsWithFams(Model model, TreeMetrics metrics) {
      super(model, metrics);
      
      // .. fams ancestors
      padFams  = new double[]{  
         metrics.pad/2, 
         metrics.pad*0.05, 
        -metrics.pad*0.40, 
         metrics.pad*0.05 
      };

      // done      
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    public TreeNode parse(Fam fam, Point2D at) {
      TreeNode node = iterate(fam);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    public TreeNode parse(Indi indi, Point2D at) {
      TreeNode node = iterate(indi, CENTER);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, false);
      return node;
    }
    /**
     * parse a family and its ancestors
     */
    private TreeNode iterate(Fam fam) {
      // node for the fam
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padFams));
      // husband & wife
      Indi
        husb = fam.getHusband(),
        wife = fam.getWife();
      model.add(new TreeArc(node, iterate(wife, hasParents(husb)?LEFT:CENTER), false));
      model.add(new TreeArc(node, model.add(new TreeNode(null, shapeMarrs, padMarrs)), false));
      model.add(new TreeArc(node, iterate(husb, hasParents(wife)?RIGHT:CENTER), false));
      // done
      return node;
    }
    /**
     * helper that checks if an individual is child in a family
     */
    private boolean hasParents(Indi indi) {
      if (indi==null) return false;
      return indi.getFamc()!=null;
    }
    /**
     * parse an individual and its ancestors
     */
    private TreeNode iterate(Indi indi, final int alignment) {
      // node for indi      
      TreeNode node;
      if (alignment==CENTER) {
        node = new TreeNode(indi, shapeIndis, padIndis);
      } else {
        node = new TreeNode(indi, shapeIndis, padIndis) {
          /** patching longitude */
          public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
            if (alignment==LEFT) return maxt+metrics.pad/2;
            return mint-metrics.pad/2;
          }
        };
      }
      model.add(node);
      // do we have a family we're child in?
      if (indi!=null) {
        Fam famc = indi.getFamc();
        // grab the family
        if (famc!=null) 
          model.add(new TreeArc(node, iterate(famc), true));
      } 
      // done
      return node;
    }
  } //AncestorsWithFams
  
  /**
   * Parser - Descendants no Families
   */
  private static class DescendantsNoFams extends Parser {
    /**
     * Constructor
     */
    protected DescendantsNoFams(Model model, TreeMetrics metrics) {
      super(model, metrics);
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    protected TreeNode parse(Indi indi, Point2D at) {
      // parse
      TreeNode node = iterate(indi);
      if (at!=null) node.getPosition().setLocation(at);
      // do the layout
      super.layout(node, true);
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam, Point2D at) {
      // parse
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padIndis));
      iterate(fam, node);
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, true);
      return node;
    }
    
    /**
     * parses an indi
     * @param indi the indi to parse
     * @return MyNode
     */
    private TreeNode iterate(Indi indi) {
      // create node for indi
      TreeNode node = model.add(new TreeNode(indi, shapeIndis, padIndis)); 
      // loop through our fams
      Fam[] fams = indi.getFamilies();
      for (int f=0; f<fams.length; f++) {
        iterate(fams[f], node);
      }
      // done
      return node;
    }
    
    /**
     * parses a fam and its descendants
     */
    private void iterate(Fam fam, TreeNode parent) {
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        model.add(new TreeArc(parent, iterate(children[c]), true));       
      }
      // done
    }
  } //DescendantsNoFams
  
  /**
   * Parser - Descendants with Families 
   */
  private static class DescendantsWithFams extends Parser {
    
    /** the alignment offset for an individual above its 1st fam */
    private Point2D.Double alignOffsetIndiAbove1stFam;
    
    /** how we pad families */
    private double[] padFams;
      
    /**
     * Constructor
     */
    protected DescendantsWithFams(Model model, TreeMetrics metrics) {
      super(model, metrics);

      // how we pad fams (n, e, s, w)
      padFams  = new double[]{  
        -metrics.pad*0.4, 
         metrics.pad*0.1, 
         metrics.pad/2, 
         metrics.pad*0.1     
      };
      
      // patched alignment for 1st family
      alignOffsetIndiAbove1stFam = new Point2D.Double(
        padFams [TreeNode.WEST]-padIndis[TreeNode.WEST] +  metrics.wFams/2 - metrics.wIndis  - ( metrics.wMarrs)/2,
        padIndis[TreeNode.WEST]-padFams [TreeNode.WEST] - metrics.hFams/2 + metrics.hIndis + (metrics.hMarrs)/2
      );
      
      // done
    }
    
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    protected TreeNode parse(Indi indi, Point2D at) {
      // won't support at
      if (at!=null) throw new IllegalArgumentException("at is not supported");
      // parse under pivot
      TreeNode pivot = model.add(new TreeNode(null, null, null));
      TreeNode node = iterate(indi, pivot, 0);
      // do the layout
      super.layout(pivot, true);
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam, Point2D at) {
      TreeNode node = iterate(fam, null, 0);
      node.padding = padIndis; // patch first fams padding
      if (at!=null) node.getPosition().setLocation(at);
      super.layout(node, true);
      return node;
    }
        
    /**
     * parses an indi
     * @param indi the indi to parse
     * @param pivot all nodes of descendant are added to pivot
     * @return MyNode
     */
    private TreeNode iterate(Indi indi, TreeNode pivot, final int group) {
      // create node for indi
      TreeNode node = model.add(new TreeNode(indi, shapeIndis, padIndis) {
        /** patch longitude */
        public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
          return minc + o.getLongitude( alignOffsetIndiAbove1stFam );
        }
      });
      // create an arc for pivot to indi
      model.add(new TreeArc(pivot, node, pivot.getShape()!=null) {
        /**
         * @see genj.tree.TreeArc#getPort(gj.model.Arc, gj.model.Node)
         */
        public Point2D getPort(Arc arc, Node node, Orientation orntn) {
          // delegate to super if end of arc
          if (node==arc.getEnd()) return super.getPort(arc, node, orntn);
          // the start of an arc is moved in longitude by 
          //   ([w/h]Fams+padFams) * group
          // so that it starts under the appropriate marriage
          double dlon = 
            group*(padFams[1]+padFams[3]+(model.isVertical()?metrics.wFams:metrics.hFams));
          Point2D 
            o = node.getPosition(),
            d = orntn.getPoint2D(0, dlon),
            n = new Point2D.Double(o.getX()+d.getX(), o.getY()+d.getY());
          return n;
        }
      });
      // loop through our fams
      Fam[] fams = indi.getFamilies();
      TreeNode fam1 = null;
      for (int f=0; f<fams.length; f++) {
        // add arc : node-fam
        TreeNode fami = iterate(fams[f], fam1, f);
        if (fam1==null) fam1 = fami;
        model.add(new TreeArc(node, fami, false));
        // add arcs : pivot-marr, pivot-spouse
        model.add(new TreeArc(pivot, model.add(new TreeNode(null, shapeMarrs, padMarrs)), false));
        model.add(new TreeArc(pivot, model.add(new TreeNode(fams[f].getOtherSpouse(indi), shapeIndis, padIndis)), false));
        // next family
      }
      // done
      return node;
    }
    /**
     * parses a fam and its descendants
     * @parm pivot all nodes of descendant are added to pivot
     */
    private TreeNode iterate(Fam fam, TreeNode pivot, int group) {
      // node for fam
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padFams));
      // pivot is me if unset
      if (pivot==null) pivot = node;
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        // create an arc from node to node for indi
        iterate(children[c], pivot, group);       
         // next child
      }
      // done
      return node;
    }
    
  } //DescendantsWithFams
  

} //Parser

