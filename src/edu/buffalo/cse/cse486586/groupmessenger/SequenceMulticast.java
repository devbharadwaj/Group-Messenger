package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class SequenceMulticast {

	private static final String TAG = SequenceMulticast.class.getSimpleName();
	private final String[] peers = {"11124","11120", "11116","11112", "11108"};
	Sequencer sequence;
	
	
	public SequenceMulticast(Sequencer sequence) {
		this.sequence = sequence;
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null, null);
	}
	
    private class ClientTask extends AsyncTask<String, Void, Void> {

		@Override
        protected Void doInBackground(String... msgs) {
            try {
            	for (String port: peers){
            		Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
            				Integer.parseInt(port));
            		/*
            		 * TODO: Fill in your client code that sends out a message.
            		 */
                    OutputStream os = socket.getOutputStream();
                    ObjectOutputStream msgObject = new ObjectOutputStream(os);
                    msgObject.writeObject(sequence);
            		//socket.getOutputStream().write(msgToSend.getBytes("UTF-8"));
            		socket.close();
            		}
            	} catch (UnknownHostException e) {
            		Log.e(TAG, "ClientTask UnknownHostException");
            	} catch (IOException e) {
            		Log.e(TAG, "ClientTask socket IOException" + e);
            	}
            	return null;
            }

        }
}
