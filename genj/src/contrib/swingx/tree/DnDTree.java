/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * This is code written by Sven Meier and adapted by Nils Meier for
 * use in GenJ - please note the license terms as stated above.
 */
package swingx.tree;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A tree that supports editing of its tree model via DnD.
 * 
 * @see #toTransferable(List);
 * @see #fromTransferable(Transferable, int);
 */
public class DnDTree extends JTree implements Autoscroll {

  /**
   * The default margin for autoscrolling.
   */
  private static final int DEFAULT_AUTOSCROLL_MARGIN = 12;

  /**
   * The sourceListener of a DnDTree in this JVM that wants to remove node before they are inserted into another tree.
   */
  private static DragHandler sourceDragSourceHandler;

  /**
   * The margin for autoscrolling.
   */
  private int autoscrollMargin = DEFAULT_AUTOSCROLL_MARGIN;

  private DragGestureRecognizer dragGestureRecognizer;

  private DragHandler dragSourceListener;

  private DropHandler dropTargetListener;

  /**
   * This is the default constructor
   */
  public DnDTree() {
    this(getDefaultTreeModel());
  }

  /**
   * Create a DnDTree for the given model.
   */
  public DnDTree(TreeModel model) {
    super(model);

    // re-set the selectionModel so dragSourceListener gets registered
    // as a listener
    setSelectionModel(getSelectionModel());

    // enable dragging on tree with a dummy transferHandler, otherwise
    // the default node selection would interfere with drag gestures
    setDragEnabled(true);
    setTransferHandler(new TransferHandler() {
      public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_COPY;
      }
    });

