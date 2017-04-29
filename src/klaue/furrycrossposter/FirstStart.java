package klaue.furrycrossposter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * this class will be called if no user data was found, it is responsible to get the firefox profile and tags from e621
 * @author klaue
 *
 */
public class FirstStart extends JDialog implements WindowListener {
	private static final long serialVersionUID = 1L;
	private Path workingDirectory = null;
	
	private File chromeProfileDir = null;
	private TreeSet<Tag> imageTags = new TreeSet<Tag>();
	private boolean imageTagsDownloading = false;
	private JButton closeButton = new JButton("Finished!");
	private boolean saved = false;
	
	public static int MIN_TAGCOUNT = 10;
	
	public FirstStart(Path p) {
		workingDirectory = p;
		
		this.setTitle("Furry Crossposter - First Start");
		this.setSize(700, 600);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.setModal(true);
		
		closeButton.setEnabled(false);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.add(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(new JLabel("<html><body>Heya! It seems this is the first time you start FurryCrossposter. There's a few things you " +
				"have to do before you can use FurryCrossposter. Shouldn't take too long :)</body></html>"));
		mainPanel.add(Box.createVerticalStrut(10));
		
		JLabel browser = new JLabel("Browser:");
		browser.setFont(browser.getFont().deriveFont(24f));
		mainPanel.add(browser);
		mainPanel.add(Box.createVerticalStrut(10));

		JLabel lbl = new JLabel("<html><body>First, a new chrome profile has to be set up for FurryCrossposter to use. You can skip that, but then you need to manually put in your passwords all the time as they can't be saved. To make one, just hit the button below and a new chrome profile will be generated and the browser will open if you want to log in everywhere now, but that can be done later as well, in that case, just close it again.</body></html>");
		Font font = lbl.getFont().deriveFont(Font.PLAIN);
		lbl.setFont(font);
		mainPanel.add(lbl);
		mainPanel.add(Box.createVerticalStrut(10));
		
		JPanel pnlSelProfile = new JPanel();
		pnlSelProfile.setLayout(new BoxLayout(pnlSelProfile, BoxLayout.X_AXIS));
		final JLabel profileName = new JLabel("                                     ");
		JButton genProfile = new JButton("Generate profile");
		genProfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Path chromeProfilePath = workingDirectory.resolve("chrome").resolve("furry_crossposter_profile");
				try {
					Files.createDirectories(chromeProfilePath);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not generate directory for chrome profile, " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ChromeOptions options = new ChromeOptions();
				String profileDir = chromeProfilePath.getParent().toString().replace("\\", "/");
				options.addArguments("user-data-dir=" + profileDir);
				options.addArguments("profile-directory=" + chromeProfilePath.getFileName().toString());
				//options.addArguments("--start-maximized");
				options.addArguments("--no-sandbox");
				DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
				desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
				if (FurryCrossposter.driverSVC == null) {
					FurryCrossposter.driverSVC = ChromeDriverService.createDefaultService();
				}
				WebDriver webDriver = new ChromeDriver(FurryCrossposter.driverSVC,desiredCapabilities);
				webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
				webDriver.get("https://www.google.com");
				((JavascriptExecutor)webDriver).executeScript("alert('Furry Crossposter - chrome profile set up');");
				
				profileName.setText(chromeProfilePath.getFileName().toString());
				chromeProfileDir = chromeProfilePath.toFile();
			}
		});
		pnlSelProfile.add(genProfile);
		pnlSelProfile.add(Box.createHorizontalStrut(10));
		pnlSelProfile.add(profileName);
		pnlSelProfile.add(Box.createHorizontalGlue());
		pnlSelProfile.setMaximumSize(new Dimension(pnlSelProfile.getMaximumSize().width, pnlSelProfile.getMinimumSize().height));
		pnlSelProfile.setAlignmentX(browser.getAlignmentX());
		mainPanel.add(pnlSelProfile);
		mainPanel.add(Box.createVerticalStrut(10));
		
		JLabel tags = new JLabel("Tags:");
		tags.setFont(tags.getFont().deriveFont(24f));
		mainPanel.add(tags);
		mainPanel.add(Box.createVerticalStrut(10));
		
		lbl = new JLabel("<html><body>Now, you need to download some tags. They will be provided by e621.net, and will be used later to determine if a tag is a species etc. To not strain e621 too much, tags with less than 10 images (or after 100 pages) will be ignored.</body></html>");
		lbl.setFont(font);
		mainPanel.add(lbl);
		mainPanel.add(Box.createVerticalStrut(10));
		final JButton downloadTags = new JButton("Download tags from e621.net");
		downloadTags.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				downloadTags.setEnabled(false);
				imageTagsDownloading = true;
				//downloadTags
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean completedSuccessfully = false;
						try {
							for (int i = 1; i <= 100; ++i) {
								downloadTags.setText("Downloading page " + i + ", please wait..");
								System.out.println("Downloading page " + i);
								ArrayList<Tag> tags = downloadTag(i, workingDirectory);
								imageTags.addAll(tags);
								if (tags.get(tags.size() - 1).getCount() < MIN_TAGCOUNT) {
									// it doesn't matter if the current list of tags contains some with less than 10
									break;
								}
								
								// give e621 some time to breathe
								try {Thread.sleep(100);} catch (Exception e){/*ignore*/}
							}
							completedSuccessfully = true;
							
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "Could not download tags, " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
						}
						
