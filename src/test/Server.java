package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import test.AnomalyDetectionHandler.SocketIO;

public class Server {
	
	ServerSocket serverSocket;

	public interface ClientHandler{
		
	}

	volatile boolean stop;
	public Server() {
		stop=false;
	}
	
	
	private void startServer(int port, ClientHandler ch) {
		try {
			serverSocket = new ServerSocket(port);
			Socket aclient = serverSocket.accept();
			AnomalyDetectionHandler x = new AnomalyDetectionHandler();
			SocketIO s = x.new SocketIO(aclient);
			CLI clientCli = new CLI(s);
			clientCli.start();
			s.write("bye");
			aclient.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// runs the server in its own thread
	public void start(int port, ClientHandler ch) {
		new Thread(()->startServer(port,ch)).start();
	}
	
	public void stop() {
		stop=true;
	}
}
