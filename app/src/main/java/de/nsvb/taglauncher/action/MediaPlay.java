package de.nsvb.taglauncher.action;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyEvent;
import de.nsvb.taglauncher.R;

public class MediaPlay extends Action {

	public MediaPlay() {
		mImageResource = R.drawable.action_play;
		mMessage.add(ActionID.MEDIA_PLAY);
	}

	@Override
	public boolean execute(Context ctx) {
		
		Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
		downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
		ctx.sendOrderedBroadcast(downIntent, null);

		Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY);
		upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
		ctx.sendOrderedBroadcast(upIntent, null);
		
		return true;
	}

	@Override
	public String getDescription(Context ctx) {
		return ctx.getString(R.string.ac_media_play);
	}

	// Parcel functions

	public static final Creator<MediaPlay> CREATOR = new Creator<MediaPlay>() {
		public MediaPlay createFromParcel(Parcel in) {
			return new MediaPlay(in);
		}

		public MediaPlay[] newArray(int size) {
			return new MediaPlay[size];
		}
	};

	private MediaPlay(Parcel in) {
		this();
	}

}
