package klaue.furrycrossposter.sites;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import klaue.furrycrossposter.FurryCrossposter;
import klaue.furrycrossposter.ImageInfo;

public abstract class Site {
	/**
	 * upload the given image
	 * @param chromeProfile the firefox profile to use
	 * @param imageInfo the image info
	 * @return true if uploaded
	 */
	public abstract boolean doUpload(ImageInfo imageInfo);
	
	/**
	 * gets the page name
	 * @return the page name
	 */
	public abstract String getName();
	
	/**
	 * Check if the site can upload the given image
	 * @param imageInfo
	 * @param tags the tags from the tag database, to crosscheck
	 * @return true if yes
	 */
	public boolean canUpload(ImageInfo imageInfo) {
		return getErrorReasons(imageInfo).size() == 0;
	}
	
	/**
	 * Get the reasons why stuff might not be ideal on the site (like lots of unrecognized tags)
	 * Only call this if getErrorReasons didn't report any
	 * @param imageInfo
	 * @param tags the tags from the tag database, to crosscheck
	 * @return the reasons
	 */
	public abstract ArrayList<String> getWarningReasons(ImageInfo imageInfo);
	
	/**
	 * Get the reasons why stuff will not work on the site (like adult stuff on dA)
	 * @param imageInfo
	 * @param tags the tags from the tag database, to crosscheck
	 * @return the reasons
	 */
	public abstract ArrayList<String> getErrorReasons(ImageInfo imageInfo);
	
	protected WebDriver getDriver() {
		return getDriver(DesiredCapabilities.chrome());
	}
	
	protected WebDriver getDriver(DesiredCapabilities desiredCapabilities){
		if (FurryCrossposter.chromeProfile != null) {
			ChromeOptions options = new ChromeOptions();
			String profileDir = FurryCrossposter.chromeProfile.getParent().toString().replace("\\", "/");
			options.addArguments("user-data-dir=" + profileDir);
			options.addArguments("profile-directory=" + FurryCrossposter.chromeProfile.getFileName().toString());
			//options.addArguments("--start-maximized");
			options.addArguments("--no-sandbox");
			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		}
		if (FurryCrossposter.driverSVC == null) {
			FurryCrossposter.driverSVC = ChromeDriverService.createDefaultService();
		}
		WebDriver driver = new ChromeDriver(FurryCrossposter.driverSVC,desiredCapabilities);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		return driver;
	}
	
	protected void showFinishMessage(WebDriver driver) {
		((JavascriptExecutor)driver).executeScript("alert('Furry Crossposter finished - close browser window when you confirmed everything');");
	}
	
	/**
	 * Returns the user/Password from the properties. If properties don't exist or have no user/password, asks the user for it and
	 * lets them save it to the given properties file
	 * @param propertiesPath the path to save or load the user/pass from or to
	 * @return a string array with user and pass, or null if user aborted.
	 */
	protected String[] getUserPassword(Path propertiesPath) {
		String user = null;
		String password = null;
		
		// try to get saved passes
		Properties properties = new Properties();
		if (Files.exists(propertiesPath)) {
			try (InputStream in = new FileInputStream(propertiesPath.toString())) {
				properties.load(in);
				user = properties.getProperty("user");
				password = properties.getProperty("password");
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Could not open " + getName() + " properties, " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		// properties did not exist or has no user/password
		if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
			// ask user for it
			StringUtils.isNotBlank("");
			String[] loginValues = null;
			// loop until the user aborted or put in all info
			do {
				loginValues = askForUserPass();
			} while (loginValues != null && (StringUtils.isBlank(loginValues[0]) || StringUtils.isBlank(loginValues[1])));
			if (loginValues == null) return null;
			user = loginValues[0];
			password = loginValues[1];
			
			if (Boolean.parseBoolean(loginValues[2])) {
				// save to properties
				properties.setProperty("user", user);
				properties.setProperty("password", password);
				
				File f = propertiesPath.toFile();
		        try (OutputStream out = new FileOutputStream(f)) {
		        	properties.store(out, getName() + " Settings");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not save credentials for site " + getName() + ": " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		return new String[]{user, password};
	}
	
	protected void removeUserPasswordFromProperties(Path propertiesPath) {
		Properties properties = new Properties();
		if (Files.exists(propertiesPath)) {
			try (InputStream in = new FileInputStream(propertiesPath.toString())) {
				properties.load(in);
				properties.remove("user");
				properties.remove("password");
				File f = propertiesPath.toFile();
		        try (OutputStream out = new FileOutputStream(f)) {
		        	properties.store(out, getName() + " Settings");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not remove credentials for site " + getName(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {
				e.printStackTrace();
				//JOptionPane.showMessageDialog(null, "Could not open " + getName() + " properties, " + e.getMessage(), "FurryCrossposter", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * ask the user for username password and gives him the option to "remember" it
	 * @return a String array consisting of username, password, remember value, or null if it was aborted. It is not checked if user/pass are empty
	 */
	protected String[] askForUserPass() {
	    JPanel panel = new JPanel(new BorderLayout(5, 5));

	    panel.add(new JLabel("Please enter your " + getName() + " User/Password"), BorderLayout.NORTH);
	    
	    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
	    label.add(new JLabel("Username", SwingConstants.RIGHT));
	    label.add(new JLabel("Password", SwingConstants.RIGHT));
	    panel.add(label, BorderLayout.WEST);

	    JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
	    JTextField username = new JTextField();
	    controls.add(username);
	    JPasswordField password = new JPasswordField();
	    controls.add(password);
	    panel.add(controls, BorderLayout.CENTER);
	    
	    JCheckBox saveIt = new JCheckBox("Remember this (saved plaintext to .FurryCrossposter in your home dir)");
	    panel.add(saveIt, BorderLayout.SOUTH);

	    //JOptionPane.showMessageDialog(null, panel, "login", JOptionPane.QUESTION_MESSAGE);
	    int answer = JOptionPane.showConfirmDialog(null, panel, "Login to " + getName(), JOptionPane.OK_CANCEL_OPTION);

	    if (answer != JOptionPane.OK_OPTION) {
	    	return null;
	    }
	    
	    return new String[]{username.getText(), new String(password.getPassword()), Boolean.toString(saveIt.isSelected())};
	}
}
