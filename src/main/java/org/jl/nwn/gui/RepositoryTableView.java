/*
 * RepositoryTableView.java
 *
 * Created on 01.09.2007, 16:46:11
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

/**
 *
 * @author ich
 */
public class RepositoryTableView {

    protected JXTable table;
    protected PatternFilter filter;
    protected NwnRepository rep;
    protected RepositoryTableModel model;

    public RepositoryTableView() {
        model = new RepositoryTableModel();
        table = new JXTable(model);
        filter = new PatternFilter();
        filter.setColumnIndex(0);
        table.setFilters(new FilterPipeline(filter));
    }

    public void setRepository(NwnRepository rep){
        this.rep = rep;
        model.setRepository(rep);
    }

    public NwnRepository getRepository(){
        return rep;
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
        List<ResourceID> r = new ArrayList<ResourceID>();
        int[] s = table.getSelectedRows();
        RepositoryTableModel model = (RepositoryTableModel) table.getModel();
        for (int i : s){
            r.add(model.getResourceID(table.convertRowIndexToModel(i)));
        }
        return r;
    }

    public static class RepositoryTableModel extends AbstractTableModel {

        protected NwnRepository repository;
        protected List<ResourceID> resources = new ArrayList<ResourceID>();

        public RepositoryTableModel(){
        }

        public RepositoryTableModel( NwnRepository rep ){
            this.repository = rep;
            init();
        }

        public void clear(){
            repository = null;
            resources = new ArrayList<ResourceID>();
            fireTableDataChanged();
        }

        private void init(){
            resources = new ArrayList<ResourceID>();
            for (ResourceID id : repository){
                resources.add(id);
            }
        }

        public ResourceID getResourceID(int index){
            return resources.get(index);
        }

        public void setRepository(NwnRepository rep){
            this.repository = rep;
            init();
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
        public Object getValueAt(int arg0, int arg1) {
            ResourceID id = resources.get(arg0);
            switch (arg1) {
            case 0:
                return id.getNameExt();
            case 1:
                return id.getName();
            case 2:
                return id.getType();
            case 3:
                return id.getExtension();
            case 4:
                return repository.getResourceSize(id);
            case 5:
                return repository.getResourceLocation(id);
            default:
                return "<what's this?!?>";
            }
        }



        @Override
        public Class<?> getColumnClass(int arg0) {
            switch (arg0) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Short.class;
            case 3:
                return String.class;
            case 4:
                return Long.class;
            case 5:
                return File.class;
            default:
                return String.class;
            }
        }

        @Override
        public String getColumnName(int arg0) {
            switch (arg0) {
            case 0:
                return "Full Name";
            case 1:
                return "Name";
            case 2:
                return "Type";
            case 3:
                return "Extension";
            case 4:
                return "Size";
            case 5:
                return "Location";
            default:
                return "foo";
            }
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }
}
