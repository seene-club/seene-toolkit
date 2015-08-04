package org.seeneclub.toolkit;

// Object for the AWS-Metadata needed for upload
public class SeeneAWS {

	private String access_key_id;
	private String bucket_name;
	private String model_dir;
	private String poster_dir;
	private String session_token;
	private String secret_access_key;
	
	public String getAccess_key_id() {
		return access_key_id;
	}
	public void setAccess_key_id(String access_key_id) {
		this.access_key_id = access_key_id;
	}
	public String getBucket_name() {
		return bucket_name;
	}
	public void setBucket_name(String bucket_name) {
		this.bucket_name = bucket_name;
	}
	public String getModel_dir() {
		return model_dir;
	}
	public void setModel_dir(String model_dir) {
		this.model_dir = model_dir;
	}
	public String getPoster_dir() {
		return poster_dir;
	}
	public void setPoster_dir(String poster_dir) {
		this.poster_dir = poster_dir;
	}
	public String getSession_token() {
		return session_token;
	}
	public void setSession_token(String session_token) {
		this.session_token = session_token;
	}
	public String getSecret_access_key() {
		return secret_access_key;
	}
	public void setSecret_access_key(String secret_access_key) {
		this.secret_access_key = secret_access_key;
	} 
}
