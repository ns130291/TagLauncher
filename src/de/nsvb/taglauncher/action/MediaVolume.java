package de.nsvb.taglauncher.action;

import android.content.Context;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.SeekBar;
import de.nsvb.taglauncher.ActivityExecuteTag;
import de.nsvb.taglauncher.R;
import de.nsvb.taglauncher.util.Log;

public class MediaVolume extends ExtendedAction {

	private int mVolume = -1;

	public MediaVolume() {
		super();
		mImageResource = R.drawable.perm_group_audio_settings;
		mMessage.add(new Byte(ActionID.MEDIA_VOLUME));
		mMessage.add(new Byte((byte) 128));
		mView = R.layout.ac_media_volume;
	}

	/**
	 * Volume between 0 and 255 (0xFF)
	 * 
	 * @param volume
	 */
	public void setVolume(int volume) {
		mVolume = volume;
		if (mVolume < 0) {
			mVolume = mVolume + 256;
		}
		Log.d("mVolume " + mVolume);
		if (mMessage.size() > 1) {
			mMessage.remove(1);
		}
		if (mVolume >= 0 && mVolume <= 255) {
			mMessage.add(new Byte((byte) mVolume));
		}
	}

	public int getVolume() {
		return mVolume;
	}

	@Override
	public boolean execute(Context ctx) {
		AudioManager am = (AudioManager) ctx
				.getSystemService(Context.AUDIO_SERVICE);
		int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		double factor = maxVol / 255.0;
		int vol = (int) Math.round(factor * mVolume);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
		return true;
	}

	@Override
	public String getDescription(Context ctx) {
		if (mVolume == -1) {
			return String.format(ctx.getString(R.string.ac_media_volume), "");
		} else {
			double factor = 100 / 255.0;
			int vol = (int) Math.round(factor * mVolume);
			return String.format(ctx.getString(R.string.ac_media_volume), vol
					+ "%");
		}
	}

	@Override
	public ExtendedAction saveUserInput(View v) {
		SeekBar seekBar = (SeekBar) v.findViewById(R.id.volume);
		setVolume(seekBar.getProgress());
		return this;
	}

	@Override
	public void addInteractionToView(View v, Context ctx) {
		SeekBar seekBar = (SeekBar) v.findViewById(R.id.volume);
		if (mVolume == -1) {
			seekBar.setProgress(128);
		} else {
			seekBar.setProgress(mVolume);
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Class<? extends MediaVolume> classAction = this.getClass();
		MediaVolume newA;
		try {
			newA = classAction.newInstance();
			newA.setVolume(mVolume);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new CloneNotSupportedException();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new CloneNotSupportedException();
		}
		return newA;
	}

	@Override
	public void init(Context ctx, byte[] message) {
		Log.d("+MediaVolume+ init-message "
						+ ActivityExecuteTag.toHex(message) + " "
						+ message.length);
		setVolume(message[0]);
		Log.d("+MediaVolume+ after init "
						+ mMessage.get(1) + " "
						+ mMessage.size());
	}

	// Parcel functions

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mVolume);
	}

	public static final Parcelable.Creator<MediaVolume> CREATOR = new Parcelable.Creator<MediaVolume>() {
		public MediaVolume createFromParcel(Parcel in) {
			return new MediaVolume(in);
		}

		public MediaVolume[] newArray(int size) {
			return new MediaVolume[size];
		}
	};

	private MediaVolume(Parcel in) {
		this();
		setVolume(in.readInt());
	}

}
