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
import gj.layout.tree.Orientation;
import gj.model.Node;

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
  protected Path shapeMarrs, shapeIndis, shapeFams, shapePlus, shapeMinus; 

  /** padding (n, e, s, w) */
  protected double[] padIndis, padMinusPlus; 
  
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
    initMarrShapes();
    initEntityShapes();
    initSignShapes();
    
    // done    
  }
   
  /**
   * parses a tree starting from entity
   * @param entity either fam or indi
   */
  public final TreeNode parse(Entity root) {
    return (root instanceof Indi) ? parse((Indi)root) : parse((Fam )root);
  }
  
  /**
   * Place another node at the origin
   */
  public TreeNode align(TreeNode other) {
    return other;
  }
  
  /**
   * parses a tree starting from an indi
   */
  protected abstract TreeNode parse(Indi indi);
  
  /**
   * parses a tree starting from a family
   */
  protected abstract TreeNode parse(Fam fam);
  
  /**
   * Calculates marriage rings
   */
  private void initMarrShapes() {

    shapeMarrs = new Path();
    
    // check model
    if (!model.isMarrSymbols()) { 
      shapeMarrs.append(new Rectangle2D.Double());
      return; 
    }

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
 
    shapeMarrs.append(e.getPathIterator(at1));
    shapeMarrs.append(e.getPathIterator(at2));
    
    // patch bounds
    shapeMarrs.setBounds2D( model.isVertical() ?
      new Rectangle2D.Float(-d/2,-(metrics.hIndis+metrics.pad)/2,d,metrics.hIndis+metrics.pad) :
      new Rectangle2D.Float(-metrics.wIndis/2,-d/2,metrics.wIndis,d)
    );
    
    // done
  }

  /**
   * Init shapes/padding for entities
   */
  private void initEntityShapes() {
    
    // .. padding (n, e, s, w)
    padIndis  = new double[] { 
      metrics.pad/2, 
      metrics.pad/2, 
      metrics.pad/2, 
      metrics.pad/2
    };
    
    // indis
    shapeIndis = new Path().append(new Rectangle2D.Double(
      -metrics.wIndis/2,
      -metrics.hIndis/2,
       metrics.wIndis,
       metrics.hIndis
    ));
     
    // fams
    shapeFams = new Path().append(new Rectangle2D.Double(
      -metrics.wFams/2,
      -metrics.hFams/2,
       metrics.wFams,
       metrics.hFams
    ));
     
    // done
  }
  
  /**
   * Init shapes/padding for signs
   */
  private void initSignShapes() {
    
    // how we pad signs (n, e, s, w)
    padMinusPlus  = new double[]{  
      -padIndis[0], 
       padIndis[1], 
       padIndis[2], 
       padIndis[3]     
    };

    // size of signs
    double d = 0.3;
    
    // plus
    shapePlus = new Path();
    shapePlus.moveTo(new Point2D.Double( 0,-d*0.3));
    shapePlus.lineTo(new Point2D.Double( 0, d*0.3));
    shapePlus.moveTo(new Point2D.Double(-d*0.3, 0));
    shapePlus.lineTo(new Point2D.Double( d*0.3, 0));
    shapePlus.append(new Rectangle2D.Double(-d/2,-d/2,d,d));

    // minus    
    shapeMinus = new Path();
    shapeMinus.moveTo(new Point2D.Double(-d*0.3, 0));
    shapeMinus.lineTo(new Point2D.Double( d*0.3, 0));
    shapeMinus.append(new Rectangle2D.Double(-d/2,-d/2,d,d));
    
    // done
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
    protected TreeNode parse(Fam fam) {
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padIndis));
      recurse(fam, node);
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi, java.awt.geom.Point2D)
     */
    protected TreeNode parse(Indi indi) {
      return recurse(indi);
    }
    /**
     * parse an individual and its ancestors
     */
    private TreeNode recurse(Indi indi) {
      // node for indi      
      TreeNode node = model.add(new TreeNode(indi, shapeIndis, padIndis));
      // do we have a family we're child in?
      Fam famc = indi.getFamc();
      if (famc!=null) {
        // check minus as a substitute pivot
        TreeNode pivot = node;
        if (model.isFoldSymbols()) {
          pivot = model.add(new TreeNode(null, shapePlus, padMinusPlus));
          model.add(new TreeArc(node, pivot, false));
        }
        // grab the family's husband/wife and their ancestors
        recurse(famc, pivot);
      } 
      // done
      return node;
    }
    /**
     * parses a family and its ancestors
     */
    private void recurse(Fam fam, TreeNode child) {
      Indi wife = fam.getWife();
      Indi husb = fam.getHusband();
      if (wife!=null) model.add(new TreeArc(child, recurse(wife), true));
      if (husb!=null) model.add(new TreeArc(child, recurse(husb), true));
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
      
      // .. fams ancestors (n, e, s, w)
      padFams  = new double[]{  
         padIndis[0], 
         padIndis[1], 
        -metrics.pad*0.40, 
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
      // done      
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam) {
      // node for the fam
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padFams));
      // husband & wife
      Indi
        husb = fam.getHusband(),
        wife = fam.getWife();
      model.add(new TreeArc(node, recurse(wife, hasParents(husb)?LEFT:CENTER, padHusband), false));
      model.add(new TreeArc(node, model.add(new TreeNode(null, shapeMarrs, null)), false));
      model.add(new TreeArc(node, recurse(husb, hasParents(wife)?RIGHT:CENTER, padWife), false));
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    protected TreeNode parse(Indi indi) {
      return recurse(indi, CENTER, padIndis);
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
    private TreeNode recurse(Indi indi, final int alignment, double[] pad) {
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
          model.add(new TreeArc(node, parse(famc), true));
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
    protected TreeNode parse(Indi indi) {
      // create node for indi
      TreeNode node = model.add(new TreeNode(indi, shapeIndis, padIndis)); 
      // grab fams
      Fam[] fams = indi.getFamilies();
      if (fams.length>0) {
        // check hiding of descendants
        if (model.isHideDescendants(indi)) {
          if (model.isFoldSymbols()) {
            TreeNode plus = model.add(new TreeNode(null, shapePlus, padMinusPlus));
            model.add(new TreeArc(node, plus, false));
          }
        } else {
          TreeNode pivot = node;
          if (model.isFoldSymbols()) {
            pivot = model.add(new TreeNode(null, shapeMinus, padMinusPlus));
            model.add(new TreeArc(node, pivot, false));
          }
          // loop through fams
          for (int f=0; f<fams.length; f++) {
            recurse(fams[f], pivot);
          }
        }
      }
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam) {
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padIndis));
      return recurse(fam, node);
    }
    
    /**
     * parses a fam and its descendants
     */
    private TreeNode recurse(Fam fam, TreeNode parent) {
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        model.add(new TreeArc(parent, parse(children[c]), true));       
      }
      // done
      return parent;
    }
  } //DescendantsNoFams
  
  /**
   * Parser - Descendants with Families 
   */
  private static class DescendantsWithFams extends Parser {
    
    /** real origin */
    private TreeNode origin;
  
    /** how we pad families */
    private double[] padFams;
    
    /** how we pad husband, wife */
    private double[] padHusband, padWife;
    
    /** how we offset an indi above its marr */
    private double offsetHusband;
      
    /**
     * Constructor
     */
    protected DescendantsWithFams(Model model, TreeMetrics metrics) {
      super(model, metrics);

      // how we pad fams (n, e, s, w)
      padFams  = new double[]{  
        -metrics.pad*0.4, 
         padIndis[1], 
         padIndis[2], 
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
      
      offsetHusband = model.isVertical() ? 
        metrics.wFams/2 - metrics.wIndis - shapeMarrs.getBounds2D().getWidth()/2 :
        metrics.hFams/2 - metrics.hIndis - shapeMarrs.getBounds2D().getHeight()/2;
      // done
    }
    
    /**
     * Place another node at the origin
     */
    public TreeNode align(TreeNode other) {
      other.getPosition().setLocation(origin.getPosition());    
      return other;
    }
  
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    protected TreeNode parse(Indi indi) {
      // parse under artificial pivot
      TreeNode pivot = model.add(new TreeNode(null, null, null));
      origin = recurse(indi, pivot);
      // done
      return pivot;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam) {
      TreeNode node = recurse(fam);
      node.padding = padIndis; // patch first fams padding
      return node;
    }
        
    /**
     * recurse into indi
     * @param indi the indi to parse
     * @param pivot all nodes of descendant are added to pivot
     * @return MyNode
     */
    private TreeNode recurse(Indi indi, TreeNode pivot) {

      // lookup its families      
      Fam[] fams = indi.getFamilies();
      
      // no families is simply
      if (fams.length==0) {
        TreeNode nIndi = new TreeNode(indi,shapeIndis,padIndis);
        model.add(new TreeArc(pivot, model.add(nIndi), pivot.getShape()!=null));
        return nIndi;        
      }

      // otherwise indi as husband first of family and arc pivot-indi
      TreeNode nIndi = new TreeNode(indi,shapeIndis,padHusband) {
        /**
         * patch alignment above children so that marr is centered above
         */
        public double getLongitude(Node node, double minc, double maxc, double mint, double maxt, Orientation o) {
          return minc + offsetHusband;
        }
      };
      model.add(new TreeArc(pivot, model.add(nIndi), pivot.getShape()!=null));
      
      // with one selected fam
      Fam fam = fams[0];
      
      // add marr and arc pivot-marr
      TreeNode nMarr = new TreeNode(null, shapeMarrs, null);
      model.add(new TreeArc(pivot, model.add(nMarr), false));
      
      // add spouse and arc pivot-spouse
      TreeNode nSpouse = new TreeNode(fam.getOtherSpouse(indi), shapeIndis, padWife);
      model.add(new TreeArc(pivot, model.add(nSpouse), false));
      
      // add arc : marr-fam
      TreeNode nFam = recurse(fam);
      model.add(new TreeArc(nIndi, nFam, false));
      
      // done
      return nIndi;
    }
    
    /**
     * recurses into fam and its descendants
     */
    private TreeNode recurse(Fam fam) {
      // node for fam
      TreeNode nFam = new TreeNode(fam, shapeFams, padFams);
      model.add(nFam);
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        // create an arc from node to node for indi
        recurse(children[c], nFam);       
         // next child
      }
      // done
      return nFam;
    }
    
  } //DescendantsWithFams
  

} //Parser

