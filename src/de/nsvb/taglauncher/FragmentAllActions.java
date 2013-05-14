package de.nsvb.taglauncher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.nsvb.taglauncher.action.Action;
import de.nsvb.taglauncher.action.ActionID;
import de.nsvb.taglauncher.action.ExtendedAction;
import de.nsvb.taglauncher.util.Log;

public class FragmentAllActions extends ListFragment {

	private static List<Action> mActionList;
	private OnExtendedActionSelectedListener mCallback;
	
	public interface OnExtendedActionSelectedListener{
		public void onExtendedActionSelected(ExtendedAction action);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnExtendedActionSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnExtendedActionSelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mActionList.get(position).isExtended()) {
			mCallback.onExtendedActionSelected((ExtendedAction) mActionList.get(position));
		} else {
			Intent returnIntent = new Intent();
			returnIntent.putExtra("action", mActionList.get(position));
			getActivity().setResult(Activity.RESULT_OK, returnIntent);
			getActivity().finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActivity().getActionBar().setTitle(R.string.title_activity_activity_all_actions);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		setHasOptionsMenu(true);
		
		mActionList = new ActionID().getActionList();

		setListAdapter(new ActionListAdapter(getActivity(),
				R.layout.list_item_action_all, (ArrayList<Action>) mActionList));
	}

	private class ActionListAdapter extends ArrayAdapter<Action> {

		private ArrayList<Action> mActions;
		private LayoutInflater mLayoutInflater;
		private int mTextViewResourceId;

		public ActionListAdapter(Context context, int textViewResourceId,
				ArrayList<Action> actions) {
			super(context, textViewResourceId, actions);
			mActions = actions;
			mLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mTextViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {

			if (v == null) {
				v = mLayoutInflater.inflate(mTextViewResourceId, null);
			}

			Action action = mActions.get(position);
			if (action != null) {
				TextView name = (TextView) v.findViewById(R.id.actionText);
				ImageView image = (ImageView) v.findViewById(R.id.actionImg);
				name.setText(action.getDescription(getContext()));
				image.setImageResource(action.getImage());
			}

			return v;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("up selected");
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent returnIntent = new Intent();
			getActivity().setResult(Activity.RESULT_CANCELED, returnIntent);
			getActivity().finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
