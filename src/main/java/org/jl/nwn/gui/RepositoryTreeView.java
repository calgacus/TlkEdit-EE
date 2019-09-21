package org.jl.nwn.gui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

/**
 */
public class RepositoryTreeView {
    
    private JXTreeTable treeTable;
    private Model model = null;
    
    /** Creates a new instance of RepositoryView */
    public RepositoryTreeView() {
        model = new Model();
        treeTable = new JXTreeTable(model);
        //treeTable = new JXTreeTable();
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
        if ( model == null ){
            model = new Model();
            treeTable.setTreeTableModel(model);
            
        } else
            model.setRepository(rep);
        //treeTable.expandRow(0);
    }
    
    public NwnRepository getRepository(){
        return model.rep;
    }
    
    public void clear(){
        model.clear();
    }
    
    protected static class Model extends AbstractTreeTableModel{
        private Set<ResourceID> resources;
        private Object root;
        private NwnRepository rep;
        
        public Model(){
            super();
            root = new DefaultListNode("<Empty>", Collections.EMPTY_LIST);
        }
        
        public void clear(){
            root = new DefaultListNode("<Empty>", Collections.EMPTY_LIST);
            rep = null;
            modelSupport.fireNewRoot();
        }
        
        public void setRepository( NwnRepository rep ){
            this.rep = rep;
            init();
        }
        
        private void init(){
            TreeSet<ResourceID> idsByType =
                    new TreeSet<ResourceID>(ResourceID.TYPECOMPARATOR);
            idsByType.addAll(rep.getResourceIDs());
            //root = new TypeNode( idsByType, ResourceID.TYPE_2DA );
            
            List<TypeNode> list = new ArrayList<TypeNode>();
            //for ( short type : ResourceID.type2extensionMap.keySet() ){
            for ( String typeName : ResourceID.extension2typeMap.keySet() ){
                short type = ResourceID.getTypeForExtension(typeName);
                ResourceID id = new ResourceID( "", type );
                SortedSet<ResourceID> ss = idsByType.tailSet(id);
                if ( !ss.isEmpty() && ss.first().getType() == type ){
                    list.add(new TypeNode(idsByType, type));
                }
            }
            root = new DefaultListNode( "Resources", list );
            modelSupport.fireNewRoot();
        }
        
        public String convertValueToText(Object o){
            return getValueAt(o, 0).toString();
        }
        
        @Override
        public Object getValueAt(Object object, int i) {
            if ( object instanceof ResourceID ){
                ResourceID id = (ResourceID) object;
                switch (i){
                    case 0 : return id.getName();
                    case 1 : return id.getExtensionForType(id.getType());
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
        public void setValueAt(Object object, Object object0, int i) {
        }
        
        @Override public String getColumnName(int c){
            switch (c){
                case 0 : return "Name";
                case 1 : return "Type";
                case 2 : return "Size";
                default : return "";
            }
        }
        
        @Override public Class getColumnClass(int c){
            switch (c){
                case 0 : return super.getColumnClass(c);
                case 1 : return String.class;
                case 2 : return Integer.class;
                default : return null;
            }
        }
        
        @Override public Object getRoot(){
            return root;
        }
        
        @Override public boolean isLeaf(Object o){
            return (o instanceof ResourceID);
        }
        
        @Override public int getChildCount( Object o ){
            if ( o instanceof ListNode )
                return ((ListNode)o).getChildCount();
            return -1;
        }
        
        @Override public Object getChild(java.lang.Object parent, int index){
            if ( parent instanceof ListNode ){
                return ((ListNode)parent).getChild(index);
            }
            return null;
        }
        
        @Override public int getIndexOfChild(Object arg0, Object arg1){
            if ( arg0 instanceof ListNode ){
                return ((ListNode)arg0).indexOf( arg1 );
            }
            return -1;
        }

        @Override
        public int getHierarchicalColumn() {
            return 0;
        }
        
    }
    
    protected static class TypeNode extends DefaultListNode<ResourceID>{
        private TreeSet<ResourceID> ids;
        private short type;
        public TypeNode( TreeSet<ResourceID> resources, short type ){
            super(ResourceID.getExtensionForType(type), null);
            this.ids = resources;
            this.type = type;
        }
        
        @Override protected List<ResourceID> createList(){

            ResourceID from = new ResourceID("", type);
            ResourceID to = new ResourceID("", (short)(type+1));
            SortedSet<ResourceID> ss = ids.subSet(from, to);
            ArrayList children = new ArrayList<ResourceID>(ss.size());
            children.addAll(ss);
            return children;
        }
        
        @Override public int indexOf(ResourceID id){
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
        NwnRepository r = null;
        if ( f.isDirectory() )
            r = new BifRepository( f );
        else
            r = new ErfFile( f );
        RepositoryTreeView v = new RepositoryTreeView();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JXTreeTable tt = v.getViewComponent();
        //tt.setRootVisible(true);
        JScrollPane scroll = new JScrollPane(tt,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.getContentPane().add(scroll);
        v.setRepository(r);
        frame.pack();
        frame.setVisible(true);
    }
    
}
