package com.zendesk;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ZendeskDialog {
	public static final String TAG = "Zendesk";
	private static final String TITLE_DEFAULT = "Title";
	private static final String DESCRIPTION_DEFAULT = "How may we help you? Please fill in details below, and we'll get back to you as soon as possible.";

	private static String title;
	private static String description;
	private static String url;

	private static Context context;
	private static View dialogView;
	private static Handler toastHandler;

	private static TextView descriptionTV;
	private static EditText descriptionET;
	private static TextView subjectTV;
	private static EditText subjectET;
	private static TextView emailTV;
	private static EditText emailET;

	private static AlertDialog aDialog;

	private ZendeskDialog(Context context) {
		ZendeskDialog.context = context;
		dialogView = createDialogView(context);
		aDialog = new AlertDialog.Builder(context).setTitle(TITLE_DEFAULT).setView(dialogView).create();
	}

	public static ZendeskDialog Builder(Context context) {
		return new ZendeskDialog(context);
	}

	public ZendeskDialog setTitle(String title) {
		ZendeskDialog.title = title;
		return new ZendeskDialog(ZendeskDialog.context);
	}

	public ZendeskDialog setDescription(String description) {
		ZendeskDialog.description = description;
		return new ZendeskDialog(ZendeskDialog.context);
	}

	public ZendeskDialog setUrl(String url) {
		ZendeskDialog.url = url;
		return new ZendeskDialog(ZendeskDialog.context);
	}

	public AlertDialog create() {
		// set Dialog Title
		if (ZendeskDialog.title != null)
			aDialog.setTitle(ZendeskDialog.title);
		else if (getMetaDataByKey("zendesk_title") != null)
			aDialog.setTitle(getMetaDataByKey("zendesk_title"));

		// set Dialog description
		descriptionTV.setText(DESCRIPTION_DEFAULT);
		if (ZendeskDialog.description != null)
			descriptionTV.setText(ZendeskDialog.description);
		else if (getMetaDataByKey("zendesk_description") != null)
			descriptionTV.setText(getMetaDataByKey("zendesk_description"));

		// set Dialog url
		if (ZendeskDialog.url == null)
			ZendeskDialog.url = getMetaDataByKey("zendesk_url");

		if (ZendeskDialog.url != null) {
			return aDialog;
		} else {
			Log.e(TAG, "Meta Data with value \"zendesk_url\" couldn't be found in AndroidManifext.xml");
			return null;
		}
	}

	private static String getMetaDataByKey(String key) {
		PackageManager manager = null;
		ApplicationInfo info = null;
		String valueByKey = "";
		try {
			manager = context.getPackageManager();
			info = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			valueByKey = info.metaData.getString(key);
		} catch (Exception e) {
			Log.e(TAG, "Error reading meta data from AndroidManifest.xml", e);
			return null;
		}
		return valueByKey;
	}

	static Runnable runnable = new Runnable() {
		public void run() {
			Message message = new Message();
			String text = descriptionET.getText().toString();
			String subject = subjectET.getText().toString();
			String email = emailET.getText().toString();

			// Submit query here
			try {
				String server = ZendeskDialog.url;
				String dir = "/requests/embedded/create.json";
				String reqDesc = "description=" + URLEncoder.encode(text, "UTF-8");
				String reqEmail = "email=" + URLEncoder.encode(email, "UTF-8");
				String reqSubject = "subject=" + URLEncoder.encode(subject, "UTF-8");

				String requestUrl = "http://" + server + dir + "?" + reqDesc + "&" + reqEmail + "&" + reqSubject + "&tag=dropbox";

				URL url = new URL(requestUrl);
				Log.d(TAG, "Sending Request " + url.toExternalForm());

				InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
				BufferedReader bufferReader = new BufferedReader(inputStreamReader, 8192);
				String line = "";
				while ((line = bufferReader.readLine()) != null) {
					Log.d(TAG, line);
				}

				message.getData().putString("submit", "successfully");
				toastHandler.sendMessage(message);

			} catch (Exception e) {
				message.getData().putString("submit", "failed");
				toastHandler.sendMessage(message);
				Log.e(TAG, "Error while, submit request", e);
			}
		}
	};

	private static View createDialogView(Context context) {
		LinearLayout llRoot = new LinearLayout(context);
		llRoot.setOrientation(LinearLayout.VERTICAL);
		llRoot.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		ScrollView sv = new ScrollView(context);
		sv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));

		LinearLayout llContent = new LinearLayout(context);
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		llContent.setPadding(10, 0, 10, 0);

		LinearLayout llTop = new LinearLayout(context);
		llTop.setOrientation(LinearLayout.VERTICAL);

		descriptionTV = new TextView(context);
		descriptionTV.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		descriptionTV.setTextColor(Color.WHITE);
		descriptionET = new EditText(context);
		descriptionET.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		descriptionET.setMinLines(2);
		descriptionET.setMaxLines(2);

		subjectTV = new TextView(context);
		subjectTV.setText("Subject:");
		subjectTV.setTextColor(Color.WHITE);
		subjectET = new EditText(context);
		subjectET.setSingleLine(true);

		emailTV = new TextView(context);
		emailTV.setText("E-Mail:");
		emailTV.setTextColor(Color.WHITE);
		emailET = new EditText(context);
		emailET.setSingleLine(true);

		LinearLayout llBottom = new LinearLayout(context);
		llBottom.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		TextView poweredByTV = new TextView(context);
		poweredByTV.setText("Powered By");
		poweredByTV.setPadding(0, 0, 10, 0);
		poweredByTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
		poweredByTV.setGravity(Gravity.CENTER_VERTICAL);

		ImageView poweredByIV = new ImageView(context);
		InputStream in = ZendeskDialog.class.getResourceAsStream("/com/zendesk/zendesk.png");
		Bitmap poweredBy = BitmapFactory.decodeStream(in);
		poweredByIV.setImageBitmap(poweredBy);

		LinearLayout llButton = new LinearLayout(context);
		llButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		llButton.setOrientation(LinearLayout.HORIZONTAL);
		llButton.setBackgroundColor(0xFFBDBDBD);
		llButton.setPadding(0, 4, 0, 0);

		Button submit = new Button(context);
		submit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
		submit.setText("Submit Query");
		submit.setId(DialogInterface.BUTTON1);
		submit.setOnClickListener(buttonListener);

		Button cancel = new Button(context);
		cancel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
		cancel.setText("Cancel");
		cancel.setId(DialogInterface.BUTTON2);
		cancel.setOnClickListener(buttonListener);

		llRoot.addView(sv);
		llRoot.addView(llButton);

		sv.addView(llContent);

		llContent.addView(llTop);
		llContent.addView(llBottom);

		llTop.addView(descriptionTV);
		llTop.addView(descriptionET);
		llTop.addView(subjectTV);
		llTop.addView(subjectET);
		llTop.addView(emailTV);
		llTop.addView(emailET);

		llBottom.addView(poweredByTV);
		llBottom.addView(poweredByIV);

		llButton.addView(submit);
		llButton.addView(cancel);
		return llRoot;
	}

	private static void resetDialogView() {
		descriptionET.setText("");
		subjectET.setText("");
		emailET.setText("");
		descriptionTV.setTextColor(Color.WHITE);
		subjectTV.setTextColor(Color.WHITE);
		emailTV.setTextColor(Color.WHITE);
	}

	private static View.OnClickListener buttonListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case DialogInterface.BUTTON1:
				if (descriptionET.length() != 0 && subjectET.length() != 0 && emailET.length() != 0) {
					toastHandler = new Handler() {
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							// notify to user here
							String message = msg.getData().getString("submit");
							if (message.equals("successfully"))
								message = "Your request has successfully been submitted";
							else
								message = "Your request couldn't be submitted, please try again";
							Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
						}
					};
					new Thread(runnable).start();
					aDialog.dismiss();
					resetDialogView();
				} else {
					if (descriptionET.length() == 0)
						descriptionTV.setTextColor(Color.RED);
					else
						descriptionTV.setTextColor(Color.WHITE);
					if (subjectET.length() == 0)
						subjectTV.setTextColor(Color.RED);
					else
						subjectTV.setTextColor(Color.WHITE);
					if (emailET.length() == 0)
						emailTV.setTextColor(Color.RED);
					else
						emailTV.setTextColor(Color.WHITE);

					Toast.makeText(context, "Please fill out all Fields", Toast.LENGTH_SHORT).show();
				}
				break;
			case DialogInterface.BUTTON2:
				aDialog.dismiss();
				resetDialogView();
				break;
			}
		}

	};
}