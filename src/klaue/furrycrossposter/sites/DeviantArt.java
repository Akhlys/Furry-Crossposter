package klaue.furrycrossposter.sites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import klaue.furrycrossposter.ImageInfo;
import klaue.furrycrossposter.ImageInfo.Gender;
import klaue.furrycrossposter.ImageInfo.RatingSexual;
import klaue.furrycrossposter.ImageInfo.RatingViolence;
import klaue.furrycrossposter.ImageInfo.Type;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.ElementScrollBehavior;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DeviantArt extends Site {
	private WebDriver driver;
	
	@Override
	public boolean doUpload(ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;
		Path imagePath = imageInfo.getImagePath();
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
		desiredCapabilities.setCapability(CapabilityType.ELEMENT_SCROLL_BEHAVIOR, ElementScrollBehavior.BOTTOM);
		driver = getDriver(desiredCapabilities);
		
		
		driver.get("https://www.deviantart.com/users/login");
		
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("oh-menu-deviant")));
		
		// open iframe content
		driver.get("http://www.deviantart.com/submit/deviation");//?no-overhead=1");
		driver.switchTo().frame(driver.findElement(By.name("deviation-0")));

		WebElement hiddenField = driver.findElement(By.name("file"));
		if (driver instanceof JavascriptExecutor) {
		    ((JavascriptExecutor)driver).executeScript("arguments[0].className = ''; arguments[0].parentNode.style.height='180px';", hiddenField);
		} else {
			// the normal driver we use can use JS, but let's be sure
		    throw new IllegalStateException("This driver does not support JavaScript!");
		}
		hiddenField.sendKeys(imagePath.toString());
		
		// wait until thumb is loaded
