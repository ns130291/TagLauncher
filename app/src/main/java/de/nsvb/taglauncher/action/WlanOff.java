package de.nsvb.taglauncher.action;

import de.nsvb.taglauncher.R;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.os.Parcelable;

public class WlanOff extends Action {
	
	public WlanOff() {
		mImageResource = R.drawable.perm_group_network;
		mMessage.add(ActionID.WLAN_OFF);
	}

	@Override
	public boolean execute(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager != null)
		{
			return wifiManager.setWifiEnabled(false);
		}else{
			return false;
		}
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_wlan_off);
	}

	// Parcel functions

	public static final Creator<WlanOff> CREATOR = new Creator<WlanOff>() {
		public WlanOff createFromParcel(Parcel in) {
			return new WlanOff(in);
		}

		public WlanOff[] newArray(int size) {
			return new WlanOff[size];
		}
	};

	private WlanOff(Parcel in) {
		this();
	}

}
