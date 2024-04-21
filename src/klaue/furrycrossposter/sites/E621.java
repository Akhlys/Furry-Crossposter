package klaue.furrycrossposter.sites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import klaue.furrycrossposter.FurryCrossposter;
import klaue.furrycrossposter.ImageInfo;
import klaue.furrycrossposter.ImageInfo.Gender;
import klaue.furrycrossposter.ImageInfo.RatingSexual;
import klaue.furrycrossposter.ImageInfo.RatingViolence;
import klaue.furrycrossposter.Tag;

public class E621 extends Site {
	private WebDriver driver;
	
	@Override
	public boolean doUpload(ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;
		Path imagePath = imageInfo.getImagePath();
		
		this.driver = getDriver();
		
		this.driver.get("https://e621.net/user/login");
		
		WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds(60));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@href='/user/edit']")));
		
		this.driver.get("https://e621.net/post/upload");

		this.driver.findElement(By.id("post_file")).sendKeys(imagePath.toString());
		
		// tags
		// tags were already tested to all be valid in getErrorReasons() so no need to check them again
		TreeSet<String> tags = getStringTags(imageInfo);
		StringBuffer tagString = new StringBuffer();
		for (String tag : tags) {
			tagString.append(tag).append(" ");
		}
		tagString.deleteCharAt(tagString.length() - 1);
		
		this.driver.findElement(By.id("post_tags")).clear();
		this.driver.findElement(By.id("post_tags")).sendKeys(tagString.toString());
		
		// build description
		StringBuffer description = new StringBuffer();
		if (imageInfo.getTitle() != null) {
			description.append("h1.").append(imageInfo.getTitle()).append("\n");
		}
		description.append(imageInfo.getDescription());
		
		this.driver.findElement(By.id("post_description")).clear();
		this.driver.findElement(By.id("post_description")).sendKeys(description.toString());
		
		if (imageInfo.getSexualRating() == RatingSexual.NUDITY_EX || imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_EX) {
			this.driver.findElement(By.id("post_rating_questionable")).click();
		} else if (imageInfo.getSexualRating() == RatingSexual.NUDITY_MOD || imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_MOD) {
			this.driver.findElement(By.id("post_rating_questionable")).click();
		} else {
			this.driver.findElement(By.id("post_rating_safe")).click();
		}

		this.driver.findElement(By.xpath("//input[@type='submit' and @value='Upload']")).click();
		
		showFinishMessage(this.driver);
		
		//driver.quit();
		return true;
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
			// filesize
			try {
				long bytes = Files.size(imageInfo.getImagePath());
				if (bytes > 50 * 1024 * 1024) { // 50 MB
					reasons.add("image file too large (>50MB)");
				}
			} catch (IOException e) {
				e.printStackTrace();
				// shouldn't happen, but let's assume it's correct
			}
		}


		/*
		 * thumb
		 */
		// only if set
		if (imageInfo.getThumbPath() != null && Files.exists(imageInfo.getThumbPath())) {
			reasons.add("custom thumbnail not supported");
		}
		
		TreeSet<String> allTags = getStringTags(imageInfo);
		TreeMap<String, Tag> realTags = getTagMap(allTags);
		if(realTags.size() < 4) {
			reasons.add("min 4 tags (incl. genders)");
		} else if(realTags.size() != allTags.size()) {
			reasons.add("tags contain unrecognized entries (marked red)");
		}
		
		if (imageInfo.isToScraps()) reasons.add("no support for scraps");
		if (imageInfo.hasNoNotification()) reasons.add("no support for 'no notification'");
		if (imageInfo.isFriendsOnly()) reasons.add("no support for 'friends only'");
		if (imageInfo.isUnlisted()) reasons.add("no support for 'unlisted'");

		return reasons;
	}

	@Override
	public ArrayList<String> getWarningReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<>();	
		return reasons;
	}

	@Override
	public String getName() {
		return "e621";
	}

	private static TreeSet<String> getStringTags(ImageInfo imageInfo) {
		TreeSet<String> tags = new TreeSet<>();
		
		// add genders first
		boolean containsIntersex = false;
		for (Gender gender : imageInfo.getGenders()) {
			tags.add(gender.getTag());
			if (!containsIntersex) containsIntersex = !(gender.equals(Gender.M2F) || gender.equals(Gender.F2M) || gender.equals(Gender.MALE) || gender.equals(Gender.FEMALE));
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
	
	private TreeMap<String, Tag> getTagMap(TreeSet<String> stringTags) {
		TreeMap<String, Tag> tagMap = new TreeMap<>();
		
		for (String stringTag : stringTags) {
			Tag realTag = FurryCrossposter.tags.get(stringTag);
			if (realTag != null) {
				tagMap.put(stringTag, realTag);
			}
		}
		
		return tagMap;
	}
	
	@Override
	protected void showFinishMessage(WebDriver driver) {
		((JavascriptExecutor)driver).executeScript("alert('Furry Crossposter finished - close browser window when you confirmed everything\\nPlease edit and add source URL and artist name!');");
	}
}
