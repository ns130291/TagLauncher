package de.nsvb.taglauncher.action;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyEvent;
import de.nsvb.taglauncher.R;

public class MediaPause extends Action {

	public MediaPause() {
		mImageResource = R.drawable.ic_pause_white_24dp;
		mMessage.add(ActionID.MEDIA_PAUSE);
	}

	@Override
	public boolean execute(Context ctx) {
		
		Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
		downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
		ctx.sendOrderedBroadcast(downIntent, null);

		Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE);
		upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
		ctx.sendOrderedBroadcast(upIntent, null);
		
		return true;
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_media_pause);
	}

	// Parcel functions

	public static final Creator<MediaPause> CREATOR = new Creator<MediaPause>() {
		public MediaPause createFromParcel(Parcel in) {
			return new MediaPause(in);
		}

		public MediaPause[] newArray(int size) {
			return new MediaPause[size];
		}
	};

	private MediaPause(Parcel in) {
		this();
	}

}
