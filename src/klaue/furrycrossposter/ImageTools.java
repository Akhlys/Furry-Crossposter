package klaue.furrycrossposter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ImageTools {
	/**
	 * Returns a new image that is the original image, resized preserving aspect ratio, to not be larger than
	 * given amount of pixels
	 * @param pixelnum the max amount of pixels, 36000000 for 36MP 
	 * @param original
	 * @return a resized BufferedImage instance
	 */
	public static BufferedImage getResizedInstance(long pixelnum, BufferedImage original) {
		// taken from https://www.nayuki.io/page/resizing-images-by-area
		int originalWidth  = original.getWidth();
		int originalHeight = original.getHeight();
		long resizedArea    = pixelnum;
		
		// Calculate outputs
		double resizedWidth  = Math.sqrt(resizedArea * originalWidth / originalHeight);
		double resizedHeight = Math.sqrt(resizedArea * originalHeight / originalWidth);
		// floor is playing it safe, round() might in some cases result in a pic that is slightly larger than pixelnum
		resizedWidth  = resizedWidth  > 0.5 ? Math.floor(resizedWidth)  : 1;
		resizedHeight = resizedHeight > 0.5 ? Math.floor(resizedHeight) : 1;
		
		// refactor, use scalr
		return Scalr.resize(original, (int)resizedWidth, (int)resizedHeight);
	}
	
	/**
	 * Returns a new image that is the original image, resized preserving aspect ratio, to not be larger than
	 * given amount of pixels
	 * @param pixelnum the max amount of pixels, 36000000 for 36MP 
	 * @param imageFile
	 * @return a resized BufferedImage instance
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static BufferedImage getResizedInstance(long pixelnum, File imageFile) throws IllegalArgumentException, IOException {
		return getResizedInstance(pixelnum, getImageFromFile(imageFile));
	}
	
	/**
	 * Returns a new image that is the original image, resized preserving aspect ratio, so that neither with is larger
	 * than maxWidth nor height larger than MaxHeight.
	 * As the resize is preserving aspect ratio, one size may be considerably smaller than max
	 * @param maxWidth the maximum width
	 * @param maxHeight the maximum height
	 * @param imageFile
	 * @return a resized BufferedImage instance
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static BufferedImage getResizedInstance(int maxWidth, int maxHeight, File imageFile) throws IllegalArgumentException, IOException {
		// refactor, use scalr
		return Scalr.resize(getImageFromFile(imageFile), maxWidth, maxHeight);
	}

	/**
	 * Saves an image, that is the original image, resized preserving aspect ratio, to not be larger than
	 * given amount of pixels
	 * @param pixelnum the max amount of pixels, 36000000 for 36MP 
	 * @param image
	 * @param targetFile
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static void getResizedFile(long pixelnum, BufferedImage image, File targetFile) throws IllegalArgumentException, IOException {
		saveImageToFile(getResizedInstance(pixelnum, image), targetFile);
	}
	
	/**
	 * Saves an image, that is the original image, resized preserving aspect ratio, so that neither with is larger
	 * than maxWidth nor height larger than MaxHeight.
	 * As the resize is preserving aspect ratio, one size may be considerably smaller than max
	 * @param maxWidth the maximum width
	 * @param maxHeight the maximum height
	 * @param image
	 * @param targetFile
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static void getResizedFile(int maxWidth, int maxHeight, BufferedImage image, File targetFile) throws IllegalArgumentException, IOException {
		// refactor, use scalr
		saveImageToFile(Scalr.resize(image, maxWidth, maxHeight), targetFile);
	}
	
	/**
	 * Saves an image, that is the original image, resized preserving aspect ratio, to not be larger than
	 * given amount of pixels
	 * @param pixelnum the max amount of pixels, 36000000 for 36MP 
	 * @param imageFile
	 * @param targetFile
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static void getResizedFile(long pixelnum, File imageFile, File targetFile) throws IllegalArgumentException, IOException {
		saveImageToFile(getResizedInstance(pixelnum, getImageFromFile(imageFile)), targetFile);
	}
	
	/**
	 * Saves an image, that is the original image, resized preserving aspect ratio, so that neither with is larger
	 * than maxWidth nor height larger than MaxHeight.
	 * As the resize is preserving aspect ratio, one size may be considerably smaller than max
	 * @param maxWidth the maximum width
	 * @param maxHeight the maximum height
	 * @param imageFile
	 * @param targetFile
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static void getResizedFile(int maxWidth, int maxHeight, File imageFile, File targetFile) throws IllegalArgumentException, IOException {
		// refactor, use scalr
		saveImageToFile(Scalr.resize(getImageFromFile(imageFile), maxWidth, maxHeight), targetFile);
	}
	
	private static BufferedImage getImageFromFile(File imageFile) throws IOException, IllegalArgumentException {
		BufferedImage image = ImageIO.read(imageFile);
		if (image == null) throw new IllegalArgumentException("No image type");
		return image;
	}
	
	private static void saveImageToFile(BufferedImage image, File targetFile) throws IOException, IllegalArgumentException {
		if (targetFile.exists()) targetFile.delete();
		int i = targetFile.getName().toString().lastIndexOf('.');
		if (i == -1) throw new IllegalArgumentException("Save file has no extension/type: " + targetFile.getName());
		String extension = targetFile.getName().toString().substring(i+1).toLowerCase();
		ImageIO.write(image, extension, targetFile);
	}
}
