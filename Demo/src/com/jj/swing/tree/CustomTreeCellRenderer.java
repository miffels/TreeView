package com.jj.swing.tree;

import java.awt.Color;

import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CustomTreeCellRenderer() {
		super();
		this.setBorder(new LineBorder(Color.BLACK));
	}

}
