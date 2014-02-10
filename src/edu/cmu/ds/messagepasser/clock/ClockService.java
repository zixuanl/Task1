package edu.cmu.ds.messagepasser.clock;

public abstract class ClockService {

	public abstract Object incrementAndGetTimeStamp();
	public abstract Object getTimeStamp();
	public abstract void updateTime(Object timeStamp);

}
