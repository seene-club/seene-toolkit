package org.seeneclub.toolkit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.seeneclub.domainvalues.LogLevel;

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
	private List<Float> mFloats = new ArrayList<Float>();
	private float maxFloat = -1;
	private File modelFile;
	private URL modelURL;
	
	// Constructors
	public SeeneModel() {
		// TODO Auto-generated constructor stub
	}
	SeeneModel(File sFile) {
	     this.modelFile = sFile;
	}
	SeeneModel(URL sURL) {
	     this.modelURL = sURL;
	}
	
	private void saveModelDateToFile(File sFile) {
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
			for (int i=0;i<mFloats.size();i++) {
				putFloatAtCurPos(out, mFloats.get(i));
			}
			
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		    
		    int floatCount = mDepthWidth * mDepthHeight;
		    mFloats.clear();
		    float scale = 1;
		    float f;
		    for(int i = 0; i<floatCount;i++)
		    {
		    	f = getFloatAtCurPos(in) * scale;
		    	if (f > maxFloat) maxFloat = f;
		    	mFloats.add(f);
		    	//System.out.println("[" + i + "]: " + mFloats.get(i));
		    }
			
		    in.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
