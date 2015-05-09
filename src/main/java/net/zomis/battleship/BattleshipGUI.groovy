package net.zomis.battleship

import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import static javax.swing.JFrame.EXIT_ON_CLOSE

import java.awt.*

import javax.swing.ActionPropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel
import javax.swing.JButton

class Ship {
	String name
	int width
	int height
}

@Bindable
class Address {
	String name, chatSend
	int gameid
	int playerIndex
	String toString() {
		"address[name=$name]"
	}
}

def address = new Address(name: '')

def swingBuilder = new SwingBuilder()
swingBuilder.edt {
	// edt method makes sure UI is build on Event Dispatch Thread.
	lookAndFeel 'nimbus'  // Simple change in look and feel.
	frame(title: 'Battleship', size: [800, 480],
	show: true, locationRelativeTo: null,
	defaultCloseOperation: EXIT_ON_CLOSE) {
		Client client
		def listener = {String mess ->
			println address.name + " Incoming: " + mess
			def arr = mess.split " "
			if (mess.startsWith("CHAT")) {
				chat.append(mess + "\n")
			}
			if (mess.startsWith("INVT")) {
				def inviteID = arr[1]
				if (!arr[2].equals("Battleship")) {
					println "Unsupported type: " + arr[2]
					client.send "INVN $inviteID"
					return;
				}
				def userName = arr[2]
				def sb = new SwingBuilder()
				def pane = sb.optionPane(message: 'Welcome to the wonderful world of GroovySwing',
					options: ['Accept', 'Decline'])
				def dialog = pane.createDialog(null, 'You got an invite!')
				dialog.visible = true
				println "Dialog result: " + pane.value
				if (pane.value.equals("Accept")) {
					client.send "INVY $inviteID"
				} else {
					client.send "INVN $inviteID"
				}
			}
			if (mess.startsWith("STUS")) {
				boolean online = arr[2].equals("online")
				def username = arr[1]
				println "$username is $online"
				def model = userList.model
				if (online) {
					model.addElement arr[1]
				} else {
					model.removeElement arr[1]
				}
			}
			if (mess.startsWith("NEWG")) {
				address.gameid = Integer.parseInt arr[1]
				address.playerIndex = Integer.parseInt arr[2]
			}
			if (mess.startsWith("CONF")) {
				// CONF 0 10 10 Air_Carrier 5 1 Battleship 4 1 Submarine 3 1 Submarine 3 1 Patrol 2 1
				def shipArr = Arrays.copyOf(arr, 4, arr.length);
				
			}
		}
	
		borderLayout(vgap: 5)
		
		panel(constraints: BorderLayout.NORTH, id: 'namePanel',
		border: compoundBorder([emptyBorder(10), titledBorder('Enter your name:')])) {
			tableLayout {
				tr {
					td {
						textField address.name, id: 'name', columns: 20
					}
					td {
					}
				}
				tr {
					td {
						button text: 'Test', actionPerformed: {
						}
					}
					td {
						button text: 'Save', actionPerformed: {
							if (address.name.contains(" ")) {
								return;
							}
							namePanel.visible = false
							lobbyPanel.visible = true
							client = new Client(name: address.name);
							new Thread({ client.listen(listener) }).start();
							
/*							panelGrid.layout.rows = 10
							(1..100).each{
								panelGrid.add(new JButton("" + it))
							}
							panelGrid.revalidate()*/
						}
					}
				}
			}
		}
		panel(constraints: BorderLayout.CENTER, id: 'panelGrid', visible: false) {
			gridLayout { }
		}
		panel(constraints: BorderLayout.SOUTH, id: 'lobbyPanel', visible: false) {
			borderLayout()
			panel(constraints: BorderLayout.CENTER) {
				textArea id: 'chat', rows: 20, columns: 50
			}
			panel(constraints: BorderLayout.SOUTH) {
				textField address.chatSend, id: 'chatSend', columns: 20
				button text: 'Send', actionPerformed: {
					if (address.chatSend.trim().equals("")) {
						return;
					}
					client.send "CHAT 0 " + address.chatSend
				}
			}
			panel(constraints: BorderLayout.EAST) {
				boxLayout(axis: BoxLayout.Y_AXIS)
				
				list id: 'userList', model: new DefaultListModel<String>()
				button 'Invite', actionPerformed: {
					client.send "INVT Battleship " + userList.selectedValue
				}
			}
		}
		// Binding of textfield's to address object.
		bean address,
			name: bind { name.text },
			chatSend: bind { chatSend.text }
	}

}
