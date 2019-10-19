package org.jl.nwn.twoDa.cellEditors;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BitFlagEditor
        extends AbstractCellEditor
        implements TableCellEditor {

    private int value;
    private WeakReference<JTable> tableRef;
    private MyTextField valueField = new MyTextField();
    private JPanel panel = new JPanel(new GridLayout(0, 1));
    private JLabel label = new JLabel(){
        @Override
        public void requestFocus(){
            valueField.requestFocus();
        }
        @Override
        public boolean processKeyBinding(KeyStroke ks,
                KeyEvent e,
                int condition,
                boolean pressed){
            return valueField.processKeyBinding(ks,e,condition,pressed);
        }

    };

    private JDialog dialog;

    private String paddingString = "0x00";

    protected int[] flags;
    protected JCheckBox[] boxes;


    private static class MyTextField extends JTextField{
        @Override
        public boolean processKeyBinding(KeyStroke ks,
                KeyEvent e,
                int condition,
                boolean pressed){
            return super.processKeyBinding(ks,e,condition,pressed);
        }
    }

    private ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            update();
        }
    };

    {
        // initializer -------------------
        label.setLabelFor(valueField);
    }

    BitFlagEditor(){
        panel.add(valueField);
    }

    public BitFlagEditor( String[] labels, int[] flags ){
        this();
        setup( labels, flags );
    }

    private void setup( String[] labels, int[] flags ){
        panel.removeAll();

        panel.setLayout(new GridLayout(Math.min(8,labels.length+1),0));

        panel.add( valueField );
        boxes = new JCheckBox[ labels.length ];
        this.flags = flags;

        for ( int i = 0; i < boxes.length; i++ ){
            boxes[i] = new JCheckBox( labels[i] );
            boxes[i].addActionListener(al);
            panel.add(boxes[i]);
        }
        int max = 0;
        for ( int i = 0; i < flags.length; i++ )
            if ( max < flags[i] ) max = flags[i];
        paddingString = "0x0";
        while ( ( max = max / 16 ) > 0 )
            paddingString += "0";
    }

    private void update() {
        value = 0;
        for (int i = 0; i < boxes.length; i++)
            value = value | (boxes[i].isSelected() ? flags[i] : 0);
        valueField.setText(hex(value));
    }

    // lazy initialization so that we can get a proper JFrame parent for the popup
    private void init( JTable table ){
        dialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(table));

        final HashSet<AWTKeyStroke> s1 = new HashSet<>();
        s1.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0, false));
        s1.addAll(
                dialog.getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        dialog.setFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                s1);
        final HashSet<AWTKeyStroke> s2 = new HashSet<>();
        s2.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP, 0, false));
        s2.addAll(
                dialog.getFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        dialog.setFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                s2);
        dialog.getRootPane().getActionMap().put( "cancel", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e){ cancelCellEditing(); } } );
        dialog.getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "cancel" );
        dialog.getRootPane().getActionMap().put( "endEdit", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e){ stopCellEditing(); } } );
        dialog.getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), "endEdit" );
        dialog.setUndecorated(true);
        //dialog.setAlwaysOnTop(true);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        if ( dialog == null )
            init(table);
        this.tableRef = new WeakReference<>(table);
        this.value = 0;
        try {
            String s = value.toString();
            if ( s.indexOf( 'x' ) == -1 ) this.value = Integer.parseInt( s, 16);
            else this.value = Integer.parseInt( s.substring(2), 16);
            value = hex(this.value);
        } catch (Exception ex) {
        }
        for (int i = 0; i < boxes.length; i++)
            boxes[i].setSelected((this.value & flags[i]) == flags[i]);
        this.valueField.setText(value.toString());
        Rectangle r = table.getCellRect(row, column, false);
        Point p = table.getLocationOnScreen();
        //System.out.println(r.x + ", " + r.y);
        dialog.setSize(Math.max( r.width, dialog.getPreferredSize().width ), dialog.getSize().height);
        label.setText(value.toString());
        valueField.selectAll();
        valueField.requestFocusInWindow();
        show(p.x + r.x, p.y + r.y);
        return label;
    }

    private void show( int x, int y ){
        dialog.getContentPane().add(panel);
        dialog.setLocation(x, y);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void hide(){
        dialog.getContentPane().removeAll();
        dialog.setVisible(false);
        dialog.dispose();
    }

    private String hex(int i) {
        String h = Integer.toHexString(i);
        //return "0x00".substring(0, 4 - h.length()) + h;
        return paddingString.substring(0, paddingString.length() - h.length()) + h;
    }

    @Override
    public boolean stopCellEditing() {
        if ( super.stopCellEditing() ){
            //dialog.setVisible(false);
            hide();
            JTable table = tableRef.get();
            if ( table != null )
                table.requestFocusInWindow();
            return true;
        } else
            return false;
    }

    @Override
    public void cancelCellEditing(){
        hide();
        super.cancelCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        return valueField.getText();
    }

    public BitFlagEditor( Element n ){
        this();
        NodeList flags = n.getElementsByTagName("flag");
        int[] bitflags = new int[ flags.getLength() ];
        String[] labels = new String[ flags.getLength() ];
        for ( int entryNum = 0; entryNum < flags.getLength(); entryNum++ ){
            Element entryNode = (Element) flags.item( entryNum );
            bitflags[ entryNum ] = Integer.parseInt( entryNode.getAttributes().getNamedItem( "hexvalue" ).getNodeValue(), 16 );
            labels[ entryNum ] = entryNode.getAttributes().getNamedItem( "label" ).getNodeValue();
        }
        setup( labels, bitflags );
    }

    public void dispose(){
        if ( dialog != null )
            dialog.dispose();
    }

    @Override protected void finalize() throws Throwable{
        if ( dialog != null )
            dialog.dispose();
    }
}
