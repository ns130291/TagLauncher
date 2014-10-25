package de.nsvb.taglauncher.action;

import android.content.Context;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import de.nsvb.taglauncher.R;

public class RingerSilent extends Action {
	
	public RingerSilent() {
		mImageResource = R.drawable.perm_group_audio_settings;
		mMessage.add(ActionID.RINGER_SILENT);
	}

	@Override
	public boolean execute(Context ctx) {
		AudioManager am = (AudioManager) ctx
				.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		return true;
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_ringer_silent);
	}

	// Parcel functions

	public static final Creator<RingerSilent> CREATOR = new Creator<RingerSilent>() {
		public RingerSilent createFromParcel(Parcel in) {
			return new RingerSilent(in);
		}

		public RingerSilent[] newArray(int size) {
			return new RingerSilent[size];
		}
	};

	private RingerSilent(Parcel in) {
		this();
	}

}
