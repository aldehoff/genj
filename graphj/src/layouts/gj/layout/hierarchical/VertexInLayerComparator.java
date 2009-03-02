/**
 * 
 */
package gj.layout.hierarchical;

import gj.layout.Layout2D;
import gj.model.Vertex;

/**
 * A comparator for an initial ordering within a layer
 */
public interface VertexInLayerComparator {

  /**
   * compare two vertices
   */
  int compare(Vertex v1, Vertex v2, int layer, Layout2D layout);
  
}
