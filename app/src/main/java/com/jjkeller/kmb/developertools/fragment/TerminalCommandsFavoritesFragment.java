package com.jjkeller.kmb.developertools.fragment;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.adapter.TerminalCommandsAdapter;
import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.model.TerminalCommandModel;
import com.jjkeller.kmb.developertools.viewmodel.TerminalCommandsViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment used in the TerminalCommandsActivity TabLayout to display list of available commands and corresponding command description.
 */

public class TerminalCommandsFavoritesFragment extends Fragment {

	private TerminalCommandsViewModel mViewModel = new TerminalCommandsViewModel();

	private TerminalCommandsAdapter mAdapter;

	private RecyclerView mRecyclerCommands;
	private TextView mNoRecords;
	private LinearLayout mProgressContainer;

	public TerminalCommandsFavoritesFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_terminal_commands, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		mRecyclerCommands = (RecyclerView) getView().findViewById(R.id.listCommands);
		mRecyclerCommands.setLayoutManager(new LinearLayoutManager(getActivity()));

		// add separater line
		DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), LinearLayout.VERTICAL);
		int colorDivider = Services.Theme().getThemeAttribute(getActivity().getTheme(), android.R.attr.divider);
		Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.recycler_divider_drawable);
		divider.setColorFilter(colorDivider, PorterDuff.Mode.SRC_ATOP);
		itemDecorator.setDrawable(divider);
		mRecyclerCommands.addItemDecoration(itemDecorator);

		mProgressContainer = (LinearLayout) getView().findViewById(R.id.llProgressContainer);
		mNoRecords = (TextView) getView().findViewById(R.id.textNoRecords);

		subscribeToViewModel();
	}

	/**
	 * Bind the View and ViewModel.
	 */
	private void subscribeToViewModel() {
		mViewModel.getCommands(true)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::getCommandsComplete, this::handleError);
	}

	/**
	 * Successful Async fetch terminal available commands.
	 */
	private void getCommandsComplete(final List<TerminalCommandModel> results) {
		if (results.isEmpty()) {
			mNoRecords.setText(this.getText(R.string.no_favorite_commands_defined));
			mNoRecords.setVisibility(View.VISIBLE);
			mProgressContainer.setVisibility(View.GONE);
		}
		else {
			mProgressContainer.setVisibility(View.GONE);
		}

		mAdapter = new TerminalCommandsAdapter(getContext(), results, null, new TerminalCommandsAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				TerminalCommandModel model = mAdapter.getItem(position);

				Intent data = new Intent();
				data.setData(Uri.parse(model.getCommand()));
				getActivity().setResult(RESULT_OK, data);
				getActivity().finish();
			}
		});

		mRecyclerCommands.setAdapter(mAdapter);
		mRecyclerCommands.setVisibility(View.VISIBLE);
	}

	/**
	 * Handle generic Throwable error.
	 */
	private void handleError(@NonNull final Throwable e) {
		mNoRecords.setText(e.getMessage());
		mNoRecords.setVisibility(View.VISIBLE);
		mProgressContainer.setVisibility(View.GONE);
	}

	public void onFavoriteChanged(TerminalCommandModel model) {
		if (model.getIsFavorite()) {

			boolean found = false;

			for (int i = 0; i < mAdapter.getItemCount(); i++) {
				if (mAdapter.getItem(i).getId() > model.getId()) {
					mAdapter.addItemAt(i, model);
					found = true;
					break;
				}
			}

			if (!found) {
				mAdapter.addItem(model);
			}
		}
		else {

			for (int i = 0; i < mAdapter.getItemCount(); i++) {
				if (mAdapter.getItem(i).getId() == model.getId()) {
					mAdapter.removeItem(i);
					mAdapter.notifyItemRemoved(i);
					break;
				}
			}
		}

		int count = mAdapter.getItemCount();

		mNoRecords.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
		mRecyclerCommands.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
	}
}