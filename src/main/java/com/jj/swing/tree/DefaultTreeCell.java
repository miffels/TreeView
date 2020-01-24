package com.jj.swing.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * Sample tree cell component demonstrating how to combine multiple components.
 * 
 * @author Michael Jess
 *
 */
public class DefaultTreeCell extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JLabel textLabel = new JLabel();
	
	
	public DefaultTreeCell() {
		this.setBackground(Color.YELLOW);
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		this.setLayout(flowLayout);
		this.setPreferredSize(new Dimension(200, 30));
		this.add(textLabel);
		textLabel.setBorder(new LineBorder(Color.BLACK));
		this.setBorder(new LineBorder(Color.BLACK));
	}
	
	@Override
	public Dimension getMaximumSize() {
		return this.getPreferredSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return this.getPreferredSize();
	}

	public void setText(String text) {
		this.textLabel.setText(text);
	}

}
