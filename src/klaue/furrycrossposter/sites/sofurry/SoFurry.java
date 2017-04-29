package klaue.furrycrossposter.sites.sofurry;

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
import klaue.furrycrossposter.sites.Site;

public class SoFurry extends Site {
	private WebDriver driver;
	private String user = null;
	private String password = null;
	Path sofurryPropertiesPath = FurryCrossposter.workingDir.resolve("sofurry.properties");
	
	@Override
	public boolean doUpload(final ImageInfo imageInfo) {
		if (!canUpload(imageInfo)) return false;
		
//		String[] credentials = getUserPassword(sofurryPropertiesPath);
//		if (credentials == null) return false; // no uplad without user/pass
//		
//		user = credentials[0];
//		password = credentials[1];
//		
//		SoFurryAuthentification soa = new SoFurryAuthentification(user, password);
//		// first request the submission of the furrycrossposter-image so we don't have to send the whole file to upload
//		// multiple time for sofurrys OTP login
//		TreeMap<String, String> params = new TreeMap<>();
//		params.put("id", "1043958");
//		String reply = soa.requestPost("http://api2.sofurry.com/std/getSubmissionDetails", params);
//		if (reply == null) {
//			JOptionPane.showMessageDialog(null, "Could not log in to SoFurry", "Error", JOptionPane.ERROR_MESSAGE);
//			// remove the entries from the properties
//			removeUserPasswordFromProperties(sofurryPropertiesPath);
//			return false;
//		}
//		
//		// if we came to this area, it means username worked and all, yay, so let's upload our file
//		params = new TreeMap<>();
//		params.put("f", "postBinary");
//		params.put("id", ""); // new
//		params.put("contentType", "artwork");
//		params.put("cb", "496"); // no clue what this is for
//		reply = soa.requestPostMultipart("https://chat.sofurry.com/ajaxfetch.php", params, "file", imageInfo.getImagePath().toFile());
//		System.out.println(reply);
//		
//		return true;

		Path imagePath = imageInfo.getImagePath();
		Path thumbPath = imageInfo.getThumbPath();
		
		
		driver = getDriver();
		
		driver.get("https://www.sofurry.com/user/login");
		
		// wait for login
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@href='/upload']")));
		
		driver.get("https://www.sofurry.com/upload/details?contentType=1");
		
		driver.findElement(By.id("UploadForm_P_title")).sendKeys(imageInfo.getTitle());
		
		driver.findElement(By.id("UploadForm_binarycontent")).sendKeys(imagePath.toString());
		
		// upload dialog progress thingy
		wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.className("ui-dialog"))));
		
		if (thumbPath != null) {
			driver.findElement(By.id("UploadForm_binarycontent_5")).sendKeys(thumbPath.toString());
			// upload dialog progress thingy
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.className("ui-dialog"))));
		}
		
		// folder
		Select folderSelect = new Select(driver.findElement(By.id("UploadForm_folderId")));
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
			((JavascriptExecutor)driver).executeScript("arguments[0].click()", driver.findElement(By.id("UploadForm_contentLevel_0")));
		} else {
			// check just for cub here, rest the user can choose himself afterwards but cub could bring drama
			if (getTags(imageInfo).trim().matches("^(.*, )*cub(, .*)*$")) {
				//driver.findElement(By.xpath("//input[@id='UploadForm_contentLevel_2']/../span")).click();
				// forcing it to be clicked through js as above way stopped to work for some reason
				//TODO: why tho? makes no sense
				((JavascriptExecutor)driver).executeScript("arguments[0].click()", driver.findElement(By.id("UploadForm_contentLevel_2")));
			} else {
				//driver.findElement(By.xpath("//input[@id='UploadForm_contentLevel_1']/../span")).click();
				// forcing it to be clicked through js as above way stopped to work for some reason
				//TODO: why tho? makes no sense
				((JavascriptExecutor)driver).executeScript("arguments[0].click()", driver.findElement(By.id("UploadForm_contentLevel_1")));
			}
		}
		
		if (imageInfo.isUnlisted()) {
			//driver.findElement(By.xpath("//input[@id='UploadForm_P_hidePublic_3']/../span")).click();
			// forcing it to be clicked through js as above way stopped to work for some reason
			//TODO: why tho? makes no sense
			((JavascriptExecutor)driver).executeScript("arguments[0].click()", driver.findElement(By.id("UploadForm_P_hidePublic_3")));
		} else if (imageInfo.isFriendsOnly()) {
			//driver.findElement(By.xpath("//input[@id='UploadForm_P_hidePublic_2']/../span")).click();
			// forcing it to be clicked through js as above way stopped to work for some reason
			//TODO: why tho? makes no sense
			((JavascriptExecutor)driver).executeScript("arguments[0].click()", driver.findElement(By.id("UploadForm_P_hidePublic_2")));
		} else {
			//driver.findElement(By.xpath("//input[@id='UploadForm_P_hidePublic_0']/../span")).click();
			// forcing it to be clicked through js as above way stopped to work for some reason
			//TODO: why tho? makes no sense
			((JavascriptExecutor)driver).executeScript("arguments[0].click()", driver.findElement(By.id("UploadForm_P_hidePublic_0")));
		}
		
		if (!imageInfo.getDescription().isEmpty()) {
			driver.findElement(By.id("UploadForm_description")).sendKeys(imageInfo.getDescription());
		}
		
		String tags = getTags(imageInfo);
		driver.findElement(By.id("sf-upload-tags")).sendKeys(tags);
		
		//driver.findElement(By.className("bigsubmit")).click();
		// forcing it to be clicked through js as above way stopped to work for some reason
		//TODO: why tho? makes no sense
		((JavascriptExecutor)driver).executeScript("arguments[0].click()", driver.findElement(By.className("bigsubmit")));
		
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
		ArrayList<String> reasons = new ArrayList<String>();

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
			if (!containsIntersex) containsIntersex = !(gender.equals(Gender.M2F) || gender.equals(Gender.F2M) || gender.equals(Gender.MALE) || gender.equals(Gender.FEMALE) || gender.equals(Gender.AMBIGUOUS));
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
