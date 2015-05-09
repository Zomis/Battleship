package net.zomis.battleship

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Client {
	
	def socket = new Socket("localhost", 7282);
	def writer = new PrintWriter(socket.outputStream);
	def name
	
	public Client() {
		
	}
	
	def listen() {
		send "USER xxx $name password"
		def input = socket.inputStream;
		def reader = new BufferedReader(new InputStreamReader(input));
		
		reader.lines().forEach({mess -> System.out.println name + " Incoming: " + mess})
	}
	
	def send(message) {
		System.out.println name + " Send: " + message
		writer.print(message + (char) 0)
		writer.flush()
	}
}

def scan = new Scanner(System.in);
print "Enter your name: "
def name = scan.nextLine()

def client = new Client(name: name)

def console(Client client, Scanner scan) {
	
	new Thread({ client.listen() }).start();
	
	scan.withCloseable({ scanner ->
		while (true) {
			def input = scanner.nextLine();
			client.send input
//				println input
		}
	});
	
}

new Thread({ console(client) }).start();
