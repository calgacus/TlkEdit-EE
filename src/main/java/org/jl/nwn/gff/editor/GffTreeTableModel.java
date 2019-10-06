package org.jl.nwn.gff.editor;

import java.util.ArrayDeque;

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

public class GffTreeTableModel extends AbstractTreeTableModel {

    protected GffStruct root;

    protected GffMutator mutator = new GffMutator();

    public GffTreeTableModel( GffStruct root ){
        super(root);
        this.root = root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((GffField) parent).getChild(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((GffField) parent).getChildCount();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child){
        return ((GffField) parent).getChildIndex((GffField)child);
    }

    @Override
    public GffStruct getRoot() {
        return root;
    }

    public void setRoot( GffStruct s ){
        root = s;
        mutator.stateSaved();
        mutator.setModified(false);
        modelSupport.fireTreeStructureChanged( makePath(root) );
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        GffField f = (GffField)node;
        return ( node != root && // cannot edit top level struct
                ( column == 0 && f.getType() != GffCExoLocString.SUBSTRINGTYPE && f.getParent().getType() != Gff.LIST ) || // cannot edit substring label
                ( column==2 && f.getType()!=Gff.LIST ) ); // cannot edit void / list has no additional data
    }

    @Override
    public boolean isLeaf(Object node) {
        return !((GffField)node).allowsChildren();
    }

    /**
     * For columns 0 and 1 return label and typename strings, for column 2 return
     * {@link GffField} object.
     *
     * {@inheritDoc }
     */
    @Override
    public Object getValueAt(Object node, int column){
        GffField f = (GffField)node;
        switch (column) {
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
    public String getColumnName(int column) {
        switch (column) {
            case 0 : return "Label";
            case 1 : return "Type";
            case 2 : return "Value";
            default : return "<unknown>";
        }
    }

    @Override
    public int getHierarchicalColumn() {
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
        final ArrayDeque<GffField> stack = new ArrayDeque<>();
        GffField f = field;
        while ( f != null ){
            stack.addFirst(f);
            f = f.getParent();
        }
        return new TreePath(stack.toArray());
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
