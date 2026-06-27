package com.fitwell.boundry;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class UIBuilder {

	// ***** JLabel ***** //

	public static class LabelBuilder {
		private final JLabel label;

		public static LabelBuilder of(String title) {
			return new LabelBuilder(title);
		}

		public static LabelBuilder of(JLabel label) {
			return new LabelBuilder(label);
		}

		public LabelBuilder(JLabel label) {
			this.label = label;
		}

		public LabelBuilder(String title) {
			this.label = new JLabel(title);
		}

		public LabelBuilder font(Font font) {
			label.setFont(font);
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

		public JButton build() {
			return btn;
		}
	}
}
