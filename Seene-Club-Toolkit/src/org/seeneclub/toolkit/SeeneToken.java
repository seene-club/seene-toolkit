package org.seeneclub.toolkit;

import java.security.KeyStore.Entry;
import java.util.*;

public class SeeneToken {

	public class Token {
		public String api_token;
		public Date api_token_expires_at;
	}
	
	/// can be cached until expires; if expires is null, cached forever.
	Token login(String user, String password) {
	/*
		"api_token": "Z......................................M",
		"api_token_expires_at": null (or "2015-03-25T20:35:47.522Z"),
	*/
		//return "TODO"; // zettlerm - commented out to compile / refactor
		return null;
	}

	public class Item {
		public String access_key_id;
		public String bucket_name;
		public String model_dir;
		public String poster_dir;
		public String session_token;
		public String secret_access_key;
	}
	
	/* zettlerm - commented out to compile / refactor
	
	/// Only scene.(header fields) are used at this stage
	Item createItem(Token token, Scene scene) {
		Entry result = new Entry; 
		//  TODO
		return result;
	}
	
	/// after files are uploaded to S3
	/// Only scene.identifier is used
	void finalizeItem(Token token, Scene scene, Token token) {
		// TODO
	}
	
	*/

}
