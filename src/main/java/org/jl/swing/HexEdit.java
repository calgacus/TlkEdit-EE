/*
 * JHexEdit.java
 *
 * Created on 9. November 2005, 11:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jl.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class HexEdit extends JPanel{
    
    public JTable table;
    public JScrollPane sPane;
    protected Font font = Font.decode("Monospaced");
    ByteBuffer buffer = ByteBuffer.wrap("hello world !".getBytes());
    
    AbstractTableModel model = new AbstractTableModel(){
        byte[] bytes16 = new byte[16];
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            int bufferPos = rowIndex * 16 + (columnIndex-1);
            if ( columnIndex == 0 )
                return Integer.toHexString(rowIndex * 16);
            if ( columnIndex == 17 ){
                buffer.position(rowIndex * 16);
                int len = Math.min(16, buffer.capacity()-buffer.position() );
                buffer.get(bytes16, 0, len );
                StringBuilder sb = new StringBuilder();
                for ( int i = 0; i < len; i++ ){
                    char c = (char) bytes16[i];
                    sb.append( Character.isWhitespace(c)? ' ' : font.canDisplay(c)? c : ' ' );
                }
                return sb.toString();
                /*
                String s;
                try {
                    s = new String(bytes16, 0, len, "ASCII");
                    return s;
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
                return null;
                 */
            }
            if ( bufferPos >= buffer.capacity() )
                return null;
            buffer.position(bufferPos);
            int b = buffer.get();
            b = b < 0 ? b + 256 : b;
            return b < 16 ? "0" + Integer.toHexString(b) : Integer.toHexString(b);
        }
        
        public int getRowCount() {
            int r = (int) Math.ceil(buffer.capacity() / 16.0);
            return r;
        }
        
        public int getColumnCount() {
            return 18;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0 : return "Position";
                case 17 : return "ASCII";
                default : return Integer.toHexString(column-1);
            }
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            int bufferPos = rowIndex * 16 + (columnIndex-1);
            int value = Integer.parseInt(aValue.toString(), 16);
            if ( value > -1 && value < 256 ){
                byte b = (byte)(value > 127? value - 256 : value);
                buffer.position(bufferPos);
                buffer.put(b);
                fireTableRowsUpdated(rowIndex, rowIndex);
            }
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex > 0 && columnIndex < 17) &&
                    rowIndex * 16 + columnIndex < buffer.capacity();
        }
        
    };
    
    JTextField textField = new JTextField(2);
    DefaultCellEditor byteEd = new DefaultCellEditor(textField){
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            java.awt.Component retValue;
            retValue = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            textField.selectAll();
            return retValue;
        }
    };
    
    /** Creates a new instance of HexEdit */
    public HexEdit() {
        table = new JTable();
        table.setModel(model);
        Font fntMono = Font.decode("Monospaced");
        table.setFont(fntMono);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setSurrendersFocusOnKeystroke(true);
        table.setGridColor(table.getBackground());
        int width = 30;
        for ( int i = 1; i < 17; i++ ){
            TableColumn c = table.getColumnModel().getColumn(i);
            c.setMaxWidth(width);
            c.setPreferredWidth(width);
        }
        table.setDefaultEditor(Object.class, byteEd);
        TableColumn c0 = table.getColumnModel().getColumn(0);        
        c0.setPreferredWidth(90);
        c0.setMaxWidth(90);
        TableColumn stringColumn = table.getColumnModel().getColumn(17);
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setFont(fntMono);
        stringColumn.setCellRenderer(renderer);
        table.setPreferredScrollableViewportSize(new Dimension(700, 300));
        sPane = new JScrollPane(table);
        add(sPane);        
    }
    
    public void setBuffer(ByteBuffer b){
        buffer = b;
        model.fireTableDataChanged();
    }
    
    public static void main(String ... args) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(args[0], "rw");
        System.out.println(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        ByteBuffer b = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
        HexEdit hex = new HexEdit();
        hex.setBuffer(b);
        JFrame f = new JFrame("test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(hex.sPane, BorderLayout.CENTER);
        f.pack();
        f.setVisible(true);
        //raf.close();
        
        //String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        //System.out.println(Arrays.asList(fonts));
    }
    
}
