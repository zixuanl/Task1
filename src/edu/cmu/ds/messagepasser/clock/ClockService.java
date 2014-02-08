package edu.cmu.ds.messagepasser.clock;

public abstract class ClockService {

	public abstract Object getIncTimeStamp();
	public abstract Object getTimeStamp();
	public abstract void updateTime(Object timeStamp);

}
