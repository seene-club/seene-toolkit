package org.vanitasvitae.depthmapneedle;


import java.io.File;
import java.util.Base64;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
public class JPEG extends Const
{

	/** MEMBERS */

	/**
	 * location of the source file
	 */
	private String m_Filename;

	/**
	 * byte array containing the actual file
	 */
	private byte[] m_RawData;

	/**
	 * byte array containing EXIF block information without header
	 */
	private byte[] m_Exif;

	/**
	 * byte array containing StandardXMP block information without header
	 */
	private byte[] m_StandardXMP;

	/**
	 * byte array containing ExtendedXMP block information without header
	 */
	private byte[] m_ExtendedXMP;

	/**
	 * byte array containing the trailing headless data block containing the blurred jpeg image
	 */
	private byte[] m_ImageTail;

    private Base64Wrapper base64;

	/** METHODS */

	/**
	 * Constructor
	 *
	 * @param rawData raw byte array
	 */
	public JPEG(byte[] rawData, Base64Wrapper wrapper)
	{
		this.base64 = wrapper;
        this.init(rawData, null);
	}

	/**
	 * Constructor, that reads a byte array from file
	 *
	 * @param filename raw byte array
	 */
	public JPEG(String filename, Base64Wrapper wrapper)
	{
        this.base64 = wrapper;
		this.init(IO.read(new File(filename)), filename);
	}
	
	public JPEG(String filename) {
        this.base64 = new Base64Wrapper(){
			@Override
			public byte[] decode(byte[] data)
			{
				return Base64.getDecoder().decode(data);
			}

			@Override
			public byte[] encode(byte[] data)
			{
				return Base64.getEncoder().encode(data);
			}
		};
		this.init(IO.read(new File(filename)), filename);
	}
	
	

	/**
	 * Disassemble the rawData byte array into sub arrays
	 */
	public void disassemble()
	{
		m_Exif = this.getEXIFBlock();
		m_StandardXMP = this.getStandardXMPBlockContent();
		m_ExtendedXMP = this.getExtendedXMPContent();
		m_ImageTail = this.getImageTail();
	}

	/**
	 * extract the depth map from the jpeg and store it in the same location as the jpeg itself but with the suffix _depth.png
	 *
	 * @return
	 */
	public boolean exportDepthMap()
	{
		String out = m_Filename;
		if (out.endsWith("_xmp.jpg") || out.endsWith("_xmp.JPG")) out = out.substring(0, out.length()-8);
		return this.exportDepthMap(out+"_depth.png");
	}

	/**
	 * extract the depthmap from the jpeg and store it under the location specified in "file"
	 *
	 * @param file String of the output location
	 * @return success
	 */
	public boolean exportDepthMap(String file)
	{
		byte[] b64 = ArrayUtils.unsign(extractDepthMap());
		byte[] depth = base64.decode(b64);
		if (depth != null)
		{
			IO.write(depth, file);
			return true;
		} else return false;
	}
	
	public byte[] base64_encode(byte[] depthmap) {
		return base64.encode(depthmap);
	}
	
	public byte[] base64_decode(byte[] depthmap) {
		return base64.decode(depthmap);
	}

	/**
	 * Extract and save the unblurred source image. The image will be saved in the same location as the jpeg itself, but with the suffix "_unblurred.jpg"
	 *
	 * @return success
	 */
	public boolean exportSourceImage()
	{
		String out = m_Filename;
		if (out.endsWith("_xmp.jpg") || out.endsWith("_xmp.JPG")) out = out.substring(0, out.length()-8);
		return this.exportSourceImage(out+"_unblurred.jpg");
	}

	/**
	 * Extract the unblurred source image and save it under the location specified in "file"
	 *
	 * @param file String of the location
	 * @return success
	 */
	public boolean exportSourceImage(String file)
	{
		byte[] b64 = ArrayUtils.unsign(extractSourceImage());
		if (b64==null) return false;
		byte[] src = base64.decode(b64);
		if (src != null)
		{
			IO.write(src, file);
			return true;
		} else return false;
	}

