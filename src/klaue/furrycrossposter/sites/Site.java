package klaue.furrycrossposter.sites;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import klaue.furrycrossposter.FurryCrossposter;
import klaue.furrycrossposter.ImageInfo;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

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
	
	protected WebDriver getDriver(){
		return getDriver(DesiredCapabilities.chrome());
	}
	
	protected WebDriver getDriver(DesiredCapabilities desiredCapabilities) {
		if (FurryCrossposter.chromeProfile != null) {
			ChromeOptions options = new ChromeOptions();
			String profileDir = FurryCrossposter.chromeProfile.getParent().toString().replace("\\", "/");
			options.addArguments("user-data-dir=" + profileDir);
			options.addArguments("profile-directory=" + FurryCrossposter.chromeProfile.getFileName().toString());
			options.addArguments("--start-maximized");
			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		}
		WebDriver driver = new ChromeDriver(desiredCapabilities);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		return driver;
	}
	
	protected void showFinishMessage(WebDriver driver) {
		((JavascriptExecutor)driver).executeScript("alert('Furry Crossposter finished - close browser window when you confirmed everything');");
	}
}
