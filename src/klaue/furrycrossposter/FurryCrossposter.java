package klaue.furrycrossposter;

import io.github.bonigarcia.wdm.ChromeDriverManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * the main and start class of furry crossposter
 * @author klaue
 *
 */
public class FurryCrossposter extends JFrame {
	private static final long serialVersionUID = 8633446169040040976L;
	public static Path workingDir = Paths.get(System.getProperty("user.home") + "/.FurryCrossposter/");
	public static Path chromeProfile = null;
	public static TreeMap<String, Tag> tags = null;
	
	/**
	 * sets up furry crossposter
	 */
	public FurryCrossposter() {
	    try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    }
	    catch(Exception ex) {
	        ex.printStackTrace();
	    }
		
		final SplashScreen splash = new SplashScreen();
		
		new Thread(splash).start();
		
		gatherData(splash);
		new MainWindow();
		
		splash.stop();
	}
	
	/**
	 * tries to gather the data from the user directory or starts the first run wizard if none around
	 */
	@SuppressWarnings("unchecked")
	private void gatherData(SplashScreen splash) {
		// if it's there but a file, delete it
		if (Files.exists(workingDir) && !Files.isDirectory(workingDir)) {
			try {
				Files.delete(workingDir);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, workingDir.toString() + " exists but is not a directory and can't be deleted, please delete it manually and restart FurryCrossposter", "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
		
		// if it's not there or was a file and therefore deleted, make it
		if (!Files.exists(workingDir)) {
			try {
				Files.createDirectory(workingDir);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Could not create " + workingDir.toString(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
		
		// now that we have a working dir, download latest driver
		System.setProperty("wdm.targetPath", workingDir.toString());
		ChromeDriverManager.getInstance().setup();
		
		// if it doesn't contain the tag file, first start wizard did not run
		if (!Files.exists(workingDir.resolve("tags"))) {
			// data dir does not exist or is empty, first start wizard
			splash.stop();
			new FirstStart(workingDir);
		}
		
		TreeSet<Tag> tagsSet = new TreeSet<Tag>();
		try (FileInputStream fis = new FileInputStream(workingDir.resolve("tags").toString())) {
			try (ObjectInputStream ois = new ObjectInputStream(fis)) {
				tagsSet = (TreeSet<Tag>)ois.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open " + workingDir.resolve("tags") + ", " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		// reform to map for easier searching
		FurryCrossposter.tags = new TreeMap<String, Tag>();
		for (Tag tag : tagsSet) {
			FurryCrossposter.tags.put(tag.getName(), tag);
		}
		
		System.out.println("loaded " + FurryCrossposter.tags.size() + " tags");
		
		Properties probs = new Properties();
		try (InputStream in = new FileInputStream(workingDir.resolve("FurryCrossposter.properties").toString())) {
			probs.load(in);
			String ffprofile = probs.getProperty("ProfileFolder");
			if (ffprofile.equals("generic")) {
				FurryCrossposter.chromeProfile = null;
			} else {
				FurryCrossposter.chromeProfile = Paths.get(probs.getProperty("ProfileFolder"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open " + workingDir.resolve("FurryCrossposter.properties") + ", " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		if (FurryCrossposter.chromeProfile != null && (!Files.exists(FurryCrossposter.chromeProfile) || !Files.isDirectory(FurryCrossposter.chromeProfile))) {
			JOptionPane.showMessageDialog(null, "Chrome profile folder " + FurryCrossposter.chromeProfile + " not found, please delete content of " + workingDir + " to run first start wizard again", "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (FurryCrossposter.chromeProfile == null) {
			System.out.println("using generic chrome profile");
		} else {
			System.out.println("loaded chrome profile path " + FurryCrossposter.chromeProfile);
		}
	}
	
	public static void main(String[] args) {
		new FurryCrossposter();
	}
}

