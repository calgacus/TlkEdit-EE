/*
 * Created on 28.03.2004
 *
 */
package org.jl.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Dialog for searching/replacing text in a JTextComponent.
 */
public abstract class SearchAndReplaceDialog extends JDialog{
    
    //protected SimpleMatcher matcher;
    protected Matcher matcher;
    protected Pattern searchPattern;
    
    protected JLabel statusLabel = new JLabel("[]");
    // the string on which the matcher operates
    private String string = null;
    
    // true if the matcher etc. should be initialized again
    protected boolean invalidState = true;
    private StringBuffer sb = new StringBuffer();
    
    // first search operation will call matcher.find( searchStartPos )
    protected int searchStartPos = 0;
    protected boolean firstSearch = false;
    
    protected int selectionStart;
    protected int selectionEnd;
    
    // used to keep track of the difference between text with&without replacements
    protected int deltaReplacement = 0;
    
    protected JCheckBox cbRegExp = new JCheckBox( "regular expression", false );
    protected JCheckBox cbIgnoreCase = new JCheckBox( "ignore case", true );
    protected JCheckBox cbIncrementalSearch = new JCheckBox( "incremental search", true );
    
    protected JTextField tfSearchString = new JTextField(40);
    protected JTextField tfReplaceString = new JTextField(40);
    
    protected ButtonGroup bgSearchIn = new ButtonGroup();
    protected JRadioButton rbSearchSelection = new JRadioButton( "selection", false );
    protected JRadioButton rbSearchAll = new JRadioButton( "whole file", true );
    protected Box boxSelSettings = new Box( BoxLayout.Y_AXIS );
    
    protected JPanel bottomPanel;
    
    protected JCheckBox cbKeepDialog = new JCheckBox( "keep dialog", false );
    
    protected JButton btnSearch;
    protected JButton btnReplace;
    protected JButton btnReplaceAll;
    
    Action aSearch = new AbstractAction(){
        @Override
        public void actionPerformed( ActionEvent e ){
            doSearch();
            if ( !cbKeepDialog.isSelected() ){
                setVisible(false);
            }
            //btnSearch.requestFocusInWindow();
        }
    };
    
    Action aReplace = new AbstractAction(){
        @Override
        public void actionPerformed( ActionEvent e ){
            if ( haveMatch() ){
                replaceMatch();
                doSearch();
                if ( !cbKeepDialog.isSelected() )
                    setVisible(false);
            }
        }
    };
    
    Action aReplaceAll = new AbstractAction(){
        @Override
        public void actionPerformed( ActionEvent e ){
            replaceAll();
            if ( !cbKeepDialog.isSelected() )
                setVisible(false);
        }
    };
    
    // initializer
    {
        bgSearchIn.add( rbSearchAll );
        bgSearchIn.add( rbSearchSelection );
    }
    
