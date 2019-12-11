package com.sc.chatserver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

@ApplicationScoped
@Startup
public class Server {

	private static final int maxClientCount = 18;
	private static final int serverSocketPort = 65467;

	private static ServerSocket serverSocket = null;
	private Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

	private ExecutorService executorService;
	private ExecutorService socketClientsExecutor;
	private boolean started = false;

	private BlockingDeque<MessageFromClient> messages = new LinkedBlockingDeque<MessageFromClient>();

	private static final SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

	@PostConstruct
	public void init() {
		System.out.println("Initializing server");
		System.out.println("Creating server socket on " + serverSocketPort + " port....");

		try {
			serverSocket = new ServerSocket(serverSocketPort);
		} catch (IOException e) {
			System.err.println("Unable to create server at socket port " + serverSocketPort);
			e.printStackTrace();
		}

		if (serverSocket != null) {

			started = true;

			System.out.println("Server socket at port " + serverSocket + " started successfully!");
			executorService = Executors.newFixedThreadPool(3);
			executorService.submit(new ServerListener(this));
			executorService.submit(new Reaper());
			executorService.submit(new MessageDispatcher());

			socketClientsExecutor = Executors.newFixedThreadPool(maxClientCount);

		}

	}

	@PreDestroy
	public void shutdown() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println("Server socket cannot close");
				e.printStackTrace();
			}
		}

		if (executorService != null) {
			executorService.shutdown();
		}

		if (socketClientsExecutor != null) {
			socketClientsExecutor.shutdown();
		}
	}

	public void greetings(String name) {


		MessageFromClient greetinMessage = new MessageFromClient();

		greetinMessage.setText("Welcome " + name + " to our chat room! To send a message, just type it and hit enter. If yout want to send message to" +
				" specific user use @ symbol. If you want to quit, please, type :quit or :exit");
		greetinMessage.setFrom("ChatServer");
		greetinMessage.setTo(name);
		greetinMessage.setDatetime(new Date());
		offerFirstMessage(greetinMessage);
	}

	public void offerFirstMessage(MessageFromClient message) {
		try {
			while (!messages.offerFirst(message, 100L, TimeUnit.SECONDS)) {

			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void registerClient(Client client) {
		clients.put(client.getUsername(), client);
	}

	public class ServerListener implements Runnable {
		private Server server;

		public ServerListener(Server server) {
			this.server = server;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("ChatServerListener");
			System.out.println(Thread.currentThread().getName() + " started.");
			Socket socket;

			while (started) {
				try {
					socket = serverSocket.accept();

					if (socket != null) {
						System.out.println("Accepted new incoming connection to put socket");
						PrintStream printStream = new PrintStream(socket.getOutputStream());
						printStream.println("HELLO!");

						if (clients.size() < maxClientCount) {
							Client client = new Client();
							client.setId(UUID.randomUUID().toString());
							client.setSocket(socket);
							client.setServer(server);
							socketClientsExecutor.submit(client);
						} else {
							System.out.println("Max clients count reached! Sorry!");
							printStream.println("Dear user, please, try again later. Server is too busy. (((");
							socket.close();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public class MessageDispatcher implements Runnable {
		public void run() {
			Thread.currentThread().setName("MessageDispatcher");
			while (started) {
				try {
					MessageFromClient msg = messages.poll();
					if (msg == null) {
						msg = messages.poll(100, TimeUnit.MILLISECONDS);
					}

					if (msg != null) {
						String to = msg.getTo();
						if (to != null && !to.trim().isEmpty()) {
							Client client = clients.get(to);
							if (client != null) {
								if (client.isActive()) {
									client.getOs().println(sf.format(msg.getDatetime()) + " > " + msg.getFrom() + ": " + msg.getText());
								}
							}
						}
					} else {
						LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	public class Reaper implements Runnable {
		public void run() {
			Thread.currentThread().setName("Reaper");
			while (started) {
				for (Map.Entry<String, Client> entry : clients.entrySet()) {
					if (!entry.getValue().isActive()) {
						System.out.println("--- REAPER --- Removed " + entry.getKey());
						clients.remove(entry.getKey());
					}
				}
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5000));
			}
		}
	}


}
