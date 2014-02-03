package lab0;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Logger_logical {
	
	
	public static Queue<TimeStampedMessage> information;
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
	
	
	public static void print_info() throws InterruptedException {
		mutex.acquire();
		if (information.isEmpty()) {
			System.out.println("There is no log information now");
		}
		
		int temp = information.size();
		ArrayList<TimeStampedMessage> list = new ArrayList<TimeStampedMessage>();
		for (int i = 0; i < temp; i++) {
			TimeStampedMessage e = information.poll();
			list.add(e);
			System.out.println(e.get_Src() + " to " + e.get_Dest() + " "+ e.get_SeqNum() + " " + (Integer)e.getTimeStamp());
		}
		for (int i = 0; i < list.size(); i++){
			information.add(list.get(i));
		}
		mutex.release();
		
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		
        Comparator<TimeStampedMessage> OrderIsdn =  new Comparator<TimeStampedMessage>(){  
            public int compare(TimeStampedMessage o1, TimeStampedMessage o2) {  
                // TODO Auto-generated method stub  
                int numbera = (Integer)o1.getTimeStamp();
                int numberb = (Integer)o2.getTimeStamp();  
                if(numberb > numbera)  
                {  
                    return -1;  
                }  
                else if(numberb<numbera)  
                {  
                    return 1;  
                }  
                else  
                {  
                    return 0;  
                }  
              
            }     
        };
		
		information = new PriorityQueue<TimeStampedMessage>(100, OrderIsdn);  
				
		String command = new String("");
		System.out.println("Log server is running!!!");
		Scanner in = new Scanner(System.in);
		listener = new ServerSocket(3333);
		CreateThread();
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
