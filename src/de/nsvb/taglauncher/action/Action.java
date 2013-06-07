package de.nsvb.taglauncher.action;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class Action implements Comparable<Action>, Cloneable,
		Parcelable {
	protected int mImageResource;
	protected boolean mExtended = false;
	protected List<Byte> mMessage = new ArrayList<Byte>();

	public abstract boolean execute(Context ctx);

	public abstract String getDescription(Context ctx);

	public int getImage() {
		return mImageResource;
	}

	public List<Byte> getMessage() {
		return mMessage;
	}

	public boolean isExtended() {
		return mExtended;
	}

	@Override
	public int compareTo(Action another) {
		return this.getClass().getName()
				.compareTo(another.getClass().getName());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Class<? extends Action> classAction = this.getClass();
		Action newA;
		try {
			newA = classAction.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new CloneNotSupportedException();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new CloneNotSupportedException();
		}
		return newA;
	}

	// Parcel functions

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
	}
}
