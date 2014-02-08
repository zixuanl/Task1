package edu.cmu.ds.messagepasser.logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import edu.cmu.ds.messagepasser.ConfigFileParser;
import edu.cmu.ds.messagepasser.model.TimeStampedMessage;

public class VectorLogger {

	public static Vector<TimeStampedMessage> information = new Vector<TimeStampedMessage>();
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
					ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
					TimeStampedMessage message;
					while (true) {
						message = (TimeStampedMessage) is.readObject();
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

	public static String compare(int a, int b) {
		@SuppressWarnings("unchecked")
		ArrayList<Integer> m1 = (ArrayList<Integer>) information.get(a).getTimeStamp();
		@SuppressWarnings("unchecked")
		ArrayList<Integer> m2 = (ArrayList<Integer>) information.get(b).getTimeStamp();
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

	public static void printInformation() throws InterruptedException {

		mutex.acquire();
		if (information.isEmpty()) {
			System.out.println("There is no logged messages.");
		}
		for (int i = 0; i < information.size(); i++) {
			TimeStampedMessage e = information.get(i);
			@SuppressWarnings("unchecked")
			ArrayList<Integer> list = (ArrayList<Integer>) e.getTimeStamp();

			System.out.print(i + " " + e.getSource() + " to " + e.getDestination() + " "
					+ e.getSequenceNumber());
			System.out.print(": current time stampe is ( ");
			for (int j = 0; j < list.size(); j++) {
				System.out.print(list.get(j) + " ");
			}
			System.out.println(")");
		}

		System.out.print(0);
		for (int i = 1; i < information.size(); i++) {
			System.out.print(compare(i - 1, i));
			System.out.print(i);
		}
		System.out.println("");

		for (int i = 0; i < information.size() - 1; i++) {
			for (int j = i + 1; j < information.size(); j++) {
				String temp = compare(i, j);
				if (temp.equals(" -> "))
					System.out.println(i + compare(i, j) + j);
				else if (temp.equals(" <- ")) {
					System.out.println(j + " -> " + i);
				} else {
					System.out.println(i + compare(i, j) + j);
				}
			}
		}
		mutex.release();

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.println("Please enter configuration file name");
			System.out.print("VectorLogger>: ");
			String configFileName = in.next();
			try {
				ConfigFileParser configParser = new ConfigFileParser(configFileName, null);
				int port = configParser.getLoggerPort();
				startListenerThread(new ServerSocket(port));
				System.out.println("Log server is running at port "+port);
				break;
			} catch (Exception e) {
				System.out.println("Could not load config file. " + e);
			}
		}
		System.out.println("Available commands: print, exit");
		System.out.print("VectorLogger>: ");
		String command;
		while ((command = in.next()) != null) {
			if (command.equals("exit"))
				break;
			else if (command.equals("print"))
				printInformation();
			else
				System.out.println("Available commands: print, exit");
			System.out.print("VectorLogger>: ");
		}
		in.close();
		System.out.println("VectorLogger terminated normally");
		System.exit(0);
	}

}
