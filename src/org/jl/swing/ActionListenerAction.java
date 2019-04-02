package org.jl.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import javax.swing.AbstractAction;

/**
 * An action that wraps an ActionListener object ( the reason behind this is
 * that ActionListeners can be easily created with EventHandler )
 */
public class ActionListenerAction extends AbstractAction{
    
    protected ActionListener al;
    
    /** Creates a new instance of ActionListenerAction */
    public ActionListenerAction( ActionListener al ) {
        super();
        this.al = al;
    }
    
    public ActionListenerAction( Object target, String action, String eventPropertyName ){
        this(EventHandler.create(ActionListener.class, target, action, eventPropertyName));
    }
    
    public void actionPerformed( ActionEvent e ){
        al.actionPerformed(e);
    }    
}
