package org.seeneclub.domainvalues;

public class LogLevel {
	public static final String debug = "D";
	public static final String info = "I";
	public static final String warn = "W";
	public static final String error = "E";
	public static final String fatal = "F";
	
	public static String getLogLevelText(String logLevelKey) {
        switch (logLevelKey.toUpperCase()) {
            case "D":
                return "DEBUG";
            case "I":
            	return "INFO";
            case "W":
            	return "WARN";
            case "E":
            	return "ERROR";
            case "F":
            	return "FATAL";
        }
		return "ERROR: unknown loglevel key";
	}

}
