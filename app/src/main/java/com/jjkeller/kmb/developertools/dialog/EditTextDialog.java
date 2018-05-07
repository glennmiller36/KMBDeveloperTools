package com.jjkeller.kmb.developertools.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.manager.Services;

/**
 * Re-usable dialog to collect input Text (used for backup file name or Search text)
 */

public class EditTextDialog extends Dialog implements View.OnClickListener {

	private String mTitleText;
	private String mInitialText;
	private String mPositiveButtonText;

	private TextView mTitle;
	private Button mPositiveButton;
	private Button mNegativeButton;
	private EditText mEditText;
	private String mTextInput;

	public String getTextInput() { return mTextInput; }

	public EditTextDialog(Activity activity, String titleText, String initialText, String positiveButtonText) {
		super(activity, Services.Theme().getDialogThemeResourceId());

		mTitleText = titleText;
		mInitialText = initialText;
		mPositiveButtonText = positiveButtonText;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_edittext);

		mTitle = (TextView) findViewById(R.id.textTitle);
		mTitle.setText(mTitleText);

		mEditText = (EditText) findViewById(R.id.editText);
		mEditText.setText(mInitialText);

		// put the cursor at the end of the initial text
		if (!TextUtils.isEmpty(mInitialText)) {
			int textLength = mInitialText.length();
			mEditText.setSelection(textLength, textLength);
		}

		mPositiveButton = (Button) findViewById(R.id.buttonPositive);
		mPositiveButton.setText(mPositiveButtonText);
		mNegativeButton = (Button) findViewById(R.id.buttonNegative);

		mPositiveButton.setOnClickListener(this);
		mNegativeButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonPositive:
				mTextInput = mEditText.getText().toString().trim();
				if (!TextUtils.isEmpty(mTextInput)) {
					dismiss();
				}
				break;

			default:
				cancel();
				break;
		}
	}
}
