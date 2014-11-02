package de.nsvb.taglauncher.action;

import de.nsvb.taglauncher.ActivityExecuteTag;
import de.nsvb.taglauncher.R;
import de.nsvb.taglauncher.util.Log;
import android.content.Context;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;

public class RingerNormal extends ExtendedAction {

	private int mVolume = 255;

	private SeekBar mSeekBar;

	public RingerNormal() {
		super();
		mImageResource = R.drawable.ic_ring_volume_white_24dp;
		mMessage.add(ActionID.RINGER_NORMAL);
		mMessage.add((byte) 255);
		mView = R.layout.ac_ringer_normal;
	}

	/**
	 * Volume between 0 and 254, 255 (0xFF) for no change in volume
	 * 
	 * @param volume
	 */
	public void setVolume(int volume) {
		mVolume = volume;
		if (mVolume < 0) {
			mVolume = mVolume + 256;
		}
		//Log.d("mVolume " + mVolume);
		if (mMessage.size() > 1) {
			mMessage.remove(1);
		}
		if (mVolume >= 0 && mVolume <= 254) {
			mMessage.add((byte) mVolume);
		} else {
			mMessage.add((byte) 255);
		}
	}

	public int getVolume() {
		return mVolume;
	}

	@Override
	public boolean execute(Context ctx) {
		AudioManager am = (AudioManager) ctx
				.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		if (mVolume >= 0 && mVolume <= 254) {
			int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_RING);
			double factor = maxVol / 254.0;
			int vol = (int) Math.round(factor * mVolume);
			am.setStreamVolume(AudioManager.STREAM_RING, vol, 0);
		}
		return true;
	}

	@Override
	public String getDescription(Context ctx) {
		if (mVolume >= 0 && mVolume <= 254) {
			double factor = 100 / 254.0;
			int vol = (int) Math.round(factor * mVolume);
			return String.format(ctx.getString(R.string.ac_ringer_normal), vol
					+ "%");
		} else {
			return String.format(ctx.getString(R.string.ac_ringer_normal), "");
		}
	}

	@Override
	public ExtendedAction saveUserInput(View v) {
		CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkPreviousVolume);
		if (!checkBox.isChecked()) {
			SeekBar seekBar = (SeekBar) v.findViewById(R.id.volume);
			setVolume(seekBar.getProgress());
		} else {
			setVolume(255);
		}
		return this;
	}

	@Override
	public void addInteractionToView(View v, Context ctx) {
		CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkPreviousVolume);
		mSeekBar = (SeekBar) v.findViewById(R.id.volume);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mSeekBar.setEnabled(!isChecked);
			}
		});
		if (mVolume >= 0 && mVolume <= 254) {
			mSeekBar.setProgress(mVolume);
		} else {
			mSeekBar.setEnabled(false);
			checkBox.setChecked(true);
		}
	}

    @Override
    public String getActivityDescription(Context ctx) {
        return String.format(ctx.getString(R.string.ac_ringer_normal), "");
    }

    @Override
	protected Object clone() throws CloneNotSupportedException {
		Class<? extends RingerNormal> classAction = this.getClass();
		RingerNormal newA;
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
		//Log.d("+RingerNormal+ init-message "
		//		+ ActivityExecuteTag.toHex(message) + " " + message.length);
		setVolume(message[0]);
		//Log.d("+RingerNormal+ after init " + mMessage.get(1) + " "
		//		+ mMessage.size());
	}

	// Parcel functions

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mVolume);
	}

	public static final Parcelable.Creator<RingerNormal> CREATOR = new Parcelable.Creator<RingerNormal>() {
		public RingerNormal createFromParcel(Parcel in) {
			return new RingerNormal(in);
		}

		public RingerNormal[] newArray(int size) {
			return new RingerNormal[size];
		}
	};

	private RingerNormal(Parcel in) {
		this();
		setVolume(in.readInt());
	}

}
