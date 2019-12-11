package com.sc.chatserver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Startup
public class Server {

	private static final int maxClientCount = 18;
	private static final int serverSocketPort = 65463;

	private static ServerSocket serverSocket = null;
	private Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

	private ExecutorService executorService;

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
			System.out.println("Server socket at port " + serverSocket + " started successfully!");
			executorService = Executors.newFixedThreadPool(3);
			executorService.submit(new ServerListener());
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

		if(executorService != null) {
			executorService.shutdown();
		}
	}

	public class ServerListener implements Runnable {

		@Override
		public void run() {
			Thread.currentThread().setName("ChatServerListener");
			System.out.println(Thread.currentThread().getName() + " started.");
			Socket socket;

			while (true) {
				try {
					socket = serverSocket.accept();

					if (socket != null) {
						System.out.println("Accepted new incoming connection to put socket");
						PrintStream printStream = new PrintStream(socket.getOutputStream());
						printStream.println("HELLO!");

						if (clients.size() < maxClientCount) {

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


}
