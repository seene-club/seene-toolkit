package org.seeneclub.toolkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardCopyOption.*;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.seeneclub.domainvalues.LogLevel;


public class SeeneDownloader extends Thread {
	
	private SeeneObject seeneObject;
	private File targetDir;
	private String username;
	private ProxyData proxyData;
	

	SeeneDownloader(SeeneObject seeneObject, File targetDir, String username, ProxyData proxyData) {
		this.seeneObject = seeneObject;
		this.targetDir = targetDir;
		this.username = username;
		this.proxyData = proxyData;
	}
	
	private final Set<SeeneDownloadCompleteListener> listeners = new CopyOnWriteArraySet<SeeneDownloadCompleteListener>();

	public final void addListener(final SeeneDownloadCompleteListener seeneDownloadCompleteListener) {
		listeners.add((SeeneDownloadCompleteListener) seeneDownloadCompleteListener);
	}
	
	public final void removeListener(final SeeneDownloadCompleteListener listener) {
		listeners.remove(listener);
	}
	
	private final void notifyListeners() {
		for (SeeneDownloadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this);
		}
	}

	@Override
	public void run() {
		try {
			Boolean successTexture = false;
			Boolean successModel = false;
			
			String folderName = SeeneStorage.generateSeeneFolderName(seeneObject, username);
			File seeneFolder = new File(targetDir.getAbsolutePath() + File.separator + folderName);
			File seeneTempFolder = new File(targetDir.getAbsolutePath() + File.separator + ".temp" + File.separator + folderName);
    	
			if (!seeneFolder.exists()) {
				seeneTempFolder.mkdirs();
				successTexture = Helper.downloadFile(seeneObject.getPosterURL(), proxyData, seeneTempFolder);
				successModel = Helper.downloadFile(seeneObject.getModelURL(), proxyData, seeneTempFolder);
			} else {
				org.seeneclub.toolkit.SeeneToolkit.log(seeneFolder.getAbsolutePath() + " already exists!", LogLevel.info);
			}
    	
			if ((successModel) && (successTexture)) {
				try {
					Files.move(seeneTempFolder.toPath(), seeneFolder.toPath(), StandardCopyOption.ATOMIC_MOVE);
					org.seeneclub.toolkit.SeeneToolkit.log("DOWNLOAD COMPLETE: " + seeneFolder.getAbsolutePath(), LogLevel.info);
					String seenesname = seeneFolder.getName();
					
					SeeneToolkit.generateXMP(seeneFolder.getAbsolutePath());
					File fsource = new File(seeneFolder.getAbsolutePath() + File.separator + STK.XMP_COMBINED_JPG);
					File fdest = new File(seeneFolder.toPath().getParent().getParent().toString() + File.separator + seenesname + ".jpg");
					
					Files.move(fsource.toPath(), fdest.toPath(), StandardCopyOption.ATOMIC_MOVE);
					
					if (seeneObject.getUserinfo().equalsIgnoreCase(username)) {
						Helper.createFolderIcon(seeneFolder, null);
					} else {
						Helper.createFolderIcon(seeneFolder, seeneObject.getAvatarURL());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} finally {
			notifyListeners();
		}
	}
	
	
	// Getter and Setter
	public SeeneObject getSeeneObject() {
		return seeneObject;
	}

	public void setSeeneObject(SeeneObject seeneObject) {
		this.seeneObject = seeneObject;
	}

	public File getTargetDir() {
		return targetDir;
	}

	public void setTargetDir(File targetDir) {
		this.targetDir = targetDir;
	}

	public ProxyData getProxyData() {
		return proxyData;
	}

	public void setProxyData(ProxyData proxyData) {
		this.proxyData = proxyData;
	}
}
