package org.jl.nwn.gff.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jl.nwn.Version;
import org.jl.nwn.gff.DefaultGffReader;
import org.jl.nwn.gff.GffField;
import org.jl.nwn.gff.GffList;
import org.jl.nwn.gff.GffStruct;

public class GffTreeModel implements TreeModel {

	protected GffStruct root;

	protected List<TreeModelListener> listeners = new ArrayList<>();

	public GffTreeModel( GffStruct s ){
		root = s;
	}

	@Override
	public GffStruct getRoot() {
		return root;
	}

	@Override
	public GffField getChild(Object parent, int index) {
        final GffField f = (GffField) parent;
        if (f instanceof GffList) {
            return ((GffList)f).get(index);
        }
        return f.getChild(index);
	}

	@Override
	public int getChildCount(Object parent) {
        final GffField f = (GffField) parent;
        return f.getChildCount();
	}

	@Override
	public boolean isLeaf(Object node) {
		GffField f = (GffField) node;
		return f.isDataField();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
        final GffField f = (GffField) parent;
        return f.getChildIndex((GffField)child);
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public static void main( String[] args ) throws IOException{
        final GffStruct s = (new DefaultGffReader(Version.getDefaultVersion()).load(new File("/usr/local/neverwinter/localvault/stormofblaark.bic"))).getTopLevelStruct();
		TreeModel m = new GffTreeModel(s);
		JTree tree = new JTree(m);
		JFrame f = new JFrame("test");
		f.getContentPane().add(new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}
