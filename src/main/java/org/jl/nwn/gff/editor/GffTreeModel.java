package org.jl.nwn.gff.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

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
}
