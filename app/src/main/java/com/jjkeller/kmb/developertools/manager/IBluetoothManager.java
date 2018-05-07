package com.jjkeller.kmb.developertools.manager;

import android.bluetooth.BluetoothDevice;

import com.jjkeller.kmb.developertools.model.TerminalCommandModel;

import java.util.List;

/**
 * This class provides communication to the connected ELD Device.
 */

public interface IBluetoothManager {
	List<BluetoothDevice> getPairedEldDevices();

	void saveTerminalCommandFavorites(List<TerminalCommandModel> updatedCommands);
	List<TerminalCommandModel> getTerminalCommandAllList();
	List<TerminalCommandModel> getTerminalCommandFavoritesList();
	void parseTerminalCommandList(String commandData);
}
