package rs.kockasystems.auraluxtest.android;

import android.os.Bundle;
import android.bluetooth.*;
import android.content.*;

import java.io.*;
import java.util.*;

import rs.kockasystems.auraluxtest.AuraluxTest;
import rs.kockasystems.auraluxtest.BluetoothConnection;
import rs.kockasystems.auraluxtest.Data;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import android.bluetooth.*;
import android.content.*;
import android.app.*;

public class AndroidLauncher extends AndroidApplication
{
	private final int BLUETOOTH_TURN_ON_CODE = 1745;
	private final String BLUETOOTH_NAME = "rs.kockasystems.mrkonjic.Bluetooth";
	private WriteThread wt;
	private ReadThread rt;
	private ConnectThread ct;
	private AcceptThread at;
	private BluetoothAdapter bta;
	private BluetoothDevice scanned;
	private Set<BluetoothDevice> devices;
	private final UUID BLUETOOTH_UUID = UUID.fromString("457807c0-4897-11df-9879-0800200c9a66");
	private String buffer = "";
	private boolean host;
	private boolean connected;
	private String message;
	private BluetoothDevice lastConnectedDevice;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.hideStatusBar = true;
        initialize(new AuraluxTest(new AndroidBluetoothConnection()), cfg);
    }
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(btr);
	}
	
	private class AndroidBluetoothConnection implements BluetoothConnection
	{

		@Override
		public String read()
		{
			if(message != "")System.out.println("Found a message!");
			String temp = message;
			message = "";
			return temp;
		}

		@Override
		public void write(Data data) { buffer = data.toString(); System.out.println("Wrote");}


		public AndroidBluetoothConnection()
		{
			bta = BluetoothAdapter.getDefaultAdapter();
			if(bta == null)
			{
				//notSupported = true;
				return;
			}
			if(!bta.isEnabled())startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_TURN_ON_CODE);
			devices = bta.getBondedDevices();
			registerReceiver(btr, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
		
		@Override
		public void scan() { bta.startDiscovery(); }
		
		@Override
		public BluetoothDevice getScannedDevice() { return scanned; }
		
		@Override
		public void connect(final Object device)
		{
			//if(notSupported){}
			//else
			//{
				lastConnectedDevice = (BluetoothDevice)device;
				ct = new ConnectThread();
				ct.start();
			//}
		}
		
		@Override
		public Object[] getPairedDevices() { return devices.toArray(); }
		
		@Override
		public String[] getPairedDevicesNames()
		{
			String[] names = new String[devices.size()];
			int i = 0;
			for(BluetoothDevice dev : devices)names[i++] = dev.getName();
			return names;
		}

		@Override
		public void accept()
		{
			at = new AcceptThread();
			at.start();
		}

		@Override
		public boolean isConnected(){ return connected; }

		@Override
		public void reconnect() { connect(lastConnectedDevice); }
		
	}
	
	
	
	private class AcceptThread extends Thread
	{
		private final BluetoothServerSocket server;

		public AcceptThread()
		{
			startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
			BluetoothServerSocket tmp = null;
			host = true;
			try { tmp = bta.listenUsingRfcommWithServiceRecord(BLUETOOTH_NAME, BLUETOOTH_UUID); }
			catch(IOException e){ }
			server = tmp;
		}

		@Override
		public void run()
		{
			BluetoothSocket socket = null;
			while(true)
			{
				try{ socket = server.accept(); }
				catch(IOException e) { break; }
				if(socket != null)
				{
					rt = new ReadThread(socket);
					wt = new WriteThread(socket);
					rt.start();
					wt.start();
					connected = true;
					break;
				}
			}
		}
		public void cancel()
		{
			try{ server.close(); }
			catch(IOException e){  }
		}

	}


	private class ConnectThread extends Thread
	{
		private final BluetoothSocket client;
		private final BluetoothDevice device;

		public ConnectThread()
		{
			BluetoothSocket tmp = null;
			host = false;
			device = lastConnectedDevice;
			try{ tmp = device.createRfcommSocketToServiceRecord(BLUETOOTH_UUID); }
			catch(IOException e){  }
			client = tmp;
		}

		@Override
		public void run()
		{
			bta.cancelDiscovery();
			try{ client.connect(); }
			catch(IOException e1)
			{
				try{ client.close(); }
				catch(IOException e2){  }
				return;
			}
			rt = new ReadThread(client);
			wt = new WriteThread(client);
			rt.start();
			wt.start();
			connected = true;
		}
		public void cancel()
		{
			try{ client.close(); }
			catch(IOException e){  }
		}
	}

	private class ReadThread extends Thread
	{
		private final BluetoothSocket socket;
		private final InputStream is;

		public ReadThread(BluetoothSocket s)
		{
			socket = s;
			InputStream tmpi = null;
			try{ tmpi = socket.getInputStream(); }
			catch(IOException e){  }
			is = tmpi;
		}

		@Override
		public void run()
		{
			int bytes;
			byte[] buffer = new byte[1024];
			while(true)
			{
				try
				{
					if(is.available() > 0)
					{
						bytes = is.read(buffer);
						message = new String(buffer, 0, bytes);
						System.out.println("Found a message!");
					}
				}
				catch(IOException e)
				{
					connected = false;
					break;
				}
			}
		}
	}

	private class WriteThread extends Thread
	{
		private final BluetoothSocket socket;
		private final OutputStream os;

		WriteThread(BluetoothSocket s)
		{
			socket = s;
			OutputStream tmpo = null;
			try{ tmpo = socket.getOutputStream(); }
			catch(IOException e){  }
			os = tmpo;
		}
		
		@Override
		public void run()
		{
			System.out.println("Started running WriteThread");
			while(true)
			{
				try
				{
					if(buffer != "")
					{
						os.write(buffer.getBytes());
						os.flush();
						buffer = "";
					}
				}
				catch(IOException e) { break; }
			}
		}

	}
	
	
	
	private final BroadcastReceiver btr = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent) { if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND))scanned = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); }
	};
	
}
