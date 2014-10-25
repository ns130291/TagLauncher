package de.nsvb.taglauncher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Store extends SQLiteOpenHelper {
	
	private static Store mInstance;
	private static Context mContext;

	private static final int DB_VERSION = 1;

	public static final String DB_NAME = "de.nsvb.taglauncher";
	public static final String DB_AB_TABLENAME = "action_bundle";
	public static final String DB_AB_NAME = "name";
	public static final String DB_AB_MESSAGE = "message";

	public Store(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/*try{
			db.execSQL("DROP TABLE " + DB_AB_TABLENAME + ";");
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		db.execSQL("CREATE TABLE " + DB_AB_TABLENAME
				+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + DB_AB_NAME
				+ " TEXT, " + DB_AB_MESSAGE + " BLOB);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public static Store instance(){
		if(mContext == null){
			return null;
		}
		if(mInstance == null){
			mInstance = new Store(mContext);
		}
		return mInstance;
	}
	
	public static void setContext(Context context){
		mContext = context;
	}

}
