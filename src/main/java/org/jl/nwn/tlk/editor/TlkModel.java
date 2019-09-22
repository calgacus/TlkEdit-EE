package org.jl.nwn.tlk.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.tlk.TlkContent;
import org.jl.nwn.tlk.TlkEntry;
import org.jl.nwn.tlk.TlkLookup;
import org.jl.swing.undo.ListMutator;
import org.jl.swing.undo.RowMutator;

public class TlkModel extends AbstractTableModel
        implements RowMutator.RowMutable<TlkEntry>, ListMutator.ListMutable<TlkEntry>{

    private TlkContent tlkContent;
    private boolean isUserTlk = false;
    private boolean displayHex = false;

    public TlkModel( TlkContent tlkContent ) {
        super();
        this.tlkContent = tlkContent;
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public int getRowCount() {
        return tlkContent.size() + 1;
    }

    private String hex(byte[] b){
        StringBuffer ret = new StringBuffer();
        int unsigned = 0;
        boolean nonZero = false;
        for (int i = 0; i < b.length; i++) {
            unsigned = b[i] < 0 ? 256 + b[i] : b[i];
            if (unsigned != 0)
                nonZero = true;
            if (unsigned < 16)
                ret.append("0"); //$NON-NLS-1$
            ret.append(Integer.toHexString(unsigned));
            ret.append(" "); //$NON-NLS-1$
        }
        return nonZero ? ret.toString() : ""; //$NON-NLS-1$
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row == tlkContent.size())
            if (col == 3)
                return 0.0f;
            else
                return ""; //$NON-NLS-1$
        TlkEntry entry = tlkContent.get(row);
        switch (col) {
            case 0 :
            {
                int rowNumber = row + (isUserTlk? TlkLookup.USERTLKOFFSET : 0);
                return ((entry instanceof EditorTlkEntry)&&((EditorTlkEntry)entry).isModified() ? "*" : "") //$NON-NLS-1$ //$NON-NLS-2$
                +(displayHex?"0x" + Integer.toHexString(rowNumber)
                :Integer.toString(rowNumber));
            }
            case 1 :
                return entry.getSoundResRef();
            case 2 :
                return entry.getString();
            case 3 :
                return entry.getSoundLength();
            case 4 :
                return entry.getFlags();
                //return Integer.toHexString(entry.getFlags());
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 0 :
                return Integer.class;
            case 1 :
                return File.class;
            case 2 :
                return String.class;
            case 3 :
                return Float.class;
            case 4 :
                return Byte.class;
        }
        return Object.class;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0 :
                return Messages.getString("TlkEdit.column_header_StrRef"); //$NON-NLS-1$
            case 1 :
                return Messages.getString("TlkEdit.column_header_ResRef"); //$NON-NLS-1$
            case 2 :
                return Messages.getString("TlkEdit.column_header_String"); //$NON-NLS-1$
            case 3 :
                return Messages.getString("TlkEdit.column_header_SoundLength"); //$NON-NLS-1$
            case 4 :
                return Messages.getString("TlkEdit.column_header_Flags"); //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col > 0 && col < 5;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        //System.out.println("TlkModel.setValueAt " + aValue);
        boolean append = false;
        Object old = getValueAt(row, col);
        if (row == tlkContent.size()) { // append
            //tlkContent.add(new TlkEntry());
            tlkContent.add(new EditorTlkEntry());
            append = true;
        }
        EditorTlkEntry entry = getEditorEntry( row );
        if (col == 1)
            entry.setSoundResRef(aValue.toString());
        else if (col == 2)
            entry.setString(aValue.toString());
        else if (col == 3)
            entry.setSoundLength(
                    ((Float) aValue).floatValue());
        else if (col == 4)
            entry.setFlags(((Byte) aValue).byteValue());
        boolean isModified = !old.toString().equals(aValue.toString());
        if (append)
            fireTableRowsInserted(row, row);
        else if (isModified){
            //System.out.println("fireTableRowsUpdated");
            fireTableRowsUpdated(row, row);
        }
    }

    public EditorTlkEntry getEditorEntry( int i ){
        TlkEntry e = (TlkEntry) tlkContent.get(i);
        if ( !(e instanceof EditorTlkEntry ) ){
            tlkContent.set( i, e = new EditorTlkEntry(e, false) );
        }
        return (EditorTlkEntry) e;
    }

    public boolean getEntryModified( int pos ){
        return ( tlkContent.get(pos) instanceof EditorTlkEntry && ((EditorTlkEntry)tlkContent.get(pos)).isModified());
    }

    public void setEntryModified( int pos, boolean modified ){
        EditorTlkEntry e = getEditorEntry(pos);
        if ( modified ^ e.isModified() ){
            e.setModified(modified);
            fireTableRowsUpdated(pos, pos);
        }
    }

    public void setIsUserTlk( boolean userTlk ){
        if ( userTlk ^ isUserTlk ){
            isUserTlk = userTlk;
            fireTableDataChanged();
        }
    }

    public boolean getIsUserTlk(){
        return isUserTlk;
    }

    public int size(){
        return tlkContent.size();
    }

    public void setSize(int newSize){
        if (newSize < tlkContent.size()) { // remove
            int[] indexes = new int[size()-newSize];
            for ( int i = 0; i < indexes.length; i++ )
                indexes[i] = newSize + i;
            removeRows(indexes);
        } else {
            List<TlkEntry> e = new ArrayList<TlkEntry>(newSize-size());
            for ( int i = 0; i < newSize-size(); i++ )
                e.add(new EditorTlkEntry());
            insertRows( size(), e );
        }
    }

    @Override
    public List<TlkEntry> removeRows( int[] selection ){
        if (selection.length > 0) {
            TlkEntry[] r = new TlkEntry[selection.length];
            for (int i = selection.length - 1; i > -1; i--){
                r[i] = tlkContent.remove(selection[i]);
            }
            fireTableRowsDeleted(
                    selection[0],
                    selection[selection.length - 1]);
            return Arrays.asList(r);
        } else
            return null;
    }

    @Override
    public void insertRows( int pos, List<TlkEntry> entries ){
        int p = pos;
        for ( TlkEntry e : entries )
            tlkContent.add( p++, e );
        fireTableRowsInserted( pos, pos + entries.size() - 1 );
    }

    public void setTlkContent( TlkContent c ){
        this.tlkContent = c;
        propertyChangeSupport.firePropertyChange("language", null, tlkContent.getLanguage());
        fireTableDataChanged();
    }

    /**
     * Utility field used by bound properties.
     */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Getter for property language.
     * @return Value of property language.
     */
    public NwnLanguage getLanguage() {
        return tlkContent.getLanguage();
    }

    /**
     * Setter for property language.
     * @param language New value of property language.
     */
    public void setLanguage(NwnLanguage language){
        NwnLanguage oldLanguage = tlkContent.getLanguage();
        tlkContent.setLanguage(language);
        propertyChangeSupport.firePropertyChange("language", oldLanguage, language);
    }

    @Override
    public void add( int index, TlkEntry e ){
        tlkContent.add( index, e );
        fireTableRowsInserted( index, index );
    }

    @Override
    public TlkEntry remove( int index ){
        TlkEntry e = tlkContent.remove( index );
        fireTableRowsDeleted( index, index );
        return e;
    }

    public boolean isDisplayHex() {
        return displayHex;
    }

    public void setDisplayHex(boolean displayHex) {
        if ( this.displayHex ^ displayHex ){
            this.displayHex = displayHex;
            fireTableDataChanged();
        }
    }

}
