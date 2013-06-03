package de.nsvb.taglauncher;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.nsvb.taglauncher.DialogFragmentDelete.DeleteDialogListener;
import de.nsvb.taglauncher.DialogFragmentRename.RenameDialogListener;
import de.nsvb.taglauncher.action.Action;
import de.nsvb.taglauncher.action.ActionBundle;
import de.nsvb.taglauncher.util.Log;

public class FragmentActionBundleList extends ListFragment implements
		RenameDialogListener, DeleteDialogListener {
	private OnActionBundleSelectedListener mCallback;
	private int mPosition;
	private ArrayAdapter<ActionBundle> mAdapter;
	private View mSelectedView;
	private boolean mNewActionBundle;

	private final static int PICK_ACTION_REQUEST = 12345;
    private final static int IMPORT_ACTION_BUNDLE_REQUEST = 827327;

	public interface OnActionBundleSelectedListener {
		public void onActionBundleSelected(int position);
        public void onActionBundleImported(int id);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnActionBundleSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnActionBundleSelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallback.onActionBundleSelected(position);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		//Log.d("##-onCreate-## Fragment " + ActivityMain.mActionBundles.getClass().getName() + '@' + Integer.toHexString(ActivityMain.mActionBundles.hashCode()) + " ");

		mAdapter = new ActionBundleListAdapter(getActivity()
				.getApplicationContext(), R.layout.list_item_card,
				ActivityMain.mActionBundles);

		setListAdapter(mAdapter);
	}

    @Override
    public void onResume() {
        getActivity().getActionBar().setTitle(R.string.app_name);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

        mAdapter.notifyDataSetChanged();

        super.onResume();
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_action_bundle_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.add_action_bundle:
			createNewActionBundle();
			return true;
        case R.id.test_tag:
            testTag();
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    private void testTag() {
        if(ActivityMain.noNFC){
            Toast.makeText(getActivity(), getString(R.string.no_nfc_support),
                    Toast.LENGTH_LONG).show();
        }else{
            startActivityForResult(new Intent(getActivity(), ActivityTagInfo.class), IMPORT_ACTION_BUNDLE_REQUEST);
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.fragment_action_bundle_list, null);
		registerForContextMenu(view.findViewById(android.R.id.list));

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

		Button newAb = (Button) view
				.findViewById(R.id.button_new_action_bundle);

		if (newAb != null) {
			newAb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					createNewActionBundle();
				}
			});
		}

		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(
				R.menu.fragment_action_bundle_list_2, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		mPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		mSelectedView = ((AdapterContextMenuInfo) item.getMenuInfo()).targetView;
		switch (item.getItemId()) {
		case R.id.rename_action_bundle:
			showRenameDialog();
			return true;
		case R.id.delete_action_bundle:
			deleteActionBundle();
			return true;
		case R.id.duplicate_action_bundle:
			duplicateActionBundle();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void duplicateActionBundle() {
		try {
			ActionBundle newAb = (ActionBundle) ActivityMain.mActionBundles
					.get(mPosition).clone();
			if (newAb != null) {
				ActivityMain.mActionBundles.add(newAb);
			}
		} catch (CloneNotSupportedException e) {
			Toast.makeText(getActivity(), getResources().getString(R.string.duplicate_error),
					Toast.LENGTH_SHORT).show();
		}
		mAdapter.notifyDataSetChanged();
        scrollToBottom();
	}

	public void showRenameDialog() {
		DialogFragmentRename dialog = new DialogFragmentRename();
		dialog.setTargetFragment(this, 0);
		Bundle args = new Bundle();
		args.putString(DialogFragmentRename.NAME, ActivityMain.mActionBundles
				.get(mPosition).getName());
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "DialogFragmentRename");
	}

	@Override
	public void onRenameDialogPositiveClick(String name) {
		ActivityMain.mActionBundles.get(mPosition).setName(name);
	}

	public void createNewActionBundle() {
		mNewActionBundle = true;
		startActivityForResult(new Intent(getActivity(),
				ActivityAllActions.class), PICK_ACTION_REQUEST);
	}

	public void addActionToActionBundle(int position) {
		mPosition = position;
		mNewActionBundle = false;
		startActivityForResult(new Intent(getActivity(),
				ActivityAllActions.class), PICK_ACTION_REQUEST);
	}

	public void deleteActionBundle() {
		DialogFragmentDelete dialog = new DialogFragmentDelete();
		dialog.setTargetFragment(this, 0);
		Bundle args = new Bundle();
		args.putString(DialogFragmentDelete.NAME, ActivityMain.mActionBundles
				.get(mPosition).getName());
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "DialogFragmentDelete");
	}

	@Override
	public void onDeleteDialogPositiveClick() {

		final ViewGroup.LayoutParams lp = mSelectedView.getLayoutParams();
		final int originalHeight = mSelectedView.getHeight();

		long animationTime = 300;

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1)
				.setDuration(animationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {

				// mCallback.onDismiss(mSelectedView, mToken);
				ActivityMain.mActionBundles.get(mPosition).delete();
				// ActivityMain.mActionBundles.remove(mPosition);
				mAdapter.remove(ActivityMain.mActionBundles.get(mPosition));
				mAdapter.notifyDataSetChanged();

				// Reset view presentation
				// mSelectedView.setAlpha(1f);
				// mSelectedView.setTranslationX(0);
				lp.height = originalHeight;
				mSelectedView.setLayoutParams(lp);
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				mSelectedView.setLayoutParams(lp);
			}
		});

		animator.start();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_ACTION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {

				Action temp = data.getParcelableExtra("action");
				if (temp != null) {
					ActionBundle ab;
					if (mNewActionBundle) {
						ab = new ActionBundle(getActivity());
					} else {
						ab = ActivityMain.mActionBundles.get(mPosition);
					}
					ab.addAction(temp);
					if (mNewActionBundle) {
						ActivityMain.mActionBundles.add(ab);
						mCallback
								.onActionBundleSelected(ActivityMain.mActionBundles
                                        .indexOf(ab));
					} else {
						mCallback.onActionBundleSelected(mPosition);
					}
				}
				// mAdapter.notifyDataSetChanged();
			}
		}
        if (requestCode == IMPORT_ACTION_BUNDLE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                mCallback.onActionBundleImported(data.getIntExtra(ActivityTagInfo.ACTION_BUNDLE_ID, -1));
                mAdapter.notifyDataSetChanged();
                scrollToBottom();
            }
        }
    }

    public void scrollToBottom() {
        getListView().setSelection(getListView().getAdapter().getCount());
    }

	private class ActionBundleListAdapter extends ArrayAdapter<ActionBundle> {

		private ArrayList<ActionBundle> mActionBundles;
		private LayoutInflater mLayoutInflater;

		public ActionBundleListAdapter(Context context, int textViewResourceId,
				ArrayList<ActionBundle> actionBundles) {
			super(context, textViewResourceId, actionBundles);
			mActionBundles = actionBundles;
			mLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int position, View v, ViewGroup parent) {

			LinearLayout container;

			v = mLayoutInflater.inflate(R.layout.list_item_card, null);
			container = (LinearLayout) v.findViewById(R.id.container);

			ActionBundle actionBundle = mActionBundles.get(position);
			if (actionBundle != null) {
				TextView heading = (TextView) v.findViewById(R.id.heading);
				TextView size = (TextView) v.findViewById(R.id.size);

				if (container.getChildCount() > 0) {
					container.removeAllViews();
				}
				if (actionBundle.getActionList().size() == 0) {
					View emptyView = mLayoutInflater.inflate(
							R.layout.list_item_action_empty, null);

					Button addAction = (Button) emptyView
							.findViewById(R.id.add_action);
					addAction.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							addActionToActionBundle(position);
						}
					});

					container.addView(emptyView);
				} else {
					for (Action action : actionBundle) {
						View actionView = mLayoutInflater.inflate(
								R.layout.list_item_action, null);

						ImageView actionImg = (ImageView) actionView
								.findViewById(R.id.actionImg);
						actionImg.setImageResource(action.getImage());

						TextView actionText = (TextView) actionView
								.findViewById(R.id.actionText);
						actionText.setText(action.getDescription(getContext()));

						container.addView(actionView);
					}
				}

				heading.setText(actionBundle.getName());
				Log.d("~~Size of current action bundle "
						+ actionBundle.getSize());

                size.setText(String.format(
                        getResources().getString(R.string.s_byte),
                        actionBundle.getSize())
                        + "");
			}

			return v;
		}
	}
}
