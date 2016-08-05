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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONValue;
import org.seeneclub.domainvalues.LogLevel;

import com.adobe.xmp.XMPException;
import com.vdurmont.emoji.EmojiParser;

@SuppressWarnings({ "rawtypes", "deprecation" })
public class SeeneAPI {
	
	private ProxyData proxyData;
	
	public SeeneAPI(ProxyData proxyData) {
		this.proxyData = proxyData;
	}

	// Retrieves a key with the key_id from a WebService. Keys are only returned for experienced users with valid credentials
	private String getSeeneKeyFromRemoteServer(String username, String password, String key_id) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		URL url = new URL("https://" + STK.AWS_INSTANCE_IP_ADDRESS + "/actions/" + key_id);
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
      	    	if (hostname.equals(STK.AWS_INSTANCE_IP_ADDRESS))
                   return true;
                return false;
    	    }
   		};

       	HttpsURLConnection.setDefaultHostnameVerifier(myHostValid);
        
        //HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        HttpsURLConnection conn = (HttpsURLConnection) proxyGate(url);
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
	
	private HttpURLConnection openOAuthRequestConnection(String request_url, String request_method, String bearer) throws Exception {
		HttpURLConnection conn = proxyGate(new URL(request_url));
		
		conn.setReadTimeout(20000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod("GET");
	    
	    conn.setRequestProperty("Host","https://api.seene.co");
	    conn.setRequestProperty("Authorization", "Bearer " + bearer);
	    conn.setRequestProperty("Accept", "application/vnd.seene.v1+json");
	    conn.setRequestProperty("Content-Type", "application/json");
	    conn.setRequestProperty("Cache-Control", "no-cache");
	    conn.setDoInput(true);
	    
	    return conn;
	}
	
	public String requestUserID(String username, String bearer) throws Exception {
		String r_url =  new String("https://api.seene.co/users/@" + username);
		SeeneToolkit.log("Requesting User-ID from " + r_url, LogLevel.info);

		HttpURLConnection conn = openOAuthRequestConnection(r_url, "GET", bearer);
	    Map response = (Map)JSONValue.parseWithException(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		
		return response.get("id").toString();
	}
	
	public String requestUserIDfromOldAPI(String username) throws Exception {
		Map map = requestNonOAuth("GET", new URL("http://seene.co/api/seene/-/users/@" + username), null);
		return map.get("id").toString();
	}
	
	public Map requestUserInfo(String uID, String bearer) throws Exception {
		
		String r_url =  new String("https://api.seene.co/users/" + uID);
		SeeneToolkit.log("Requesting User Info from " + r_url, LogLevel.info);

		HttpURLConnection conn = openOAuthRequestConnection(r_url, "GET", bearer);
	    Map response = (Map)JSONValue.parseWithException(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	    
		return response;
	}
	
	public List<SeeneObject> requestUserSeenes(String userID, int count, String privat, String bearer) throws Exception {
		
		String r_url =  new String("https://api.seene.co/users/" + userID + "/seenes?count=" + count + "&private=" + privat);
		SeeneToolkit.log("Requesting Seenes from " + r_url, LogLevel.info);
		
		HttpURLConnection conn = openOAuthRequestConnection(r_url, "GET", bearer);
		Map response = (Map)JSONValue.parseWithException(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		
	    return createSeeneListFromResponse(response,"seenes");   
	}
	
	public Map requestNewSeene(SeeneObject sO, String bearer) throws Exception {
		
		String r_url =  new String("https://api.seene.co/seenes");
		SeeneToolkit.log("Requesting new Seene on " + r_url, LogLevel.info);
		
		HttpURLConnection conn = openOAuthRequestConnection(r_url, "POST", bearer);
		
		// adding output to the OAuthRequestConnection
	    conn.setDoOutput(true);
	    
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
	
	public Boolean uploadSeene(SeeneObject sO, String bearer, File uploadsLocalDir) throws Exception {
		
		Boolean success = false;
		
		if (bearer==null) return false;
		
		Map response = requestNewSeene(sO, bearer);
		String uuid = (String)response.get("identifier");
		
		sO.setIdentifier(uuid);
		SeeneToolkit.log("Response = New UUID:" + uuid, LogLevel.info);
		Map upload_url = (Map)response.get("upload_url");
		Map upload_fields = (Map)upload_url.get("fields");
		
		SeeneAWSUploadMeta awsMeta = new SeeneAWSUploadMeta();
		awsMeta.setUpload_url((String)upload_url.get("url"));
		awsMeta.setSignature((String)upload_fields.get("signature"));
		awsMeta.setAWSAccessKeyId((String)upload_fields.get("AWSAccessKeyId"));
		awsMeta.setAcl((String)upload_fields.get("acl"));
		awsMeta.setKey((String)upload_fields.get("key"));
		awsMeta.setPolicy((String)upload_fields.get("policy"));
		sO.setAwsMeta(awsMeta);
		
		// Store Model and Texture in the upload folder and generate a XMP enhanced JPG for the upload
		String folderName = SeeneStorage.generateSeeneFolderName(sO, null);
		
		File savePath = new File(uploadsLocalDir.getAbsolutePath() + File.separator + folderName);
		savePath.mkdirs();
		
		File mFile = new File(savePath.getAbsoluteFile() + File.separator + STK.SEENE_MODEL);
		File pFile = new File(savePath.getAbsoluteFile() + File.separator + STK.SEENE_TEXTURE);
		sO.getModel().saveModelDataToFile(mFile);
		sO.getPoster().saveTextureToFile(pFile);
		Helper.createFolderIcon(savePath, null);
		
		SeeneToolkit.generateXMP(savePath.getAbsolutePath(),STK.CALCULATION_METHOD_GOOGLE_RANGELINEAR);
		
		File xFile = new File(savePath.getAbsolutePath() + File.separator + STK.XMP_COMBINED_JPG);
		
		// Upload the XMP to the AWS Server
		String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
		
		SeeneToolkit.log("Uploading Seene: " + uuid, LogLevel.info);
		
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
    	
    	writer.append("--" + boundary).append(STK.LINE_FEED);
    	writer.append("Content-Disposition: form-data; name=\"AWSAccessKeyId\"").append(STK.LINE_FEED);
    	writer.append(STK.LINE_FEED);
    	writer.append(awsMeta.getAWSAccessKeyId()).append(STK.LINE_FEED);
    	
    	writer.append("--" + boundary).append(STK.LINE_FEED);
    	writer.append("Content-Disposition: form-data; name=\"key\"").append(STK.LINE_FEED);
    	writer.append(STK.LINE_FEED);
    	writer.append(awsMeta.getKey()).append(STK.LINE_FEED);
    	
    	writer.append("--" + boundary).append(STK.LINE_FEED);
    	writer.append("Content-Disposition: form-data; name=\"policy\"").append(STK.LINE_FEED);
    	writer.append(STK.LINE_FEED);
    	writer.append(awsMeta.getPolicy()).append(STK.LINE_FEED);
    	
    	writer.append("--" + boundary).append(STK.LINE_FEED);
    	writer.append("Content-Disposition: form-data; name=\"signature\"").append(STK.LINE_FEED);
    	writer.append(STK.LINE_FEED);
    	writer.append(awsMeta.getSignature()).append(STK.LINE_FEED);
    	
    	writer.append("--" + boundary).append(STK.LINE_FEED);
    	writer.append("Content-Disposition: form-data; name=\"acl\"").append(STK.LINE_FEED);
    	writer.append(STK.LINE_FEED);
    	writer.append(awsMeta.getAcl()).append(STK.LINE_FEED);
    	
    	writer.append("--" + boundary).append(STK.LINE_FEED);
    	writer.append("Content-Disposition: form-data; name=\"Content-Type\"").append(STK.LINE_FEED);
    	writer.append(STK.LINE_FEED);
    	writer.append("image/jpeg").append(STK.LINE_FEED);
    	
    	writer.append("--" + boundary).append(STK.LINE_FEED);
    	//writer.println("Content-Disposition: form-data; name=\"file\"; filename=\"" + sO.getXMP_combined().getName() + "\"");
    	//writer.println("Content-Disposition: form-data; name=\"file\"");
    	writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"poster-gdepth.jpg\"").append(STK.LINE_FEED);
    	writer.append("Content-Transfer-Encoding: binary").append(STK.LINE_FEED);
    	writer.append(STK.LINE_FEED);
    	
    	writer.flush();
    	
    	FileInputStream inputStream = new FileInputStream(xFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        inputStream.close();
        
        //writer.println();
        writer.append(STK.LINE_FEED);
        writer.append("--" + boundary + "--").append(STK.LINE_FEED);
        writer.flush();
        writer.close();
        
        os.close();
  
        int responseCode = conn.getResponseCode();
        System.out.println("ResponseCode is : " + responseCode);
        
        if ((responseCode>199) && (responseCode<205)) success = true;
        
        // FINALIZING
        if (success) {
	     	URL patchURL = new URL("https://api.seene.co/seenes/" + uuid);
	     		
	     	SeeneToolkit.log("Upload complete! Finalizing new Seene " + uuid,LogLevel.info);
	     		
	     	// Unfortunately the HttpsURLConnection does not support method PATCH
	     	// so we use Apache HttpComponents instead (https://hc.apache.org/)
	     	CloseableHttpClient client = HttpClients.createDefault();
	     	RequestConfig proxyConfig = null;
	     	
	     	// Proxy configured?
	     	if ((proxyData!=null) && (proxyData.getHost().length()>0)) {
	     		HttpHost proxy = new HttpHost(proxyData.getHost(), proxyData.getPort(), "http");
	     		
	     		// Proxy with Authentication?
	     		if (proxyData.getUser().length()>0) {
	     			CredentialsProvider proxyCredsProvider = new BasicCredentialsProvider();
	     			proxyCredsProvider.setCredentials(
	     	                new AuthScope(proxyData.getHost(), proxyData.getPort()),
	     	                new UsernamePasswordCredentials(proxyData.getUser(), proxyData.getPass()));
	     			client = HttpClients.custom().setDefaultCredentialsProvider(proxyCredsProvider).build();
	     		}
	     		
	     		proxyConfig = RequestConfig.custom().setProxy(proxy).build();
	     		
	     	}
	     	
	     	HttpPatch hp = new HttpPatch(patchURL.toURI());
	     	if (proxyConfig!=null) hp.setConfig(proxyConfig);
	     	
	     	hp.setHeader("Authorization", "Bearer " + bearer);
	     	hp.setHeader("Accept", "application/vnd.seene.v1+json");
	   		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	   		nvps.add(new BasicNameValuePair("finalize", "true"));
	   		hp.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	   		client.execute(hp);
	   		
	   		SeeneToolkit.log("Finalized. Check your private seenes!",LogLevel.info);
        } // if (success)
   		
   		return success;
	}

	
	public Map requestBearerToken(String username, String password, String auth_or_refresh_code, Boolean isRefresh) throws Exception {
		
		//String sclient = getSeeneKeyFromRemoteServer(username, password, STK.API_CLIENT_KEY);
		String sclient = XOREncryption.xorIt(STK.API_CLIENT_PUBLIC_KEY);
		SeeneToolkit.log(sclient,LogLevel.debug);

		if (sclient.length() != 64) throw new Exception("Format exception! Invalid Key!");
		
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
	
	private Map requestNonOAuth(String method, URL url, Map<String,String> params) throws Exception {
		
		SeeneToolkit.log("REQUEST.url: " + url.toString(),LogLevel.debug);
		//if(params != null) SeeneToolkit.log("REQUEST.params: " + getQuery(params),LogLevel.debug);
		
		HttpURLConnection conn = proxyGate(url);
		
		conn.setReadTimeout(20000);
		conn.setConnectTimeout(15000);
	    conn.setRequestMethod(method);
	    
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
	
	public static Date parseISO8601(String s) throws ParseException {
		if (s == null) return null;

		s = s.replace("Z", "+00:00");
		try {
		    s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
		} catch (IndexOutOfBoundsException e) {
		    throw new ParseException("Invalid length", 0);
		}
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
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
	
	
	public List<SeeneObject> getSetByURLoldAPI(String surl) throws Exception {
		 
		if (surl.endsWith("/")) surl = surl.substring(0, surl.length()-1);
		String shortkey = surl.substring(surl.lastIndexOf('/') + 1);
		
		Map map = requestNonOAuth("GET", new URL(surl), null);
		
		//List<SeeneObject> result = new ArrayList<SeeneObject>();
		//result.add(createSeeneObjectFromMap(map));
		
		//return result;
		return null;
	}
	
	public List<SeeneObject> getPublicSeeneByURLoldAPI(String surl) throws Exception {
		 
		if (surl.endsWith("/")) surl = surl.substring(0, surl.length()-1);
		String shortkey = surl.substring(surl.lastIndexOf('/') + 1);
		
		Map map = requestNonOAuth("GET", new URL(String.format("https://seene.co/api/seene/-/scenes/%s", shortkey)), null);
		
		List<SeeneObject> result = new ArrayList<SeeneObject>();
		result.add(createSeeneObjectFromMap(map));
		
		return result;
	}
	
	public List<SeeneSet> getUsersSetList(String username) throws Exception {
			Map response = requestNonOAuth("GET", new URL(String.format("https://seene.co/api/seene/-/users/@%s/albums?count=200", username)), null);
			return createSetListFromResponse(response);
	}
	
	
	public SeeneSet getPublicSetInfoByURLoldAPI(String surl) throws Exception {
		if (surl.endsWith("/")) surl = surl.substring(0, surl.length()-1);
		String shortkey = surl.substring(surl.lastIndexOf('/') + 1);
		
		Map response = requestNonOAuth("GET", new URL(String.format("https://seene.co/api/seene/-/albums/_%s", shortkey)), null);
		
		return createSetObjectFromMap(response);
	}
	
	
	public List<SeeneObject> getPublicSetByURLoldAPI(String surl) throws Exception {
		if (surl.endsWith("/")) surl = surl.substring(0, surl.length()-1);
		String shortkey = surl.substring(surl.lastIndexOf('/') + 1);
		
		Map response = requestNonOAuth("GET", new URL(String.format("https://seene.co/api/seene/-/albums/_%s/scenes?count=500", shortkey)), null);

		return createSeeneListFromResponse(response,"scenes");
	}

	// /!\ Seene inconsitency: nodeIdentifier could be "seenes" (new API) or "scenes" (old API) 
	private static List<SeeneObject> createSeeneListFromResponse(Map map, String nodeIdentifier) throws Exception {
		List<SeeneObject> result = new ArrayList<SeeneObject>();

		List seenes = (List)map.get(nodeIdentifier);
		for (Object o : seenes)
			result.add(createSeeneObjectFromMap((Map)o));
		
		return result;
	}
	
	private static List<SeeneSet> createSetListFromResponse(Map map) throws Exception {
		List<SeeneSet> result = new ArrayList<SeeneSet>();

		List sets = (List)map.get("albums");
		for (Object o : sets)
			result.add(createSetObjectFromMap((Map)o));
		
		return result;
	}
	
	private static SeeneSet createSetObjectFromMap(Map j) throws Exception {
		SeeneSet st = new SeeneSet();
		st.setTitle(EmojiParser.parseToAliases((String)j.get("title")));
		String descr = (String)j.get("description");
		if(descr == null) descr = ""; 
		st.setDescription(EmojiParser.parseToAliases(descr));
		st.setIdentifier((Long)j.get("id"));
		st.setShortCode((String)j.get("short_code"));
		st.setCount((Long)j.get("scenes_count"));
		Map u = (Map)j.get("user");
		st.setUserid((String)u.get("username"));
		
		return st;
	}
	
	private static SeeneObject createSeeneObjectFromMap(Map j) throws Exception {
		SeeneObject s = new SeeneObject();
		s.setCaptured_at(parseISO8601((String)j.get("captured_at")));
		String caption = (String)j.get("caption");
		if(caption == null) caption = ""; 
		s.setCaption(EmojiParser.parseToAliases(caption));
		s.setFilter_code((String)j.get("filter_code"));
		s.setShortCode((String)j.get("short_code"));
		s.setTextureURL(new URL((String)j.get("poster_url")));
		s.setModelURL(new URL((String)j.get("model_url")));
		Map u = (Map)j.get("user");
		s.setUserinfo((String)u.get("username"));
		String avatar = (String)u.get("avatar_url");
		if (avatar == null) {
			s.setAvatarURL(new URL(STK.DEFAULT_AVATAR));
		} else {
			s.setAvatarURL(new URL(avatar));
		}
			
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
