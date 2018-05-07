package com.jjkeller.kmb.developertools.adapter;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;
import com.jjkeller.kmb.developertools.database.DataColumn;
import com.jjkeller.kmb.developertools.database.DataRow;
import com.jjkeller.kmb.developertools.database.DataSet;
import com.jjkeller.kmb.developertools.enumerator.DataTypeEnum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jjkeller.kmb.developertools.enumerator.DataTypeEnum.DATA_TYPE_BOOLEAN;
import static com.jjkeller.kmb.developertools.enumerator.DataTypeEnum.DATA_TYPE_DOUBLE;
import static com.jjkeller.kmb.developertools.enumerator.DataTypeEnum.DATA_TYPE_INTEGER;

/**
 * Adapter to dynamically display query results data in a table.
 *
 * (Unfortunately, couldn't use RecyclerView.Adapter because since the RecyclerView
 * sits inside a ScrollView so we can get both vertical and horizontal scrolling, it
 * would create a new ViewHolderInformation for each row making it take a long time to initially load)
 */
public class SqlQueryAdapter extends BaseAdapter
{
	public interface ISqlQueryAdapterListener {
		void selectedOnCheckedChanged(final int position, final boolean isChecked);
	}

	public static final int COLUMNN_WIDTH_NUMBER = 200;
	public static final int COLUMNN_WIDTH_STRING = 400;

	public static final int COLUMN_PADDING_BOTTOM = 15;
	public static final int COLUMN_PADDING_LEFT = 15;
	public static final int COLUMN_PADDING_RIGHT = 15;
	public static final int COLUMN_PADDING_TOP = 15;

	public static final String DATETIME_PATTERN = "yyyy-MM-dd hh:mm:ss a";
	public static final String DATE_PATTERN = "yyyy-MM-dd";
	public static final String TIME_PATTERN_12HOUR = "hh:mm:ss a";

	private DataSet mResults;
	private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat(DATETIME_PATTERN);
	private SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_PATTERN);

	public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnClickListener {
		private LinearLayout mRowContainer;
		private LinearLayout mIsSelectedContainer;
		private CheckBox mSelectedCheck;
		private List<View> mViews = new ArrayList<View>();
		ISqlQueryAdapterListener clickHandler;

		public ViewHolder(Context activityContext, View view, List<DataColumn> columns, boolean hasPrimaryField)
		{
			super(view);

			mRowContainer = (LinearLayout) view.findViewById(R.id.rowContainer);
			mIsSelectedContainer = (LinearLayout) view.findViewById(R.id.cbContainer);

			mSelectedCheck = (CheckBox) view.findViewById(R.id.cbSelected);
			if (mSelectedCheck != null) {
				mSelectedCheck.setOnClickListener(this);
			}

			if (hasPrimaryField) {
				mIsSelectedContainer.setVisibility(View.VISIBLE);
			}

			// create a readonly placeholder column for every column
			for(DataColumn column : columns) {
				if (column.getDataType() == DataTypeEnum.DATA_TYPE_BOOLEAN) {
					LinearLayout ll = createCheckBox(activityContext);	// need the actual Activity Context to default CheckBox style correctly
					mViews.add(ll);
					mRowContainer.addView(ll);
				}
				else {
					TextView tv = createTextView(activityContext, column);
					mViews.add(tv);
					mRowContainer.addView(tv);
				}
			}
		}

		@Override
		public void onClick(View v) {
			if (clickHandler != null) {
				int position = (int) v.getTag();
				clickHandler.selectedOnCheckedChanged(position, ((CompoundButton)v).isChecked());
			}
		}

		private LinearLayout createCheckBox(Context context) {
			RelativeLayout.LayoutParams linearLayoutParams = new RelativeLayout.LayoutParams(COLUMNN_WIDTH_NUMBER, ViewGroup.LayoutParams.MATCH_PARENT);
			LinearLayout linearLayout = new LinearLayout(KmbApplication.getContext());
			linearLayout.setLayoutParams(linearLayoutParams);
			linearLayout.setGravity(Gravity.CENTER);

			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			AppCompatCheckBox checkBox = new AppCompatCheckBox(context);
			checkBox.setLayoutParams(layoutParams);
			checkBox.setGravity(Gravity.CENTER);
			checkBox.setClickable(false);	// make it look enabled (i.e. white background) but disabled so user can't change
			checkBox.setGravity(Gravity.CENTER);

			linearLayout.addView(checkBox);

			return linearLayout;
		}

		private TextView createTextView(Context context, DataColumn column) {
			int width = COLUMNN_WIDTH_STRING;
			if (column.getDataType() == DATA_TYPE_BOOLEAN || column.getDataType() == DATA_TYPE_DOUBLE || column.getDataType() == DATA_TYPE_INTEGER) {
				width = COLUMNN_WIDTH_NUMBER;
			}

			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);

			TextView tv = new TextView(context);
			tv.setLayoutParams(layoutParams);

			TextViewCompat.setTextAppearance(tv, android.R.style.TextAppearance_Medium);
			tv.setGravity(Gravity.CENTER_VERTICAL);
			tv.setSingleLine(true);
			tv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			tv.setPadding(COLUMN_PADDING_LEFT, COLUMN_PADDING_TOP, COLUMN_PADDING_RIGHT, COLUMN_PADDING_BOTTOM);

			if (column.getDataType() == DATA_TYPE_DOUBLE || column.getDataType() == DATA_TYPE_INTEGER) {
				tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
			}

			return  tv;
		}
	}

	private ISqlQueryAdapterListener mOnClickListener;

	public SqlQueryAdapter(DataSet results, ISqlQueryAdapterListener listener) {
		this.mResults = results;
		mOnClickListener = listener;
	}

	@Override
	public int getCount() {
		return mResults.getRows().size();
	}

	@Override
	public Object getItem(int position) {
		return mResults.getRowAt(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item_queryresults, parent, false);
			viewHolder = new ViewHolder(parent.getContext(), convertView, mResults.getColumns(), mResults.getPrimaryKeys().size() == 1);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		DataRow row = mResults.getRowAt(position);

		for (int i = 0; i < viewHolder.mViews.size(); i++) {
			DataColumn column = mResults.getColumns().get(i);

			if (column.getDataType() == DataTypeEnum.DATA_TYPE_BOOLEAN) {
				LinearLayout ll = (LinearLayout) viewHolder.mViews.get(i);
				if (ll.getChildCount() == 1) {
					CheckBox checkBox = (CheckBox) ll.getChildAt(0);
					if (checkBox != null) {
						checkBox.setChecked((boolean) row.get(column.getColumnName()));
					}
				}
			}
			else {
				TextView tv = (TextView) viewHolder.mViews.get(i);

				Object object = row.get(column.getColumnName());
				if (object == null) {
					tv.setText(KmbApplication.getContext().getText(R.string.null_string));
				}
				else if (column.getDataType() == DataTypeEnum.DATA_TYPE_DATETIME) {
					tv.setText(mDateTimeFormat.format((Date) object));
				}
				else if (column.getDataType() == DataTypeEnum.DATA_TYPE_DATE) {
					tv.setText(mDateFormat.format((Date) object));
				}
				else {
					tv.setText(String.valueOf(object));
				}
			}
		}

		viewHolder.mSelectedCheck.setTag(position);
		viewHolder.mSelectedCheck.setChecked(row.getIsRowSelected());
		viewHolder.clickHandler = this.mOnClickListener;

		return convertView;
	}

	public DataSet getDataSet() { return mResults; }
}