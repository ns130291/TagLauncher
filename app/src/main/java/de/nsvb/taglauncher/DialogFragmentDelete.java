package de.nsvb.taglauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

public class DialogFragmentDelete extends DialogFragment {
	public final static String NAME = "name";

	public interface DeleteDialogListener {
		public void onDeleteDialogPositiveClick();
	}

	private DeleteDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (DeleteDialogListener) activity;
		} catch (ClassCastException e) {
			try {
				mListener = (DeleteDialogListener) getTargetFragment();
			} catch (ClassCastException f) {
				// The fragment doesn't implement the interface, throw exception
				throw new ClassCastException(
						activity.toString()
								+ " must implement DeleteDialogListener (or its fragment)");
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				
		String name = getArguments().getString(NAME, "");
		String messageString = String.format(getResources().getString(R.string.delete_action_bundle_dialog), name);
		SpannableString message = new SpannableString(messageString);
		message.setSpan(new StyleSpan(Typeface.ITALIC), 0, name.length(), 0);
		message.setSpan(new TypefaceSpan("sans-serif-light"), 0, name.length(), 0);
		
		builder.setTitle(R.string.delete_action_bundle_q)
		.setMessage(message)
				.setPositiveButton(R.string.delete,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								mListener.onDeleteDialogPositiveClick();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}
}
