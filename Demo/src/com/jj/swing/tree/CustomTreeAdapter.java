package com.jj.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class CustomTreeAdapter extends com.jj.swing.tree.DefaultTreeAdapter {
	
	private static class TreeCell extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private JLabel iconLabel = new JLabel();
		private JTextField textField = new JTextField();
		private JButton button = new JButton("Remove");
		private DefaultTreeModel model;
		private MutableTreeNode value;
		private ActionListener listener;
		
		public TreeCell(TreeModel model) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			this.model = (DefaultTreeModel)model;
			this.setLayout(flowLayout);
			this.setPreferredSize(new Dimension(200, 35));
			this.add(iconLabel);
			this.add(textField);
			this.add(button);
			this.button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					TreeCell cell = TreeCell.this;
					DefaultTreeModel model = (DefaultTreeModel)cell.model;
					model.removeNodeFromParent(cell.value);
					System.out.println("removed");
				}
			});
			
			this.setBackground(Color.WHITE);
		}
		
		public void setValue(TreeNode value) {
			this.value = (MutableTreeNode)value;
			this.textField.setText("" + value);
			this.button.removeActionListener(this.listener);
			this.listener = new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println(TreeCell.this.value.getClass());
				}
			};
			this.button.addActionListener(this.listener);
		}

		@Override
		public Dimension getMaximumSize() {
			return this.getPreferredSize();
		}

		@Override
		public Dimension getMinimumSize() {
			return this.getPreferredSize();
		}

	}
	
	private TreeModel model;
	
	public CustomTreeAdapter(TreeModel model) {
		this.model = model;
	}
	
	@Override
	public Component getView(Component view, TreePath path, TreeNode value, boolean isLeaf,
			boolean isExpanded) {
		TreeCell cell = (TreeCell)view;
		if(view == null) {
			cell = new TreeCell(this.model);
		}
		
		cell.setValue(value);
		
		return cell;
	}
	
}
