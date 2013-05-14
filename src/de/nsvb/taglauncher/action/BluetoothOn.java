package de.nsvb.taglauncher.action;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import de.nsvb.taglauncher.R;

public class BluetoothOn extends Action {

	public BluetoothOn() {
		mImageResource = R.drawable.perm_group_bluetooth;
		mMessage.add(new Byte(ActionID.BLUETOOTH_ON));
	}

	@Override
	public boolean execute(Context ctx) {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.enable();
			return true;
		}else{
			return false;
		}
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_bluetooth_on);
	}

	// Parcel functions

	public static final Parcelable.Creator<BluetoothOn> CREATOR = new Parcelable.Creator<BluetoothOn>() {
		public BluetoothOn createFromParcel(Parcel in) {
			return new BluetoothOn(in);
		}

		public BluetoothOn[] newArray(int size) {
			return new BluetoothOn[size];
		}
	};

	private BluetoothOn(Parcel in) {
		this();
	}

}
