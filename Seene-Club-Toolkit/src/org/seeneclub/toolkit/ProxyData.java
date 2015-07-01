package org.seeneclub.toolkit;

public class ProxyData {
	private String host;
	private int port;
	private String user;
	private String pass;
	
	// Constructor
	public ProxyData(String host,int port, String user, String pass) {
		this.host=host;
		this.port=port;
		this.user=user;
		this.pass=pass;
	}
	
	public ProxyData(String host,int port) {
		this.host=host;
		this.port=port;
		this.user=null;
		this.pass=null;
	}

	// Getter and Setter
	public String getHost() {
		if (host==null) return "";
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public String getPortString() {
		if (port==0) return "";
		return Integer.toString(port);
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void setPort(String port) {
		if (port.length()==0) port="0";
		this.port = Integer.parseInt(port);
	}
	public String getUser() {
		if (user==null) return "";
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		if (pass==null) return "";
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}

}
