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
 */
package swingx.tree;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
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
     * The dataFlavor used for transfers in one JVM.
     */
    private static DataFlavor serializedFlavor = new DataFlavor(java.io.Serializable.class, "Object");

    /**
     * The dataFlavor used for transfers between different JVMs.
     */
    private static DataFlavor localFlavor;

    static {
        try {
            localFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.Object");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    /**
     * The sourceListener of a DnDTree in this JVM that wants to remove
     * node before they are inserted into another tree.
     */
    private static DragHandler removeBeforeInsert;
    
    /**
     * The margin for autoscrolling.
     */
    private int autoscrollMargin = DEFAULT_AUTOSCROLL_MARGIN;

    private DragGestureRecognizer      dragGestureRecognizer; 
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
        dragGestureRecognizer = new DragSource().createDefaultDragGestureRecognizer(this,
                                                            DnDConstants.ACTION_NONE,
                                                            getDragSourceListener());
        // enable dropping
        new DropTarget(this, getDropTargetListener());
    }

    /**
     * Overriden to register listener for dropTarget.
     */
    public void setModel(TreeModel model) {
        TreeModel oldModel = getModel(); 
        if (oldModel != null) {
            oldModel.removeTreeModelListener(getDropTargetListener());
        }
        
        super.setModel(model);
        
        if (model != null) {
            model.addTreeModelListener(getDropTargetListener());
        }
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
        return new DefaultDnDTreeModel((MutableTreeNode)JTree.getDefaultTreeModel().getRoot());     
    }
    
    public boolean hasDnDTreeModel() {
        return getModel() instanceof DnDTreeModel;
    }
    
    public DnDTreeModel getDnDTreeModel() {
        if (getModel() instanceof DnDTreeModel) {
            return (DnDTreeModel)getModel();
        }
        throw new IllegalStateException("no DnDTreeModel");
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
     * @return  margin for autoscrolling
     */
    public int getAutoscrollMargin() {
        return autoscrollMargin;
    }

    /**
     * Set the margin to be used for autoscrolling while DnD.
     * 
     * @param margin    margin for autoscrolling
     */
    public void setAutoscrollMargin(int margin) {
        autoscrollMargin = margin;
    }

    public void autoscroll(Point point) {
        Dimension dimension = getParent().getSize();

        int row = getClosestRowForLocation(point.x, point.y);
        if (row != -1) {
            if (getY() + point.y < dimension.height/2) {
                row = Math.max(0, row - 1);
            } else {
                row = Math.min(row + 1, getRowCount() - 1);
            }
            scrollRowToVisible(row);
        }        
    }

    public Insets getAutoscrollInsets() {
        Rectangle bounds = getParent().getBounds();

        return new Insets(
            bounds.y - getY() + autoscrollMargin,
            bounds.x - getX() + autoscrollMargin,
            getHeight() - bounds.height - bounds.y + getY() + autoscrollMargin,
            getWidth()  - bounds.width  - bounds.x + getX() + autoscrollMargin);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        getDropTargetListener().paint(g);
    }

    /**
     * Convert the given objects to a transferable.
     * <br>
     * This default implementation creates transferables with the
     * following dataflavors:
     * <ul>
     *   <li>
     *     <code>application/x-java-jvm-local-objectref; class=java.lang.Object</code>
     *   </li>
     *   <li>
     *     <code>application/x-java-serialized-object; class=java.io.Serialiazable</code>
     *   </li>
     *   <li>
     *     <code>application/x-java-serialized-object; class=java.lang.String</code>
     *   </li>
     * </ul>
     * , the first two containing a collection of objects. 
     */
    protected Transferable toTransferable(List objects) {

      return new DnDTreeTransferable(objects);
    }

    /**
     * Convert transferable to objects to be inserted into the model.
     * <br>
     * This default implementation supports transferables with the
     * following dataflavors:
     * <ul>
     *   <li>
     *     <code>application/x-java-jvm-local-objectref; class=java.lang.Object</code>
     *   </li>
     *   <li>
     *     <code>application/x-java-serialized-object; class=java.io.Serialiazable</code>
     *   </li>
     * </ul>
     * , each containing a collection of objects. 
     * 
     * @param transferable      transferable to convert
     * @throws IllegalArgumentException     if the given action is not supported
     * @throws IOException                  if data could not fetched
     * @throws UnsupportedFlavorException   if flavor is not supported   
     */
    protected List fromTransferable(Transferable transferable) throws UnsupportedFlavorException, IOException, IllegalArgumentException {
      Object object;
      if (transferable.isDataFlavorSupported(localFlavor)) {
        object = transferable.getTransferData(localFlavor);
      } else {
        object = transferable.getTransferData(serializedFlavor);
      }
      if (object instanceof Collection) {
          return new ArrayList((Collection)object);
      }
      throw new IOException("unknown transferdata '" + object.getClass() + "'");
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
     *   <li>enables drag dependent on the currently selected nodes,</li>
     *   <li>starts a drag in response to a drag gestures and</li>
     *   <li>removes dragged nodes on successful move.</li>
     * </ul>
     */    
    private class DragHandler extends DragSourceAdapter implements DragGestureListener,
                                                                   TreeSelectionListener,
                                                                   Comparator {
        /**
         * The nodes to be dragged.
         */
        private List nodes;
        
        /**
         * Enabled source actions depending on the possibility to remove the
         * currently selected nodes from the model.
         */
        public void valueChanged(TreeSelectionEvent e) {
            int actions = DnDConstants.ACTION_COPY | DnDConstants.ACTION_LINK;

            if (hasDnDTreeModel()) {
                TreePath[] paths = getSelectionPaths();
                if (paths != null && paths.length > 0) {
                    List children = new ArrayList();
                    for (int p = 0; p < paths.length; p++) {
                      children.add(paths[p].getLastPathComponent());
                    }
                    if (getDnDTreeModel().canRemove(children)) {
                        actions = actions | DnDConstants.ACTION_MOVE;
                    }
                }
            }

            dragGestureRecognizer.setSourceActions(actions);
        }

        /**
         * Start a drag with all currently selected nodes if one of them
         * is hit by the mouse event that originates the drag.
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
                
                if (selectionHit) {
                    if (hasDnDTreeModel() && getDnDTreeModel().removeBeforeInsert()) {
                        removeBeforeInsert = this;
                    }
                    
                    this.nodes = nodes;
                     
                    dge.startDrag(null, 
                                  createDragImage(paths),
                                  new Point(),
                                  toTransferable(nodes), this);
                }
            }
        }

        /**
         * Compare two treePaths because the selectionModel doesn't keep them
         * necessarily ordered - lower rows come first.
         */
        public int compare(Object object1, Object object2) {
            TreePath path1 = (TreePath)object1;
            TreePath path2 = (TreePath)object2;
            
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
            removeBeforeInsert = null;
                
            dragDropEnd(dsde.getDropSuccess(), dsde.getDropAction());            
        }
        
        /**
         * On successful drop of a move action the nodes are removed.
         */
        protected void dragDropEnd(boolean success, int action) {
            if (nodes != null) {
                if (success && action == DnDConstants.ACTION_MOVE) {
                    ((DnDTreeModel)getModel()).removeFrom(nodes); 
                }
                nodes = null;
            }
        }
    }

    /**
     * The transferable used to transfer nodes.
     * 
     * @see #toTransferable(java.util.List)
     * @see #toTransferable(java.awt.datatransfer.Transferable)
     */    
    protected class DnDTreeTransferable implements Transferable {
        
        private List flavors;
        
        private List nodes;

        public DnDTreeTransferable(List nodes) {
            this.nodes = nodes;

            flavors = createFlavors(nodes);            
        }
        
        protected List createFlavors(List nodes) {
            List flavors = new ArrayList();
            
            flavors.add(localFlavor);
            flavors.add(DataFlavor.stringFlavor);

            boolean serializable = true;
            for (int n = 0; n < nodes.size(); n++) {
                serializable = serializable && (nodes.get(n) instanceof Serializable); 
            }
            if (serializable) {
                flavors.add(serializedFlavor);
            }
            
            return flavors;
        }        

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
          if (isDataFlavorSupported(flavor)) {
              if (localFlavor.equals(flavor)) {
                return nodes;
              }
              if (serializedFlavor.equals(flavor)) {
                return nodes;
              }
              if (DataFlavor.stringFlavor.equals(flavor)) {
                return toString();
              }
          }
          throw new UnsupportedFlavorException(flavor);
        }

        public String toString() {
          StringBuffer buffer = new StringBuffer();
          for (int n = 0; n < nodes.size(); n++) {
            if (n > 0) {
              buffer.append("\n");
            }
            buffer.append(nodes.get(n));
          }
          return buffer.toString();
        }

        public DataFlavor[] getTransferDataFlavors() {
          return (DataFlavor[])flavors.toArray(new DataFlavor[flavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
          return flavors.contains(flavor);
        }  
      }; 


    /**
     * The handler of drop
     * <ul>
     *   <li>expandes nodes when hovered over them,</li>
     *   <li>accepts or rejects drops,</li>
     *   <li>draws a drop index indicator</li>
     *   <li>surveilles removal of nodes and</li>
     *   <li>optionally performs a drop.</li>
     * </ul>
     */    
    private class DropHandler extends DropTargetAdapter implements ActionListener, TreeModelListener {
            
        private Timer timer;
        
        private TreePath   parentPath;
        private int        childIndex = 0;
        private Rectangle  indicator;
        private List       children;

        public DropHandler() {
            timer = new Timer(1500, this);
            timer.setRepeats(false);            
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            dragOver(dtde);
        }

        public void dragOver(DropTargetDragEvent dtde) {
            int     action   = dtde.getDropAction();
            boolean accepted = false;
            
            if (hasDnDTreeModel()) {

                update(dtde.getLocation());

                if (parentPath != null) {
                    Transferable transferable = getTigerTransferable(dtde);
                    if (transferable == null) {
                        accepted = true;
                    } else {
                        if (childrenAvailable(transferable)) {                        
                            if (canInsert(action)) {
                                accepted = true;
                            }
                        }
                    }                        
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
            int     action   = dtde.getDropAction();
            boolean complete = false;

            try {
                if (hasDnDTreeModel() && parentPath != null) {
                    dtde.acceptDrop(action);
    
                    if (childrenAvailable(dtde.getTransferable())) {
                        if (canInsert(action)) {
                            if (removeBeforeInsert != null) {
                                removeBeforeInsert.dragDropEnd(true, action);
                            }
    
                            insert(action);
                        
                            complete = true;
                        }
                    }
                }
    
                dtde.dropComplete(complete); 
            } finally {
                clear();
            }
        }

        /**
         * Are children available from the given transferable.
         * <br>
         * As a side effect this method stores possibly available new children
         * in the member {@link #children}.
         *  
         * @param transferable      transferable to get new children from
         * @return                  <code>true</code> if children are
         *                          available
         */
        protected boolean childrenAvailable(Transferable transferable) {
            if (children == null) {
                try {
                    children = fromTransferable(transferable);
                } catch (UnsupportedFlavorException ex) {
                    children = null;
                } catch (IOException ex) {
                    children = null;
                }
            }
            return children != null;
        }       

        protected boolean canInsert(int action) {
            DnDTreeModel model  = getDnDTreeModel();                
            Object       parent = parentPath.getLastPathComponent();

            return model.canInsert(children, parent, childIndex, action);
        }

        protected void insert(int action) {
            DnDTreeModel model  = getDnDTreeModel();                
            Object       parent = parentPath.getLastPathComponent();

            model.insertInto(children, parent, childIndex, action);
            
            TreePath[] paths = new TreePath[children.size()];
            for (int c = 0; c < children.size(); c++) {
                paths[c] = parentPath.pathByAddingChild(model.getChild(parent, childIndex + c));
            }
            setSelectionPaths(paths);
        }
        
        private void update(Point point) {
            TreePath oldParentPath = parentPath;
            
            TreePath path = getClosestPathForLocation(point.x, point.y);
            if (path == null) {
                parentPath = null;
                childIndex = -1;
                indicator  = null;
            } else if (path.getPathCount() == 1) {
                parentPath = path;
                childIndex = 0;
                indicator  = null;
            } else {
                parentPath = path.getParentPath();
                childIndex = getModel().getIndexOfChild(parentPath.getLastPathComponent(), path.getLastPathComponent());
                indicator  = getPathBounds(path);
                                
                if ((getModel().isLeaf(path.getLastPathComponent())) ||
                    (point.y < indicator.y + indicator.height*1/4)   ||
                    (point.y > indicator.y + indicator.height*3/4 && !isExpanded(path))) {

                    if (point.y > indicator.y + indicator.height/2) {
                        indicator.y = indicator.y + indicator.height;
                        childIndex++; 
                    }
                    indicator.width = getWidth() - indicator.x - getInsets().right;
                    indicator.y      -= 1;
                    indicator.height  = 2;
                } else {
                    parentPath = path; 
                    indicator  = null;
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
            indicator  = null;
            children   = null;

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

            g.drawLine(rect.x, rect.y - 2              , rect.x + 1, rect.y - 2);
            g.drawLine(rect.x, rect.y - 1              , rect.x + 2, rect.y - 1);
            g.drawLine(rect.x, rect.y + rect.height + 0, rect.x + 2, rect.y + rect.height + 0);
            g.drawLine(rect.x, rect.y + rect.height + 1, rect.x + 1, rect.y + rect.height + 1);
            
            g.drawLine(rect.x + rect.width - 2, rect.y - 2              , rect.x + rect.width - 1, rect.y - 2);
            g.drawLine(rect.x + rect.width - 3, rect.y - 1              , rect.x + rect.width - 1, rect.y - 1);
            g.drawLine(rect.x + rect.width - 3, rect.y + rect.height + 0, rect.x + rect.width - 1, rect.y + rect.height + 0);
            g.drawLine(rect.x + rect.width - 2, rect.y + rect.height + 1, rect.x + rect.width - 1, rect.y + rect.height + 1);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (parentPath != null) {
                expandPath(parentPath);
            }
        }
        
        public void treeNodesChanged(TreeModelEvent e) {}

        public void treeNodesInserted(TreeModelEvent e) {}

        public void treeNodesRemoved(TreeModelEvent e) {
            if (parentPath != null) {
                TreePath path = e.getTreePath();

                if (path.equals(parentPath)) {
                    int[] childIndices = e.getChildIndices();
                    for (int i = 0; i < childIndices.length; i++) {
                        if (childIndices[i] < childIndex) {
                            childIndex--;
                        }
                    }
                }
            }
        }

        public void treeStructureChanged(TreeModelEvent e) {
            if (parentPath != null) {
                TreePath path = e.getTreePath();

                if (path.equals(parentPath)) {
                    childIndex = 0;
                }
            }
        }
    }

    /**
     * Create an image representation for the selection about to be
     * dragged.
     * <br>
     * Drag images are currently not supported under Windows. On
     * Mac OS X the whole tree is used as drag image. This is why
     * this default implementation creates a dummy image.
     * <br>
     * Subclasses may subclass this method for fancy image creation.
     *  
     * @param selectionPaths    paths of selection to drag
     * @return                  image representation
     */
    protected Image createDragImage(TreePath[] selectionPaths) {
        return createImage(1, 1);
    }

    /**
     * Get the transferable of a <code>DropTargetDragEvent</code> - only
     * supported since Java 1.5 Tiger.
     * 
     * @param dtde  event to get transferable from
     * @return      transferable or <code>null</code> if running under pre
     *              1.5 version of Java
     */
    private static Transferable getTigerTransferable(DropTargetDragEvent dtde) {
        try {
            return (Transferable)DropTargetDragEvent.class.getMethod("getTransferable", new Class[0]).invoke(dtde, new Object[0]);
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