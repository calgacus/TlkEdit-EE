package org.jl.nwn.gui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jl.nwn.bif.BifRepository;
import org.jl.nwn.erf.ErfFile;
import org.jl.nwn.resource.NwnRepository;
import org.jl.nwn.resource.ResourceID;

public class RepositoryTreeView {

    private final Model model = new Model();
    private final JXTreeTable treeTable = new JXTreeTable(model);

    public RepositoryTreeView() {
        treeTable.setTreeCellRenderer(new DefaultTreeRenderer(){

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return super.getTreeCellRendererComponent(tree, model.getValueAt(value, 0), selected, expanded, leaf, row, hasFocus);
            }

        });
    }

    public JXTreeTable getViewComponent(){
        return treeTable;
    }

    public void setRepository( NwnRepository rep ){
        model.setRepository(rep);
    }

    public NwnRepository getRepository(){
        return model.rep;
    }

    public void clear(){
        model.clear();
    }

    protected static class Model extends AbstractTreeTableModel{
        private NwnRepository rep;

        public Model(){
            super(new DefaultListNode<>("<Empty>", Collections.emptyList()));
        }

        public void clear(){
            root = new DefaultListNode<>("<Empty>", Collections.emptyList());
            rep = null;
            modelSupport.fireNewRoot();
        }

        public void setRepository( NwnRepository rep ){
            final TreeSet<ResourceID> idsByType = new TreeSet<>(ResourceID.TYPECOMPARATOR);
            idsByType.addAll(rep.getResourceIDs());

            final List<TypeNode> list = new ArrayList<>();
            for ( String typeName : ResourceID.extension2typeMap.keySet() ){
                short type = ResourceID.getTypeForExtension(typeName);
                ResourceID id = new ResourceID( "", type );
                SortedSet<ResourceID> ss = idsByType.tailSet(id);
                if ( !ss.isEmpty() && ss.first().getType() == type ){
                    list.add(new TypeNode(idsByType, type));
                }
            }
            root = new DefaultListNode<>( "Resources", list );
            this.rep = rep;
            modelSupport.fireNewRoot();
        }

        @Override
        public Object getValueAt(Object object, int i) {
            if ( object instanceof ResourceID ){
                ResourceID id = (ResourceID) object;
                switch (i){
                    case 0 : return id.getName();
                    case 1 : return ResourceID.getExtensionForType(id.getType());
                    case 2 : return rep.getResourceSize(id);
                }
            }
            if ( object instanceof ListNode && i == 0 )
                return ((ListNode)object).getPresentationName();
            return "";
        }

        @Override public int getColumnCount(){
            return 3;
        }

        @Override
        public void setValueAt(Object value, Object node, int column) {
        }

        @Override public String getColumnName(int c){
            switch (c){
                case 0 : return "Name";
                case 1 : return "Type";
                case 2 : return "Size";
                default : return "";
            }
        }

        @Override public Class<?> getColumnClass(int c) {
            switch (c){
                case 0 : return super.getColumnClass(c);
                case 1 : return String.class;
                case 2 : return Integer.class;
                default : return null;
            }
        }

        @Override public boolean isLeaf(Object o){
            return (o instanceof ResourceID);
        }

        @Override public int getChildCount( Object o ){
            if ( o instanceof ListNode )
                return ((ListNode)o).getChildCount();
            return -1;
        }

        @Override public Object getChild(Object parent, int index) {
            if ( parent instanceof ListNode ){
                return ((ListNode)parent).getChild(index);
            }
            return null;
        }

        @Override public int getIndexOfChild(Object parent, Object child) {
            if ( parent instanceof ListNode ){
                return ((ListNode)parent).indexOf( child );
            }
            return -1;
        }

        @Override
        public int getHierarchicalColumn() {
            return 0;
        }
    }

    protected static class TypeNode extends DefaultListNode<ResourceID>{
        private final TreeSet<ResourceID> ids;
        private final short type;
        public TypeNode( TreeSet<ResourceID> resources, short type ){
            super(ResourceID.getExtensionForType(type), null);
            this.ids = resources;
            this.type = type;
        }

        @Override
        protected List<ResourceID> createList(){
            final ResourceID from = new ResourceID("", type);
            final ResourceID to   = new ResourceID("", (short)(type+1));
            return new ArrayList<>(ids.subSet(from, to));
        }

        @Override
        public int indexOf(ResourceID id) {
            return Collections.binarySearch(list, id, ResourceID.TYPECOMPARATOR);
        }
    }

    interface ListNode<T>{
        public int indexOf(T child);
        public T getChild( int index );
        public int getChildCount();
        public String getPresentationName();
    }

    static class DefaultListNode<T> implements ListNode<T>{
        protected List<T> list;
        protected String presentationName;
        public DefaultListNode(String name, List<T> children){
            this.list = children;
            this.presentationName = name;
        }

        protected List<T> createList(){
            return null;
        }

        private void init(){
            list = createList();
        }

        @Override
        public int indexOf(T child) {
            //if (list==null) init();
            return list.indexOf(child);
        }

        @Override
        public T getChild(int index) {
            //if (list==null) init();
            return list.get(index);
        }

        @Override
        public String getPresentationName() {
            return presentationName;
        }

        @Override
        public int getChildCount(){
            if (list==null) init();
            return list.size();
        }
    }

    public static void main( String ... args ) throws Exception{
        File f = new File(args[0]);
        final NwnRepository r = f.isDirectory() ? new BifRepository(f) : new ErfFile(f);
        RepositoryTreeView v = new RepositoryTreeView();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JXTreeTable tt = v.getViewComponent();
        JScrollPane scroll = new JScrollPane(tt,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.getContentPane().add(scroll);
        v.setRepository(r);
        frame.pack();
        frame.setVisible(true);
    }
}
