/*
 * TwoDaCompare.java
 *
 * Created on 28.08.2007, 08:25:49
 */

package org.jl.nwn.twoDa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jl.nwn.Version;

/**
 *
 */
public class TwoDaCompare {

    public static final int UNCHANGED = 0;
    public static final int CHANGED = 1;
    public static final int ROWNEWINTABLEA = 2;
    public static final int COLUMNNEWINTABLEA = 3;

    TableModel tableA;
    TableModel tableB;

    protected CompareFilter filter = null;
    protected Highlighter highlighter = null;

    public TwoDaCompare(TableModel a, TableModel b) {
        this.tableA = a;
        this.tableB = b;
    }

    public void setComparedModel(TableModel tableB) {
        this.tableB = tableB;
        getFilter().setComparedModel(tableB);
    }

    public int changeStatus(int row, int column) {
        if (row >= tableB.getRowCount()) {
            return ROWNEWINTABLEA;
        }
        //String cName = tableA.getColumnHeader(column);
        if (column >= tableB.getColumnCount()) {
            return COLUMNNEWINTABLEA;
        }
        return tableA.getValueAt(row, column).equals(tableB.getValueAt(row, column)) ? UNCHANGED : CHANGED;
    }

    public Highlighter getHighlighter(final JXTable table) {
        if (highlighter != null) {
            return highlighter;
        } else {
            return highlighter = new AbstractHighlighter() {

                @Override
                protected Component doHighlight(Component component, ComponentAdapter adapter) {
                    switch (changeStatus(table.convertRowIndexToModel(adapter.row), adapter.column)) {
                        case UNCHANGED:
                            break;
                        case CHANGED:
                            component.setBackground(Color.GREEN);
                            break;
                        default:
                            component.setBackground(Color.CYAN);
                    }
                    return component;
                }
            };
        }
    }

    public CompareFilter getFilter() {
        return filter == null ? filter = new CompareFilter(tableB) : filter;
    }

    public static class CompareFilter extends Filter {

        protected List<Integer> rows = new ArrayList<Integer>();
        protected TableModel tableB = null;
        protected boolean enabled = true;

        public CompareFilter(TableModel tableB) {
            this.tableB = tableB;
            tableB.addTableModelListener(new TableModelListener() {

                @Override
                public void tableChanged(TableModelEvent e) {
                    if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                        filter();
                        fireFilterChanged();
                    } else {
                        for (int row = e.getFirstRow(), n = e.getLastRow() + 1; row < n; row++) {
                            if (showRow(row) ^ rows.contains(row)) {
                                filter();
                                fireFilterChanged();
                                break;
                            }
                        }
                    }
                }
            });
        }

        public void setComparedModel(TableModel tableB) {
            this.tableB = tableB;
            if (enabled) {
                filter();
                fireFilterChanged();
            }
        }

        protected int changeStatus(int row, int column) {
            if (row >= tableB.getRowCount()) {
                return ROWNEWINTABLEA;
            }
            //String cName = tableA.getColumnHeader(column);
            if (column >= tableB.getColumnCount()) {
                return COLUMNNEWINTABLEA;
            }
            return adapter.getValueAt(row, column).equals(tableB.getValueAt(row, column)) ? UNCHANGED : CHANGED;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            if (this.enabled != enabled) {
                filter();
                fireFilterChanged();
            }
            this.enabled = enabled;
        }

