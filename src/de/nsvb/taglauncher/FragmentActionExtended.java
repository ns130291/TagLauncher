package de.nsvb.taglauncher;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ScrollView;
import de.nsvb.taglauncher.action.ActionBundle;
import de.nsvb.taglauncher.action.ActionID;
import de.nsvb.taglauncher.action.ExtendedAction;
import de.nsvb.taglauncher.util.Log;

public class FragmentActionExtended extends Fragment {
	public static final String ACTION_ID = "ai";
	public static final String OBJECT_MODE = "om";
	public static final String AB_ID = "ab";

	private ExtendedAction mAction;
	private boolean mObjectMode;
	private boolean mEditMode;
	private OnApplyListener mCallback;
	private ActionBundle mActionBundle;

	public interface OnApplyListener {
		public void onApply();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnApplyListener) activity;
			mEditMode = true;
		} catch (ClassCastException e) {
			mEditMode = false;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		Bundle args = getArguments();
		if (args != null) {
			mObjectMode = args.getBoolean(OBJECT_MODE);
			if (mObjectMode) {
				int abId = args.getInt(AB_ID);
				int actionId = args.getInt(ACTION_ID);
				mActionBundle = ActivityMain.mActionBundles.get(abId);
				mAction = (ExtendedAction) mActionBundle.getActionList().get(
						actionId);
			} else {
				byte actionId = args.getByte(ACTION_ID);
				mAction = (ExtendedAction) ActionID.getAction(actionId);
			}
			getActivity().getActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_action_extended, null);
		ScrollView actionContainer = (ScrollView) view
				.findViewById(R.id.ac_container);
		final View actionView = inflater.inflate(mAction.getView(), null);
		LayoutParams acViewParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		actionContainer.addView(actionView, acViewParams);

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

		mAction.addInteractionToView(actionView, getActivity());

		Button button = (Button) view.findViewById(R.id.ac_apply);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditMode) {
					mAction.saveUserInput(actionView);
					if (mObjectMode) {
						mActionBundle.notifyChange();
					}
					mCallback.onApply();
				} else {
					Intent returnIntent = new Intent();
					returnIntent.putExtra("action",
							mAction.saveUserInput(actionView));
					getActivity().setResult(Activity.RESULT_OK, returnIntent);
					getActivity().finish();
				}
			}
		});

		return view;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("up selected");
		switch (item.getItemId()) {
		case android.R.id.home:
			getFragmentManager().popBackStack();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().getActionBar().setDisplayShowTitleEnabled(true);
	}
}
