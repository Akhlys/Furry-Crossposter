package klaue.furrycrossposter.sites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import klaue.furrycrossposter.FurryCrossposter;
import klaue.furrycrossposter.ImageInfo;
import klaue.furrycrossposter.ImageInfo.Gender;
import klaue.furrycrossposter.ImageInfo.RatingSexual;
import klaue.furrycrossposter.ImageInfo.RatingViolence;
import klaue.furrycrossposter.ImageInfo.Type;
import klaue.furrycrossposter.ImageTools;

public class FurAffinity extends Site {
	private WebDriver driver;
	
	@Override
	public boolean doUpload(ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;
		
		// before we start, let's resize the images, if need be
		Path imagePath = imageInfo.getImagePath();
		Path thumbPath = imageInfo.getThumbPath();
		
		try {
			BufferedImage image = ImageIO.read(imageInfo.getImagePath().toFile());
			// is image below 1280x1280?
			int width          = image.getWidth();
			int height         = image.getHeight();
			if (width > 1280 || height > 1280) {
				Path newImgPath = FurryCrossposter.workingDir.resolve(imagePath.getFileName());
				ImageTools.getResizedFile(1280, 1280, image, newImgPath.toFile());
				imagePath = newImgPath;
				//newImgPath.toFile().deleteOnExit(); seems not to work
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not resize image file that has width or height over 1280", "Furry Crossposter - Furaffinity", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (thumbPath != null) {
			try {
				BufferedImage image = ImageIO.read(imageInfo.getThumbPath().toFile());
				// is image below 1280x1280?
				int width          = image.getWidth();
				int height         = image.getHeight();
				if (width > 1280 || height > 1280) {
					Path newImgPath = FurryCrossposter.workingDir.resolve(imagePath.getFileName());
					ImageTools.getResizedFile(1280, 1280, image, newImgPath.toFile());
					thumbPath = newImgPath;
					//newImgPath.toFile().deleteOnExit(); seems not to work
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Could not resize thumb file that is over 36MP", "Furry Crossposter - InkBunny", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		
		this.driver = getDriver();

		this.driver.get("https://www.furaffinity.net/login/");
		
		removeMobileStuff();
		
		// wait for login
		WebDriverWait wait = new WebDriverWait(this.driver, 60);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("my-username")));//By.xpath("(//*[@id='my-username'])")));//By.id("my-username")));
		
		this.driver.findElement(By.xpath("//a[@href='/submit/']")).click();
		
		removeMobileStuff();
//		// default, "Artwork", is selected, just continue
//		this.driver.findElement(By.xpath("//button[@value='Next']")).click();
//		removeMobileStuff();
		
		this.driver.findElement(By.name("submission")).sendKeys(imagePath.toString());
		
		if (thumbPath != null) {
			this.driver.findElement(By.name("thumbnail")).sendKeys(thumbPath.toString());
		}
		this.driver.findElement(By.xpath("//button[contains(.,'Upload')]")).click();
		removeMobileStuff();
		
		// remove cookie overlay that may obscure clicks
		JavascriptExecutor js = (JavascriptExecutor)this.driver;
		js.executeScript("var cookiediv = document.getElementById('cookie-notification'); if (cookiediv) cookiediv.remove();");
		
		this.driver.findElement(By.name("title")).sendKeys(imageInfo.getTitle());
		this.driver.findElement(By.id("message")).sendKeys(imageInfo.getDescription());
		
		// rating
		List<WebElement> ratingElements = this.driver.findElements(By.name("rating"));
		for (WebElement ratingElement : ratingElements) {
			if (ratingElement.getAttribute("value").equals("1") && imageInfo.getSexualRating() == RatingSexual.NUDITY_EX
					|| imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_EX) {
				ratingElement.click();
			} else if(ratingElement.getAttribute("value").equals("2") && imageInfo.getSexualRating() == RatingSexual.NUDITY_MOD
					|| imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_MOD) {
				ratingElement.click();
			} else if(ratingElement.getAttribute("value").equals("0")) {
				ratingElement.click();
			}
		}
//		if (imageInfo.getSexualRating() == RatingSexual.NUDITY_EX
//				|| imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_EX) {
//			this.driver.findElement(By.id("rating-type-adult")).click();
//		} else if (imageInfo.getSexualRating() == RatingSexual.NUDITY_MOD
//				|| imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_MOD) {
//			this.driver.findElement(By.id("rating-type-mature")).click();
//		} else {
//			this.driver.findElement(By.id("rating-type-general")).click();
//		}
		
		if (imageInfo.getType() == Type.TRADITIONAL) {
			new Select(this.driver.findElement(By.name("cat"))).selectByVisibleText("Artwork (Traditional)");
		} else {
			// digital, sketch
			new Select(this.driver.findElement(By.name("cat"))).selectByVisibleText("Artwork (Digital)");
		}
		
		if (imageInfo.isToScraps()) {
			this.driver.findElement(By.name("scrap")).click();
		}
		
		// theme
		Select typeList = new Select(this.driver.findElement(By.name("atype")));
		WebElement entryToSelect = null;
		for (WebElement entry : typeList.getOptions()) {
			String textOfEntry = entry.getText().toLowerCase().replace(" ", "_");
			
			if (textOfEntry.contains("macro") // macro / micro
					&& (imageInfo.getKinkTags().contains("macro") || imageInfo.getOtherTags().contains("macro")
						|| imageInfo.getOtherTags().contains("micro") || imageInfo.getOtherTags().contains("micro"))) {
				entryToSelect = entry;
				continue;
			}

			textOfEntry = (textOfEntry.contains("gore_")) ? "gore" : textOfEntry;
			textOfEntry = (textOfEntry.equals("water_sports")) ? "watersports" : textOfEntry;
			textOfEntry = (textOfEntry.contains("my_little_pony")) ? "my_little_pony" : textOfEntry;
			if (imageInfo.getKinkTags().contains(textOfEntry) || imageInfo.getOtherTags().contains(textOfEntry)) {
				entryToSelect = entry;
			}
		}
		if (entryToSelect != null) {
			typeList.selectByVisibleText(entryToSelect.getText());
		} else {
			typeList.selectByValue("100"); // General Furry Art
		}
		
		// species
		Select speciesList = new Select(this.driver.findElement(By.name("species")));
		ArrayList<WebElement> possibleEntries = new ArrayList<>();
		for (WebElement entry : speciesList.getOptions()) {
			String textOfEntry = entry.getText().toLowerCase();
			if (imageInfo.getSpeciesTags().contains(textOfEntry)) {
				possibleEntries.add(entry);
			} else {
				// fa adds some stuff like "feline - leopard". bastards.
				String[] textParts = textOfEntry.split(" ");
				for (String part : textParts) {
					if (imageInfo.getSpeciesTags().contains(part)) {
						possibleEntries.add(entry);
					}
				}
			}
			
		}
		if (possibleEntries.size() == 1) {
			speciesList.selectByVisibleText(possibleEntries.get(0).getText());
		} else { // more or less than 1
			speciesList.selectByValue("1"); // Unspecified / Any
		}
		
		// gender
		Select genderList = new Select(this.driver.findElement(By.name("gender")));
		if (imageInfo.getGenders().size() == 0) {
			genderList.selectByValue("7"); // Other / Not Specified
		} else if (imageInfo.getGenders().size() > 1) {
			genderList.selectByValue("6"); // Multiple characters
		} else {
			Gender gender = imageInfo.getGenders().first();
			switch(gender) {
				case MALE:
					genderList.selectByValue("2");
					break;
				case FEMALE:
					genderList.selectByValue("3");
					break;
				case CUNTBOY:
				case DICKGIRL:
				case HERM:
				case MALEHERM:
					genderList.selectByValue("4");
					break;
				case F2M:
				case M2F:
					genderList.selectByValue("5");
					break;
				default:// never reached
					genderList.selectByValue("0"); // Any
					break;
			}
		}
		
		// tags
		String keywordTags = getTags(imageInfo);
		if (keywordTags.length() > 250) {
			keywordTags = keywordTags.substring(0, keywordTags.lastIndexOf(" ", 250) - 1);
		}
		
		this.driver.findElement(By.id("keywords")).sendKeys(keywordTags);
		
		
		// folders
		if (!imageInfo.getFolders().isEmpty()) {
			List<WebElement> fieldsets = this.driver.findElements(By.tagName("fieldset"));
			for (WebElement fieldset : fieldsets) {
				// folder groups
				String folderName = fieldset.findElement(By.tagName("legend")).getText();
				folderName = folderName + "_-_";
				
				List<WebElement> folderDivs = fieldset.findElements(By.className("folder_name"));
				for (WebElement folderDiv : folderDivs) {
					folderName += folderDiv.findElement(By.tagName("label")).getText();
					folderName = folderName.toLowerCase().replace(" ", "_");
					if (imageInfo.getFolders().contains(folderName)) {
						folderDiv.findElement(By.tagName("input")).click();
					}
				}
			}
		}
		
		this.driver.findElement(By.xpath("//input[@value='Finalize ']")).click();
		
		showFinishMessage(this.driver);
		
		// delete img/thumb if resized to working dir
		try {
			if (imagePath.getParent().equals(FurryCrossposter.workingDir)) Files.delete(imagePath);
			if (thumbPath != null && thumbPath.getParent().equals(FurryCrossposter.workingDir))  Files.delete(thumbPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//driver.quit();
		return true;
	}
	
	private void removeMobileStuff() {
		JavascriptExecutor js = (JavascriptExecutor)this.driver;
		js.executeScript("var mobileElements = document.getElementsByClassName('hideondesktop'); if (mobileElements) for (let elem of mobileElements) elem.remove();");
		js.executeScript("var mobileElements = document.getElementsByClassName('mobile-navigation'); if (mobileElements) for (let elem of mobileElements) elem.remove();");
	}

	@Override
	public ArrayList<String> getErrorReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<>();

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
			if (!extension.equals("jpeg") && !extension.equals("jpg") && !extension.equals("png") && !extension.equals("gif")) {
				reasons.add("unsupported image type ." + extension);
			} else {
				// how about filesize
				try {
					long bytes = Files.size(imageInfo.getImagePath());
					if (bytes > 10 * 1024 * 1024) { // 10 MB
						reasons.add("image file too large (>10MB)");
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
			// is file of correct type?
			int i = imageInfo.getThumbPath().getFileName().toString().lastIndexOf('.');
			String extension = "";
			if (i > 0) {
				extension = imageInfo.getThumbPath().getFileName().toString().substring(i+1).toLowerCase();
			}
			if (!extension.equals("jpeg") && !extension.equals("jpg") && !extension.equals("png") && !extension.equals("gif")) {
				reasons.add("unsupported thumb image type ." + extension);
			} else {
				// how about filesize
				try {
					long bytes = Files.size(imageInfo.getThumbPath());
					if (bytes > 10 * 1024 * 1024) { // 10 MB
						reasons.add("thumb file too large (>10MB)");
					}
				} catch (IOException e) {
					e.printStackTrace();
					// shouldn't happen, but let's assume it's correct
				}
			}
		}

		if (imageInfo.getTitle().isEmpty()) reasons.add("no title");
		if (imageInfo.getDescription().isEmpty()) reasons.add("no description");

		// cannot be both at same time, but unlisted implies the other
		//if (imageInfo.isUnlisted() && imageInfo.hasNoNotification()) reasons.add("");

		if (imageInfo.getSexualRating() != ImageInfo.RatingSexual.NONE && (imageInfo.getOtherTags().contains("cub")
				|| imageInfo.getKinkTags().contains("cub"))) {
			reasons.add("cubs in sexual context");
		}
		
		if (imageInfo.hasNoNotification()) reasons.add("no support for 'no notification'");
		if (imageInfo.isFriendsOnly()) reasons.add("no support for 'friends only'");
		if (imageInfo.isUnlisted()) reasons.add("no support for 'unlisted'");

		return reasons;
	}

	@Override
	public ArrayList<String> getWarningReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<>();

		try {
			BufferedImage image = ImageIO.read(imageInfo.getImagePath().toFile());
			// is image below 36 MP?
			int width          = image.getWidth();
			int height         = image.getHeight();
			if (width > 1280 || height > 1280) {
				reasons.add("image too large (>1280x1280), will be resized before upload");
			}

			if (imageInfo.getThumbPath() != null) {
				image = ImageIO.read(imageInfo.getThumbPath().toFile());
				// is image below 36 MP?
				width          = image.getWidth();
				height         = image.getHeight();
				if (width > 1280 || height > 1280) { // ?
					reasons.add("thumb too large (>1280x1280), will be resized before upload");
				}
			}
		} catch (IOException e) {
			// If that generates an error, getErrorReasons() or the main window image display would already
			// have reported it, eat the error
			e.printStackTrace();
		}

		if (imageInfo.getType() == ImageInfo.Type.SKETCH) {
			reasons.add("no support for type 'sketch', will default to digital");
		}
		
		int tagLength = getTags(imageInfo).length();
		if (tagLength > 250) reasons.add("too many tags (>250 chars), will shorten");
		
		return reasons;
	}

	@Override
	public String getName() {
		return "FurAffinity";
	}

	private static String getTags(ImageInfo imageInfo) {
		StringBuffer tags = new StringBuffer();
		
		// add genders first
		boolean containsIntersex = false;
		for (Gender gender : imageInfo.getGenders()) {
			tags.append(gender.getTag()).append(" ");
			if (!containsIntersex) containsIntersex = !(gender.equals(Gender.M2F) || gender.equals(Gender.F2M) || gender.equals(Gender.MALE) || gender.equals(Gender.FEMALE));
		}
		if (containsIntersex) tags.append("intersex ");	
		
		// add other tags
		for (String species : imageInfo.getSpeciesTags()) {
			tags.append(species).append(" ");
		}
		for (String kink : imageInfo.getKinkTags()) {
			tags.append(kink).append(" ");
		}
		for (String other : imageInfo.getOtherTags()) {
			tags.append(other).append(" ");
		}
		if (tags.length() > 1) tags.deleteCharAt(tags.length() - 1);
		return tags.toString();
	}
}
