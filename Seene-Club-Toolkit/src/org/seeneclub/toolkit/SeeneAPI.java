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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
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

import org.apache.commons.codec.binary.Base64;
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

@SuppressWarnings({ "rawtypes", "deprecation" })
public class SeeneAPI {
	
	private ProxyData proxyData;
	
	public SeeneAPI(ProxyData proxyData) {
		this.proxyData = proxyData;
	}

	public static class Token {
		public String api_id;
		
		public String api_token;
		public Date api_token_expires_at;
		
		public Token() {
			api_token = "(null)";
		}
	}
	
	/// can be cached until expires; if expires is null, cached forever.
	public Token login(String apiId, String username, String password) throws Exception {
		
		if ((apiId.length() == 0) || (apiId.equals(STK.CONFIG_API_ID_HINT))) {
			//SeeneToolkit.log("API-ID not configured!\nTrying to retrieve from remote service...",LogLevel.info);
			apiId = getSeeneKeyFromRemoteServer(username, password,STK.API_ID_KEY);
			//SeeneToolkit.log(apiId,LogLevel.debug);
		}
		
		if (apiId.length() != 40) throw new Exception("API-ID format exception!");

		Token result = new Token();
		result.api_id = apiId;

		Map<String,String> params = new HashMap<String,String>();
		params.put("username", username);
		params.put("password", password);
		Map map = request(result, "POST", 
				new URL("https://oecamera.herokuapp.com/api/users/authenticate"),params);
		result.api_token = (String) map.get("api_token");
		result.api_token_expires_at = parseISO8601((String)map.get("api_token_expires_at"));
		return result;
	}
	
	// Retrieves a key with the key_id from a WebService. Keys are only returned for experienced users with valid credentials
	private static String getSeeneKeyFromRemoteServer(String username, String password, String key_id) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		URL url = new URL("https://54.243.113.182/actions/" + key_id);
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
	
