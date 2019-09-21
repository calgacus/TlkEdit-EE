package org.jl.swing.undo;

import java.util.Arrays;
import java.util.Collection;
import javax.swing.ListSelectionModel;

/**
 */
public class ListMutator<E> extends Mutator{
    protected ListMutable<E> model;
    protected ListSelectionModel lsl;
    
    public static interface ListMutable<E>{
        public E remove( int index );
        public void add( int index, E element );
    }
    
    /** Creates a new instance of ListMutator */
    public ListMutator(Mutator m, ListMutable<E> model, ListSelectionModel lsl){
        super(m);
        this.model = model;
        this.lsl = lsl;
    }
    public ListMutator(ListMutable<E> model, ListSelectionModel lsl){
        super();
        this.model = model;
        this.lsl = lsl;
    }
    
    public class RemoveEdit extends ModelEdit{
        protected E oldValue;
        protected int index;
        public RemoveEdit( String name, int index ){
            super(name);
            this.index = index;
        }
        @Override
        public void redo() {
            super.redo();
            if (lsl != null)
                if ( !isCompoundEdit )
                    lsl.setSelectionInterval(index, index);
                else
                    lsl.addSelectionInterval(index, index);
        }
        
        @Override
        public void undo() {
            super.undo();
            model.add(index, oldValue);
            if (lsl != null)
                if ( !isCompoundEdit )
                    lsl.setSelectionInterval(index, index);
                else
                    lsl.addSelectionInterval(index, index);
        }        
        @Override
        protected E performEdit() {
            return (oldValue = model.remove(index));
        }
        @Override
        public E invoke(){
            return (E) super.invoke();
        }
    }
    
    public class AddEdit extends ModelEdit{
        protected E value;
        protected int index;
        public AddEdit( String name, int index, E value ){
            super(name);
            this.index = index;
            this.value = value;
        }
        @Override
        public void redo() {
            super.redo();
            if (lsl != null)
                if ( !isCompoundEdit )
                    lsl.setSelectionInterval(index, index);
                else
                    lsl.addSelectionInterval(index, index);
        }
        
        @Override
        public void undo() {
            super.undo();
            model.remove(index);
            if (lsl != null)
                if ( !isCompoundEdit )
                    lsl.setSelectionInterval(index, index);
                else
                    lsl.addSelectionInterval(index, index);
        }        
        @Override
        protected Object performEdit() {
            model.add(index, value);
            return null;
        }
    }
    
    public void add( int index, E elem ){
        new AddEdit( "Insert", index, elem ).invoke();
    }
    
    public E remove( int index ){
        return new RemoveEdit( "Remove", index ).invoke();
    }
    
    public void addAll( int index, Collection<E> elements ){
        if ( lsl != null )
            lsl.clearSelection();
        beginUpdate();
        for ( E e : elements )
            add( index++, e );
        endUpdate();
    }
    
    public void remove( int[] indices ){
        if ( lsl != null )
            lsl.clearSelection();
        Arrays.sort(indices);
        beginUpdate();
        for ( int i = indices.length-1; i > -1; i-- )
            remove(indices[i]);
        endUpdate();
    }
    
    @Override
    protected void compoundUndo() {
        if ( lsl != null ) lsl.clearSelection();
    }
    
    @Override
    protected void compoundRedo() {
        if ( lsl != null ) lsl.clearSelection();
    }    
}
