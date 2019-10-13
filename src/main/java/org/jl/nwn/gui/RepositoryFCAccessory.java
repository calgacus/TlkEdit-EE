package org.jl.nwn.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Action;
import javax.swing.JButton;
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
 * Class, that shows table with contents of the ERF and MOD files in preview area
 * of the {@link JFileChooser}. Instance of this class must be registered in JFileChooser
 * using {@link JFileChooser#setAccessory} method and listen events from it.
 */
public final class RepositoryFCAccessory extends JPanel implements PropertyChangeListener {

    //private RepositoryTreeView rview = new RepositoryTreeView();
    /** Table with contents of the ERF/MOD file. */
    private RepositoryTableView rview = new RepositoryTableView();
    /** Button, that can be used to run some action on resource in the table. */
    private final JButton actionButton;
    /** Field with regular expression that used to filter contents of the table. */
    private final JTextField filterField;

    public RepositoryFCAccessory(Action action) {
        super(new BorderLayout());
        final JXTable tt = rview.getViewComponent();
        tt.getColumnExt(5).setVisible(false);
        tt.getColumnExt(2).setVisible(false);
        tt.getColumnExt(1).setVisible(false);
        tt.getColumnExt(0).setMinWidth(150);
        rview.getViewComponent().setColumnControlVisible(true);

        actionButton = new JButton(action);

        filterField = new JTextField("", 12);
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
                String s = filterField.getText();
                if (!s.isEmpty()) {
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
            public void insertUpdate(DocumentEvent event) {
                changedUpdate(event);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                changedUpdate(event);
            }
        });

        final JLabel filterLabel = new JLabel("Filter: ");
        filterLabel.setLabelFor(filterField);

        final JScrollPane scroll = new JScrollPane(tt);
        scroll.setMinimumSize(new Dimension(200, 300));

        final JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(actionButton);
        toolbar.add(filterLabel);
        toolbar.add(filterField);

        add(scroll, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);

        actionButton.setEnabled(false);
        filterField.setEnabled(false);
    }

    /**
     * Returns selected files in the repository, if in {@link JFileChooser} some
     * file selected and it is {@link NwnRepository repository}.
     *
     * @return List of selected resources or empty list is no file is selected or
     *         selected file is not repository
     */
    public List<ResourceID> getSelectedResources() {
        return rview.getSelectedResources();
    }

    /**
     * Returns selected file as repository.
     *
     * @return Repository if some file is selected in {@link JFileChooser} and
     *         it is a repository, {@code null} otherwise
     */
    public NwnRepository getRepository() {
        return rview.getRepository();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        // When currently selected file in JFileChooser is changed update our table
        if (event.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            rview.getViewComponent().setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                boolean enable = false;
                try {
                    final File f = (File) event.getNewValue();
                    if (f == null) {
                        rview.clear();
                    } else
                    if (ErfEdit.accept(f)) {
                        rview.setRepository(Repositories.getInstance().getErfRepository(f));
                        enable = true;
                    } else
                    if (f.getName().toLowerCase().endsWith(".zip")) {
                        rview.setRepository(Repositories.getInstance().getZipRepository(f));
                        enable = true;
                    } else
                    if (f.getName().toLowerCase().endsWith(".rep")) {
                        rview.setRepository(Repositories.getInstance().getChainRepository(f));
                        enable = true;
                    } else {
                        rview.clear();
                    }
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                } finally {
                    rview.getViewComponent().setEnabled(enable);
                    actionButton.setEnabled(enable);
                    filterField.setEnabled(enable);
                }
            });
        }
    }
}
