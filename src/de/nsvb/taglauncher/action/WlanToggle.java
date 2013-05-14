package de.nsvb.taglauncher.action;

import de.nsvb.taglauncher.R;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.os.Parcelable;

public class WlanToggle extends Action {
	
	public WlanToggle() {
		mImageResource = R.drawable.perm_group_network;
		mMessage.add(new Byte(ActionID.WLAN_TOGGLE));
	}

	@Override
	public boolean execute(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager != null)
		{
			return wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
		}else{
			return false;
		}
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_wlan_toggle);
	}

	// Parcel functions

	public static final Parcelable.Creator<WlanToggle> CREATOR = new Parcelable.Creator<WlanToggle>() {
		public WlanToggle createFromParcel(Parcel in) {
			return new WlanToggle(in);
		}

		public WlanToggle[] newArray(int size) {
			return new WlanToggle[size];
		}
	};

	private WlanToggle(Parcel in) {
		this();
	}

}
