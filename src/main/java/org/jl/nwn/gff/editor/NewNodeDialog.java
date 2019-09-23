package org.jl.nwn.gff.editor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.event.MessageSourceSupport;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.GffField;
import org.jl.nwn.gff.GffList;
import org.jl.nwn.gff.GffStruct;

class NewNodeDialog {

    GffEditX gffEd;
    MessageSourceSupport msgSup;
    JXTreeTable treeTable;
    GffTreeTableModel model;
    JOptionPane op;
    JDialog newNodeDialog;

    JPanel newNodeInListPanel = new JPanel();
    JTextField txtIndex = new JTextField(16);
    JTextField txtLabel = new JTextField(16);
    final JComboBox<String> cbType = new JComboBox<>(Gff.TYPENAMES);
    JTextField txtStructID = new JTextField(16);
    JSpinner spIndex = new JSpinner( new SpinnerNumberModel() );
    String newNodeOK = "OK";
    JPanel pNewSubstring;
    final JComboBox<NwnLanguage> cbLang = new JComboBox<>(NwnLanguage.LANGUAGES);
    final JComboBox<String> cbGender = new JComboBox<>(new String[]{ "masculine / neutral", "feminine" });

    /** Creates a new instance of NewNodeDialog */
    public NewNodeDialog( GffEditX gffEd, MessageSourceSupport msgSup ){
        this.gffEd = gffEd;
        this.model = gffEd.model;
        this.msgSup = msgSup;
        this.treeTable = gffEd.treeTable;

        JPanel newNodePanel = new JPanel(){
            final JLabel lLabel = new JLabel("Label");
            final JLabel lType = new JLabel( "Type" );
            final JLabel lStructID = new JLabel( "Struct ID" );
            final JLabel lIndex = new JLabel( "List index" );
            final Box centerBox = new Box(BoxLayout.Y_AXIS);
            {
                setLayout(new BorderLayout());
                lLabel.setLabelFor(txtLabel);
                lLabel.setDisplayedMnemonic('L');
                lType.setLabelFor(cbType);
                lType.setDisplayedMnemonic('T');
                lStructID.setLabelFor(txtStructID);
                lStructID.setDisplayedMnemonic('S');
                //lIndex.setLabelFor(txtIndex);
                lIndex.setLabelFor(spIndex);
                lStructID.setDisplayedMnemonic('I');

                JPanel p1 = new JPanel();
                GridLayout grid1 = new GridLayout(0,2);
                grid1.setHgap(10);
                grid1.setVgap(10);
                p1.setLayout(grid1);
                p1.add(lLabel);
                p1.add(txtLabel);
                p1.add(lType);
                p1.add(cbType);

                GridLayout grid2 = new GridLayout(0,2);
                grid2.setHgap(10);
                grid2.setVgap(10);
                newNodeInListPanel.setLayout(grid2);
                newNodeInListPanel.add(lStructID);
                newNodeInListPanel.add(txtStructID);
                newNodeInListPanel.add(lIndex);
                //newNodeInListPanel.add(txtIndex);
                newNodeInListPanel.add(spIndex);
                newNodeInListPanel.add(Box.createVerticalStrut(5));
                centerBox.add(newNodeInListPanel);

                centerBox.add(p1);
                add( centerBox, BorderLayout.CENTER );
                setVisible(true);
                //setModal(true);
            }
        };
        op = new JOptionPane(newNodePanel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_OPTION, null, new String[]{newNodeOK}, newNodeOK);
    }

    public void newNode(){
        if ( newNodeDialog == null )
            newNodeDialog = op.createDialog(treeTable, "new node");
        try{
            TreePath path = treeTable.getTreeSelectionModel().getSelectionPath();
            if ( path == null )
                return;
            GffField field = (GffField) path.getLastPathComponent();

            if ( field.getType() == Gff.CEXOLOCSTRING || field.getType() == GffCExoLocString.SUBSTRINGTYPE ){
                doNewSubstring();
                return;
            }

            GffStruct struct = null;
            GffList list = null;
            int insertionindex = 0;
            newNodeInListPanel.setVisible(false);
            if ( field.isDataField() ){
                struct = (GffStruct) field.getParent();
                insertionindex = struct.indexOf(field);
            } else {
                if ( field.getType() == Gff.STRUCT )
                    struct = (GffStruct) field;
                else {
                    list = (GffList) field;
                    txtIndex.setText(Integer.toString(list.getSize()));
                    if ( list.getSize() > 0 ){
                        txtStructID.setText(Integer.toString(list.get(0).getId()));
                    } else
                        txtStructID.setText("0");
                    ((SpinnerNumberModel)spIndex.getModel()).setMaximum( Integer.valueOf(list.getSize()) );
                    spIndex.getModel().setValue( Integer.valueOf(list.getSize()) );
                    newNodeInListPanel.setVisible(true);
                }
            }
            //newNodeDialog.validate();
            newNodeDialog.pack();
            newNodeDialog.setAlwaysOnTop(true);
            newNodeDialog.setVisible(true);
            GffField newField = null;
            if ( op.getValue() == newNodeOK ){
                byte type = (byte) cbType.getSelectedIndex();
                newField = GffField.createField(type);
                String label = txtLabel.getText();
                // TODO : parse label
                newField.setLabel(txtLabel.getText());
                if  ( struct != null && struct.getChild(label) != null ){
                    // duplicate label
                    msgSup.fireMessage("new node : cannot create field, a field with the same label already exists", Level.WARNING);
                    return;
                }
            } else
                return;
            if ( struct != null ){ // new node in struct
                model.insert( model.makePath(struct), newField, insertionindex );
            } else{ // new node in list
                GffStruct newStruct = new GffStruct(Integer.parseInt(txtStructID.getText()));
                if ( txtLabel.getText().length() > 0 )
                    newStruct.addChild(newField);
                //insertionindex = Integer.parseInt(txtIndex.getText());
                insertionindex = ((Number)spIndex.getValue()).intValue();
                model.insert(model.makePath(list), newStruct, insertionindex);
            }
        } finally {
            if ( newNodeDialog != null )
                newNodeDialog.dispose();
        }
    }

    public void doNewSubstring(){
        TreePath path = treeTable.getTreeSelectionModel().getSelectionPath();
        if ( path == null )
            return;
        GffField field = (GffField) path.getLastPathComponent();
        if ( field.getType() != Gff.CEXOLOCSTRING && field.getType() != GffCExoLocString.SUBSTRINGTYPE )
            return;
        GffCExoLocString cels = (GffCExoLocString) ((field.getType() == Gff.CEXOLOCSTRING)?field : field.getParent());
        if ( pNewSubstring == null ){
            pNewSubstring = new JPanel();
            pNewSubstring.add( cbLang );
            pNewSubstring.add( cbGender );
        }
        int r = JOptionPane.showConfirmDialog(
                treeTable,
                pNewSubstring,
                "Select Substring Language and Gender",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
                );
        if ( r == JOptionPane.OK_OPTION ){
            System.out.println("new substring ...");
            if ( cels.getSubstring( (NwnLanguage) cbLang.getSelectedItem(), cbGender.getSelectedIndex() ) != null )
                msgSup.fireMessage( "cannot add substring : substring exists" );
            else{
                CExoLocSubString sub = new CExoLocSubString("", (NwnLanguage)cbLang.getSelectedItem(), cbGender.getSelectedIndex() );
                model.insert(model.makePath(cels), sub, 0);
            }
        }
    }
}
