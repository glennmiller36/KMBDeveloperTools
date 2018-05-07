package com.jjkeller.kmb.developertools.manager;

/**
 * This class provides access to application managers. Using
 * interface managers allows unit tests to mock these managers.
 */

public final class Services {
    private static Services mServices = new Services();
    private IDatabaseManager mDatabaseManager = null;
    private IFileManager mFileManager = null;
    private IPreferencesManager mPreferencesManager = null;
    private IBluetoothManager mBluetoothManager = null;
    private IDeviceManager mDeviceManager = null;
    private IThemeManager mThemeManager = null;

    /**
     * Returns the database manager.
     */
    public static IDatabaseManager Database() {
        // lazy load manager
        if (mServices.mDatabaseManager == null)
            mServices.mDatabaseManager = new DatabaseManager();

        return mServices.mDatabaseManager;
    }

    /**
     * Setter allows unit test to to assign this to a mock data manager.
     */
    public static void setDatabaseManager(IDatabaseManager databaseManager) {
        mServices.mDatabaseManager = databaseManager;
    }


    /**
     * Returns the file manager.
     */
    public static IFileManager File() {
        // lazy load manager
        if (mServices.mFileManager == null)
            mServices.mFileManager = new FileManager();

        return mServices.mFileManager;
    }

    /**
     * Setter allows unit test to to assign this to a mock file manager.
     */
    public static void setFileManager(IFileManager fileManager) {
        mServices.mFileManager = fileManager;
    }


    /**
     * Returns the App preferences manager.
     */
    public static IPreferencesManager Preferences() {
        // lazy load manager
        if (mServices.mPreferencesManager == null)
            mServices.mPreferencesManager = new PreferencesManager();

        return mServices.mPreferencesManager;
    }

    /**
     * Setter allows unit test to to assign this to a mock file manager.
     */
    public static void setSettingsManager(IPreferencesManager settingsManager) {
        mServices.mPreferencesManager = settingsManager;
    }


    /**
     * Returns the Bluetooth manager.
     */
    public static IBluetoothManager Bluetooth() {
        // lazy load manager
        if (mServices.mBluetoothManager == null)
            mServices.mBluetoothManager = new BluetoothManager();

        return mServices.mBluetoothManager;
    }

    /**
     * Setter allows unit test to to assign this to a mock bluetooth manager.
     */
    public static void setBluetoothManager(IBluetoothManager bluetoothManager) {
        mServices.mBluetoothManager = bluetoothManager;
    }


    /**
     * Returns the Device manager.
     */
    public static IDeviceManager Device() {
        // lazy load manager
        if (mServices.mDeviceManager == null)
            mServices.mDeviceManager = new DeviceManager();

        return mServices.mDeviceManager;
    }

    /**
     * Setter allows unit test to to assign this to a mock device manager.
     */
    public static void setDeviceManager(IDeviceManager deviceManager) {
        mServices.mDeviceManager = deviceManager;
    }


    /**
     * Returns the Theme manager.
     */
    public static IThemeManager Theme() {
        // lazy load manager
        if (mServices.mThemeManager == null)
            mServices.mThemeManager = new ThemeManager();

        return mServices.mThemeManager;
    }

    /**
     * Setter allows unit test to to assign this to a mock theme manager.
     */
    public static void setThemeManager(IThemeManager themeManager) {
        mServices.mThemeManager = themeManager;
    }
}
