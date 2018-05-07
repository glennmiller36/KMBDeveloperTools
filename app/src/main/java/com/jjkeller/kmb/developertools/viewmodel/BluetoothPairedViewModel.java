package com.jjkeller.kmb.developertools.viewmodel;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.jjkeller.kmb.developertools.manager.Services;

import java.util.List;

import io.reactivex.Observable;

/**
 * View model for reading and displaying bluetooth devices currently connected.
 */

public class BluetoothPairedViewModel {

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
    public Observable<List<BluetoothDevice>>  getConnectedEldDevices() {
        return Observable.create(e -> {
            List<BluetoothDevice> result = Services.Bluetooth().getPairedEldDevices();
            e.onNext(result);
            e.onComplete();
        });
    }
}