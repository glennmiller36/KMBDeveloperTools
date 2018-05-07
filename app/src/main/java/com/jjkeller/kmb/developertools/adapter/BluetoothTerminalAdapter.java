package com.jjkeller.kmb.developertools.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.enumerator.TerminalCommandTypeEnum;
import com.jjkeller.kmb.developertools.model.BluetoothTerminalModel;

import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;

/**
 * Adapter to display list of Bluetooth terminal commands and results from ELD Device.
 */

public class BluetoothTerminalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final int TYPE_INFORMATION = 0;
	private final int TYPE_COMMANDRESULTS = 1;

	private List<BluetoothTerminalModel> mData = Collections.emptyList();

	// data is passed into the constructor
	public BluetoothTerminalAdapter(Context context, List<BluetoothTerminalModel> data) {
		this.mData = data;
	}

	// total number of rows
	@Override
	public int getItemCount() {
		return mData.size();
	}

	//Returns the view type of the item at position for the purposes of view recycling.
	@Override
	public int getItemViewType(int position) {

		if (mData.get(position).getType() == TerminalCommandTypeEnum.COMMAND) {
			return TYPE_COMMANDRESULTS;
		} else {
			return TYPE_INFORMATION;
		}
	}

	/**
	 * inflates the row layout from xml when needed
	 */
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

		RecyclerView.ViewHolder viewHolder;
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

		switch (viewType) {
			case TYPE_COMMANDRESULTS:
				View v1 = inflater.inflate(R.layout.item_bluetooth_terminal_command, viewGroup, false);
				viewHolder = new ViewHolderCommandResults(v1);
				break;
			default:
				View v = inflater.inflate(R.layout.item_bluetooth_terminal_information, viewGroup, false);
				viewHolder = new ViewHolderInformation(v);
				break;
		}
		return viewHolder;
	}

	/**
	 * This method internally calls onBindViewHolder(ViewHolder, int) to update the
	 * RecyclerView.ViewHolder contents with the item at the given position
	 * and also sets up some private fields to be used by RecyclerView.
	 */
	// binds the data to the textview in each row
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		BluetoothTerminalModel model = mData.get(position);

		switch (viewHolder.getItemViewType()) {
			case TYPE_COMMANDRESULTS:
				ViewHolderCommandResults vhCommand = (ViewHolderCommandResults) viewHolder;
				vhCommand.mDateTime.setText(model.getDateTimeFormatted());
				vhCommand.mCommand.setText(model.getCommand());
				vhCommand.mResults.setText(model.getData());
				break;
			default:
				ViewHolderInformation vhInfo = (ViewHolderInformation) viewHolder;
				vhInfo.mDateTime.setText(model.getDateTimeFormatted());
				vhInfo.mCommand.setText(model.getData());

				if (TextUtils.isEmpty(model.getErrorMessage())) {
					vhInfo.mWhyErrorButton.setVisibility(View.GONE);
				}
				else {
					vhInfo.mWhyErrorButton.setVisibility(View.VISIBLE);
				}

				vhInfo.mErrorMessage.setText(model.getErrorMessage());
				vhInfo.mErrorMessage.setVisibility(View.GONE);

				break;
		}
	}

	/**
	 * ViewHolder for Information like Bluetooth Connection events
	 */
	public class ViewHolderInformation extends RecyclerView.ViewHolder implements View.OnClickListener {
		public TextView mDateTime;
		public TextView mCommand;
		public TextView mWhyErrorButton;
		public TextView mErrorMessage;

		public ViewHolderInformation(View itemView) {
			super(itemView);
			mDateTime = (TextView) itemView.findViewById(R.id.tvDateTime);
			mCommand = (TextView) itemView.findViewById(R.id.tvCommand);
			mErrorMessage = (TextView) itemView.findViewById(R.id.tvErrorMessage);

			mWhyErrorButton = (TextView) itemView.findViewById(R.id.tvWhyError);
			if (mWhyErrorButton != null) {
				mWhyErrorButton.setOnClickListener(this);
			}
		}

		@Override
		public void onClick(final View view) {
			if (mErrorMessage.getVisibility() == View.VISIBLE) {
				mErrorMessage.setVisibility(GONE);
			}
			else {
				mErrorMessage.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * ViewHolder for Command Results from the Bluetooth device
	 */
	public class ViewHolderCommandResults extends RecyclerView.ViewHolder {
		public TextView mDateTime;
		public TextView mCommand;
		public TextView mResults;

		public ViewHolderCommandResults(View itemView) {
			super(itemView);
			mDateTime = (TextView) itemView.findViewById(R.id.tvDateTime);
			mCommand = (TextView) itemView.findViewById(R.id.tvCommand);
			mResults = (TextView) itemView.findViewById(R.id.tvResults);
		}
	}

	// convenience method for getting data at click position
	public BluetoothTerminalModel getItem(int id) {
		return mData.get(id);
	}
}