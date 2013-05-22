package de.nsvb.taglauncher.action;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import de.nsvb.taglauncher.ActivityExecuteTag;
import de.nsvb.taglauncher.R;
import de.nsvb.taglauncher.util.Log;

public class LaunchApp extends ExtendedActionVariableSize {

	private String mAppName;
	private String mAppPackageName;
	private String mClassName;
	
	private byte mDelimiter2;
	private ArrayList<ResolveInfo> mAppList;
	private PackageManager mPm;

	public LaunchApp() {
		super();
		mImageResource = R.drawable.app_icon;
		mMessage.add(new Byte(ActionID.LAUNCH_APP));
		mDelimiter = ';';
		mDelimiter2 = ':';
		mView = R.layout.ac_launch_app;
	}

	@Override
	public void init(Context ctx, byte[] message) {
		PackageManager pm = ctx.getPackageManager();
		if(mAppList == null){
			loadApps(ctx, pm);
		}		
		
		//Log.d(ActivityExecuteTag.toHex(message) + " blubbbbbbbbb");
		int pos = -1;
		for (int i = 0; i < message.length; i++) {
			if (message[i] == mDelimiter2) {
				pos = i;
				i = message.length;
			}
		}

		byte[] pName = new byte[pos];
		for (int i = 0; i < pName.length; i++) {
			pName[i] = message[i];
		}

		String packageName = null;
		try {
			packageName = new String(pName, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] cName = new byte[message.length - pos - 2];
		int j = 0;
		for (int i = pos + 1; i < (message.length - 1); i++) {
			cName[j] = message[i];
			j++;
		}

		String className = null;
		try {
			className = new String(cName, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (ResolveInfo resolveInfo : mAppList) {
			ComponentInfo ci = resolveInfo.activityInfo != null ? resolveInfo.activityInfo
					: resolveInfo.serviceInfo;
			if(ci.packageName.equals(packageName) && ci.name.equals(className)){
			 	mAppName = resolveInfo.loadLabel(pm)+"";
			 	break;
			}
		}

		//Log.d("pName " + packageName + " cName " + className +" "+className.length()+" "+cName.length);

		setPackageName(packageName, className);

	}

	@Override
	public boolean execute(Context ctx) {
		ComponentName cn = new ComponentName(mAppPackageName, mClassName);

		Intent start = new Intent(Intent.ACTION_MAIN, null);
		start.addCategory(Intent.CATEGORY_LAUNCHER);
		start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		start.setComponent(cn);

		try {
			ctx.startActivity(start);
		} catch (ActivityNotFoundException e) {
			//App nicht vorhanden, Play Store öffnen
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=" + mAppPackageName));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(intent);
		}

		return true;
	}

	@Override
	public String getDescription(Context ctx) {
		if (mAppName == null) {
			return String.format(ctx.getString(R.string.ac_launch_app), "App");
		} else {
			return String.format(ctx.getString(R.string.ac_launch_app),
					mAppName);
		}
	}
	
	private void loadApps(Context ctx, PackageManager pm){
        //long start = System.currentTimeMillis();
		Intent localIntent = new Intent("android.intent.action.MAIN", null);
		localIntent.addCategory("android.intent.category.LAUNCHER");
		mAppList = (ArrayList<ResolveInfo>) pm.queryIntentActivities(
				localIntent, 0);
        //Log.d("loadApps: "+(System.currentTimeMillis()-start)+" ms");
	}

	@Override
	public void addInteractionToView(View v, Context ctx) {
		mPm = ctx.getPackageManager();
		if(mAppList == null){
			loadApps(ctx, mPm);
            //start = System.currentTimeMillis();
            Collections.sort(mAppList, new ResolveInfo.DisplayNameComparator(mPm));
            //Log.d("loadApps sort: "+(System.currentTimeMillis()-start)+" ms");
		}

		Spinner spinner = (Spinner) v.findViewById(R.id.select_app);
		spinner.setAdapter(new PackageInfoAdapter(ctx, R.layout.spinner_item,
				mAppList, mPm));
		if (mAppPackageName != null && mClassName != null) {
			spinner.setSelection(findPackagePosition(mAppPackageName, mClassName));
		}
	}

	@Override
	public ExtendedAction saveUserInput(View v) {
		Spinner spinner = (Spinner) v.findViewById(R.id.select_app);
		ResolveInfo resolveInfo = mAppList.get(spinner
				.getSelectedItemPosition());
		mAppName = resolveInfo.loadLabel(mPm) + "";
		ComponentInfo ci = resolveInfo.activityInfo != null ? resolveInfo.activityInfo
				: resolveInfo.serviceInfo;
		setPackageName(ci.packageName, ci.name);
		mPm = null;
		return this;
	}

	private int findPackagePosition(String packageName, String className) {
		if (mAppList == null) {
			return 0;
		}
		int i = 0;
		for (ResolveInfo resolveInfo : mAppList) {
			ComponentInfo ci = resolveInfo.activityInfo != null ? resolveInfo.activityInfo
					: resolveInfo.serviceInfo;
			if (ci.packageName.equals(packageName) && ci.name.equals(className)) {
				return i;
			}
			i++;
		}
		return 0;
	}

	private void setPackageName(String packageName, String className) {
		mAppPackageName = packageName;
		mClassName = className;
		byte[] pName = packageName.getBytes(Charset.forName("US-ASCII"));
		byte[] cName = className.getBytes(Charset.forName("US-ASCII"));

		List<Byte> message = new ArrayList<Byte>();
		message.add(new Byte(ActionID.LAUNCH_APP));

		for (int i = 0; i < pName.length; i++) {
			message.add(new Byte(pName[i]));
		}

		message.add(new Byte(mDelimiter2));

		for (int i = 0; i < cName.length; i++) {
			message.add(new Byte(cName[i]));
		}

		message.add(new Byte(mDelimiter));

		mMessage = message;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Class<? extends LaunchApp> classAction = this.getClass();
		LaunchApp newA;
		try {
			newA = classAction.newInstance();
			newA.mAppName = mAppName;
			newA.setPackageName(mAppPackageName, mClassName);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new CloneNotSupportedException();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new CloneNotSupportedException();
		}
		return newA;
	}

	// ---------------

	private class PackageInfoAdapter extends ArrayAdapter<ResolveInfo> {

		private ArrayList<ResolveInfo> mList;
		private LayoutInflater mLayoutInflater;
		private int mTextViewResourceId;
		private PackageManager mPm;
		private HashMap<String, Drawable> mDrawables;

		public PackageInfoAdapter(Context context, int textViewResourceId,
				ArrayList<ResolveInfo> list, PackageManager pm) {
			super(context, textViewResourceId, list);
			mList = list;
			mLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mTextViewResourceId = textViewResourceId;
			mPm = pm;
			mDrawables = new HashMap<String, Drawable>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getDropDownView(position, convertView, parent);
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {

			View view;

			if (convertView == null) {
				view = mLayoutInflater.inflate(mTextViewResourceId, parent,
						false);
			} else {
				view = convertView;
			}

			ResolveInfo appInfo = mList.get(position);

			ImageView imageView = (ImageView) view.findViewById(R.id.app_image);
			TextView textView = (TextView) view.findViewById(R.id.app_name);

			if (mPm == null) {
				Log.d("null mPm");
			}

			if (mDrawables.containsKey(appInfo.hashCode() + "")) {
				imageView.setImageDrawable(mDrawables.get(appInfo.hashCode()
						+ ""));
			} else {
				Drawable drawable = appInfo.loadIcon(mPm);
				imageView.setImageDrawable(drawable);
				mDrawables.put(appInfo.hashCode() + "", drawable);
			}

			textView.setText(appInfo.loadLabel(mPm));

			return view;
		}

	}

	// Parcel functions

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mAppName);
		out.writeString(mAppPackageName);
		out.writeString(mClassName);
	}

	public static final Parcelable.Creator<LaunchApp> CREATOR = new Parcelable.Creator<LaunchApp>() {
		public LaunchApp createFromParcel(Parcel in) {
			return new LaunchApp(in);
		}

		public LaunchApp[] newArray(int size) {
			return new LaunchApp[size];
		}
	};

	private LaunchApp(Parcel in) {
		this();
		mAppName = in.readString();
		setPackageName(in.readString(), in.readString());
	}

}
