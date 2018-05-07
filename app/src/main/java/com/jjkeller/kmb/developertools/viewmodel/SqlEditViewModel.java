package com.jjkeller.kmb.developertools.viewmodel;

import android.support.annotation.NonNull;

import com.jjkeller.kmb.developertools.database.DataSet;
import com.jjkeller.kmb.developertools.enumerator.EditModeEnum;
import com.jjkeller.kmb.developertools.manager.Services;

import io.reactivex.Observable;

/**
 * View model for the SQL Edit activity
 */

public class SqlEditViewModel {
    private DataSet mDataSet = new DataSet();
    private EditModeEnum mEditMode = EditModeEnum.EDIT;

    /**
     * Constructors
     */


    /**
     * Properties
     */

    @NonNull
    public DataSet getDataSet() { return mDataSet; }

    public void setDataSet(DataSet dataSet) { mDataSet = dataSet; }

    @NonNull
    public EditModeEnum getEditMode() { return mEditMode; }

    public void setEditMode(EditModeEnum editMode) { mEditMode = editMode; }

    /**
     * Methods
     */

    @NonNull
    public Observable<DataSet>  executeQuery(String sql) {
        return Observable.create(e -> {
            DataSet result = Services.Database().executeRawSql(sql);
            e.onNext(result);
            e.onComplete();
        });
    }
}