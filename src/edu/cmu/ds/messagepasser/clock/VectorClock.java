package edu.cmu.ds.messagepasser.clock;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class VectorClock extends ClockService {
	private ArrayList<Integer> array = null;
	private Integer sumProc = null;
	private Integer index= null;
	private Semaphore mutex = new Semaphore(1);
	
	public VectorClock(int sum, int index){
		sumProc = sum;
		this.index = index;
		array = new ArrayList<Integer>();
		for (int i = 0; i < sumProc; ++i) {
			array.add(0);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getIncTimeStamp() {
		// TODO Auto-generated method stub
		ArrayList<Integer> result = null;
		try {
			mutex.acquire();
			array.set(index, array.get(index)+1);
			result = (ArrayList<Integer>)array.clone();
			mutex.release();
		} catch (Exception e) {
			mutex.release();
		}
		return result;
	}

	@Override
	public void updateTime(Object timeStamp) {
		ArrayList<Integer> newTime = (ArrayList<Integer>) timeStamp;
		try {
			mutex.acquire();
			array.set(index, array.get(index)+1);
			for (int i = 0; i < array.size(); ++i) {
				if (newTime.get(i) > array.get(i)) {
					array.set(i, newTime.get(i));
				}
			}
			mutex.release();
		} catch (Exception e) {
			mutex.release();
		}
	}

	@Override
	public Object getTimeStamp() {
		// TODO Auto-generated method stub
		ArrayList<Integer> result = null;
		try {
			mutex.acquire();
			result = (ArrayList<Integer>)array.clone();
			mutex.release();
		} catch (Exception e) {
			// e.printStackTrace();
			mutex.release();
		}
		return result;
	}
	
}
