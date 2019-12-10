package com.sc.chatserver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.ServerSocket;

@ApplicationScoped
@Startup
public class Server {

	private static final int maxClientCount = 18;
	private static final int serverSocketPort = 666;

	private static ServerSocket serverSocket = null;


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
	}
}