//		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.tt-fh-tc > span.shadow.mild > span.thumb.ismature > img")));
		
		// rating
		if (imageInfo.getSexualRating() != RatingSexual.NONE || imageInfo.getViolenceRating() != RatingViolence.NONE) {
			driver.findElement(By.xpath("//input[@name='ile-mature-input' and @value='yes']")).click();
			
			// get if mature or explicit
			if (imageInfo.getSexualRating() == RatingSexual.NUDITY_EX || imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_EX) {
				driver.findElement(By.className("ile-mature-strict")).click();
			} else {
				// not ex, not none
				driver.findElement(By.className("ile-mature-moderate")).click();
			}
			
			
			// find all checkboxes because fucking DA did not set the text as checkbox value D:
			TreeMap<String, WebElement> checkboxes = new TreeMap<>();
			List<WebElement> labels = driver.findElement(By.className("matureSubOptions")).findElements(By.tagName("label"));
			for (WebElement label : labels) {
				String text = label.getText().toLowerCase(); // first text node, not full innerhtml
				WebElement checkbox = label.findElement(By.tagName("input"));
				
				// deselect all, because dA seems to randomly save selected
				if (checkbox.isSelected()) checkbox.click();
				
				checkboxes.put(text, checkbox);
			}
			
			if (imageInfo.getSexualRating() == RatingSexual.NUDITY_MOD) {
				checkboxes.get("nudity").click();
			} else if (imageInfo.getSexualRating() == RatingSexual.NUDITY_EX) {
				// since we set above if it's ex or not, we could also set nudity here, but.. eeeeh
				checkboxes.get("sexual themes").click();
			}
			
			if (imageInfo.getViolenceRating() != RatingViolence.NONE) {
				if (!checkboxes.get("violence/gore").isSelected()) checkboxes.get("violence/gore").click();
			}
			
		} else {
			driver.findElement(By.xpath("//input[@name='ile-mature-input' and @value='no']")).click();
		}
		
		driver.findElement(By.id("devtitle")).clear();
		
		if (!imageInfo.getTitle().isEmpty()) {
			driver.findElement(By.id("devtitle")).sendKeys(imageInfo.getTitle());
		}
		
		if (!imageInfo.getDescription().isEmpty()) {
			WebElement input = driver.findElement(By.cssSelector("[id^=writer][id$=-writer]"));
			input.click();
			input.sendKeys(imageInfo.getDescription());
		}
		
		// theme
		{
			WebElement form = driver.findElement(By.id("modal-form-category"));
			form.findElement(By.xpath(".//a[@menuri='artsubmit/Anthro']")).click();
			if (imageInfo.getType() == Type.TRADITIONAL) {
				form.findElement(By.xpath(".//a[@menuri='artsubmit/Anthro/Traditional Media']")).click();
				form.findElement(By.xpath(".//a[@menuri='artsubmit/Anthro/Traditional Media/Drawings']")).click();
				form.findElement(By.xpath(".//a[@menuri='artid/3915']")).click(); // animal
			} else {
				// digital, sketch
				form.findElement(By.xpath(".//a[@menuri='artsubmit/Anthro/Digital Media']")).click();
				form.findElement(By.xpath(".//a[@menuri='artsubmit/Anthro/Digital Media/Drawings']")).click();
				form.findElement(By.xpath(".//a[@menuri='artid/3899']")).click(); // animal
			}
		}
		
		// folder
		if (!imageInfo.getFolders().isEmpty()) {
			// for some reason, click() does not work on that element
			driver.findElement(By.className("ile-gallery-groups-button")).sendKeys(Keys.ENTER);
			WebElement popupContent = driver.findElement(By.className("ile-gallery-groups"));
			for (WebElement label : popupContent.findElements(By.tagName("label"))) {
				// dA adds invisible ones, like scrapbook (when did they make that one pay only? or would I have to activate it?)
				if (!label.isDisplayed()) continue;
				String text = label.getText().trim().toLowerCase().replace(" ", "_"); // first text node, not full innerhtml
				
				if (imageInfo.getFolders().contains(text)) {
					label.click(); // should select checkbox as well
				}
				
			}
		}
		
		// tags
		// NOTE: they open an annoying mouseover, that I don't know how to close
		// therefore: DO NOT PUT THAT ABOVE THEME OR ANY OTHER ITEM BELOW IT ON THE PAGE
		TreeSet<String> allTags = getTags(imageInfo);
		if (!allTags.isEmpty()) {
			WebElement input = driver.findElement(By.cssSelector("div.tags-widget"));
			input.click();
			input = input.findElement(By.xpath(".//span//span//span"));
			for (String tag : allTags) {
				input.sendKeys(tag);
				input.sendKeys(" ");
			}
		}
		
		for (WebElement button : driver.findElements(By.className("ile-submit-button"))) {
			if (button.isDisplayed()) {
				// there are several buttons, but either one works
				button.click();
				break;
			}
		}
		
		showFinishMessage(driver);
		
		//driver.quit();
		return true;
	}

	@Override
	public ArrayList<String> getErrorReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<String>();

		/*
		 * main image
		 */
		// does file exist?
		if (imageInfo.getImagePath() == null || !Files.exists(imageInfo.getImagePath())) {
			reasons.add("no image");
		} else {
			// is file of correct type?
			int i = imageInfo.getImagePath().getFileName().toString().lastIndexOf('.');
			String extension = "";
			if (i > 0) {
				extension = imageInfo.getImagePath().getFileName().toString().substring(i+1).toLowerCase();
			}
			if (!extension.equals("jpeg") && !extension.equals("jpg") && !extension.equals("png")) {
				reasons.add("unsupported image type ." + extension);
			} else {
				// how about filesize
				try {
					long bytes = Files.size(imageInfo.getImagePath());
					if (bytes > 30 * 1024 * 1024) { // 30 MB
						reasons.add("image file too large (>30MB)");
					}
				} catch (IOException e) {
					e.printStackTrace();
					// shouldn't happen, but let's assume it's correct
				}
			}
		}


		/*
		 * thumb
		 */
		// only if set
		if (imageInfo.getThumbPath() != null && Files.exists(imageInfo.getThumbPath())) {
			reasons.add("custom thumbnail not supported");
		}
		
		if (imageInfo.isToScraps()) reasons.add("no support for scraps");
		if (imageInfo.hasNoNotification()) reasons.add("no support for 'no notification'");
		if (imageInfo.isFriendsOnly()) reasons.add("no support for 'friends only'");
		if (imageInfo.isUnlisted()) reasons.add("no support for 'unlisted'");

		if (imageInfo.getSexualRating() != ImageInfo.RatingSexual.NONE && (imageInfo.getOtherTags().contains("cub")
				|| imageInfo.getKinkTags().contains("cub"))) {
			reasons.add("cubs in sexual context");
		}

		return reasons;
	}

	@Override
	public ArrayList<String> getWarningReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<String>();

		if (imageInfo.getType() == ImageInfo.Type.SKETCH) {
			reasons.add("no support for type 'sketch', will default to digital");
		}
		
		if (!imageInfo.getFolders().isEmpty()) {
			reasons.add("only top-level folders are supported");
		}
		
		return reasons;
	}

	@Override
	public String getName() {
		return "DeviantArt";
	}

	private static TreeSet<String> getTags(ImageInfo imageInfo) {
		TreeSet<String> tags = new TreeSet<>();
		
		// add genders first
		boolean containsIntersex = false;
		for (Gender gender : imageInfo.getGenders()) {
			tags.add(gender.getTag());
			if (!containsIntersex) containsIntersex = !(gender.equals(Gender.M2F) || gender.equals(Gender.F2M) || gender.equals(Gender.MALE) || gender.equals(Gender.FEMALE) || gender.equals(Gender.AMBIGUOUS));
		}
		if (containsIntersex) tags.add("intersex");	
		
		// add other tags
		for (String species : imageInfo.getSpeciesTags()) {
			tags.add(species);
		}
		for (String kink : imageInfo.getKinkTags()) {
			tags.add(kink);
		}
		for (String other : imageInfo.getOtherTags()) {
			tags.add(other);
		}
		return tags;
	}
}
