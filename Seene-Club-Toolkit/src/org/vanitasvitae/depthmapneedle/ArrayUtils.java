package org.vanitasvitae.depthmapneedle;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
public class ArrayUtils
{
	/**
	 * Return true, if the array part is sub array of src from offset
	 *
	 * @param src    array
	 * @param part   array
	 * @param offset int
	 * @return true, if part is completely a subarray of src on offset offset, else false
	 */
	public static boolean arrayIsPartOfOtherArrayOnOffset(byte[] src, byte[] part, int offset)
	{
		if (offset < 0 || part.length+offset > src.length) return false;
		for (int i = 0; i < part.length; i++)
		{
			if (part[i] != src[i+offset]) return false;
		}
		return true;
	}

	/**
	 * converts the byte array b into a char array by casting all the bytes into chars
	 *
	 * @param b byte array
	 * @return char array
	 */
	public static char[] bytesToChars(byte[] b)
	{
		char[] c = new char[b.length];
		for (int i = 0; i < b.length; i++)
			c[i] = (char) b[i];
		return c;
	}

	/**
	 * converts the char array into a byte array by casting all the chars into bytes
	 *
	 * @param c char array
	 * @return byte array
	 */
	public static byte[] charsToBytes(char[] c)
	{
		byte[] b = new byte[c.length];
		for (int i = 0; i < c.length; i++)
			b[i] = (byte) c[i];
		return b;
	}

	/**
	 * concatenate two byte arrays
	 *
	 * @param first  first byte array
	 * @param second second byte array
	 * @return first + second
	 */
	public static byte[] concatenate(byte[] first, byte[] second)
	{
		if (first == null) return second;
		if (second == null) return first;
		int aLen = first.length;
		int bLen = second.length;
		byte[] c = new byte[aLen+bLen];
		System.arraycopy(first, 0, c, 0, aLen);
		System.arraycopy(second, 0, c, aLen, bLen);
		return c;
	}

	/**
	 * convert an integer into a 32 bit byte array
	 *
	 * @param value integer
	 * @return byte array
	 */
	public static final byte[] intToByteArray(int value)
	{
		return new byte[]{
				(byte) (value >>> 24),
				(byte) (value >>> 16),
				(byte) (value >>> 8),
				(byte) value};
	}

	/**
	 * convert a signed byte array into an unsigned byte array (sort of)
	 *
	 * @param b byte array of signed bytes
	 * @return byte array of unsigned bytes
	 */
	public static byte[] unsign(byte[] b)
	{
		if (b==null) return null;
		byte[] u = new byte[b.length];
		for (int i = 0; i < b.length; i++)
			u[i] = (byte) (b[i] & 0xFF);
		return u;
	}
}

