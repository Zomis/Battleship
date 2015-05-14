package net.zomis.battleship

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Client {
	
	def socket = new Socket("stats.zomis.net", 7282);
	def writer = new PrintWriter(socket.outputStream);
	def name
	
	public Client() {
		
	}
	
	def listen(Closure<String> incoming) {
		send "USER xxx $name password"
		def input = socket.inputStream;
		def reader = new BufferedReader(new InputStreamReader(input));
		
		reader.lines().forEach(incoming); // {mess -> System.out.println name + " Incoming: " + mess})
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
	def listener = {mess -> System.out.println client.name + " Incoming: " + mess}
	new Thread({ client.listen(listener)}).start();
	
	scan.withCloseable({ scanner ->
		while (true) {
			def input = scanner.nextLine();
			client.send input
		}
	});
	
}

new Thread({ console(client, scan) }).start();
