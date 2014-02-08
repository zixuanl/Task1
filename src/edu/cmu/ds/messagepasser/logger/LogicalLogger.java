package edu.cmu.ds.messagepasser.logger;

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

import edu.cmu.ds.messagepasser.ConfigFileParser;
import edu.cmu.ds.messagepasser.model.TimeStampedMessage;

public class LogicalLogger {

	public static Queue<TimeStampedMessage> loggedMessages;
	private static Semaphore mutex = new Semaphore(1);

	/**
	 * Start a thread that keeps listening to incoming connections from
	 * MessagePassers
	 * 
	 * @throws IOException
	 */
	public static void startListenerThread(final ServerSocket listenerSocket) throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					ArrayList<Socket> acceptedClientsList = new ArrayList<Socket>();
					try {
						while (true) {
							Socket socket = listenerSocket.accept();
							acceptedClientsList.add(socket);
							startClientThread(socket);
						}
					} catch (SocketException e) {
						for (int i = 0; i < acceptedClientsList.size(); i++) {
							acceptedClientsList.get(i).close();
						}
					} finally {
						listenerSocket.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Start a thread that receives an accepted connection from client and keeps
	 * listening to incoming TimeStampMessages
	 * 
	 * @param socket
	 * @throws IOException
	 */
	private static void startClientThread(final Socket socket) throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
					TimeStampedMessage message;
					while (true) {
						message = (TimeStampedMessage) inputStream.readObject();
						if (message == null)
							continue;
						mutex.acquire();
						loggedMessages.add(message);
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

	public static void printLoggedMessages() throws InterruptedException {
		mutex.acquire();
		if (loggedMessages.isEmpty()) {
			System.out.println("There is no logged messages.");
		}
		ArrayList<TimeStampedMessage> list = new ArrayList<TimeStampedMessage>();
		for (int i = 0; i < loggedMessages.size(); i++) {
			TimeStampedMessage message = loggedMessages.poll();
			list.add(message);
			System.out.println(message.getSource() + " to " + message.getDestination() + " "
					+ message.getSequenceNumber() + " " + (Integer) message.getTimeStamp());
		}
		for (int i = 0; i < list.size(); i++) {
			loggedMessages.add(list.get(i));
		}
		mutex.release();
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		Comparator<TimeStampedMessage> logicalComparator = new Comparator<TimeStampedMessage>() {
			public int compare(TimeStampedMessage o1, TimeStampedMessage o2) {
				int time1 = (Integer) o1.getTimeStamp();
				int time2 = (Integer) o2.getTimeStamp();
				if (time1 < time2) {
					return -1;
				} else if (time1 > time2) {
					return 1;
				} else {
					return 0;
				}
			}
		};

		loggedMessages = new PriorityQueue<TimeStampedMessage>(100, logicalComparator);

		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.println("Please enter configuration file name");
			System.out.print("LogicalLogger>: ");
			String configFileName = in.next();
			try {
				ConfigFileParser configParser = new ConfigFileParser(configFileName, null);
				int port = configParser.getLoggerPort();
				startListenerThread(new ServerSocket(port));
				System.out.println("Log server is running at port " + port);
				break;
			} catch (Exception e) {
				System.out.println("Could not load config file. " + e);
			}
		}
		System.out.println("Available commands: print, exit");
		System.out.print("LogicalLogger>: ");
		String command;
		while ((command = in.next()) != null) {
			if (command.equals("exit"))
				break;
			else if (command.equals("print"))
				printLoggedMessages();
			else
				System.out.println("Available commands: print, exit");
			System.out.print("LogicalLogger>: ");
		}
		in.close();
		System.out.println("LogicalLogger terminated normally");
	}

}
