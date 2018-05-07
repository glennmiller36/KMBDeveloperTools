package com.jjkeller.kmb.developertools.viewmodel;

import android.support.annotation.NonNull;

import com.jjkeller.kmb.developertools.database.DataSet;
import com.jjkeller.kmb.developertools.manager.Services;

import java.util.List;

import io.reactivex.Observable;

/**
 * View model for the SQL Query activity
 */

public class SqlQueryViewModel {

	@NonNull
	public Observable<DataSet>  executeQuery(String sql) {
		return Observable.create(e -> {
			DataSet result = Services.Database().executeRawSql(sql);
			e.onNext(result);
			e.onComplete();
		});
	}

	public Observable<Integer> deleteRecord(String tableName, List<Integer> keysToDelete) {
		return Observable.create(e -> {
			int result = Services.Database().deleteRecord(tableName, keysToDelete);
			e.onNext(result);
			e.onComplete();
		});
	}
}