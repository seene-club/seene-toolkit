package org.seeneclub.toolkit;


public class SeeneSet {
	
	private String userid;
	private Long count;
	private Long identifier;
	private String short_code;
	private String title;
	private String description;
	
	SeeneSet() {
		userid = new String();
		count = new Long(0);
		identifier = new Long(0);
		short_code = new String();
		title = new String();
		description = new String();
	}
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public Long getIdentifier() {
		return identifier;
	}
	public void setIdentifier(Long identifier) {
		this.identifier = identifier;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getShortCode() {
		return short_code;
	}

	public void setShortCode(String short_code) {
		this.short_code = short_code;
	}

}
