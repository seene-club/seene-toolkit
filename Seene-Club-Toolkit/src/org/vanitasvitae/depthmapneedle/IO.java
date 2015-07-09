package org.vanitasvitae.depthmapneedle;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
public class IO
{
	/**
	 * Read file from disk to byte array
	 *
	 * @param file path to file
	 * @return byte array
	 */
	public static byte[] read(File file)
	{
		try
		{
			InputStream is = new FileInputStream(file);
			byte[] b = new byte[(int) file.length()];
			is.read(b);
			is.close();
			return b;
		} catch (IOException e)
		{
			System.err.println("Couldn't read file "+file.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Write byte array to file on disk
	 *
	 * @param b        byte array
	 * @param filepath path to outputfile
	 */
	public static void write(byte[] b, String filepath)
	{
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(filepath);
			fos.write(b);
			fos.close();
		} catch (Exception e)
		{
			System.err.println("Couldn't write file "+filepath);
			e.printStackTrace();
		}
	}
}
