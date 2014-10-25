package de.nsvb.taglauncher.action;

import android.content.Context;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import de.nsvb.taglauncher.R;

public class RingerVibrate extends Action {
	
	public RingerVibrate() {
		mImageResource = R.drawable.perm_group_audio_settings;
		mMessage.add(ActionID.RINGER_VIBRATE);
	}

	@Override
	public boolean execute(Context ctx) {
		AudioManager am = (AudioManager) ctx
				.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		return true;
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_ringer_vibrate);
	}

	// Parcel functions

	public static final Creator<RingerVibrate> CREATOR = new Creator<RingerVibrate>() {
		public RingerVibrate createFromParcel(Parcel in) {
			return new RingerVibrate(in);
		}

		public RingerVibrate[] newArray(int size) {
			return new RingerVibrate[size];
		}
	};

	private RingerVibrate(Parcel in) {
		this();
	}

}
