package net.zomis.battleship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class JavaMain {

	private static Socket socket;
	private static PrintWriter printer;

	public static void main(String[] args) throws UnknownHostException, IOException {
		socket = new Socket("localhost", 7282);
		
		OutputStream out = socket.getOutputStream();
		printer = new PrintWriter(out);
		write("USER Bubu2 Bwakkit");
		new Thread(JavaMain::receive).start();
	}
	
	private static void write(String string) {
		printer.print(string + (char) 0);
		printer.flush();
	}

	private static void receive() {
		InputStream in;
		try {
			in = socket.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		while (true) {
			reader.lines().forEach(System.out::println);
		}
	}
	
}
