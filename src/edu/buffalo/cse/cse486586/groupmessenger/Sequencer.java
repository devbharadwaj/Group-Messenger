package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;

public class Sequencer implements Serializable {

    public static int Sg = -1;
	public String id;
	public String order;

	public Sequencer(String id) {
		this.id = id;
		Sg = Sg + 1;
		this.order = Integer.toString(Sg);
	}
	
	public String getID() {
		return id;
	}
	
	public int getOrder() {
		return Sg;
	}
	

}