        protected boolean showRow(int row) {
            for (int c = 0, n = adapter.getColumnCount(); c < n; c++) {
                if (changeStatus(row, c) == CHANGED) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void filter() {
            if (!enabled) {
                return;
            }
            rows.clear();
            for (int row = 0, n = adapter.getRowCount(); row < n; row++) {
                if (showRow(row)) {
                    rows.add(row);
                }
            }
        }

        @Override
        public int getSize() {
            return enabled ? rows.size() : adapter.getRowCount();
        }

        @Override
        protected void init() {
            if (rows == null) {
                rows = new ArrayList<Integer>();
            } else {
                rows.clear();
            }
        }

        @Override
        protected int mapTowardModel(int row) {
            return enabled ? rows.get(row) : row;
        }

        @Override
        protected void reset() {
            rows.clear();
        }
    }

    public static class TwoDaCompareUI {

        JXTable jxTable;

        JXPanel panel;

        TwoDaCompare compare;

        public TwoDaCompareUI(JXTable jxTable, TableModel tableB) {
            this.jxTable = jxTable;
            compare = new TwoDaCompare(jxTable.getModel(), tableB);
            panel = new JXPanel();
            panel.setLayout(new GridLayout(0, 1, 0, 8));
            final JToggleButton tb = new JToggleButton();
            tb.setText("filter rows");
            tb.setSelected(true);
            tb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    setFilterEnabled(tb.isSelected());
                }
            });

            final JLabel originalValue = new JLabel("<original value>");
            ListSelectionListener lsl = new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent arg0) {
                    int row = TwoDaCompareUI.this.jxTable.getSelectedRow();
                    int col = TwoDaCompareUI.this.jxTable.getSelectedColumn();
                    if (row == -1 || col == -1) {
                        originalValue.setText("");
                    } else {
                        row = TwoDaCompareUI.this.jxTable.convertRowIndexToModel(row);
                        col = TwoDaCompareUI.this.jxTable.convertColumnIndexToModel(col);
                        if (col < compare.tableB.getColumnCount() && row < compare.tableB.getRowCount()) {
                            originalValue.setText(compare.tableB.getValueAt(row, col).toString());
                        } else {
                            originalValue.setText("<n/a>");
                        }
                    }
                }
            };
            jxTable.getSelectionModel().addListSelectionListener(lsl);
            jxTable.getColumnModel().getSelectionModel().addListSelectionListener(lsl);

            jxTable.addHighlighter(compare.getHighlighter(jxTable));
            jxTable.setFilters(new FilterPipeline(compare.getFilter()));

            Action actSelectCompareFile = new AbstractAction("select file") {
                JFileChooser fChooser = new JFileChooser();
                {
                    fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fChooser.setMultiSelectionEnabled(false);
                }

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (fChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                        try {
                            TwoDaTable t = new TwoDaTable(fChooser.getSelectedFile());
                            TableModel tableB = new TwoDaEdit.TwoDaTableModel(t);
                            setComparedModel(tableB);
                        } catch (IOException ioex) {
                            JOptionPane.showMessageDialog(panel, ioex);
                        } finally {
                        }
                    }
                }
            };
            panel.add(new JButton(actSelectCompareFile));
            panel.add(tb);
            panel.add(originalValue);
        }

        public JXPanel getUIPanel() {
            return panel;
        }

        public void setComparedModel(TableModel modelB) {
            compare.setComparedModel(modelB);
        }

        public void setFilterEnabled(boolean filter) {
            compare.getFilter().setEnabled(filter);
        }

        public boolean getFilterEnabled() {
            return compare.getFilter().isEnabled();
        }
    }

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame(args[0]);
        f.getContentPane().setLayout(new BorderLayout());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TwoDaEdit ed = new TwoDaEdit(new File(args[0]), Version.getDefaultVersion());

        if (args.length > 1) {
            TwoDaTable t2 = new TwoDaTable(new File(args[1]));
            TwoDaCompare.TwoDaCompareUI ui = new TwoDaCompare.TwoDaCompareUI(ed.table, new TwoDaEdit.TwoDaTableModel(t2));
            JXTaskPaneContainer tpc = new JXTaskPaneContainer();
            f.getContentPane().add(tpc, BorderLayout.WEST);
            JXTaskPane p = new JXTaskPane();
            p.setTitle("compare");
            p.add(ui.getUIPanel());
            tpc.add(p);
        }
        f.getContentPane().add(ed);
        f.pack();
        f.setVisible(true);
    }
}
