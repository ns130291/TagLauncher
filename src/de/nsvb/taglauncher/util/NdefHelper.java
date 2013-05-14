package de.nsvb.taglauncher.util;

import java.nio.charset.Charset;
import java.util.Locale;

import android.nfc.NdefRecord;

public class NdefHelper {
	@Deprecated
	public static NdefRecord createExternal(String domain, String type, byte[] data) {
		if (domain == null)
			throw new NullPointerException("domain is null");
		if (type == null)
			throw new NullPointerException("type is null");

		domain = domain.trim().toLowerCase(Locale.US);
		type = type.trim().toLowerCase(Locale.US);

		if (domain.length() == 0)
			throw new IllegalArgumentException("domain is empty");
		if (type.length() == 0)
			throw new IllegalArgumentException("type is empty");

		byte[] byteDomain = domain.getBytes(Charset.forName("UTF_8"));
		byte[] byteType = type.getBytes(Charset.forName("UTF_8"));
		byte[] b = new byte[byteDomain.length + 1 + byteType.length];
		System.arraycopy(byteDomain, 0, b, 0, byteDomain.length);
		b[byteDomain.length] = ':';
		System.arraycopy(byteType, 0, b, byteDomain.length + 1, byteType.length);

		return new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, b, new byte[0], data);
	}
}
