package com.jjkeller.kmb.developertools.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;

import java.util.Collections;
import java.util.List;

/**
 * Adapter to display list of Bluetooth Paired ELD Devices.
 */

public class BluetoothPairedAdapter extends RecyclerView.Adapter<BluetoothPairedAdapter.ViewHolder> {

	private List<BluetoothDevice> mData = Collections.emptyList();
	private LayoutInflater mInflater;
	private ItemClickListener mClickListener;

	// data is passed into the constructor
	public BluetoothPairedAdapter(Context context, List<BluetoothDevice> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mData = data;
	}

	// inflates the row layout from xml when needed
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = mInflater.inflate(R.layout.item_bluetooth_paired, parent, false);
		return new ViewHolder(view);
	}

	// binds the data to the textview in each row
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		BluetoothDevice btDevice = mData.get(position);
		holder.mDeviceName.setText(btDevice.getName());
	}

	// total number of rows
	@Override
	public int getItemCount() {
		return mData.size();
	}


	// stores and recycles views as they are scrolled off screen
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		public TextView mDeviceName;

		public ViewHolder(View itemView) {
			super(itemView);
			mDeviceName = (TextView) itemView.findViewById(R.id.tvDeviceName);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
		}
	}

	// convenience method for getting data at click position
	public BluetoothDevice getItem(int id) {
		return mData.get(id);
	}

	// allows clicks events to be caught
	public void setClickListener(ItemClickListener itemClickListener) {
		this.mClickListener = itemClickListener;
	}

	// parent activity will implement this method to respond to click events
	public interface ItemClickListener {
		void onItemClick(View view, int position);
	}
}