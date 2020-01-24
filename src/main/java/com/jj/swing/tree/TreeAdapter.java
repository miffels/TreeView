package com.jj.swing.tree;

import java.awt.Component;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public interface TreeAdapter {
	
	public Component getView(Component view, TreePath path, TreeNode value, boolean isLeaf, boolean isExpanded);

}
