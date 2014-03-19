package com.jj.swing.tree;

import java.awt.Component;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class DefaultTreeAdapter implements TreeAdapter {
	
	@Override
	public Component getView(Component view, TreePath path, TreeNode value, boolean isLeaf,
			boolean isExpanded) {
		DefaultTreeCell label = (DefaultTreeCell)view;
		if(view == null) {
			label = new DefaultTreeCell();
		}
		
		label.setText("" + value);
		
		return label;
	}
	
}
