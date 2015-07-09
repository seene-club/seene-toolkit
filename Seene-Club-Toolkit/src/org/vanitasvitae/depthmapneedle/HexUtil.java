package org.vanitasvitae.depthmapneedle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
public class HexUtil
{
	/**
	 * Print out a byte array in hexadecimal
	 *
	 * @param bytes array
	 */
	public static void printHex(byte[] bytes)
	{
		for (byte b : bytes) System.out.print(byteToHexString(b));
	}

	/**
	 * convert a byte to a hex string
	 *
	 * @param data array
	 * @return String representation in hex
	 */
	public static String byteToHexString(byte data)
	{

		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));

		return buf.toString();
	}

	/**
	 * convert a integer into a hexadecimal character
	 *
	 * @param i integer
	 * @return hex char
	 */
	public static char toHexChar(int i)
	{
		if ((0 <= i) && (i <= 9))
		{
			return (char) ('0'+i);
		} else
		{
			return (char) ('a'+(i-10));
		}
	}

	/**
	 * Generate the md5 digest of the byte array data in hexadecimal
	 *
	 * @param data array
	 * @return byte array of the md5 digest in hex
	 */
	public static byte[] generateMD5(byte[] data)
	{
		try
		{
			byte[] md5 = MessageDigest.getInstance("MD5").digest(data);
			String m = "";
			for (int i = 0; i < md5.length; i++)
			{
				m = m+byteToHexString(md5[i]).toUpperCase();
			}
			return m.getBytes();
		} catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
