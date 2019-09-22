/*
 * Created on 24.03.2005
 */
package org.jl.nwn.gff.editor;

import java.util.LinkedList;

import javax.swing.event.UndoableEditListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.GffField;
import org.jl.nwn.gff.GffList;
import org.jl.nwn.gff.GffStruct;
import org.jl.swing.undo.Mutator;

/**
 */
public class GffTreeTableModel extends AbstractTreeTableModel {

    protected GffStruct root;

    /** Used for constructing {@link TreePath} objects. */
    private final LinkedList stack = new LinkedList();

    protected GffMutator mutator = new GffMutator();

    public GffTreeTableModel( GffStruct root ){
        super(root);
        this.root = root;
    }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
         */
    @Override
    public Object getChild(Object arg0, int arg1) {
        return ((GffField) arg0).getChild(arg1);
    }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
         */
    @Override
    public int getChildCount(Object arg0) {
        return ((GffField) arg0).getChildCount();
    }
        /* (non-Javadoc)
         * @see org.jdesktop.swing.treetable.TreeTableModel#getColumnCount()
         */
    @Override
    public int getColumnCount() {
        return 3;
    }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
         */
    @Override
    public int getIndexOfChild(Object arg0, Object arg1){
        return ((GffField) arg0).getChildIndex((GffField)arg1);
    }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#getRoot()
         */
    @Override
    public Object getRoot() {
        return root;
    }

    public void setRoot( GffStruct s ){
        root = s;
        mutator.stateSaved();
        mutator.setModified(false);
        modelSupport.fireTreeStructureChanged( makePath(root) );
    }
        /* (non-Javadoc)
         * @see org.jdesktop.swing.treetable.TreeTableModel#isCellEditable(java.lang.Object, int)
         */
    @Override
    public boolean isCellEditable(Object arg0, int arg1) {
        GffField f = (GffField)arg0;
        return ( arg0 != root && // cannot edit top level struct
                ( arg1 == 0 && f.getType() != GffCExoLocString.SUBSTRINGTYPE && f.getParent().getType() != Gff.LIST ) || // cannot edit substring label
                ( arg1==2 && f.getType()!=Gff.LIST ) ); // cannot edit void / list has no additional data
    }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
         */
    @Override
    public boolean isLeaf(Object arg0) {
        return !((GffField)arg0).allowsChildren();
    }

    /** for columns 0 and 1 return label and typename strings, for column 2 return GffField object
     */
    @Override
    public Object getValueAt(Object arg0, int arg1){
        GffField f = (GffField)arg0;
        switch (arg1) {
            case 0 : { // label or list index
                if ( f.getType() == Gff.STRUCT && f.getParent() != null && f.getParent().getType() == Gff.LIST )
                    return "["+((GffList)f.getParent()).indexOf(f)+"]";
                else if ( f.getType() == GffCExoLocString.SUBSTRINGTYPE ){
                    return ((CExoLocSubString)f).gender;
                } else
                    return f.getLabel();
            }
            case 1 :
                return f.getTypeName();
            case 2 : {
                return f.getData();
            }
            default : return null;
        }
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        if ( value == null )
            throw new IllegalArgumentException( "value must not be null" );
        mutator.new ValueChangeEdit( (GffField)node, column, value ).invoke();
    }

    @Override
    public String getColumnName(int arg0) {
        switch (arg0){
            case 0 : return "Label";
            case 1 : return "Type";
            case 2 : return "Value";
            default : return "foo";
        }
    }

    @Override
    public Class getColumnClass(int arg0) {
        return super.getColumnClass(arg0);
    }

    @Override public int getHierarchicalColumn(){
       return 0;
    }

    public void insert( TreePath parentPath, GffField field, int index ){
        mutator.new InsertEdit( (GffField)parentPath.getLastPathComponent(),
                field, index ).invoke();
        /*
        GffField parent = (GffField) parentPath.getLastPathComponent();
        parent.addChild( index, field );
        undoSupport.postEdit(makeInsertEdit(parent, field, index));
        fireTreeNodesInserted( this, parentPath.getPath(), new int[]{parent.getChildIndex(field)}, new Object[]{field} );
         */
    }

