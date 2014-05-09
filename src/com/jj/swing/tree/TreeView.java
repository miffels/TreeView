package com.jj.swing.tree;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class TreeView extends JComponent {
	
	private class RowPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		
		private static final int LEVEL_INSET_PIXELS = 20;
		
		private Component content;
		private Dimension size = new Dimension(0, 0);
		private TreeNode value;
		private JLabel expander = new JLabel((Icon)null, JLabel.CENTER);
		private JLabel icon = new JLabel((Icon)null, JLabel.CENTER);
		private MouseListener expanderListener;
		private Component additionalGap = Box.createHorizontalStrut(LEVEL_INSET_PIXELS);
		private Component gap;
		
		public RowPanel(int level) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			this.add(this.icon);
			this.expander.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			this.setBackground(Color.WHITE);
			this.setFocusable(true);
		}
		
		private void calculateSize() {
			Icon icon = this.icon.getIcon();
			if(this.content != null) {
				int iconHeight = icon == null ? 0 : icon.getIconHeight();
				int iconWidth = icon == null ? 0 : icon.getIconWidth();
				this.icon.setPreferredSize(new Dimension(iconWidth, Math.max(iconHeight, this.content.getPreferredSize().height)));
				this.size = new Dimension(content.getPreferredSize().width + LEVEL_INSET_PIXELS * TreeView.this.getDepthOf(value) + iconWidth, this.content.getPreferredSize().height);
				this.expander.setPreferredSize(new Dimension(LEVEL_INSET_PIXELS, content.getPreferredSize().height));
			}
		}
		
		public void setContent(Component content) {
			if(this.content != null) {
				this.remove(this.content);
			}
			this.content = content;
			this.add(content);
			this.calculateSize();
		}
		
		
		public TreeNode getValue() {
			return value;
		}

		public void setValue(TreeNode value) {
			this.value = value;
			this.expander.removeMouseListener(this.expanderListener);
			this.icon.removeMouseListener(this.expanderListener);
			if(this.gap != null) {
				this.remove(this.gap);
			}
			this.gap = Box.createHorizontalStrut((TreeView.this.getDepthOf(value) - 1) * LEVEL_INSET_PIXELS);
			this.add(gap, 0);
			if(TreeView.this.isLeaf(value)) {
				this.remove(expander);
				this.icon.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				this.add(additionalGap, 1);
			} else {
				this.remove(additionalGap);
				this.add(expander, 1);
				this.expanderListener = new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						TreeView.this.toggle(RowPanel.this.value);
					}
					
				};
				this.expander.addMouseListener(this.expanderListener);
				this.icon.addMouseListener(this.expanderListener);
				this.icon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			this.calculateSize();
		}
		
		public void setIcon(Icon icon) {
			this.icon.setIcon(icon);
			this.calculateSize();
		}

		public Component getContent() {
			return this.content;
		}
		
		public Dimension getPreferredSize() {
			return this.size;
		}
	}
	
	private static class ModelListener implements TreeModelListener {
		
		private TreeView tree;
		
		public ModelListener(TreeView tree) {
			this.tree = tree;
		}

		@Override
		public void treeNodesChanged(TreeModelEvent e) {
		}

		@Override
		public void treeNodesInserted(TreeModelEvent e) {
			TreeNode parent = (TreeNode)e.getPath()[e.getPath().length - 1];
			TreeNode child = (TreeNode)e.getChildren()[0];
			this.tree.valueDepths.put(child, this.tree.rootVisible ?  e.getPath().length : e.getPath().length - 1);
			if(this.tree.expandedNodes.contains(parent)) {
				this.tree.addRecursively(this.tree.indexOf(this.tree.getComponentFor(parent)) + e.getChildIndices()[0], child);
				this.tree.calculateSize();
				this.tree.revalidate();
			}
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent e) {
			TreeNode node = (TreeNode)e.getChildren()[0];
			TreeNode parent = (TreeNode)e.getPath()[e.getPath().length - 1];
			this.tree.remove(node);
			if(this.tree.isLeaf(parent)) {
				// Just became a leaf
				this.tree.refresh((TreeNode)e.getPath()[e.getPath().length - 1]);
			}
			this.tree.calculateSize();
			this.tree.revalidate();
		}

		@Override
		public void treeStructureChanged(TreeModelEvent e) {
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	private TreeAdapter adapter;
	private TreeModel model;
	private boolean rootVisible = true;
	private ModelListener modelListener = new ModelListener(this);
	Map<TreeNode, RowPanel> reuseComponents = new HashMap<TreeNode, RowPanel>();
	Set<TreeNode> expandedNodes = new HashSet<TreeNode>();
	Map<TreeNode, Integer> valueDepths = new HashMap<TreeNode, Integer>();
	Map<TreeNode, TreeNode> parents = new HashMap<TreeNode, TreeNode>();
	
	private List<TreeSelectionListener> selectionListeners = new ArrayList<TreeSelectionListener>();
	
	private Icon leafIcon;
	private Icon closedIcon;
	private Icon openIcon;

	private TreeNode lastSelectedNode;

	private AWTEventListener globalListener = new AWTEventListener() {
		
	    public void eventDispatched(AWTEvent e) {
	    	if(e.getID() != MouseEvent.MOUSE_CLICKED) {
	    		return;
	    	}
	        for(Component component : TreeView.this.getComponents()) {
	        	if(SwingUtilities.isDescendingFrom((Component)e.getSource(), component)) {
	        		TreeView.this.fireTreeSelectionChangeEvent(((RowPanel)component).value, (Component)e.getSource());		        		
	        	}
	        }
	    }
	    
	};
	
	public TreeView(TreeAdapter adapter, JFrame parentFrame) {
		this(adapter, null, parentFrame);
	}
	
	public TreeView(TreeAdapter adapter, TreeModel model, JFrame parentFrame) {
		this.adapter = adapter;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
		this.setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
		this.setOpenIcon(UIManager.getIcon("Tree.openIcon"));
		this.setModel(model);
		
		Toolkit.getDefaultToolkit().addAWTEventListener(this.globalListener, AWTEvent.MOUSE_EVENT_MASK);
		
		parentFrame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				Toolkit.getDefaultToolkit().removeAWTEventListener(TreeView.this.globalListener);
			}
		});
	}
	
	public void setModel(TreeModel model) {
		if(this.model != null) {
			this.model.removeTreeModelListener(this.modelListener);
		}
		this.model = model;
		
		this.expandedNodes.clear();
		this.reuseComponents.clear();
		this.valueDepths.clear();
		this.parents.clear();
		this.removeAll();
		
		if(model != null) {
			model.addTreeModelListener(this.modelListener);
			this.populate();
		}
		this.invalidate();
		this.revalidate();
		this.repaint();
	}
	
	public TreeModel getModel() {
		return this.model;
	}
	
	private void remove(TreeNode node) {
		this.removeRecursively2(node);
		this.calculateSize();
		this.revalidate();
	}
	
	private void removeRecursively2(TreeNode node) {
		this.remove(this.getComponentFor(node));
		this.expandedNodes.remove(node);
		this.reuseComponents.remove(node);
		this.valueDepths.remove(node);
		this.parents.remove(node);
		for(TreeNode child : this.getChildrenOf(node)) {
			this.removeRecursively2(child);
		}
	}
	
	private void populate() {
		if(this.rootVisible) {
			this.addComponents(this.getRoot(), 0);
		} else {
			for(TreeNode node : this.getChildrenOf(this.getRoot())) {
				this.addComponents(node, 0);
			}
		}
		this.calculateSize();
	}
	
	@Override
	public Dimension getMaximumSize() {
		return this.getPreferredSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return this.getPreferredSize();
	}
	
	public void setRootVisible(boolean rootVisible) {
		this.rootVisible = rootVisible;
		this.setModel(this.model);
	}

	private void addComponents(TreeNode node, int depth) {
		Component component = this.getComponentFor(node);
		this.add(component);
		
		if(this.expandedNodes.contains(node)) {
			for(TreeNode child : this.getChildrenOf(node)) {
				this.addComponents(child, depth + 1);
			}
		}
	}
	
	private void calculateSize() {
		int width = 0;
		int height = 0;
		for(Component component : this.getComponents()) {
			Dimension componentSize = component.getPreferredSize();
			width = Math.max(width, componentSize.width);
			height += componentSize.height;
		}
		this.setPreferredSize(new Dimension(width, height));
	}
	
	public void expandAll() {
		this.expandAll(this.getRoot());
		this.populate();
	}
	
	private TreeNode getRoot() {
		return (TreeNode)this.model.getRoot();
	}
	
	private void expandAll(TreeNode node) {
		this.expandedNodes.add(node);
		for(TreeNode child : this.getChildrenOf(node)) {
			this.expandedNodes.add(child);
			this.expandAll(child);
		}
	}
	
	private List<TreeNode> getChildrenOf(TreeNode node) {
		List<TreeNode> children = new ArrayList<TreeNode>();
		for(int i = 0; i < this.model.getChildCount(node); i++) {
			children.add((TreeNode)this.model.getChild(node, i));
		}
		
		return children;
	}
	
	/**
	 * Updates the data of the corresponding TreeRow.
	 * 
	 * @param value
	 */
	protected void refresh(TreeNode value) {
		this.getComponentFor(value);
	}
	
	private Component getComponentFor(final TreeNode value) {
		RowPanel reusePanel = this.reuseComponents.get(value);
		if(reusePanel == null) {
			reusePanel = new RowPanel(this.getDepthOf(value));
			reusePanel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2) {
						TreeView.this.toggle(value);
					}
				}
				
			});
		}
		boolean isLeaf = this.isLeaf(value);
		boolean isExpanded = this.expandedNodes.contains(value);
		if(isLeaf) {
			reusePanel.setIcon(this.getLeafIcon());
		} else if(isExpanded) {
			reusePanel.setIcon(this.getOpenIcon());
		} else {
			reusePanel.setIcon(this.getClosedIcon());
		}
		
		reusePanel.setContent(this.adapter.getView(reusePanel.getContent(), null, value,
				isLeaf, isExpanded));
		reusePanel.setValue(value);
		this.reuseComponents.put(value, reusePanel);
		
		return reusePanel;
	}
	
	private boolean isLeaf(Object node) {
		return this.model.getChildCount(node) == 0;
	}
	
	private int getDepthOf(TreeNode node) {
		if(this.valueDepths.containsKey(node)) {
			return this.valueDepths.get(node);
		}
		int depth = 0;
		TreeNode parent = node;
		while((parent = this.getParentOf(parent)) != null) {
			depth++;
		}
		this.valueDepths.put(node, this.rootVisible ? depth : depth - 1);
		return depth;
	}
	
	private void toggle(TreeNode value) {
		if(this.expandedNodes.contains(value)) {
			this.expandedNodes.remove(value);
			for(TreeNode child : this.getChildrenOf(value)) {
				this.removeRecursively(child);
			}
			this.getComponentFor(value);
		} else {
			this.expandedNodes.add(value);
			int index = this.indexOf(this.getComponentFor(value));
			for(TreeNode child : this.getChildrenOf(value)) {
				index = this.addRecursively(index, child);
			}
		}
		this.calculateSize();
		this.invalidate();
		this.repaint();
		this.revalidate();
	}
	
	private void removeRecursively(TreeNode node) {
		if(this.reuseComponents.containsKey(node)) {
			this.remove(this.getComponentFor(node));
		}
		for(TreeNode child : this.getChildrenOf(node)) {
			this.removeRecursively(child);
		}
	}
	
	private int addRecursively(int index, TreeNode child) {
		index++;
		this.add(this.getComponentFor(child), index);
		for(TreeNode node : this.getChildrenOf(child)) {
			if(this.expandedNodes.contains(child)) {
				index = this.addRecursively(index, node);
			}
		}
		return index;
	}
	
	private int indexOf(Component component) {
		return Arrays.asList(this.getComponents()).indexOf(component);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D graphics = (Graphics2D)g;
		
		BasicStroke dashed = /*new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                1.0f, new float[] {1.0f}, 0.0f);*/
				new BasicStroke();
		
		graphics.setStroke(dashed);
		graphics.setColor(UIManager.getColor("Tree.hash"));
		boolean skip = true;
		
		for(Component row : this.getComponents()) {
			if(skip) {
				skip = false;
				continue;
			}
			
			RowPanel rowPanel = (RowPanel)row;
			TreeNode node = rowPanel.getValue();
			Component rowContent = rowPanel.icon;
			
			Rectangle contentBounds = rowContent.getBounds();
			Rectangle rowBounds = row.getBounds();
			int nodeDepth = this.getDepthOf(node);
			TreeNode parent = node;
			
			for(int i = nodeDepth; i > 0; i--) {
				int malus = 0;
				if(i == nodeDepth && this.isLastChild(node)) {
					malus = -rowBounds.height / 2;
					graphics.drawLine(20 * (i - 1) + 10, rowBounds.y, 20 * (i - 1) + 10, rowBounds.y + rowBounds.height + malus);
				} else if(i == nodeDepth){
					graphics.drawLine(20 * (i - 1) + 10, rowBounds.y, 20 * (i - 1) + 10, rowBounds.y + rowBounds.height + malus);
				} else if(this.model.getRoot() != parent && !this.isLastChild(parent)) {
					graphics.drawLine(20 * (i - 1) + 10, rowBounds.y, 20 * (i - 1) + 10, rowBounds.y + rowBounds.height + malus);
				}
				parent = this.getParentOf(parent);
			}
			int mid = rowBounds.y + rowBounds.height / 2;
			
			if(nodeDepth > 0) {
				graphics.drawLine(contentBounds.x - 10, mid, contentBounds.x, mid);
				
				if(!this.isLeaf(node)) {
					graphics.drawOval(contentBounds.x - 13, mid - 3, 6, 6);
				}
			}
		}
	}
	
	private boolean isLastChild(TreeNode child) {
		Object parent = this.getParentOf(child);
		return parent != null && this.model.getIndexOfChild(parent, child) == this.model.getChildCount(parent) - 1;
	}
	
	public TreeNode getParentOf(TreeNode child) {
		TreeNode parent = this.parents.get(child);
		TreeNode root = this.getRoot();
		if(parent == null && root != parent)  {
			parent = this.getParentOf(this.getRoot(), child);
		}
		this.parents.put(child, parent);
		return parent;
	}
	
	private TreeNode getParentOf(TreeNode parent, TreeNode child) {
		if(this.model.getIndexOfChild(parent, child) != -1) {
			return parent;
		}
		TreeNode result = null;
		for(TreeNode node : this.getChildrenOf(parent)) {
			result = this.getParentOf(node, child);
			if(result != null) {
				return result;
			}
		}
		return null;
	}
	
	public void setClosedIcon(Icon closedIcon) {
		this.closedIcon = closedIcon;
	}

	public void setOpenIcon(Icon openIcon) {
		this.openIcon = openIcon;
	}

	public void setLeafIcon(Icon leafIcon) {
		this.leafIcon = leafIcon;
	}
	
	public Icon getLeafIcon() {
		return leafIcon;
	}

	public Icon getClosedIcon() {
		return closedIcon;
	}

	public Icon getOpenIcon() {
		return openIcon;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		this.selectionListeners.add(listener);
	}
	
	public void removeTreeSelectionListener(TreeSelectionListener listener) {
		this.selectionListeners.remove(listener);
	}
	
	private void fireTreeSelectionChangeEvent(TreeNode node, Component source) {
		if(this.lastSelectedNode != node) {
			this.lastSelectedNode = node;
			for(TreeSelectionListener listener : this.selectionListeners) {
				listener.nodeSelected(node, source);
			}
		}
	}
	
	public void focusRow(int rowIndex) {
		Component row = this.getComponents()[rowIndex];
		row.requestFocus();
		for(Entry<TreeNode, RowPanel> entry : this.reuseComponents.entrySet()) {
			if(entry.getValue().equals(row)) {
				this.fireTreeSelectionChangeEvent(entry.getKey(), row);
				break;
			}
		}
	}
	
}
