package org.jl.swing;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;

/**
 */
public class CheckBoxAction extends AbstractAction{
    
    protected class TogglePropListener implements PropertyChangeListener{
        AbstractButton ab;
        public TogglePropListener( AbstractButton ab ){
            this.ab = ab;
        }        
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if ( evt.getPropertyName() == SELECTED_PROPERTY )
                ab.setSelected((Boolean)evt.getNewValue());
        }        
    }
    
    public static final String SELECTED_PROPERTY = "actionselected";
    private boolean selected = false;
    
    /**
     * Creates a new instance of CheckBoxAction
     */
    public CheckBoxAction(){
    }
    
    @Override
    public void actionPerformed( ActionEvent e ){
        setSelected(!isSelected());
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected){
        firePropertyChange(SELECTED_PROPERTY, this.selected, selected);
        this.selected = selected;
    }
    
    public void connectButton( AbstractButton b ){
        this.addPropertyChangeListener( new TogglePropListener(b) );
    }
    
}
