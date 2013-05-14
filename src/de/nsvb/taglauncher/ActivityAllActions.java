package de.nsvb.taglauncher;

import de.nsvb.taglauncher.FragmentAllActions.OnExtendedActionSelectedListener;
import de.nsvb.taglauncher.action.ExtendedAction;
import de.nsvb.taglauncher.R;
import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.view.Menu;

public class ActivityAllActions extends Activity implements OnExtendedActionSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_actions);
		
		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState != null) {
				return;
			}

			FragmentAllActions fragmentAllActions = new FragmentAllActions();
			fragmentAllActions.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, fragmentAllActions).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_all_actions, menu);
		return true;
	}

	@Override
	public void onExtendedActionSelected(ExtendedAction action) {
		FragmentActionExtended fragment = new FragmentActionExtended();

		Bundle args = new Bundle();
		args.putByte(FragmentActionExtended.ACTION_ID, action.getMessage().get(0));
		fragment.setArguments(args);

		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.replace(R.id.fragment_container,
				fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

}
