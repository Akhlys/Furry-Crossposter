package klaue.furrycrossposter;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public abstract class ImageDropTarget extends DropTarget {
	private FileFilter fileFilter = null;
	private Component parent = null;
	
	public ImageDropTarget(FileFilter fileFilter, Component parent) {
		super();
		this.fileFilter = fileFilter;
		this.parent = parent;
	}
	
	@Override
	public synchronized void drop(DropTargetDropEvent dtde) {
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		try {
			@SuppressWarnings("unchecked")
			List<File> droppedFiles = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			dtde.dropComplete(true);
			if (droppedFiles == null || droppedFiles.isEmpty() || droppedFiles.size() > 1) {
				System.err.println("Only one file drop allowed"); // just do nothing in UI
			} else {
				File f = droppedFiles.get(0);
				if (this.fileFilter.accept(f)) {
					fileAccepted(f);
				} else {
					JOptionPane.showMessageDialog(this.parent, "Not an allowed image type", "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract void fileAccepted(File f);
}
