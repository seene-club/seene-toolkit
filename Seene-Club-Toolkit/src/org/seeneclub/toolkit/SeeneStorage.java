package org.seeneclub.toolkit;

import java.io.File;
import java.text.SimpleDateFormat;

import org.seeneclub.domainvalues.LogLevel;

public class SeeneStorage {
	
	private String path;
	private File storageBaseDir;
	private File publicDir;
	private File privateDir;
	private File offlineDir;
	
	// check and initialize the storage
	public Boolean initializer() {
		storageBaseDir = new File(this.path);
		if(storageBaseDir.exists() && storageBaseDir.isDirectory()) {
				
			publicDir = new File(this.path + File.separator + "public backup");
			boolean publicOK = createSubCatalogueDir(publicDir);
				
			privateDir = new File(this.path + File.separator + "private backup");
			boolean privateOK = createSubCatalogueDir(privateDir);
				
			offlineDir = new File(this.path + File.separator + "local seenes");
			boolean offlineOK = createSubCatalogueDir(offlineDir);
			
			if ((publicOK) && (privateOK) && (offlineOK)) {
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
	
	public static String generateSeeneFolderName(SeeneObject sO) {
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		return new String(sdf.format(sO.getCaptured_at()) + " " + sO.getCaption().replaceAll("\n",  " ").replaceAll("/", "."));
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
	public File getOfflineDir() {
		return offlineDir;
	}
	public void setOfflineDir(File offlineDir) {
		this.offlineDir = offlineDir;
	}

}
