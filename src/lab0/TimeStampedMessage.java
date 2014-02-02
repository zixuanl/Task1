package lab0;

public class TimeStampedMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object timeStamp = null;

	public TimeStampedMessage(String string, String string2, String message_body) {
		// TODO Auto-generated constructor stub
		super(string,  string2,  message_body);
	}

	public Object getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Object timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
}
