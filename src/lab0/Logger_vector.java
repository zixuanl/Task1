package lab0;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Logger_vector {
	
	
	public static Vector<TimeStampedMessage> information = new Vector<TimeStampedMessage>();
	public static ServerSocket listener;
	private static Semaphore mutex = new Semaphore(1);
	
	private static void CreateListenThread(final Socket socket)
			throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
					TimeStampedMessage message;
					while (true) {
						message = (TimeStampedMessage)is.readObject();
						if (message == null)
							continue;
						mutex.acquire();
						information.add(message);
						mutex.release();
					}
					
				} catch (IOException e) {
					return;
				} catch (ClassNotFoundException e) {
					return;
				} catch (InterruptedException e) {
					mutex.release();
				}	
			}
		}).start();
	}
	
	public static void CreateThread() throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					ArrayList<Socket> l = new ArrayList<Socket>();
					try {
						while (true) {
							Socket socket = listener.accept();
							l.add(socket);
							CreateListenThread(socket);
							
						}
					} catch (SocketException e) {
						
						for (int i = 0; i < l.size(); i++) {
							l.get(i).close();
						}
						listener.close();
					} 
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}
	
	public static String compare (int a, int b) {
		@SuppressWarnings("unchecked")
		ArrayList<Integer> m1 = (ArrayList<Integer>)information.get(a).getTimeStamp();
		@SuppressWarnings("unchecked")
		ArrayList<Integer> m2 = (ArrayList<Integer>)information.get(b).getTimeStamp();
		int length = m1.size();
		
		boolean check = true;
		for (int i = 0; i < length; i++) {
			if (m1.get(i) <= m2.get(i))
				continue;
			else {
				check = false;
				break;
			}
		}
		if (check == true)
			return " <- ";
		
		check = true;
		for (int i = 0; i < length; i++) {
			if (m1.get(i) >= m2.get(i))
				continue;
			else {
				check = false;
				break;
			}
		}
		if (check == true)
			return " -> ";
		
		return " || ";
	
	}
	
	
	public static void print_info() throws InterruptedException {
		
		mutex.acquire();
		if (information.isEmpty()) {
			System.out.println("There is no log information now");
		}
		for (int i = 0; i < information.size(); i++) {
			TimeStampedMessage e = information.get(i);
			@SuppressWarnings("unchecked")
			ArrayList<Integer> list = (ArrayList<Integer>)e.getTimeStamp();
			
			
			System.out.print(i + " " + e.get_Src() + " to " + e.get_Dest() + " "+ e.get_SeqNum());
			System.out.print(": current time stampe is ( ");
			for (int j = 0; j < list.size(); j++) {
				System.out.print(list.get(j) + " ");
			}
			System.out.println(")");
		}
		
		System.out.print(0);
		for (int i = 1; i < information.size(); i++) {
			System.out.print(compare(i-1, i));
			System.out.print(i);
		}
		System.out.println("");
		
		for (int i = 0; i < information.size()-1; i++) {
			for (int j = i+1; j < information.size(); j ++)
				System.out.println(i + compare(i, j) + j);
		}
		mutex.release();
		
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		String command = new String("");		
		System.out.println("Log server is running!!!");
		
		listener = new ServerSocket(3333);
	
		CreateThread();
		Scanner in = new Scanner(System.in);
		System.out.println("Please enter exit or print");
		System.out.print(">: ");
		command = in.next();
		
		while (true) {
			while (!command.equals("exit") && !command.equals("print")) {
				System.out.print(">: ");
				command = in.next();
			}
			
			if (command.equals("exit"))
				break;
			
			if (command.equals("print")) {
				print_info();
			}
			
			System.out.print(">: ");
			command = in.next();
		}
		listener.close();
		System.out.println("Log server exit normally");
		
	}
	
	
	
}
