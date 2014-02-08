package edu.cmu.ds.messagepasser.model;

public class TimeStampedMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object timeStamp = null;

	public TimeStampedMessage(String destination, String kind, Object body) {
		super(destination,  kind,  body);
	}

	public Object getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Object timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
}
