package org.seeneclub.toolkit;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.seeneclub.domainvalues.LogLevel;

public class SeeneTexture {
	
	private Image textureImage;
	private File textureFile;
	private URL textureURL;
	
	// Constructors
	public SeeneTexture() {
		textureImage = new BufferedImage(1936, 1936, BufferedImage.TYPE_INT_ARGB);
		textureFile = null;
		textureURL = null;
	}
	SeeneTexture(Image sImage) {
	     this.textureImage = sImage;
	}
	SeeneTexture(File sFile) {
	     this.textureFile = sFile;
	}
	SeeneTexture(URL sURL) {
	     this.textureURL = sURL;
	}
	
	public void saveTextureRotatedToFile(File tF, int degree) {
		try {
			int w = textureImage.getWidth(null);
			int h = textureImage.getHeight(null);
			BufferedImage bImage      = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D bImageGraphics = bImage.createGraphics();
			if (degree != 0) bImageGraphics.rotate(Math.toRadians(degree), w/2, h/2);
			bImageGraphics.drawImage(textureImage, null, null);
			bImageGraphics.dispose();
			
			ImageIO.write(bImage, "jpg", tF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveTextureToFile(File tF) {
		saveTextureRotatedToFile(tF, 0);
	}
	
	public void loadTextureFromFile() {
		loadTextureFromFile(textureFile);
	}
	
	private Image loadTextureFromFile(File tF) {
		textureFile = tF;
		try {
			 textureImage = ImageIO.read(tF);
		} catch (IOException e) {
			SeeneToolkit.log(e.getMessage(),LogLevel.error);
		}
		return textureImage;
	}
	
	
	// Getters and Setters
	public Image getTextureImage() {
		return textureImage;
	}
	public void setTextureImage(Image textureImage) {
		this.textureImage = textureImage;
	}
	public File getTextureFile() {
		return textureFile;
	}
	public void setTextureFile(File textureFile) {
		this.textureFile = textureFile;
	}
	public URL getTextureURL() {
		return textureURL;
	}
	public void setTextureURL(URL textureURL) {
		this.textureURL = textureURL;
	}
	
}
