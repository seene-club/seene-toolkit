package org.seeneclub.toolkit;

import java.awt.Image;
import java.io.File;
import java.net.URL;

public class SeeneTexture {
	
	private Image textureImage;
	private File textureFile;
	private URL textureURL;
	
	// Constructors
	public SeeneTexture() {
		textureImage=null;
		textureFile=null;
		textureURL=null;
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
