package klaue.furrycrossposter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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
import javax.swing.WindowConstants;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * this class will be called if no user data was found, it is responsible to get the firefox profile and tags from e621
 * @author klaue
 *
 */
public class FirstStart extends JDialog implements WindowListener {
	private static final long serialVersionUID = 1L;
	Path workingDirectory = null;
	
	File chromeProfileDir = null;
	TreeSet<Tag> imageTags = new TreeSet<>();
	boolean imageTagsDownloading = false;
	private JButton closeButton = new JButton("Finished!");
	boolean saved = false;
	
	public static int MIN_TAGCOUNT = 10;
	
	public FirstStart(Path p) {
		this.workingDirectory = p;
		
		this.setTitle("Furry Crossposter - First Start");
		this.setSize(700, 600);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.setModal(true);
		
		this.closeButton.setEnabled(false);
		
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
				Path chromeProfilePath = FirstStart.this.workingDirectory.resolve("chrome").resolve("furry_crossposter_profile");
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
				options.addArguments("--start-maximized");
				WebDriver webDriver = new ChromeDriver(options);
				webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
				webDriver.get("https://www.google.com");
				((JavascriptExecutor)webDriver).executeScript("alert('Furry Crossposter - chrome profile set up');");
				
				profileName.setText(chromeProfilePath.getFileName().toString());
				FirstStart.this.chromeProfileDir = chromeProfilePath.toFile();
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
				FirstStart.this.imageTagsDownloading = true;
				//downloadTags
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean completedSuccessfully = false;
						try {
							for (int i = 1; i <= 100; ++i) {
								downloadTags.setText("Downloading page " + i + ", please wait..");
								System.out.println("Downloading page " + i);
								ArrayList<Tag> tags = downloadTag(i, FirstStart.this.workingDirectory, MIN_TAGCOUNT);
								FirstStart.this.imageTags.addAll(tags);
								if (tags.isEmpty() || tags.get(tags.size() - 1).getCount() < MIN_TAGCOUNT) {
									// should have been excluded anyway
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
						
						FirstStart.this.imageTagsDownloading = false;
						if (!completedSuccessfully) {
							FirstStart.this.imageTags.clear();
							downloadTags.setEnabled(true);
							downloadTags.setText("Try again to download tags from e621.net");
						} else {
							downloadTags.setText("Downloaded " + FirstStart.this.imageTags.size() + " tags successfully! :D");
							setCloseButton();
						}
					}
				}).start();
				
			}
		});
		mainPanel.add(downloadTags);
		mainPanel.add(Box.createVerticalStrut(10));
		

		this.closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (FirstStart.this.imageTags.size() == 0 || (FirstStart.this.chromeProfileDir != null && (!FirstStart.this.chromeProfileDir.exists() || !FirstStart.this.chromeProfileDir.isDirectory()))) return;
				
				// save ff profile dir
				Properties props = new Properties();
				if (FirstStart.this.chromeProfileDir != null) {
					props.setProperty("ProfileFolder", FirstStart.this.chromeProfileDir.getAbsolutePath());
				} else {
					props.setProperty("ProfileFolder", "generic");
				}
		        File f = FirstStart.this.workingDirectory.resolve("FurryCrossposter.properties").toFile();
		        if (f.exists()) f.delete();
		        try (OutputStream out = new FileOutputStream(f)) {
		        	props.store(out, "Furry Crossposter Settings");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not save settings file: " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
					return;
				}
		        
		        // save tags
		        f = FirstStart.this.workingDirectory.resolve("tags").toFile();
		        if (f.exists()) f.delete();
		        try (OutputStream out = new FileOutputStream(f)) {
		        	try (ObjectOutputStream oout = new ObjectOutputStream(out)) {
		        		oout.writeObject(FirstStart.this.imageTags);
		        	}
		        } catch (IOException e) {
		        	f.delete();
		        	e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not save settings file: " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
					return;
				}
		        
		        FirstStart.this.saved = true;
		        FirstStart.this.dispose();
			}
		});
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(this.closeButton);
		
		this.setVisible(true);
	}
	
	ArrayList<Tag> downloadTag(int page, Path workingDir, int minTagCount) throws IOException {
		Path json = workingDir.resolve("tag.json");
		if (Files.exists(json)) Files.delete(json);
		
		URL website = new URL("https://e621.net/tags.json?limit=1000&search[order]=count&page=" + page);
		URLConnection c = website.openConnection();
		c.setRequestProperty("User-Agent", "FurryCrossposter (by Klaue on e621)");
		try (InputStream is = c.getInputStream()) {
			Files.copy(is, json);
		}
		
	    // parse
  		ArrayList<Tag> tagList = new ArrayList<>();
  		JsonElement rootElem = null;
  		try(FileReader reader = new FileReader(json.toFile())) {
  			rootElem = new JsonParser().parse(reader);
  		}
		JsonArray rootArr = rootElem.getAsJsonArray();
		for (JsonElement elem : rootArr) {
			JsonObject curObj = elem.getAsJsonObject();
			//{"id":12054,"name":"mammal","post_count":1468092,"related_tags":"mammal 300 anthro 203 male 176 female 174 hi_res 154 clothing 148 solo 127 hair 121 fur 119 breasts 113 canid 112 canine 112 duo 107 penis 104 nipples 90 genitals 89 bodily_fluids 83 clothed 81 nude 80 blush 74 sex 74 simple_background 72 erection 68 video_games 67 felid 65","related_tags_updated_at":"2020-05-05T01:15:34.306-04:00","category":5,"is_locked":false,"created_at":"2020-03-05T05:49:37.994-05:00","updated_at":"2020-05-05T01:15:34.306-04:00"}
			JsonElement nameEle = curObj.get("name");
			JsonElement typeEle = curObj.get("category");
			JsonElement countEle = curObj.get("post_count");
			JsonElement idEle = curObj.get("id");
			
			// only save tag if all fields are set. Tag with ID 148522 for example has no name
			if (nameEle == null || typeEle == null || countEle == null || idEle == null) continue;
			
			String name = nameEle.getAsString();
			int type = typeEle.getAsInt();
			int count = countEle.getAsInt();
			int id = idEle.getAsInt();
			
			if (minTagCount > count) continue;
			
			tagList.add(new Tag(name, type, count, id));
		}
  		
  		Files.delete(json);
  		
		return tagList;
	}
	
	void setCloseButton() {
		this.closeButton.setEnabled(this.imageTags.size() > 0 && !this.imageTagsDownloading && (this.chromeProfileDir == null || this.chromeProfileDir.exists()));
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		if (!this.saved) {
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