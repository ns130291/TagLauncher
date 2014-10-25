package de.nsvb.taglauncher.action;

public abstract class ExtendedActionVariableSize extends ExtendedAction {
	protected byte mDelimiter;
	
	public ExtendedActionVariableSize() {
		super();
		mVariableExtraSize = true;
	}
	
	public byte getDelimiter(){
		return mDelimiter;
	}
}
