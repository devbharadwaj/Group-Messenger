package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class MessageMulticast implements OnClickListener {
	
	private static final String TAG = MessageMulticast.class.getSimpleName();
	private EditText editText;
	private String msgString;
	private String myPort;
	private MessageData msgData;
	private final String[] peers = {"11124","11120", "11116","11112", "11108"};

	public MessageMulticast(EditText editText, String myPort) {
		this.myPort = myPort;
		this.editText = editText;
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		this.msgString = editText.getText().toString() + "\n";
		this.editText.setText("");
		this.msgData = new MessageData(myPort,msgString);
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgString, myPort);
	}

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
            	for (String port: peers) {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(port));
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream msgObject = new ObjectOutputStream(os);
                msgObject.writeObject(msgData);
                //socket.getOutputStream().write(msgString.getBytes("UTF-8"));
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
