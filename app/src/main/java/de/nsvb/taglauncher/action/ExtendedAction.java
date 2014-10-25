package de.nsvb.taglauncher.action;

import android.content.Context;
import android.view.View;

public abstract class ExtendedAction extends Action {
	protected int mView;
	protected boolean mVariableExtraSize = false;

	public ExtendedAction() {
		mExtended = true;
	}

	public int getView() {
		return mView;
	}

	public abstract ExtendedAction saveUserInput(View v);

	public void addInteractionToView(View v, Context ctx) {

	}

	public int getExtendedLength() {
		return 1;
	}
	
	public boolean isVariableExtraSize(){
		return mVariableExtraSize;
	}

	public abstract void init(Context ctx, byte[] message);

	@Override
	protected abstract Object clone() throws CloneNotSupportedException;
}
