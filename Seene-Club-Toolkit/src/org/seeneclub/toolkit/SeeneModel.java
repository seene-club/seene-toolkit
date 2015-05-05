package org.seeneclub.toolkit;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

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
	
	
	public void getModelDataFromFile() {
		mFloats = getModelDataFromFile(modelFile);
	}
	
	// Origin of this method is https://github.com/BenVanCitters/SeeneLib---Processing-Library
	private List<Float> getModelDataFromFile(File mFile) {
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(mFile)));
			
			//0 offset
			mSeeneVersion = Integer.reverseBytes(in.readInt());
		    System.out.println("version: " + mSeeneVersion); 
		    
		    //4 offset should be something like 720
		    mCameraWidth = Integer.reverseBytes(in.readInt());
		    System.out.println("cameraWidth: " + mCameraWidth);
		    
		    //8 should be something like 720
		    mCameraHeight = Integer.reverseBytes(in.readInt());
		    System.out.println("cameraHeight: " + mCameraHeight);
		    
		    //at byte 12 should be something like 1252.39842
		    mCameraFX = getFloatAtCurPos(in);
		    System.out.println("cameraFX: " + mCameraFX);
		    
		    //at byte 16 should be something like 1247.39842
		    mCameraFY = getFloatAtCurPos(in);
		    System.out.println("cameraFy: " + mCameraFY);
		    
		    //at byte 20 should be something like 0.023
		    mCameraK1 = getFloatAtCurPos(in);
		    System.out.println("cameraK1: " + mCameraK1);
		    
		    //at byte 20 should be something like .3207...
		    mCameraK2 = getFloatAtCurPos(in);
		    System.out.println("cameraK2: " + mCameraK2);
		    
		    //at byte 28 ~~90
		    mDepthWidth = Integer.reverseBytes(in.readInt());
		    System.out.println("depthmapwidth: " + mDepthWidth);
		    //at byte 32 ~~90
		    mDepthHeight = Integer.reverseBytes(in.readInt());
		    System.out.println("depthmapheight: " + mDepthHeight);
		    
		    int floatCount = mDepthWidth * mDepthHeight;
		    mFloats.clear();
		    float scale = 1;
		    for(int i = 0; i<floatCount;i++)
		    {
		    	mFloats.add(scale* getFloatAtCurPos(in));
		    	System.out.println("[" + i + "]: " + mFloats.get(i));
		    }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mFloats; 
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
	
}
