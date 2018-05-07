package com.jjkeller.kmb.developertools.model;

import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import com.jjkeller.kmb.developertools.KmbApplication;
import com.jjkeller.kmb.developertools.R;

import java.util.Date;

/**
 * DatabaseBackupModel class containing metadata about a database backup file.
 */

public class DatabaseBackupModel {

	@NonNull
	private String mName;

	@NonNull
	private final String mPath;

	private Date mLastModifiedDate;
	private long mFileSizeInBytes;

	public DatabaseBackupModel(@NonNull final String name, @NonNull final String path, final Date lastModifiedDate, final long fileSizeInBytes) {
		mName = name;
		mPath = path;
		mLastModifiedDate = lastModifiedDate;
		mFileSizeInBytes = fileSizeInBytes;
	}

	@NonNull
	public String getName() {
		return mName;
	}

	@NonNull
	public String getPath() {
		return mPath;
	}

	public Date getLastModifiedDate() {
		return mLastModifiedDate;
	}

	public String getFormattedLastModifiedDate() {
		return DateFormat.format("EEE, MMM d, yyyy hh:mm:ss a", mLastModifiedDate).toString();
	}

	public String getFormattedSize() {
		String hrSize;

		// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
		long fileSizeInKB = mFileSizeInBytes / 1024;

		// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
		long fileSizeInMB = fileSizeInKB / 1024;

		if (fileSizeInMB > 1) {
			hrSize = String.format(KmbApplication.getContext().getString(R.string.file_size_mb), fileSizeInMB);
		} else {
			hrSize = String.format(KmbApplication.getContext().getString(R.string.file_size_kb), fileSizeInKB);
		}

		return hrSize;
	}
}
