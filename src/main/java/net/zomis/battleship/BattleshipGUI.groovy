package net.zomis.battleship

import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import static javax.swing.JFrame.EXIT_ON_CLOSE
import java.awt.*
import javax.swing.JButton

@Bindable
class Address {
	String name, chatSend
	String toString() {
		"address[name=$name]"
	}
	Client client
}

def address = new Address(name: '')

def swingBuilder = new SwingBuilder()
swingBuilder.edt {
	// edt method makes sure UI is build on Event Dispatch Thread.
	lookAndFeel 'nimbus'  // Simple change in look and feel.
	frame(title: 'Address', size: [350, 230],
	show: true, locationRelativeTo: null,
	defaultCloseOperation: EXIT_ON_CLOSE) {
		borderLayout(vgap: 5)
		
		panel(constraints: BorderLayout.NORTH, id: 'namePanel',
		border: compoundBorder([emptyBorder(10), titledBorder('Enter your name:')])) {
			tableLayout {
				tr {
					td {
						textField address.name, id: 'name', columns: 20
					}
				}
				tr {
					td {
						button text: 'Save', actionPerformed: {
							if (address.name.contains(" ")) {
								return;
							}
							namePanel.visible = false
							lobbyPanel.visible = true
							address.client = new Client(name: address.name);
							new Thread({ address.client.listen() }).start();
							
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
					address.client.send "CHAT 0 " + address.chatSend
				}
			} 
		}
		// Binding of textfield's to address object.
		bean address,
			name: bind { name.text },
			chatSend: bind { chatSend.text }
	}

}
