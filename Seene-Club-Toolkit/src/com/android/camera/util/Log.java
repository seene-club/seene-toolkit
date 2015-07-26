package com.android.camera.util;

import org.seeneclub.domainvalues.LogLevel;
import org.seeneclub.toolkit.SeeneToolkit;


public class Log {

	public static void d(String tag, String string) {
		SeeneToolkit.log(tag + ": " + string, LogLevel.debug);
	}

	public static void e(String tag, String string, Exception e) {
		SeeneToolkit.log(tag + ": " + string + "\n" + e.getStackTrace(), LogLevel.debug);
	}
}
