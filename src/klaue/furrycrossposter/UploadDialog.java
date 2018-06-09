package klaue.furrycrossposter;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import klaue.furrycrossposter.sites.Site;

public class UploadDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -6246905513443973300L;
	private ImageInfo imageInfo = null;
	
	public UploadDialog(ImageInfo imageInfo, ArrayList<Site> sites) {
		this.imageInfo = imageInfo;
		
		this.setTitle("Furry Crossposter - Upload");
		this.setSize(700, 400);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		this.add(mainPanel);
		
		JLabel lblTitle = new JLabel("Upload");
		lblTitle.setFont(lblTitle.getFont().deriveFont(24f));
		mainPanel.add(lblTitle);
		mainPanel.add(Box.createVerticalStrut(10));
		
		JLabel lblInfos = new JLabel("<html><body>Finally time to upload! Below you'll find buttons for all the pages for which your image data is eligible. Clicking on a button will open a new browser info on the page. Log in if neccessary and touch nothing afterwards, Furry Crossposter will take over. After the image is uploaded, the browser window will stay to let you edit it, close the window after you're done. The login form of the sites will not fill automatically, use right click menu to fill them with your saved passwords, if you saved some on the profile given FurryCrossposter at first start.</body></html>");
		lblInfos.setFont(lblInfos.getFont().deriveFont(Font.PLAIN));
		mainPanel.add(lblInfos);
		mainPanel.add(Box.createVerticalStrut(10));
		
		TreeMap<String, ArrayList<String>> errorMessages = new TreeMap<>();
		TreeMap<String, ArrayList<String>> warningMessages = new TreeMap<>();
		ArrayList<Site> eligibleSites = new ArrayList<>();
		
		for (Site site : sites) {
			ArrayList<String> errorMessagesForSite = site.getErrorReasons(imageInfo);
			if (errorMessagesForSite.size() > 0) {
				errorMessages.put(site.getName(), errorMessagesForSite);
			} else {
				ArrayList<String> warningMessagesForSite = site.getWarningReasons(imageInfo);
				if (warningMessagesForSite.size() > 0) warningMessages.put(site.getName(), warningMessagesForSite);
				eligibleSites.add(site);
			}
		}
		
		if (errorMessages.size() > 0) {
			StringBuffer failReasons = new StringBuffer("<html><body>");
			for (String pageName : errorMessages.keySet()) {
				failReasons.append(pageName).append(": ");
				for (String errorMessage : errorMessages.get(pageName)) {
					failReasons.append(errorMessage).append(", ");
				}
				failReasons.delete(failReasons.length() - 2, failReasons.length());
				failReasons.append("<br>");
			}
			failReasons.delete(failReasons.length() - 4, failReasons.length());

			JLabel failLabel = new JLabel(failReasons.toString() + "</body></html>");
			failLabel.setFont(failLabel.getFont().deriveFont(Font.PLAIN));
			mainPanel.add(new JLabel("Reason some pages are missing:"));
			mainPanel.add(failLabel);
			mainPanel.add(Box.createVerticalStrut(10));
		}
		
		if (warningMessages.size() > 0) {
			StringBuffer warningReasons = new StringBuffer("<html><body>");
			for (String pageName : warningMessages.keySet()) {
				warningReasons.append(pageName).append(": ");
				for (String warningMessage : warningMessages.get(pageName)) {
					warningReasons.append(warningMessage).append(", ");
				}
				warningReasons.delete(warningReasons.length() - 2, warningReasons.length());
				warningReasons.append("<br>");
			}
			warningReasons.delete(warningReasons.length() - 4, warningReasons.length());

			JLabel warnLabel = new JLabel(warningReasons.toString() + "</body></html>");
			warnLabel.setFont(warnLabel.getFont().deriveFont(Font.PLAIN));
			mainPanel.add(new JLabel("Warnings:"));
			mainPanel.add(warnLabel);
			mainPanel.add(Box.createVerticalStrut(10));
		}
		mainPanel.add(Box.createVerticalStrut(10));
		
		// min. 1 page is eligible
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		buttonsPanel.setAlignmentX(LEFT_ALIGNMENT);
		buttonsPanel.setAlignmentY(BOTTOM_ALIGNMENT);
		for (Site site : eligibleSites) {
			JButton button = new JButton(site.getName());
			button.addActionListener(this);
			button.setActionCommand(site.getClass().getName());
			buttonsPanel.add(button);
		}
		//buttonsPanel.setMaximumSize(buttonsPanel.getPreferredSize());
		mainPanel.add(buttonsPanel);
		
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// try to get the instance by the command
		Site site = null;
		try {
			Class<?> clazz = Class.forName(e.getActionCommand());
			Constructor<?> ctor = clazz.getConstructor();
			site = (Site)ctor.newInstance();
		} catch (Exception e1) {
			// just doesn't happen, all that can throw actions here are of type SiteInterface
			e1.printStackTrace();
			return;
		}
		
		try {
			boolean worked = site.doUpload(this.imageInfo);
			if (!worked) {
				JOptionPane.showMessageDialog(this, "Errors while uploading to " + site.getName(), "Furry Crossposter", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			String message = e1.getMessage();
			message = message.substring(0, message.indexOf("\n"));
			JOptionPane.showMessageDialog(this, site.getName() + " generated an error: " + message, "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}
