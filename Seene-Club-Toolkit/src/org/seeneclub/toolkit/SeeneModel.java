package org.seeneclub.toolkit;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.seeneclub.domainvalues.LogLevel;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.android.camera.util.XmpUtil;

public class SeeneModel {
	
	private int mSeeneVersion = -1;
	private int mCameraWidth = -1;
	private int mCameraHeight = -1;
	private float mCameraFX;
	private float mCameraFY; 
	private float mCameraK1;
	private float mCameraK2;
	private int mDepthWidth = -1;
	private int mDepthHeight = -1;
	private float mMinDepth;
	private float mMaxDepth;
	private List<Float> mFloats = new ArrayList<Float>();
	private float maxFloat = -1;
	private float minFloat = 1000;
	private File modelFile;
	private URL modelURL;
	
	// Constructors
	public SeeneModel() {
		setDepthWidth(STK.WORK_WIDTH);
		setDepthHeight(STK.WORK_HEIGHT);
		mFloats = new ArrayList<Float>(Collections.nCopies(getDepthWidth()*getDepthHeight(), STK.INIT_DEPTH));
	}
	SeeneModel(File sFile) {
	     this.modelFile = sFile;
	}
	SeeneModel(URL sURL) {
	     this.modelURL = sURL;
	}
	
	public void saveModelDataToFile(File sFile) {
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
			
			out.writeInt(Integer.reverseBytes(mSeeneVersion));
			out.writeInt(Integer.reverseBytes(mCameraWidth));
			out.writeInt(Integer.reverseBytes(mCameraHeight));
			putFloatAtCurPos(out, mCameraFX);
			putFloatAtCurPos(out, mCameraFY);
			putFloatAtCurPos(out, mCameraK1);
			putFloatAtCurPos(out, mCameraK2);
			out.writeInt(Integer.reverseBytes(mDepthWidth));
			out.writeInt(Integer.reverseBytes(mDepthHeight));
			setMinDepth(getMinFloat());
			setMaxDepth(getMaxFloat());
			putFloatAtCurPos(out, mMinDepth);
			putFloatAtCurPos(out, mMaxDepth);
			for (int i=0;i<mFloats.size();i++) {
				putFloatAtCurPos(out, mFloats.get(i));
			}
			
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveModelDataToPNG(File pngFile) {
		saveModelDataToPNGwithFar(pngFile, getMaxFloat());
	}
	
	// Save PNG with ability to override far. 
	public void saveModelDataToPNGwithFar(File pngFile, float far) {
		BufferedImage pngImage = new BufferedImage(mDepthWidth, mDepthHeight, BufferedImage.TYPE_INT_ARGB);
		int c = 0;
		float d;
		float dn;
		float near = getMinFloat();
		
		for (int x=0;x<mDepthHeight;x++) {
			for (int y=0;y<mDepthWidth;y++) {
				d = mFloats.get(c);
				
				//https://developers.google.com/depthmap-metadata/encoding
				dn = ((far * (d - near)) / (d * (far - near)));
				pngImage.setRGB(mDepthHeight - 1 - x, y, getIntFromColor(dn, dn, dn));
				
        		//pngImage.setRGB(mDepthHeight - 1 - x, y, getIntFromColor(d/far,d/far,d/far));
				c++;
			}
		}
		try {
			ImageIO.write(pngImage,"PNG",pngFile);
			SeeneToolkit.log("PNG: " + pngFile.getAbsolutePath() + " written!",LogLevel.info);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getIntFromColor(float Red, float Green, float Blue){
	    int R = Math.round(255 * Red);
	    int G = Math.round(255 * Green);
	    int B = Math.round(255 * Blue);

	    R = (R << 16) & 0x00FF0000;
	    G = (G << 8) & 0x0000FF00;
	    B = B & 0x000000FF;

	    return 0xFF000000 | R | G | B;
	}
	
	private List<Float> loadModelDataFromPNG(File pngFile, float min, float max) {
		try {
			return loadModelDataFromBufferedImage(ImageIO.read(pngFile), min, max);
		} catch (IOException e) {
			SeeneToolkit.log("Could not load depthmap from " + pngFile.getAbsolutePath(),LogLevel.error);
		}
		return null;
	}

	public List<Float> loadModelDataFromPNG(File pngFile, File xmpFile) {
		try {
			
			float min = STK.INIT_DEPTH;
			float max = 6.0f;
			
			// We'll get a better result if we have min- and maxDepth values. So perhaps there's a XMP File to read the values.
			if ((xmpFile!=null) && (xmpFile.exists())) {
				XMPMeta xmpMeta = XmpUtil.extractXMPMeta(xmpFile.getAbsolutePath());
				min = Float.parseFloat(xmpMeta.getProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:Near").toString());
				max = Float.parseFloat(xmpMeta.getProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:Far").toString());
			} 
			
			return loadModelDataFromBufferedImage(ImageIO.read(pngFile), min, max);
		} catch (IOException | XMPException e) {
			SeeneToolkit.log("Could not load depthmap from " + pngFile.getAbsolutePath(),LogLevel.error);
		}
		return null;
	}
	
	private List<Float> loadModelDataFromBufferedImage(BufferedImage depthmap, float min, float max) {
		mDepthWidth = depthmap.getHeight();
		mDepthHeight = depthmap.getWidth();
		maxFloat = -1;
		minFloat = 1000;
		float f;
		mFloats.clear();
		SeeneToolkit.log("PNG depthmap size is h:" + mDepthWidth + " -  w:" + mDepthHeight,LogLevel.info);
		if ((mDepthWidth > STK.WORK_WIDTH) || (mDepthHeight > STK.WORK_HEIGHT)) depthmap = resizeDepthmapPNG(depthmap);
		for (int x=0;x<mDepthHeight;x++) {
			for (int y=0;y<mDepthWidth;y++) {
				Color col = new Color(depthmap.getRGB(mDepthHeight - 1 - x, y));
				
				//f = (float) ( col.getRed() * (max - min) ) / 255 + min;
				f = (float) ( col.getRed() * (max) ) / 255;
				
				//https://developers.google.com/depthmap-metadata/encoding
				//f = (max * min) / (max - (col.getRed()/255.0f) * (max - min));  
			
				//System.out.println("R:" + col.getRed() + " - G:" + col.getGreen() + " - B:" + col.getBlue() + " - f:" + f);
				if (f > maxFloat) maxFloat = f;
		    	if (f < minFloat) minFloat = f;
				mFloats.add(f);
			}
		}
		SeeneToolkit.log("minimum float: " + minFloat,LogLevel.debug);
	    SeeneToolkit.log("maximum float: " + maxFloat,LogLevel.debug);
		return mFloats;
	}
	
	private BufferedImage resizeDepthmapPNG(BufferedImage depthmap) {
		depthmap = Helper.resizeImage(depthmap, STK.WORK_WIDTH, STK.WORK_HEIGHT);
		mDepthWidth = depthmap.getHeight();
		mDepthHeight = depthmap.getWidth();
		return depthmap;
	}
	
	public void loadModelDataFromFile() {
		mFloats = loadModelDataFromFile(modelFile);
	}
	
	// Origin of this method is https://github.com/BenVanCitters/SeeneLib---Processing-Library
	private List<Float> loadModelDataFromFile(File mFile) {
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(mFile)));
			
			//0 offset
			mSeeneVersion = Integer.reverseBytes(in.readInt());
		    SeeneToolkit.log("version: " + mSeeneVersion,LogLevel.debug); 
		    
		    //4 offset should be something like 720
		    mCameraWidth = Integer.reverseBytes(in.readInt());
		    SeeneToolkit.log("cameraWidth: " + mCameraWidth,LogLevel.debug);
		    
		    //8 should be something like 720
		    mCameraHeight = Integer.reverseBytes(in.readInt());
		    SeeneToolkit.log("cameraHeight: " + mCameraHeight,LogLevel.debug);
		    
		    //at byte 12 should be something like 1252.39842
		    mCameraFX = getFloatAtCurPos(in);
		    SeeneToolkit.log("cameraFX: " + mCameraFX,LogLevel.debug);
		    
		    //at byte 16 should be something like 1247.39842
		    mCameraFY = getFloatAtCurPos(in);
		    SeeneToolkit.log("cameraFy: " + mCameraFY,LogLevel.debug);
		    
		    //at byte 20 should be something like 0.023
		    mCameraK1 = getFloatAtCurPos(in);
		    SeeneToolkit.log("cameraK1: " + mCameraK1,LogLevel.debug);
		    
		    //at byte 20 should be something like .3207...
		    mCameraK2 = getFloatAtCurPos(in);
		    SeeneToolkit.log("cameraK2: " + mCameraK2,LogLevel.debug);
		    
		    //at byte 28 ~~90
		    mDepthWidth = Integer.reverseBytes(in.readInt());
		    SeeneToolkit.log("depthmapwidth: " + mDepthWidth,LogLevel.debug);
		    //at byte 32 ~~90
		    mDepthHeight = Integer.reverseBytes(in.readInt());
		    SeeneToolkit.log("depthmapheight: " + mDepthHeight,LogLevel.debug);

		    mMinDepth = getFloatAtCurPos(in);
		    SeeneToolkit.log("mindepth: " + mMinDepth,LogLevel.debug);
		    
		    mMaxDepth = getFloatAtCurPos(in);
		    SeeneToolkit.log("maxdepth: " + mMaxDepth,LogLevel.debug);
		    
		    int floatCount = mDepthWidth * mDepthHeight;
		    mFloats.clear();
		    maxFloat = -1;
			minFloat = 1000;
		    float scale = 1;
		    float f;
		    for(int i = 0; i<floatCount;i++)
		    {
		    	f = getFloatAtCurPos(in) * scale;
		    	if (f > maxFloat) maxFloat = f;
		    	if (f < minFloat) minFloat = f;
		    	mFloats.add(f);
		    	//System.out.println("[" + i + "]: " + mFloats.get(i));
		    }
			
		    in.close();
		    
		    // if depthmap is larger than WORK_WIDTH or WORK_HEIGHT it will be resized to WORK_WIDTH / WORK_HEIGHT
		    if ((mDepthWidth > STK.WORK_WIDTH) || (mDepthHeight > STK.WORK_HEIGHT)) {
		    	SeeneToolkit.log("original depthmap: " + mDepthWidth + " x " + mDepthHeight + " will be resized to " + STK.WORK_WIDTH + " x " + STK.WORK_HEIGHT, LogLevel.info);
		    	File pFile = new File(mFile.getAbsolutePath().substring(0,mFile.getAbsolutePath().lastIndexOf(File.separator)) + File.separator + "poster_depth.png");
		    	saveModelDataToPNG(pFile);
		    	loadModelDataFromPNG(pFile, minFloat, maxFloat);
		    }
		    	
		    SeeneToolkit.log("minimum float: " + minFloat,LogLevel.debug);
		    SeeneToolkit.log("maximum float: " + maxFloat,LogLevel.debug);
		    
		} catch (IOException e) {
			SeeneToolkit.log(e.getMessage(),LogLevel.error);
		}
		
		return mFloats; 
	}

	// Writing floats as BIG ENDIAN bytes.
	private static void putFloatAtCurPos(DataOutputStream out,float f) {
		try {
			ByteBuffer bBuff = ByteBuffer.allocate(4);
			bBuff.putFloat(f);
			bBuff.position(0);
			byte[] bytes = new byte[4];
			bBuff.get(bytes);
			for(int i = bytes.length-1; i >= 0; i--)
				out.writeByte(bytes[i]);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	

	// This method was taken from https://github.com/BenVanCitters/SeeneLib---Processing-Library
	// reverses the endianness of the data stream so we can 
	// properly pull a 32 bit float out...  Slow but the only way I 
	// could figure to do it in Java...
	private static float getFloatAtCurPos(DataInputStream in)
	{
		  byte[] bytes = new byte[4];
		  float result = 0;
		  try {
		    for(int i = bytes.length-1; i >= 0; i--)
		      bytes[i] = in.readByte();
		      
		      result = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN ).getFloat();
		  } catch(Exception e) {
			  System.out.println(e);
		  }
		  return result;
	}
	
	
	// Getters and Setters
	public File getModelFile() {
		return modelFile;
	}

	public void setModelFile(File modelFile) {
		this.modelFile = modelFile;
	}

	public URL getModelURL() {
		return modelURL;
	}

	public void setModelURL(URL modelURL) {
		this.modelURL = modelURL;
	}
	public int getSeeneVersion() {
		return mSeeneVersion;
	}
	public void setSeeneVersion(int mSeeneVersion) {
		this.mSeeneVersion = mSeeneVersion;
	}
	public int getCameraWidth() {
		return mCameraWidth;
	}
	public void setCameraWidth(int mCameraWidth) {
		this.mCameraWidth = mCameraWidth;
	}
	public int getCameraHeight() {
		return mCameraHeight;
	}
	public void setCameraHeight(int mCameraHeight) {
		this.mCameraHeight = mCameraHeight;
	}
	public float getCameraFX() {
		return mCameraFX;
	}
	public void setCameraFX(float mCameraFX) {
		this.mCameraFX = mCameraFX;
	}
	public float getCameraFY() {
		return mCameraFY;
	}
	public void setCameraFY(float mCameraFY) {
		this.mCameraFY = mCameraFY;
	}
	public float getCameraK1() {
		return mCameraK1;
	}
	public void setCameraK1(float mCameraK1) {
		this.mCameraK1 = mCameraK1;
	}
	public float getCameraK2() {
		return mCameraK2;
	}
	public void setCameraK2(float mCameraK2) {
		this.mCameraK2 = mCameraK2;
	}
	public int getDepthWidth() {
		return mDepthWidth;
	}
	public void setDepthWidth(int mDepthWidth) {
		this.mDepthWidth = mDepthWidth;
	}
	public int getDepthHeight() {
		return mDepthHeight;
	}
	public void setDepthHeight(int mDepthHeight) {
		this.mDepthHeight = mDepthHeight;
	}
	public List<Float> getFloats() {
		return mFloats;
	}
	public void setFloats(List<Float> mFloats) {
		this.mFloats = mFloats;
	}
	public float getMaxFloat() {
		return maxFloat;
	}
	public void setMaxFloat(float maxFloat) {
		this.maxFloat = maxFloat;
	}
	public float getMinFloat() {
		return minFloat;
	}
	public void setMinFloat(float minFloat) {
		this.minFloat = minFloat;
	}
	public float getMinDepth() {
		return mMinDepth;
	}
	public void setMinDepth(float mMinDepth) {
		this.mMinDepth = mMinDepth;
	}
	public float getMaxDepth() {
		return mMaxDepth;
	}
	public void setMaxDepth(float mMaxDepth) {
		this.mMaxDepth = mMaxDepth;
	}
}
