/*
 * RepositoryFCAccessory.java
 *
 * Created on 29.08.2007, 19:08:39
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jl.nwn.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXTable;
import org.jl.nwn.erf.ErfEdit;
import org.jl.nwn.resource.NwnRepository;
import org.jl.nwn.resource.Repositories;
import org.jl.nwn.resource.ResourceID;

/**
 *
 * @author ich
 */
public class RepositoryFCAccessory implements PropertyChangeListener {

    //protected RepositoryTreeView rview;
    protected RepositoryTableView rview;
    protected JPanel accPanel;
    protected JToolBar toolbar = new JToolBar();
    protected JButton actionButton;
    protected JScrollPane scroll;
    protected JTextField filterField;

    public RepositoryFCAccessory() {
        //rview = new RepositoryTreeView();
        rview = new RepositoryTableView();
        accPanel = new JPanel(new BorderLayout());
        actionButton = new JButton("---");
        actionButton.setEnabled(false);
        final JXTable tt = rview.getViewComponent();
        tt.getColumnExt(5).setVisible(false);
        tt.getColumnExt(2).setVisible(false);
        tt.getColumnExt(1).setVisible(false);
        tt.getColumnExt(0).setMinWidth(150);
        scroll = new JScrollPane(tt);
        rview.getViewComponent().setColumnControlVisible(true);
        scroll.setMinimumSize(new Dimension(200, 300));

        toolbar.add(actionButton);
        toolbar.setFloatable(false);
        JLabel l = new JLabel("Filter : ");
        filterField = new JTextField("", 12);
        filterField.setEnabled(false);
        l.setLabelFor(filterField);
        toolbar.add(l);
        toolbar.add(filterField);

        filterField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                String s = filterField.getText();
                System.out.println("Repo..FCA...java changedUpdate "+s);
                //if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                if (s.length() > 0) {
                    try {
                        final Pattern p = Pattern.compile(s);
                        tt.setEnabled(false);
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    rview.setFilterPattern(p);
                                } finally {
                                    tt.setEnabled(true);
                                }
                            }
                        });
                    } catch (PatternSyntaxException pse) {
                        //JOptionPane.showMessageDialog(tf, pse);
                    }
                } else {
                    rview.setFilterPattern(null);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });

        actionButton.setVisible(false);
        accPanel.add(scroll, BorderLayout.CENTER);
        accPanel.add(toolbar, BorderLayout.SOUTH);
    }

    public List<ResourceID> getSelectedResources() {
        return rview.getSelectedResources();
    }

    public void setButtonAction(Action a) {
        actionButton.setAction(a);
        actionButton.setVisible(true);
    }

    public NwnRepository getRepository() {
        return rview.getRepository();
    }

    public JComponent getAccessoryComponent() {
        return accPanel;
    }

    public JXTable getView() {
        return rview.getViewComponent();
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        if (arg0.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            final File f = (File) arg0.getNewValue();
            if (f != null) {
                rview.getViewComponent().setEnabled(false);
                SwingUtilities.invokeLater(new Runnable() {
                    boolean enable = false;

                    @Override
                    public void run() {
                        try {
                            if (f == null) {
                                rview.clear();
                            } else if (ErfEdit.accept(f)) {
                                try {
                                    rview.setRepository(Repositories.getInstance().getErfRepository(f));
                                    enable = true;
                                } catch (IOException ioex) {
                                    ioex.printStackTrace();
                                }
                            } else if (f.getName().toLowerCase().endsWith(".zip")) {
                                try {
                                    rview.setRepository(Repositories.getInstance().getZipRepository(f));
                                    enable = true;
                                } catch (IOException ioex) {
                                    ioex.printStackTrace();
                                }
                            } else if (f.getName().toLowerCase().endsWith(".rep")) {
                                try {
                                    rview.setRepository(Repositories.getInstance().getChainRepository(f));
                                    enable = true;
                                } catch (IOException ioex) {
                                    ioex.printStackTrace();
                                }
                            } else {
                                rview.clear();
                            }
                        } finally {
                            rview.getViewComponent().setEnabled(enable);
                            actionButton.setEnabled(enable);
                            filterField.setEnabled(enable);
                        }
                    }
                });
            }
        }
    }

    public void resourceSelected(NwnRepository r, ResourceID id) {
    }

    public static void main(String... args) {
        File f = new File("/media/sdb6/spiele/Neverwinter Nights 2/");
        f = new File("/media/sdb7/Java/Netbeans/TlkEdit/distribution_files");
        JFileChooser fc = new JFileChooser(f);
        final RepositoryFCAccessory acc = new RepositoryFCAccessory();
        acc.setButtonAction(new AbstractAction("print ResourceIDs") {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Repo..FCA...java actionPerformed "+acc.getSelectedResources());
            }
        });
        fc.setAccessory(acc.accPanel);
        fc.addPropertyChangeListener(acc);
        fc.validate();
        fc.showOpenDialog(null);
    }
}
