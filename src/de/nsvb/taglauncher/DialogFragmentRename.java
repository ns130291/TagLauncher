package de.nsvb.taglauncher;

import de.nsvb.taglauncher.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;

public class DialogFragmentRename extends DialogFragment {
	public final static String NAME = "name";

	public interface RenameDialogListener {
		public void onRenameDialogPositiveClick(String name);
	}

	RenameDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (RenameDialogListener) activity;
		} catch (ClassCastException e) {
			try {
				mListener = (RenameDialogListener) getTargetFragment();
			} catch (ClassCastException f) {
				// The fragment doesn't implement the interface, throw exception
				throw new ClassCastException(
						activity.toString()
								+ " must implement RenameDialogListener (or its fragment)");
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final EditText edit = new EditText(getActivity());

		String name = getArguments().getString(NAME, "");
		edit.setText(name);
		edit.setSelection(name.length());

		builder.setView(edit)
				.setTitle(R.string.rename_action_bundle)
				.setPositiveButton(R.string.rename,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								mListener.onRenameDialogPositiveClick(edit.getText()
										.toString());
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		Dialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		return dialog;
	}
}