	/**
	 * Extract and return the depthmap information
	 *
	 * @return depthmap as byte array
	 */
	public byte[] extractDepthMap()
	{
		return JPEGUtils.extractDepthMap(m_RawData);
	}

	/**
	 * Extract and return the unblurred source image
	 *
	 * @return source image as byte array
	 */
	public byte[] extractSourceImage()
	{
		return JPEGUtils.extractSourceImage(m_RawData);
	}

	/**
	 * Find all indizes of APP1 markers in the jpegs byte array
	 *
	 * @return integer array of all positions of the APP1 marker in the rawData array
	 */
	public int[] getBoundaries()
	{
		return JPEGUtils.getBoundaries(m_RawData);
	}

	/**
	 * return the exif block of the jpeg as a byte array
	 *
	 * @return exifblock (member)
	 */
	public byte[] getExif()
	{
		return m_Exif;
	}

	/**
	 * Gets the exif block from the rawData array instead from the member
	 *
	 * @return the exif block from rawData
	 */
	public byte[] getEXIFBlock()
	{
		return JPEGUtils.getEXIFBlock(m_RawData);
	}

	/**
	 * Extract and return the content of all the ExtendedXMP blocks concatenated.
	 *
	 * @return content of the ExtendedXMP blocks
	 */
	public byte[] getExtendedXMPContent()
	{
		return JPEGUtils.getExtendedXMPBlockContent(m_RawData);
	}

	/**
	 * return the filename and path
	 *
	 * @return member filename
	 */
	public String getFilename()
	{
		return m_Filename;
	}

	/**
	 * Return the headless block of data in the image (this contains the JPEG you see when opening the file)
	 *
	 * @return headless JPEG tail
	 */
	public byte[] getImageTail()
	{
		return JPEGUtils.getImageTail(m_RawData);
	}

	/**
	 * Return the Metadata of the image (content of all APP1 marked blocks)
	 *
	 * @return metadata
	 */
	public byte[] getMetadata()
	{
		return JPEGUtils.getMetadata(m_RawData);
	}

	/**
	 * Returns the byte array that IO.read(image) returned
	 *
	 * @return byte array of the image file
	 */
	public byte[] getRawData()
	{
		return this.m_RawData;
	}

	/**
	 * Extracts and returns the content of the StandardXMP block
	 *
	 * @return content of the StandardXMP block
	 */
	public byte[] getStandardXMPBlockContent()
	{
		return JPEGUtils.getStandardXMPBlockContent(m_RawData);
	}

	/**
	 * Returns the Image tail (member)
	 *
	 * @return member image tail
	 */
	public byte[] getTail()
	{
		return m_ImageTail;
	}

	/**
	 * Extracts and returns the content of all XMP blocks concatenated (StandardXMP + ExtendedXMPs)
	 *
	 * @return content of the XMP blocks
	 */
	public byte[] getXMPBlocksContent()
	{
		return JPEGUtils.getXMPBlocksContent(m_RawData);
	}

	/**
	 * Return the member containing the ExtendedXMP information
	 *
	 * @return XmpExt (member)
	 */
	public byte[] getXmpExt()
	{
		return m_ExtendedXMP;
	}

	/**
	 * Return the member containing the StandardXMP information
	 *
	 * @return xmpSta (member)
	 */
	public byte[] getXmpSta()
	{
		return m_StandardXMP;
	}

	/**
	 * initializes the JPEG object.
	 * set the array containing the image as member rawData, set filename
	 * disassemble the Image and set the other members with content.
	 *
	 * @param raw      byte array of the image
	 * @param filename location
	 */
	private void init(byte[] raw, String filename)
	{
        if(ArrayUtils.arrayIsPartOfOtherArrayOnOffset(raw, Const.markJPG,0))
        {
            this.m_RawData = raw;
            this.m_Filename = filename;
            this.disassemble();
        }
	}
	
	public byte[] getDepthMap(String filename) {
		return base64.encode(IO.read(new File(filename)));
	}
	
