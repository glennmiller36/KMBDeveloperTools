/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jjkeller.kmb.developertools.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.jjkeller.kmb.developertools.enumerator.TerminalCommandTypeEnum;
import com.jjkeller.kmb.developertools.model.ResponsePacket;
import com.jjkeller.kmb.developertools.model.BluetoothTerminalModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * This class wass modeled after Android's sample BluetoothChatService (https://developer.android.com/samples/BluetoothChat/index.html)
 */

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {

	// Debugging
	private static final String TAG = "BluetoothService";

	// Unique UUID for this application
	private final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private final int EUCMD_CONSOLE_COMMAND = 0x33;
	private final int EUCMD_GET_CONSOLE_LOG = 0x34;

	private final int RECORD_ID_TYPE = 0;
	private final int FINAL_RECORD_ID = 0xFFFFFFFF;

	private final String TAG_CMD_OPEN = "<cmd>";
	private final String TAG_CMD_CLOSE = "</cmd>";

	public static final int MESSAGE_STATE_CHANGE = 1;
	//public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_COMMAND = 3;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;       			// we're doing nothing
	//public static final int STATE_LISTEN = 1;     			// now listening for incoming connections
	public static final int STATE_CONNECTING = 2; 			// now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;  			// now connected to a remote device
	public static final int STATE_CONNECTION_FAILED = 4;	// connection lost or failed
	public static final int STATE_STOP = 5;       			// service has been explicitly stopped (activity stopped)

	public static final String COMMAND_MENU_HELP_ALL = "help all:menu";
	public static final String COMMAND_HELP_ALL = "help all";

	// Member fields
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	private int mNewState;


	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 *
	 * @param context The UI Activity Context
	 * @param handler A Handler to send messages back to the UI Activity
	 */
	public BluetoothService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mNewState = mState;
		mHandler = handler;
	}

	/**
	 * Update UI title according to the current state of the chat connection
	 */
	private synchronized void updateUserInterface() {
		mState = getState();
		Log.d(TAG, "updateUserInterface() " + mNewState + " -> " + mState);
		mNewState = mState;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service.
	 */
	public synchronized void start() {
		Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mState = STATE_NONE;

		// Update UI title
		updateUserInterface();
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 *
	 * @param device The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();

		// Update UI title
		updateUserInterface();
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 *
	 * @param socket The BluetoothSocket on which the connection was made
	 * @param device The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
		Log.d(TAG, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);

		// Update UI title
		updateUserInterface();
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		Log.d(TAG, "stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mState = STATE_STOP;

		// Update UI title
		updateUserInterface();
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed(String exceptionMessage) {
		if (mState != STATE_STOP) {
			mState = STATE_CONNECTION_FAILED;
			mNewState = mState;

			// Give the new state to the Handler so the UI Activity can update
			mHandler.obtainMessage(MESSAGE_STATE_CHANGE, mNewState, -1, exceptionMessage).sendToTarget();

			// Start the service over to restart listening mode
			BluetoothService.this.start();
		}
	}


	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = "Insecure";

			// Get a BluetoothSocket for a connection with the given BluetoothDevice
			try {
				tmp = device.createInsecureRfcommSocketToServiceRecord(
						MY_UUID_INSECURE);
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
			}
			mmSocket = tmp;
			mState = STATE_CONNECTING;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {

				// Close the socket
				try {
					mmSocket.close();
					//}
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + mSocketType +
							" socket during connection failure", e2);
				}
				connectionFailed(e.getMessage());
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
			}
		}
	}

	/**
	 * Send text command to the VCM console.
	 * The response will include the data within <cmd> tag and if there is more
	 * data than 250 limit, it will include the nextRecordId to be fetched using
	 * ConsoleLog command. The final data should within the closing </cmd> tag.
	 */
	public void sendConsoleCommandToDevice(String commandInput) {

		String command = commandInput;

		// special command issued to get all help commands without writing to output console
		if (commandInput.equalsIgnoreCase(COMMAND_MENU_HELP_ALL)) {
			command = COMMAND_HELP_ALL;
		}

		int commandLength = command.getBytes().length;

		byte[] message = new byte[commandLength + 4];
		message[0] = EUCMD_CONSOLE_COMMAND;
		message[1] = (byte) 255;	// per Bruce = you can pass FF for the CRC signature instead of having to query the device
		message[2] = (byte) 255;
		message[3] = (byte) command.getBytes().length;

		// add console command string
		for (int i=0; i<commandLength; i++) {
			message[i+4] = command.getBytes()[i];
		}

		synchronized (this) {
			if (mState != STATE_CONNECTED) return;
		}

		mConnectedThread.sendConsoleCommandToDevice(commandInput, message);
	}


	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		private Object btReaderLock = new Object();

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			mState = STATE_CONNECTED;
		}


		/**
		 * Write to the connected OutStream.
		 *
		 * @param buffer The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}

		public void sendConsoleCommandToDevice(String commandStr, byte[] message) {

			byte[] response = null;
			StringBuilder consoleLogBuilder = new StringBuilder();

			// Keep listening to the InputStream while connected
			if (mState == STATE_CONNECTED) {
				try {
					// send Cmd_EUCMD_CONSOLE_COMMAND
					// 	Cmd        uint8     EUCMD_CONSOLE_COMMAND [hex]
					//	CRCSig     uint16    CRC signature of EOBR [hex]
					//	Len        uint8     Length of data in packet (1-80)
					//	ConsoleCmd[80] uint8 Console command string [str]
					write(message);

					// get Rsp_EUCMD_CONSOLE_COMMAND response
					//	Cmd        uint8     EUCMD_CONSOLE_COMMAND [hex]
					//	Len        uint8     Length of data in packet (0-255)
					//	RecordID   uint32    Next record ID (0xffffffff=EOF) [hex]
					//	CmdResp[251] uint8   Console log text string [str]
					response = readEntireInputStream(this.mmInStream);

					ResponsePacket commandPacket = parseResponse(response);

					consoleLogBuilder.append(commandPacket.getData());

					int nextRecordId = commandPacket.getNextRecordId();

					// Keep getting the console log 250 bytes at a time until we hit the end
					while (nextRecordId != FINAL_RECORD_ID && mState == STATE_CONNECTED) {

						// Get the next segment of the console log
						ResponsePacket nextPacket = getConsoleLog(nextRecordId);
						nextRecordId = nextPacket.getNextRecordId();

						if(nextPacket.getNextRecordId() != FINAL_RECORD_ID) {
							// Save this segment
							consoleLogBuilder.append(nextPacket.getData());
						}
					}

				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);

					connectionFailed(e.getMessage());
					return;
				}

				String results = consoleLogBuilder.toString();

				int cmdOpen = results.indexOf(TAG_CMD_OPEN);
				int cmdClose = results.indexOf(TAG_CMD_CLOSE);

				// strip off <cmd> and </cmd> tags
				if (cmdOpen >= 0 && cmdClose > 0) {
					results = results.substring(cmdOpen + TAG_CMD_OPEN.length(), cmdClose);
				}

				// strip command name from results
				if (results.startsWith(commandStr)) {
					int indexFirstNewline = results.indexOf("\n");
					if (indexFirstNewline > -1) {
						results = results.substring(indexFirstNewline + 1);
					}
				}

				BluetoothTerminalModel commandModel = new BluetoothTerminalModel(TerminalCommandTypeEnum.COMMAND, commandStr, results);

				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(MESSAGE_COMMAND, -1, -1, commandModel).sendToTarget();
			}
		}

		/**
		 * Gets the first 250 bytes of the console log at the specified timestamp or record ID
		 */
		private ResponsePacket getConsoleLog(int recordId) {

			byte[] response;
			ResponsePacket packet = new ResponsePacket();

			// Keep listening to the InputStream while connected
			if (mState == STATE_CONNECTED) {
				try {
					byte[] message = new byte[13];
					message[0] = EUCMD_GET_CONSOLE_LOG;
					message[1] = (byte) 255;	// per Bruce = you can pass FF for the CRC signature instead of having to query the device
					message[2] = (byte) 255;
					message[3] = (byte) 9;
					message[4] = RECORD_ID_TYPE;

					byte[] recordIdArray = intToByteArray(recordId);
					message[5] = recordIdArray[0];
					message[6] = recordIdArray[1];
					message[7] = recordIdArray[2];
					message[8] = recordIdArray[3];

					// timestamp can be null
					// message[9] = ;
					// message[10] = ;
					// message[11] = ;
					// message[12] = ;

					// Cmd_EUCMD_GET_CONSOLE_LOG
					//	Cmd        uint8     EUCMD_GET_CONSOLE_LOG [hex]
					//	CRCSig     uint16    CRC signature of EOBR [hex]
					//	Len        uint8     Length of data in packet (9)
					//	Method     uint8     Query method (0=rec ID, 1=time)
					//	RecordID   uint32    Record ID (0=oldest, 0xffffffff=newest) [hex]
					//	StartSecs  int32     Negative offset or Unix timestamp (sec) [/1 s]
					write(message);

					// Rsp_EUCMD_GET_CONSOLE_LOG
					//	Cmd        uint8     EUCMD_GET_CONSOLE_LOG [hex]
					//	Len        uint8     Length of data in packet (0-255)
					//	RecordID   uint32    Next record ID (0xffffffff=EOF) [hex]
					//	CmdResp[251] uint8   Console log text string [str]
					response = readEntireInputStream(this.mmInStream);

					packet = parseResponse(response);

				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);

					connectionFailed(e.getMessage());
				}
			}

			return packet;
		}

		/**
		 * Get all the bytes for the command response.
		 */
		private byte[] readEntireInputStream(InputStream inputStream) throws IOException {
			int RECEIVED_PACKET_SIZE = 258;

			boolean retVal = false;
			int bytesRead = 0;
			int totalBytesRead = 0;
			byte[] buffer = new byte[RECEIVED_PACKET_SIZE];
			byte[] response = new byte[RECEIVED_PACKET_SIZE];

			int len = -1;

			boolean interrupted = false;
			boolean done = false;

			synchronized(btReaderLock)
			{
				while (!done && !interrupted && inputStream != null)
				{
					if(Thread.currentThread().isInterrupted()) {
						interrupted = true;
						break;
					}

					try {
						bytesRead = inputStream.read(buffer);
					} catch (IOException e) {
						Log.e(TAG, "disconnected", e);
						connectionFailed(e.getMessage());
						return null;
					}

					if (bytesRead <= 0)
					{
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							interrupted = true;
						}
					}
					else if (bytesRead > 0)
					{
						for (int i=0; i<bytesRead; i++)
						{
							if (i+totalBytesRead < RECEIVED_PACKET_SIZE)
								response[i + totalBytesRead] = buffer[i];
						}
						totalBytesRead += bytesRead;

						if (len == -1 && totalBytesRead > 1)
						{
							len = (int)response[1] & 0xff;
						}

						if (!done && len >= 0 && totalBytesRead >= len+2)
						{
							done = true;
							retVal = true;
						}
					}
				}

				if (!retVal && totalBytesRead > 0) {
					retVal = true;
				}
			}

			return response;
		}

		private ResponsePacket parseResponse(byte[] response) {
			StringBuilder consoleLogBuilder = new StringBuilder();
			ResponsePacket packet = new ResponsePacket();

			if (response == null) {
				return packet;
			}

			int dataSize = response[1] & 0xFF; //anding with FF to make it interpret the byte as unsigned
			if(dataSize <= 0) {
				return packet;
			}

			byte[] data = new byte[dataSize];
			for (int i=0; i<dataSize; i++) {
				data[i] = response[i+2];
			}

			int nextRecordId;

			ByteBuffer buffer = ByteBuffer.wrap(data, 0, 4);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			nextRecordId = buffer.getInt();

			if (data.length > 4) {
				for (int i = 4; i < data.length; i++)
					consoleLogBuilder.append((char) data[i]);
			}

			packet.setData(consoleLogBuilder.toString());
			packet.setNextRecordId(nextRecordId);

			return packet;
		}

		protected final byte[] intToByteArray(int value)
		{
			//NOTE:  Data on eobr is LITTLE_ENDIAN
			return new byte[] {
					(byte) value,
					(byte)(value >>> 8),
					(byte)(value >>> 16),
					(byte)(value >>> 24)};
		}
	}
}
