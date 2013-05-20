package de.nsvb.taglauncher.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Build;
import de.nsvb.taglauncher.ActivityExecuteTag;
import de.nsvb.taglauncher.R;
import de.nsvb.taglauncher.db.Store;
import de.nsvb.taglauncher.util.Log;
import de.nsvb.taglauncher.util.NdefHelper;

public class ActionBundle implements Iterable<Action>, Cloneable {
	private int mId;
	private int mSize;
	private String mName = "";
	private List<Action> mActions = new ArrayList<Action>();
	private Context mAppContext;
	private NdefMessage mNdefMessage;

	public ActionBundle(Context appContext) {
		this.mAppContext = appContext;
	}

	public void addAction(Action action) {
		mSize = 0;
		mNdefMessage = null;
		mActions.add(action);
		// TODO: in DB speichern
		SQLiteDatabase db = Store.instance().getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Store.DB_AB_MESSAGE, getMessageByte());

		if (mId == 0) {
			values.put(Store.DB_AB_NAME, mName);
			mId = (int) db.insert(Store.DB_AB_TABLENAME, null, values);
		} else {
			db.update(Store.DB_AB_TABLENAME, values, "id=" + mId, null);
		}
		Log.d("** Store in addAction()");

	}

	public void removeAction(int position) {
		mSize = 0;
		mNdefMessage = null;
		mActions.remove(position);
		// TODO: in DB speichern
		SQLiteDatabase db = Store.instance().getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Store.DB_AB_MESSAGE, getMessageByte());

		db.update(Store.DB_AB_TABLENAME, values, "id=" + mId, null);

		Log.d("** Store in removeAction()");
	}

	public void switchActions(int from, int to) {
		// TODO: in DB speichern
		SQLiteDatabase db = Store.instance().getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Store.DB_AB_MESSAGE, getMessageByte());

		db.update(Store.DB_AB_TABLENAME, values, "id=" + mId, null);

		Log.d("** Store in switchActions()");
	}

	public boolean execute() {
		boolean result = true;
		for (Action action : mActions) {
			if (!action.execute(mAppContext)) {
				result = false;
			}
		}
		return result;
	}

	public NdefMessage getMessage() {
		if (mNdefMessage == null) {
			generateMessage();
		}
		return mNdefMessage;
	}

	public void init(byte[] message) {
		Log.d("init-message " + ActivityExecuteTag.toHex(message));
		for (int i = 0; i < message.length; i++) {
			Action a = ActionID.getAction(message[i]);
			if (a != null) {
				if (a.isExtended()) {
					ExtendedAction ea = (ExtendedAction) a;
					if (ea.isVariableExtraSize()) {
						ExtendedActionVariableSize eavs = (ExtendedActionVariableSize) ea;
						byte delimiter = eavs.getDelimiter();
						int pos = -1;
						for (int j = i; j < message.length; j++) {
							if (message[j] == delimiter) {
								pos = j;
								j = message.length;
							}
						}
						if (pos >= 0) {
							byte[] extendedMessage = new byte[pos - i];
							for (int j = 0; j < extendedMessage.length
									&& (i + 1) < message.length; j++) {
								i++;
								extendedMessage[j] = message[i];
							}
							eavs.init(mAppContext, extendedMessage);
						}
					} else {
						byte[] extendedMessage = new byte[ea
								.getExtendedLength()];
						for (int j = 0; j < ea.getExtendedLength()
								&& (i + 1) < message.length; j++) {
							i++;
							extendedMessage[j] = message[i];
						}
						ea.init(mAppContext, extendedMessage);
					}
				}
				mActions.add(a);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getSize() {
		if (mSize == 0) {
			if (mNdefMessage == null) {
				generateMessage();
			}
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
				mSize = mNdefMessage.getByteArrayLength();
			} else {

				mSize = mNdefMessage.toByteArray().length;
			}
		}
		return mSize;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void generateMessage() {
		NdefRecord data;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			data = NdefRecord.createExternal("de.nsvb", "taglauncher",
					getMessageByte());
		} else {
			data = NdefHelper.createExternal("de.nsvb", "taglauncher",
					getMessageByte());
		}
		// TODO vermutlich weglassen, macht es einfacher die richtige Activity
		// zu starten
		// NdefRecord aar =
		// NdefRecord.createApplicationRecord("de.nsvb.writenfc");
		// NdefRecord records[] = new NdefRecord[2];
		NdefRecord records[] = new NdefRecord[1];
		// records[0] = aar;
		// records[1] = data;
		records[0] = data;
		mNdefMessage = new NdefMessage(records);
	}

	private byte[] getMessageByte() {

		List<Byte> recordMessage = new ArrayList<Byte>();
		for (Action action : mActions) {
			recordMessage.addAll(action.getMessage());
		}
		byte[] recordMessageByte = new byte[recordMessage.size()];
		for (int i = 0; i < recordMessage.size(); i++) {
			recordMessageByte[i] = recordMessage.get(i);
		}
		Log.d("getMessageByte() "
						+ ActivityExecuteTag.toHex(recordMessageByte));
		return recordMessageByte;
	}

	public String getName() {
		if (mName.isEmpty()) {
			return mAppContext.getString(R.string.action_bundle);
		} else {
			return mName;
		}
	}

	public void setName(String name) {
		this.mName = name;
		// TODO: in DB speichern
		SQLiteDatabase db = Store.instance().getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Store.DB_AB_NAME, mName);

		db.update(Store.DB_AB_TABLENAME, values, "id=" + mId, null);

		Log.d("** Store in setName()");
	}

	public void setId(int id) {
		mId = id;
	}

	public void delete() {
		// TODO: in DB speichern
		SQLiteDatabase db = Store.instance().getWritableDatabase();
		db.delete(Store.DB_AB_TABLENAME, "id=" + mId, null);

		Log.d("** Store in delete()");
	}

	public void notifyChange() {
		mSize = 0;
		mNdefMessage = null;

		// TODO: in DB speichern
		SQLiteDatabase db = Store.instance().getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Store.DB_AB_MESSAGE, getMessageByte());

		db.update(Store.DB_AB_TABLENAME, values, "id=" + mId, null);

		Log.d("** Store in notifyChange()");
	}

	public List<Action> getActionList() {
		return mActions;
	}

	@Override
	public Iterator<Action> iterator() {
		return mActions.iterator();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ActionBundle newAb = new ActionBundle(mAppContext);
		for (Action action : this) {
			newAb.addAction((Action) action.clone());
		}
		newAb.setName(String.format(mAppContext.getString(R.string.copy_string),
				this.getName()));
		return newAb;
	}
}
