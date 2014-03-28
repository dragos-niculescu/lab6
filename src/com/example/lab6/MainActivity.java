package com.example.lab6;

 
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
 
public class MainActivity extends Activity {
 
	private String greeting = "Hello world";
	private SingleThreadedServer greetingServer = null;
	private Thread greetingThread; 
	private final static String TAG = "MainActivity";
	 
	
	private class SingleThreadedServer implements Runnable {
 
		private final static String TAG = "ServerThread";
 
		// For a TCP connection (i.e. a server) we need a ServerSocket
		private ServerSocket in;
 
				
		private volatile boolean serverEnabled = false; 
		
		public synchronized void setEnabled(boolean state){
				serverEnabled = state;
		}
		
		public  synchronized boolean isEnabled(){
			return serverEnabled;
		}
		
		// In the constructor we try creating the server socket, on port 9000.
		public SingleThreadedServer() {
			try {
				// Beware: Only privileged users can use ports below 1023.
				in = new ServerSocket(9000);
			} catch (Exception e) {
				Log.e(TAG, "Cannot create socket. Due to: " + e.getMessage());
			}
		}
 
		@Override
		public void run() {

			// Always try serving incoming requests.
			while(isEnabled()) {

				//For every request we are allocated a new socket.
				Socket incomingRequest = null;

				try {
					// Wait in blocked state for a request.
					incomingRequest = in.accept();
				} catch (IOException e) {
					Log.e(TAG, "Error when accepting connection.");
				}

				// When accept() returns a new request was received.
				// We use the incomingRequest socket for I/O
				Log.d(TAG, "New request from: " + incomingRequest.getInetAddress());

				// Get its associated OutputStream for writing.
				OutputStream responseStream = null;
				try {
					responseStream = incomingRequest.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Cannot get outputstream.");
				}

				// Wrap it with a PrinStream for convenience.
				PrintStream writer = new PrintStream(responseStream);
				try { Thread.sleep(300, 0);} 
				catch (InterruptedException e) {	}
				writer.print(greeting + "\n");

				// Make sure data is sent and allocated resources are cleared.
				try {
					incomingRequest.close();
				} catch (IOException e) {
					Log.e(TAG, "Error finishing request.");
				}

				Log.d(TAG, "Sent greeting.");
				// Continue the looping.
			}
			try {
				in.close();
			} catch (IOException e){ 
				Log.d(TAG, "problem closing");
			}
		}
	
	}
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 
		EditText greetingField = (EditText)findViewById(R.id.greeting);
 
		/**
		 * When the text changes, we change the greeting accordingly.
		 */
		greetingField.addTextChangedListener(new TextWatcher() {
 
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
 
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
 
			@Override
			public void afterTextChanged(Editable s) {
				greeting = s.toString();
			}
		});
 
		if(greetingServer == null){
			greetingServer = new SingleThreadedServer();
			greetingThread = new Thread(greetingServer);
			greetingThread.start();
			greetingServer.setEnabled(true);
		}
 
	}

	@Override
    protected void onDestroy() {
		/*try {
	    	greetingThread.join();
      	} catch (InterruptedException e) {
      		Log.d(TAG, "exception in thread join");
      	}*/
		Log.d(TAG, "onDestroy");
		//try{
	    	//if(greetingServer.in != null)
	    		greetingServer.setEnabled(false);
	    	//	greetingServer.in.close();
	    	//	greetingServer = null; 
        	//} catch (IOException e) {
        	//	Log.d(TAG, "exception in server close");
        	//	e.printStackTrace();
        	//}
		super.onDestroy();
	}
	 
}

