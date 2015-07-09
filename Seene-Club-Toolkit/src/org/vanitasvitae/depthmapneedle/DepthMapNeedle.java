package org.vanitasvitae.depthmapneedle;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
import java.util.Base64;

public class DepthMapNeedle
{
	/**
	 * Interprete the arguments and execute the programm
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		Base64Wrapper wrapper = new Base64Wrapper(){
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
		//No arguments given or '-h' as first argument -> show help text
		if (args.length == 0 || args[0].equals("-h")) help();

		//export depthmap
		if (args.length >= 2 && args[0].equals("-d"))
		{
			for (int i = 1; i < args.length; i++)
			{
				JPEG image = new JPEG(args[i], wrapper);
				if (image.exportDepthMap())
					System.out.println("Depthmap extracted for file "+args[i]+".");
				else System.err.println("There is no Depthmap in file "+args[i]);
			}
		}

		//export source image
		else if (args.length >= 2 && args[0].equals("-s"))
		{
			for (int i = 1; i < args.length; i++)
			{
				JPEG image = new JPEG(args[i], wrapper);
				if (image.exportSourceImage())
					System.out.println("Unblurred source image extracted for file "+args[i]+".");
				else
					System.err.println("There is no unblurred source image in file "+args[i]+". Maybe this photo has not been taken with the blur function?");
			}
		}

		//inject depthmap
		else if (args.length >= 3 && args[0].equals("-D"))
		{
			String depthmap = args[1];
			for (int i = 2; i < args.length; i++)
			{
				JPEG image = new JPEG(args[i], wrapper);
				if (image.injectDepthMap(depthmap))
					System.out.println("Depthmap injected into file "+args[i]+".");
				else
					System.err.println("Something went wrong while injecting "+depthmap+" into "+args[i]+".\nRemember: The first argument has to be a png and the following arguments must be jpgs shot with the blur function.");
				image.save();
			}
		}
		//inject source image
		else if (args.length >= 3 && args[0].equals("-S"))
		{
			String source = args[1];
			for (int i = 2; i < args.length; i++)
			{
				JPEG image = new JPEG(args[i], wrapper);
				if (image.injectSourceImage(source))
					System.out.println("Source image injected into file "+args[i]+".");
				else
					System.err.println("Something went wrong while injecting "+source+" into "+args[i]+".\nRemember: The first argument has to be a jpg and the following arguments must be jpgs shot with the blur function.");
				image.save();
			}
		}
	}

	/**
	 * Show help text
	 */
	public static void help()
	{
		System.out.println("Welcome to DepthMapNeedle!"
				+"\nDepthMapNeedle is a tool to inject or extract depth information in form of depthmaps from photos shot using Google Cameras Blur function."
				+"\n"
				+"\nAvailable Options:"
				+"\n'-d <file1>.jpg ... <fileN>.jpg':"
				+"\n   Extract the depthmap from the specified photo(s). The depthmaps will be stored with the suffix \"_d.png\"."
				+"\n'-s <file1>.jpg ... <fileN>.jpg':"
				+"\n   Extract the unblurred source image from the photo(s). These files will be stored with the suffix \"_s.jpg\"."
				+"\n'-D <depthmap>.png <file1>.jpg ... <fileN>.jpg':"
				+"\n   Inject a png file as Depthmap into the following specified jpg files."
				+"\n'-S <unblurred>.jpg <file1>.jpg ... <fileN>.jpg':"
				+"\n   Inject an unblurred source image into the following specified jpg files. "
				+"\n   Note: The unblurred source image is a simple jpg file of the same dimensions as the photos."
				+"\n'-h':"
				+"\n   Show this help text.");
	}
}
