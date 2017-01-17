package klaue.furrycrossposter.sites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import klaue.furrycrossposter.FurryCrossposter;
import klaue.furrycrossposter.ImageInfo;
import klaue.furrycrossposter.ImageInfo.Gender;
import klaue.furrycrossposter.ImageInfo.RatingSexual;
import klaue.furrycrossposter.ImageInfo.RatingViolence;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FurryNetwork extends Site {
	private WebDriver driver;
	
	@Override
	public boolean doUpload(ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;
		
		Path originalImagePath = imageInfo.getImagePath();
		
		// FN sets the file name of the image as the default title, so we have to save it
		// to make sure to get the image we uploaded, we temporarily copy it with a random name
		int random = new Random().nextInt();
		if (random < 0) random *= -1;
		String pseudoTitle = Integer.toString(random);
		int index = imageInfo.getImagePath().getFileName().toString().lastIndexOf('.');
		String extension = imageInfo.getImagePath().getFileName().toString().substring(index+1).toLowerCase();
		Path copiedImagePath = FurryCrossposter.workingDir.resolve(pseudoTitle + "." + extension);
		try {
			copiedImagePath = Files.copy(originalImagePath, copiedImagePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not make temporary copy of image file " + copiedImagePath, "Furry Crossposter - Furry Network", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		driver = getDriver();
		
		driver.get("https://beta.furrynetwork.com/login/");
		
		// wait for login
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("user-nav__button-menu__trigger")));
		
//		driver.findElement(By.cssSelector("button.user-nav__click-icon.onclick")).click();
//		driver.findElement(By.xpath("//a[@href='/submissions']")).click();
//		
//		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		driver.get("https://beta.furrynetwork.com/submissions");
		driver.findElement(By.xpath("//a[@href='/submissions/artwork/draft/']")).click();
		
		//driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		
		// this is a bit of a hack. the dropper of FN uses a hidden field as drop target, somehow, search it's app.js for
		// "dropper" and the input with display = 'none' it generates
		// since it is hidden, we can't send stuff directly to it. but we can make it visible and then upload.
		WebElement hiddenField = driver.findElement(By.xpath(".//div[contains(@class, 'submission-uploader__dropper')]/input"));
		if (driver instanceof JavascriptExecutor) {
		    ((JavascriptExecutor)driver).executeScript("arguments[0].style.display = 'block';", hiddenField);
		} else {
			// the normal driver we use can use JS, but let's be sure
		    throw new IllegalStateException("This driver does not support JavaScript!");
		}
		hiddenField.sendKeys(copiedImagePath.toString());
		
		// for some reason, it seems to display all your items afterwards, not just the draft, and the image may have a little
		// while to show up
		// during upload, the image looks nearly the same, the difference is the url, during upload it's a local one, after
		// it it's on cloudfront
		// uploading: <img class="thumbnail__img" src="/1e38971a7f090c817cc7abdd9312e828.jpg" alt="bigfile" data-reactid=".0.2.0.1.0.0.0.0.$submission-0.0.0.0.0">
		// uploaded:  <img class="thumbnail__img" src="https://d3gz42uwgl1r1y.cloudfront.net/kl/klaue/submission/2016/05/d2632e127759dddbc7f22a7a264ed677/315x315.jpg" alt="bigfile" data-reactid=".0.2.0.1.0.0.0.0.$submission-0.0.0.0.0">
		// TODO: find a less hacky way
		driver.findElement(By.xpath("//img[@alt='" + pseudoTitle + "' and contains(@src, 'cloudfront.net')]/..")).click();
		
		// upload complete, delete temp file
		try {
			Files.delete(copiedImagePath);
		} catch (IOException e) {
			// not a leg break
			e.printStackTrace();
		}
		
		WebElement form = driver.findElement(By.xpath(".//form[contains(@class, 'submission-form')]"));
		
		if (!imageInfo.getTitle().isEmpty()) {
			// selecting first, clearing wont work if not
			WebElement title = form.findElement(By.cssSelector("[id$=-title]"));
			title.findElement(By.xpath("..")).click();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			title.clear();
			title.sendKeys(imageInfo.getTitle());
		}
		
		if (!imageInfo.getDescription().isEmpty()) {
			WebElement descr = form.findElement(By.cssSelector("[id$=-description]"));
			descr.clear();
			descr.sendKeys(imageInfo.getDescription());
		}
		
		TreeSet<String> allTags = getTags(imageInfo);
		if (!allTags.isEmpty()) {
			WebElement input = form.findElement(By.cssSelector("[id$=-tags]"));
			for (String tag : allTags) {
				if (tag.length() <= 2) continue;
				input.sendKeys(tag);
				input.sendKeys(Keys.RETURN);
			}
		}
		
		
		// folders - find folder list
		WebElement colList = driver.findElement(By.className("edit-collections__list"));
		List<WebElement> colElements = colList.findElements(By.className("edit-collections__list__item__name"));
		ArrayList<String> foldersToAdd = new ArrayList<>();
		for (WebElement colElement : colElements) {
			String folderName = colElement.getText();
			if (imageInfo.getFolders().contains(folderName.toLowerCase().replace(" ", "_"))) {
				foldersToAdd.add(folderName);
			}
		}
		if (!foldersToAdd.isEmpty()) {
			WebElement input = form.findElement(By.cssSelector("[id$=-collections]"));
			for (String folder : foldersToAdd) {
				input.sendKeys(folder);
				input.sendKeys(Keys.RETURN);
			}
		}
		
		// rating
		WebElement ratingContainer = form.findElement(By.xpath(".//li[contains(@class, 'tabs__title') and text()='Rating:']/.."));
		if (imageInfo.getSexualRating() == RatingSexual.NUDITY_EX || imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_EX) {
			ratingContainer.findElement(By.xpath(".//span[contains(text(), 'Explicit')]")).click();
		} else if (imageInfo.getSexualRating() == RatingSexual.NUDITY_MOD || imageInfo.getViolenceRating() == RatingViolence.VIOLENCE_MOD) {
			WebElement e = ratingContainer.findElement(By.xpath(".//span[contains(text(), 'Mature')]/../.."));
			e.click();
		} else {
			// default
			//ratingContainer.findElement(By.name("//span[contains(text(), 'General')]/..")).click();
		}
		
		// status
		WebElement statusContainer = form.findElement(By.xpath(".//li[contains(@class, 'tabs__title') and text()='Status:']/.."));
		if (imageInfo.isUnlisted()) {
			statusContainer.findElement(By.xpath(".//span[contains(text(), 'Unlisted')]")).click();
		} else {
			statusContainer.findElement(By.xpath(".//span[contains(text(), 'Public')]")).click();
		}
		
		form.findElement(By.className("submission-form__save")).click();
		
		// now we have the confirm message
		if (!imageInfo.isUnlisted()) {
			WebElement confirmForm = driver.findElement(By.className("panel__content"));
			boolean notificationCheckboxSelected = confirmForm.findElement(By.id("publish")).isSelected();
			if (notificationCheckboxSelected && imageInfo.hasNoNotification()) {
				confirmForm.findElement(By.className("checkbox-input__label")).click();
			}
			confirmForm.findElement(By.className("button--action--create")).click();
		}
		
		// open correct end url
		// TODO: timeout or else the confirm dialog will not be correctly submitted
//		if (imageInfo.isUnlisted()) {
//			driver.get("https://beta.furrynetwork.com/submissions/artwork/unlisted/");
//		} else {
//			driver.get("https://beta.furrynetwork.com/submissions/artwork/public/");
//		}
		
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
					if (bytes > 32 * 1024 * 1024) { // 32 MB
						reasons.add("image file too large (>32MB)");
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

		if (imageInfo.getSexualRating() != ImageInfo.RatingSexual.NONE && (imageInfo.getOtherTags().contains("cub")
				|| imageInfo.getKinkTags().contains("cub"))) {
			reasons.add("cubs in sexual context");
		}
		
		if (imageInfo.isToScraps()) reasons.add("no support for scraps");
		//if (imageInfo.hasNoNotification()) reasons.add("no support for 'no notification'");
		if (imageInfo.isFriendsOnly()) reasons.add("no support for 'friends only'");

		return reasons;
	}

	@Override
	public ArrayList<String> getWarningReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<String>();

		reasons.add("type will be Artwork, not " + imageInfo.getType());

		TreeSet<String> allTags = getTags(imageInfo);
		boolean hasTooShortTags = false;
		for (String tag : allTags) {
			if (tag.length() > 2) continue;
			hasTooShortTags = true;
			break;
		}
		if (hasTooShortTags) reasons.add("tags under 3 characters will be ignored");
		
		return reasons;
	}

	@Override
	public String getName() {
		return "FurryNetwork";
	}


	private static TreeSet<String> getTags(ImageInfo imageInfo) {
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
}
