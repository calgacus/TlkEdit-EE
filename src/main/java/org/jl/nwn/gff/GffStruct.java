package org.jl.nwn.gff;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import org.jl.nwn.Version;

/**
 */
public class GffStruct extends GffField implements Iterable<GffField>{

    private int id = 0;

    private List<GffField> children = new ArrayList<GffField>();

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
     * */
    public static GffStruct mkTopLevelStruct(){
        return new GffStruct( Gff.STRUCT_ID_TOPLEVEL );
    }

    /**
     * get field by label
     * @return field with given label or null if no such field exists in this struct
     * */
    public GffField getChild( String label ){
        GffField field = null;
        for ( int i = 0; i < getSize(); i++ ){
            if ( getChild(i).getLabel().equals( label ) ){
                field = getChild(i);
                break;
            }
        }
        return field;
    }

    public void addChild( GffField s ){
        addChild( getSize(), s );
    }

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
    public void removeChild( GffField field ){
        children.remove( field );
        field.parent = null;
    }

    /**
     * remove field at position pos
     * @param pos position of field to be removed
     * */
    public void remove( int pos ){
        ((GffField) children.remove(pos)).parent = null;
    }

    /**
     * @return number of fields in this struct
     * */
    public int getSize(){
        return children.size();
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("********[struct " + label + " (" + id + ") ]********\n" );
        for ( int i = 0; i < getSize(); i++ )
            sb.append( getChild(i).toString() + "\n" );
        sb.append("********[struct end]********\n" );
        return sb.toString();
    }

    /**
     * @return id of this struct
     */
    public int getId() {
        return id;
    }

    /**
     * @param id new struct id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * does a deep copy of this struct
     * @return deep copy of this GffStruct
     * */
    public Object clone(){
        GffStruct clone = ( GffStruct ) super.clone();
        clone.children = new Vector();
        for ( int i = 0; i < children.size(); i++ ){
            clone.children.add( (GffField) children.get(i).clone() );
        }
        return clone;
    }

    /**
     * return an iterator over this struct's children.
     */
    public Iterator<GffField> iterator(){
        return children.iterator();
    }

    public Iterator<GffField> getDFIterator(){
        return new Iterator<GffField>(){
            Stack<Iterator> iterators = new Stack<Iterator>();
            boolean first = true;
            {
                iterators.push( iterator() );
            }

            public boolean hasNext(){
                while ( !first && (!iterators.isEmpty() && !iterators.peek().hasNext()) )
                    iterators.pop();
                return !iterators.isEmpty();
            }

            public GffField next(){
                if ( first ){
                    first = false;
                    return GffStruct.this;
                }
                Iterator it = iterators.peek();
                GffField f = (GffField) it.next();
                if ( f.type == Gff.STRUCT || f.type == Gff.LIST ){
                    Iterator subIt = (f.type == Gff.STRUCT)? ((GffStruct) f).iterator() : ((GffList) f).iterator();
                    iterators.push( subIt );
                }
                return f;
            }

            public void remove(){
                throw new UnsupportedOperationException();
            }
        };
    }

    public int indexOf(Object o) {
        return children.indexOf(o);
    }

    public boolean allowsChildren(){
        return true;
    }

    public int getChildCount(){
        return getSize();
    }

    public GffField getChild( int index ){
        return children.get(index);
    }

    public int getChildIndex( GffField f ){
        return indexOf( f );
    }

    public Object getData(){
        return getId();
    }

    public void setData( Object data ){
        setId(((Number)data).intValue());
    }

    public static void main( String ... args ) throws Exception{
        GffContent c =
                new DefaultGffReader(Version.getDefaultVersion())
                .load( new java.io.File( args[0] ) );
        Iterator it = c.getTopLevelStruct().getDFIterator();
        while ( it.hasNext() ){
            GffField f = ( GffField ) it.next();
            System.out.println( "GFFStruct " + (f.isDataField() ? f.toString() : f.label + "(" + f.getTypeName() + ")" ));
        }
    }

}
