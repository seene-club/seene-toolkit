package org.seeneclub.toolkit;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

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
	
	public void saveTextureToFile(File tF) {
		try {
			BufferedImage bImage      = new BufferedImage(textureImage.getWidth(null), textureImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D bImageGraphics = bImage.createGraphics();
			bImageGraphics.drawImage(textureImage, null, null);
			ImageIO.write(bImage, "jpg", tF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadTextureFromFile() {
		loadTextureFromFile(textureFile);
	}
	
	private Image loadTextureFromFile(File tF) {
		textureFile = tF;
		try {
			 textureImage = ImageIO.read(tF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
