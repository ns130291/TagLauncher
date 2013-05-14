package de.nsvb.taglauncher;

import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import de.nsvb.taglauncher.FragmentActionBundleDetails.FragmentActionBundleListener;
import de.nsvb.taglauncher.FragmentActionBundleList.OnActionBundleSelectedListener;
import de.nsvb.taglauncher.FragmentActionExtended.OnApplyListener;
import de.nsvb.taglauncher.action.ActionBundle;
import de.nsvb.taglauncher.db.Store;
import de.nsvb.taglauncher.util.Log;

public class ActivityMain extends Activity implements
		OnActionBundleSelectedListener, FragmentActionBundleListener,
		OnApplyListener {

	public static ArrayList<ActionBundle> mActionBundles = new ArrayList<ActionBundle>();
	private static final String ACTION_BUNDLE_LIST = "abl";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// TODO set to false
		Log.DEBUG = true;

		// Background von Activity entfernen um Overdraw zu vermeiden --> macht
		// animation kaputt
		// getWindow().setBackgroundDrawable(null);

		Store.setContext(getApplicationContext());

		if (savedInstanceState == null || mActionBundles.size() == 0) {
			loadFromDB();
			//Log.d("##-onCreate-## savedInstanceState == null || mActionBundles.size() == 0 " + mActionBundles.getClass().getName() + '@' + Integer.toHexString(mActionBundles.hashCode()) + " ßß");
		}

		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState != null) {
				return;
			}

			FragmentActionBundleList fragmentActionList = new FragmentActionBundleList();
			fragmentActionList.setArguments(getIntent().getExtras());
			getFragmentManager()
					.beginTransaction()
					.add(R.id.fragment_container, fragmentActionList,
							ACTION_BUNDLE_LIST).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mActionBundles.size() == 0) {
			loadFromDB();
			//Log.d("##-onResume-## mActionBundles.size() == 0 " + mActionBundles.getClass().getName() + '@' + Integer.toHexString(mActionBundles.hashCode()) + " ßß");
		}
	}

	private void loadFromDB() {
		SQLiteDatabase db = Store.instance().getReadableDatabase();
		Cursor cs = db.query(Store.DB_AB_TABLENAME, new String[] { "id",
				Store.DB_AB_NAME, Store.DB_AB_MESSAGE }, null, null, null,
				null, "id");

		cs.moveToFirst();
		while (cs.isAfterLast() == false) {
			ActionBundle ab = new ActionBundle(getApplicationContext());
			ab.setName(cs.getString(cs.getColumnIndex(Store.DB_AB_NAME)));
			ab.init(cs.getBlob(cs.getColumnIndex(Store.DB_AB_MESSAGE)));
			ab.setId((int) cs.getLong(cs.getColumnIndex("id")));
			mActionBundles.add(ab);
			cs.moveToNext();
		}
		//Log.d("## " + mActionBundles.getClass().getName() + '@' + Integer.toHexString(mActionBundles.hashCode()) + " ßß");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onActionBundleSelected(int position) {
		FragmentActionBundleDetails fragmentActionDetails = new FragmentActionBundleDetails();

		Bundle args = new Bundle();
		args.putInt(FragmentActionBundleDetails.ARG_POSITION, position);
		fragmentActionDetails.setArguments(args);

		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.replace(R.id.fragment_container,
				fragmentActionDetails);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onDuplicate(int position) {
		getFragmentManager().popBackStackImmediate();
		FragmentActionBundleList frag = (FragmentActionBundleList) getFragmentManager()
				.findFragmentByTag(ACTION_BUNDLE_LIST);
		frag.setSelection(position);
	}

	@Override
	public void onActionSelected(int abPos, int actionPos) {
		FragmentActionExtended fragmentActionExtended = new FragmentActionExtended();

		Bundle args = new Bundle();
		args.putInt(FragmentActionExtended.AB_ID, abPos);
		args.putInt(FragmentActionExtended.ACTION_ID, actionPos);
		args.putBoolean(FragmentActionExtended.OBJECT_MODE, true);
		fragmentActionExtended.setArguments(args);

		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.replace(R.id.fragment_container,
				fragmentActionExtended);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onApply() {
		getFragmentManager().popBackStackImmediate();
	}

}
