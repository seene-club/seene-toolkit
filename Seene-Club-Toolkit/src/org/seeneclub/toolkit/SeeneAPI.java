package org.seeneclub.toolkit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class SeeneAPI {

	public static class Token {
		public String api_id;
		
		public String api_token;
		public Date api_token_expires_at;
		
		public Token() {
			api_token = "(null)";
		}
	}
	
	/// can be cached until expires; if expires is null, cached forever.
	@SuppressWarnings("rawtypes")
	public static Token login(String apiId, String username, String password) throws Exception {
		Token result = new Token();
		result.api_id = apiId;

		Map<String,String> params = new HashMap<String,String>();
		params.put("username", username);
		params.put("password", password);
		Map map = request(result, 
				"POST", 
				new URL("https://oecamera.herokuapp.com/api/users/authenticate"), 
				params);
		result.api_token = (String) map.get("api_token");
		//TODO result.apiTokenExpiresAt = json.getString("api_token_expires_at");
		return result;
	}
	
	private static String getQuery(Map<String,String> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (Map.Entry<String, String> entry : params.entrySet())
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private static Map request(Token token, String method, URL url, Map<String,String> params) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod(method);
	    conn.setRequestProperty("Authorization",
	    		String.format("Seene api=%s,user=%s",
	    				token.api_id,
	    				token.api_token)
	    		);
	    conn.setRequestProperty("Accept", "application/vnd.seene.co; version=3,application/json");

	    conn.setDoOutput(true);
	    conn.setDoInput(true);

	    OutputStream os = conn.getOutputStream();
	    BufferedWriter writer = new BufferedWriter(
	            new OutputStreamWriter(os, "UTF-8"));
	    writer.write(getQuery(params));
	    writer.flush();
	    writer.close();
	    os.close();

	    conn.connect();

	    // create JSON object from content
	    return (Map)JSONValue.parseWithException(
	    		new InputStreamReader(conn.getInputStream(), 
	    				StandardCharsets.UTF_8));
	}

	public class Item {
		public String access_key_id;
		public String bucket_name;
		public String model_dir;
		public String poster_dir;
		public String session_token;
		public String secret_access_key;
	}
	/*
	/// Only scene.(header fields) are used at this stage
	public Item createItem(Token token, SeeneObject obj) {
		Item result = new Item(); 
		//  TODO
		return result;
	}
	
	/// after files are uploaded to S3
	/// Only scene.identifier is used
	public void finalizeItem(Token token, SeeneObject obj) {
		// TODO
	}
	
	*/

}