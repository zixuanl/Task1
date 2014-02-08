package edu.cmu.ds.messagepasser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import edu.cmu.ds.messagepasser.clock.ClockService;
import edu.cmu.ds.messagepasser.clock.LogicalClock;
import edu.cmu.ds.messagepasser.clock.VectorClock;
import edu.cmu.ds.messagepasser.model.Message;
import edu.cmu.ds.messagepasser.model.Node;
import edu.cmu.ds.messagepasser.model.Rule;
import edu.cmu.ds.messagepasser.model.TimeStampedMessage;

public class MessagePasser {
	private static final String COMMAND_PROMPT = ">: ";
	private String configurationFileName;
	private String localName;
	private AtomicInteger sequenceNumber = new AtomicInteger(-1);
	private ConcurrentLinkedQueue<Message> receiveBuffer = new ConcurrentLinkedQueue<Message>();
	private ConcurrentLinkedQueue<Message> receiveDelayedBuffer = new ConcurrentLinkedQueue<Message>();
	private ConcurrentLinkedQueue<Message> sendDelayedBuffer = new ConcurrentLinkedQueue<Message>();
	private ArrayList<Rule> receiveRuleList;
	private ArrayList<Rule> sendRuleList;
	private ArrayList<Node> peerNodeList;
	private ServerSocket listenerSocket;
	private Map<String, Socket> nodeNameSocketPool = new HashMap<String, Socket>();
	private Map<String, ObjectOutputStream> nodeNameOutputPool = new HashMap<String, ObjectOutputStream>();
	private boolean willTerminate = false;
	private boolean isLogicalClock;
	private Integer localNodeIndex;
	private String loggerIp = null;
	private Integer loggerPort = null;
	private ClockService clockService = null;

	public MessagePasser(String configuration_filename, String local_name) throws FileNotFoundException {
		this.configurationFileName = configuration_filename;
		this.localName = local_name;

		ConfigFileParser parser;
		parser = new ConfigFileParser(this.configurationFileName, this.localName);
		this.peerNodeList = parser.getPeerNodes();
		this.receiveRuleList = parser.getReceiveRules();
		this.sendRuleList = parser.getSendRules();
		this.loggerIp = parser.getLoggerIp();
		this.loggerPort = parser.getLoggerPort();
		this.localNodeIndex = parser.getLocalNodeIndex();

		setClockService(isLogicalClock, (peerNodeList.size() + 1), localNodeIndex);
		try {
			this.listenerSocket = new ServerSocket(parser.getLocalNode().getPort());
			startListenerThread(); // setUp the initial connection
			startMessageReceiverThread(); // create receive
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Print all MessagePasser's information
	 */
	public void printInfo() {
		System.out.println("Local name is " + localName);
		System.out.println("Total number of node is " + (this.peerNodeList.size() + 1));
		System.out.println("Local node index is " + localNodeIndex);
		if (isLogicalClock)
			System.out.println("Using logical clock");
		else
			System.out.println("Using vector clock");
	}

	public void setIsLogicalClock(boolean isLogicalClock) {
		this.isLogicalClock = isLogicalClock;
	}

	/**
	 * Initiate the selected clock service
	 * @param isLogical True if you want to use a logical clock. False if vector.
	 * @param numberOfNodes Number of nodes (for vector clock only)
	 * @param localNodeIndex Local node's index (for vector clock only)
	 */
	public void setClockService(boolean isLogical, int numberOfNodes, int localNodeIndex) {
		if (isLogical == true) {
			clockService = new LogicalClock();
		} else {
			clockService = new VectorClock(numberOfNodes, localNodeIndex);
		}
	}

	/**
	 * Send a message to the logger but not increment time stamp (for used by send() and receive() only)
	 * @param message
	 * @throws IOException
	 */
	private void sendLog(Message message) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(loggerIp, loggerPort);
		} catch (ConnectException e) {
			System.out.println("Couldn't connect to Logger server. Log info lost");
			return;
		}
		ObjectOutputStream ot = new ObjectOutputStream(socket.getOutputStream());
		ot.writeObject(message);
		ot.flush();
		ot.close();
		socket.close();
	}

