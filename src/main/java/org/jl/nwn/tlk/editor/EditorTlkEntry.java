package org.jl.nwn.tlk.editor;

import java.io.Serializable;
import org.jl.nwn.tlk.*;

class EditorTlkEntry extends TlkEntry implements Cloneable, Serializable{
    
    private boolean modified = false;
    
    public EditorTlkEntry(){
    }
    
    public EditorTlkEntry( TlkEntry entry, boolean modified ){
        super( entry );
        this.modified = modified;
    }
    
    @Override
    public Object clone(){
        EditorTlkEntry ret = new EditorTlkEntry( this, this.modified );
        return ret;
    }
    
    @Override
    public void setFlags(byte type) {
        modified = (type != getFlags()) | modified;
        super.setFlags( type );
    }
    
    @Override
    public void setSoundResRef(String sndResRef) {
        modified = !sndResRef.equals( getSoundResRef() ) | modified;
        if ( modified ) setSoundFlag( sndResRef.length() > 0 );
        super.setSoundResRef( sndResRef );
    }
    
    @Override
    public void setString(String content) {
        modified = !content.equals( getString() ) | modified;
        if ( modified ) setStringFlag( content.length() > 0 );
        super.setString( content );
    }
    
    @Override
    public void setSoundLength(float soundLength){
        modified = getSoundLength() != soundLength | modified;
        if ( modified ) setSoundLengthFlag( soundLength != 0 );
        super.setSoundLength( soundLength );
    }
    
    void setModified(boolean modified) {
        this.modified = modified;
    }
    
    public boolean isModified() {
        return modified;
    }
}