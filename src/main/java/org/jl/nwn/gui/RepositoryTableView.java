package org.jl.nwn.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;
import org.jl.nwn.resource.NwnRepository;
import org.jl.nwn.resource.ResourceID;

public class RepositoryTableView {

    private final JXTable table;
    private final PatternFilter filter;

    public RepositoryTableView() {
        table = new JXTable(new RepositoryTableModel());
        filter = new PatternFilter();
        filter.setColumnIndex(0);
        table.setFilters(new FilterPipeline(filter));
    }

    public void setRepository(NwnRepository rep){
        ((RepositoryTableModel)table.getModel()).setRepository(rep);
    }

    public NwnRepository getRepository(){
        return ((RepositoryTableModel)table.getModel()).repository;
    }

    public JXTable getViewComponent(){
        return table;
    }

    public void setFilterPattern(Pattern p){
        filter.setPattern(p);
    }

    public void clear(){
        ((RepositoryTableModel)table.getModel()).clear();
    }

    public List<ResourceID> getSelectedResources(){
        final List<ResourceID> r = new ArrayList<>();
        int[] s = table.getSelectedRows();
        RepositoryTableModel model = (RepositoryTableModel) table.getModel();
        for (int i : s){
            r.add(model.getResourceID(table.convertRowIndexToModel(i)));
        }
        return r;
    }

    public static class RepositoryTableModel extends AbstractTableModel {

        protected NwnRepository repository;
        protected List<ResourceID> resources = new ArrayList<>();

        public void clear(){
            repository = null;
            resources = new ArrayList<>();
            fireTableDataChanged();
        }

        public ResourceID getResourceID(int index){
            return resources.get(index);
        }

        public void setRepository(NwnRepository rep){
            repository = rep;
            resources = new ArrayList<>(rep.getResourceIDs());
            fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public int getRowCount() {
            return resources.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            final ResourceID id = resources.get(row);
            switch (column) {
            case 0: return id.getFileName();
            case 1: return id.getName();
            case 2: return id.getType();
            case 3: return id.getExtension();
            case 4: return repository.getResourceSize(id);
            case 5: return repository.getResourceLocation(id);
            default: return "<what's this?!?>";
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
            case 0: return String.class;
            case 1: return String.class;
            case 2: return Short.class;
            case 3: return String.class;
            case 4: return Long.class;
            case 5: return File.class;
            default: return String.class;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case 0: return "Full Name";
            case 1: return "Name";
            case 2: return "Type";
            case 3: return "Extension";
            case 4: return "Size";
            case 5: return "Location";
            default: return "<unknown>";
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
