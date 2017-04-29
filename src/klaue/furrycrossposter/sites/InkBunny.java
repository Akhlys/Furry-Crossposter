package klaue.furrycrossposter.sites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import klaue.furrycrossposter.FurryCrossposter;
import klaue.furrycrossposter.ImageInfo;
import klaue.furrycrossposter.ImageInfo.Gender;
import klaue.furrycrossposter.ImageInfo.RatingSexual;
import klaue.furrycrossposter.ImageInfo.RatingViolence;
import klaue.furrycrossposter.ImageTools;
import klaue.furrycrossposter.Tag;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InkBunny extends Site {
	private WebDriver driver;
	
	@Override
	public boolean doUpload(ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;
		
		// before we start, let's resize the images, if need be
		Path imagePath = imageInfo.getImagePath();
		Path thumbPath = imageInfo.getThumbPath();
		
		try {
			BufferedImage image = ImageIO.read(imageInfo.getImagePath().toFile());
			// is image below 36 MP?
			int width          = image.getWidth();
			int height         = image.getHeight();
			if (width * height > 36000000) { // 36 megapixel max
				Path newImgPath = FurryCrossposter.workingDir.resolve(imagePath.getFileName());
				ImageTools.getResizedFile(36000000, image, newImgPath.toFile());
				imagePath = newImgPath;
				// newImgPath.toFile().deleteOnExit(); seems not to work
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not resize image file that is over 36MP", "Furry Crossposter - InkBunny", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (thumbPath != null) {
			try {
				BufferedImage image = ImageIO.read(imageInfo.getThumbPath().toFile());
				// is image below 36 MP?
				int width          = image.getWidth();
				int height         = image.getHeight();
				if (width * height > 36000000) { // 36 megapixel max?
					Path newImgPath = FurryCrossposter.workingDir.resolve(imagePath.getFileName());
					ImageTools.getResizedFile(36000000, image, newImgPath.toFile());
					thumbPath = newImgPath;
					//newImgPath.toFile().deleteOnExit(); seems not to work
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Could not resize thumb file that is over 36MP", "Furry Crossposter - InkBunny", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		driver = getDriver();
		
		driver.get("https://inkbunny.net/login.php");
		
		// wait for login
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("privatemessage_notification")));
		
		// go to upload (menu will only show on hover)
		WebElement menu = driver.findElement(By.xpath("//div[@id='usernavigation']/div[4]/ul/li[2]"));
		//build and perform the mouseOver with Advanced User Interactions API
		Actions builder = new Actions(driver);
		builder.moveToElement(menu).build().perform();
		driver.findElement(By.xpath("//div[@id='usernavigation']/div[4]/ul/li[2]/ul/li/a/span")).click();
		
		// upload image and thumb, wait for upload to complete, give it 2 mins
		driver.findElement(By.name("uploadedfile[]")).sendKeys(imagePath.toString());
		
		if (thumbPath != null) {
			driver.findElement(By.name("uploadedthumbnail[]")).sendKeys(thumbPath.toString());
		}
		driver.findElement(By.xpath("//input[@value='Upload']")).click();
		wait = new WebDriverWait(driver, 120);
		WebElement elem = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value='Next >']")));
		elem.click();
		
		// image type, only change if sketch
		if (imageInfo.getType() == ImageInfo.Type.SKETCH) {
			new Select(driver.findElement(By.id("typeselector"))).selectByVisibleText("Sketch");
		}
		
		if (imageInfo.isToScraps()) {
			driver.findElement(By.id("scraps")).click();
		}
		
		driver.findElement(By.name("title")).clear();
		driver.findElement(By.name("title")).sendKeys(imageInfo.getTitle());
		
		if (!imageInfo.getDescription().trim().isEmpty()) {
			driver.findElement(By.id("desc")).clear();
			driver.findElement(By.id("desc")).sendKeys(imageInfo.getDescription());
		}
		
		// gender
		if (imageInfo.getGenders().size() > 0) {
			StringBuffer genders = new StringBuffer();
			boolean containsIntersex = false;
			boolean containsTrans = false;
			for (Gender gender : imageInfo.getGenders()) {
				genders.append(gender.getTag()).append(" ");
				if (!containsIntersex) containsIntersex = !(gender.equals(Gender.M2F) || gender.equals(Gender.F2M) || gender.equals(Gender.MALE) || gender.equals(Gender.FEMALE) || gender.equals(Gender.AMBIGUOUS));
				if (!containsTrans) containsTrans = (gender.equals(Gender.M2F) || gender.equals(Gender.F2M));
			}
			genders.deleteCharAt(genders.length() - 1);
			if (containsIntersex) genders.append(" intersex");	
			if (containsTrans) genders.append(" gender_transformation");			

			driver.findElement(By.id("keywords_sexgender")).sendKeys(genders.toString());
		} else {
			driver.findElement(By.id("keywords_sexgender_na")).click();
		}
		
		// species
		if (imageInfo.getSpeciesTags().size() > 0) {
			StringBuffer animals = new StringBuffer();
			for (String species : imageInfo.getSpeciesTags()) {
				animals.append(species).append(" ");
			}
			animals.deleteCharAt(animals.length() - 1);
			driver.findElement(By.id("keywords_species")).sendKeys(animals.toString());
		} else {
			driver.findElement(By.id("keywords_species_na")).click();
		}

		// themes
		if (imageInfo.getKinkTags().size() > 0) {
			StringBuffer kinks = new StringBuffer();
			for (String kink : imageInfo.getKinkTags()) {
				kinks.append(kink).append(" ");
			}
			kinks.deleteCharAt(kinks.length() - 1);
			driver.findElement(By.id("keywords_themes")).sendKeys(kinks.toString());
		} else {
			driver.findElement(By.id("keywords_themes_na")).click();
		}

		// others
		if (imageInfo.getOtherTags().size() > 0) {
			StringBuffer others = new StringBuffer();
			for (String other : imageInfo.getOtherTags()) {
				others.append(other).append(" ");
			}
			others.deleteCharAt(others.length() - 1);
			driver.findElement(By.id("keywords_other")).sendKeys(others.toString());
		}
		
		// rating
		if (imageInfo.getSexualRating() == RatingSexual.NONE && imageInfo.getViolenceRating() == RatingViolence.NONE) {
			driver.findElement(By.id("general_norating")).click();
		} else {
			if (imageInfo.getSexualRating() == RatingSexual.NUDITY_MOD) {
				driver.findElement(By.id("ct_2")).click();
			} else if (imageInfo.getSexualRating() == RatingSexual.NUDITY_EX) {
				driver.findElement(By.id("ct_4")).click();
			}
			
			if (imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_MOD) {
				driver.findElement(By.id("ct_3")).click();
			} else if (imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_EX) {
				driver.findElement(By.id("ct_5")).click();
			}
		}
		
		if (imageInfo.getFolders().size() > 0) {
			driver.findElement(By.id("pools")).click();
		}
		
		if (imageInfo.isFriendsOnly()) {
			driver.findElement(By.id("friends_only")).click();
		}
		
		if (imageInfo.isUnlisted()) {
			new Select(driver.findElement(By.name("visibility"))).selectByValue("no");
		} else if(imageInfo.hasNoNotification()) {
			new Select(driver.findElement(By.name("visibility"))).selectByValue("yes_nowatch");
		}
		
		driver.findElement(By.xpath("//input[@value='Next >']")).click();
		

		if (imageInfo.getFolders().size() > 0) {
			// we selected the pool button, so now we're at pool choosing
			List<WebElement> poolLinks = driver.findElements(By.xpath("//a[contains(@href, 'poolview_process.php?pool_id=')]"));
			
			//boolean poolFound = false;
			for (WebElement element : poolLinks) {
				String text = element.getText().trim().toLowerCase().replace(" ", "_");
				if (imageInfo.getFolders().contains(text)) {
					//poolFound = true;
					String href = element.getAttribute("href");
					int start = href.indexOf("pool_id=") + 8;
					String poolId = href.substring(start);
					driver.findElement(By.xpath("//a[contains(@onclick, '" + poolId + "') and @title='Add to Pool']")).click();
				}
			}
			
			driver.findElement(By.xpath("//input[@value='Done']")).click();
		}
		
		showFinishMessage(driver);
		
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
			if (!extension.equals("jpeg") && !extension.equals("jpg") && !extension.equals("png") && !extension.equals("gif")) {
				reasons.add("unsupported image type ." + extension);
			} else {
				// how about filesize
				try {
					long bytes = Files.size(imageInfo.getImagePath());
					if (bytes > 36 * 1024 * 1024) { // 36 MB
						reasons.add("image file too large (>36MB)");
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
					if (bytes > 36 * 1024 * 1024) { // 36 MB
						reasons.add("thumb file too large (>36MB)");
					}
				} catch (IOException e) {
					e.printStackTrace();
					// shouldn't happen, but let's assume it's correct
				}
			}
		}

		if (imageInfo.getTitle().isEmpty()) reasons.add("no title");
		if (imageInfo.getDescription().length() >= 100000) reasons.add("description too long (>100000 characters)");

		// https://wiki.inkbunny.net/wiki/Keyword_Policy#Minimum_Required_Keywords
		if (imageInfo.getGenders().size() == 0) reasons.add("no genders set");
		boolean foundCorrect = false;
		for (String species : imageInfo.getSpeciesTags()) {
			Tag tag = FurryCrossposter.tags.get(species);
			if (tag == null || tag.getType() != Tag.Type.SPECIES) continue;
			foundCorrect = true;
			break;
		}
		if (!foundCorrect) reasons.add("no species set");

		foundCorrect = false;
		ArrayList<String> cummulativeTags = new ArrayList<String>();
		cummulativeTags.addAll(imageInfo.getKinkTags());
		cummulativeTags.addAll(imageInfo.getOtherTags());
		for (String other : cummulativeTags) {
			Tag tag = FurryCrossposter.tags.get(other);
			if (tag == null || tag.getType() != Tag.Type.GENERAL) continue;
			foundCorrect = true;
			break;
		}
		if (!foundCorrect) reasons.add("no themes set");

		// cannot be both at same time, but unlisted implies the other
		//if (imageInfo.isUnlisted() && imageInfo.hasNoNotification()) reasons.add("");

		//https://wiki.inkbunny.net/wiki/ACP
		if (imageInfo.getSexualRating() != ImageInfo.RatingSexual.NONE && imageInfo.getSpeciesTags().contains("human")) {
			reasons.add("humans in sexual context");
		}

		return reasons;
	}

	@Override
	public ArrayList<String> getWarningReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<String>();

		try {
			BufferedImage image = ImageIO.read(imageInfo.getImagePath().toFile());
			// is image below 36 MP?
			int width          = image.getWidth();
			int height         = image.getHeight();
			if (width * height > 36000000) { // 36 megapixel max
				reasons.add("image too large (>36MP), will be resized before upload");
			}

			if (imageInfo.getThumbPath() != null) {
				image = ImageIO.read(imageInfo.getThumbPath().toFile());
				// is image below 36 MP?
				width          = image.getWidth();
				height         = image.getHeight();
				if (width * height > 36000000) { // 36 megapixel max?
					reasons.add("thumb too large (>36MP), will be resized before upload");
				}
			}
		} catch (IOException e) {
			// If that generates an error, getErrorReasons() or the main window image display would already
			// have reported it, eat the error
			e.printStackTrace();
		}

		return reasons;
	}

	@Override
	public String getName() {
		return "InkBunny";
	}

}
