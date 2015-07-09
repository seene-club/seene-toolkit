package org.vanitasvitae.depthmapneedle;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
public class Const
{
	/**
	 * Size of a chunk of data (used to split the ExtendedXMP information into blocks
	 */
	public static final int CHUNKSIZE = 65400;

	/**
	 * Strings for JPEGUtils.decorate()
	 */

	public static final String EXIF = "EXIF";
	public static final String STANDARDXMP = "StandardXMP";
	public static final String EXTENDEDXMP = "ExtendedXMP";

	/**
	 * Magical bytes that identify the file as JPG (FF D8) (length: 2)
	 */
	public static byte[] markJPG = {(byte) 0xFF, (byte) 0xD8};

	/**
	 * Magical bytes that identify the file as PNG. (0x89 P N G) (length: 4)
	 */
	public static byte[] markPNG = {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47};

	/**
	 * Marker that defines the beginning of a new block of metadata (FF E1) (length: 2)
	 */
	public static byte[] markAPP1 = {(byte) 0xFF, (byte) 0xE1};

	/**
	 * Header of the EXIF block (EXIF\0\0) (length: 6)
	 */
	public static byte[] markEXIF = {(byte) 0x45, (byte) 0x78, (byte) 0x69, (byte) 0x66, (byte) 0x00, (byte) 0x00};

	/**
	 * Header of the StandardXMP block (http://ns.adobe.com/xap/1.0/\0) (length: 29)
	 */
	public static byte[] markStandardXMP = {
			(byte) 0x68, (byte) 0x74, (byte) 0x74, (byte) 0x70,
			(byte) 0x3A, (byte) 0x2F, (byte) 0x2F, (byte) 0x6E, (byte) 0x73,
			(byte) 0x2E, (byte) 0x61, (byte) 0x64, (byte) 0x6F, (byte) 0x62,
			(byte) 0x65, (byte) 0x2E, (byte) 0x63, (byte) 0x6F, (byte) 0x6D,
			(byte) 0x2F, (byte) 0x78, (byte) 0x61, (byte) 0x70, (byte) 0x2F,
			(byte) 0x31, (byte) 0x2E, (byte) 0x30, (byte) 0x2F, (byte) 0x00};

	/**
	 * Header of the ExtendedXMP block (http://ns.adobe.com/xmp/extension/\0) (length: 35)
	 */
	public static byte[] markExtendedXMP = {
			(byte) 0x68, (byte) 0x74, (byte) 0x74, (byte) 0x70,
			(byte) 0x3A, (byte) 0x2F, (byte) 0x2F, (byte) 0x6E, (byte) 0x73,
			(byte) 0x2E, (byte) 0x61, (byte) 0x64, (byte) 0x6F, (byte) 0x62,
			(byte) 0x65, (byte) 0x2E, (byte) 0x63, (byte) 0x6F, (byte) 0x6D,
			(byte) 0x2F, (byte) 0x78, (byte) 0x6D, (byte) 0x70, (byte) 0x2F,
			(byte) 0x65, (byte) 0x78, (byte) 0x74, (byte) 0x65, (byte) 0x6e,
			(byte) 0x73, (byte) 0x69, (byte) 0x6f, (byte) 0x6e, (byte) 0x2f,
			(byte) 0x00};

	/** Keys (following scheme: key="value") */

	/**
	 * Key for the MD5 digest of the full ExtendedXMP block content (without headers etc.)
	 * (xmpNote:HasExtendedXMP) (length: 22)
	 */
	public static byte[] keyHasExtendedXMP = {
			(byte) 0x78, (byte) 0x6d, (byte) 0x70, (byte) 0x4e, (byte) 0x6f,
			(byte) 0x74, (byte) 0x65, (byte) 0x3a, (byte) 0x48, (byte) 0x61,
			(byte) 0x73, (byte) 0x45, (byte) 0x78, (byte) 0x74, (byte) 0x65,
			(byte) 0x6e, (byte) 0x64, (byte) 0x65, (byte) 0x64, (byte) 0x58,
			(byte) 0x4d, (byte) 0x50
	};

	/**
	 * Key for the Base64 encoded png image containing the depth information
	 * (GDepth:Data) (length: 11)
	 */
	public static byte[] keyGDepthData = {
			(byte) 0x47, (byte) 0x44, (byte) 0x65, (byte) 0x70, (byte) 0x74,
			(byte) 0x68, (byte) 0x3a, (byte) 0x44, (byte) 0x61, (byte) 0x74,
			(byte) 0x61
	};

	/**
	 * Key for the Base64 encoded unblurred jpg source image (Google camera will combine this with the depth information to render the blurred image)
	 * (GImage:Data) (length: 11)
	 */
	public static byte[] keyGImageData = {
			(byte) 0x47, (byte) 0x49, (byte) 0x6d, (byte) 0x61, (byte) 0x67,
			(byte) 0x65, (byte) 0x3a, (byte) 0x44, (byte) 0x61, (byte) 0x74,
			(byte) 0x61
	};
}

