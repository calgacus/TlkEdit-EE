package org.jl.nwn.spell;

import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.WordTokenizer;
import org.jl.nwn.tlk.editor.TlkModel;

/**
 */
public class TlkWordFinder extends CharSequenceWordFinder{
    
    protected TlkModel model;
    protected StringBuilder sb = new StringBuilder();
    protected boolean modified = false;
    protected int row;
    
    /** Creates a new instance of TlkWordFinder */
    public TlkWordFinder( TlkModel model ){
        super("");
        this.model = model;
    }
    
    public void setEntry( int pos ){
        modified = false;
        row = pos;
        String s = String.valueOf(model.getValueAt(pos, 2));
        sb.replace(0, sb.length(), s);
        getTokenizer().setCharSequence(sb);        
        //updateCharSequence(0, sb.length(), WordTokenizer.CHANGE_SEQUENCE);
        init();
        //System.out.println(getTokenizer().getCharSequence());
    }
    
    @Override public void replace(String newWord, Word currentWord){
        int start = currentWord.getStart();
        int end = currentWord.getEnd();
        sb.replace( start, end, newWord );
        modified = true;
        updateCharSequence(start, end, WordTokenizer.INSERT_CHARS);
        updateModel(sb.toString(), row);
    }
    
    public void updateModel( String s, int row ){
        model.setValueAt( sb.toString(), row, 2 );
    }
    
    public boolean getModified(){
        return modified;
    }
    
    public String currentString(){
        return sb.toString();
    }
    
}
