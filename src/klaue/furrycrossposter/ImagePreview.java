package klaue.furrycrossposter;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * CHANGED 2016 BY KLAUE
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.imgscalr.Scalr;

/* ImagePreview.java by FileChooserDemo2.java. */
public class ImagePreview extends JComponent implements PropertyChangeListener {
	private static final long serialVersionUID = 6883402245318564687L;
	ImageIcon thumbnail = null;
	File file = null;

	public ImagePreview(JFileChooser fc) {
		setPreferredSize(new Dimension(120, 100)); // only width important
		fc.addPropertyChangeListener(this);
	}

	public void loadImage() {
		if (this.file == null) {
			this.thumbnail = null;
			return;
		}

		BufferedImage image = null;
		try {
			image = ImageIO.read(this.file);
		} catch (IOException e) {
			e.printStackTrace();
			this.thumbnail = null;
			return;
		}
		int width = (int) this.getSize().getWidth();
		int height = (int) this.getSize().getHeight();

		if (image.getWidth() > width || image.getHeight() > height) {
			image = Scalr.resize(image,  width,  height);
		}
		this.thumbnail = new ImageIcon(image);
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		boolean update = false;
		String prop = e.getPropertyName();

		// If the directory changed, don't show an image.
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			this.file = null;
			update = true;

			// If a file became selected, find out which one.
		} else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			this.file = (File) e.getNewValue();
			update = true;
		}

		// Update the preview accordingly.
		if (update) {
			this.thumbnail = null;
			if (isShowing()) {
				loadImage();
				repaint();
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (this.thumbnail == null) {
			loadImage();
		}
		if (this.thumbnail != null) {
			int x = getWidth() / 2 - this.thumbnail.getIconWidth() / 2;
			int y = getHeight() / 2 - this.thumbnail.getIconHeight() / 2;

			if (y < 0) {
				y = 0;
			}

			if (x < 5) {
				x = 5;
			}
			this.thumbnail.paintIcon(this, g, x, y);
		}
	}
}
