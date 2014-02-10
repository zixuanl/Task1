package edu.cmu.ds.messagepasser.model;

public class TimeStampedMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object timeStamp = null;
	
	public TimeStampedMessage(TimeStampedMessage target) {
		super((Message) target);
		this.timeStamp = target.timeStamp;
	}

	public TimeStampedMessage(String destination, String kind, Object body) {
		super(destination, kind, body);
	}

	public Object getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Object timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "TimeStampedMessage[" +
				"\n\ttimeStamp = "+ timeStamp +
				"\n\tsource = " + source + 
				"\n\tdestination = " + destination +
				"\n\tsequenceNumber = " + sequenceNumber + 
				"\n\tisDuplicate = " + isDuplicate + 
				"\n\tkind = " + kind + 
				"\n\tbody = " + (String) data + 
				"\n]";
	}

}