	/**
	 * Increment local timestamp and send a message only to the logger
	 * @param message
	 * @throws IOException
	 */
	public void mark(Message message) throws IOException {
		if (isLogicalClock == true) {
			((TimeStampedMessage) message).setTimeStamp(clockService.getIncTimeStamp());
			System.out.println(clockService.getTimeStamp());
		} else {

			((TimeStampedMessage) message).setTimeStamp(clockService.getIncTimeStamp());
			@SuppressWarnings("unchecked")
			ArrayList<Integer> tmp = (ArrayList<Integer>) clockService.getTimeStamp();

			System.out.print(localName + ": current time stampe is ( ");
			for (int i = 0; i < tmp.size(); i++) {
				System.out.print(tmp.get(i) + " ");
			}
			System.out.println(")");

		}

		Socket socket = null;
		try {
			socket = new Socket(loggerIp, 3333);
		} catch (ConnectException e) {
			System.out.println("Can not connect to Log server. Log info lost");
			return;
		}
		ObjectOutputStream ot = new ObjectOutputStream(socket.getOutputStream());
		ot.writeObject(message);
		ot.flush();
		ot.close();
		socket.close();

	}

	/** 
	 * Send a message to destination
	 * @param message
	 * @param index
	 * @throws IOException
	 */
	public void send(Message message, int index) throws IOException {

		if (message instanceof TimeStampedMessage) {
			if (isLogicalClock == true) {
				((TimeStampedMessage) message).setTimeStamp(clockService.getIncTimeStamp());
				System.out.println(clockService.getTimeStamp());
			} else {

				((TimeStampedMessage) message).setTimeStamp(clockService.getIncTimeStamp());
				@SuppressWarnings("unchecked")
				ArrayList<Integer> tmp = (ArrayList<Integer>) clockService.getTimeStamp();

				System.out.print(localName + ": current time stampe is ( ");
				for (int i = 0; i < tmp.size(); i++) {
					System.out.print(tmp.get(i) + " ");
				}
				System.out.println(")");

			}
		}

		ObjectOutputStream ot;
		Socket socket;
		boolean isDupe = false;
		try {
			if (!nodeNameOutputPool.containsKey(message.getDestination())) {
				socket = new Socket(peerNodeList.get(index).getIp(), peerNodeList.get(index)
						.getPort().intValue());
				ObjectOutputStream ot_temp = new ObjectOutputStream(socket.getOutputStream());
				nodeNameSocketPool.put(message.getDestination(), socket);
				nodeNameOutputPool.put(message.getDestination(), ot_temp);
				System.out.println("Connect to " + message.getDestination() + " is established");
			}
		} catch (ConnectException e) {
			System.out.println(message.getDestination() + " is not online!");
			return;
		}
		ot = nodeNameOutputPool.get(message.getDestination());

		message.setSource(localName);
		message.setSequenceNumber(sequenceNumber.addAndGet(1));
		Rule r = checkSendRule(message);
		if (r != null) {
			String action = new String(r.getAction());
			if (action.equals("drop")) {
				System.out.println("Message has been dropped in send side");
				return;
			}
			if (action.equals("duplicate")) {

				System.out.println("Message has been duped in send side");
				isDupe = true;

			}
			if (action.equals("delay")) {
				System.out.println("Message has been delayed in send side");
				sendDelayedBuffer.add(message);
				return;
			}
		}

		try {
			ot.writeObject(message);
			ot.flush();
			if (isDupe == true) {
				Message dup_message = new Message(message);
				dup_message.setIsDuplicate(true);
				ot.writeObject(dup_message);
				ot.flush();
				isDupe = false;
			}

		} catch (SocketException e) {

			nodeNameSocketPool.remove(message.getDestination());
			nodeNameOutputPool.remove(message.getDestination());
			System.out.println(message.getDestination() + " is not online!");

		}

		while (!sendDelayedBuffer.isEmpty()) {
			int index_delay = -1;
			ObjectOutputStream ot2;
			Message new_message = new Message(sendDelayedBuffer.poll());

			for (int i = 0; i < peerNodeList.size(); i++) {
				if (peerNodeList.get(i).getName().equals(new_message.getDestination())) {
					index_delay = i;
				}
			}
			if (index_delay == -1) {
				System.out.println("Dest is not in the established node list");
				return;
			}
			try {
				if (!nodeNameOutputPool.containsKey(new_message.getDestination())) {
					socket = new Socket(peerNodeList.get(index_delay).getIp(), peerNodeList
							.get(index_delay).getPort().intValue());
					ObjectOutputStream ot_temp = new ObjectOutputStream(socket.getOutputStream());
					nodeNameSocketPool.put(new_message.getDestination(), socket);
					nodeNameOutputPool.put(new_message.getDestination(), ot_temp);
					System.out.println("Connect to " + new_message.getDestination()
							+ " is established");
				}
			} catch (ConnectException e) {
				System.out.println(new_message.getDestination() + " is not online!");
				return;
			}
			ot2 = nodeNameOutputPool.get(new_message.getDestination());
			ot2.writeObject(new_message);
			ot2.flush();

		}

	}

