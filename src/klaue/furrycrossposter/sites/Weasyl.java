package klaue.furrycrossposter.sites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import klaue.furrycrossposter.ImageInfo;
import klaue.furrycrossposter.ImageInfo.Gender;
import klaue.furrycrossposter.ImageInfo.RatingSexual;
import klaue.furrycrossposter.ImageInfo.RatingViolence;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Weasyl extends Site {
	private WebDriver driver;
	
	@Override
	public boolean doUpload(ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;
		
		// before we start, let's resize the images, if need be
		Path imagePath = imageInfo.getImagePath();
		Path thumbPath = imageInfo.getThumbPath();
		
		driver = getDriver();
		
		driver.get("https://www.weasyl.com/signin");
		
		// wait for login
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));

		driver.findElement(By.xpath("//a[@href='/submit']")).click();
		driver.findElement(By.xpath("//a[@href='/submit/visual']")).click();
		
		driver.findElement(By.id("submitfile")).sendKeys(imagePath.toString());
		if (thumbPath != null) {
			driver.findElement(By.name("thumbfile")).sendKeys(thumbPath.toString());
		}
		
		driver.findElement(By.id("submissiontitle")).sendKeys(imageInfo.getTitle());
		
		if (!imageInfo.getDescription().isEmpty()) {
			driver.findElement(By.id("submissiondesc")).sendKeys(imageInfo.getDescription());
		}
		
		// category
		Select category = new Select(driver.findElement(By.id("submissioncat")));
		switch (imageInfo.getType()) {
			case DIGITAL:
				category.selectByValue("1030");
				break;
			case SKETCH:
				category.selectByValue("1010");
				break;
			case TRADITIONAL:
				category.selectByValue("1020");
				break;
		}
		
		//rating
		Select rating = new Select(driver.findElement(By.id("submissionrating")));
		if (imageInfo.getSexualRating() == RatingSexual.NONE && imageInfo.getViolenceRating() == RatingViolence.NONE) {
			rating.selectByValue("10"); // general
		} else if (imageInfo.getSexualRating() == RatingSexual.NONE && imageInfo.getViolenceRating() != RatingViolence.VIOLENCE_EX) {
			// sex not, violence moderate. weasyl counts any nudity as mature, so better play it safe and make every
			// sexual thing at least mature
			rating.selectByValue("20"); // Moderate (13+)
		} else if(imageInfo.getSexualRating() != RatingSexual.NUDITY_EX) {
			// explicit violence and/or moderate nudity
			rating.selectByValue("30"); // Mature (18+ non-sexual)
		} else {
			// nudity explicit, violence maybe too
			rating.selectByValue("40"); // Explicit (18+ sexual)
		}
		
		// folders
		Select folderSelect = new Select(driver.findElement(By.id("submissionfolder")));
		// weasyl has noth folders and subfolders, both of which can be used
		// in the select, they're seperated by a /, like "parentfolder / subfolder". The problem is that foldernames
		// themselfes can contain /, so a folder named "sub / folder" inside a folder named "parent / folder" would
		// show up as "parent / folder / sub / folder" in the list. To check if our folder is in it, we have to search
		// for all names that either start with our folder, or have " / " before it.
		// not a foolproof way, if he has one parent folder called "folder" and a sub-folder called "sub / folder", it
		// would generate two entries in the list, "folder" and "folder / sub / folder". If we searched for "folder" in
		// that way, both would be valid
		//
		// while weasyl can only contain / set one folder, but FurryCrossposter may contain many (for other sites), we can't
		// just select the shortest one and be sure, but.. we still do. Screw the user anyway if he made such folders!
		ArrayList<WebElement> foundFolders = new ArrayList<>();
		for (WebElement entry : folderSelect.getOptions()) {
			String currentFolderFromSelect = entry.getText().toLowerCase().replace(" ", "_");
			
			for (String currentFolderFromImageInfo : imageInfo.getFolders()) {
				if (currentFolderFromSelect.matches("^(.+_[\\/]_)*\\Q" + currentFolderFromImageInfo + "\\E$")) {
					foundFolders.add(entry);
					//break; NO! multiple may match
				}
			}
		}
		WebElement folderToSelect = null;
		for (WebElement foundFolder : foundFolders) {
			if (folderToSelect == null || folderToSelect.getText().length() > foundFolder.getText().length()) {
				folderToSelect = foundFolder;
			}
		}
		if (folderToSelect != null) {
			folderSelect.selectByVisibleText(folderToSelect.getText());
		}
		
		if (imageInfo.isFriendsOnly()) driver.findElement(By.id("submit-friends")).click();
		if (imageInfo.hasNoNotification()) driver.findElement(By.id("nonotifcation")).click();
		
		
		driver.findElement(By.cssSelector("ul.listbuilder")).click();
		driver.switchTo().activeElement().sendKeys(getTags(imageInfo) + " ");

		driver.findElement(By.cssSelector("button.button.positive")).click();
		
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
					if (bytes > 36 * 1024 * 1024) { // 10 MB
						reasons.add("thumb file too large (>10MB)");
					}
				} catch (IOException e) {
					e.printStackTrace();
					// shouldn't happen, but let's assume it's correct
				}
			}
		}

		if (imageInfo.getTitle().isEmpty()) reasons.add("no title");
		
		int tagAmount = imageInfo.getGenders().size() + imageInfo.getSpeciesTags().size() + imageInfo.getKinkTags().size() + imageInfo.getOtherTags().size();
		if (tagAmount < 2) reasons.add("needs at least 2 tags (incl. gender)");

		if (imageInfo.getSexualRating() != ImageInfo.RatingSexual.NONE && (imageInfo.getOtherTags().contains("cub")
				|| imageInfo.getKinkTags().contains("cub"))) {
			reasons.add("cubs in sexual context");
		}
		
		if (imageInfo.isToScraps()) reasons.add("no support for scraps - use scraps folder, see weasyl help");
		if (imageInfo.isUnlisted()) reasons.add("no support for 'unlisted'");

		return reasons;
	}

	@Override
	public ArrayList<String> getWarningReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<String>();
		
		// none
		
		return reasons;
	}

	@Override
	public String getName() {
		return "Weasyl";
	}

	private static String getTags(ImageInfo imageInfo) {
		StringBuffer tags = new StringBuffer();
		
		// add genders first
		boolean containsIntersex = false;
		for (Gender gender : imageInfo.getGenders()) {
			tags.append(gender.getTag()).append(" ");
			if (!containsIntersex) containsIntersex = !(gender.equals(Gender.M2F) || gender.equals(Gender.F2M) || gender.equals(Gender.MALE) || gender.equals(Gender.FEMALE) || gender.equals(Gender.AMBIGUOUS));
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
		tags.deleteCharAt(tags.length() - 1);
		return tags.toString();
	}
}
