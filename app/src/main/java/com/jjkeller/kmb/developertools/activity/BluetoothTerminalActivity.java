package com.jjkeller.kmb.developertools.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.adapter.BluetoothTerminalAdapter;
import com.jjkeller.kmb.developertools.enumerator.TerminalCommandTypeEnum;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.model.BluetoothTerminalModel;
import com.jjkeller.kmb.developertools.service.BluetoothService;
import com.jjkeller.kmb.developertools.viewmodel.BluetoothTerminalViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.jjkeller.kmb.developertools.service.BluetoothService.COMMAND_MENU_HELP_ALL;
import static com.jjkeller.kmb.developertools.service.BluetoothService.MESSAGE_COMMAND;
import static com.jjkeller.kmb.developertools.service.BluetoothService.MESSAGE_STATE_CHANGE;
import static com.jjkeller.kmb.developertools.service.BluetoothService.STATE_CONNECTED;
import static com.jjkeller.kmb.developertools.service.BluetoothService.STATE_CONNECTION_FAILED;

/**
 * Activity to send/receive commands to Bluetooth connected ELD device.
 */
public class BluetoothTerminalActivity extends AppCompatActivity {

	public static final String EXTRA_BLUETOOTHDEVICE_ADDRESS = "bluetoothDeviceAddress";
	public static final String EXTRA_LIST_MESSAGES = "listMessages";

	public static final long COUNTDOWNTIMER_SECONDS = 30;

	public static final String APPNAME_JJKELLER = "com.jjkeller.kmb";
	public static final String APPNAME_JJKELLER_ALK = "com.jjkeller.kmb.alk";

	private static final int ACTIVITY_COMMANDS = 1;	// Intent request codes

	private BluetoothTerminalViewModel mViewModel = new BluetoothTerminalViewModel();

	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BroadcastReceiver mBluetoothReceiver;

	private BluetoothService mChatService = null;
	private BluetoothDevice mDevice = null;
	private List<BluetoothTerminalModel> mListTerminalMessages = new ArrayList<>();
	private BluetoothTerminalAdapter mRecyclerAdapter;
	private LinearLayoutManager mLinearLayoutManager;
	private boolean mOnFailureCheckIfKellerMobileIsRunning = true;
	private ProgressDialog mLoadingDialog;

