package klaue.furrycrossposter.sites;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

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

public class SoFurry extends Site {
	private WebDriver driver;
	Path sofurryPropertiesPath = FurryCrossposter.workingDir.resolve("sofurry.properties");
	
	@Override
	public boolean doUpload(final ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;

		Path imagePath = imageInfo.getImagePath();
		Path thumbPath = imageInfo.getThumbPath();
		
		
		this.driver = getDriver();
		
		this.driver.get("https://www.sofurry.com/user/login");
		
		// wait for login
		WebDriverWait wait = new WebDriverWait(this.driver, 60);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@href='/upload']")));
		
		this.driver.get("https://www.sofurry.com/upload/details?contentType=1");
		
		this.driver.findElement(By.id("UploadForm_P_title")).sendKeys(imageInfo.getTitle());
		
		this.driver.findElement(By.id("UploadForm_binarycontent")).sendKeys(imagePath.toString());
		
		// upload dialog progress thingy
		wait.until(ExpectedConditions.invisibilityOfAllElements(this.driver.findElements(By.className("ui-dialog"))));
		
		if (thumbPath != null) {
			this.driver.findElement(By.id("UploadForm_binarycontent_5")).sendKeys(thumbPath.toString());
			// upload dialog progress thingy
			wait.until(ExpectedConditions.invisibilityOfAllElements(this.driver.findElements(By.className("ui-dialog"))));
		}
		
		// folder
		Select folderSelect = new Select(this.driver.findElement(By.id("UploadForm_folderId")));
		for (WebElement option : folderSelect.getOptions()) {
			if (imageInfo.getFolders().contains(option.getText().trim().toLowerCase().replace(" ", "_"))) {
				folderSelect.selectByVisibleText(option.getText());
				break; // only one folder
			}
		}
		
		// rating
		// we have to click on the span silbling as the input is invisible
		if (imageInfo.getSexualRating() == RatingSexual.NONE && imageInfo.getViolenceRating() == RatingViolence.NONE) {
			//driver.findElement(By.xpath("//input[@id='UploadForm_contentLevel_0']/../span")).click();
			// forcing it to be clicked through js as above way stopped to work for some reason
			//TODO: why tho? makes no sense
			((JavascriptExecutor)this.driver).executeScript("arguments[0].click()", this.driver.findElement(By.id("UploadForm_contentLevel_0")));
		} else {
			// check just for cub here, rest the user can choose himself afterwards but cub could bring drama
			if (getTags(imageInfo).trim().matches("^(.*, )*cub(, .*)*$")) {
				//driver.findElement(By.xpath("//input[@id='UploadForm_contentLevel_2']/../span")).click();
				// forcing it to be clicked through js as above way stopped to work for some reason
				//TODO: why tho? makes no sense
				((JavascriptExecutor)this.driver).executeScript("arguments[0].click()", this.driver.findElement(By.id("UploadForm_contentLevel_2")));
			} else {
				//driver.findElement(By.xpath("//input[@id='UploadForm_contentLevel_1']/../span")).click();
				// forcing it to be clicked through js as above way stopped to work for some reason
				//TODO: why tho? makes no sense
				((JavascriptExecutor)this.driver).executeScript("arguments[0].click()", this.driver.findElement(By.id("UploadForm_contentLevel_1")));
			}
		}
		
		if (imageInfo.isUnlisted()) {
			//driver.findElement(By.xpath("//input[@id='UploadForm_P_hidePublic_3']/../span")).click();
			// forcing it to be clicked through js as above way stopped to work for some reason
			//TODO: why tho? makes no sense
			((JavascriptExecutor)this.driver).executeScript("arguments[0].click()", this.driver.findElement(By.id("UploadForm_P_hidePublic_3")));
		} else if (imageInfo.isFriendsOnly()) {
			//driver.findElement(By.xpath("//input[@id='UploadForm_P_hidePublic_2']/../span")).click();
			// forcing it to be clicked through js as above way stopped to work for some reason
			//TODO: why tho? makes no sense
			((JavascriptExecutor)this.driver).executeScript("arguments[0].click()", this.driver.findElement(By.id("UploadForm_P_hidePublic_2")));
		} else {
			//driver.findElement(By.xpath("//input[@id='UploadForm_P_hidePublic_0']/../span")).click();
			// forcing it to be clicked through js as above way stopped to work for some reason
			//TODO: why tho? makes no sense
			((JavascriptExecutor)this.driver).executeScript("arguments[0].click()", this.driver.findElement(By.id("UploadForm_P_hidePublic_0")));
		}
		
		if (!imageInfo.getDescription().isEmpty()) {
			this.driver.findElement(By.id("UploadForm_description")).sendKeys(imageInfo.getDescription());
		}
		
		String tags = getTags(imageInfo);
		this.driver.findElement(By.id("sf-upload-tags")).sendKeys(tags);
		
		//driver.findElement(By.className("bigsubmit")).click();
		// forcing it to be clicked through js as above way stopped to work for some reason
		//TODO: why tho? makes no sense
		((JavascriptExecutor)this.driver).executeScript("arguments[0].click()", this.driver.findElement(By.className("bigsubmit")));
		
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
			// is file of correct type?
			int i = imageInfo.getImagePath().getFileName().toString().lastIndexOf('.');
			String extension = "";
			if (i > 0) {
				extension = imageInfo.getImagePath().getFileName().toString().substring(i+1).toLowerCase();
			}
			if (!extension.equals("jpeg") && !extension.equals("jpg") && !extension.equals("png") && !extension.equals("gif")) {
				reasons.add("unsupported image type ." + extension);
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
			}
		}
		
		if (imageInfo.isToScraps()) reasons.add("no support for scraps");
		if (imageInfo.hasNoNotification()) reasons.add("no support for 'no notification'");

		if (imageInfo.getTitle().isEmpty()) reasons.add("no title");
		
		int tagAmount = imageInfo.getGenders().size() + imageInfo.getSpeciesTags().size() + imageInfo.getKinkTags().size() + imageInfo.getOtherTags().size();
		if (tagAmount < 2) reasons.add("needs at least 2 tags (incl. gender)");

		return reasons;
	}

	@Override
	public ArrayList<String> getWarningReasons(ImageInfo imageInfo) {
		ArrayList<String> reasons = new ArrayList<>();

		reasons.add("no type support, all types will result in same");
		
		return reasons;
	}

	@Override
	public String getName() {
		return "SoFurry";
	}

	private static String getTags(ImageInfo imageInfo) {
		StringBuffer tags = new StringBuffer();
		
		// add genders first
		boolean containsIntersex = false;
		for (Gender gender : imageInfo.getGenders()) {
			tags.append(gender.getTag()).append(", ");
			if (!containsIntersex) containsIntersex = !(gender.equals(Gender.M2F) || gender.equals(Gender.F2M) || gender.equals(Gender.MALE) || gender.equals(Gender.FEMALE));
		}
		if (containsIntersex) tags.append("intersex, ");	
		
		// add other tags
		for (String species : imageInfo.getSpeciesTags()) {
			tags.append(species).append(", ");
		}
		for (String kink : imageInfo.getKinkTags()) {
			tags.append(kink).append(", ");
		}
		for (String other : imageInfo.getOtherTags()) {
			tags.append(other).append(", ");
		}
		tags.deleteCharAt(tags.length() - 1);
		tags.deleteCharAt(tags.length() - 1);
		return tags.toString();
	}
}
