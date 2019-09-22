/*
 * Created on 05.04.2005
 */
package org.jl.nwn.gff.editor;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jl.nwn.Version;
import org.jl.nwn.gff.DefaultGffReader;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffField;
import org.jl.nwn.gff.GffList;
import org.jl.nwn.gff.GffStruct;

/**
 */
public class GffTreeModel implements TreeModel {

	protected GffStruct root;

	protected List listeners = new LinkedList();

	public GffTreeModel( GffStruct s ){
		root = s;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
		return root;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		GffField f = (GffField) parent;
		if ( f.getType() == Gff.LIST )
			return ((GffList)f).get(index);
		else
			return ((GffStruct)f).getChild(index);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		GffField f = (GffField) parent;
		if ( f.getType() == Gff.LIST )
			return ((GffList)f).getSize();
		else
			return ((GffStruct)f).getSize();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		GffField f = (GffField) node;
		return f.isDataField();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		GffField f = (GffField) parent;
		if ( f.getType() == Gff.LIST )
			return ((GffList)f).indexOf(child);
		else
			return ((GffStruct)f).indexOf(child);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public static void main( String[] args ) throws IOException{
		GffStruct s = (GffStruct)(new DefaultGffReader(Version.getDefaultVersion()).load(new File("/usr/local/neverwinter/localvault/stormofblaark.bic"))).getTopLevelStruct();
		TreeModel m = new GffTreeModel(s);
		JTree tree = new JTree(m);
		JFrame f = new JFrame("test");
		f.getContentPane().add(new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}

}
