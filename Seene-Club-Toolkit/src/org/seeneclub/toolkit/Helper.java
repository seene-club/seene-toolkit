package org.seeneclub.toolkit;

import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.seeneclub.domainvalues.LogLevel;

public class Helper {
	
	public static void createFolderIcon(File sFolder, URL avatarURL) {
		try {
			
			BufferedImage overlayOriginialSize = null;

			// First try to find Seene's proprietary poster.jpg 
			File jpgPoster = new File(sFolder.getAbsolutePath() + File.separator + STK.SEENE_TEXTURE);		
			if (jpgPoster.exists()) {
				overlayOriginialSize = ImageIO.read(jpgPoster);
			} else {
				// Second try to find a XMP enhanced JPG
				jpgPoster = new File(sFolder.getAbsolutePath() + File.separator + STK.XMP_COMBINED_JPG);
				if (jpgPoster.exists()) {
					overlayOriginialSize = ImageIO.read(jpgPoster);
					overlayOriginialSize = rotateAndResizeImage(overlayOriginialSize, 
																overlayOriginialSize.getWidth(), 
																overlayOriginialSize.getHeight(), -90);
				}
			}
			
			ImageIcon standardFolderIcon = new ImageIcon(SeeneToolkit.class.getResource("/images/folder.png")); // template Folder
			// transfer icon -> image -> buffered image
			Image standardFolderImage = standardFolderIcon.getImage();
			BufferedImage standardFolderBuffered = new BufferedImage(standardFolderImage.getWidth(null), standardFolderImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			// draw the folder image on to the buffered image
		    Graphics2D bGr = standardFolderBuffered.createGraphics();
		    bGr.drawImage(standardFolderImage, 0, 0, null);
		    bGr.dispose();
			
			File finishedFolderFile = new File(sFolder.getAbsolutePath() + File.separator + "folder.png");    // Combined File
			BufferedImage avatar = null;
			if (avatarURL != null) avatar = ImageIO.read(avatarURL);
    	
			if (overlayOriginialSize != null) {

			    // size calculations
			    int new_height=standardFolderBuffered.getHeight()/2 + 10;
			    
			    int factor = new_height * 100 / overlayOriginialSize.getHeight();
			    int new_width = overlayOriginialSize.getWidth() * factor / 80;
			    int offsetX = standardFolderBuffered.getWidth()/2 - new_width/2;
			    int offsetY = standardFolderBuffered.getHeight() - new_height - new_height/3 + 8;
			    
			    BufferedImage overlayResized = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_ARGB); 

			    Graphics2D tGr = overlayResized.createGraphics();
			    tGr.rotate(Math.toRadians(90), new_width/2, new_height/2);
			    tGr.drawImage(overlayOriginialSize, 0, 0, new_height, new_width, null);
			    tGr.dispose();
			    
			    BufferedImage avatarResized = null;
			    
			    if (avatarURL != null) {
			    	avatarResized = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
			    
			    	Graphics2D aGr = avatarResized.createGraphics();
			    	aGr.drawImage(avatar, 0, 0, 32, 32, null);
			    	aGr.dispose();
			    }
			    
				BufferedImage combined = new BufferedImage(standardFolderBuffered.getWidth(), standardFolderBuffered.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
				// paint all images, preserving the alpha channels
				Graphics g = combined.getGraphics();
				g.drawImage(standardFolderBuffered, 0, 0, null);
				g.drawImage(overlayResized, offsetX, offsetY, null);
				if (avatarURL != null) g.drawImage(avatarResized, 0, standardFolderBuffered.getHeight()-32, null);
			
				ImageIO.write(combined, "PNG", finishedFolderFile);
			} else {
				BufferedImage folderonly = new BufferedImage(standardFolderBuffered.getWidth(), standardFolderBuffered.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics g = folderonly.getGraphics();
				g.drawImage(standardFolderBuffered, 0, 0, null);
				
				ImageIO.write(folderonly,"PNG",finishedFolderFile);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
	
		
    // downloads a File to a target Directory
    public static boolean downloadFile(URL fU,ProxyData proxyData, File sFolder) {
    	File dF = new File(sFolder.getAbsolutePath() + File.separator + getDownloadFileName(fU));
    	
    	BufferedInputStream in = null;
    	
    	try {
    		// Proxy configured?
    		if ((proxyData!=null) && (proxyData.getHost().length()>0)) {
    			Proxy proxy = new Proxy(Proxy.Type.HTTP, 
    					new InetSocketAddress(proxyData.getHost(), proxyData.getPort()));
    			
    			// Proxy with Authentication?
    			if (proxyData.getUser().length()>0) {
    				Authenticator authenticator = new Authenticator() {
    		
    			        public PasswordAuthentication getPasswordAuthentication() {
    			            return (new PasswordAuthentication(proxyData.getUser(),
    			                    proxyData.getPass().toCharArray()));
    			        }
    			    };
    			    Authenticator.setDefault(authenticator);
    			}
    			
    		    // Connection with Proxy!
    			SeeneToolkit.log("Using Proxy: " + proxyData.getHost(), LogLevel.debug);
    			in = new BufferedInputStream(fU.openConnection(proxy).getInputStream());
    		} else {
    			// Connection without Proxy!
    			SeeneToolkit.log("Using NO Proxy!", LogLevel.debug);
    			in = new BufferedInputStream(fU.openConnection().getInputStream());
    		}
    	    
			FileOutputStream fos = new FileOutputStream(dF);
        	BufferedOutputStream bous = new BufferedOutputStream(fos);
        	byte data[] = new byte[1024];
        	int read;
        	while((read = in.read(data,0,1024))>=0) {
            	bous.write(data, 0, read);
        	}
        	bous.close();
        	in.close();
        	return true;
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    public static String getDownloadFileName(URL u) {
    	return u.getFile().substring(u.getFile().lastIndexOf('/')+1, u.getFile().length());
    }
    
    public static ImageIcon iconFromImageResource(String iconName,int iconSize) { 
    	return new ImageIcon((new ImageIcon(SeeneToolkit.class.getResource("/images/" + iconName)).getImage())
    				.getScaledInstance(iconSize, iconSize, java.awt.Image.SCALE_SMOOTH));
    }
    
    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                    	files[i].setWritable(true);
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }
    
    public static String getFileExtension(String fileName) {
    	String extension = "";

    	int i = fileName.lastIndexOf('.');
    	int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

    	if (i > p) {
    	    extension = fileName.substring(i+1);
    	}
    	
    	return extension;
    }
    
    public static BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        return null;
    }
    
    public static BufferedImage resizeImage(Image origImage, int new_width, int new_height) {
		BufferedImage transformedImage = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_ARGB); 

		Graphics2D tGr = transformedImage.createGraphics();
		tGr.drawImage(origImage, 0, 0, new_height, new_width, null);
		tGr.dispose();
		return transformedImage;
	}
    
    public static BufferedImage rotateAndResizeImage(Image origImage, int new_width, int new_height, int degree) {
		BufferedImage transformedImage = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_ARGB); 

		Graphics2D tGr = transformedImage.createGraphics();
		tGr.rotate(Math.toRadians(degree), new_width/2, new_height/2);
		tGr.drawImage(origImage, 0, 0, new_height, new_width, null);
		tGr.dispose();
		return transformedImage;
	}
    
    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
