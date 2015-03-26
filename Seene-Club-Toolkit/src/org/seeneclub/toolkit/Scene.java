package org.seeneclub.toolkit;

import java.util.*;

public class Scene {
	/// header
	public String caption;
	public Date captured_at;
	public String filter_code;
	public int flash_level;
	public UUID identifier;
	public int orientation;
	public int shared;
	public int storage_version;
	
	/// scene.oemodel
	public File model;
	
	/// poster.jpg
	public File poster;

	Scene() {
		caption = "#synthetic (Uploaded by https://github.com/seene-club/seene-toolkit by @sclub)"
		captured_at = new Date();
		filter_code = "none";
		flash_level = 0;
		identifier = new UUID();
		orientation = 0;
		shared = 0;
		storage_version = 3;
	}

}