	/**
	 * Inject a new depthmap into the jpeg ("replace" the old GDepth:Data value with the Base64 encoded png file)
	 *
	 * @param filename location of the new depthmap png
	 * @return success
	 */
	public boolean injectDepthMap(String filename)
	{
		byte[] depth = base64.encode(IO.read(new File(filename)));
		byte[] newExtendedXMP = JPEGUtils.replace(m_ExtendedXMP, keyGDepthData, depth);
		if (newExtendedXMP != null)
		{
			m_ExtendedXMP = newExtendedXMP;
			return true;
		} else return false;
	}

	public boolean injectDepthMap(byte[] data)
	{
		byte[] depth = base64.encode(data);
		byte[] newExtendedXMP = JPEGUtils.replace(m_ExtendedXMP, keyGDepthData, depth);
		if (newExtendedXMP != null)
		{
			m_ExtendedXMP = newExtendedXMP;
			return true;
		} else return false;
	}

	/**
	 * Inject a new unblurred source image into the jpeg ("replace" the old GImage:Data value with the Base64 encoded jpg file)
	 *
	 * @param filename location of the new unblurred source image jpg
	 * @return success
	 */
	public boolean injectSourceImage(String filename)
	{
		byte[] image = base64.encode(IO.read(new File(filename)));
		byte[] newExtendedXMP = JPEGUtils.replace(m_ExtendedXMP, keyGImageData, image);
		if (newExtendedXMP != null)
		{
			m_ExtendedXMP = newExtendedXMP;
			return true;
		} else return false;
	}

	public boolean injectSourceImage(byte[] data)
	{
		byte[] image = base64.encode(data);
		byte[] newExtendedXMP = JPEGUtils.replace(m_ExtendedXMP, keyGImageData, image);
		if (newExtendedXMP != null)
		{
			m_ExtendedXMP = newExtendedXMP;
			return true;
		} else return false;
	}

	/**
	 * Reassemble a functional jpeg byte array from block data.
	 * Set rawData to the new array and also return it
	 *
	 * @return reassembled jpeg byte array
	 */
	public byte[] reassemble()
	{
		byte[] md5 = HexUtil.generateMD5(m_ExtendedXMP);

		byte[] out = markJPG;
		out = ArrayUtils.concatenate(out, JPEGUtils.decorateBlock(m_Exif, EXIF));
		out = ArrayUtils.concatenate(out, JPEGUtils.decorateBlock(m_StandardXMP, STANDARDXMP));
		out = ArrayUtils.concatenate(out, JPEGUtils.decorateBlock(m_ExtendedXMP, EXTENDEDXMP));
		out = ArrayUtils.concatenate(out, m_ImageTail);
		out = JPEGUtils.replace(out, keyHasExtendedXMP, md5);
		this.m_RawData = out;
		return out;
	}

	/**
	 * Write the image to disk (location is defined in filename)
	 */
	public void save()
	{
		this.save(m_Filename);
	}

	/**
	 * Write the image to the file specified in "file"
	 *
	 * @param file
	 */
	public void save(String file)
	{
		IO.write(this.reassemble(), file);
	}

	/**
	 * Set the exif member to a new byte array
	 *
	 * @param exif new array
	 */
	public void setExif(byte[] exif)
	{
		this.m_Exif = exif;
	}

	/**
	 * Set the tail member array to a new byte array
	 *
	 * @param tail new array
	 */
	public void setTail(byte[] tail)
	{
		this.m_ImageTail = tail;
	}

	/**
	 * set the xmpExt member array to a new byte array
	 *
	 * @param xmpExt new array
	 */
	public void setXmpExt(byte[] xmpExt)
	{
		this.m_ExtendedXMP = xmpExt;
	}

	/**
	 * set the xmpSta member array to a new byte array
	 *
	 * @param xmpSta new array
	 */
	public void setXmpSta(byte[] xmpSta)
	{
		this.m_StandardXMP = xmpSta;
	}
}