	/**
	 * Get a message from receiveBuffer and print it
	 */
	public void receive() {

		if (receiveBuffer.peek() != null) {
			System.out.println("");
			Message message = receiveBuffer.poll();
			if (message instanceof TimeStampedMessage) {
				if (isLogicalClock == true) {
					clockService.updateTime(((TimeStampedMessage) message).getTimeStamp());
					System.out.println(clockService.getTimeStamp());
				} else {
					clockService.updateTime(((TimeStampedMessage) message).getTimeStamp());
					@SuppressWarnings("unchecked")
					ArrayList<Integer> tmp = (ArrayList<Integer>) clockService.getTimeStamp();
					System.out.print(localName + ": current time stampe is ( ");
					for (int i = 0; i < tmp.size(); i++) {
						System.out.print(tmp.get(i) + " ");
					}
					System.out.println(")");
				}
			}

			System.out.println("Received message from " + message.getSource());
			System.out.println("Message seqNum is " + message.getSequenceNumber());
			System.out.println("Message dup is  " + message.getIsDuplicate());
			System.out.println("Message kind is " + message.getKind());
			System.out.println("Message body is " + (String) (message.getData()));
			System.out.print(COMMAND_PROMPT);
		}
	}
	
	/**
	 * Get an index number of a node with a specified name.
	 * @param nodeName A name of the node
	 * @return Index of the node. Null if the node with that name doesn't exist.
	 */
	public Integer getNodeIndex(String nodeName) {
		for (int i = 0; i < peerNodeList.size(); i++) {
			Node peerNode = peerNodeList.get(i);
			if (peerNode.getName().equals(nodeName)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Start a thread that monitors for incoming data from a MessagePasser
	 * @param socket
	 * @throws IOException
	 */
	private void startClientThread(final Socket socket) throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
					Message message;
					boolean isDuplicate = false;
					while (!willTerminate) {
						message = (Message) is.readObject();
						if (message == null)
							continue;
						Rule rule = checkReceiveRule(message);
						if (rule != null) {
							String action = new String(rule.getAction());
							if (action.equals("drop")) {
								System.out.println("Message has been dropped in receive side");
								System.out.print(COMMAND_PROMPT);
								continue;
							}
							if (action.equals("duplicate")) {
								isDuplicate = true;
								System.out.println("Message has been duped in receive side");
								System.out.print(COMMAND_PROMPT);
							}
							if (action.equals("delay")) {
								System.out.println("Message has been delayed in receive side");
								System.out.print(COMMAND_PROMPT);
								receiveDelayedBuffer.add(message);
								continue;
							}
						}
						receiveBuffer.add(message);
						if (isDuplicate == true) {
							Message dup_message = new Message(message);
							dup_message.setIsDuplicate(true);
							receiveBuffer.add(dup_message);
							isDuplicate = false;
						}
						while (!receiveDelayedBuffer.isEmpty()) {
							receiveBuffer.add(receiveDelayedBuffer.poll());
						}
					}
				} catch (Exception e) {
					return;
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
					}
				}
			}
		}).start();
	}

	/**
	 * Start a thread that keeps listening to incoming connections from
	 * MessagePassers
	 */
	private void startListenerThread() {
		new Thread(new Runnable() {
			public void run() {
				System.out.println("Local server is listening on port "
						+ listenerSocket.getLocalPort());
				try {
					while (!willTerminate) {
						Socket socket = listenerSocket.accept();
						startClientThread(socket);
					}
				} catch (IOException e) {
				} finally {
					try {
						listenerSocket.close();
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}

	/**
	 * Start a thread that monitors receiveBuffer and print messages in it
	 * 
	 * @throws IOException
	 */
	private void startMessageReceiverThread() throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					while (!willTerminate) {
						receive();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Check whether the sending message conforms with a send rule
	 * 
	 * @param message
	 *            Sending message
	 * @return A rule to apply
	 */
	private Rule checkSendRule(Message message) {
		try {
			ConfigFileParser p = new ConfigFileParser(configurationFileName, localName);
			sendRuleList = p.getSendRules();
			for (int i = 0; i < sendRuleList.size(); i++) {
				if (sendRuleList.get(i).matches(message))
					return sendRuleList.get(i);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Check whether the receiving message conforms with a receive rule
	 * 
	 * @param message
	 *            Receiving message
	 * @return A rule to apply
	 */
	private Rule checkReceiveRule(Message message) {
		try {
			ConfigFileParser p = new ConfigFileParser(configurationFileName, localName);
			receiveRuleList = p.getReceiveRules();
			for (int i = 0; i < receiveRuleList.size(); i++) {
				if (receiveRuleList.get(i).matches(message))
					return receiveRuleList.get(i);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Terminate MessagePasser This will set a terminate flag that is constantly
	 * monitored by every thread.
	 */
	public void terminate() {
		willTerminate = true;
	}

	public static void main(String[] args) throws Exception {
		InputStreamReader reader = new InputStreamReader(System.in);
		BufferedReader input = new BufferedReader(reader);
		
		System.out.println("Please enter the local host name");
		System.out.print(COMMAND_PROMPT);
		String localName = input.readLine();
		
		MessagePasser messagePasser;
		while (true) {
			try {
				System.out.println("Please enter the configuration file name");
				System.out.print(COMMAND_PROMPT);
				String configurationFileName = input.readLine();
				// Create a MessagePasser instance
				messagePasser = new MessagePasser(configurationFileName, localName);
				break;
			} catch (FileNotFoundException e) {
				System.out.println("Configuration file not found.");
			}
		}

		String clockType;
		while (true) {
			System.out.println("Please choose the clock type between 'logical' or 'vector'");
			System.out.print(COMMAND_PROMPT);
			clockType = input.readLine();
			if ("logical".equals(clockType)) {
				messagePasser.setIsLogicalClock(true);
				break;
			} else if ("vector".equals(clockType)) {
				messagePasser.setIsLogicalClock(false);
				break;
			}
		}
		
		messagePasser.printInfo();
		System.out.println("Please enter 'send' or 'exit' or 'mark'");
		System.out.print(COMMAND_PROMPT);
		String command;
		while ((command = input.readLine()) != null) {
			if (command.equals("exit")) {
				/*
				 * Exit
				 */
				break;
			} else if (command.equals("send")) {
				/*
				 * Send
				 */
				// Retrieve message destination and kind
				String[] sendInfo;
				do {
					System.out.println("Please specify the message: <destination> <kind>");
					System.out.print(COMMAND_PROMPT);
					sendInfo = input.readLine().split(" ");
				} while (sendInfo.length != 2);
				String destination = sendInfo[0];
				String kind = sendInfo[1];

				// Retrieve message body
				System.out.println("Please enter the message body");
				System.out.print(COMMAND_PROMPT);
				String messageBody = input.readLine();
				
				// Check if the user wants to log
				String logInfo;
				do {
					System.out.println("Do you want log this message? (y/n)");
					System.out.print(COMMAND_PROMPT);
					logInfo = input.readLine();
				} while (!logInfo.equals("y") && !logInfo.equals("n"));
				boolean mustLog = (logInfo.toLowerCase().equals("y"));

				// Check destination
				Integer nodeIndex = messagePasser.getNodeIndex(destination);
				if (nodeIndex == null) {
					System.out.println("Invalid destination");
				} else {
					// Create and send a time stamped message
					Message message = new TimeStampedMessage(destination, kind, messageBody);
					messagePasser.send(message, nodeIndex);
					if (mustLog) {
						messagePasser.sendLog(message);
					}
				}
			} else if (command.equals("mark")) {
				/*
				 * Mark
				 */
				Message markMessage = new TimeStampedMessage("logger", "log", "This is a mark.");
				markMessage.setSource(localName);
				markMessage.setSequenceNumber(Integer.MAX_VALUE); // We don't care this.
				messagePasser.mark(markMessage);
			}
			
			System.out.println("Please enter 'send' or 'exit' or 'mark'");
			System.out.print(COMMAND_PROMPT);
		}
		input.close();
		messagePasser.terminate();
		System.out.println("Program exited normally");
	}

}
