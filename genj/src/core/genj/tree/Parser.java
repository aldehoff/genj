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
import gj.awt.geom.Path;
import gj.layout.LayoutException;
import gj.layout.tree.Orientation;
import gj.layout.tree.TreeLayout;
import gj.model.Node;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
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
  protected double[] padIndis; 
  
  /** 
   * gets an instance of a parser
   */
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
   * Constructor
   */
  protected Parser(Model mOdel, TreeMetrics mEtrics) {
    
    // keep the model&metrics
    model = mOdel;
    metrics = mEtrics;
    
    // init values
    shapeMarrs = calcMarriageShape();
    shapeIndis = calcIndiShape();
    shapeFams  = calcFamShape();
    
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
    
    // check model
    if (!model.isMarrSymbols())
      return new Rectangle2D.Double(); 

    // calculate maximum extension    
    float d = Math.min(metrics.wIndis/4, metrics.hIndis/4);
    
    // create result      
    Ellipse2D e = new Ellipse2D.Float(-d*0.3F,-d*0.3F,d*0.6F,d*0.6F);

    float 
      dx = model.isVertical() ? d*0.2F : d*0.0F,
      dy = model.isVertical() ? d*0.0F : d*0.2F;

    AffineTransform 
      at1 = AffineTransform.getTranslateInstance(-dx,-dy),
      at2 = AffineTransform.getTranslateInstance( dx, dy);
 
    Path result = new Path();
    result.append(e.getPathIterator(at1));
    result.append(e.getPathIterator(at2));
    
    // patch bounds
    result.setBounds2D( model.isVertical() ?
      new Rectangle2D.Float(-d/2,-(metrics.hIndis+metrics.pad)/2,d,metrics.hIndis+metrics.pad) :
      new Rectangle2D.Float(-metrics.wIndis/2,-d/2,metrics.wIndis,d)
    );
    
    // done
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
     * Constructor
     */
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
      
    /** how we pad husband, wife */
    private double[] padHusband, padWife;
    
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

      padHusband = new double[]{
        padIndis[0],
        0,
        padIndis[2],
        padIndis[3]
      };

      padWife = new double[]{
        padIndis[0],
        padIndis[1],
        padIndis[2],
        0
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
      TreeNode node = iterate(indi, CENTER, padIndis);
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
      model.add(new TreeArc(node, iterate(wife, hasParents(husb)?LEFT:CENTER, padHusband), false));
      model.add(new TreeArc(node, model.add(new TreeNode(null, shapeMarrs, null)), false));
      model.add(new TreeArc(node, iterate(husb, hasParents(wife)?RIGHT:CENTER, padWife), false));
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
    private TreeNode iterate(Indi indi, final int alignment, double[] pad) {
      // node for indi      
      TreeNode node;
      switch (alignment) {
        case CENTER: default:
          node = new TreeNode(indi, shapeIndis, pad);
          break;
        case LEFT:
          node = new TreeNode(indi, shapeIndis, pad) {
            /** patching longitude */
            public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
              return maxt+metrics.pad/2;
            }
          };
          break;
        case RIGHT:
          node = new TreeNode(indi, shapeIndis, pad) {
            /** patching longitude */
            public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
              return mint-metrics.pad/2;
            }
          };
          break;
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
    
    /** how we pad families */
    private double[] padFams;
    
    /** how we pad husband, wife */
    private double[] padHusband, padWife;
    
    /** how we offset an indi above its marr */
    private Point2D offsetHusband;
      
    /**
     * Constructor
     */
    protected DescendantsWithFams(Model model, TreeMetrics metrics) {
      super(model, metrics);

      // how we pad fams (n, e, s, w)
      padFams  = new double[]{  
        -metrics.pad*0.4, 
         0, 
         metrics.pad/2, 
         padIndis[3]     
      };
      
      padHusband = new double[]{
        padIndis[0],
        0,
        padIndis[2],
        padIndis[3]
      };

      padWife = new double[]{
        padIndis[0],
        padIndis[1],
        padIndis[2],
        0
      };
      
      offsetHusband = new Point2D.Double(
        (metrics.wIndis + shapeMarrs.getBounds2D().getWidth ())/2,
        -(metrics.hIndis + shapeMarrs.getBounds2D().getHeight())/2
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
      TreeNode node = iterate(indi, pivot);
      // do the layout
      super.layout(pivot, true);
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam, Point2D at) {
      TreeNode node = iterate(fam);
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
    private TreeNode iterate(final Indi indi, TreeNode pivot) {

      // lookup its families      
      Fam[] fams = indi.getFamilies();

      // create indi and arc pivot-indi
      TreeNode nIndi = new TreeNode(indi,shapeIndis,fams.length>0?padHusband:padIndis) {
        /**
         * @see genj.tree.Parser.DescendantsWithFams#getLongitude(gj.model.Node, double, double, double, double, gj.layout.tree.Orientation)
         */
        public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
          return super.getLongitude(node, minc, maxc, mint, maxt, o) - o.getLongitude(offsetHusband);
        }
      };
      model.add(new TreeArc(pivot, model.add(nIndi), pivot.getShape()!=null));
      
      // Get the fam (1st for now)
      if (fams.length>0) {
        Fam fam = fams[0];
        // add marr and arc pivot-marr
        TreeNode nMarr = new TreeNode(null, shapeMarrs, null);
        model.add(new TreeArc(pivot, model.add(nMarr), false));
        // add spouse and arc pivot-spouse
        TreeNode nSpouse = new TreeNode(fam.getOtherSpouse(indi), shapeIndis, padWife);
        model.add(new TreeArc(pivot, model.add(nSpouse), false));
        // add arc : marr-fam
        TreeNode nFam = iterate(fam);
        model.add(new TreeArc(nIndi, nFam, false));
        // next family
      }
      // done
      return nIndi;
    }
    /**
     * parses a fam and its descendants
     * @parm pivot all nodes of descendant are added to pivot
     */
    private TreeNode iterate(Fam fam) {
      // node for fam
      TreeNode nFam = new TreeNode(fam, shapeFams, padFams);
      model.add(nFam);
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        // create an arc from node to node for indi
        iterate(children[c], nFam);       
         // next child
      }
      // done
      return nFam;
    }
    
  } //DescendantsWithFams
  

} //Parser

