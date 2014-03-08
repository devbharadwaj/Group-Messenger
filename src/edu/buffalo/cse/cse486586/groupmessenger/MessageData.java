package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;

public class MessageData implements Serializable {

	public String id;
	public String messageToSend;
	
	public MessageData (String id, String messageToSend) {
		this.id = id;
		this.messageToSend = messageToSend;
	}
	
}
