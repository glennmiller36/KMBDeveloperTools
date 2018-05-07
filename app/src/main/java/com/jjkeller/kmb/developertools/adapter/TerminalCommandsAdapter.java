package com.jjkeller.kmb.developertools.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.model.TerminalCommandModel;

import java.util.Collections;
import java.util.List;

/**
 * Adapter to display list of available commands and corresponding command description.
 */

public class TerminalCommandsAdapter extends RecyclerView.Adapter<TerminalCommandsAdapter.ViewHolder> {

	// parent activity will implement this method to respond to click events
	public interface OnFavoriteCheckedChangeListener {
		void isFavoriteOnCheckedChanged(View view, int position);
	}

	public interface OnItemClickListener {
		void onItemClick(int position);
	}

	private List<TerminalCommandModel> mData = Collections.emptyList();
	private LayoutInflater mInflater;
	private OnFavoriteCheckedChangeListener mIsFavoriteOnClickListener;
	private OnItemClickListener mOnRowClickListener;

	// data is passed into the constructor
	public TerminalCommandsAdapter(Context context, List<TerminalCommandModel> data, OnFavoriteCheckedChangeListener isFavoriteCheckedListener, OnItemClickListener itemClickListener) {
		this.mInflater = LayoutInflater.from(context);
		this.mData = data;
		this.mIsFavoriteOnClickListener = isFavoriteCheckedListener;
		mOnRowClickListener = itemClickListener;
	}

	// inflates the row layout from xml when needed
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = mInflater.inflate(R.layout.item_terminal_command, parent, false);
		return new ViewHolder(view);
	}

	// binds the data to the textview in each row
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		TerminalCommandModel model = mData.get(position);
		holder.mIsFavorite.setTag(model.getIsFavorite());
		holder.mIsFavorite.setChecked(model.getIsFavorite());
		holder.mCommand.setText(model.getCommand());
		holder.mCommandDescription.setText(model.getCommandDescription());
	}

	// total number of rows
	@Override
	public int getItemCount() {
		return mData.size();
	}


	// stores and recycles views as they are scrolled off screen
	public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
		public CheckBox mIsFavorite;
		public TextView mCommand;
		public TextView mCommandDescription;

		public ViewHolder(View itemView) {
			super(itemView);
			mIsFavorite = (CheckBox) itemView.findViewById(R.id.checkIsFavorite);

			// hide IsFavorite checkbox on the Favorites list
			if (mIsFavoriteOnClickListener == null) {
				mIsFavorite.setVisibility(View.GONE);
			}
			else {
				if (mIsFavorite != null) {
					mIsFavorite.setOnCheckedChangeListener(this);
				}
			}

			mCommand = (TextView) itemView.findViewById(R.id.tvCommand);
			mCommandDescription = (TextView) itemView.findViewById(R.id.tvDescription);

			itemView.setOnClickListener(this);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// don't propagate the CheckedChanged event if the Recycler caused the change
			if ((boolean) buttonView.getTag() == isChecked) {
				return;
			}

			if (mIsFavoriteOnClickListener != null) mIsFavoriteOnClickListener.isFavoriteOnCheckedChanged(buttonView, getAdapterPosition());
		}

		@Override
		public void onClick(View v) {
			if (mOnRowClickListener != null) mOnRowClickListener.onItemClick(getAdapterPosition());
		}
	}

	public List<TerminalCommandModel> getData() {
		return mData;
	}

	// convenience method for getting data at click position
	public TerminalCommandModel getItem(int index) {
		return mData.get(index);
	}

	public TerminalCommandModel removeItem(int index) {
		return mData.remove(index);
	}

	public void addItem(TerminalCommandModel model) {
		mData.add(model);
		notifyItemInserted(mData.size()-1);
	}

	public void addItemAt(int index, TerminalCommandModel model) {
		mData.add(index, model);
		notifyItemInserted(index);
	}
}