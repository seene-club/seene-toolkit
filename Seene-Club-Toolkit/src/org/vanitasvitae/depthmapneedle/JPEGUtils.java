package org.vanitasvitae.depthmapneedle;


import java.util.Arrays;
import java.util.Vector;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
public class JPEGUtils extends Const
{
	/**
	 * Read the length of the block from the two bytes after the APP1 Marker
	 *
	 * @param boundaryPos position of the APP1 Marker in the byte array
	 * @param data        byte array containing the block WITH APP1 Marker
	 * @return length of the block
	 */
	public static int readBlockLength(int boundaryPos, byte[] data)
	{
		//Check whether entered position is APP1 Marker
		if (!JPEGUtils.isAPP1Marker(data, boundaryPos))
		{
			System.err.println("JPEGUtils blockLength: Block is no APP1 Block!");
			return -1;
		}
		int o;
		//Read length
		try
		{
			o = 256 * (data[boundaryPos+2] & 0xFF)+(data[boundaryPos+3] & 0xFF);
		} catch (ArrayIndexOutOfBoundsException e)
		{
			System.err.println("JPEGUtils blockLength threw ArrayIndexOutOfBoundsException. Maybe the block is cut after APP1 Marker?");
			e.printStackTrace();
			return -1;
		}
		return o;
	}

	/**
	 * decorate information with a block header of a certain type. The type is specified via the argument type.
	 * In case, type == EXTENDEDXMP and data.length > CHUNKSIZE multiple ExtendedXMPBlocks will be generated and concatenated.
	 * Available types:
	 * Const.EXIF, Const.STANDARDXMP, Const.EXTENDEDXMP
	 *
	 * @param data byte array of data that will be decorated
	 * @param type String declaring the type of header for the block.
	 * @return decorated block
	 */
	public static byte[] decorateBlock(byte[] data, String type)
	{
		//EXIF Block: 'APP1 + BLOCKLENGTH + EXIF\0\0 + data'
		if (type.equals(EXIF))
		{
			data = ArrayUtils.concatenate(markEXIF, data);
			byte[] pre = ArrayUtils.concatenate(markAPP1, genLen(data.length+2));
			return ArrayUtils.concatenate(pre, data);
		}
		//StandardXMP: 'APP1 + BLOCKLENGTH + http://ns.adobe.com/xap/1.0/\0 + data'
		else if (type.equals(STANDARDXMP))
		{
			data = ArrayUtils.concatenate(markStandardXMP, data);
			byte[] pre = ArrayUtils.concatenate(markAPP1, genLen(data.length+2));
			return ArrayUtils.concatenate(pre, data);
		}
		//ExtendedXMP: 'APP1 + BLOCKLENGTH + http://ns.adobe.com/xmp/extension/\0 + MD5 + EXTENDEDLENGTH + EXTENDEDOFFSET + DATAPORTION
		else if (type.equals(EXTENDEDXMP))
		{
			byte[] out = new byte[0];
			//MD5 checksum is digest of the datacontent
			byte[] md5 = HexUtil.generateMD5(data);
			int i = 0;
			int blockCount = data.length / CHUNKSIZE;
			//decorate blockportions of size CHUNKSIZE
			while (i < blockCount)
			{
				byte[] part = Arrays.copyOfRange(data, i * CHUNKSIZE, (i+1) * CHUNKSIZE);
				byte[] pre = markAPP1;
				pre = ArrayUtils.concatenate(pre, genLen(2+markExtendedXMP.length+32+4+4+part.length));
				pre = ArrayUtils.concatenate(pre, markExtendedXMP);
				pre = ArrayUtils.concatenate(pre, md5);
				pre = ArrayUtils.concatenate(pre, ArrayUtils.intToByteArray(data.length));
				pre = ArrayUtils.concatenate(pre, ArrayUtils.intToByteArray(i * CHUNKSIZE));
				part = ArrayUtils.concatenate(pre, part);
				out = ArrayUtils.concatenate(out, part);
				i++;
			}
			//Decorate the restportion < CHUNKSIZE
			byte[] part = Arrays.copyOfRange(data, i * CHUNKSIZE, data.length);
			byte[] pre = markAPP1;
			pre = ArrayUtils.concatenate(pre, genLen(2+markExtendedXMP.length+32+4+4+part.length));
			pre = ArrayUtils.concatenate(pre, markExtendedXMP);
			pre = ArrayUtils.concatenate(pre, md5);
			pre = ArrayUtils.concatenate(pre, ArrayUtils.intToByteArray(data.length));
			pre = ArrayUtils.concatenate(pre, ArrayUtils.intToByteArray(i * CHUNKSIZE));
			part = ArrayUtils.concatenate(pre, part);
			out = ArrayUtils.concatenate(out, part);
			return out;
		}
		System.err.println("JPEGUtils decorateBlock no valid type entered.");
		return null;
	}

