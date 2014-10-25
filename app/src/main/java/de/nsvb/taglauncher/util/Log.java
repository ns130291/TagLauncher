package de.nsvb.taglauncher.util;

public class Log {
	public static final String TAG = "taglauncher";
	public static boolean DEBUG = false;

	public static int d(String msg) {
		if (DEBUG) {
			return android.util.Log.d(TAG, msg);
		}else{
			return 0;
		}
	}
}
