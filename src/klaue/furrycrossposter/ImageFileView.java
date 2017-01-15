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

package klaue.furrycrossposter;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

/* ImageFileView.java is used by FileChooserDemo2.java. */
public class ImageFileView extends FileView {

//    public String getName(File f) {
//        return null; //let the L&F FileView figure this out
//    }
//
//    public String getDescription(File f) {
//        return null; //let the L&F FileView figure this out
//    }
//
//    public Boolean isTraversable(File f) {
//        return null; //let the L&F FileView figure this out
//    }
//
//    public String getTypeDescription(File f) {
//        return 
//    }
	
	private static int ICONSIDE = 50;

    public Icon getIcon(File f) {
    	if (f.isDirectory()) return null;
    	
        int i = f.getName().lastIndexOf('.');
        String extension = (i != -1) ? f.getName().substring(i+1).toLowerCase() : f.getName().toLowerCase();
        
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png") && !extension.equals("gif")) return null;
        
        try {
        	Image image = ImageIO.read(f);
        	
        	int beforeWidth = image.getWidth(null);
    		int beforeHeight = image.getHeight(null);
    		double zoomX = beforeWidth / (double)ICONSIDE;
    		double zoomY = beforeHeight / (double)ICONSIDE;
    		
    		// -1: keep aspect ratio
    		if (zoomX > zoomY) {
    			image = image.getScaledInstance(ICONSIDE, -1, Image.SCALE_FAST);
    		} else {
    			image = image.getScaledInstance(-1, ICONSIDE, Image.SCALE_FAST);
    		}
    		return new ImageIcon(image);
        } catch (IOException e){
        	e.printStackTrace();
        }
        
        
        return null;
    }
}
