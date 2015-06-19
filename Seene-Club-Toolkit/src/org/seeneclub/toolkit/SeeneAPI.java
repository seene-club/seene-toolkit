package org.seeneclub.toolkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
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
	
	
	@SuppressWarnings("deprecation")
	public static void uploadSeene(File uploadsLocalDir, SeeneObject sO, String username, Token token) throws MalformedURLException, Exception {
		Map<String,String> params = new HashMap<String,String>();
		
		params.put("caption", sO.getCaption());
		params.put("captured_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sO.getCaptured_at()));
		params.put("filter_code", sO.getFilter_code());
		params.put("flash_level", Integer.toString(sO.getFlash_level()));
		params.put("identifier", "");
		params.put("orientation", Integer.toString(sO.getOrientation()));
		params.put("shared", Integer.toString(sO.getShared()));
		params.put("storage_version", Integer.toString(sO.getStorage_version()));
		
		SeeneToolkit.log("Preparing upload on Seene servers...",LogLevel.info);
		
		// PREPARING UPLOAD (CREATING ENTRY)
		Map metamap = request(token, "POST", new URL("https://oecamera.herokuapp.com/api/scenes"), params);
		
		Map awsmeta = (Map)metamap.get("meta");
		Map scenemeta = (Map)metamap.get("scene");
		
		SeeneAWS awsMeta = new SeeneAWS();
		
		awsMeta.setAccess_key_id((String)awsmeta.get("access_key_id"));
		awsMeta.setBucket_name((String)awsmeta.get("bucket_name"));
		awsMeta.setModel_dir((String)awsmeta.get("model_dir"));
		awsMeta.setPoster_dir((String)awsmeta.get("poster_dir"));
		awsMeta.setSession_token((String)awsmeta.get("session_token"));
		awsMeta.setSecret_access_key((String)awsmeta.get("secret_access_key"));
		sO.setAWSmeta(awsMeta);
		sO.setIdentifier(UUID.fromString((String)scenemeta.get("identifier")));
		sO.setShortCode((String)scenemeta.get("short_code"));
		sO.setUserinfo(username);
		
		SeeneToolkit.log("Starting file upload for new Seene " + sO.getShortCode(),LogLevel.info);
		
		String folderName = SeeneStorage.generateSeeneFolderName(sO, username);
		
		File savePath = new File(uploadsLocalDir.getAbsolutePath() + File.separator + folderName);
		savePath.mkdirs();
		
		File mFile = new File(savePath.getAbsoluteFile() + File.separator + "scene.oemodel");
		File pFile = new File(savePath.getAbsoluteFile() + File.separator + "poster.jpg");
		sO.getModel().saveModelDateToFile(mFile);
		sO.getPoster().saveTextureToFile(pFile);
		Helper.createFolderIcon(savePath, null);
		
		// UPLOADING THE FILES
		awsFileUpload(awsMeta.getModel_dir(),mFile,"application/octet-stream",awsMeta);
		awsFileUpload(awsMeta.getPoster_dir(),pFile,"image/jpeg",awsMeta);
		
		// FINALIZING
		URL patchURL = new URL("https://oecamera.herokuapp.com/api/scenes/" + sO.getIdentifier());
		
		SeeneToolkit.log("Finalizing new Seene " + sO.getShortCode(),LogLevel.info);
		
		// Unfortunately a HttpsURLConnection does not support method PATCH
		// so we use Apache HttpComponents instead (https://hc.apache.org/)
		HttpClient client = new DefaultHttpClient();
		HttpPatch hp = new HttpPatch(patchURL.toURI());
		hp.setHeader("Authorization", String.format("Seene api=%s,user=%s",token.api_id,token.api_token));
		hp.setHeader("Accept", "application/vnd.seene.co; version=3,application/json");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("finalize", "1"));
		hp.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		client.execute(hp);
		
		SeeneToolkit.log("Upload finished. Check your private seenes!",LogLevel.info);
		
	}
	
	private static void awsFileUpload(String targetDir, File file, String mimeType, SeeneAWS meta) {
		try {
			String dateHeader = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",Locale.US).format(new Date());
			String fileName = file.getName();
			String resource  = "/" + meta.getBucket_name() + "/" + targetDir + "/" + fileName;
			StringBuffer stringToSign = new StringBuffer("PUT\n\n");
			stringToSign.append(mimeType + "\n");
			stringToSign.append(dateHeader + "\n");
			stringToSign.append("x-amz-acl:public-read\n");
			stringToSign.append("x-amz-security-token:");
			stringToSign.append(meta.getSession_token() + "\n");
			stringToSign.append(resource);
			String signature = SeeneAWSsignature.calculateRFC2104HMAC(stringToSign.toString(), meta.getSecret_access_key()); 
			
			URL url = new URL("https://" + meta.getBucket_name() + ".s3.amazonaws.com/" + targetDir + "/" + fileName);
			
			SeeneToolkit.log("Uploading " + fileName + " (" + mimeType + ") to target: " + targetDir,LogLevel.info);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Host", meta.getBucket_name() + ".s3.amazonaws.com");
			conn.setRequestProperty("Authorization", "AWS " + meta.getAccess_key_id() + ":" + signature);
			conn.setRequestProperty("x-amz-acl", "public-read");
			conn.setRequestProperty("x-amz-security-token", meta.getSession_token());
			conn.setRequestProperty("Date", dateHeader);
			conn.setRequestProperty("Content-Type", mimeType);
			
			InputStream in = new FileInputStream(file.getAbsolutePath());
		    OutputStream out = conn.getOutputStream();
		    copyStream(in, conn.getOutputStream());
		    out.flush();
		    out.close();
		    conn.getInputStream();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected static long copyStream(InputStream input, OutputStream output) throws IOException {
	    byte[] buffer = new byte[12288]; // 12K
	    long count = 0L;
	    int n = 0;
	    while (-1 != (n = input.read(buffer))) {
	        output.write(buffer, 0, n);
	        count += n;
	    }
	    return count;
	}
	
	public static List<SeeneObject> getPublicSeeneByURL(String surl) throws Exception {
		 
		if (surl.endsWith("/")) surl = surl.substring(0, surl.length()-1);
		String shortkey = surl.substring(surl.lastIndexOf('/') + 1);
		
		Map map = request(null, 
				"GET", 
				new URL(String.format("http://seene.co/api/seene/-/scenes/%s", shortkey)), 
				null);
		
		List<SeeneObject> result = new ArrayList<SeeneObject>();
		result.add(createFromMap(map));
		
		return result;
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
