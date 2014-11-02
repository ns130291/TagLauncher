package de.nsvb.taglauncher.action;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.os.Parcelable;
import de.nsvb.taglauncher.R;

public class WlanOn extends Action {

	public WlanOn() {
		mImageResource = R.drawable.ic_network_wifi_white_24dp;
		mMessage.add(ActionID.WLAN_ON);
	}

	@Override
	public boolean execute(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager != null)
		{
			return wifiManager.setWifiEnabled(true);
		}else{
			return false;
		}
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_wlan_on);
	}

	// Parcel functions

	public static final Creator<WlanOn> CREATOR = new Creator<WlanOn>() {
		public WlanOn createFromParcel(Parcel in) {
			return new WlanOn(in);
		}

		public WlanOn[] newArray(int size) {
			return new WlanOn[size];
		}
	};

	private WlanOn(Parcel in) {
		this();
	}

}
