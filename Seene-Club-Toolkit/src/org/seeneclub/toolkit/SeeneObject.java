package org.seeneclub.toolkit;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class SeeneObject {
	
	// header
	private String caption;
	private Date captured_at;
	private String short_code;
	private String filter_code;
	private int flash_level;
	private UUID identifier;
	private int orientation;
	private int shared;
	private int storage_version;
	private String userinfo;
	private URL avatarURL;
	private String localname;

    // model and poster
	private SeeneModel model;
	private SeeneTexture poster;
	
	// XMP Components
	private File XMP_original;		// poster_original.jpg
	private File XMP_depthpng;		// poster_depth.png
	private File XMP_combined;		// poster_xmp.jpg
	
	// AWS-Metadata (upload)
	private SeeneAWSmetadataOldMethod awsmetaOldMethod;
	private SeeneAWSUploadMeta awsMeta;

	SeeneObject() {
		caption = "#synthetic (created with https://github.com/seene-club/seene-toolkit)";
		captured_at = new Date();
		filter_code = "none";
		flash_level = 0;
		identifier = UUID.randomUUID();
		orientation = 0;
		shared = 0;
		storage_version = 3;
		poster = new SeeneTexture();
		model = new SeeneModel();
		awsMeta = new SeeneAWSUploadMeta();
	}
	
	// constructor for a seene stored at local filesystem
	public SeeneObject(File seeneFolder) {
		if(seeneFolder.exists()) {
	    	  if (seeneFolder.isDirectory()) {
	    		  setModel(new SeeneModel(new File(seeneFolder.getAbsolutePath() + File.separator + STK.SEENE_MODEL)));
	    		  setPoster(new SeeneTexture(new File(seeneFolder.getAbsolutePath() + File.separator + STK.SEENE_TEXTURE)));
	    		  setXMP_combined(new File(seeneFolder.getAbsolutePath() + File.separator + STK.XMP_COMBINED_JPG));
	    		  setXMP_depthpng(new File(seeneFolder.getAbsolutePath() + File.separator + STK.XMP_DEPTH_PNG));
	    		  setXMP_original(new File(seeneFolder.getAbsolutePath() + File.separator + STK.XMP_ORIGINAL_JPG));
	    	  }
		}
	}
	
	// Getter and Setter
	
	public void loadModelfromXMP_depthpng() {
		model.loadModelDataFromPNG(XMP_depthpng, XMP_combined);
	}

	public String getCaption() {
		if ((caption==null) || (caption.length()==0)) 
			caption = "#synthetic (created with https://github.com/seene-club/seene-toolkit)";
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public Date getCaptured_at() {
		if (captured_at==null) 
			captured_at = new Date();
		return captured_at;
	}
	public void setCaptured_at(Date captured_at) {
		this.captured_at = captured_at;
	}
	public String getFilter_code() {
		if ((filter_code==null) || (filter_code.length()==0))
			filter_code = "none";
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
	public void setIdentifier(String identifier) {
		this.identifier = UUID.fromString(identifier);
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
		if (storage_version==0) storage_version=3;
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
	public File getModelFile() {
		return model.getModelFile();
	}
	public void setModelFile(File modelFile) {
		model.setModelFile(modelFile);
	}
	public URL getModelURL() {
		return model.getModelURL();
	}
	public void setModelURL(URL modelURL) {
		model.setModelURL(modelURL);
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
	public URL getPosterURL() {
		return poster.getTextureURL();
	}
	public void setTextureURL(URL textureURL) {
		poster.setTextureURL(textureURL);
	}
	public String getShortCode() {
		return short_code;
	}
	public void setShortCode(String short_code) {
		this.short_code = short_code;
	}
	public String getUserinfo() {
		return userinfo;
	}
	public void setUserinfo(String userinfo) {
		this.userinfo = userinfo;
	}
	public URL getAvatarURL() {
		return avatarURL;
	}
	public void setAvatarURL(URL avatarURL) {
		this.avatarURL = avatarURL;
	}
	public String getLocalname() {
		return localname;
	}
	public void setLocalname(String localname) {
		this.localname = localname;
	}
	public SeeneAWSmetadataOldMethod getAWSmetaOldMethod() {
		return awsmetaOldMethod;
	}
	public void setAWSmetaOldMethod(SeeneAWSmetadataOldMethod awsmeta) {
		this.awsmetaOldMethod = awsmeta;
	}
	public File getXMP_original() {
		return XMP_original;
	}
	public void setXMP_original(File xMP_unblurred) {
		XMP_original = xMP_unblurred;
	}
	public File getXMP_depthpng() {
		return XMP_depthpng;
	}
	public void setXMP_depthpng(File xMP_depthpng) {
		XMP_depthpng = xMP_depthpng;
	}
	public File getXMP_combined() {
		return XMP_combined;
	}
	public void setXMP_combined(File xMP_combined) {
		XMP_combined = xMP_combined;
	}
	public SeeneAWSUploadMeta getAwsMeta() {
		return awsMeta;
	}
	public void setAwsMeta(SeeneAWSUploadMeta awsMeta) {
		this.awsMeta = awsMeta;
	}
}
