package org.jl.nwn.gff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GffList extends GffField implements Iterable<GffStruct> {

    private List<GffStruct> structs;

    public GffList( String label ){
        this(new ArrayList<>(), label);
    }

    public GffList( List<GffStruct> structs, String label ){
        super( label, Gff.LIST );
        this.structs = structs;
    }

    public GffStruct get( int pos ){
        return structs.get( pos );
    }

    public void add( GffStruct s ){
        structs.add( s );
        s.parent = this;
    }

    public void add( int pos, GffStruct s ){
        structs.add( pos, s );
        s.parent = this;
    }

    public void remove( GffStruct s ){
        structs.remove( s );
        s.parent = null;
    }

    public void remove( int pos ){
        structs.remove( pos ).parent = null;
    }

    public int getSize(){
        return structs.size();
    }

    /**
     * return an iterator over the structs contained in this list.
     */
    @Override
    public Iterator<GffStruct> iterator() {
        return structs.iterator();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "----------------- [ begin list " + label + " ] -------------------\n" );
        for (final GffStruct struct : structs) {
            sb.append(struct).append('\n');
        }
        sb.append( "----------------- [ end of list " + label + " ] ------------------"   );
        return sb.toString();
    }

    /**
     * does a deep copy of this list
     * @return deep copy of this GffLIst
     */
    @Override
    public GffList clone() {
        final GffList clone = ( GffList ) super.clone();
        clone.structs = new ArrayList<>();
        for (final GffStruct struct : structs) {
            clone.structs.add(struct.clone());
        }
        return clone;
    }

    public int indexOf(Object o) {
        return structs.indexOf(o);
    }


    @Override
    public boolean allowsChildren(){
        return true;
    }

    @Override
    public int getChildCount(){
        return getSize();
    }

    @Override
    public GffField getChild( int index ){
        return get(index);
    }

    @Override
    public int getChildIndex( GffField f ){
        return indexOf( f );
    }

    @Override
    public void addChild( int index, GffField f ){
        add(index, (GffStruct) f);
    }

    @Override
    public void removeChild( GffField f ){
        remove((GffStruct)f);
    }

    @Override public void setData( Object o ){} // no-op
    @Override public Object getData(){ return null; } // no-op
}
