package de.nsvb.taglauncher.action;

import de.nsvb.taglauncher.R;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class BluetoothOff extends Action {
	
	public BluetoothOff() {
		mImageResource = R.drawable.perm_group_bluetooth;
		mMessage.add(ActionID.BLUETOOTH_OFF);
	}

	@Override
	public boolean execute(Context ctx) {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.disable();
			return true;
		}else{
			return false;
		}
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_bluetooth_off);
	}

	// Parcel functions

	public static final Parcelable.Creator<BluetoothOff> CREATOR = new Parcelable.Creator<BluetoothOff>() {
		public BluetoothOff createFromParcel(Parcel in) {
			return new BluetoothOff(in);
		}

		public BluetoothOff[] newArray(int size) {
			return new BluetoothOff[size];
		}
	};

	private BluetoothOff(Parcel in) {
		this();
	}

}