    // handle dragging with AWT drag & drop, since Swing DnD does not
    // support dragging of multiple nodes
    dragGestureRecognizer = new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_NONE, getDragSourceListener());
    // enable dropping
    new DropTarget(this, getDropTargetListener());
  }

  /**
   * Returns the DnDModel or null if not available
   */
  public DnDTreeModel getDnDModel() {
    TreeModel result = getModel();
    return result instanceof DnDTreeModel ? (DnDTreeModel) result : null;
  }

  /**
   * Overriden to register listener for dragSource.
   */
  public void setSelectionModel(TreeSelectionModel model) {
    TreeSelectionModel oldModel = getSelectionModel();
    if (oldModel != null) {
      oldModel.removeTreeSelectionListener(getDragSourceListener());
    }

    super.setSelectionModel(model);

    if (model != null) {
      model.addTreeSelectionListener(getDragSourceListener());
    }
  }

  /**
   * Create a treeModel to use as default.
   */
  protected static TreeModel getDefaultTreeModel() {
    return new DefaultDnDTreeModel((MutableTreeNode) JTree.getDefaultTreeModel().getRoot());
  }

  /**
   * Overriden to return the current row during drop operation.
   */
  public boolean isRowSelected(int row) {
    if (getDropTargetListener().getParentPath() == null) {
      return super.isRowSelected(row);
    } else {
      return row == getRowForPath(getDropTargetListener().getParentPath());
    }
  }

  /**
   * Get the margin used for autoscrolling while DnD.
   * 
   * @return margin for autoscrolling
   */
  public int getAutoscrollMargin() {
    return autoscrollMargin;
  }

  /**
   * Set the margin to be used for autoscrolling while DnD.
   * 
   * @param margin
   *          margin for autoscrolling
   */
  public void setAutoscrollMargin(int margin) {
    autoscrollMargin = margin;
  }

  public void autoscroll(Point point) {
    Dimension dimension = getParent().getSize();

    int row = getClosestRowForLocation(point.x, point.y);
    if (row != -1) {
      if (getY() + point.y < dimension.height / 2) {
        row = Math.max(0, row - 1);
      } else {
        row = Math.min(row + 1, getRowCount() - 1);
      }
      scrollRowToVisible(row);
    }
  }

  public Insets getAutoscrollInsets() {
    Rectangle bounds = getParent().getBounds();

    return new Insets(bounds.y - getY() + autoscrollMargin, bounds.x - getX() + autoscrollMargin, getHeight() - bounds.height - bounds.y + getY() + autoscrollMargin, getWidth() - bounds.width - bounds.x + getX() + autoscrollMargin);
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    getDropTargetListener().paint(g);
  }

  protected DragHandler getDragSourceListener() {
    if (dragSourceListener == null) {
      dragSourceListener = new DragHandler();
    }
    return dragSourceListener;
  }

  protected DropHandler getDropTargetListener() {
    if (dropTargetListener == null) {
      dropTargetListener = new DropHandler();
    }
    return dropTargetListener;
  }

  /**
   * The handler of drag
   * <ul>
   * <li>enables drag dependent on the currently selected nodes,</li>
   * <li>starts a drag in response to a drag gestures and</li>
   * <li>removes dragged nodes on successful move.</li>
   * </ul>
   */
  private class DragHandler extends DragSourceAdapter implements DragGestureListener, TreeSelectionListener, Comparator {
    /**
     * The nodes to be dragged.
     */
    private List nodes;

    /**
     * Enabled source actions 
     */
    public void valueChanged(TreeSelectionEvent e) {

      int actions = 0;
      
      DnDTreeModel dndModel = getDnDModel();
      if (dndModel != null) {
        
        TreePath[] paths = getSelectionPaths();
        if (paths != null && paths.length > 0) {
          
          List children = new ArrayList();
          for (int p = 0; p < paths.length; p++) {
            children.add(paths[p].getLastPathComponent());
          }
          
          actions = dndModel.canDrag(children);
        }
      }

      dragGestureRecognizer.setSourceActions(actions);
    }

    /**
     * Start a drag with all currently selected nodes if one of them is hit by the mouse event that originates the drag.
     */
    public void dragGestureRecognized(DragGestureEvent dge) {

      TreePath[] paths = getSelectionPaths();
      if (paths != null && paths.length > 0) {
        Arrays.sort(paths, this);

        boolean selectionHit = false;
        List nodes = new ArrayList();
        for (int p = 0; p < paths.length; p++) {
          if (paths[p].getPathCount() > 1) {
            nodes.add(paths[p].getLastPathComponent());

            Rectangle rect = getPathBounds(paths[p]);
            if (rect.contains(dge.getDragOrigin())) {
              selectionHit = true;
            }
          }
        }

        // no hit no action
        if (!selectionHit)
          return;

        // initiate drag with transferable
        Transferable transferable = getDnDModel().getTransferable(nodes);
        if (transferable == null)
          return;

        dge.startDrag(null, createDragImage(paths), new Point(), transferable, this);

        // remember me as the one receiving remove before insert on move
        sourceDragSourceHandler = this;

        // keep nodes
        this.nodes = nodes;

        // done
      }
    }

    /**
     * Compare two treePaths because the selectionModel doesn't keep them necessarily ordered - lower rows come first.
     */
    public int compare(Object object1, Object object2) {
      TreePath path1 = (TreePath) object1;
      TreePath path2 = (TreePath) object2;

      int row1 = getRowForPath(path1);
      int row2 = getRowForPath(path2);
      if (row1 < row2) {
        return -1;
      } else if (row2 < row1) {
        return 1;
      } else {
        return 0;
      }
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
      sourceDragSourceHandler = null;

      drag(dsde.getDropAction(), dsde.getDropSuccess(), null, -1);
    }

    /**
     * On successful drop the drag is executed
     */
    protected void drag(int action, boolean success, Object target, int index) {
      if (nodes != null) {
        if (success)
          ((DnDTreeModel)getModel()).drag(action, nodes, target, index);
        nodes = null;
      }
    }
  }

  /**
   * The handler of drop
   * <ul>
   * <li>expandes nodes when hovered over them,</li>
   * <li>accepts or rejects drops,</li>
   * <li>draws a drop index indicator</li>
   * <li>surveilles removal of nodes and</li>
   * <li>optionally performs a drop.</li>
   * </ul>
   */
  private class DropHandler extends DropTargetAdapter implements ActionListener {

    private Timer timer;

    private TreePath parentPath;

    private int childIndex;

    private Rectangle indicator;

    public DropHandler() {
      timer = new Timer(1500, this);
      timer.setRepeats(false);
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
      dragOver(dtde);
    }

    public void dragOver(DropTargetDragEvent dtde) {
      int action = dtde.getDropAction();
      boolean accepted = false;

      update(dtde.getLocation());

      if (parentPath != null) {
        Transferable transferable = getTigerTransferable(dtde);
        if (transferable == null) {
          accepted = true;
        } else {
          accepted = canDrop(transferable, action);
        }
      }

      if (accepted) {
        dtde.acceptDrag(action);
      } else {
        dtde.rejectDrag();
      }
    }

    public void dragExit(DropTargetEvent dte) {
      clear();
    }

    public void drop(DropTargetDropEvent dtde) {
      int action = dtde.getDropAction();
      boolean complete = false;

      try {

        DnDTreeModel model = getDnDModel();
        if (model != null && parentPath != null) {
          dtde.acceptDrop(action);
          Transferable transferable = dtde.getTransferable();
          if (canDrop(transferable, action)) {
            complete = drop(transferable, action);
          }
        }

        dtde.dropComplete(complete);
      } finally {
        clear();
      }
    }

    protected boolean canDrop(Transferable transferable, int action) {

      // gotta have a model
      DnDTreeModel model = getDnDModel();
      if (model == null)
        return false;

      // ask model
      Object parent = parentPath.getLastPathComponent();
      return model.canDrop(action, transferable, parent, childIndex);
    }

    protected boolean drop(Transferable transferable, int action) {

      DnDTreeModel model = getDnDModel();
      Object parent = parentPath.getLastPathComponent();
      
      // remember queue of children before childIndex
      List queue = new ArrayList();
      for (int i=0,j=model.getChildCount(parent); i<j&&i<childIndex; i++) {
        queue.add(model.getChild(parent, i));
      }

      // first tell drag handler if known (same VM)
      if (sourceDragSourceHandler != null) 
        sourceDragSourceHandler.drag(action, true, parent, childIndex);

      // check if children in queue are still there - they might
      // have moved by the drag (e.g. dragging from pos n to n+1)
      for (int i=0,j=model.getChildCount(parent);i<queue.size()&&i<j;i++) {
        if (model.getIndexOfChild(parent, queue.get(i))<0)
          childIndex--;
      }
      
      // let model drop transferable
      List children;
      try {
        children = model.drop(action, transferable, parent, childIndex);
      } catch (Throwable t) {
        return false;
      }

      // update selection
      TreePath[] paths = new TreePath[children.size()];
      for (int c = 0; c < children.size(); c++) {
        paths[c] = parentPath.pathByAddingChild(children.get(c));
      }
      setSelectionPaths(paths);

      // done
      return true;
    }

    private void update(Point point) {
      TreePath oldParentPath = parentPath;
    
      TreePath path = getClosestPathForLocation(point.x, point.y);
      if (path == null) {
        parentPath = null;
        childIndex = -1;
        indicator = null;
      } else if (path.getPathCount() == 1) {
        parentPath = path;
        childIndex = 0;
        indicator = null;
      } else {
        parentPath = path.getParentPath();
        childIndex = getModel().getIndexOfChild(parentPath.getLastPathComponent(), path.getLastPathComponent());
        indicator = getPathBounds(path);
    
        if ((getModel().isLeaf(path.getLastPathComponent())) || (point.y < indicator.y + indicator.height * 1 / 4) || (point.y > indicator.y + indicator.height * 3 / 4 && !isExpanded(path))) {
    
          if (point.y > indicator.y + indicator.height / 2) {
            indicator.y = indicator.y + indicator.height;
            childIndex++;
          }
          indicator.width = getWidth() - indicator.x - getInsets().right;
          indicator.y -= 1;
          indicator.height = 2;
        } else {
          parentPath = path;
          indicator = null;
          childIndex = 0;
        }
      }
    
      repaint();
    
      if (parentPath == null) {
        if (timer.isRunning()) {
          timer.stop();
        }
      } else {
        if (!parentPath.equals(oldParentPath)) {
          timer.start();
        }
      }
    }

    private void clear() {
      if (timer.isRunning()) {
        timer.stop();
      }

      parentPath = null;
      childIndex = -1;
      indicator = null;

      repaint();
    }

    public TreePath getParentPath() {
      return parentPath;
    }

    public void paint(Graphics g) {
      if (indicator != null) {
        paintIndicator(g, indicator);
      }
    }

    private void paintIndicator(Graphics g, Rectangle rect) {
      g.setColor(getForeground());

      g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);

      g.drawLine(rect.x, rect.y - 2, rect.x + 1, rect.y - 2);
      g.drawLine(rect.x, rect.y - 1, rect.x + 2, rect.y - 1);
      g.drawLine(rect.x, rect.y + rect.height + 0, rect.x + 2, rect.y + rect.height + 0);
      g.drawLine(rect.x, rect.y + rect.height + 1, rect.x + 1, rect.y + rect.height + 1);

      g.drawLine(rect.x + rect.width - 2, rect.y - 2, rect.x + rect.width - 1, rect.y - 2);
      g.drawLine(rect.x + rect.width - 3, rect.y - 1, rect.x + rect.width - 1, rect.y - 1);
      g.drawLine(rect.x + rect.width - 3, rect.y + rect.height + 0, rect.x + rect.width - 1, rect.y + rect.height + 0);
      g.drawLine(rect.x + rect.width - 2, rect.y + rect.height + 1, rect.x + rect.width - 1, rect.y + rect.height + 1);
    }

    public void actionPerformed(ActionEvent e) {
      if (parentPath != null) {
        expandPath(parentPath);
      }
    }
  }

  /**
   * Create an image representation for the selection about to be dragged. <br>
   * Drag images are currently not supported under Windows. On Mac OS X the whole tree is used as drag image. This is why this default implementation creates a dummy image. <br>
   * Subclasses may subclass this method for fancy image creation.
   * 
   * @param selectionPaths
   *          paths of selection to drag
   * @return image representation
   */
  protected Image createDragImage(TreePath[] selectionPaths) {
    return createImage(1, 1);
  }

  /**
   * Get the transferable of a <code>DropTargetDragEvent</code>- only supported since Java 1.5 Tiger.
   * 
   * @param dtde
   *          event to get transferable from
   * @return transferable or <code>null</code> if running under pre 1.5 version of Java
   */
  private static Transferable getTigerTransferable(DropTargetDragEvent dtde) {
    try {
      return (Transferable) DropTargetDragEvent.class.getMethod("getTransferable", new Class[0]).invoke(dtde, new Object[0]);
    } catch (Exception ex) {
      return null;
    }
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      // keep default look and feel
    }

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(new DnDTree()), new JScrollPane(new DnDTree())));
    frame.pack();
    frame.setVisible(true);
  }
}