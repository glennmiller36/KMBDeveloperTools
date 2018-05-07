package com.jjkeller.kmb.developertools.manager;

import android.os.Environment;
import android.text.TextUtils;

import com.jjkeller.kmb.developertools.KmbApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import io.reactivex.exceptions.Exceptions;

/**
 * This class provides access to the file system.
 */

public class FileManager implements IFileManager {
    private static final String DIRECTORY_KMBDEVTOOLS = "kmbdevtools";
	private static final String DIRECTORY_DBBACKUPS = "dbbackups";
	private static final String DIRECTORY_SQL = "sql";
	private static final String DIRECTORY_TEMP = "temp";
	private static final File[] NO_FILES = {};
	private static final String FILE_PATH_FILES = "/data/data/com.jjkeller.kmb/files/";
	private static final String FILE_PATH_FILES_ALK = "/data/data/com.jjkeller.kmb.alk/files/";
	public static final String FILE_KMBERRLOG = "kmberrlog.txt";

    @Override
    public boolean doesDatabaseExist(String path) {
        return KmbApplication.getContext().getDatabasePath(path).exists();
    }

	@Override
	public boolean canReadFile(String path) {
    	if (TextUtils.isEmpty(path)) {
    		return false;
		}

		File file = new File(path);
		if (file.exists()) {
			return file.canRead();
		}

		return false;
	}

    /**
     * Copy file from source to destination.
     */
    @Override
    public int copyFile(String sourcePath, String destinationPath) throws IOException {
        // create a handle to the source file
        File sourceFile = new File(sourcePath);
        FileInputStream fis;
		fis = new FileInputStream(sourceFile);

        File destinationFile = new File(destinationPath);

        // Open the empty file as the output stream
        OutputStream output = null;
        try {
            if (!destinationFile.exists()) {
                destinationFile.createNewFile();
            }
            output = new FileOutputStream(destinationFile);
        } catch (IOException e) {
			fis.close();
			Exceptions.propagate(e);
        }

        // Transfer bytes from the sourcefile to the destinationfile
        byte[] buffer = new byte[1024];
        int length = 0;
        try {
            while ((length = fis.read(buffer))>0){
				if (output != null) {
					output.write(buffer, 0, length);
				}
			}
        } catch (IOException e) {
			fis.close();
			if (output != null) {
				output.flush();
			}
			if (output != null) {
				output.close();
			}
			Exceptions.propagate(e);
        }

        // Close the streams
		if (output != null) {
			output.flush();
		}
		if (output != null) {
			output.close();
		}
		fis.close();

		return length;
    }

	/**
	 * Read a file and return the content as string.
	 */
	@Override
	public String getStringFromFile (String sourcePath) throws IOException {
		File fl = new File(sourcePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		//Make sure you close all streams.
		fin.close();
		return ret;
	}

	public String convertStreamToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	/**
	 * Save string to a file.
	 */
	@Override
	public void saveStringToFile (String sourcePath, String data) throws IOException {
		File file = new File(sourcePath);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream outstream = new FileOutputStream(file);
		outstream.write(data.getBytes());
		outstream.close();
	}

	/**
     * Delete file from External Storage.
     */
    @Override
    public boolean deleteFile(String sourcePath) {
		File file = new File(sourcePath);
		return file.exists() && file.delete();

	}

	/**
	 * Delete all files from a directory.
	 */
	@Override
	public void deleteDirectoryFiles(String sourcePath) {
		File[] files = getDirectoryFiles(sourcePath);
		for (File file : files) {
			file.delete();
		}
	}

	/**
	 * Return a list of files from a directory.
	 */
	@Override
	public File[] getDirectoryFiles(String sourcePath) {
		File directory = new File(sourcePath);
		if (!directory.exists())
			return NO_FILES;

		return directory.listFiles();
	}

	/**
     * Manage the main external storage directory for the app. Creates the
     * directory if it does not exist.
     */
    @Override
    public String getExternalStorageDirectory() {
        File file = new File(Environment.getExternalStorageDirectory(), DIRECTORY_KMBDEVTOOLS);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file.getPath() + "/";
    }

	@Override
	public String getDatabaseBackupFileDirectory() {
		File file = new File(getExternalStorageDirectory(), DIRECTORY_DBBACKUPS);
		if (!file.exists()) {
			file.mkdirs();
		}

		return file.getPath() + "/";
	}

	@Override
	public String getSqlFileDirectory() {
		File file = new File(getExternalStorageDirectory(), DIRECTORY_SQL);
		if (!file.exists()) {
			file.mkdirs();
		}

		return file.getPath() + "/";
	}

	@Override
	public String getTempFileDirectory() {
		File file = new File(getExternalStorageDirectory(), DIRECTORY_TEMP);
		if (!file.exists()) {
			file.mkdirs();
		}

		return file.getPath() + "/";
	}

	/**
	 * KMBERRLOG.TXT
	 */
	@Override
	public String getErrorLogPath() {
		File file = new File(FILE_PATH_FILES, FILE_KMBERRLOG);
		if (file.exists()) {
			return file.getPath();
		}

		file = new File(FILE_PATH_FILES_ALK, FILE_KMBERRLOG);
		if (file.exists()) {
			return file.getPath();
		}

		return null;
	}
}
