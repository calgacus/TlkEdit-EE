package org.jl.nwn.tlk.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.jdesktop.swingx.event.MessageListener;
import org.jdesktop.swingx.event.MessageSource;
import org.jdesktop.swingx.event.MessageSourceSupport;
import org.jl.nwn.tlk.*;

/**
 */
public class TlkLookupPanel extends JPanel implements MessageSource {

    private TlkEdit tlkedit = null;
    private TlkEdit usertlkedit = null;

    protected MessageSourceSupport messageSupport = new MessageSourceSupport(this);

    private JTextField resField = new JTextField(16);
    private JTextArea textArea = new JTextArea();
    private JLabel posLabel = new JLabel("0");
    private JButton updateButton;
    private int position = -1; // displayed position
    private Action updateModel = new AbstractAction("update tlk") {
        {
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TlkEdit ed = position >= TlkLookup.USERTLKOFFSET ? usertlkedit : tlkedit;
            //ed.tlkTable.getModel().setValueAt(resField.getText(), position, 1);
            //ed.tlkTable.getModel().setValueAt(textArea.getText(), position, 2);
            ed.mutator.new SetValueAtEdit("Update", textArea.getText(), position, 2).invoke();
        }
    };

    public TlkEdit getTlkEdit() {
        return tlkedit;
    }

    public TlkEdit getUserTlkEdit() {
        return usertlkedit;
    }

    public TlkLookupPanel() {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        setLayout(new BorderLayout());
        setSize(300, 400);
        JPanel p = new JPanel();
        p.add(posLabel);
        //p.add(resField);
        add(p, BorderLayout.NORTH);
        add(updateButton = new JButton(updateModel), BorderLayout.SOUTH);
        updateButton.setMnemonic('u');
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        setVisible(true);
    }

    private void setNoValue() {
        posLabel.setText("---");
        textArea.setText("");
        updateModel.setEnabled(false);
    }

    /**
    try to parse value as int and display strref
     */
    public void lookup(Object value) {
        try {
            int strref = Integer.parseInt(value.toString());
            if (strref > -1) {
                lookup(strref);
            } else {
                setNoValue();
            }
        } catch (NumberFormatException nfe) {
            setNoValue();
        }
    }

    public void lookup(int position) {
        this.position = position;
        updateModel.setEnabled(false);
        if (position < 0) {
            throw new IllegalArgumentException("error : negative value");
        }
        if (isUserEntry(position)) {
            lookupUserTlk(position);
        } else {
            if (tlkedit == null && !isUserEntry(position)) {
                setNoValue();
            } else if (position > tlkedit.tlkTable.getModel().getRowCount() - 2) {
                posLabel.setText("no such entry : " + position);
                resField.setText("no such entry");
                textArea.setText("");
            } else {
                updateModel.setEnabled(true);
                //resField.setText(tlkedit.tlkTable.getModel().getValueAt(position, 1).toString());
                String tlkString = tlkedit.tlkTable.getModel().getValueAt(position, 2).toString();
                textArea.setText(tlkString);
                message(tlkString, position);
                textArea.setCaretPosition(0);
                posLabel.setText("StrRef " + Integer.toString(position));
                updateButton.setText("update " + tlkedit.getFile().getName());
            }
        }
    }

    private void lookupUserTlk(int position) {
        if (usertlkedit == null) {
            setNoValue();
        } else {
            int p = position ^ TlkLookup.USERTLKOFFSET;
            if (p > usertlkedit.tlkTable.getModel().getRowCount() - 2) {
                resField.setText("no such entry in user tlk table");
                posLabel.setText("no such entry : " + position);
                textArea.setText("");
            } else {
                updateModel.setEnabled(true);
                resField.setText(usertlkedit.tlkTable.getModel().getValueAt(p, 1).toString());
                String tlkString = usertlkedit.tlkTable.getModel().getValueAt(p, 2).toString();
                textArea.setText(tlkString);
                message(tlkString, position);
                textArea.setCaretPosition(0);
                posLabel.setText("User StrRef " + this.position + " ( " + p + " )");
                updateButton.setText("update " + usertlkedit.getFile().getName());
            }
        }
    }
    
    protected void message(String tlkString, int position){
        messageSupport.fireMessage(MessageFormat.format("[StrRef {0}] {1}", position, tlkString));
    }

    private boolean isUserEntry(int strref) {
        return (strref & TlkLookup.USERTLKOFFSET) > 0;
    }

    public void setTlkEdit(TlkEdit ed) {
        tlkedit = ed;
        setNoValue();
    }

    public void setUserTlkEdit(TlkEdit ed) {
        usertlkedit = ed;
        setNoValue();
    }

    @Override
    public void removeMessageListener(MessageListener l) {
        messageSupport.removeMessageListener(l);
    }

    @Override
    public MessageListener[] getMessageListeners() {
        return messageSupport.getMessageListeners();
    }

    @Override
    public void addMessageListener(MessageListener l) {
        messageSupport.addMessageListener(l);
    }
}
