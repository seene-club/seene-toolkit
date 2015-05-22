package org.seeneclub.toolkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONValue;
import org.seeneclub.domainvalues.LogLevel;

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
		
		if ((apiId.length() == 0) || (apiId.equals("<insert Seene API ID here>"))) {
			apiId = getSeeneAPIidFromRemoteServer(username, password);
			//SeeneToolkit.log(apiId,LogLevel.debug);
		}
		
		if (apiId.length() != 40) throw new Exception("API-ID format exception!");

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
	
	// Retrieves the API-ID from a WebService. API-ID is only returned for experienced users with valid credentials
	private static String getSeeneAPIidFromRemoteServer(String username, String password) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		URL url = new URL("https://54.243.113.182/actions/sapi");
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("username", username);
        params.put("password", password);
        
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        
        TrustManager[] trustMyCert = new TrustManager[] {
       	   new X509TrustManager() {
       	      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
       	        return null;
       	      }

       	      public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

       	      public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

      	   }
      	};

      	SSLContext sc = SSLContext.getInstance("SSL");
      	sc.init(null, trustMyCert, new java.security.SecureRandom());
      	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

       	// Create host name verifier that trusts IP 54.243.113.182
      	HostnameVerifier myHostValid = new HostnameVerifier() {
      	    public boolean verify(String hostname, SSLSession session) {
      	    	if (hostname.equals("54.243.113.182"))
                   return true;
                return false;
    	    }
   		};

       	HttpsURLConnection.setDefaultHostnameVerifier(myHostValid);
        
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder result = new StringBuilder();
        for ( int c = in.read(); c != -1; c = in.read() )
        	result.append((char)c);
        
        return result.toString();
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
		if (s == null)
			return null;
		
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
		String caption = (String)j.get("caption");
		if(caption == null)
			caption = ""; // playing nice to £clients
		s.setCaption(EmojiParser.parseToAliases(caption));
		s.setFilter_code((String)j.get("filter_code"));
		s.setShortCode((String)j.get("short_code"));
		s.setTextureURL(new URL((String)j.get("poster_url")));
		s.setModelURL(new URL((String)j.get("model_url")));
		Map u = (Map)j.get("user");
		s.setUserinfo((String)u.get("username"));
		s.setAvatarURL(new URL((String)u.get("avatar_url")));
		return s;
	}

}
