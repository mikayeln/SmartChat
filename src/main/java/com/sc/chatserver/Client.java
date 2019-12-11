package com.sc.chatserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Client implements Runnable {
	private String id;
	private String username;
	private Socket socket;

	private DataInputStream is = null;
	private PrintStream os = null;

	private boolean isActive;

	private Server server;

	public Client() {

	}

	public Client(String id, String username) {
		this.id = id;
		this.username = username;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public DataInputStream getIs() {
		return is;
	}

	public void setIs(DataInputStream is) {
		this.is = is;
	}

	public PrintStream getOs() {
		return os;
	}

	public void setOs(PrintStream os) {
		this.os = os;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("Client thread - " + id);
		System.out.println(Thread.currentThread().getName() + " started!");
		try {
			is = new DataInputStream(socket.getInputStream());
			os = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Unable to get input and/or output for client. Stopping thread");
			e.printStackTrace(System.err);
		}

		if (is != null && os != null) {
			isActive = true;

			while (isActive) {
				try {
					os.println("Hello from chat server and welcome! What is your name? Please, do not use @ symbol in your name.");
					username = is.readLine();
					if (username != null) {
						if (username.contains("@")) {
							os.println("As I asked before, please, DO NOT USE @ symbol in your name.");
						} else {
							break;
						}
					}
					LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
				} catch (Exception e) {
					System.err.println("Unable to get name of client.");
					e.printStackTrace(System.err);

					isActive = false;
					return;
				}
			}

			server.registerClient(this);
			server.greetings(username);

			int failCounter = 0;

			while (isActive) {
				try {
					String msg = is.readLine().trim();
					failCounter = 10;
					if (msg.equals(":quit") || msg.equals(":exit")) {
						os.println("Bye-bye, " + username + "!");
						isActive = false;
					} else {
						System.out.println("Recieved message from @" + username + ":" + msg);

						if (msg.startsWith("@")) {
							String to = msg.substring(1, msg.indexOf(" "));
							String text = msg.substring(msg.indexOf(" ")).trim();

							MessageFromClient newMessageFromClient = new MessageFromClient();
							newMessageFromClient.setDatetime(new Date());
							newMessageFromClient.setFrom(username);
							newMessageFromClient.setTo(to);
							newMessageFromClient.setText(text);


							server.offerFirstMessage(newMessageFromClient);
						}
					}
				} catch (Exception e) {
					System.err.println("Unable to read message from @" + username);
					e.printStackTrace(System.err);
					LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
					failCounter--;
				}

				if (failCounter < 0) {
					isActive = false;
				}
			}

			if (socket != null) {
				try {
					os.close();
					is.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}


		}
	}
}