	private ImageView mImageBluetoothSearching;
	private ImageView mImageBluetoothConnected;
	private ImageView mImageBluetoothDisabled;
	private TextView mDeviceName;
	private TextView mConnectionStatus;
	private TextView mIsKellerMobileRunningText;
	private LinearLayout mRetryContainer;
	private TextView mRetryTimerText;
	private RecyclerView mRecyclerMessages;
	private ImageView mRetryButton;
	private ImageButton mCommandMenuButton;
	private EditText mEditCommandMessage;
	private ImageButton mSendButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_terminal);

		if (savedInstanceState != null) {
			String messages = savedInstanceState.get(EXTRA_LIST_MESSAGES).toString();
			mListTerminalMessages = new Gson().fromJson(messages, new TypeToken<List<BluetoothTerminalModel>>(){}.getType());
		}

		setupViews();
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mBluetoothAdapter.isEnabled()) {
			// Initialize the BluetoothService to perform bluetooth connections
			if (mChatService == null) {
				mChatService = new BluetoothService(this, mHandler);
				mChatService.start();
			}

			// Attempt to connect to the device
			if (mChatService != null && mDevice != null && mChatService.getState() != STATE_CONNECTED) {
				mChatService.connect(mDevice);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		String messages = new Gson().toJson(mListTerminalMessages);
		outState.putString(EXTRA_LIST_MESSAGES, messages);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mRetryTimer.cancel();

		if (mChatService != null) {
			mChatService.stop();
		}

		if (mBluetoothReceiver != null) {
			unregisterReceiver(mBluetoothReceiver);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_bluetoothterminal, menu);

		// set icon color of menu
		Services.Theme().setMenuIconColor(menu, Services.Theme().getThemeAttribute(getTheme(), R.attr.colorAccent));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menuEmail:
				onClickMenuEmail();
				return true;
			case R.id.menuClear:
				onClickMenuClear();
				return true;
			case R.id.menuScrollTop:
				onClickMenuScrollToTop();
				return true;
			case R.id.menuScrollBottom:
				onClickMenuScrollToBottom();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// document orientation change so you know why Bluetooth is attempting to connect again
		mListTerminalMessages.add(new BluetoothTerminalModel(TerminalCommandTypeEnum.ORIENTATION, "", getText(R.string.orientation_change).toString()));
	}

	/**
	 * Define handles to the child views.
	 */
	private void setupViews() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mImageBluetoothSearching = (ImageView) findViewById(R.id.imageBluetoothSearching);
		mImageBluetoothConnected = (ImageView) findViewById(R.id.imageBluetoothConnected);
		mImageBluetoothDisabled = (ImageView) findViewById(R.id.imageBluetoothDisabled);
		mDeviceName = (TextView) findViewById(R.id.textDeviceName);
		mConnectionStatus = (TextView) findViewById(R.id.textConnectionStatus);
		mRetryContainer = (LinearLayout) findViewById(R.id.llRetryContainer);
		mRetryTimerText = (TextView) findViewById(R.id.textRetryTimer);

		mIsKellerMobileRunningText = (TextView) findViewById(R.id.textIsKellerMobileRunning);
		mIsKellerMobileRunningText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(BluetoothTerminalActivity.this);
				alert.setMessage(R.string.prompt_reset_bluetooth);
				alert.setPositiveButton(R.string.reset_bluetooth, (dialog, which) -> {
					// if KMB is currently running, it's probably Bluetooth connected to the ELD,
					// prompt user to disable and enable Bluetooth as sledgehammer approach to disconnect the device
					mBluetoothAdapter.disable();
					while(mBluetoothAdapter.isEnabled());
					mBluetoothAdapter.enable();
					while(!mBluetoothAdapter.isEnabled());

					// Attempt to connect to the device
					if (mChatService != null && mDevice != null) {
						mChatService.connect(mDevice);
					}
				});
				alert.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
				alert.show();
			}
		});

		mRecyclerMessages = (RecyclerView) findViewById(R.id.listMessages);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mRecyclerMessages.setLayoutManager(mLinearLayoutManager);

		mRecyclerAdapter = new BluetoothTerminalAdapter(this, mListTerminalMessages);
		mRecyclerMessages.setAdapter(mRecyclerAdapter);

		// scroll list to bottom of the list (most recent message
		mLinearLayoutManager.scrollToPosition(mListTerminalMessages.size() - 1);

		mRetryButton = (ImageView) findViewById(R.id.imageRetry);
		mRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Attempt to connect to the device
				if (mChatService != null && mDevice != null) {
					mChatService.connect(mDevice);
				}
			}
		});

		String address = getIntent().getExtras().getString(EXTRA_BLUETOOTHDEVICE_ADDRESS);
		mDevice = mBluetoothAdapter.getRemoteDevice(address);
		if (mDevice != null) {
			mDeviceName.setText(mDevice.getName());
		}

		BluetoothTerminalActivity self = this;

		mCommandMenuButton = (ImageButton) findViewById(R.id.btnMenu);
		mCommandMenuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// first time per app session, query ELD for list of all possible commands and store globally in the Manager
				if (Services.Bluetooth().getTerminalCommandAllList().isEmpty()) {

					mLoadingDialog = new ProgressDialog(self);
					mLoadingDialog.setMessage(getString(R.string.loading_help_all));
					mLoadingDialog.setCancelable(false);
					mLoadingDialog.show();

					BluetoothTerminalActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							new Thread(new Runnable() {
								@Override
								public void run() {
									mChatService.sendConsoleCommandToDevice(COMMAND_MENU_HELP_ALL);
								}
							}).start();
						}
					});
				}
				else {
					startTerminalCommandsActivity();
				}
			}
		});

		mEditCommandMessage = (EditText) findViewById(R.id.editCommand);

		mSendButton = (ImageButton) findViewById(R.id.btnSend);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String command = mEditCommandMessage.getText().toString().trim();
				if (command.length() == 0) {
					AlertDialog.Builder alert = new AlertDialog.Builder(BluetoothTerminalActivity.this);
					alert.setMessage(getString(R.string.command_message_required));
					alert.setPositiveButton(R.string.ok, (dialog, which) -> { });
					alert.show();
				}
				else if (mChatService != null) {
					mEditCommandMessage.setText(null);
					clearFocusAndDismissSIP();

					mChatService.sendConsoleCommandToDevice(command);
				}
			}
		});

		// register BroadcastReceiver to listen for Bluetooth disconnect
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

		mBluetoothReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device != null && mDevice != null && device.getAddress().equalsIgnoreCase(mDevice.getAddress())) {
					if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
						if (mChatService != null && mChatService.getState() == STATE_CONNECTED) {
							mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTION_FAILED, -1).sendToTarget();
						}
					}
				}
			}
		};

		// initially disable buttons until we successfully Connect
		enableUiBasedOnConnectivityStatus(false);

		registerReceiver(mBluetoothReceiver, filter);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ACTIVITY_COMMANDS:
				if(resultCode == Activity.RESULT_OK){
					Uri command = data.getData();
					mEditCommandMessage.setText(command.toString());
				}
		}
	}

	protected static final byte[] shortToByteArray(short value)
	{
		//NOTE:  Data on eobr is LITTLE_ENDIAN
		return new byte[] {
				(byte) value,
				(byte)(value >>> 8)};
	}

	/**
	 * Update UI during Connecting... state
	 */
	private void onBluetoothConnecting() {
		mImageBluetoothConnected.setVisibility(View.GONE);
		mImageBluetoothDisabled.setVisibility(View.GONE);
		mImageBluetoothSearching.setVisibility(View.VISIBLE);

		mConnectionStatus.setText(getText(R.string.connecting));
		mIsKellerMobileRunningText.setVisibility(View.GONE);

		mRetryContainer.setVisibility(View.INVISIBLE);
		mRetryTimerText.setText("");
		mRetryTimer.cancel();

		enableUiBasedOnConnectivityStatus(false);

		mListTerminalMessages.add(new BluetoothTerminalModel(TerminalCommandTypeEnum.BLUETOOTH, "", getText(R.string.connecting).toString()));
		mRecyclerAdapter.notifyDataSetChanged();

		// scroll list to bottom of the list (most recent message
		mLinearLayoutManager.scrollToPosition(mListTerminalMessages.size() - 1);
	}

	/**
	 * Update UI when Connected state
	 */
	private void onBluetoothConnected() {
		mImageBluetoothSearching.setVisibility(View.GONE);
		mImageBluetoothDisabled.setVisibility(View.GONE);
		mImageBluetoothConnected.setVisibility(View.VISIBLE);

		mConnectionStatus.setText(getText(R.string.connected));

		mIsKellerMobileRunningText.setVisibility(View.GONE);
		mOnFailureCheckIfKellerMobileIsRunning = false;

		mRetryContainer.setVisibility(View.INVISIBLE);
		mRetryTimerText.setText("");
		mRetryTimer.cancel();

		enableUiBasedOnConnectivityStatus(true);

		mListTerminalMessages.add(new BluetoothTerminalModel(TerminalCommandTypeEnum.BLUETOOTH, "", getText(R.string.connected).toString()));
		mRecyclerAdapter.notifyDataSetChanged();

		// scroll list to bottom of the list (most recent message
		mLinearLayoutManager.scrollToPosition(mListTerminalMessages.size() - 1);
	}

	/**
	 * Update UI when Command is processed
	 */
	private void onCommandResults(BluetoothTerminalModel commandModel) {
		// special command issued to get all help commands
		if (commandModel.getCommand().equalsIgnoreCase(COMMAND_MENU_HELP_ALL)) {
			// don't add command to console output but update Manager list
			Services.Bluetooth().parseTerminalCommandList(commandModel.getData());

			if (mLoadingDialog != null) {
				mLoadingDialog.dismiss();
			}

			startTerminalCommandsActivity();
		}
		else {
			mListTerminalMessages.add(commandModel);
			mRecyclerAdapter.notifyDataSetChanged();

			// scroll list to bottom of the list (most recent message
			mLinearLayoutManager.scrollToPosition(mListTerminalMessages.size() - 1);
		}
	}

	/**
	 * Update UI when Failure state
	 */
	private void onBluetoothFailed(String exceptionMessage) {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}

		mImageBluetoothSearching.setVisibility(View.GONE);
		mImageBluetoothConnected.setVisibility(View.GONE);
		mImageBluetoothDisabled.setVisibility(View.VISIBLE);

		mConnectionStatus.setText(getText(R.string.not_connected));

		if (mOnFailureCheckIfKellerMobileIsRunning && isKellerMobileAppRunning()) {
			mIsKellerMobileRunningText.setVisibility(View.VISIBLE);
		}
		else {
			mIsKellerMobileRunningText.setVisibility(View.GONE);
		}
		mOnFailureCheckIfKellerMobileIsRunning = false;

		mRetryContainer.setVisibility(View.VISIBLE);
		mRetryTimerText.setText("");
		mRetryTimer.cancel();

		enableUiBasedOnConnectivityStatus(false);

		mListTerminalMessages.add(new BluetoothTerminalModel(TerminalCommandTypeEnum.BLUETOOTH, "", getText(R.string.not_connected).toString(), exceptionMessage));
		mRecyclerAdapter.notifyDataSetChanged();

		// scroll list to bottom of the list (most recent message
		mLinearLayoutManager.scrollToPosition(mListTerminalMessages.size() - 1);

		// start timer to automatically retry
		mRetryTimer.start();
	}

	/**
	 * Update UI during service stop
	 */
	private void onBluetoothStop() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}

		mImageBluetoothSearching.setVisibility(View.GONE);
		mImageBluetoothConnected.setVisibility(View.GONE);
		mImageBluetoothDisabled.setVisibility(View.VISIBLE);

		mConnectionStatus.setText(getText(R.string.not_connected));
		mIsKellerMobileRunningText.setVisibility(View.GONE);
		mOnFailureCheckIfKellerMobileIsRunning = false;

		mRetryContainer.setVisibility(View.INVISIBLE);
		mRetryTimerText.setText("");
		mRetryTimer.cancel();

		enableUiBasedOnConnectivityStatus(false);

		mListTerminalMessages.add(new BluetoothTerminalModel(TerminalCommandTypeEnum.BLUETOOTH, "", getText(R.string.not_connected).toString()));
		mRecyclerAdapter.notifyDataSetChanged();

		// scroll list to bottom of the list (most recent message
		mLinearLayoutManager.scrollToPosition(mListTerminalMessages.size() - 1);
	}

	private void onClickMenuEmail() {

		if (mListTerminalMessages.isEmpty()) {
			AlertDialog.Builder alert = new AlertDialog.Builder(BluetoothTerminalActivity.this);
			alert.setMessage(getString(R.string.no_command_results_to_email));
			alert.setPositiveButton(R.string.ok, (dialog, which) -> { });
			alert.show();

			return;
		}

		// You must copy the file to the external directory (aka SD Card). It's because the email application cannot access your data directory
		// (in the same way that you can't access other app's data directory)
		mViewModel.copyTerminalFileToTempDirectory(mListTerminalMessages)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::copyTerminalFileToTempDirectoryComplete, this::handleError);
	}

	private void onClickMenuClear() {
		mListTerminalMessages.clear();
		mRecyclerAdapter.notifyDataSetChanged();
	}

	private void onClickMenuScrollToTop() {
		if (mListTerminalMessages.size() > 0) {
			mRecyclerMessages.scrollToPosition(0);
		}
	}

	private void onClickMenuScrollToBottom() {
		if (mListTerminalMessages.size() > 0) {
			mRecyclerMessages.scrollToPosition(mListTerminalMessages.size() - 1);
		}
	}

	/**
	 * Successful Async Copy of the Terminal Log to the Temp directory.
	 * Email Terminal Log as attachments
	 */
	private void copyTerminalFileToTempDirectoryComplete(final ArrayList<Uri> results) {

		final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.kmb_terminal_log));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Services.Device().getDeviceInfo());

		if (!results.isEmpty()) {
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, results);
		}

		startActivity(Intent.createChooser(emailIntent, getString(R.string.send_via)));
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.error).toUpperCase())
				.setMessage(e.getMessage())
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
	}

	/*----------------------------------------
	 * Helper methods
	 *----------------------------------------*/

	/**
	 * Clear focus from active control and dismiss the keyboard if showing.
	 */
	private void clearFocusAndDismissSIP() {
		View current = getCurrentFocus();
		if (current != null) {
			current.clearFocus();

			// hide keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
		}
	}

	/**
	 * Determine if KMB is currently running, it's probably Bluetooth
	 * connected to the ELD so we won't be able to connect ourselves.
	 */
	private boolean isKellerMobileAppRunning() {
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
		if (procInfos != null)
		{
			for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
				if (processInfo.processName.equals(APPNAME_JJKELLER) || processInfo.processName.equals(APPNAME_JJKELLER_ALK)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Apply Alpha value to ImageButton to make it look disabled
	 */
	private void enableUiBasedOnConnectivityStatus(boolean enabled) {
		mCommandMenuButton.setImageAlpha(enabled ? 0xFF : 0x3F);
		mEditCommandMessage.setEnabled(enabled);
		mSendButton.setImageAlpha(enabled ? 0xFF : 0x3F);
	}

	private void startTerminalCommandsActivity() {
		Intent intent = new Intent(getApplicationContext(), TerminalCommandsActivity.class);
		startActivityForResult(intent, ACTIVITY_COMMANDS);
	}

	/**
	 * The Handler that gets information back from the BluetoothService
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
						case BluetoothService.STATE_CONNECTING:
							onBluetoothConnecting();
							break;

						case BluetoothService.STATE_CONNECTED:
							onBluetoothConnected();
							break;

						case BluetoothService.STATE_CONNECTION_FAILED:
							onBluetoothFailed((String) msg.obj);
							break;

						case BluetoothService.STATE_STOP:
							onBluetoothStop();
							break;
					}
					break;

				case MESSAGE_COMMAND:
					onCommandResults((BluetoothTerminalModel) msg.obj);
					break;
			}
		}
	};

	/**
	 * CountDownTimer to count down when a Connection is dropped how long until it will automatically attempt to re-try connecting.
	 */
	private final CountDownTimer mRetryTimer = new CountDownTimer((COUNTDOWNTIMER_SECONDS * 1000), 1000) {

		public void onTick(long millisUntilFinished) {
			mRetryTimerText.setText(String.valueOf(millisUntilFinished / 1000));
		}

		public void onFinish() {
			mRetryTimerText.setText("0");
			if (mChatService != null) {
				// Attempt to re-connect to the device
				if (mChatService != null && mDevice != null) {
					mChatService.connect(mDevice);
				}
			}
		}
	};
}
