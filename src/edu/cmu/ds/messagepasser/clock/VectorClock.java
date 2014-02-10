package edu.cmu.ds.messagepasser.clock;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class VectorClock extends ClockService {
	private ArrayList<Integer> vectorTimeStamp = null;
	private Integer processCount = null;
	private Integer localProcessIndex = null;
	private Semaphore mutex = new Semaphore(1);
	
	public VectorClock(int sum, int index){
		processCount = sum;
		localProcessIndex = index;
		vectorTimeStamp = new ArrayList<Integer>();
		for (int i = 0; i < processCount; ++i) {
			vectorTimeStamp.add(0);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getIncTimeStamp() {
		ArrayList<Integer> result = null;
		try {
			mutex.acquire();
			vectorTimeStamp.set(localProcessIndex, vectorTimeStamp.get(localProcessIndex)+1);
			result = (ArrayList<Integer>)vectorTimeStamp.clone();
		} catch (Exception e) {
		} finally {
			mutex.release();
		}
		return result;
	}
	
	/**
	 * Increment vector timestamp at a specific index
	 * @param processIndex
	 */
	public void incTimeStamp(int processIndex) {
		try {
			mutex.acquire();
			vectorTimeStamp.set(processIndex, vectorTimeStamp.get(processIndex)+1);
		} catch (Exception e) {
		} finally {
			mutex.release();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateTime(Object timeStamp) {
		ArrayList<Integer> newTime = (ArrayList<Integer>) timeStamp;
		try {
			mutex.acquire();
			vectorTimeStamp.set(localProcessIndex, vectorTimeStamp.get(localProcessIndex)+1);
			for (int i = 0; i < vectorTimeStamp.size(); ++i) {
				if (newTime.get(i) > vectorTimeStamp.get(i)) {
					vectorTimeStamp.set(i, newTime.get(i));
				}
			}
		} catch (Exception e) {
		} finally {
			mutex.release();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getTimeStamp() {
		ArrayList<Integer> result = null;
		try {
			mutex.acquire();
			result = (ArrayList<Integer>)vectorTimeStamp.clone();
		} catch (Exception e) {
		} finally {
			mutex.release();
		}
		return result;
	}
	
}
