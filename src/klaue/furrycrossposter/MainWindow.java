package klaue.furrycrossposter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
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
import java.util.List;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
import layout.TableLayoutConstants;
import layout.TableLayoutConstraints;

public class MainWindow extends JFrame implements ActionListener, DocumentListener, ChangeListener {
	private static final String windowTitle = "Furry Crossposter by Double Helix Industries - 1.23";
	
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
	private JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
	JLabel thumbLabel = new JLabel("", SwingConstants.CENTER);

	private JComboBox<ImageInfo.Type> typeBox = new JComboBox<>();
	private JComboBox<ImageInfo.RatingSexual> ratingSexualBox = new JComboBox<>();
	private JComboBox<ImageInfo.RatingViolence> ratingViolenceBox = new JComboBox<>();
	
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
		this.imageInfo.addChangeListener(this);
		FileFilter imageFilter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif");
		this.fileChooser.setFileFilter(imageFilter);
		this.fileChooser.setAccessory(new ImagePreview(this.fileChooser));
		//fileChooser.setFileView(new ImageFileView());
		
		this.setTitle(windowTitle);
		this.setSize(900, 768);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
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
		
		this.btnSelMainImage.addActionListener(this);
		mainImagePanel.add(this.btnSelMainImage);
		mainImagePanel.add(Box.createVerticalStrut(10));
		
		this.imageLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		//imageLabel.setSize(100, 100);
		this.imageLabel.setMinimumSize(new Dimension(200, 150));
		this.imageLabel.setPreferredSize(this.imageLabel.getMinimumSize());
		this.imageLabel.setMaximumSize(this.imageLabel.getMinimumSize());
		mainImagePanel.add(this.imageLabel);
		imagesPanel.add(mainImagePanel);
		imagesPanel.add(Box.createHorizontalStrut(10));
		
		JPanel thumbImagePanel = new JPanel();
		thumbImagePanel.setLayout(new BoxLayout(thumbImagePanel, BoxLayout.Y_AXIS));
		
		this.btnSelThumbImage.addActionListener(this);
		thumbImagePanel.add(this.btnSelThumbImage);
		thumbImagePanel.add(Box.createVerticalStrut(10));
		