    public SearchAndReplaceDialog( JFrame owner ){
        super(owner);
        //setModal(true);
        Action aHide = new AbstractAction(){
            @Override
            public void actionPerformed( ActionEvent e ){
                setVisible(false);
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "hide" );
        getRootPane().getActionMap().put( "hide", aHide );
        
        setDefaultCloseOperation( HIDE_ON_CLOSE );
        setTitle( "Search And Replace" );
        Box boxMain = new Box( BoxLayout.Y_AXIS );
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add( boxMain, BorderLayout.CENTER );
        
        Box boxInputFields = new Box( BoxLayout.Y_AXIS );
        JLabel fLabel = new JLabel();
        I18nUtil.setText( fLabel, "&Find" );
        boxInputFields.add( fLabel );
        fLabel.setLabelFor( tfSearchString );
        boxInputFields.add( tfSearchString );
        JLabel rLabel = new JLabel();
        I18nUtil.setText( rLabel, "R&eplace With" );
        rLabel.setLabelFor( tfReplaceString );
        boxInputFields.add( rLabel );
        boxInputFields.add( tfReplaceString );
        
        //Box boxActionButtons = new Box( BoxLayout.Y_AXIS );
        JPanel boxActionButtons = new JPanel(new GridLayout(0,1,0,5));
        btnSearch = new JButton(aSearch);
        btnSearch.setDefaultCapable( true );
        getRootPane().setDefaultButton(btnSearch);
        boxActionButtons.add( btnSearch );
        boxActionButtons.add( btnReplace = new JButton(aReplace) );
        boxActionButtons.add( btnReplaceAll = new JButton(aReplaceAll) );
        
        JPanel topPanel = new JPanel();
        topPanel.add( boxInputFields );
        topPanel.add( boxActionButtons );
        
        Box boxSettings = new Box( BoxLayout.Y_AXIS );
        //boxSettings.add( new JLabel( "settings : ") );
        boxSettings.setAlignmentY(Box.TOP_ALIGNMENT);
        boxSettings.setBorder( new TitledBorder("Options") );
        boxSettings.add( cbRegExp );
        boxSettings.add( cbIgnoreCase );
        
        //boxSelSettings.add( new JLabel("search in : ") );
        boxSelSettings.setAlignmentY(Box.TOP_ALIGNMENT);
        boxSelSettings.setBorder( new TitledBorder("Scope") );
        boxSelSettings.add( rbSearchAll );
        boxSelSettings.add( rbSearchSelection );
        
        bottomPanel = new JPanel( new FlowLayout(FlowLayout.LEADING) );
        bottomPanel.add( boxSelSettings );
        bottomPanel.add( boxSettings );
        
        rbSearchSelection.addChangeListener( new ChangeListener(){
            @Override
            public void stateChanged( ChangeEvent e ){
                btnSearch.setEnabled( !rbSearchSelection.isSelected() );
                btnReplace.setEnabled( !rbSearchSelection.isSelected() );
            }
        } );
        
        boxMain.add( topPanel );
        boxMain.add( bottomPanel );
        
        ChangeListener cl = new ChangeListener(){
            @Override
            public void stateChanged( ChangeEvent e ){
                invalidState = true;
                //invalidate();//init();
            }
        };
        cbIgnoreCase.addChangeListener(cl);
        cbRegExp.addChangeListener(cl);
        
        KeyListener kl = new KeyAdapter(){
            @Override
            public void keyPressed( KeyEvent e ){
                invalidState = true;
                //invalidate();//init();
            }
        };
        tfReplaceString.addKeyListener(kl);
        
        DocumentListener dl = new DocumentListener(){
            @Override
            public void changedUpdate( DocumentEvent de ){}
            @Override
            public void removeUpdate( DocumentEvent de ){}
            @Override
            public void insertUpdate( DocumentEvent de ){
                if ( !cbIncrementalSearch.isSelected() ||
                        rbSearchSelection.isSelected() )
                    return;
                if ( matcher == null )
                    invalidate();
                else
                    changePattern();
                doSearch();
            }
        };
        tfSearchString.getDocument().addDocumentListener(dl);
        
        //bottomPanel.add( cbKeepDialog );
        JPanel bottom = new JPanel( new FlowLayout(FlowLayout.LEFT) );
        bottom.add( cbKeepDialog );
        bottom.add( cbIncrementalSearch );
        boxMain.add( bottom );
        
        // setup hot keys & tooltips
        I18nUtil.setText( cbRegExp, "Regular E&xpression" );
        I18nUtil.setText( cbIgnoreCase, "Ignore &Case" );
        I18nUtil.setText( cbKeepDialog, "&Keep Dialog" );
        I18nUtil.setText( cbIncrementalSearch, "&Incremental Search" );
        I18nUtil.setText( rbSearchAll, "A&ll" );
        I18nUtil.setText( rbSearchSelection, "&Selection" );
        I18nUtil.setText( btnSearch, "Fi&nd" );
        I18nUtil.setText( btnReplace, "&Replace && Find" );
        I18nUtil.setText( btnReplaceAll, "Replace &All" );
        
        getContentPane().add(statusLabel, BorderLayout.SOUTH);
        //pack();
        setResizable(false);
    }
    
    @Override public void setVisible(boolean v){
        if ( v ){
            if ( !isDisplayable() )
                pack();
            super.setVisible(true);
        }
        else {
            super.setVisible(false);
            dispose();
        }
    }
    
    public SearchAndReplaceDialog(){
        this(null);
    }
    
    public void init(){
        //System.out.println( "SnD.init()" );
        invalidState = false;
        firstSearch = true;
        sb.delete( 0, sb.length() );
        deltaReplacement = 0;
        string = getString();
        searchPattern = newPattern();
        if ( searchPattern != null ){
            matcher = searchPattern.matcher(string);
        } else
            invalidState = true;
        searchStartPos = 0;
    }
    
    protected Pattern newPattern(){
        int flags = 0;
        if ( cbIgnoreCase.isSelected() )
            flags |= Pattern.CASE_INSENSITIVE;
        if ( !cbRegExp.isSelected() )
            flags |= Pattern.LITERAL;
        Pattern p = null;
        try{
            p = Pattern.compile(tfSearchString.getText(), flags);
        } catch ( PatternSyntaxException pse ){
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Invalid RegExp : " + pse.getDescription());
        }
        return p;
    }
    
    
    /**
     * try to replace currently used pattern with content of search text field.
     */
    protected void changePattern(){
        try {
            if (matcher != null && matcher.start() > -1 ){
                searchStartPos = matcher.start();
                firstSearch = true;
            }
        } catch ( IllegalStateException ise ){
            //thrown by matcher.start() if there was no match
        }
        searchPattern = newPattern();
        if ( searchPattern == null )
            invalidate();
        else
            matcher.usePattern(searchPattern);
    }
    
    /**
     * should be called by the editor/client when the value of string has changed,
     * i.e. when this object must reread the string and reinitialize the search pattern & matcher.
     * */
    @Override
    public void invalidate(){
        invalidState = true;
        btnReplace.setEnabled(false);
            /*
                if ( !this.hasFocus() ){
                        invalidState = true;
                        btnReplace.setEnabled(false);
                        //btnReplaceAll.setEnabled(false);
                }
             */
        //if (invalidState) System.out.println( "state invalid" );
    }
    
    /**
     * test whether the matcher has found a match
     * */
    public boolean haveMatch(){
        if ( matcher == null )
            return false;
        try {
            matcher.end();
        } catch ( IllegalStateException ise ){
            return false;
        }
        return true;
    }
    
    /**
     return a replacement string that will work with the current matcher
     */
    protected String getReplacement(){
        String replacement = tfReplaceString.getText();
        if ( !cbRegExp.isSelected() )
            replacement = Matcher.quoteReplacement(replacement);
        return replacement;
    }
    
    public final void replaceMatch(){
        matcher.appendReplacement( sb, getReplacement() );
        updateString( sb.toString() + string.substring( matcher.end() ) );
        deltaReplacement = sb.length() - matcher.end();
        //System.out.println( deltaReplacement );
    }
    
    /**
     * reset matcher and replace all occurences in selection or in whole file
     * */
    public void replaceAll(){
        init();
        searchStartPos = 0;
        boolean update = false;
        while ( search() ){
            matcher.appendReplacement( sb, getReplacement() );
            update = true;
        }
        if ( update )
            updateString( matcher.appendTail( sb ).toString() );
        invalidState = true;
    }
    
    private void selectText(){
        selectText( matcher.start() + deltaReplacement, matcher.end() + deltaReplacement );
    }
    
    protected abstract void selectText( int start, int end );
    
    public abstract String getString();
    
    public abstract void updateString( String s );
    
    public final void doSearch(){
        if ( invalidState ){
            searchPattern = newPattern();
            if ( searchPattern == null )
                return;
        }
        boolean matchFound = search();
        if ( !matchFound ){
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("No match for : " + tfSearchString.getText());
            Toolkit.getDefaultToolkit().beep();
            //JOptionPane.showMessageDialog( SearchAndReplaceDialog.this, "search string not found" );
        } else {
            try{
                //System.out.println( "match at : " + matcher.start() );
                selectText();
                btnReplace.setEnabled(true);
                btnReplaceAll.setEnabled(true);
            } finally {
                SwingUtilities.invokeLater(GrabFocus);
                SwingUtilities.invokeLater(SetOK);
            }
        }
    }
    
    protected Runnable GrabFocus = new Runnable() {
        @Override
        public void run() {
            if ( isVisible() ){
                requestFocus();
                tfSearchString.requestFocusInWindow();
            }
        }
    };
    
    protected Runnable SetOK = new Runnable() {
        @Override
        public void run() {
            invalidState = false;
            //System.out.println("SetOK " + invalidState);
        }
    };
    
    public boolean search(){
        //System.out.println("SearchAndReplaceDialog.search() " + invalidState);
        boolean stringFound = false;
        if ( invalidState )
            init();
        //if ( firstSearch ) System.out.println( "start search at : " + searchStartPos );
        stringFound = firstSearch? matcher.find( searchStartPos ) : matcher.find();
        firstSearch = false;
        //System.out.println( stringFound? "match at : "+matcher.start() : "not found" );
        return stringFound && matchIsInSelection();
    }
    
    /**
     * test whether the current match is in the selection
     * @return true if the match is whithin the selection OR if there is no selection ( in that case the whole string is treated as selected ).
     * false else
     * @throws IllegalStateException if no match has been found or search was not called before
     * */
    protected boolean matchIsInSelection() throws IllegalStateException{
        if ( rbSearchSelection.isEnabled() && rbSearchSelection.isSelected() )
            return matcher.start() >= selectionStart && matcher.end() < selectionEnd;
        else
            return true;
    }
    
    public static void main(String[] args) throws IOException{
        JFrame f = new JFrame();
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        final JTextPane t = new JTextPane();
        final SearchAndReplaceDialog sar = new SearchAndReplaceDialog(){
            @Override
            public String getString(){
                return t.getText();
            }
            @Override
            public void selectText( int a, int b ){
                t.select( a, b );
            }
            @Override
            public void updateString( String s ){
                int p = t.getCaretPosition();
                t.setText( s );
                //t.setCaretPosition( p );
            }
            @Override
            public void init(){
                super.init();
                super.searchStartPos = t.getCaretPosition();
            }
        };
        t.addFocusListener( new FocusAdapter(){
            @Override
            public void focusGained(FocusEvent e){
                sar.invalidate();
            }
        } );
        
        f.getContentPane().add( new JScrollPane(t) );
        f.pack();
        f.setSize( 700, 300 );
        f.setVisible(true);
        sar.setVisible(true);
    }
    
}
