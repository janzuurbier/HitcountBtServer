package nl.hu.zrb.btserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.UUID;

import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class HitCountService extends Service {
	
	private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
	private AcceptThread mAcceptThread;


	int counter;
	

	NotificationManager mNotificationManager; 
	
	private final int SERVICE_RUNNINNG = 1021;

	private final String TAG = "Hitcountservice";
	
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.i(TAG, "onCreate");

		//show notification
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		showNotification(SERVICE_RUNNINNG);

		//read counter from storage
		counter = getSharedPreferences("COUNTSTORAGE", 0).getInt("COUNT", 0);

		//start listenerthread
		mAcceptThread = new AcceptThread();		
		mAcceptThread.start();
		
	}
	
	
	public void onDestroy(){
		super.onDestroy();
		if(mAcceptThread != null)
			mAcceptThread.cancel();
		mNotificationManager.cancel(SERVICE_RUNNINNG);
	}


	private void showNotification(int NOTIFICATION_ID){
		String title = getResources().getString(R.string.oktitle);
		String message = getResources().getString(R.string.okmessage);
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.count)
						.setContentTitle(title)
						.setContentText(message);
		Intent monitorIntent = new Intent(this, HitCountServiceMonitor.class);

		PendingIntent monitorPendingIntent =
				PendingIntent.getActivity(
						this,
						0,
						monitorIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		mBuilder.setContentIntent(monitorPendingIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;
		private final String NAME = "HitCounter";
		private final UUID MY_UUID = new UUID(0xffffffffffffffffL,0x1L);

		public AcceptThread() {
			// Use a temporary object that is later assigned
			// to mmServerSocket, because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				Log.v(TAG, "uuid = " + MY_UUID);
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME,MY_UUID);
			} catch (IOException e) { }
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					Log.v(TAG, "accepting");
					socket = mmServerSocket.accept();
					counter++;
					String output = "U bent nummer: " + counter + "\n";
					Log.v(TAG, output);
					
					OutputStream os = socket.getOutputStream();
					os.write(output.getBytes());
					os.flush();

					//store counter in storage
					getSharedPreferences("COUNTSTORAGE", 0).edit().putInt("COUNT", counter).apply();


				} catch (IOException e) {
					Log.e(TAG, e.toString());
					break;
				}
				
			}
		}

		/** Will cancel the listening socket, and cause thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) { }
		}
	}//AcceptThread



}
