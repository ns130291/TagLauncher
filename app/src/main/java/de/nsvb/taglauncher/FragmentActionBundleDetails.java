package de.nsvb.taglauncher;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import de.nsvb.taglauncher.DialogFragmentDelete.DeleteDialogListener;
import de.nsvb.taglauncher.DialogFragmentRename.RenameDialogListener;
import de.nsvb.taglauncher.action.Action;
import de.nsvb.taglauncher.action.ActionBundle;

public class FragmentActionBundleDetails extends ListFragment implements
		RenameDialogListener, DeleteDialogListener {
	public final static String ARG_POSITION = "de.nsvb.taglauncher.position";
	private final static int PICK_ACTION_REQUEST = 1234;

	private int mPosition;
	private int mAPosition;
	private ArrayAdapter<Action> mAdapter;
	private FragmentActionBundleListener mCallback;
	private TextView mSize;
	private EditText mName;
    private ActionMode mActionMode;
    private Toolbar mActionBar;

	private DragSortListView mDslv;
	private DragSortController mController;

	private int dragStartMode = DragSortController.ON_DOWN;
	// public boolean removeEnabled = false;
	// public int removeMode = DragSortController.FLING_RIGHT_REMOVE;
    private boolean sortEnabled = true;
	private boolean dragEnabled = true;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            mActionBar.setVisibility(View.GONE); // TODO resolve workaround as <item name="windowActionModeOverlay">true</item> doesn't work
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.fragment_action_bundle_detail_cab, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.done:
                    ActivityMain.mActionBundles.get(mPosition).setName(mName.getText().toString());
                    actionMode.finish();
                    return true;
                default:
                    actionMode.finish();
                    return false;
            }

        }

        private void loseFocus() {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mName.getApplicationWindowToken(), 0);
            mName.clearFocus();
            mName.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP); // TODO remove when better method is available
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mName.setText(ActivityMain.mActionBundles.get(mPosition).getName());
            loseFocus();
            mActionBar.setVisibility(View.VISIBLE); // TODO resolve workaround as <item name="windowActionModeOverlay">true</item> doesn't work
        }
    };

	public interface FragmentActionBundleListener {
		public void onDuplicate(int position);

		public void onActionSelected(int abPos, int actionPos);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (FragmentActionBundleListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDuplicateChangeListener");
		}
	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				Action item = mAdapter.getItem(from);
				mAdapter.remove(item);
				mAdapter.insert(item, to);
				ActivityMain.mActionBundles.get(mPosition).store();
			}
		}
	};

	/**
	 * Called in onCreateView. Override this to provide a custom
	 * DragSortController.
	 */
	public DragSortController buildController(DragSortListView dslv) {
		// defaults are
		// dragStartMode = onDown
		// removeMode = flingRight
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		// controller.setClickRemoveId(R.id.click_remove);
		// controller.setRemoveEnabled(removeEnabled);
		controller.setSortEnabled(sortEnabled);
		controller.setDragInitMode(dragStartMode);
		// controller.setRemoveMode(removeMode);
		return controller;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mDslv = (DragSortListView) getListView();

		mController = buildController(mDslv);
		mDslv.setFloatViewManager(mController);
		mDslv.setOnTouchListener(mController);
		mDslv.setDragEnabled(dragEnabled);

		SimpleFloatViewManager sFVWM = new SimpleFloatViewManager(mDslv);
		sFVWM.setBackgroundColor(0xffe5e5e5);
		mDslv.setFloatViewManager(sFVWM);

		mDslv.setDropListener(onDrop);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_action_bundle_details,
				container, false);
		mDslv = (DragSortListView) view.findViewById(android.R.id.list);
		registerForContextMenu(mDslv);

		mName = (EditText) view.findViewById(R.id.heading);
        mName.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP); // TODO remove when better method is available
		mName.setText(ActivityMain.mActionBundles.get(mPosition).getName());
        mName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    ((ActivityMain) getActivity()).mActionMode = mActionMode;
                }else{
                    if(mActionMode != null) {
                        mActionMode.finish();
                        mActionMode = null;
                        ((ActivityMain) getActivity()).mActionMode = null;
                    }
                    mName.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP); // TODO remove when better method is available
                }
            }
        });


		mSize = (TextView) view.findViewById(R.id.size);
		setSize();

        View fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddActionClick();
            }
        });

        mActionBar = (Toolbar) view.findViewById(R.id.action_bar_toolbar);
        ((ActionBarActivity) getActivity()).setSupportActionBar(mActionBar);

		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(
				R.menu.fragment_action_bundle_detail_2, menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		Bundle args = getArguments();
		mPosition = args.getInt(ARG_POSITION);

		mAdapter = new ActionBundleAdapter(getActivity(),
				R.layout.list_item_action_drag,
				(ArrayList<Action>) ActivityMain.mActionBundles.get(mPosition)
						.getActionList());
		setListAdapter(mAdapter);

	}

    @Override
    public void onResume() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.details);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onResume();
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_ACTION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {

				Action temp = data.getParcelableExtra("action");
				if (temp != null) {
					ActivityMain.mActionBundles.get(mPosition).addAction(temp);
				}
				mAdapter.notifyDataSetChanged();
				setSize();
			}
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (ActivityMain.mActionBundles.get(mPosition).getActionList()
				.get(position).isExtended()) {
			mCallback.onActionSelected(mPosition, position);
		}
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_action:
                onAddActionClick();
                break;
            case R.id.rename_action_bundle:
                showRenameDialog();
                break;
            case R.id.duplicate_action_bundle:
                duplicateActionBundle();
                break;
            case R.id.delete_action_bundle:
                deleteActionBundle();
                break;
            case R.id.write_ab_to_tag:
                writeAbToTag();
                break;
            case R.id.execute_action_bundle:
                executeActionBundle();
                break;
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void executeActionBundle() {
        if(ActivityMain.mActionBundles.get(mPosition).execute()){
            Toast.makeText(getActivity(), R.string.tag_execute_success, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getActivity(), R.string.tag_execute_failure, Toast.LENGTH_LONG).show();
        }
    }

    private void writeAbToTag() {
        if (ActivityMain.noNFC) {
            Toast.makeText(getActivity(), getString(R.string.no_nfc_support),
                    Toast.LENGTH_LONG).show();
        } else {
            Intent writeMessage = new Intent(getActivity(),
                    ActivityWriteActionBundleToTag.class);
            writeMessage.putExtra(ActivityWriteActionBundleToTag.MESSAGE,
                    ActivityMain.mActionBundles.get(mPosition).getMessage());

            startActivity(writeMessage);
        }
    }

	private void deleteActionBundle() {
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
		ActivityMain.mActionBundles.get(mPosition).delete();
		ActivityMain.mActionBundles.remove(mPosition);
		getFragmentManager().popBackStackImmediate();
	}

	private void duplicateActionBundle() {
		try {
			ActionBundle newAb = (ActionBundle) ActivityMain.mActionBundles
					.get(mPosition).clone();
			if (newAb != null) {
				ActivityMain.mActionBundles.add(newAb);
				mCallback.onDuplicate(mPosition);
			}
		} catch (CloneNotSupportedException e) {
			Toast.makeText(getActivity(), getResources().getString(R.string.duplicate_error),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void onAddActionClick() {
		startActivityForResult(new Intent(getActivity(),
				ActivityAllActions.class), PICK_ACTION_REQUEST);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_action_bundle_detail, menu);
	}

	private void showRenameDialog() {
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

		mName.setText(name);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		mAPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		switch (item.getItemId()) {
		case R.id.delete_action:
			deleteAction();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void deleteAction() {
		ActivityMain.mActionBundles.get(mPosition).removeAction(mAPosition);
		mAdapter.notifyDataSetChanged();
		setSize();
	}

	private void setSize() {
		mSize.setText(String.format(getResources().getString(R.string.s_byte),
				ActivityMain.mActionBundles.get(mPosition).getSize() + ""));
	}

	private class ActionBundleAdapter extends ArrayAdapter<Action> {

		private ArrayList<Action> mActions;
		private LayoutInflater mLayoutInflater;

		public ActionBundleAdapter(Context context, int textViewResourceId,
				ArrayList<Action> actions) {
			super(context, textViewResourceId, actions);
			mActions = actions;
			mLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {

			if (v == null) {
				v = mLayoutInflater.inflate(R.layout.list_item_action_drag,
						null);
			}

			Action action = mActions.get(position);
			if (action != null) {
				TextView name = (TextView) v.findViewById(R.id.actionText);
				ImageView image = (ImageView) v.findViewById(R.id.actionImg);
                ImageView settings = (ImageView) v.findViewById(R.id.extended_settings);
				name.setText(action.getDescription(getContext()));
				image.setImageResource(action.getImage());
                if(action.isExtended()){
                    settings.setImageResource(R.drawable.ic_settings_black_24dp);
                }else{
                    settings.setImageResource(0);
                }
			}

			return v;
		}

	}

}
