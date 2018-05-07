package com.jjkeller.kmb.developertools.viewmodel;

import android.support.annotation.NonNull;

import com.jjkeller.kmb.developertools.manager.Services;
import com.jjkeller.kmb.developertools.model.TerminalCommandModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * View model for display list of available commands and corresponding command description.
 */

public class TerminalCommandsViewModel {

    /**
     * Constructors
     */


    /**
     * Properties
     */


    /**
     * Methods
     */

    @NonNull
    public Observable<List<TerminalCommandModel>>  getCommands(boolean showFavorites) {
        return Observable.create(e -> {
            List<TerminalCommandModel> result = showFavorites ? Services.Bluetooth().getTerminalCommandFavoritesList() : Services.Bluetooth().getTerminalCommandAllList();
            e.onNext(result);
            e.onComplete();
        });
    }
}