	public Map requestUserInfo(String uID, String bearer) throws Exception {

		HttpURLConnection conn = proxyGate(new URL("https://api.seene.co/users/" + uID));
		
		conn.setReadTimeout(20000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod("GET");
	    
	    conn.setRequestProperty("Host","https://api.seene.co");
	    conn.setRequestProperty("Authorization", "Bearer " + bearer);
	    conn.setRequestProperty("Accept", "application/vnd.seene.v1+json");
	    conn.setRequestProperty("Content-Type", "application/json");
	    //conn.setRequestProperty("Accept-Encoding", "gzip");
	    conn.setRequestProperty("Cache-Control", "no-cache");
		
	    conn.setDoInput(true);
	    
	    // create JSON object from response
	    Map response = (Map)JSONValue.parseWithException(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	    
		return response;
	}
	
	public Map requestNewSeene(SeeneObject sO, String bearer) throws Exception {
		HttpURLConnection conn = proxyGate(new URL("https://api.seene.co/seenes"));
		
		conn.setReadTimeout(20000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod("POST");
	    
	    conn.setRequestProperty("Host","https://api.seene.co");
	    conn.setRequestProperty("Authorization", "Bearer " + bearer);
	    conn.setRequestProperty("Accept", "application/vnd.seene.v1+json");
	    conn.setRequestProperty("Content-Type", "application/json");
	    //conn.setRequestProperty("Accept-Encoding", "gzip");
	    conn.setRequestProperty("Cache-Control", "no-cache");
		
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    
	    OutputStream os = conn.getOutputStream();
    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
    	writer.write("{");
   		writer.write("\"caption\": \"" + sO.getCaption() + "\",");
   		writer.write("\"captured_at\": \"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sO.getCaptured_at()) + "\",");
   		writer.write("\"shared\": \"false\"");
        writer.write("}");

    	writer.flush();
    	writer.close();
	    os.close();
	    
	    // create JSON object from response
	    Map response = (Map)JSONValue.parseWithException(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	    
		return response;
	}
	
	public void uploadSeene(SeeneObject sO, String bearer) throws Exception {
		Map response = requestNewSeene(sO, bearer);
		
		sO.setIdentifier((String)response.get("identifier"));
		Map upload_url = (Map)response.get("upload_url");
		Map upload_fields = (Map)upload_url.get("fields");
		
		SeeneAWSUploadMeta awsMeta = new SeeneAWSUploadMeta();
		awsMeta.setUpload_url((String)upload_url.get("url"));
		awsMeta.setSignature((String)upload_fields.get("signature"));
		awsMeta.setAWSAccessKeyId((String)upload_fields.get("AWSAccessKeyId"));
		awsMeta.setAcl((String)upload_fields.get("acl"));
		awsMeta.setKey((String)upload_fields.get("key"));
		awsMeta.setPolicy((String)upload_fields.get("policy"));
		
		String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
		
		HttpURLConnection conn = proxyGate(new URL(awsMeta.getUpload_url()));
		
		conn.setReadTimeout(20000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod("POST");
	    
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    
	    conn.setRequestProperty("Cache-Control", "no-cache");
	    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
	    
	    OutputStream os = conn.getOutputStream();
	    PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"),true);
    	
    	writer.println("--" + boundary);
    	writer.println("Content-Disposition: form-data; name=\"AWSAccessKeyId\"");
    	writer.println();
    	writer.println(awsMeta.getAWSAccessKeyId());
    	
    	writer.println("--" + boundary);
    	writer.println("Content-Disposition: form-data; name=\"key\"");
    	writer.println();
    	writer.println(awsMeta.getKey());
    	
    	writer.println("--" + boundary);
    	writer.println("Content-Disposition: form-data; name=\"policy\"");
    	writer.println();
    	writer.println(awsMeta.getPolicy());
    	
    	writer.println("--" + boundary);
    	writer.println("Content-Disposition: form-data; name=\"signature\"");
    	writer.println();
    	writer.println(awsMeta.getSignature());
    	
    	writer.println("--" + boundary);
    	writer.println("Content-Disposition: form-data; name=\"acl\"");
    	writer.println();
    	writer.println(awsMeta.getAcl());
    	
    	writer.println("--" + boundary);
    	writer.println("Content-Disposition: form-data; name=\"Content-Type\"");
    	writer.println();
    	writer.println("image/jpeg");
    	
    	writer.println("--" + boundary);
    	//writer.println("Content-Disposition: form-data; name=\"file\"; filename=\"" + sO.getXMP_combined().getName() + "\"");
    	//writer.println("Content-Disposition: form-data; name=\"file\"");
    	writer.println("Content-Disposition: form-data; name=\"file\"; filename=\"poster-gdepth.jpg\"");
    	writer.println("Content-Transfer-Encoding: binary");
    	writer.println();
    	
    	writer.flush();
    	
    	FileInputStream inputStream = new FileInputStream(sO.getXMP_combined());
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        inputStream.close();
        
        //writer.println();
        writer.println();
        writer.println("--" + boundary + "--");
        writer.flush();
        writer.close();
        
        os.close();
  
        int responseCode = conn.getResponseCode();
        System.out.println("ResponseCode is : " + responseCode);
        
        // FINALIZING
     	URL patchURL = new URL("https://api.seene.co/seenes/" + sO.getIdentifier());
     		
     	SeeneToolkit.log("Finalizing new Seene " + sO.getIdentifier(),LogLevel.info);
     		
     	// Unfortunately the HttpsURLConnection does not support method PATCH
     	// so we use Apache HttpComponents instead (https://hc.apache.org/)
     	HttpClient client = new DefaultHttpClient();
     	HttpPatch hp = new HttpPatch(patchURL.toURI());
     	hp.setHeader("Authorization", "Bearer " + bearer);
     	hp.setHeader("Accept", "application/vnd.seene.v1+json");
   		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
   		nvps.add(new BasicNameValuePair("finalize", "true"));
   		hp.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
   		client.execute(hp);
     		
   		SeeneToolkit.log("Upload finished. Check your private seenes!",LogLevel.info);
	}

	
	public Map requestBearerToken(String username, String password, String auth_or_refresh_code, Boolean isRefresh) throws Exception {
		
		String sclient = getSeeneKeyFromRemoteServer(username, password, STK.API_CLIENT_KEY);
		byte[] basicBytes = new String(STK.API_CLIENT_ID + ":" + sclient).getBytes();
		String basicBase64 = new String(Base64.encodeBase64(basicBytes));
		
		HttpURLConnection conn = proxyGate(new URL("https://api.seene.co/oauth/token"));
		
		conn.setReadTimeout(20000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod("POST");
	    
	    conn.setRequestProperty("Host","https://api.seene.co");
	    conn.setRequestProperty("Authorization", "Basic " + basicBase64);
	    conn.setRequestProperty("Accept", "application/vnd.seene.v1+json");
	    conn.setRequestProperty("Content-Type", "application/json");
	    //conn.setRequestProperty("Accept-Encoding", "gzip");
	    conn.setRequestProperty("Cache-Control", "no-cache");
	    
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    
	    OutputStream os = conn.getOutputStream();
    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
    	writer.write("{");
    	if (isRefresh) {
    		writer.write("\"grant_type\": \"refresh_token\",");
    		writer.write("\"redirect_uri\": \"" + STK.API_REDIRECT + "\",");
    		writer.write("\"refresh_token\": \"" + auth_or_refresh_code + "\"");
    	} else {
    		writer.write("\"grant_type\": \"authorization_code\",");
    		writer.write("\"redirect_uri\": \"" + STK.API_REDIRECT + "\",");
    		writer.write("\"code\": \"" + auth_or_refresh_code + "\"");
    	}
        writer.write("}");

    	writer.flush();
    	writer.close();
	    os.close();
	    
	    // create JSON object from response
	    Map response = (Map)JSONValue.parseWithException(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	    
		return response;
	}
	
	private Map request(Token token, String method, URL url, Map<String,String> params) throws Exception {
		
		SeeneToolkit.log("REQUEST.url: " + url.toString(),LogLevel.debug);
		//if(params != null) SeeneToolkit.log("REQUEST.params: " + getQuery(params),LogLevel.debug);
		
		HttpURLConnection conn = proxyGate(url);
		
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
	    
	    // create JSON object from response
	    Map response = (Map)JSONValue.parseWithException(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	    
	    //SeeneToolkit.log("RESPONSE.json: " + response.toString(),LogLevel.debug);
	    
	    return response;
	}

	private HttpURLConnection proxyGate(URL url) throws IOException {
		HttpURLConnection conn = null;
		
		// Proxy configured?
		if ((proxyData!=null) && (proxyData.getHost().length()>0)) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, 
					new InetSocketAddress(proxyData.getHost(), proxyData.getPort()));
			
			// Proxy with Authentication?
			if (proxyData.getUser().length()>0) {
				Authenticator authenticator = new Authenticator() {
		
			        public PasswordAuthentication getPasswordAuthentication() {
			            return (new PasswordAuthentication(proxyData.getUser(),
			                    proxyData.getPass().toCharArray()));
			        }
			    };
			    Authenticator.setDefault(authenticator);
			}
			
		    // Connection with Proxy!
			SeeneToolkit.log("Using Proxy: " + proxyData.getHost(), LogLevel.debug);
		    conn = (HttpURLConnection) url.openConnection(proxy);
		} else {
			// Connection without Proxy!
			SeeneToolkit.log("Using NO Proxy!", LogLevel.debug);
			conn = (HttpURLConnection) url.openConnection();
		}
		return conn;
	}
	
	public  String usernameToId(String username) throws Exception {
		Map map = request(null, "GET", 
				new URL("http://seene.co/api/seene/-/users/@" + username), null);
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
	
	public List<SeeneObject> getPublicSeenes(String userId, int last) throws Exception {
		Map map = request(null, 
				"GET", 
				new URL(String.format("http://seene.co/api/seene/-/users/%s/scenes?count=%d", userId, last)), 
				null);
		
		return createSeeneListFromResponse(map);    	
	}

	public List<SeeneObject> getPrivateSeenes(Token token, String userId, int last) throws Exception {
		Map map = request(token, 
				"GET", 
				new URL(String.format("https://oecamera.herokuapp.com/api/users/%s/scenes?count=%d&only_private=1", userId, last)), 
				null);
		
		return createSeeneListFromResponse(map);    	
	}
	
	
	@SuppressWarnings("deprecation")
	public void uploadSeeneOldMethod(File uploadsLocalDir, SeeneObject sO, String username, Token token) throws MalformedURLException, Exception {
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
		
		SeeneAWSmetadataOldMethod awsMeta = new SeeneAWSmetadataOldMethod();
		
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
		
		File mFile = new File(savePath.getAbsoluteFile() + File.separator + STK.SEENE_MODEL);
		File pFile = new File(savePath.getAbsoluteFile() + File.separator + STK.SEENE_TEXTURE);
		sO.getModel().saveModelDataToFile(mFile);
		sO.getPoster().saveTextureToFile(pFile);
		Helper.createFolderIcon(savePath, null);
		
		// UPLOADING THE FILES
		awsFileUploadOldMethod(awsMeta.getModel_dir(),mFile,"application/octet-stream",awsMeta);
		awsFileUploadOldMethod(awsMeta.getPoster_dir(),pFile,"image/jpeg",awsMeta);
		
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
	
	private static void awsFileUploadOldMethod(String targetDir, File file, String mimeType, SeeneAWSmetadataOldMethod meta) {
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
			
			SeeneToolkit.log("Uploading " + fileName + " (" + mimeType + ")",LogLevel.info);
			
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
		    in.close();

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
	
	public List<SeeneObject> getPublicSeeneByURL(String surl) throws Exception {
		 
		if (surl.endsWith("/")) surl = surl.substring(0, surl.length()-1);
		String shortkey = surl.substring(surl.lastIndexOf('/') + 1);
		
		Map map = request(null, 
				"GET", 
				new URL(String.format("http://seene.co/api/seene/-/scenes/%s", shortkey)), 
				null);
		
		List<SeeneObject> result = new ArrayList<SeeneObject>();
		result.add(createSeeneObjectFromMap(map));
		
		return result;
	}

	private static List<SeeneObject> createSeeneListFromResponse(Map map)
			throws ParseException, MalformedURLException {
		List<SeeneObject> result = new ArrayList<SeeneObject>();

		List scenes = (List)map.get("scenes");
		for (Object o : scenes)
			result.add(createSeeneObjectFromMap((Map)o));
		
		return result;
	}

	private static SeeneObject createSeeneObjectFromMap(Map j) throws ParseException,
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

	// Getter and Setter
	public ProxyData getProxyData() {
		return proxyData;
	}
	public void setProxyData(ProxyData proxyData) {
		this.proxyData = proxyData;
	}

}