						imageTagsDownloading = false;
						if (!completedSuccessfully) {
							imageTags.clear();
							downloadTags.setEnabled(true);
							downloadTags.setText("Try again to download tags from e621.net");
						} else {
							downloadTags.setText("Downloaded " + imageTags.size() + " tags successfully! :D");
							setCloseButton();
						}
					}
				}).start();
				
			}
		});
		mainPanel.add(downloadTags);
		mainPanel.add(Box.createVerticalStrut(10));
		

		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (imageTags.size() == 0 || (chromeProfileDir != null && (!chromeProfileDir.exists() || !chromeProfileDir.isDirectory()))) return;
				
				// save ff profile dir
				Properties props = new Properties();
				if (chromeProfileDir != null) {
					props.setProperty("ProfileFolder", chromeProfileDir.getAbsolutePath());
				} else {
					props.setProperty("ProfileFolder", "generic");
				}
		        File f = workingDirectory.resolve("FurryCrossposter.properties").toFile();
		        if (f.exists()) f.delete();
		        try (OutputStream out = new FileOutputStream(f)) {
		        	props.store(out, "Furry Crossposter Settings");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not save settings file: " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
					return;
				}
		        
		        // save tags
		        f = workingDirectory.resolve("tags").toFile();
		        if (f.exists()) f.delete();
		        try (OutputStream out = new FileOutputStream(f)) {
		        	try (ObjectOutputStream oout = new ObjectOutputStream(out)) {
		        		oout.writeObject(imageTags);
		        	}
		        } catch (IOException e) {
		        	f.delete();
		        	e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not save settings file: " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
					return;
				}
		        
		        saved = true;
		        FirstStart.this.dispose();
			}
		});
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(closeButton);
		
		this.setVisible(true);
	}
	
	private ArrayList<Tag> downloadTag(int page, Path workingDir) throws ParserConfigurationException, SAXException, IOException {
		Path xml = workingDir.resolve("tag.xml");
		if (Files.exists(xml)) Files.delete(xml);
		
		URL website = new URL("https://e621.net/tag/index.xml?limit=1000&order=count&page=" + page);
		URLConnection c = website.openConnection();
		c.setRequestProperty("User-Agent", "FurryCrossposter");
		ReadableByteChannel rbc = Channels.newChannel(c.getInputStream());
		FileOutputStream fos = new FileOutputStream(xml.toString());
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	    
	    // parse
  		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  		DocumentBuilder db = dbf.newDocumentBuilder();
  		Document dom = db.parse(xml.toFile());//"https://e621.net/tag/index.xml?limit=1000&order=count&page=" + page);
  		Element docEle = dom.getDocumentElement();
  		NodeList nl = docEle.getElementsByTagName("tag");
  		
  		ArrayList<Tag> tagList = new ArrayList<Tag>();
  		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element tagEl = (Element)nl.item(i);
				

				Node nameNode = tagEl.getElementsByTagName("name").item(0);
				Node typeNode = tagEl.getElementsByTagName("type").item(0);
				Node countNode = tagEl.getElementsByTagName("count").item(0);
				Node idNode = tagEl.getElementsByTagName("id").item(0);
				
				// only save tag if all fields are set. Tag with ID 148522 for example has no name
				if (!nameNode.hasChildNodes() || !typeNode.hasChildNodes() || !countNode.hasChildNodes()
						|| !idNode.hasChildNodes()) continue;
				
				String name = nameNode.getFirstChild().getNodeValue();
				int type = Integer.parseInt(typeNode.getFirstChild().getNodeValue());
				int count = Integer.parseInt(countNode.getFirstChild().getNodeValue());
				int id = Integer.parseInt(idNode.getFirstChild().getNodeValue());
				tagList.add(new Tag(name, type, count, id));
			}
		}
  		
  		Files.delete(xml);
  		
		return tagList;
	}
	
	private void setCloseButton() {
		closeButton.setEnabled(imageTags.size() > 0 && !imageTagsDownloading && (chromeProfileDir == null || chromeProfileDir.exists()));
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		if (!saved) {
			int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to abort? FurryCrossposter will exit.", "FurryCrossposter", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				System.exit(ABORT);
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
}
