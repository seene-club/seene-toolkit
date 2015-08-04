package org.seeneclub.toolkit;

//Object for the AWS-Metadata needed for upload
public class SeeneAWSUploadMeta {
	
	private String upload_url;
	private String AWSAccessKeyId;
	private String key;
	private String policy;
	private String signature;
	private String acl;
	
	public String getUpload_url() {
		return upload_url;
	}
	public void setUpload_url(String upload_url) {
		this.upload_url = upload_url;
	}
	public String getAWSAccessKeyId() {
		return AWSAccessKeyId;
	}
	public void setAWSAccessKeyId(String aWSAccessKeyId) {
		AWSAccessKeyId = aWSAccessKeyId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getPolicy() {
		return policy;
	}
	public void setPolicy(String policy) {
		this.policy = policy;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getAcl() {
		return acl;
	}
	public void setAcl(String acl) {
		this.acl = acl;
	}

}
