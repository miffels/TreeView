package com.jj.swing.tree;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class Runner {

	public static void main(String[] args) {
		JFrame frame = new JFrame("TreeView demo");
		frame.setSize(new Dimension(1000, 800));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		DefaultTreeModel model = getTestModel();
		TreeView treeView = new TreeView(new CustomTreeAdapter(model), model);
		panel.add(treeView);
		panel.setBorder(new LineBorder(Color.BLUE));
		JTree tree = new JTree();
		tree.setModel(getTestModel());
		tree.setCellRenderer(new CustomTreeCellRenderer());
		panel.add(tree);
		panel.add(new JLabel("HiiiI!"));
		
//		treeView.setRootVisible(false);
//		treeView.expandAll();
		
		frame.add(panel);
		frame.setVisible(true);
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getChild(model.getRoot(), 2);
		model.insertNodeInto(new DefaultMutableTreeNode("test123"), node, 2);
	}

	public static DefaultTreeModel getTestModel() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
		DefaultMutableTreeNode parent;
		
		parent = new DefaultMutableTreeNode("colors");
		root.add(parent);
		parent.add(new DefaultMutableTreeNode("blue"));
		parent.add(new DefaultMutableTreeNode("violet"));
		parent.add(new DefaultMutableTreeNode("red"));
		DefaultMutableTreeNode child = new DefaultMutableTreeNode("yellow");
		parent.add(child);
		
		child.add(new DefaultMutableTreeNode("test"));

		parent = new DefaultMutableTreeNode("sports");
		root.add(parent);
		parent.add(new DefaultMutableTreeNode("basketball"));
		parent.add(new DefaultMutableTreeNode("soccer"));
		parent.add(new DefaultMutableTreeNode("football"));
		parent.add(new DefaultMutableTreeNode("hockey"));

		parent = new DefaultMutableTreeNode("food");
		root.add(parent);
		parent.add(new DefaultMutableTreeNode("hot dogs"));
		parent.add(new DefaultMutableTreeNode("pizza"));
		parent.add(new DefaultMutableTreeNode("ravioli"));
		parent.add(new DefaultMutableTreeNode("bananas"));
		return new DefaultTreeModel(root);
	}

}
