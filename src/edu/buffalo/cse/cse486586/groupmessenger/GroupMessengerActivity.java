package edu.buffalo.cse.cse486586.groupmessenger;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    private static final int SERVER_PORT = 10000;
	private static final String TAG = GroupMessengerActivity.class.getSimpleName();
	private static String myPort;

	private Queue<MessageData> messageQueue = new LinkedList<MessageData>();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        final EditText editText = (EditText) findViewById(R.id.editText1);
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        

        
        findViewById(R.id.button4).setOnClickListener( 
        		new MessageMulticast(editText,myPort));
        

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             * 
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             * 
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
    
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        private Sequencer callToSeq;
		private Sequencer callFromSeq;
	    private static final String KEY_FIELD = "key";
	    private static final String VALUE_FIELD = "value";
	    ContentResolver MyContentResolver = getContentResolver();
	    Uri MyUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");
	    ContentValues MyContentValues;

	    private Uri buildUri(String scheme, String authority) {
	        Uri.Builder uriBuilder = new Uri.Builder();
	        uriBuilder.authority(authority);
	        uriBuilder.scheme(scheme);
	        return uriBuilder.build();
	    }
	    


		@Override
        protected Void doInBackground(ServerSocket... sockets) {
            try {	
            	ServerSocket serverSocket = sockets[0];
            	while (true) {
            		Socket sock = serverSocket.accept();
            		//BufferedReader datain = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            		//String temp = datain.readLine();
            		// take object not string
            		InputStream is = sock.getInputStream();
            		ObjectInputStream ois = new ObjectInputStream(is);
            		Object recievedMessage = ois.readObject();
            		if (recievedMessage instanceof MessageData) {
            			MessageData msgData = (MessageData)recievedMessage;
            			//publishProgress(msgData.id,msgData.messageToSend);
            			messageQueue.add(msgData);
            			if (myPort.equals("11108")) {
            				callToSeq = new Sequencer(msgData.id);
            				new SequenceMulticast(callToSeq);
            			}
            		}
            		else if (recievedMessage instanceof Sequencer) {
            			callFromSeq = (Sequencer)recievedMessage;
            			for (MessageData msg: messageQueue) {
            				if(msg.id.equals(callFromSeq.getID())) {
            		            MyContentValues = new ContentValues();
            		            MyContentValues.put(KEY_FIELD, callFromSeq.order);
            		            MyContentValues.put(VALUE_FIELD, msg.messageToSend);
            		            MyContentResolver.insert(MyUri, MyContentValues);
            		            publishProgress(msg.messageToSend);
            					messageQueue.remove(msg);
            					break;
            				}
            			}
            		}
        		}
            } catch (IOException e) {
            	Log.e(TAG,"IO error: " + e);
            } catch (ClassNotFoundException e) {
				Log.e(TAG,"Class Not Found:" + e);
			}
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            
            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             * 
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            
            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }

}
