package org.jl.swing.table;

import java.beans.Expression;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 * (needs a better name) Cell editor for columns that can contain values
 * of different types. The type of the value is determined by invoking
 * computeKey() on it, then a concrete CellEditor (delegate) for the type
 * is choosen from a map. A default can be set by using null as key.
 * The default implementation of computeKey uses the
 * KeyFunction object passed in the contructor or is the identity function.
 * The value passed to the delegate can be the original value or
 * the result of invoking a Projection on the original value (see map(...)).
 *
 * example :
 * for a column that can contain String or Float use
 * MappedCellEditor.GETCLASS as key function and call
 * map(String.class, stringEditor, null) and
 * map(Float.class, floatEditor, null)
 */
public class MappedCellEditor extends AbstractCellEditor implements TableCellEditor{
    
    public static class Projection{
        String methodName;
        Object[] args;
        
        public Projection( String methodName, Object[] args ){
            this.methodName = methodName;
            this.args = args;
        }
        
        public Object invoke(Object o){
            try{
                return new Expression(o, methodName, args ).getValue();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    
    public static final KeyFunction GETCLASS = new KeyFunction(){
        public Object computeKey( Object value, int row, int column ){
            return value.getClass();
        }
    };
    
    public static abstract class KeyFunction{
        public static KeyFunction newKeyFunction(final Object target, final String methodName){
            return new KeyFunction(){
                public Object computeKey( Object value, int row, int column ){
                    try{
                        return new Expression(target, methodName, new Object[]{value, row, column} ).getValue();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
        
        public static KeyFunction newKeyFunction(final Object target, final String methodName, final Object ... args){
            return new KeyFunction(){
                public Object computeKey( Object value, int row, int column ){
                    try{
                        return new Expression(target, methodName, args ).getValue();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
        
        public abstract Object computeKey( Object value, int row, int column );
    }
    
    protected Map<Object, CellEditor> map = new HashMap<Object, CellEditor>();
    protected Map<Object, Projection> projectionMap = new HashMap<Object, Projection>();
    protected KeyFunction keyFunction;
    protected CellEditor delegate;
    
    /** Creates a new instance of MappedCellEditor */
    public MappedCellEditor(){
        super();
    }
    
    public MappedCellEditor(KeyFunction keyFunction){
        this();
        this.keyFunction = keyFunction;
    }
    
    public Object computeKey( Object value, int row, int column ){
        return keyFunction != null ? keyFunction.computeKey(value, row, column) : value;
    }
    
    public void map( Object key, CellEditor editor, Projection p ){
        for ( CellEditorListener l : getCellEditorListeners() )
            editor.addCellEditorListener(l);
        map.put(key, editor);
        if ( p != null )
            projectionMap.put(key, p);
    }
    
    public Object getCellEditorValue(){
        return delegate.getCellEditorValue();
    }
    
    public void removeCellEditorListener(CellEditorListener l) {
        super.removeCellEditorListener(l);
        for ( CellEditor e : map.values() )
            e.removeCellEditorListener(l);
    }
    
    public void addCellEditorListener(CellEditorListener l) {
        super.addCellEditorListener(l);
        for ( CellEditor e : map.values() )
            e.addCellEditorListener(l);
    }
    
    public boolean stopCellEditing() {
        boolean retValue;
        retValue = delegate.stopCellEditing();
        return retValue;
    }
    
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column){
        Object key = computeKey(value, row, column);
        delegate = map.get( key );
        Projection p = null;
        if ( delegate == null ){
            delegate = map.get( null );
            p = projectionMap.get( null );
        } else
            p = projectionMap.get( key );
        if ( p != null )
            value = p.invoke(value);
        return ((TableCellEditor)delegate).getTableCellEditorComponent(table, value,isSelected,row,column);
    }
    
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }
    
}
