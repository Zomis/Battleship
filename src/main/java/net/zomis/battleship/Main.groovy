package net.zomis.battleship

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Client {
	
	def socket = new Socket("localhost", 7282);
	def writer = new PrintWriter(socket.outputStream);

	public Client() {
		new Thread({ console() }).start();
	}
	
	def console() {
		def scan = new Scanner(System.in);
		
		print "Enter your name: "
		def name = scan.nextLine()
		new Thread({ listen(name) }).start();
		
		scan.withCloseable({ scanner ->
			while (true) {
				def input = scanner.nextLine();
				send input
//				println input
			}
		});
		
	}
	
	def listen(name) {
		
		send "USER xxx $name password"
		
		
		def input = socket.inputStream;
		def reader = new BufferedReader(new InputStreamReader(input));
		
		reader.lines().forEach({mess -> System.out.println mess})
	}
	
	def send(message) {
		System.out.println "Send " + message
		writer.print(message + (char) 0)
		writer.flush()
	}
}

def client = new Client()



