package edu.cmu.ds.messagepasser.clock;

import java.util.concurrent.Semaphore;

public class LogicalClock extends ClockService {
	private Integer count = -1;
	private Semaphore mutex = new Semaphore(1);
	
	public Object incrementAndGetTimeStamp() {
		// TODO Auto-generated method stub
		int result = 0;
		try {
			mutex.acquire();
			++count;
			result = count;
			mutex.release();
		} catch (Exception e) {
			mutex.release();
		}
		return result;
	}

	@Override
	public void updateTime(Object timeStamp) {
		// TODO Auto-generated method stub
		Integer newTime = (Integer) timeStamp;
		try {
			mutex.acquire();
			if (count >= newTime) {
				count += 1;
			} else {
				count = newTime + 1;
			}
			mutex.release();
		} catch (Exception e) {
			mutex.release();
		}
	}

	@Override
	public Object getTimeStamp() {
		// TODO Auto-generated method stub
		Integer result = 0;
		try {
			mutex.acquire();
			result = count;
			mutex.release();
		} catch (Exception e) {
			mutex.release();
		}
		return result;
	}
}
