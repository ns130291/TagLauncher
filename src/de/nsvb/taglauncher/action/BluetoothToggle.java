package de.nsvb.taglauncher.action;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import de.nsvb.taglauncher.R;

public class BluetoothToggle extends Action {

	public BluetoothToggle() {
		mImageResource = R.drawable.perm_group_bluetooth;
		mMessage.add(new Byte(ActionID.BLUETOOTH_TOGGLE));
	}

	@Override
	public boolean execute(Context ctx) {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isEnabled()) {
				return mBluetoothAdapter.disable();
			} else {
				return mBluetoothAdapter.enable();
			}
		} else {
			return false;
		}
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_bluetooth_toggle);
	}

	// Parcel functions

	public static final Parcelable.Creator<BluetoothToggle> CREATOR = new Parcelable.Creator<BluetoothToggle>() {
		public BluetoothToggle createFromParcel(Parcel in) {
			return new BluetoothToggle(in);
		}

		public BluetoothToggle[] newArray(int size) {
			return new BluetoothToggle[size];
		}
	};

	private BluetoothToggle(Parcel in) {
		this();
	}

}
