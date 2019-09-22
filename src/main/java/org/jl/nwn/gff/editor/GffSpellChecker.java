package org.jl.nwn.gff.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.JSpellDialog;
import org.dts.spell.swing.JSpellPanel;
import org.dts.spell.swing.event.UIErrorMarkerListener;
import org.dts.spell.swing.event.UISpellCheckListener;
import org.dts.spell.swing.utils.SeparatorLineBorder;
import org.dts.spell.tokenizer.WordTokenizer;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.GffField;
import org.jl.nwn.gff.GffStruct;
import org.jl.nwn.spell.Dictionaries;

/**
 */
public class GffSpellChecker {

    GffTreeTableModel model;
    JSpellDialog d = null;
    JSpellPanel sPanel = null;
    SpellChecker checker = null;
    SpellDictionary dict = null;
    GffWordFinder finder = new GffWordFinder();
    UISpellCheckListener checkListener;
    UIErrorMarkerListener marker;
    boolean cancel = false;
    JFrame owner;
    JTextArea text = new JTextArea();
    NwnLanguage languageOverride = null;

    protected class GffWordFinder extends CharSequenceWordFinder{
        protected StringBuffer sb = new StringBuffer();
        CExoLocSubString substring;

        public GffWordFinder(){
            super("");
        }

        public void setSubstring( CExoLocSubString s ){
            sb.replace(0, sb.length(), s.string);
            this.substring = s;
            getTokenizer().setCharSequence(sb);
            init();
        }

        @Override public void replace(String newWord, Word currentWord){
            int start = currentWord.getStart();
            int end = currentWord.getEnd();
            sb.delete(start, end);
            updateCharSequence(
                    start, end,
                    WordTokenizer.DELETE_CHARS) ;
            if (newWord.length() > 0) {
                sb.insert(start, newWord);
                updateCharSequence(
                        start,
                        start + newWord.length(),
                        WordTokenizer.INSERT_CHARS) ;
            }
            model.setValueAt(sb.toString(), substring, 2);

        }

    }

    /** Creates a new instance of GffSpellChecker */
    public GffSpellChecker( GffTreeTableModel model ){
        this.model = model;
    }

    public GffSpellChecker( JFrame owner, GffTreeTableModel model ){
        this.owner = owner;
        this.model = model;
    }

    /**
     * Perform all spell checking with the given language, use <code>null</code>
     * to perform checking with the substring's language ( default ).
     * @param lang The language that the spell checker should use, or null
     */
    public void forceLanguage( NwnLanguage lang ){
        languageOverride = lang;
    }

    public void performChecking(){
        GffStruct topLevel = ((GffStruct)model.getRoot());
        Iterator<GffField> it = topLevel.getDFIterator();
        for ( GffField f = it.next(); !cancel && it.hasNext(); f = it.next() ){
            if ( f.getType() == Gff.CEXOLOCSTRING ){
                for ( CExoLocSubString substring : (GffCExoLocString)f ){
                    //CExoLocSubString substring = (CExoLocSubString) f;
                    if ( languageOverride != null )
                        dict = Dictionaries.forLanguage(languageOverride);
                    else
                        dict = Dictionaries.forLanguage(substring.language);
                    if ( dict != null ){
                        if ( d == null ) init();
                        finder.setSubstring(substring);
                        text.setText( substring.string );
                        checker.check(finder, marker);
                    }
                }
            }
        }
        cancel = false;
    }

    protected void init(){
        checker = new SpellChecker(dict);
        sPanel = new JSpellPanel();
        d = new JSpellDialog(owner, sPanel);
        //marker = new UIErrorMarkerListener(d);
        checkListener = new UISpellCheckListener(d);
        //marker.setTextComponent(cellEditor.getTextComponent());
        ActionListener al = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                cancel = true;
                d.cancel();
                //d.setVisible(false);
            }
        };
        sPanel.setCancelListener(al);

        //JPanel p = (JPanel)((BorderLayout)sPanel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        JPanel p = new JPanel();
        final JCheckBox cbCaseSensitive = new JCheckBox("Case Sensitive", checker.isCaseSensitive());
        cbCaseSensitive.setMnemonic('s');
        cbCaseSensitive.setDisplayedMnemonicIndex(5);
        cbCaseSensitive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checker.setCaseSensitive(cbCaseSensitive.isSelected());
            }
        });
        JButton bResetIgnore = new JButton("Reset Ignore List");
        bResetIgnore.addActionListener(
                EventHandler.create(ActionListener.class, checker, "resetIgnore")
                );
        bResetIgnore.setToolTipText("clear the list of words that this spell checker ignores");
        JButton bResetReplace = new JButton("Reset Replace List");
        bResetReplace.addActionListener(
                EventHandler.create(ActionListener.class, checker, "resetReplace")
                );
        bResetReplace.setToolTipText("clear the list of words that this spell checker replaces");
        p.setLayout( new BorderLayout() );
        Box buttons = new Box(BoxLayout.X_AXIS);
        buttons.add(cbCaseSensitive);
        buttons.add(bResetIgnore);
        buttons.add(bResetReplace);
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        text.setPreferredSize(new Dimension(400, 300));
        text.setEditable(false);
        p.add( buttons, BorderLayout.NORTH );
        p.add( new JScrollPane(text,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                ), BorderLayout.CENTER );
        marker = new UIErrorMarkerListener(d);
        marker.setTextComponent(text);
        p.setBorder(
                BorderFactory.createTitledBorder(
                SeparatorLineBorder.get(), "Spell Checker Options"));
        d.getContentPane().add(p, BorderLayout.SOUTH);
    }
}
