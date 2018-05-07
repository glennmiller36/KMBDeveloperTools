package com.jjkeller.kmb.developertools.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.model.DatabaseBackupModel;

import java.util.List;

/**
 * Adapter to create views for the Backup List.
 */

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.ViewHolder>{

	public interface IBackupListAdapterListener {
		void deleteButtonOnClick(final int position);
		void restoreButtonOnClick(final int position);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView title;
		TextView lastModifiedDate;
		TextView size;
		LinearLayout deleteButton;
		LinearLayout restoreButton;
		IBackupListAdapterListener clickHandler;

		ViewHolder(View itemView) {
			super(itemView);
			title = (TextView)itemView.findViewById(R.id.tvTitle);
			lastModifiedDate = (TextView)itemView.findViewById(R.id.tvLastModified);
			size = (TextView)itemView.findViewById(R.id.tvSize);

			deleteButton = (LinearLayout)itemView.findViewById(R.id.buttonDelete);
			if (deleteButton != null)
				deleteButton.setOnClickListener(this);

			restoreButton = (LinearLayout)itemView.findViewById(R.id.buttonRestore);
			if (restoreButton != null)
				restoreButton.setOnClickListener(this);
		}

		@Override
		public void onClick(final View view) {
			if (clickHandler != null) {
				if (view.getId() == R.id.buttonDelete) {
					clickHandler.deleteButtonOnClick(getAdapterPosition());
				}
				else if (view.getId() == R.id.buttonRestore) {
					clickHandler.restoreButtonOnClick(getAdapterPosition());
				}
			}
		}
	}

	private IBackupListAdapterListener mOnClickListener;
	private List<DatabaseBackupModel> mBackupList;

	public BackupListAdapter(List<DatabaseBackupModel> backupList, IBackupListAdapterListener listener){
		this.mBackupList = backupList;
		mOnClickListener = listener;
	}

	@Override
	public int getItemCount() {
		return mBackupList.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item_backup, viewGroup, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {

		viewHolder.title.setText(mBackupList.get(position).getName());
		viewHolder.lastModifiedDate.setText(mBackupList.get(position).getFormattedLastModifiedDate());
		viewHolder.size.setText(mBackupList.get(position).getFormattedSize());
		viewHolder.clickHandler = this.mOnClickListener;
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	public DatabaseBackupModel getItem(final int index) {
		return mBackupList.get(index);
	}

	public void addItem(DatabaseBackupModel model){
		mBackupList.add(0, model);
	}

	public void removeItem(int position){
		mBackupList.remove(position);
	}
}