		this.thumbLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		//imageLabel.setSize(100, 100);
		this.thumbLabel.setMinimumSize(new Dimension(200, 150));
		this.thumbLabel.setPreferredSize(this.thumbLabel.getMinimumSize());
		this.thumbLabel.setMaximumSize(this.thumbLabel.getMinimumSize());
		this.thumbLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (MainWindow.this.imageInfo.getThumbPath() != null) {
					MainWindow.this.thumbLabel.setIcon(null);
					MainWindow.this.imageInfo.setThumbPath(null);
				}
				super.mouseClicked(e);
			}
		});
		thumbImagePanel.add(this.thumbLabel);
		imagesPanel.add(thumbImagePanel);
		imagesPanel.add(Box.createHorizontalStrut(10));
		imagesPanel.add(getSeparator(SwingConstants.VERTICAL));
		imagesPanel.add(Box.createHorizontalStrut(10));
		////////////////
		
		// add drag & drop
		@SuppressWarnings("serial")
		DropTarget imageDropTarget = new ImageDropTarget(imageFilter, this) {
			@Override
			public void fileAccepted(File f) {
				openImage(false, f);
			}
		};
		imagesPanel.setDropTarget(imageDropTarget);
		
		@SuppressWarnings("serial")
		DropTarget thumbDropTarget = new ImageDropTarget(imageFilter, this) {
			@Override
			public void fileAccepted(File f) {
				openImage(true, f);
			}
		};
		thumbImagePanel.setDropTarget(thumbDropTarget);
		
		
		// general image info
		this.typeBox.setModel(new DefaultComboBoxModel<>(ImageInfo.Type.values()));
		this.typeBox.setSelectedItem(this.imageInfo.getType());
		this.typeBox.addActionListener(this);
		this.ratingSexualBox.setModel(new DefaultComboBoxModel<>(ImageInfo.RatingSexual.values()));
		this.ratingSexualBox.setSelectedItem(this.imageInfo.getSexualRating());
		this.ratingSexualBox.addActionListener(this);
		this.ratingViolenceBox.setModel(new DefaultComboBoxModel<>(ImageInfo.RatingViolence.values()));
		this.ratingViolenceBox.setSelectedItem(this.imageInfo.getViolenceRating());
		this.ratingViolenceBox.addActionListener(this);
		
		double sizeGeneralImageSettings[][] = {{TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.FILL}, //width
				{TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 10,
			TableLayoutConstants.PREFERRED}}; // height
		JPanel generalImageSettingsPanel = new JPanel(new TableLayout(sizeGeneralImageSettings));
		//generalImageSettingsPanel.setLayout(new BoxLayout(generalImageSettingsPanel, BoxLayout.Y_AXIS));
		generalImageSettingsPanel.add(new JLabel("Type:"), "0, 0");
		generalImageSettingsPanel.add(this.typeBox, "2, 0");
		generalImageSettingsPanel.add(new JSeparator(), "0, 2, 2, 2");
		generalImageSettingsPanel.add(new JLabel("Ratings:"), "0, 4, 2, 4");
		generalImageSettingsPanel.add(new JLabel("Nudity:"), "0, 6");
		generalImageSettingsPanel.add(this.ratingSexualBox, "2, 6");
		generalImageSettingsPanel.add(new JLabel("Violence:"), "0, 8");
		generalImageSettingsPanel.add(this.ratingViolenceBox, "2, 8");
		
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

		this.titleText.getDocument().addDocumentListener(this);
		this.titleText.setAlignmentX(LEFT_ALIGNMENT);
		this.titleText.setMaximumSize(new Dimension(500, this.titleText.getPreferredSize().height) );

		this.descrText.setText("Uploaded using Furry Crossposter!");
		this.imageInfo.setDescription(this.descrText.getText());
		this.descrText.getDocument().addDocumentListener(this);
		this.descrText.setWrapStyleWord(true);
		this.descrText.setLineWrap(true);
		JScrollPane descrScroller = new JScrollPane(this.descrText);
		//descrScroller.setAlignmentX(LEFT_ALIGNMENT);
		descrScroller.setMaximumSize(new Dimension(descrScroller.getMaximumSize().width, this.descrText.getPreferredSize().height));
		descrScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel settingsChkPanel = new JPanel();
		settingsChkPanel.setLayout(new BoxLayout(settingsChkPanel, BoxLayout.X_AXIS));
		settingsChkPanel.setAlignmentX(LEFT_ALIGNMENT);
		this.chkScraps.addActionListener(this);
		settingsChkPanel.add(this.chkScraps);
		settingsChkPanel.add(Box.createHorizontalStrut(10));
		this.chkNoNotification.addActionListener(this);
		settingsChkPanel.add(this.chkNoNotification);
		settingsChkPanel.add(Box.createHorizontalStrut(10));
		this.chkFriendsOnly.addActionListener(this);
		settingsChkPanel.add(this.chkFriendsOnly);
		settingsChkPanel.add(Box.createHorizontalStrut(10));
		this.chkUnlisted.addActionListener(this);
		settingsChkPanel.add(this.chkUnlisted);
		
		JPanel genderChkPanel = new JPanel();
		genderChkPanel.setLayout(new BoxLayout(genderChkPanel, BoxLayout.X_AXIS));
		genderChkPanel.setAlignmentX(LEFT_ALIGNMENT);
		this.chkMale.addActionListener(this);
		genderChkPanel.add(this.chkMale);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		this.chkFemale.addActionListener(this);
		genderChkPanel.add(this.chkFemale);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		this.chkHerm.addActionListener(this);
		genderChkPanel.add(this.chkHerm);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		this.chkDickgirl.addActionListener(this);
		genderChkPanel.add(this.chkDickgirl);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		this.chkCuntboy.addActionListener(this);
		genderChkPanel.add(this.chkCuntboy);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		this.chkMaleherm.addActionListener(this);
		genderChkPanel.add(this.chkMaleherm);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		this.chkFtM.addActionListener(this);
		genderChkPanel.add(this.chkFtM);
		genderChkPanel.add(Box.createHorizontalStrut(10));
		this.chkMtF.addActionListener(this);
		genderChkPanel.add(this.chkMtF);
		
		double sizeGeneralImageInfos[][] = {{TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.FILL}, //width
				{TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.PREFERRED}}; // height
		
		JPanel generalInfoPanel = new JPanel(new TableLayout(sizeGeneralImageInfos));
		generalInfoPanel.setAlignmentX(LEFT_ALIGNMENT);
		generalInfoPanel.add(new JLabel("Title:"), "0, 0");
		generalInfoPanel.add(this.titleText, "2, 0");
		generalInfoPanel.add(new JLabel("Description:"), new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstants.LEFT, TableLayoutConstants.TOP));
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
	        @Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
	            fb.insertString(offset, string.replaceAll("\\n", ""), attr); 
	        }

	       @Override
	       public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {
	            fb.insertString(offset, string.replaceAll("\\n", ""), attr); 
	        }
		};
		
		patchTabFocusChange(this.speciesTags);
		this.speciesTags.setContentType("text/html");
		this.speciesTags.setMaximumSize(new Dimension(this.speciesTags.getMaximumSize().width, this.speciesTags.getPreferredSize().height));
		this.speciesTags.getDocument().addDocumentListener(this);
		((AbstractDocument)this.speciesTags.getDocument()).setDocumentFilter(filter);
		this.speciesTags.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "paste");
		this.speciesTags.getActionMap().put("paste", new PasteAction(this.speciesTags));
		this.speciesTags.setBorder(BorderFactory.createEtchedBorder());
		
		patchTabFocusChange(this.kinkTags);
		this.kinkTags.setContentType("text/html");
		this.kinkTags.setMaximumSize(new Dimension(this.kinkTags.getMaximumSize().width, this.kinkTags.getPreferredSize().height));
		this.kinkTags.getDocument().addDocumentListener(this);
		((AbstractDocument)this.kinkTags.getDocument()).setDocumentFilter(filter);
		this.kinkTags.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "paste");
		this.kinkTags.getActionMap().put("paste", new PasteAction(this.kinkTags));
		this.kinkTags.setBorder(BorderFactory.createEtchedBorder());
		
		patchTabFocusChange(this.otherTags);
		this.otherTags.setContentType("text/html");
		this.otherTags.setMaximumSize(new Dimension(this.otherTags.getMaximumSize().width, this.otherTags.getPreferredSize().height));
		this.otherTags.getDocument().addDocumentListener(this);
		((AbstractDocument)this.otherTags.getDocument()).setDocumentFilter(filter);
		this.otherTags.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "paste");
		this.otherTags.getActionMap().put("paste", new PasteAction(this.otherTags));
		this.otherTags.setBorder(BorderFactory.createEtchedBorder());
		
		double sizeTags[][] = {{TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.FILL}, //width
				{TableLayoutConstants.PREFERRED, 9, TableLayoutConstants.PREFERRED, 9, TableLayoutConstants.PREFERRED}}; // height
		JPanel pnlTags = new JPanel(new TableLayout(sizeTags));
		pnlTags.setAlignmentX(LEFT_ALIGNMENT);
		pnlTags.add(new JLabel("Species:"), "0, 0");
		pnlTags.add(this.speciesTags, "2, 0");
		pnlTags.add(new JLabel("Kinks:"), "0, 2");
		pnlTags.add(this.kinkTags, "2, 2");
		pnlTags.add(new JLabel("Others:"), "0, 4");
		pnlTags.add(this.otherTags, "2, 4");
		pnlTags.setMaximumSize(new Dimension(pnlTags.getMaximumSize().width, pnlTags.getPreferredSize().height));
		
		mainPanel.add(pnlTags);
		mainPanel.add(Box.createVerticalGlue());
		
		this.lblNonworkingSizes.setFont(this.lblNonworkingSizes.getFont().deriveFont(Font.PLAIN));
		mainPanel.add(this.lblNonworkingSizes);
		mainPanel.add(Box.createVerticalStrut(10));

		this.btnLetsDoThisShit.addActionListener(this);
		this.btnLetsDoThisShit.setEnabled(false);
		mainPanel.add(this.btnLetsDoThisShit);
		
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
		Set<KeyStroke> strokes = new HashSet<>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
        c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
        strokes = new HashSet<>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
        c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btnSelMainImage) {
			openImage(false, null);
		} else if (arg0.getSource() == this.btnSelThumbImage) {
			openImage(true, null);
		} else if (arg0.getSource() == this.typeBox) {
			ImageInfo.Type type = (ImageInfo.Type)this.typeBox.getSelectedItem();
			this.imageInfo.setType(type);
		} else if (arg0.getSource() == this.ratingSexualBox) {
			ImageInfo.RatingSexual rating = (ImageInfo.RatingSexual)this.ratingSexualBox.getSelectedItem();
			this.imageInfo.setSexualRating(rating);
		} else if (arg0.getSource() == this.ratingViolenceBox) {
			ImageInfo.RatingViolence rating = (ImageInfo.RatingViolence)this.ratingViolenceBox.getSelectedItem();
			this.imageInfo.setViolenceRating(rating);
		} else if (arg0.getSource() == this.chkScraps) {
			this.imageInfo.setToScraps(this.chkScraps.isSelected());
		} else if (arg0.getSource() == this.chkNoNotification) {
			this.imageInfo.setNoNotification(this.chkNoNotification.isSelected());
		} else if (arg0.getSource() == this.chkFriendsOnly) {
			this.imageInfo.setFriendsOnly(this.chkFriendsOnly.isSelected());
		} else if (arg0.getSource() == this.chkUnlisted) {
			this.imageInfo.setUnlisted(this.chkUnlisted.isSelected());
		} else if (arg0.getSource() == this.chkMale) {
			this.imageInfo.setGender(ImageInfo.Gender.MALE, this.chkMale.isSelected());
		} else if (arg0.getSource() == this.chkFemale) {
			this.imageInfo.setGender(ImageInfo.Gender.FEMALE, this.chkFemale.isSelected());
		} else if (arg0.getSource() == this.chkHerm) {
			this.imageInfo.setGender(ImageInfo.Gender.HERM, this.chkHerm.isSelected());
		} else if (arg0.getSource() == this.chkDickgirl) {
			this.imageInfo.setGender(ImageInfo.Gender.DICKGIRL, this.chkDickgirl.isSelected());
		} else if (arg0.getSource() == this.chkCuntboy) {
			this.imageInfo.setGender(ImageInfo.Gender.CUNTBOY, this.chkCuntboy.isSelected());
		} else if (arg0.getSource() == this.chkMaleherm) {
			this.imageInfo.setGender(ImageInfo.Gender.MALEHERM, this.chkMaleherm.isSelected());
		} else if (arg0.getSource() == this.chkMtF) {
			this.imageInfo.setGender(ImageInfo.Gender.M2F, this.chkMtF.isSelected());
		} else if (arg0.getSource() == this.chkFtM) {
			this.imageInfo.setGender(ImageInfo.Gender.M2F, this.chkFtM.isSelected());
		} else if (arg0.getSource() == this.btnLetsDoThisShit) {
			// will only happen if at least one page works
			new UploadDialog(this.imageInfo, pages);
		}
	}


	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if (arg0.getDocument() == this.titleText.getDocument()) {
			this.imageInfo.setTitle(this.titleText.getText().trim());
		} else if (arg0.getDocument() == this.descrText.getDocument()) {
			this.imageInfo.setDescription(this.descrText.getText().trim());
		}
		if (arg0.getDocument() == this.speciesTags.getDocument() || arg0.getDocument() == this.kinkTags.getDocument()
				|| arg0.getDocument() == this.otherTags.getDocument()) {
			
			// get the pane
			JTextPane pane = (arg0.getDocument() == this.speciesTags.getDocument()) ?  this.speciesTags : null;
			pane = (pane != null) ? pane : ((arg0.getDocument() == this.kinkTags.getDocument()) ?  this.kinkTags : null);
			pane = (pane != null) ? pane : ((arg0.getDocument() == this.otherTags.getDocument()) ?  this.otherTags : null);
			
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
	
	void textPaneChanged(JTextPane pane) {
		// tags will be added below again
		if (pane == this.otherTags) {
			this.imageInfo.getFolders().clear();
			this.imageInfo.getOtherTags().clear();
		} else if(pane == this.speciesTags) {
			this.imageInfo.getSpeciesTags().clear();
		} else if(pane == this.kinkTags) {
			this.imageInfo.getKinkTags().clear();
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
		
		// if a selection, remove selection from text and set caret to start of selection
		if (pane.getSelectionStart() != pane.getSelectionEnd()) {
			caretPosition = pane.getSelectionStart();
			text = text.substring(0, caretPosition) + text.substring(pane.getSelectionEnd());
		}
		
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
			if (pane == this.otherTags) {
				if (word.startsWith("#")) {
					// do not check
					newHTMLText.append(word);
					this.imageInfo.getFolders().add(word.substring(1));
					continue;
				}
				this.imageInfo.getOtherTags().add(word);
			} else if(pane == this.speciesTags) {
				this.imageInfo.getSpeciesTags().add(word);
			} else if(pane == this.kinkTags) {
				this.imageInfo.getKinkTags().add(word);
			}
			
			Tag tag = FurryCrossposter.tags.get(searchWord);
			if (tag == null) {
				// unrecognized tag
				newHTMLText.append("<font color=\"red\">").append(word).append("</font>");
			} else {
				// check if tag is in right field
				if ((tag.getType() == Tag.Type.ARTIST && pane != this.otherTags)
						|| (tag.getType() == Tag.Type.SPECIES && pane != this.speciesTags)
						|| (tag.getType() != Tag.Type.SPECIES && pane == this.speciesTags)) {
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
	
	void openImage(boolean thumb, File imageFile) {
		JLabel labelToDisplay = thumb ? this.thumbLabel : this.imageLabel;
		
		if (imageFile == null) {
			if (this.fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			imageFile = this.fileChooser.getSelectedFile();
		}
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
			this.imageInfo.setThumbPath(imageFile.toPath());
		} else {
			this.imageInfo.setImagePath(imageFile.toPath());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() != this.imageInfo) return;
		
		StringBuffer sitesNotWorking = new StringBuffer();
		boolean worksOnAtLeastOneSite = false;
		for (Site site : pages) {
			if (!site.canUpload(this.imageInfo)) {
				sitesNotWorking.append(site.getName()).append(", ");
			} else {
				worksOnAtLeastOneSite = true;
			}
		}
		if (sitesNotWorking.length() > 0) sitesNotWorking.delete(sitesNotWorking.length() - 2, sitesNotWorking.length());
		
		
		if (sitesNotWorking.length() > 0 && worksOnAtLeastOneSite) {
			this.lblNonworkingSizes.setText("Above config will not work on: " + sitesNotWorking.toString());
		} else {
			this.lblNonworkingSizes.setText("");
		}
		this.btnLetsDoThisShit.setEnabled(worksOnAtLeastOneSite);
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
			int offset = this.thePane.getSelectionStart();
			Document sd = this.thePane.getDocument();
			String value = getClipboard();
			sd.remove(this.thePane.getSelectionStart(), this.thePane.getSelectionEnd() - this.thePane.getSelectionStart());
			this.thePane.getDocument().insertString(offset, value, null);
			if (value != null) {
				this.thePane.setCaretPosition(offset + value.length());
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
