package org.seeneclub.toolkit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class SeeneAWSsignature {
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	public static String calculateRFC2104HMAC(String data, String key) {
		String result = null;
		try {
	
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
	
			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
	
			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
	
			// base64-encode the hmac
			result = DatatypeConverter.printBase64Binary(rawHmac);
	
		} catch (Exception e) {
			System.out.println("Failed to generate HMAC : " + e.getMessage());
		}
		
		return result;
	}
}


