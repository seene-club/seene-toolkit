package org.seeneclub.toolkit;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class SeeneObject {
	/// header
	private String caption;
	private Date captured_at;
	private String filter_code;
	private int flash_level;
	private UUID identifier;
	private int orientation;
	private int shared;
	private int storage_version;
	
	/// scene.oemodel
	private SeeneModel model;
	
	/// poster.jpg
	private SeeneTexture poster;

	SeeneObject() {
		caption = "#synthetic (Uploaded by https://github.com/seene-club/seene-toolkit by @sclub)";
		captured_at = new Date();
		filter_code = "none";
		flash_level = 0;
		identifier = UUID.randomUUID();
		orientation = 0;
		shared = 0;
		storage_version = 3;
	}
	
	// Getter and Setter
	public File getModelFile() {
		return model.getModelFile();
	}
	
	public void setModelFile(File modelFile) {
		model.setModelFile(modelFile);
	}
	
	public String getCaption() {
		return caption == null? "": caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Date getCaptured_at() {
		return captured_at;
	}

	public void setCaptured_at(Date captured_at) {
		this.captured_at = captured_at;
	}

	public String getFilter_code() {
		return filter_code;
	}

	public void setFilter_code(String filter_code) {
		this.filter_code = filter_code;
	}

	public int getFlash_level() {
		return flash_level;
	}

	public void setFlash_level(int flash_level) {
		this.flash_level = flash_level;
	}

	public UUID getIdentifier() {
		return identifier;
	}

	public void setIdentifier(UUID identifier) {
		this.identifier = identifier;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public int getShared() {
		return shared;
	}

	public void setShared(int shared) {
		this.shared = shared;
	}

	public int getStorage_version() {
		return storage_version;
	}

	public void setStorage_version(int storage_version) {
		this.storage_version = storage_version;
	}

	public SeeneModel getModel() {
		return model;
	}

	public void setModel(SeeneModel model) {
		this.model = model;
	}

	public SeeneTexture getPoster() {
		return poster;
	}

	public void setPoster(SeeneTexture poster) {
		this.poster = poster;
	}

	public File getPosterFile() {
		return poster.getTextureFile();
	}
	public void setTextureFile(File textureFile) {
		poster.setTextureFile(textureFile);
	}

}
