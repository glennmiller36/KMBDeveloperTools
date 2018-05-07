package com.jjkeller.kmb.developertools.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.adapter.BluetoothPairedAdapter;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.viewmodel.BluetoothPairedViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.jjkeller.kmb.developertools.activity.BluetoothTerminalActivity.EXTRA_BLUETOOTHDEVICE_ADDRESS;


/**
 * Activity to display bluetooth devices that are currently paired.
 */
public class BluetoothPairedActivity extends AppCompatActivity implements BluetoothPairedAdapter.ItemClickListener {

	// Intent request codes
	private static final int REQUEST_ENABLE_BT = 1;

	private BluetoothPairedViewModel mViewModel = new BluetoothPairedViewModel();

	private BluetoothPairedAdapter mAdapter;

	private Switch mSwitchBluetooth;
	private RecyclerView mRecyclerPairedDevices;
	private TextView mNoRecords;
	private LinearLayout mProgressContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Services.Theme().getThemeResourceId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_paired);

		setupViews();

		if (BluetoothAdapter.getDefaultAdapter() == null) {
			// Phone does not support Bluetooth
			mNoRecords.setText(this.getText(R.string.device_does_not_support_bluetooth));
			mNoRecords.setVisibility(View.VISIBLE);
			mProgressContainer.setVisibility(View.GONE);
		}
		else {
			subscribeToViewModel();
		}
	}

	/**
	 * Define handles to the child views.
	 */
	private void setupViews() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		LinearLayout switchClickableContainer = (LinearLayout) findViewById(R.id.switchClickableContainer);
		switchClickableContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// open master Settings to enable/disable Bluetooth
				Intent intentOpenBluetoothSettings = new Intent();
				intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivityForResult(intentOpenBluetoothSettings, REQUEST_ENABLE_BT);
			}
		});

		mSwitchBluetooth = (Switch) findViewById(R.id.switchBluetooth);
		mSwitchBluetooth.setChecked(BluetoothAdapter.getDefaultAdapter().isEnabled());

		mRecyclerPairedDevices = (RecyclerView) findViewById(R.id.listPairedDevices);
		mRecyclerPairedDevices.setLayoutManager(new LinearLayoutManager(this));

		// add separater line
		DividerItemDecoration itemDecorator = new DividerItemDecoration(this, LinearLayout.VERTICAL);
		int colorDivider = Services.Theme().getThemeAttribute(getTheme(), android.R.attr.divider);
		Drawable divider = ContextCompat.getDrawable(this, R.drawable.recycler_divider_drawable);
		divider.setColorFilter(colorDivider, PorterDuff.Mode.SRC_ATOP);
		itemDecorator.setDrawable(divider);
		mRecyclerPairedDevices.addItemDecoration(itemDecorator);

		mProgressContainer = (LinearLayout) findViewById(R.id.llProgressContainer);
		mNoRecords = (TextView) findViewById(R.id.textNoRecords);
	}

	/**
	 * Bind the View and ViewModel.
	 */
	private void subscribeToViewModel() {
		mViewModel.getConnectedEldDevices()
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::getConnectedDevicesComplete, this::handleError);
	}

	/**
	 * Successful Async fetch bluetooth devices currently connected.
	 */
	private void getConnectedDevicesComplete(final List<BluetoothDevice> results) {
		if (results.isEmpty()) {
			mNoRecords.setText(this.getText(R.string.no_paired_eld));
			mNoRecords.setVisibility(View.VISIBLE);
			mProgressContainer.setVisibility(View.GONE);
		}
		else {
			mProgressContainer.setVisibility(View.GONE);

			mAdapter = new BluetoothPairedAdapter(this, results);
			mAdapter.setClickListener(this);
			mRecyclerPairedDevices.setAdapter(mAdapter);
			mRecyclerPairedDevices.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		mNoRecords.setText(e.getMessage());
		mNoRecords.setVisibility(View.VISIBLE);
		mProgressContainer.setVisibility(View.GONE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(View view, int position) {

		if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {

			BluetoothDevice device = mAdapter.getItem(position);

			Intent intent = new Intent(this, BluetoothTerminalActivity.class);
			intent.putExtra(EXTRA_BLUETOOTHDEVICE_ADDRESS, device.getAddress());

			startActivity(intent);
		}
		else {
			AlertDialog.Builder alert = new AlertDialog.Builder(BluetoothPairedActivity.this);
			alert.setMessage(getString(R.string.bluetooth_disabled_cant_select_paired_device));
			alert.setPositiveButton(R.string.ok, (dialog, which) -> { });
			alert.show();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				mSwitchBluetooth.setChecked(BluetoothAdapter.getDefaultAdapter().isEnabled());
		}
	}
}
