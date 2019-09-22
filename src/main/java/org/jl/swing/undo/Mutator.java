package org.jl.swing.undo;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEditSupport;

/*
 * TODO: ??? add a setEditName(String name) to set the presentation name on
 * the next edit ?
 */
public abstract class Mutator{
    public static final String PROP_MODIFIED = "modified";

    private final MutatorUndoSupport undoSupport;

    protected class MutatorUndoSupport extends UndoableEditSupport{
        private final List<Mutator> mutators = new LinkedList<Mutator>();
        private int editState, editStateSaved;

        @Override protected CompoundEdit createCompoundEdit(){
            return new CompoundEdit(){
                @Override public void undo(){
                    if (isSignificant())
                        setModified(--editState != editStateSaved);
                    for ( Mutator m : mutators )
                        m.compoundUndo();
                    super.undo();
                }
                @Override public void redo(){
                    if (isSignificant())
                        setModified(++editState != editStateSaved);
                    for ( Mutator m : mutators )
                        m.compoundRedo();
                    super.redo();
                }
            };
        }
        @Override public void endUpdate(){
            if ( compoundEdit.isSignificant() )
                setModified(++editState != editStateSaved);
            super.endUpdate();
        }

        protected void stateSaved(){
            editStateSaved = editState;
            setModified(false);
        }

        protected void addMutator(Mutator m){
            mutators.add(m);
        }

        protected void incEditCount(){
            setModified( ++editState != editStateSaved );
        }

        protected void decEditCount(){
            setModified( --editState != editStateSaved );
        }
    }

    public abstract class ModelEdit extends AbstractUndoableEdit{
        /** true if this edit is part of a compund edit */
        protected final boolean isCompoundEdit =
                undoSupport.getUpdateLevel() > 0;
        String presentationName;
        public ModelEdit( String presentationName ){
            super();
            this.presentationName = presentationName;
        }
        @Override public String getPresentationName(){
            return presentationName;
        }
        @Override public void redo(){
            super.redo();
            if (isSignificant() && !isCompoundEdit)
                undoSupport.incEditCount();
            performEdit();
        }

        @Override public void undo(){
            super.undo();
            if (isSignificant() && !isCompoundEdit)
                undoSupport.decEditCount();
        }
        public Object invoke(){
            Object o = performEdit();
            undoSupport.postEdit(this);
            if (isSignificant() && !isCompoundEdit)
                undoSupport.incEditCount();
            return o;
        }
        abstract protected Object performEdit();
    }

    public Mutator(){
        undoSupport = new MutatorUndoSupport();
        undoSupport.addMutator(this);
    }

    /**
     * Chain this Mutator to the given argument, so that all edits done by this
     * mutator are reflected on the master. All listeners can subscribe to
     * the master mutator and compound edits can be aggregated over all mutators
     * in the chain.
     */
    public Mutator( Mutator master ) {
        undoSupport = master.undoSupport;
        undoSupport.addMutator(this);
    }

    public void beginUpdate(){
        undoSupport.beginUpdate();
    }

    public void endUpdate(){
        undoSupport.endUpdate();
    }

    public void addUndoableEditListener(UndoableEditListener l){
        undoSupport.addUndoableEditListener(l);
    }

    public void removeUndoableEditListener(UndoableEditListener l){
        undoSupport.removeUndoableEditListener(l);
    }

    public void stateSaved(){
        undoSupport.stateSaved();
    }

    protected abstract void compoundUndo();

    protected abstract void compoundRedo();

    // property change stuff ---------------------------------------------------
    /**
     * Holds value of property modified.
     */
    private boolean modified;

    /**
     * Utility field used by bound properties.
     */
    protected java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Getter for property modified.
     * @return Value of property modified.
     */
    public boolean isModified() {
        return this.modified;
    }

    /**
     * Setter for property modified.
     * @param modified New value of property modified.
     */
    public void setModified(boolean modified){
        //System.out.println("setModified : " + modified + " was : " + this.modified);
        boolean oldModified = this.modified;
        this.modified = modified;
        propertyChangeSupport.firePropertyChange(PROP_MODIFIED, oldModified, modified);
    }
}
