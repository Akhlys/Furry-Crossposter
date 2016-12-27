package klaue.furrycrossposter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import klaue.furrycrossposter.sites.DeviantArt;
import klaue.furrycrossposter.sites.E621;
import klaue.furrycrossposter.sites.FurAffinity;
import klaue.furrycrossposter.sites.FurryNetwork;
import klaue.furrycrossposter.sites.InkBunny;
import klaue.furrycrossposter.sites.Site;
import klaue.furrycrossposter.sites.SoFurry;
import klaue.furrycrossposter.sites.Weasyl;
import layout.TableLayout;
import layout.TableLayoutConstraints;

public class MainWindow extends JFrame implements ActionListener, DocumentListener, ChangeListener {
	private static final long serialVersionUID = 5580717767809657474L;
	private static ArrayList<Site> pages = new ArrayList<>();
	static {
		pages.add(new InkBunny());
		pages.add(new FurAffinity());
		pages.add(new Weasyl());
		pages.add(new FurryNetwork());
		pages.add(new DeviantArt());
		pages.add(new E621());
		pages.add(new SoFurry());
	}
	JFileChooser fileChooser = new JFileChooser();
	
	ImageInfo imageInfo = new ImageInfo();

	private JButton btnSelMainImage = new JButton("Select Image");
	private JButton btnSelThumbImage = new JButton("Select Thumbnail (optional)");
	private JLabel imageLabel = new JLabel("", JLabel.CENTER);
	private JLabel thumbLabel = new JLabel("", JLabel.CENTER);

	private JComboBox<ImageInfo.Type> typeBox = new JComboBox<ImageInfo.Type>();
	private JComboBox<ImageInfo.RatingSexual> ratingSexualBox = new JComboBox<ImageInfo.RatingSexual>();
	private JComboBox<ImageInfo.RatingViolence> ratingViolenceBox = new JComboBox<ImageInfo.RatingViolence>();
	
	private JTextField titleText = new JTextField();
	private JTextArea descrText = new JTextArea(5, 10);
	
	private JCheckBox chkScraps = new JCheckBox("Put in Scraps");
	private JCheckBox chkNoNotification = new JCheckBox("No notification");
	private JCheckBox chkFriendsOnly = new JCheckBox("Friends only");
	private JCheckBox chkUnlisted = new JCheckBox("Unlisted");
	
	private JCheckBox chkMale = new JCheckBox("Male");
	private JCheckBox chkFemale = new JCheckBox("Female");
	private JCheckBox chkHerm = new JCheckBox("Herm");
	private JCheckBox chkDickgirl = new JCheckBox("Dickgirl");
	private JCheckBox chkCuntboy = new JCheckBox("Cuntboy");
	private JCheckBox chkMaleherm = new JCheckBox("Maleherm");
	private JCheckBox chkMtF = new JCheckBox("M2F Trans");
	private JCheckBox chkFtM = new JCheckBox("F2M Trans");
	
	private JTextPane speciesTags = new JTextPane();
	private JTextPane kinkTags = new JTextPane();
	private JTextPane otherTags = new JTextPane();
	
	private JLabel lblNonworkingSizes = new JLabel();
	private JButton btnLetsDoThisShit = new JButton("Let's do this! Select pages to upload to!");
	
	public MainWindow() {
		imageInfo.addChangeListener(this);
		FileFilter imageFilter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif");
		fileChooser.setFileFilter(imageFilter);
		fileChooser.setAccessory(new ImagePreview(fileChooser));
		//fileChooser.setFileView(new ImageFileView());
		
		this.setTitle("Furry Crossposter by Double Helix Industries");
		this.setSize(900, 768);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.add(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JLabel artDetails = new JLabel("Artwork Details:");
		artDetails.setFont(artDetails.getFont().deriveFont(24f));
		mainPanel.add(artDetails);
		mainPanel.add(Box.createVerticalStrut(10));
		JLabel sizeNote = new JLabel("Please remember that some sites, like E621, do not resize your image. It will be uploaded as it is.");
		sizeNote.setFont(sizeNote.getFont().deriveFont(Font.PLAIN));
		mainPanel.add(sizeNote);
		mainPanel.add(Box.createVerticalStrut(10));
		
		/**
		 * START OF IMAGE PANEL (FIRST ROW)
		 */
		
		JPanel imagesPanel = new JPanel();
		imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.X_AXIS));
		imagesPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		JPanel mainImagePanel = new JPanel();
		mainImagePanel.setLayout(new BoxLayout(mainImagePanel, BoxLayout.Y_AXIS));
		
