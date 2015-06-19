package org.seeneclub.toolkit;

import java.io.File;
import java.text.SimpleDateFormat;

import org.seeneclub.domainvalues.LogLevel;

public class SeeneStorage {
	
	private String path;
	private File storageBaseDir;
	private File publicDir;
	private File privateDir;
	private File othersDir;
	private File offlineDir;
	private File uploadsDir;
	
	// check and initialize the storage
	public Boolean initializer() {
		storageBaseDir = new File(this.path);
		if(storageBaseDir.exists() && storageBaseDir.isDirectory()) {
				
			publicDir = new File(this.path + File.separator + "public backup");
			boolean publicOK = createSubCatalogueDir(publicDir);
				
			privateDir = new File(this.path + File.separator + "private backup");
			boolean privateOK = createSubCatalogueDir(privateDir);
			
			othersDir = new File(this.path + File.separator + "others backup");
			boolean othersOK = createSubCatalogueDir(othersDir);
				
			offlineDir = new File(this.path + File.separator + "local seenes");
			boolean offlineOK = createSubCatalogueDir(offlineDir);
			
			uploadsDir = new File(this.path + File.separator + "uploads");
			boolean uploadsOK = createSubCatalogueDir(uploadsDir);
			
			if ((publicOK) && (privateOK) && (othersOK) && (offlineOK) && (uploadsOK)) {
				return true;
			} else {
				SeeneToolkit.log("could not create all storage subdirs!",LogLevel.fatal);
			}
			
			return false;
		} else {
			SeeneToolkit.log("storage does not exist! Please, check your configuration!",LogLevel.error);
			return false;
		}
	}


	// method to create a directory 
	private Boolean createSubCatalogueDir(File fd) {
			
		boolean success = false;
			
		if(fd.exists() && fd.isDirectory()) {
			SeeneToolkit.log(fd.getPath() + " exists.",LogLevel.debug);
			success = true;
		} else {
			success = fd.mkdirs();
			if (success) {
				SeeneToolkit.log(fd.getPath() + " created!",LogLevel.info);
			} else {
				SeeneToolkit.log("could not create " + fd.getPath(),LogLevel.error);
			}
		}
		if (success) return true;	
		return false;
	}
	
	public static String generateSeeneFolderName(SeeneObject sO,String username) {
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH\u02d0mm\u02d0ss" ); // Using triangular colon because of Windows File System
		
		String folderName;
		String sCaption = sO.getCaption().replaceAll("\n",  " ").replaceAll("/", ".");
		if (sCaption.length()>80) sCaption = sCaption.substring(0, 80); 
				
		if (sO.getUserinfo().equalsIgnoreCase(username)) {
			folderName = new String(sdf.format(sO.getCaptured_at()) + " " + sCaption).trim();
		} else {
			folderName = new String(sO.getUserinfo() + " " + sdf.format(sO.getCaptured_at()) + " " + sCaption).trim();
		}
		
		// Windows is different
		if ((System.getProperty("os.name").length()>=7) && (System.getProperty("os.name").substring(0, 7).equals("Windows"))) {
			return folderName.replaceAll(":", ";")
					         .replaceAll("\"", "'")
					         .replaceAll("\\?", "")
					         .replaceAll("\\.", "_");
							 // to be continued?
		}	
		return folderName;
	}
	

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public File getStorageBaseDir() {
		return storageBaseDir;
	}
	public void setStorageBaseDir(File storageBaseDir) {
		this.storageBaseDir = storageBaseDir;
	}
	public File getPublicDir() {
		return publicDir;
	}
	public void setPublicDir(File publicDir) {
		this.publicDir = publicDir;
	}
	public File getPrivateDir() {
		return privateDir;
	}
	public void setPrivateDir(File privateDir) {
		this.privateDir = privateDir;
	}
	public File getOthersDir() {
		return othersDir;
	}
	public void setOthersDir(File othersDir) {
		this.othersDir = othersDir;
	}
	public File getOfflineDir() {
		return offlineDir;
	}
	public void setOfflineDir(File offlineDir) {
		this.offlineDir = offlineDir;
	}
	public File getUploadsDir() {
		return uploadsDir;
	}
	public void setUploadsDir(File uploadsDir) {
		this.uploadsDir = uploadsDir;
	}

}
