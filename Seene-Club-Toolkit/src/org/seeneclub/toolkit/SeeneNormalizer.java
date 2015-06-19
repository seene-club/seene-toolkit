package org.seeneclub.toolkit;

import org.seeneclub.domainvalues.LogLevel;


public class SeeneNormalizer {
	
	private float normMaxFloat = 8.0f;
	private float normMinFloat = 0.4f;
	
	public SeeneModel normalizeModelToFarthest(SeeneModel mO) {
		float coefficient = getNormMaxFloat() * 100 / mO.getMaxFloat();
		return normalizeModel(mO, coefficient);
	}
	
	public SeeneModel normalizeModelToClosest(SeeneModel mO) {
		float coefficient = getNormMinFloat() * 100 / mO.getMinFloat();
		return normalizeModel(mO, coefficient);
	}
	
	public SeeneModel normalizeModel(SeeneModel mO, float coefficient) {
		float nMax = -1.0f;
		float nMin = 1000f;
		
		SeeneToolkit.log("Model before MAX float: " + mO.getMaxFloat(),LogLevel.info);
		SeeneToolkit.log("Model before MIN float: " + mO.getMinFloat(),LogLevel.info);
		SeeneToolkit.log("Normalization coefficient : " + coefficient,LogLevel.info);
		
		for (int i=0;i<mO.getFloats().size();i++) {
			float nf = coefficient * mO.getFloats().get(i) / 100;
			if (nf > nMax) nMax = nf;
			if (nf < nMin) nMin = nf;
		    mO.getFloats().set(i, nf);	
		}
		mO.setMaxFloat(nMax);
		mO.setMinFloat(nMin);
		if (nMax > normMaxFloat) setNormMaxFloat(mO.getMaxFloat());
		if (nMin < normMinFloat) setNormMinFloat(mO.getMinFloat());
		
		SeeneToolkit.log("Model after MAX float: " + mO.getMaxFloat(),LogLevel.info);
		SeeneToolkit.log("Model after MIN float: " + mO.getMinFloat(),LogLevel.info);
		
		return mO;
	}
	
	public float getNormMaxFloat() {
		return normMaxFloat;
	}
	public void setNormMaxFloat(float normMaxFloat) {
		this.normMaxFloat = normMaxFloat;
	}
	public float getNormMinFloat() {
		return normMinFloat;
	}
	public void setNormMinFloat(float normMinFloat) {
		this.normMinFloat = normMinFloat;
	}
	

}
