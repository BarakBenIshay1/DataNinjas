package com.fitwell.boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class UIBuilder {


	// ***** JPanel ***** //

	public static class PanelBuilder {
		private final JPanel panel;

		public static PanelBuilder of() {
			return new PanelBuilder();
		}

		public static PanelBuilder of(LayoutManager layout) {
			return new PanelBuilder(layout);
		}

		public static PanelBuilder of(JPanel panel) {
			return new PanelBuilder(panel);
		}

		public PanelBuilder(LayoutManager layout) {
			panel = new JPanel(layout);
		}

		public PanelBuilder() {
			panel = new JPanel();
		}

		public PanelBuilder(JPanel panel) {
			this.panel = panel;
		}

		public PanelBuilder layout(LayoutManager layout) {
			panel.setLayout(layout);
			return this;
		}

		public PanelBuilder boxLayout(int axis) {
			panel.setLayout(new BoxLayout(panel, axis));
			return this;
		}

		public PanelBuilder opaque(boolean opaque) {
			panel.setOpaque(opaque);
			return this;
		}

		public PanelBuilder border(Border border) {
			panel.setBorder(border);
			return this;
		}

		public PanelBuilder background(Color color) {
			panel.setBackground(color);
			return this;
		}
		public PanelBuilder maximumSize(Dimension size) {
			panel.setMaximumSize(size);
			return this;
		}
		public PanelBuilder alignmentX(float x) {
			panel.setAlignmentX(x);
			return this;
		}
		public PanelBuilder add(Component component) {
			this.panel.add(component);
			return this;
		}

		public PanelBuilder add(Component component, Object constraints) {
			this.panel.add(component, constraints);
			return this;
		}

		public JPanel build() {
			return panel;
		}
	}

	// ***** JLabel ***** //

	public static class LabelBuilder {
		private final JLabel label;

		public static LabelBuilder of(String title) {
			return new LabelBuilder(title);
		}

		public static LabelBuilder of(String title, int axis) {
			return new LabelBuilder(title);
		}

		public static LabelBuilder of(JLabel label) {
			return new LabelBuilder(label);
		}

		public LabelBuilder(JLabel label) {
			this.label = label;
		}

		public LabelBuilder(String title, int axis) {
			this.label = new JLabel(title, axis);
		}

		public LabelBuilder(String title) {
			this.label = new JLabel(title);
		}

		public LabelBuilder font(Font font) {
			label.setFont(font);
			return this;
		}

		public LabelBuilder fontRegular() {
			label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
			return this;
		}

		public LabelBuilder background(Color color) {
			label.setBackground(color);
			return this;
		}

		public LabelBuilder foreground(Color color) {
			label.setForeground(color);
			return this;
		}

		public LabelBuilder border(Border border) {
			label.setBorder(border);
			return this;
		}

		public LabelBuilder preferredSize(Dimension dimension) {
			label.setPreferredSize(dimension);
			return this;
		}

		public LabelBuilder alignmentX(float x) {
			label.setAlignmentX(x);
			return this;
		}

		public LabelBuilder horizontalAlignment(int alignment) {
			label.setHorizontalAlignment(alignment);
			return this;
		}

		public JLabel build() {
			return label;
		}
	}

	// ***** JLabel ***** //

	public static class ButtonBuilder {
		public final JButton btn;

		public static ButtonBuilder of(String title) {
			return new ButtonBuilder(title);
		}

		public static ButtonBuilder of(JButton label) {
			return new ButtonBuilder(label);
		}

		public ButtonBuilder(JButton btn) {
			this.btn = btn;
		}

		public ButtonBuilder(String title) {
			this.btn = new JButton(title);
		}

		public ButtonBuilder font(Font font) {
			btn.setFont(font);
			return this;
		}

		public ButtonBuilder background(Color color) {
			btn.setBackground(color);
			return this;
		}

		public ButtonBuilder foreground(Color color) {
			btn.setForeground(color);
			return this;
		}

		public ButtonBuilder border(Border border) {
			btn.setBorder(border);
			return this;
		}

		public ButtonBuilder cursor(Cursor cursor) {
			btn.setCursor(cursor);
			return this;
		}

		public ButtonBuilder focus(boolean focus) {
			btn.setFocusPainted(focus);
			return this;
		}

		public ButtonBuilder alignmentX(float x) {
			btn.setAlignmentX(x);
			return this;
		}

		public ButtonBuilder preferredSize(Dimension dimension) {
			btn.setPreferredSize(dimension);
			return this;
		}

		public ButtonBuilder opaque(boolean opaque) {
			btn.setOpaque(opaque);
			return this;
		}

		public JButton build() {
			return btn;
		}
	}
}
