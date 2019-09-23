package org.jl.nwn.gff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.jl.nwn.Version;

public class GffStruct extends GffField<Integer> implements Iterable<GffField> {

    private int id = 0;

    private List<GffField> children = new ArrayList<>();

    public GffStruct( int ID ){
        this( null, ID );
    }

    public GffStruct( String label ){
        super( label==null?"":label, Gff.STRUCT );
    }

    public GffStruct( String label, int ID ){
        this( label );
        setId( ID );
    }

    /**
     * @return empty struct with the top level id ( -1 )
     */
    public static GffStruct mkTopLevelStruct(){
        return new GffStruct( Gff.STRUCT_ID_TOPLEVEL );
    }

    /**
     * get field by label
     * @return field with given label or null if no such field exists in this struct
     */
    public GffField getChild( String label ) {
        for (final GffField field : children) {
            if (field.label.equals(label)) {
                return field;
            }
        }
        return null;
    }

    public void addChild( GffField s ){
        addChild( getSize(), s );
    }

    @Override
    public void addChild( int pos, GffField s ){
        if ( s.getType() == GffCExoLocString.SUBSTRINGTYPE )
            throw new IllegalArgumentException( "cannot add this type of node ! tpye : " + GffCExoLocString.SUBSTRINGTYPE );
        children.add(pos,s);
        s.parent = this;
    }

    /**
     * remove field with given label. if no such field exists the struct remains unchanged.
     * @param label label of the field to be removed
     * */
    public void remove( String label ){
        GffField f = getChild( label );
        if ( f != null ){
            children.remove( f );
            f.parent = null;
        }
    }

    /**
     * remove given field from this struct. if field does not belong to this struct the struct remains unchanged.
     * @param field field to be removed
     * */
    @Override
    public void removeChild( GffField field ){
        children.remove( field );
        field.parent = null;
    }

    /**
     * remove field at position pos
     * @param pos position of field to be removed
     * */
    public void remove( int pos ){
        children.remove(pos).parent = null;
    }

    /**
     * @return number of fields in this struct
     */
    public int getSize() { return children.size(); }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("********[struct " + label + " (" + id + ") ]********\n" );
        for (final GffField field : children) {
            sb.append(field).append('\n');
        }
        sb.append("********[struct end]********\n" );
        return sb.toString();
    }

    /**
     * @return id of this struct
     */
    public int getId() { return id; }

    /**
     * @param id new struct id
     */
    public void setId(int id) { this.id = id; }

    /**
     * does a deep copy of this struct
     * @return deep copy of this GffStruct
     */
    @Override
    public GffStruct clone(){
        final GffStruct clone = ( GffStruct ) super.clone();
        clone.children = new ArrayList<>();
        for (final GffField field : children) {
            clone.children.add(field.clone());
        }
        return clone;
    }

    /** @return an iterator over this struct's children. */
    @Override
    public Iterator<GffField> iterator() { return children.iterator(); }

    public Iterator<GffField> getDFIterator(){
        return new Iterator<GffField>(){
            final Stack<Iterator<GffField>> iterators = new Stack<>();
            boolean first = true;
            {
                iterators.push( iterator() );
            }

            @Override
            public boolean hasNext(){
                while ( !first && (!iterators.isEmpty() && !iterators.peek().hasNext()) )
                    iterators.pop();
                return !iterators.isEmpty();
            }

            @Override
            public GffField next(){
                if ( first ){
                    first = false;
                    return GffStruct.this;
                }
                final Iterator<GffField> it = iterators.peek();
                final GffField f = it.next();
                if ( f.type == Gff.STRUCT || f.type == Gff.LIST ) {
                    final Iterable<GffField> iterable = (Iterable<GffField>) f;
                    iterators.push(iterable.iterator());
                }
                return f;
            }

            @Override
            public void remove(){
                throw new UnsupportedOperationException();
            }
        };
    }

    public int indexOf(Object o) { return children.indexOf(o); }

    @Override
    public boolean allowsChildren() { return true; }

    @Override
    public int getChildCount() { return children.size(); }

    @Override
    public GffField getChild(int index) { return children.get(index); }

    @Override
    public int getChildIndex(GffField f) { return indexOf( f ); }

    @Override
    public Integer getData() { return getId(); }

    @Override
    public void setData(Integer data) { setId(data.intValue()); }

    public static void main( String ... args ) throws Exception{
        GffContent c =
                new DefaultGffReader(Version.getDefaultVersion())
                .load( new java.io.File( args[0] ) );
        final Iterator<GffField> it = c.getTopLevelStruct().getDFIterator();
        while ( it.hasNext() ){
            final GffField f = it.next();
            System.out.println( "GFFStruct " + (f.isDataField() ? f.toString() : f.label + "(" + f.getTypeName() + ")" ));
        }
    }
}