		btnSelMainImage.addActionListener(this);
		mainImagePanel.add(btnSelMainImage);
		mainImagePanel.add(Box.createVerticalStrut(10));
		
		imageLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		//imageLabel.setSize(100, 100);
		imageLabel.setMinimumSize(new Dimension(200, 150));
		imageLabel.setPreferredSize(imageLabel.getMinimumSize());
		imageLabel.setMaximumSize(imageLabel.getMinimumSize());
		mainImagePanel.add(imageLabel);
		imagesPanel.add(mainImagePanel);
		imagesPanel.add(Box.createHorizontalStrut(10));
		
		JPanel thumbImagePanel = new JPanel();
		thumbImagePanel.setLayout(new BoxLayout(thumbImagePanel, BoxLayout.Y_AXIS));
		
		btnSelThumbImage.addActionListener(this);
		thumbImagePanel.add(btnSelThumbImage);
		thumbImagePanel.add(Box.createVerticalStrut(10));
		
		thumbLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		//imageLabel.setSize(100, 100);
		thumbLabel.setMinimumSize(new Dimension(200, 150));
		thumbLabel.setPreferredSize(thumbLabel.getMinimumSize());
		thumbLabel.setMaximumSize(thumbLabel.getMinimumSize());
		thumbLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (imageInfo.getThumbPath() != null) {
					thumbLabel.setIcon(null);
					imageInfo.setThumbPath(null);
				}
				super.mouseClicked(e);
			}
		});
		thumbImagePanel.add(thumbLabel);
		imagesPanel.add(thumbImagePanel);
		imagesPanel.add(Box.createHorizontalStrut(10));
		imagesPanel.add(getSeparator(SwingConstants.VERTICAL));
		imagesPanel.add(Box.createHorizontalStrut(10));
		////////////////
		
		// general image info
		typeBox.setModel(new DefaultComboBoxModel<ImageInfo.Type>(ImageInfo.Type.values()));
		typeBox.setSelectedItem(imageInfo.getType());
		typeBox.addActionListener(this);
		ratingSexualBox.setModel(new DefaultComboBoxModel<ImageInfo.RatingSexual>(ImageInfo.RatingSexual.values()));
		ratingSexualBox.setSelectedItem(imageInfo.getSexualRating());
		ratingSexualBox.addActionListener(this);
		ratingViolenceBox.setModel(new DefaultComboBoxModel<ImageInfo.RatingViolence>(ImageInfo.RatingViolence.values()));
		ratingViolenceBox.setSelectedItem(imageInfo.getViolenceRating());
		ratingViolenceBox.addActionListener(this);
		
		double sizeGeneralImageSettings[][] = {{TableLayout.PREFERRED, 10, TableLayout.FILL}, //width
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10,
			TableLayout.PREFERRED}}; // height
		JPanel generalImageSettingsPanel = new JPanel(new TableLayout(sizeGeneralImageSettings));
		//generalImageSettingsPanel.setLayout(new BoxLayout(generalImageSettingsPanel, BoxLayout.Y_AXIS));
		generalImageSettingsPanel.add(new JLabel("Type:"), "0, 0");
		generalImageSettingsPanel.add(typeBox, "2, 0");
		generalImageSettingsPanel.add(new JSeparator(), "0, 2, 2, 2");
		generalImageSettingsPanel.add(new JLabel("Ratings:"), "0, 4, 2, 4");
		generalImageSettingsPanel.add(new JLabel("Nudity:"), "0, 6");
		generalImageSettingsPanel.add(ratingSexualBox, "2, 6");
		generalImageSettingsPanel.add(new JLabel("Violence:"), "0, 8");
		generalImageSettingsPanel.add(ratingViolenceBox, "2, 8");
		
		imagesPanel.add(generalImageSettingsPanel);
		
		imagesPanel.add(Box.createHorizontalGlue());
		imagesPanel.setMaximumSize(new Dimension(imagesPanel.getPreferredSize().width, imagesPanel.getPreferredSize().height));

		mainPanel.add(imagesPanel);
		
		/**
		 * END OF IMAGE PANEL (FIRST ROW)
		 */
		
		/**
		 * START OF IMAGE TITLE/DESCRIPTIONS
		 */
		
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(getSeparator(SwingConstants.HORIZONTAL));
		mainPanel.add(Box.createVerticalStrut(10));

		titleText.getDocument().addDocumentListener(this);
		titleText.setAlignmentX(LEFT_ALIGNMENT);
		titleText.setMaximumSize(new Dimension(500, titleText.getPreferredSize().height) );

		descrText.setText("Uploaded using Furry Crossposter!");
		imageInfo.setDescription(descrText.getText());
		descrText.getDocument().addDocumentListener(this);
		descrText.setWrapStyleWord(true);
		descrText.setLineWrap(true);
		JScrollPane descrScroller = new JScrollPane(descrText);
		//descrScroller.setAlignmentX(LEFT_ALIGNMENT);
		descrScroller.setMaximumSize(new Dimension(descrScroller.getMaximumSize().width, descrText.getPreferredSize().height));
		descrScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel settingsChkPanel = new JPanel();
		settingsChkPanel.setLayout(new BoxLayout(settingsChkPanel, BoxLayout.X_AXIS));
		settingsChkPanel.setAlignmentX(LEFT_ALIGNMENT);
		chkScraps.addActionListener(this);
		settingsChkPanel.add(chkScraps);
		settingsChkPanel.add(Box.createHorizontalStrut(10));
		chkNoNotification.addActionListener(this);
		settingsChkPanel.add(chkNoNotification);
		settingsChkPanel.add(Box.createHorizontalStrut(10));
		chkFriendsOnly.addActionListener(this);
		settingsChkPanel.add(chkFriendsOnly);
		settingsChkPanel.add(Box.createHorizontalStrut(10));
		chkUnlisted.addActionListener(this);
		settingsChkPanel.add(chkUnlisted);
		
		JPanel genderChkPanel = new JPanel();
		genderChkPanel.setLayout(new BoxLayout(genderChkPanel, BoxLayout.X_AXIS));
		genderChkPanel.setAlignmentX(LEFT_ALIGNMENT);
		chkMale.addActionListener(this);
		genderChkPanel.add(chkMale);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		chkFemale.addActionListener(this);
		genderChkPanel.add(chkFemale);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		chkHerm.addActionListener(this);
		genderChkPanel.add(chkHerm);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		chkDickgirl.addActionListener(this);
		genderChkPanel.add(chkDickgirl);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		chkCuntboy.addActionListener(this);
		genderChkPanel.add(chkCuntboy);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		chkMaleherm.addActionListener(this);
		genderChkPanel.add(chkMaleherm);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		chkFtM.addActionListener(this);
		genderChkPanel.add(chkFtM);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		chkMtF.addActionListener(this);
		genderChkPanel.add(chkMtF);
		
		double sizeGeneralImageInfos[][] = {{TableLayout.PREFERRED, 10, TableLayout.FILL}, //width
				{TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; // height
		
		JPanel generalInfoPanel = new JPanel(new TableLayout(sizeGeneralImageInfos));
		generalInfoPanel.setAlignmentX(LEFT_ALIGNMENT);
		generalInfoPanel.add(new JLabel("Title:"), "0, 0");
		generalInfoPanel.add(titleText, "2, 0");
		generalInfoPanel.add(new JLabel("Description:"), new TableLayoutConstraints(0, 2, 0, 2, TableLayout.LEFT, TableLayout.TOP));
		generalInfoPanel.add(descrScroller, "2, 2");
		generalInfoPanel.add(new JLabel("Settings:"), "0, 4");
		generalInfoPanel.add(settingsChkPanel, "2, 4");
		generalInfoPanel.add(new JLabel("Genders:"), "0, 6");
		generalInfoPanel.add(genderChkPanel, "2, 6");
		generalInfoPanel.setMaximumSize(new Dimension(generalInfoPanel.getMaximumSize().width, generalInfoPanel.getPreferredSize().height));
		
		mainPanel.add(generalInfoPanel);
		
		
		
		/**
		 * END OF IMAGE TITLE/DESCRIPTIONS
		 */
		
		/**
		 * START OF TAGS
		 */
		
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(getSeparator(SwingConstants.HORIZONTAL));
		mainPanel.add(Box.createVerticalStrut(10));
		
		mainPanel.add(new JLabel("Tags:"));
		mainPanel.add(Box.createVerticalStrut(5));
		JLabel tagHelpText = new JLabel("<html><body>Words within each tag are separated by underscores. Black tags are allright. " +
				"<font color=\"red\">Red tags</font> means the tags were not recognized by the tag DB, which is except " +
				"for char names generally a bad sign, but it still works. <font color=\"purple\">Purple tags</font> " +
				"means they're in the wrong category.<br>Special \"other\" tag: Begin a tag with # to add it to an existing(!) pool or folder of the same name, like <i>#akhlys_(female_gargoyle)_-_clean</i>.</body></html>");
		tagHelpText.setFont(tagHelpText.getFont().deriveFont(Font.PLAIN));
		mainPanel.add(tagHelpText);
		mainPanel.add(Box.createVerticalStrut(10));
		
		DocumentFilter filter = new DocumentFilter() {
	        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
	            fb.insertString(offset, string.replaceAll("\\n", ""), attr); 
	        }

	       public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {
	            fb.insertString(offset, string.replaceAll("\\n", ""), attr); 
	        }
		};
		
		patchTabFocusChange(speciesTags);
		speciesTags.setContentType("text/html");
		speciesTags.setMaximumSize(new Dimension(speciesTags.getMaximumSize().width, speciesTags.getPreferredSize().height));
		speciesTags.getDocument().addDocumentListener(this);
		((AbstractDocument)speciesTags.getDocument()).setDocumentFilter(filter);
		speciesTags.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "paste");
		speciesTags.getActionMap().put("paste", new PasteAction(speciesTags));
		speciesTags.setBorder(BorderFactory.createEtchedBorder());
		
		patchTabFocusChange(kinkTags);
		kinkTags.setContentType("text/html");
		kinkTags.setMaximumSize(new Dimension(kinkTags.getMaximumSize().width, kinkTags.getPreferredSize().height));
		kinkTags.getDocument().addDocumentListener(this);
		((AbstractDocument)kinkTags.getDocument()).setDocumentFilter(filter);
		kinkTags.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "paste");
		kinkTags.getActionMap().put("paste", new PasteAction(kinkTags));
		kinkTags.setBorder(BorderFactory.createEtchedBorder());
		
		patchTabFocusChange(otherTags);
		otherTags.setContentType("text/html");
		otherTags.setMaximumSize(new Dimension(otherTags.getMaximumSize().width, otherTags.getPreferredSize().height));
		otherTags.getDocument().addDocumentListener(this);
		((AbstractDocument)otherTags.getDocument()).setDocumentFilter(filter);
		otherTags.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "paste");
		otherTags.getActionMap().put("paste", new PasteAction(otherTags));
		otherTags.setBorder(BorderFactory.createEtchedBorder());
		
		double sizeTags[][] = {{TableLayout.PREFERRED, 10, TableLayout.FILL}, //width
				{TableLayout.PREFERRED, 9, TableLayout.PREFERRED, 9, TableLayout.PREFERRED}}; // height
		JPanel pnlTags = new JPanel(new TableLayout(sizeTags));
		pnlTags.setAlignmentX(LEFT_ALIGNMENT);
		pnlTags.add(new JLabel("Species:"), "0, 0");
		pnlTags.add(speciesTags, "2, 0");
		pnlTags.add(new JLabel("Kinks:"), "0, 2");
		pnlTags.add(kinkTags, "2, 2");
		pnlTags.add(new JLabel("Others:"), "0, 4");
		pnlTags.add(otherTags, "2, 4");
		pnlTags.setMaximumSize(new Dimension(pnlTags.getMaximumSize().width, pnlTags.getPreferredSize().height));
		
		mainPanel.add(pnlTags);
		mainPanel.add(Box.createVerticalGlue());
		
		lblNonworkingSizes.setFont(lblNonworkingSizes.getFont().deriveFont(Font.PLAIN));
		mainPanel.add(lblNonworkingSizes);
		mainPanel.add(Box.createVerticalStrut(10));

		btnLetsDoThisShit.addActionListener(this);
		btnLetsDoThisShit.setEnabled(false);
		mainPanel.add(btnLetsDoThisShit);
		
		this.setVisible(true);
	}
	
	/**
	 * 
	 * @param orientation SwingConstants.Horizontal or vertical
	 * @return
	 */
	private JSeparator getSeparator(int orientation) {
		JSeparator j = new JSeparator(orientation);
		if (orientation == SwingConstants.HORIZONTAL) {
			j.setMaximumSize(new Dimension(j.getMaximumSize().width, j.getPreferredSize().height));
			j.setAlignmentY(TOP_ALIGNMENT);
		} else {
			j.setMaximumSize(new Dimension(j.getPreferredSize().width, j.getMaximumSize().height));
			j.setAlignmentX(LEFT_ALIGNMENT);
		}
		return j;
	}
	
	private void patchTabFocusChange(Component c) {
		Set<KeyStroke> strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
        c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
        strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
        c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btnSelMainImage) {
			openImage(false);
		} else if (arg0.getSource() == this.btnSelThumbImage) {
			openImage(true);
		} else if (arg0.getSource() == this.typeBox) {
			ImageInfo.Type type = (ImageInfo.Type)typeBox.getSelectedItem();
			imageInfo.setType(type);
		} else if (arg0.getSource() == this.ratingSexualBox) {
			ImageInfo.RatingSexual rating = (ImageInfo.RatingSexual)ratingSexualBox.getSelectedItem();
			imageInfo.setSexualRating(rating);
		} else if (arg0.getSource() == this.ratingViolenceBox) {
			ImageInfo.RatingViolence rating = (ImageInfo.RatingViolence)ratingViolenceBox.getSelectedItem();
			imageInfo.setViolenceRating(rating);
		} else if (arg0.getSource() == this.chkScraps) {
			imageInfo.setToScraps(chkScraps.isSelected());
		} else if (arg0.getSource() == this.chkNoNotification) {
			imageInfo.setNoNotification(chkNoNotification.isSelected());
		} else if (arg0.getSource() == this.chkFriendsOnly) {
			imageInfo.setFriendsOnly(chkFriendsOnly.isSelected());
		} else if (arg0.getSource() == this.chkUnlisted) {
			imageInfo.setUnlisted(chkUnlisted.isSelected());
		} else if (arg0.getSource() == this.chkMale) {
			imageInfo.setGender(ImageInfo.Gender.MALE, chkMale.isSelected());
		} else if (arg0.getSource() == this.chkFemale) {
			imageInfo.setGender(ImageInfo.Gender.FEMALE, chkFemale.isSelected());
		} else if (arg0.getSource() == this.chkHerm) {
			imageInfo.setGender(ImageInfo.Gender.HERM, chkHerm.isSelected());
		} else if (arg0.getSource() == this.chkDickgirl) {
			imageInfo.setGender(ImageInfo.Gender.DICKGIRL, chkDickgirl.isSelected());
		} else if (arg0.getSource() == this.chkCuntboy) {
			imageInfo.setGender(ImageInfo.Gender.CUNTBOY, chkCuntboy.isSelected());
		} else if (arg0.getSource() == this.chkMaleherm) {
			imageInfo.setGender(ImageInfo.Gender.MALEHERM, chkMaleherm.isSelected());
		} else if (arg0.getSource() == this.chkMtF) {
			imageInfo.setGender(ImageInfo.Gender.M2F, chkMtF.isSelected());
		} else if (arg0.getSource() == this.chkFtM) {
			imageInfo.setGender(ImageInfo.Gender.M2F, chkFtM.isSelected());
		} else if (arg0.getSource() == this.btnLetsDoThisShit) {
			// will only happen if at least one page works
			new UploadDialog(imageInfo, pages);
		}
	}


	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if (arg0.getDocument() == this.titleText.getDocument()) {
			imageInfo.setTitle(this.titleText.getText().trim());
		} else if (arg0.getDocument() == this.descrText.getDocument()) {
			imageInfo.setDescription(this.descrText.getText().trim());
		}
		if (arg0.getDocument() == speciesTags.getDocument() || arg0.getDocument() == kinkTags.getDocument()
				|| arg0.getDocument() == otherTags.getDocument()) {
			
			// get the pane
			JTextPane pane = (arg0.getDocument() == speciesTags.getDocument()) ?  speciesTags : null;
			pane = (pane != null) ? pane : ((arg0.getDocument() == kinkTags.getDocument()) ?  kinkTags : null);
			pane = (pane != null) ? pane : ((arg0.getDocument() == otherTags.getDocument()) ?  otherTags : null);
			
			// derefer all further stuff to thread to be started immediately. SwingUtilities.invokeLater() makes
			// sure it is started right after the change event went through, which is needed to edit the text and
			// for accurate caret positioning
			final JTextPane finalPane = pane;
		    SwingUtilities.invokeLater(new Runnable() {
		        @Override
		        public void run() {
		        	textPaneChanged(finalPane);
		        }
		    });
		}
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}
	
	private void textPaneChanged(JTextPane pane) {
		// tags will be added below again
		if (pane == otherTags) {
			imageInfo.getFolders().clear();
			imageInfo.getOtherTags().clear();
		} else if(pane == speciesTags) {
			imageInfo.getSpeciesTags().clear();
		} else if(pane == kinkTags) {
			imageInfo.getKinkTags().clear();
		}
		
		
		// remove any html
		String text = "";
		try {
			text = pane.getDocument().getText(0, pane.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
			//ignore?
			return;
		}
		int caretPosition = pane.getCaretPosition();
		
		if (text.length() > 100) {
			// too long, make shorter
			text = text.substring(0, 100);
			if (caretPosition > 100) caretPosition = 100;
		}
		
		// tags test
		// note, we try every tag here. Reason is that changing the text using Document.insertString() with styles does
		// not reset the styles afterwards and trying to find the right position if we extract the text as HTML would be
		// awful.
		// jEditorPane sometimes has a newline at the start if we get plain text through the document if it was from html.
		// since we add html again and the same will happen, we do not have to care about caretposition here
		// since we want to preserve spaces, we have to do a regex lookahead/behind
		String[] words = text.replace("\n",  "").split("((?<= )|(?= ))");
		
		StringBuffer newHTMLText = new StringBuffer("<html><body>");
		for (int i = 0; i < words.length; ++i) {
			String word = words[i].toLowerCase();
			String searchWord = word;
			
			if (word.isEmpty()) continue; // when input was filled and deleted again
			if (word.equals(" ")) {
				// skip if previous was a space too, since editorpane in html mode does not add two subsequent spaces
				if (i != 0 && words[i -1].equals(" ")) {
					caretPosition -= 1;
					continue;
				}
				// as it's html, just a space might cause problems
				newHTMLText.append("<font color=\"black\"> </font>");
				continue;
			}
			
			// special tags first, if other field
			if (pane == otherTags) {
				if (word.startsWith("#")) {
					// do not check
					newHTMLText.append(word);
					imageInfo.getFolders().add(word.substring(1));
					continue;
				} else {
					imageInfo.getOtherTags().add(word);
				}
			} else if(pane == speciesTags) {
				imageInfo.getSpeciesTags().add(word);
			} else if(pane == kinkTags) {
				imageInfo.getKinkTags().add(word);
			}
			
			Tag tag = FurryCrossposter.tags.get(searchWord);
			if (tag == null) {
				// unrecognized tag
				newHTMLText.append("<font color=\"red\">").append(word).append("</font>");
			} else {
				// check if tag is in right field
				if ((tag.getType() == Tag.Type.ARTIST && pane != otherTags)
						|| (tag.getType() == Tag.Type.SPECIES && pane != speciesTags)
						|| (tag.getType() != Tag.Type.SPECIES && pane == speciesTags)) {
					newHTMLText.append("<font color=\"purple\">").append(word).append("</font>");
					continue;
				}
				newHTMLText.append(word);
			}
		}
		//newHTMLText.deleteCharAt(newHTMLText.length() - 1);
		newHTMLText.append("</body></html>");
		
		pane.getDocument().removeDocumentListener(this);
		pane.setText(newHTMLText.toString());
		if (text.startsWith("\n")) {
			pane.setCaretPosition(caretPosition);
		} else {
			// editor pane tends to add an (invidible) new line at the start if you insert html, which increases caretpos
			pane.setCaretPosition(caretPosition + 1);
		}
		pane.getDocument().addDocumentListener(this);
	}
	
	private void openImage(boolean thumb) {
		JLabel labelToDisplay = thumb ? thumbLabel : imageLabel;
		
		if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File imageFile = fileChooser.getSelectedFile();
		BufferedImage image = null;
		try {
			image = ImageTools.getResizedInstance(labelToDisplay.getPreferredSize().width, labelToDisplay.getPreferredSize().height, imageFile);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open image: " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Icon imageIcon = new ImageIcon(image);
		
		labelToDisplay.setIcon(imageIcon);
		
		if (thumb) {
			imageInfo.setThumbPath(imageFile.toPath());
		} else {
			imageInfo.setImagePath(imageFile.toPath());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() != imageInfo) return;
		
		StringBuffer sitesNotWorking = new StringBuffer();
		boolean worksOnAtLeastOneSite = false;
		for (Site site : pages) {
			if (!site.canUpload(imageInfo)) {
				sitesNotWorking.append(site.getName()).append(", ");
			} else {
				worksOnAtLeastOneSite = true;
			}
		}
		if (sitesNotWorking.length() > 0) sitesNotWorking.delete(sitesNotWorking.length() - 2, sitesNotWorking.length());
		
		
		if (sitesNotWorking.length() > 0 && worksOnAtLeastOneSite) {
			lblNonworkingSizes.setText("Above config will not work on: " + sitesNotWorking.toString());
		} else {
			lblNonworkingSizes.setText("");
		}
		btnLetsDoThisShit.setEnabled(worksOnAtLeastOneSite);
	}
}

class PasteAction extends AbstractAction {
	private static final long serialVersionUID = -3772760972861482145L;
	JEditorPane thePane = null;

	public PasteAction(JEditorPane thePane) {
		this.thePane = thePane;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			int offset = thePane.getSelectionStart();
			Document sd = thePane.getDocument();
			String value = getClipboard();
			sd.remove(thePane.getSelectionStart(), thePane.getSelectionEnd() - thePane.getSelectionStart());
			thePane.getDocument().insertString(offset, value, null);
			if (value != null) {
				thePane.setCaretPosition(offset + value.length());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getClipboard() throws ClassNotFoundException, UnsupportedFlavorException {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		DataFlavor htmlStringFlavor = new DataFlavor("text/plain; class=java.lang.String");
		try {
			if (t != null && t.isDataFlavorSupported(htmlStringFlavor)) {
				String text = (String) t.getTransferData(htmlStringFlavor);
				return text;
			}
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
