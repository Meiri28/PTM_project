package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import test.Commands.DefaultIO;
import test.Server.ClientHandler;

public class AnomalyDetectionHandler implements ClientHandler{
	

	public class SocketIO implements DefaultIO{
		private BufferedReader Input;
		private PrintWriter Output;
		
		public SocketIO(Socket communicationSocket) {
				try {
					Input = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
					Output = new PrintWriter(communicationSocket.getOutputStream());
				} catch (IOException e) {
									
				}
		}

		@Override
		public String readText() {
			try {
				return Input.readLine();
			} catch (IOException e) {
				
			}
			return null;
		}

		@Override
		public void write(String text) {
			Output.write(text);
			Output.flush();
		}

		@Override
		public float readVal() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void write(float val) {
			Output.write((int) val);
			Output.flush();
		}

	}


}
