package com.jjkeller.kmb.developertools.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.jjkeller.kmb.developertools.model.TerminalCommandModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This class provides communication to the connected ELD Device.
 */

public class BluetoothManager implements IBluetoothManager {

    private static final int GEN2_RVN_BTCLASS = 0x0704;
    
    private List<TerminalCommandModel> mTerminalCommands = new ArrayList<>();

    public List<BluetoothDevice> getPairedEldDevices() {

        List<BluetoothDevice> eldDevices = new ArrayList<>();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getBluetoothClass().getDeviceClass() == GEN2_RVN_BTCLASS) {
                        String address = device.getAddress();
                        String name = device.getName();
                        eldDevices.add(device);
                    }
                }
            }
        }

        // sort alphabetically by Name
        Collections.sort(eldDevices,
                (o1, o2) -> o1.getName().compareTo(o2.getName()));

        return eldDevices;
    }
    
    /**
     * Return list of all available Terminal Commands
     */
    public List<TerminalCommandModel> getTerminalCommandAllList() {
        return mTerminalCommands;
    }

    /**
     * Return list of Favorite Terminal Commands
     */
    public List<TerminalCommandModel> getTerminalCommandFavoritesList() {
        List<TerminalCommandModel> favoriteCommands = new ArrayList<>();

        for (TerminalCommandModel model : mTerminalCommands) {
            if (model.getIsFavorite()){
                favoriteCommands.add(model);
            }
        }

        return favoriteCommands;
    }

    /**
     * Persist checked Favorites to local Preference storage
     */
    public void saveTerminalCommandFavorites(List<TerminalCommandModel> updatedCommands) {
        mTerminalCommands = updatedCommands;

        // persist comma separated string to local Preferences storage
        String commaSeparatedString = getFavoritesString();
        Services.Preferences().setTerminalCommandsFavorites(commaSeparatedString);
    }

    /**
     * Apply comma separated string of Favorites stored in local Preference storage
     */
    private void applyFavorites() {
        String commaSeparatedString = Services.Preferences().getTerminalCommandsFavorites();

        if (TextUtils.isEmpty(commaSeparatedString)) {
            return;
        }

        String[] pieces = commaSeparatedString.split(",");

        for (int i = 0; i < pieces.length; i++) {
            for (TerminalCommandModel model : mTerminalCommands) {
                if (model.getCommand().equalsIgnoreCase(pieces[i])) {
                    model.setIsFavorite(true);
                    break;
                }
            }
        }
    }

    /**
     * Generate the comma separated list of Favorites so it can be stored as Preference data.
     */
    private String getFavoritesString() {
        List<String> favorites = new ArrayList<>();

        for (TerminalCommandModel model : mTerminalCommands) {
            if (model.getIsFavorite()) {
                favorites.add(model.getCommand());
            }
        }

        return android.text.TextUtils.join(",", favorites);
    }

    public void parseTerminalCommandList(String commandData) {
        String newline = System.getProperty("line.separator");

        int id = 0;
        String[] newlinePieces = commandData.split(newline);


        for (int i = 0; i < newlinePieces.length; i++) {
            String[] dashPieces = newlinePieces[i].split("-");
            if (dashPieces.length != 2) {
                continue;
            }

            mTerminalCommands.add(new TerminalCommandModel(id, dashPieces[0].trim(), dashPieces[1].trim()));
            id++;
        }

        // set IsFavorite based on local Preference storage
        applyFavorites();
    }
}
