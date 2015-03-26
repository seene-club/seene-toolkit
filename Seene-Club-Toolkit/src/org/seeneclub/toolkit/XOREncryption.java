package org.seeneclub.toolkit;


// very simple xor-encryption just to store password in the config
public class XOREncryption {
	
	public static String xorIt(String input) {
		char[] key = {'S', 'C', 'P', 'A', 'F'}; //Can be any chars, and any length array
		StringBuilder output = new StringBuilder();
		for(int i = 0; i < input.length(); i++) {
			output.append((char) (input.charAt(i) ^ key[i % key.length]));
		}
		return output.toString();
	}
}