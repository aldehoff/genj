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
package genj.edit;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import genj.gedcom.*;
import genj.option.*;
import genj.util.*;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends JPanel implements ActionListener, TreeSelectionListener, GedcomListener {

  private Gedcom    gedcom;
  private Entity    entity;
  private Frame     frame;

  private JPanel            actionPanel;
  private AbstractButton    actionButtonAdd,
                            actionButtonRemove,
                            actionButtonUp,
                            actionButtonDown,
                            actionButtonReturn;
  private JCheckBox         actionCheckStick;

  private JPanel            createPanel;

  private JSplitPane        split;

  private JTree             treeOfProps = null;
  private JScrollPane       paneForTree = null;
  private JPanel            panelForProxy;

  private Proxy             currentProxy = null;
  private Property          currentNode = null;

  private Vector            returnStack = new Vector(MAX_RETURN);

  private final static int  MAX_RETURN  = 10;

  private boolean keepSimple     = false;

  private Registry registry;

  static final Resources resources = new Resources("genj.edit");

  private final static Object[][] entity2create = new Object[][] {
    { "CSPOUSE", Images.imgNewSpouse , "new.spouse",
      "CCHILD" , Images.imgNewChild  , "new.child",
      "CPARENT", Images.imgNewParent , "new.parent",
      "CNOTE"  , Images.imgNewNote   , "new.note",
      "CMEDIA" , Images.imgNewMedia  , "new.media" },
    { "CSPOUSE", Images.imgNewSpouse , "new.spouse",
      "CCHILD" , Images.imgNewChild  , "new.child",
      "CNOTE"  , Images.imgNewNote   , "new.note",
      "CMEDIA" , Images.imgNewMedia  , "new.media" },
    { },
    { }
  };

  /**
   * Class for rendering tree cell nodes
   */
  class PropertyCellRenderer extends Component implements TreeCellRenderer {

    // LCD

    /** members */
    private boolean selected = false, focus = false;
    private String  tag, value;
    private ImgIcon image;
    private int GAP = 2;
    private Font    font;
    private Color   color;

    /** Constructor */
    PropertyCellRenderer() {
      JLabel label = new JLabel();
      color=label.getForeground();
      font =label.getFont      ();
    }

    /** returns the component to render in a JTree */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                            boolean selected, boolean expanded,
                            boolean leaf, int row,
                            boolean hasFocus) {

      // Set the text
      Property prop = (Property)value;
      tag = prop.getTag();
      if (prop instanceof Entity)
        tag = "@"+((Entity)prop).getId()+"@ "+tag;

      this.value = (prop.getValue()==null ? "" : prop.getValue());

      // Set the image
      image = prop.getImage(true);

      // Done
      this.selected = selected;
      this.focus = focus;

      return this;
    }

    /** returns the preferred size of this component */
    public Dimension getPreferredSize() {

      // Calculate Params
      Graphics g = treeOfProps.getGraphics();
      if (g==null)
        return new Dimension(0,0);

      // Icon Width
      int w = image.getIconWidth();

      // Tag Width
      g.setFont(new Font(font.getName(),font.BOLD,font.getSize()));
      FontMetrics fm = g.getFontMetrics();
      w += fm.stringWidth(tag);

      // Value Width
      g.setFont(new Font(font.getName(),font.PLAIN,font.getSize()));
      fm = g.getFontMetrics();
      w += fm.stringWidth(value);

      // Height
      int h = Math.max( image.getIconHeight() , fm.getHeight() );

      // Done
      return new Dimension(w+2*GAP,h);
    }

    /** paint is subclassed to draw this treecell */
    public void paint(Graphics g) {

      // Color
      Color            bColor;
      if (selected)
        bColor = Color.yellow;
      else
        bColor = treeOfProps.getBackground();
      g.setColor(bColor);

      // Parms
      Dimension size = this.getSize();
      int h = size.height,
          w = size.width;

      // Background
      g.fillRect(0 , 0, w-1, h-1);

      // Image
      int iw = image.getIconWidth(),
          ih = image.getIconHeight(),
          ix = 0,
          iy = (h-ih)/2;
      image.paintIcon(g, ix, iy);

      // Text
      g.setColor(color);
      g.setFont(new Font(font.getName(),font.BOLD,font.getSize()));
      FontMetrics fm = g.getFontMetrics();
      int tx = iw + GAP,
          ty = (h+fm.getHeight())/2 - fm.getDescent();
      g.drawString(tag, tx, ty);
      tx += fm.stringWidth(tag) + GAP;
      g.setFont(new Font(font.getName(),font.PLAIN,font.getSize()));
      g.drawString(value, tx, ty);

      // Done
    }
    // EOC
  }

  /**
   * Constructor
   */
  public EditView(Gedcom setGedcom, Registry setRegistry, Frame setFrame) {

    // remember
    this.gedcom   = setGedcom;
    this.frame    = setFrame;
    this.registry = setRegistry;

    // Begin Layout
    setLayout(new BorderLayout());

    // ACTION Component
    actionPanel = new JPanel();
    actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));

    actionCheckStick  = new JCheckBox(new ImageIcon(Images.imgStickOff.getImage()));
    actionCheckStick.setSelectedIcon (new ImageIcon(Images.imgStickOn .getImage()));
    actionCheckStick.setFocusPainted(false);
    actionCheckStick.setSelected(registry.get("sticky",false));

    actionButtonAdd    = addButton(actionPanel, "ADD"   ,"action.add"   ,null            ,"tip.add_prop" , false );
    actionButtonRemove = addButton(actionPanel, "DEL"   ,"action.del"   ,null            ,"tip.del_prop" , false );
    actionButtonUp     = addButton(actionPanel, "UP"    ,"action.up"    ,null            ,"tip.up_prop"  , false );
    actionButtonDown   = addButton(actionPanel, "DOWN"  ,"action.down"  ,null            ,"tip.down_prop", false );
    actionButtonReturn = addButton(actionPanel, "RETURN",null           ,Images.imgReturn,"tip.return"   , false );
                         addButton(actionPanel, "STICK" ,null           ,null            ,"tip.stick"    , true  ,actionCheckStick);

    add(actionPanel,BorderLayout.NORTH);

    // SplitPane for top/lower section

      // TREE Component's ScrollPane
      paneForTree = new JScrollPane();
      paneForTree.setMinimumSize  (new Dimension(160, 128));
      paneForTree.setPreferredSize(new Dimension(160, 128));

      // EDIT Component
      panelForProxy = new JPanel();
      panelForProxy.setLayout(new BoxLayout(panelForProxy,BoxLayout.Y_AXIS));

    split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,paneForTree,panelForProxy);
    split.setContinuousLayout(true);
    add(split,BorderLayout.CENTER);

    Runnable run = new Runnable() {
      /** we have to defer the setDividerLocation - FIXME: still? */
      public void run() {
        int loc = registry.get("divider",-1);
        if (loc!=-1) {
          split.setDividerLocation(loc);
        }
      }
    };
    SwingUtilities.invokeLater(run);

    // NEW Component
    createPanel = new JPanel();
    createPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
    add(createPanel,BorderLayout.SOUTH);

    // Listeners
    gedcom.addListener(this);

    // Check if we can preset something to edit
    Entity lastEnt = null;
    if (!actionCheckStick.isSelected()) {
      lastEnt = gedcom.getLastEntity();
    }
    if (lastEnt==null) {
      String last = registry.get("last",(String)null);
      if (last!=null) {
        try { lastEnt = gedcom.getEntityFromId(last); } catch (Exception e) {}
      }
      if (lastEnt==null)
        try { lastEnt = gedcom.getIndi(0); } catch (Exception e) {}
      if (lastEnt==null)
        try { lastEnt = gedcom.getFam(0); } catch (Exception e) {}
    }
    if (lastEnt!=null) {
      setEntity(lastEnt);
    }

    // Done
  }

  /**
   * One of the action buttons has been pressed
   */
  public void actionPerformed(ActionEvent e) {

    if ( e.getActionCommand().equals("RETURN"))
      actionReturn();

    if (e.getActionCommand().equals("ADD"))
      actionAdd();

    if (e.getActionCommand().equals("DEL"))
      actionDel();

    if (e.getActionCommand().equals("UP"))
      actionUpDown(e);

    if (e.getActionCommand().equals("DOWN"))
      actionUpDown(e);

    if (e.getActionCommand().equals("CSPOUSE"))
      actionCreateSpouse();

    if (e.getActionCommand().equals("CCHILD"))
      actionCreateChild();

    if (e.getActionCommand().equals("CPARENT"))
      actionCreateParent();

    if (e.getActionCommand().equals("CNOTE"))
      actionCreateNote();

    if (e.getActionCommand().equals("CMEDIA"))
      actionCreateMedia();

  }

  /**
   * Starts a Gedcom transaction
   */
  private boolean startTransaction(String message) {

    // .. LockWrite
    if (gedcom.startTransaction()) {
      return true;
    }

    JOptionPane.showMessageDialog(
      this,
      message,
      resources.getString("error"),
      JOptionPane.ERROR_MESSAGE
    );

    return false;

  }

  /**
   * Ends the current Gedcom transaction
   */
  private void endTransaction() {
    gedcom.endTransaction();
  }

  /**
   * Helper: Creata an entity
   */
  private void createEntity(int type, int relation, String msg) {

    // Recheck with the user
    String message = resources.getString(
      "new.confirm", new String[] {
        msg,
        Gedcom.getNameFor(type,false)
      }
    );

    int option = JOptionPane.showOptionDialog(
      this,
      message,
      resources.getString("new"),
      JOptionPane.OK_CANCEL_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null, null, null
    );

    // .. OK or Cancel ?
    if (option != JOptionPane.OK_OPTION) {
      return;
    }

    // Lock write
    if (!startTransaction("Couldn't lock Gedcom for write")) {
      return;
    }

    // Stop editing old
    flushEditing(true);

    // Try to create
    Entity old = entity;
    Entity created = null;
    try {
      switch (type) {
        case Gedcom.INDIVIDUALS:
          created = gedcom.createIndi("", "", 0, relation, entity);
          break;
        case Gedcom.NOTES:
          created = gedcom.createNote(entity);
          break;
        case Gedcom.MULTIMEDIAS:
          created = gedcom.createMedia(entity);
          break;
      }
    } catch (GedcomException ex) {
      JOptionPane.showMessageDialog(
        getFrame(),
        ex.getMessage(),
        EditView.resources.getString("error"),
        JOptionPane.ERROR_MESSAGE
      );
    }

    // End transaction
    endTransaction();

    // Set new entity
    if (created!=null) {
      setEntity(created);
      //gedcom.fireEntitySelected(null,old    ,true);
      //gedcom.fireEntitySelected(null,created,false);
    }

  }


  /**
   * Action: Create Child
   */
  private void actionCreateChild() {
    createEntity(Gedcom.INDIVIDUALS,Gedcom.REL_CHILD,resources.getString("new.child"));
  }

  /**
   * Action: Create Parent
   */
  private void actionCreateParent() {
    createEntity(Gedcom.INDIVIDUALS,Gedcom.REL_PARENT,resources.getString("new.parent"));
  }

  /**
   * Action: Create Spouse
   */
  private void actionCreateSpouse() {
    createEntity(Gedcom.INDIVIDUALS,Gedcom.REL_SPOUSE,resources.getString("new.spouse"));
  }

  /**
   * Action: Create Note
   */
  private void actionCreateNote() {
    createEntity(Gedcom.NOTES, 0, resources.getString("new.note"));
  }

  /**
   * Action: Create Media
   */
  private void actionCreateMedia() {
    createEntity(Gedcom.MULTIMEDIAS, 0, resources.getString("new.media"));
  }

  /**
   * Action: Return
   */
  private void actionReturn() {

      // Return to last entity from return-stack
      int last = returnStack.size()-1;
      if (last==-1) {
        return;
      }

      Entity old = (Entity)returnStack.elementAt(last);
      returnStack.removeElementAt(last);
      setEntity(old,true);
      actionButtonReturn.setEnabled(returnStack.size()>0);

      // .. done
    }

  /**
   * Action: Add
   */
  private void actionAdd() {

    // Depending on Gedcom of current entity
    if (entity==null)
      return;

    Gedcom gedcom = entity.getGedcom();

    // .. LockWrite
    if (!startTransaction("Couldn't save")) {
      return;
    }

    // .. Stop Editing
    flushEditing(true);

    // .. only in case of single selection
    TreePath paths[] = treeOfProps.getSelectionPaths();
    if ( (paths==null) || (paths.length!=1) ) {
      endTransaction();
      return;
    }
    TreePath path = treeOfProps.getSelectionPath();

    // .. calculate new props
    Property prop = (Property)path.getLastPathComponent();

    // .. Confirm
    ChoosePropertyBean choose = new ChoosePropertyBean(prop.getKnownProperties(),resources);
    JCheckBox check = new JCheckBox(resources.getString("add.default_too"),true);

    Object[] message = new Object[3];
    message[0] = resources.getString("add.choose");
    message[1] = choose;
    message[2] = check;

    int option = JOptionPane.showOptionDialog(
      this,
      message,
      resources.getString("add.title"),
      JOptionPane.OK_CANCEL_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null, null, null
    );

    // .. OK or Cancel ?
    if (option != JOptionPane.OK_OPTION) {
      gedcom.endTransaction();
      return;
    }

    // .. Calculate chosen properties
    Property[] props = choose.getResultingProperties();

    if ( (props==null) || (props.length==0) ) {
      JOptionPane.showMessageDialog(
        this,
        resources.getString("add.must_enter"),
        resources.getString("error"),
        JOptionPane.ERROR_MESSAGE
      );
      gedcom.endTransaction();
      return;
    }

    // .. add properties
    boolean doSub = check.isSelected();
    for (int i=0;i<props.length;i++) {
      if (doSub) {
        props[i].addDefaultProperties();
      }
      prop.addProperty(props[i]);
    }

    // .. UnlockWrite
    endTransaction();
  }

  /**
   * Action: Del
   */
  private void actionDel() {

    TreePath paths[] = treeOfProps.getSelectionPaths();
    boolean changed = false;

    // .. check if there are some selections
    if ( (paths==null) || (paths.length==0) ) {
      return;
    }

    // .. LockWrite
    if (!gedcom.startTransaction()) {
      JOptionPane.showMessageDialog(
        this,
        "Couldn't save",
        resources.getString("error"),
        JOptionPane.ERROR_MESSAGE
      );
      return;
    }

    // .. Stop Editing
    stopEditing(true);

    // .. remove every selected node
    for (int i=0;i<paths.length;i++) {

      Property prop = (Property)paths[i].getLastPathComponent();
      String veto = prop.getDeleteVeto();

      if (veto!=null) {

        JTextPane tp = new JTextPane();
        tp.setText(veto);
        tp.setEditable(false);
        JScrollPane sp = new JScrollPane(tp,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
          public Dimension getPreferredSize() {
            return new Dimension(128,64);
          }
        };

        Object message[] = new Object[2];
        message[0] = resources.getString("del.leads_to",prop.getTag());
        message[1] = sp;

        // Show veto and respect user choice
        int rc = JOptionPane.showConfirmDialog(
          this,
          message,
          resources.getString("warning"),
          JOptionPane.OK_CANCEL_OPTION
        );
        if (rc==JOptionPane.OK_OPTION)
          veto=null;

        // Continue with/without veto
      }

      if (veto==null) {
        entity.getProperty().delProperty( prop );
        changed = true;
      }

    // Next selected prop
    }

    // .. UnlockWrite
    gedcom.endTransaction();

    // .. done
  }

  /**
   * Action: Up/Down
   */
  private void actionUpDown(ActionEvent e) {

    // Move property UP/DOWN ?
    if ( (!e.getActionCommand().equals("UP")) && (!e.getActionCommand().equals("DOWN")) ){
      return;
    }

    // .. LockWrite
    if (!gedcom.startTransaction()) {
      JOptionPane.showMessageDialog(
        this,
        "Couldn't save",
        resources.getString("error"),
        JOptionPane.ERROR_MESSAGE
      );
      return;
    }

    // .. Stop Editing
    flushEditing(true);

    // .. Calculate property that is moved
    entity.getProperty().moveProperty(
      currentNode,
      e.getActionCommand().equals("UP") ? Property.UP : Property.DOWN
    );

    // .. UnlockWrite
    gedcom.endTransaction();

    // 03.02.2000 Since the movement of properties is not
    // signalled by any event, we have to reselect the node again
    prepareTreeModel();
    TreePath path = new TreePath(entity.getProperty().getPathTo(currentNode));
    treeOfProps.setSelectionPath(path);

  }

  /**
   * Helper that adds buttons to a panel
   */
  private AbstractButton addButton(Container c, String action, String text, ImgIcon image, String tip, boolean enabled) {
    return addButton(c, action, text, image, tip, enabled, new JButton());
  }

  /**
   * Helper that adds buttons to a panel
   */
  private AbstractButton addButton(Container c, String action, String text, ImgIcon image, String tip, boolean enabled, AbstractButton result) {

    if (text!=null) {
      result.setText(resources.getString(text));
    }
    if (image!=null) {
      result.setIcon(new ImageIcon(image.getImage()));
    }
    if (tip!=null) {
      result.setToolTipText(resources.getString(tip));
    }
    result.setEnabled(enabled);
    result.setMargin(new Insets(0,0,0,0));

    result.setActionCommand(action);
    result.addActionListener(this);

    c.add(result);

    return result;
  }

  /**
   * Let proxy flush editing property
   * @return status for successfull flushing of edit
   */
  boolean flushEditing(boolean alreadyLocked) {

    // Finish old editing
    if (currentProxy == null) {
      return true;
    }

    // Prepare for finishing changed and finish
    if (currentProxy.hasChanged()) {
      if (alreadyLocked) {
        currentProxy.finish();
      } else {
        Gedcom gedcom = entity.getGedcom();
        if (gedcom.startTransaction()) {
          currentProxy.finish();
          gedcom.endTransaction();
        } else {
          int result = JOptionPane.showConfirmDialog(
            this,
            "Couldn't save",
            resources.getString("error"),
            JOptionPane.OK_CANCEL_OPTION
          );
          return (result==JOptionPane.CANCEL_OPTION);
        }
      }
    }

    // Done
    return true;
  }

  /**
   * Notification that an existing gedcom has been closed
   */
  public void gedcomClosed(Gedcom which) {
  }

  /**
   * Notification that a change in a Gedcom-object took place.
   */
  public void handleChange(Change change) {

    // Do I show an entity's properties now ?
    if (entity==null) {
      return;
    }

    // Entity deleted ?
    if ( change.isChanged(Change.EDEL) ) {

      // Loop through known entity ?
      boolean affected = false;

      Enumeration ents = change.getEntities(Change.EDEL).elements();
      while (ents.hasMoreElements()) {

        Object ent = ents.nextElement();

        // ... a removed entity has to be removed from stack
        while (returnStack.removeElement(ent)) {};

        // ... and might affect the current edit view
        affected |= (ent==entity);
      }

      // Is this a show stopper at this point?
      if (affected==true) {
        cancelEditing();
        setEntity(null,true);
        return;
      }

      // continue
    }

    // Property added/removed ?
    if ( change.isChanged(Change.PADD)
       ||change.isChanged(Change.PDEL)) {

      prepareTreeModel();

      // .. select added
      Vector padd = change.getProperties(Change.PADD);
      if (padd.size()>0) {
        PropertyTreeModel model = (PropertyTreeModel)treeOfProps.getModel();
        Property root = (Property)model.getRoot();
        Property first = (Property)padd.firstElement();
        if (first instanceof PropertyEvent) {
          Property pdate = ((PropertyEvent)first).getDate(false);
          first = (pdate!=null) ? pdate : first;
        }
        Property[] path = root.getPathTo(first);
        if (path!=null) {
          treeOfProps.setSelectionPath(new TreePath(path));
        }
      }
      return;
    }

    // Property modified ?
    if ( change.isChanged(change.PMOD) ) {
      if ( change.getEntities(Change.EMOD).contains(entity)) {
        PropertyTreeModel treeModel = (PropertyTreeModel)treeOfProps.getModel();
        treeModel.firePropertiesChanged(change.getProperties(Change.PMOD));
        return;
      }
    }

    // Done
  }

  /**
   * Notification that the gedcom is being closed
   */
  public void handleClose(Gedcom which) {

    // Empty stack
    returnStack.removeAllElements();

    // Leave entity
    if (entity!=null) {
      setEntity(null);
    }

    // Forget Gedcom
    gedcom.removeListener(this);
  }

  /**
   * Notification that an entity has been selected.
   */
  public void handleSelection(Selection selection) {

    if (!actionCheckStick.isSelected()) {
      setEntity(selection.getEntity());
    }
  }

  /**
   * Prepare the tree-model that lies behind the tree.
   */
  private void prepareTreeModel() {

    // NM 16 Dec 1998 This is some kind of hack :(
    // When a new model is set for a JTree, it signals a
    // valueChanged to its SelectionListeners, which in this
    // case means (de-)activation of buttons. That leads to
    // a focus change which leads to a redraw of the JTree.
    // When the current model has changed just a little bit
    // (which happens during GedcomWriteLock), a null-Rectangle
    // is generated somewhere in Swing which crashes the repaint 8)
    // We'll detach from JTree for the time the new model is setup
    // and attach later on

    treeOfProps.removeTreeSelectionListener(this);
    treeOfProps.setModel(new PropertyTreeModel(entity.getProperty()));
    treeOfProps.addTreeSelectionListener(this);

    // Expand all nodes
    for (int i=0;i<treeOfProps.getRowCount();i++) {
      treeOfProps.expandRow(i);
    }

    // Done
  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {

    // Remember registry
    registry.put("divider",split.getDividerLocation());
    registry.put("last", entity.getId());
    registry.put("sticky", actionCheckStick.isSelected());

    // Stop Listening
    gedcom.removeListener(this);

    // Continue
    super.removeNotify();

    // Done
  }

  /**
   * returns the frame this control resides in
   */
  /*package*/ Frame getFrame() {
    return frame;
  }

  /**
   * returns the currently viewed entity
   */
  /*package*/ Entity getEntity() {
    return entity;
  }

  /**
   * An entity in the gedcom data has been selected.
   * This method prepares editing of the selected entity.
   */
  /*package*/ void setEntity(Entity pEntity) {
    setEntity(pEntity, false);
  }

  /**
   * An entity in the gedcom data has been selected.
   * This method prepares editing of the selected entity.
   */
  /*package*/ void setEntity(Entity pEntity, boolean returned) {

    // Finish old editing
    if (!stopEditing(false)) {
      return;
    }

    // Put last entity on return-stack
    if ((!returned)&&(entity!=null)) {
      returnStack.addElement(entity);
      if (returnStack.size()>MAX_RETURN) {
        returnStack.removeElementAt(0);
      }
      actionButtonReturn.setEnabled(returnStack.size()>0);
    }

    // Remember entity
    entity=pEntity;

    // Create tree
    if (entity==null) {
      if (treeOfProps!=null) {
        treeOfProps.removeTreeSelectionListener(this);
        treeOfProps = null;
      }
    } else {
      // .. create the tree
      treeOfProps = new JTree() {
        // LCD
        /** Calculate ToopTipText depending on property under mouse */
        public String getToolTipText(MouseEvent event) {
          // .. calc path to node under mouse
          TreePath path = getPathForLocation(event.getX(),event.getY());
          if ((path==null) || (path.getPathCount()==0))
          return null;
          // .. calc property
          Property p = (Property)path.getLastPathComponent();
          // .. calc information text
          String info = p.getInfo();
          if (info==null) {
            return "Unknown";
          }
          // .. return max 60
          info = info.replace('\n',' ');
          if (info.length()<=60)
            return info;
          return info.substring(0,60)+"...";
        }
        // EOC
      };
      ToolTipManager.sharedInstance().registerComponent(treeOfProps);

      // .. prepare data
      prepareTreeModel();

      // .. prepare rendering
      treeOfProps.setCellRenderer(new PropertyCellRenderer());

      // .. done
    }


    // Update view
    paneForTree.getViewport().setView(treeOfProps);
    paneForTree.validate();
    paneForTree.repaint();

    // Pre-selected editing node ?
    if ((entity!=null)&&(treeOfProps.isShowing())) {
      treeOfProps.setSelectionRow( 0 );
    }

    // Update creation buttons
    updateCreateButtons();

    // Done
  }

 /**
   * Updates (optional) create Buttons at the bottom
   */
  private void updateCreateButtons() {

    // Same?
    String newType = ""+Gedcom.getType(entity);
    if (newType.equals(createPanel.getName())) {
      return;
    }

    createPanel.setName(newType);

    // Remove all
    createPanel.removeAll();

    Label label=new Label(resources.getString("new"));
    createPanel.add(label);

    //new Font("Arial", Font.PLAIN, 8)

    // None
    if (entity!=null) {

      // Create Buttons
      Object[] creates = entity2create[entity.getType()];

      for (int c=0;c<creates.length/3;c++) {
        String action = (String )creates[c*3+0];
        ImgIcon img   = (ImgIcon)creates[c*3+1];
        String tip    = (String )creates[c*3+2];
        addButton(createPanel, action, tip, img, null, true).setFont(label.getFont());
      }
    }

    // make sure that's seen, too
    createPanel.validate();
    createPanel.repaint();

    // Done
  }

  /**
   * Prepare a proxy for editing a property
   */
  private void startEditingOf(Property prop, boolean keepSimple) {

    // New prop ?
    if (prop == null) {
      return;
    }

    // Calculate editing for property
    String me    = getClass().getName(),
           pkg   = me.substring( 0, me.lastIndexOf(".") + 1 ),
           proxy = prop.getProxy();

    if (proxy == "") {
      return;
    }

    // Create proxy
    try {
      if (keepSimple)
        throw new Exception();
      currentProxy = (Proxy) Class.forName( pkg + "Proxy" + proxy ).newInstance();
    } catch (Exception e) {
      currentProxy = new ProxyUnknown();
    }

    // Add Image+Heading
    JLabel label = new JLabel();
    label.setIcon(new ImageIcon(prop.getImage(true).getImage()));
    label.setText(prop.getTag());
    label.setAlignmentX(0);
    label.setBorder(new EmptyBorder(2,0,8,0));
    panelForProxy.add(label);

    // Add proxy components
    try {
      currentProxy.start(panelForProxy,label,prop,this);
    } catch (ClassCastException ex) {
      System.out.println("Wrong proxy for property "+prop);
    }
    
    // Layout change !
    panelForProxy.validate();
    panelForProxy.doLayout();

    // Done
  }

  /**
   * Cancels proxy from editing
   */
  void cancelEditing() {

    // Clear up
    currentProxy = null;
    panelForProxy.removeAll();

    // Layout change !
    panelForProxy.invalidate();
    panelForProxy.validate();
    panelForProxy.repaint();

  }

  /**
   * Stop proxy from editing property
   * @return status for successfull stop of edit
   */
  boolean stopEditing(boolean alreadyLocked) {

    // Finish old editing
    if (currentProxy == null) {
      return true;
    }

    if (!flushEditing(alreadyLocked)) {
      return false;
    }

    cancelEditing();

    // Done
    return true;
  }

  /**
   * Called when the user changes a selection.
   * Changes the current proxy.
   */
  public void valueChanged(TreeSelectionEvent e) {

    // Look if exactly one node has been selected
    if (treeOfProps.getSelectionCount()==0) {
      // Disable action buttons
      actionButtonAdd   .setEnabled(false);
      actionButtonRemove.setEnabled(false);
      actionButtonUp    .setEnabled(false);
      actionButtonDown  .setEnabled(false);
      // Done
      return;
    }

    if (treeOfProps.getSelectionCount()>1) {
      // En/Disable action buttons
      actionButtonAdd   .setEnabled(false);
      actionButtonRemove.setEnabled(true );
      actionButtonUp    .setEnabled(false);
      actionButtonDown  .setEnabled(false);
      // Done
      return;
    }

    // Calculate selection path
    TreePath path = treeOfProps.getSelectionPath();

    // Prepare proxy for editing propery behind that single node
    Property prop = (Property)path.getLastPathComponent();
    currentNode = prop;

    // Stop editing via old proxy before starting with new one
    if (!stopEditing(false)) {
      // .. stop here
      return;
    }
    startEditingOf(prop,keepSimple);

    // Enable action buttons
    actionButtonAdd.setEnabled(true);
    if (path.getPathCount() > 1) {
      actionButtonRemove.setEnabled(true);
    } else {
      actionButtonRemove.setEnabled(false);
    }

    actionButtonUp    .setEnabled(currentNode.getPreviousSibling()!=null);
    actionButtonDown  .setEnabled(currentNode.getNextSibling()    !=null);

    // Done
  }
}