	/**
	 * Extract the value of a key from the data Array.
	 * For Example: data = 'blabalkey="Hallo"blabla', key = 'key'
	 * Output of extract(data, key) will be 'Hallo'
	 *
	 * @param data array of bytes
	 * @param key  array that contains the key
	 * @return the "value" of the key (the part after the key and the following '="' to the next '"'.
	 */
	public static byte[] extract(byte[] data, byte[] key)
	{
		int start = -1;
		int end = -1;
		for (int i = 0; i <= data.length-key.length; i++)
		{
			if (ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, key, i) && data[i+key.length] == 0x3D && data[i+key.length+1] == 0x22)
			{
				start = i+key.length+2;
				for (int j = i+key.length+2; j < data.length; j++)
				{
					if (data[j] == 0x22)
					{
						end = j;
						return Arrays.copyOfRange(data, start, end);
					}

				}
				System.err.println("JPEGUtils extract found end for \""+new String(key)+"\": false");
			}
		}
		System.err.println("JPEGUtils extract found start for \""+new String(key)+"\": false");
		return null;
	}

	/**
	 * Extract the depth information from a byte array (image as array)
	 *
	 * @param data array of a jpeg image
	 * @return the value of Const.keyGDepthData (GDepth:Data)
	 */
	public static byte[] extractDepthMap(byte[] data)
	{
		byte[] meta = getXMPBlocksContent(data);
		byte[] depth = extract(meta, keyGDepthData);
		if (depth == null) System.err.println("JPEGUtils extractDepthMap is null");
		return depth;
	}

	/**
	 * Extract the unblurred source image from a byte array (image as array)
	 *
	 * @param data array of a jpeg image
	 * @return the value of Const.keyGImageData (GImage:Data)
	 */
	public static byte[] extractSourceImage(byte[] data)
	{
		byte[] meta = getXMPBlocksContent(data);
		byte[] src = extract(meta, keyGImageData);
		if (src == null) System.err.println("JPEGUtils extractSourceImage is null");
		return src;
	}

	/**
	 * convert an integer into a two byte representation in base 256
	 *
	 * @param l integer
	 * @return byte array of length two containing two bytes representing l in base 256
	 */
	public static byte[] genLen(int l)
	{
		byte[] o = new byte[2];
		o[0] = (byte) (l / 256);
		o[1] = (byte) (l % 256);
		return o;
	}

	/**
	 * Get a block of data from an array containing blocks
	 *
	 * @param data     array of bytes
	 * @param boundary position of the APP1 marker of the targeted block
	 * @return the full targeted block
	 */
	public static byte[] getBlock(byte[] data, int boundary)
	{
		if (!JPEGUtils.isAPP1Marker(data, boundary))
		{
			System.err.println("JPEGUtils getBlock: Block is no APP1-block");
			return data;
		} else
		{
			return Arrays.copyOfRange(data, boundary, boundary+JPEGUtils.readBlockLength(boundary, data)+2);
		}
	}

	/**
	 * Same as getBlock, but returns the targeted block without the APP1 Marker and the two bytes containing the length of the block.
	 *
	 * @param data     byte array
	 * @param boundary position of the APP1 marker of the targeted block
	 * @return the targeted block without the APP1 marker and the two bytes containing the length
	 */
	public static byte[] getBlockWithoutAPP1(byte[] data, int boundary)
	{
		if (!JPEGUtils.isAPP1Marker(data, boundary))
		{
			System.err.println("JPEGUtils getBlockWithoutAPP1: Block is no APP1-block");
			return null;
		} else
			return Arrays.copyOfRange(data, boundary+4, boundary+JPEGUtils.readBlockLength(boundary, data)+2);

	}

	/**
	 * Same as getBlock but returns the block without any header (APP1, length, EXIF, StandardXMP, ExtendedXMP)
	 *
	 * @param data
	 * @param boundary
	 * @return
	 */
	public static byte[] getBlockWithoutHeader(byte[] data, int boundary)
	{
		if (!JPEGUtils.isAPP1Marker(data, boundary))
		{
			System.err.println("JPEGUtils getBlockWithoutHeader: Block is no APP1-block");
			return null;
		} else
		{
			int offset;
			if (ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, markStandardXMP, 4+boundary))
				offset = 2+2+markStandardXMP.length;
			else if (ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, markExtendedXMP, 4+boundary))
				offset = 2+2+markExtendedXMP.length+32+4+4;
			else if (ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, markEXIF, 4+boundary))
				offset = 2+2+markEXIF.length;
			else offset = 4;
			return Arrays.copyOfRange(data, boundary+offset, boundary+JPEGUtils.readBlockLength(boundary, data)+2);
		}
	}

	/**
	 * Return all positions of "FF E1" in the byte[]
	 * These are the bytes marking h´the beginning of a block
	 *
	 * @param data byte array
	 * @return array of positions of APP1 marker
	 */
	public static int[] getBoundaries(byte[] data)
	{
		Vector<Integer> b = new Vector<Integer>();
		for (int i = 0; i < data.length; i++)
		{
			if (JPEGUtils.isAPP1Marker(data, i))
			{
				b.add(i);
				i += 3;    //Skip E1 and length
			}
		}
		int[] out = new int[b.size()];
		for (int i = 0; i < b.size(); i++)
			out[i] = b.get(i);
		return out;

	}

	/**
	 * Return the exif block of the jpg. This is usually the first block of data, but this tolerates any position
	 *
	 * @param data array of bytes
	 * @return exif block
	 */
	public static byte[] getEXIFBlock(byte[] data)
	{
		int[] bounds = getBoundaries(data);
		for (int e : bounds)
		{
			byte[] block = getBlock(data, e);
			if (isEXIFBlock(block)) return getBlockWithoutHeader(data, e);
		}
		System.err.println("JPEGUtils getEXIFBlock: No EXIF-block found");
		return null;
	}

	/**
	 * Gets the concatenated content of all ExtendedXMPBlocks in the data array without headers
	 *
	 * @param data array
	 * @return content of the blocks
	 */
	public static byte[] getExtendedXMPBlockContent(byte[] data)
	{
		int[] bounds = JPEGUtils.getBoundaries(data);
		byte[] cont = new byte[0];
		for (int e : bounds)
		{
			byte[] block = JPEGUtils.getBlock(data, e);
			if (JPEGUtils.isExtendedXMP(block))
			{
				byte[] part = JPEGUtils.getBlockWithoutHeader(block, 0);
				if (part == null)
					System.err.println("JPEGUtils getExtendedXMPBlockContent part is null");
				cont = ArrayUtils.concatenate(cont, part);
			}
		}
		return cont;
	}

	/**
	 * Returns the tail of the data array that is not content of any block. In case of the google camera this contains the JPEG image data
	 *
	 * @param data byte array
	 * @return the trailing headless imagedata
	 */
	public static byte[] getImageTail(byte[] data)
	{
		byte[] out;
		int[] bounds = getBoundaries(data);
		if (bounds.length != 0)
		{
			int offset = 256 * (data[bounds[bounds.length-1]+2] & 0xFF)+(data[bounds[bounds.length-1]+3] & 0xFF);
			offset += bounds[bounds.length-1];
			offset += 2;
			out = Arrays.copyOfRange(data, offset, data.length);
			return out;
		} else return null;
	}

	/**
	 * Returns the concatenated contents of all APP1 Blocks without the APP1 Markers
	 *
	 * @param data byte array
	 * @return the concatenated contents of APP1 Blocks
	 */
	public static byte[] getMetadata(byte[] data)
	{
		int[] boundaries = getBoundaries(data);
		byte[] out = new byte[0];
		for (int e : boundaries)
		{
			out = ArrayUtils.concatenate(out, getBlockWithoutAPP1(data, e));
		}
		return out;
	}

	/**
	 * Returns the content of the StandardXMPBlock without header.
	 *
	 * @param data byte array
	 * @return the content of the StandardXMPBlock
	 */
	public static byte[] getStandardXMPBlockContent(byte[] data)
	{
		int[] bounds = JPEGUtils.getBoundaries(data);
		for (int e : bounds)
		{
			byte[] block = JPEGUtils.getBlock(data, e);
			if (JPEGUtils.isStandardXMP(block)) return JPEGUtils.getBlockWithoutHeader(data, e);
		}
		System.err.println("JPEGUtils getStandardXMPBlockContent is null");
		return null;
	}

	/**
	 * Returns the content of all XMPBlocks (StandardXMP + ExtendedXMP) without headers and concatenated.
	 *
	 * @param data byte array
	 * @return byte array with contents
	 */
	public static byte[] getXMPBlocksContent(byte[] data)
	{
		byte[] stand = getStandardXMPBlockContent(data);
		byte[] ext = getExtendedXMPBlockContent(data);

		byte[] out = ArrayUtils.concatenate(stand, ext);
		if (out == null) System.err.println("JPEGUtils getXMPBlocksContent is null");
		return out;
	}

	/**
	 * Returns true, if there is a APP1 marker at the given offset in data.
	 *
	 * @param data   byte array
	 * @param offset offset
	 * @return true, if there is an APP1 marker at offset in data, else false
	 */
	public static boolean isAPP1Marker(byte[] data, int offset)
	{
		return ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, markAPP1, offset);
	}

	/**
	 * Returns true, if there is an Exif marker after the APP1 marker in this block.
	 *
	 * @param block byte array
	 * @return true, if there is an Exif marker at offset 4, else false.
	 */
	public static boolean isEXIFBlock(byte[] block)
	{
		return JPEGUtils.isEXIFMarker(block, 4);
	}

	/**
	 * Returns true, if there is an Exif marker at offset in data, else false
	 *
	 * @param data
	 * @param offset
	 * @return true, if there is an Exif marker at offset in data, else false
	 */
	public static boolean isEXIFMarker(byte[] data, int offset)
	{
		return ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, markEXIF, offset);
	}

	/**
	 * Returns true, if block is an extendedXMPBlock.
	 *
	 * @param block the block with FFE1 and the two following bytes
	 * @return true, if there is an extendedXMPmarker at offset 4 in block, else false
	 */
	public static boolean isExtendedXMP(byte[] block)
	{
		return ArrayUtils.arrayIsPartOfOtherArrayOnOffset(block, markExtendedXMP, 4);
	}

	/**
	 * Returns true, if data has the magical bytes of a jpeg file at the start
	 *
	 * @param data byte array
	 * @return true, if there is a jpegmarker at offset 0 (FFD8)
	 */
	public static boolean isJPG(byte[] data)
	{
		return JPEGUtils.isJPG(data, 0);
	}

	/**
	 * Returns true, if there is a jpegMarker at offset in data
	 *
	 * @param data   byte array
	 * @param offset offset
	 * @return true, if there is a jpegMarker at offset in data, ellse false
	 */
	public static boolean isJPG(byte[] data, int offset)
	{
		return ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, markJPG, offset);
	}

	/**
	 * Returns true, if data is a standardXMPBlock
	 *
	 * @param block block with FFE1 and the two following bytes
	 * @return true, if the block is standardXMP (has standardXMP marker at offset 4), else false
	 */
	public static boolean isStandardXMP(byte[] block)
	{
		return ArrayUtils.arrayIsPartOfOtherArrayOnOffset(block, markStandardXMP, 4);
	}

	/**
	 * Returns true, if data has a standardXMPMarker at offset offset
	 *
	 * @param block byte array
	 * @return true, if the block has standardXMPMarker at offset, else false
	 */
	public static boolean isStandardXMP(byte[] block, int offset)
	{
		return ArrayUtils.arrayIsPartOfOtherArrayOnOffset(block, markStandardXMP, offset);
	}

	/**
	 * Replace the value of a key in data with another value value
	 *
	 * @param data  byte array
	 * @param key   key of the value that will be modified
	 * @param value new value of key
	 * @return modified data byte array
	 */
	public static byte[] replace(byte[] data, byte[] key, byte[] value)
	{
		int start = -1;
		int end = -1;
		for (int i = 0; i < data.length; i++)
		{
			if (ArrayUtils.arrayIsPartOfOtherArrayOnOffset(data, key, i))
			{
				start = i+key.length+2;
				break;
			}
		}
		if (start != -1)
		{
			byte[] pre = Arrays.copyOfRange(data, 0, start);
			for (int j = start; j < data.length; j++)
			{
				if (data[j] == 0x22)
				{
					end = j;
					break;
				}
			}
			if (end != -1)
			{
				byte[] post = Arrays.copyOfRange(data, end, data.length);
				byte[] out = ArrayUtils.concatenate(pre, value);
				out = ArrayUtils.concatenate(out, post);
				return out;
			} else System.err.println("JPEGUtils replace: No closing \" found in data");
		} else System.err.println("JPEGUtils replace: key not found in data");
		return null;
	}
}