    /**
     *remove last element of given path from the tree
     */
    public void remove( TreePath path ){
        GffField field = (GffField) path.getLastPathComponent();
        GffField parent = field.getParent();
        if ( parent != null ){
            mutator.new RemoveEdit(parent, field).invoke();
        } else{
            // do nothing - cannot remove top level struct
        }
    }

    /**
     * construct TreePath from root to given field
     * @return TreePath object for the path from the model root to the given field
     */
    public TreePath makePath( GffField field ){
        stack.clear();
        GffField f = field;
        while ( f != null ){
            stack.addFirst(f);
            f = f.getParent();
        }
        Object[] path = new Object[stack.size()];
        for ( int index = 0; index < path.length; index++ )
            path[index] = stack.removeFirst();
        return new TreePath(path);
    }

    // undo support ----------------------------------

    public class GffMutator extends Mutator{
        public class ValueChangeEdit extends Mutator.ModelEdit{
            GffField field;
            int col;
            Object newValue, oldValue;
            ValueChangeEdit( GffField field, int col, Object value ){
                super("Edit value");
                this.field = field;
                this.col = col;
                this.newValue = value;
            }
            @Override
            protected Object performEdit(){
                oldValue = setValue( field, col, newValue );
                return null;
            }
            @Override public void undo(){
                super.undo();
                setValue(field,col,oldValue);
            }
            private Object setValue(GffField field, int col, Object newValue){
                Object oldValue = getValueAt( field, col );
                if ( col == 0 )
                    field.setLabel(newValue.toString());
                else
                    field.setData(newValue);
                if ( !oldValue.equals( newValue ) ){
                    if ( field.getParent() == null )
                        modelSupport.firePathChanged(makePath(field));
                    else
                        modelSupport.fireChildChanged(makePath(field.getParent()), field.getParent().getChildIndex(field), field );
                }
                return oldValue;
            }
            @Override public boolean isSignificant(){
                return !oldValue.equals(newValue);
            }
        }

        public class InsertEdit extends Mutator.ModelEdit{
            GffField parent, child;
            int index;
            InsertEdit( GffField parent, GffField child, int index ){
                super("Insert Field");
                this.parent = parent;
                this.child = child;
                this.index = index;
            }
            @Override
            protected Object performEdit(){
                parent.addChild(index, child);
                modelSupport.fireChildrenAdded(
                        makePath(parent),
                        new int[]{parent.getChildIndex(child)},
                        new Object[]{child}
                );
                return null;
            }
            @Override public void undo(){
                super.undo();
                int childPos = parent.getChildIndex(child);
                parent.removeChild(child);
                modelSupport.fireChildrenRemoved(makePath(parent), new int[]{childPos}, new Object[]{child});
            }
        }

        public class RemoveEdit extends Mutator.ModelEdit{
            GffField parent, child;
            int position = -1;
            RemoveEdit( GffField parent, GffField child ){
                super("Remove Field");
                this.parent = parent;
                this.child = child;
            }
            @Override
            protected Object performEdit(){
                position = parent.getChildIndex(child);
                parent.removeChild(child);
                modelSupport.fireChildrenRemoved(makePath(parent), new int[]{position}, new Object[]{child});
                return null;
            }
            @Override public void undo(){
                super.undo();
                parent.addChild(position, child);
                modelSupport.fireChildrenAdded(makePath(parent), new int[]{position}, new Object[]{child});
            }
        }
        @Override
        public void compoundUndo(){}
        @Override
        public void compoundRedo(){}
    }

    public GffMutator getMutator(){
        return mutator;
    }

    public void addUndoableEditListener(UndoableEditListener l){
        mutator.addUndoableEditListener(l);
    }

    public void removeUndoableEditListener(UndoableEditListener l){
        mutator.removeUndoableEditListener(l);
    }
}
