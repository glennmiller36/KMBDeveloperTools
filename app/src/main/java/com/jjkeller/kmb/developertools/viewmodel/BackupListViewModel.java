package com.jjkeller.kmb.developertools.viewmodel;

import android.support.annotation.NonNull;

import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.model.DatabaseBackupModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * View model for the Backup List activity
 */

public class BackupListViewModel {

    @NonNull
    public Observable<List<DatabaseBackupModel>>  getBackupList() {
        return Observable.create(e -> {
			List<DatabaseBackupModel> model = new ArrayList<>();
			model = Services.Database().getBackupList();
			e.onNext(model);
			e.onComplete();
		});
    }

    @NonNull
    public Observable<DatabaseBackupModel>  backupDatabase(String name) {
        return Observable.create(e -> {
			DatabaseBackupModel result = Services.Database().backupDatabase(name);
			e.onNext(result);
			e.onComplete();
		});
    }

    @NonNull
    public Observable<Integer>  deleteBackupDatabase(String path, int position) {
        return Observable.create(e -> {
			int result = Services.Database().deleteDatabase(path) ? position : -1;
			e.onNext(result);
			e.onComplete();
		});
    }

    @NonNull
    public Observable<Integer>  restoreBackupDatabase(String path) {
        return Observable.create(e -> {
			Integer result = Services.Database().restoreDatabase(path);
			e.onNext(result);
			e.onComplete();
		});
    }
}
