package org.seeneclub.toolkit;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import com.vdurmont.emoji.EmojiParser;

@SuppressWarnings("rawtypes")
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
		result.api_token_expires_at = parseISO8601((String)map.get("api_token_expires_at"));
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
	
	private static Map request(Token token, String method, URL url, Map<String,String> params) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(20000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod(method);
	    if (token != null)
		    conn.setRequestProperty("Authorization",
		    		String.format("Seene api=%s,user=%s",
		    				token.api_id,
		    				token.api_token)
		    		);
	    conn.setRequestProperty("Accept", "application/vnd.seene.co; version=3,application/json");

	    conn.setDoOutput(true);
	    conn.setDoInput(true);

	    if(params != null) {
		    OutputStream os = conn.getOutputStream();
	    	BufferedWriter writer = new BufferedWriter(
	            new OutputStreamWriter(os, "UTF-8"));
	    	writer.write(getQuery(params));
	    	writer.flush();
	    	writer.close();
		    os.close();
		}

	    conn.connect();

	    // create JSON object from content
	    return (Map)JSONValue.parseWithException(
	    		new InputStreamReader(conn.getInputStream(), 
	    				StandardCharsets.UTF_8));
	}
	
	public static String usernameToId(String username) throws Exception {
		Map map = request(null, 
				"GET", 
				new URL("http://seene.co/api/seene/-/users/@" + username), 
				null);
		return map.get("id").toString();
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
	
	public static Date parseISO8601(String s) throws ParseException {
		//Calendar calendar = GregorianCalendar.getInstance();
		s = s.replace("Z", "+00:00");
		try {
		    s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
		} catch (IndexOutOfBoundsException e) {
		    throw new ParseException("Invalid length", 0);
		}
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
    }
	
	public static List<SeeneObject> getPublicSeenes(String userId, int last) throws Exception {
		Map map = request(null, 
				"GET", 
				new URL(String.format("http://seene.co/api/seene/-/users/%s/scenes?count=%d", userId, last)), 
				null);
		
		return createFromResponse(map);    	
	}

	public static List<SeeneObject> getPrivateSeenes(Token token, String userId, int last) throws Exception {
		Map map = request(token, 
				"GET", 
				new URL(String.format("https://oecamera.herokuapp.com/api/users/%s/scenes?count=%d&only_private=1", userId, last)), 
				null);
		
		return createFromResponse(map);    	
	}

	private static List<SeeneObject> createFromResponse(Map map)
			throws ParseException, MalformedURLException {
		List<SeeneObject> result = new ArrayList<SeeneObject>();

		List scenes = (List)map.get("scenes");
		for (Object o : scenes)
			result.add(createFromMap((Map)o));
		
		return result;
	}

	private static SeeneObject createFromMap(Map j) throws ParseException,
			MalformedURLException {
		SeeneObject s = new SeeneObject();
		s.setCaptured_at(parseISO8601((String)j.get("captured_at")));
		s.setCaption(EmojiParser.parseToAliases((String)j.get("caption")));
		s.setFilter_code((String)j.get("filter_code"));
		s.setShortCode((String)j.get("short_code"));
		s.setTextureURL(new URL((String)j.get("poster_url")));
		s.setModelURL(new URL((String)j.get("model_url")));
		return s;
	}

}
