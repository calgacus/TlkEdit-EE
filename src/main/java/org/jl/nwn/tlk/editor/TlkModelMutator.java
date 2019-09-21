package org.jl.nwn.tlk.editor;

import javax.swing.ListSelectionModel;
import org.jl.nwn.NwnLanguage;
import org.jl.swing.undo.Mutator.ModelEdit;
import org.jl.swing.undo.TableModelMutator;

public class TlkModelMutator extends TableModelMutator{
    
    /** Creates a new instance of TlkModelMutator */
    public TlkModelMutator( TlkModel model, ListSelectionModel lsl ){
        super(model, lsl);
    }
    
    class LanguageEdit extends ModelEdit{
        NwnLanguage newLanguage, oldLanguage;
        public LanguageEdit( String pName, NwnLanguage newLanguage ){
            super(pName);
            this.newLanguage = newLanguage;
        }
        @Override protected Object performEdit(){
            oldLanguage = ((TlkModel)model).getLanguage();
            ((TlkModel)model).setLanguage(newLanguage);
            return null;
        }
        @Override public void undo(){
            super.undo();
            ((TlkModel)model).setLanguage(oldLanguage);
        }
    }
    
    public void setSelectionModel(ListSelectionModel lsl){
        this.lsl = lsl;
    }
    
}
