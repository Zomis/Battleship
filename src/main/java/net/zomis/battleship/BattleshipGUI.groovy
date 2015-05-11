package net.zomis.battleship

import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import static javax.swing.JFrame.EXIT_ON_CLOSE

import java.util.List
import java.awt.*

import javax.swing.ActionPropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel
import javax.swing.JButton

class Ship {
	GameBoard board
	String name
	int width
	int height
	int x = -1
	int y = -1
	
	private void flip() {
		println "$this: Flip"
		int temp = this.width
		this.width = this.height
		this.height = temp
	}
	
	private void removeFromBoard(JButton[][] buttons) {
		println "$this: Remove from board"
		for (int yy = y; yy < y + height; yy++) {
			for (int xx = x; xx < x + width; xx++) {
				if (board.inRange(xx, yy) && board.ships[yy][xx] == this) {
					board.ships[yy][xx] = null
					buttons[yy][xx].text = ""
				}
			}
		}
	}
	
	private void placeAtBoard(JButton[][] buttons, int x, int y) {
		println "$this: Place at board $x, $y"
		this.x = x
		this.y = y
		for (int yy = y; yy < y + height; yy++) {
			for (int xx = x; xx < x + width; xx++) {
				board.ships[yy][xx] = this
				buttons[yy][xx].text = name.charAt(0)
			}
		}
	}
	
	private boolean canPlaceAtBoard(int x, int y) {
		for (int yy = y; yy < y + height; yy++) {
			for (int xx = x; xx < x + width; xx++) {
				if (!board.inRange(xx, yy)) {
					println "Board: $xx $yy is not in range"
					return false
				}
				if (board.ships[yy][xx]) {
					println "Board: $xx $yy contains a ship: ${board.ships[yy][xx]}"
					return false
				}
			}
		}
		return true
	}
	
	void position(JButton[][] buttons, int x, int y) {
		boolean flipIt = board.ships[y][x] == this && this.x == x && this.y == y
		this.removeFromBoard(buttons)
		if (flipIt) {
			this.flip()
		}
		
		if (this.canPlaceAtBoard(x, y)) {
			placeAtBoard(buttons, x, y)
		} else {
			this.flip()
			if (this.canPlaceAtBoard(x, y)) {
				placeAtBoard(buttons, x, y)
			} else {
				println "$this: Cannot place at $x, $y"
			}
		}
	}
	
	boolean isValid() {
		return board.inRange(x, y) && board.inRange(x + width - 1, y + height - 1)
	}
	
	String confString() {
		return "$name $width $height $x $y "
	}
	
	@Override
	public String toString() {
		return confString();
	}
}

class GameBoard {
	Ship[][] ships
	List<Ship> placedShips = []
	int width
	int height
	
	public GameBoard(int width, int height) {
		this.ships = new Ship[height][width]
		this.width = width
		this.height = height
	}
	
	public boolean inRange(int x, int y) {
		return x >= 0 && y >= 0 && x < width && y < height
	}
	
}

@Bindable
class GameData {
	String name, chatSend
	int gameid
	int playerIndex, currentPlayer
	int gameWidth, gameHeight
	GameBoard myBoard
	
	boolean isMyTurn() {
		return playerIndex == currentPlayer
	}
}

GameData gameData = new GameData(name: '')


def swingBuilder = new SwingBuilder()

swingBuilder.edt {
	// edt method makes sure UI is build on Event Dispatch Thread.
	lookAndFeel 'nimbus'  // Simple change in look and feel.
	frame(title: 'Battleship', size: [800, 480],
	show: true, locationRelativeTo: null,
	defaultCloseOperation: EXIT_ON_CLOSE) {
		Client client
		Ship currentShip
		boolean flip = false
		JButton[][] buttons
		JButton[][] opponentButtons
		List<Ship> shipsToPlace = []
		int placedShips = 0
			
		def showGame = {int width, int height ->
			panelGrid.layout.rows = height
			opponentGrid.layout.rows = height
			
			buttons = new JButton[height][width]
			opponentButtons = new JButton[height][width]
			(0..width*height - 1).each {
				JButton button = new JButton(".");
				int x = it % width
				int y = it / width
				opponentButtons[y][x] = button
				button.addActionListener({
					if (gameData.isMyTurn()) {
						// make a guess
						def gameid = gameData.gameid
						client.send("MOVE $gameid PLAY $x $y")
					}
				});
				opponentGrid.add(button)
			}
			(0..width*height - 1).each {
				JButton button = new JButton("_");
				int x = it % width
				int y = it / width
				buttons[y][x] = button
				button.addActionListener({
					if (currentShip) {
						// position ship
						currentShip.position(buttons, x, y)
					}
				});
				panelGrid.add(button)
			}
			lobbyPanel.visible = false
			gamePanel.visible = true
			gamePanel.revalidate()
			gameData.gameWidth = width
			gameData.gameHeight = height
		}
		
		def listener = {String mess ->
			println gameData.name + " Incoming: " + mess
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
				def userName = arr[3]
				def sb = new SwingBuilder()
				def pane = sb.optionPane(message: "$userName wants to play with you",
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
				gameData.gameid = Integer.parseInt arr[1]
				gameData.playerIndex = Integer.parseInt arr[2]
			}
			if (mess.startsWith("CONF")) {
				// example: CONF 0 10 10 Air_Carrier 5 1 Battleship 4 1 Submarine 3 1 Submarine 3 1 Patrol 2 1
				int width = Integer.parseInt arr[2]
				int height = Integer.parseInt arr[3]
				gameData.myBoard = new GameBoard(width, height)
				for (int i = 4; i < arr.length; i += 3) {
					shipsToPlace.add new Ship(board: gameData.myBoard, name: arr[i], 
						width: Integer.parseInt(arr[i + 1]), height: Integer.parseInt(arr[i + 2]))
				}
				placedShips = 0
				currentShip = shipsToPlace[0]
				showGame(width, height)
				placeShipButton.visible = true
			}
			if (mess.startsWith("MOVE")) {
				if (arr[2].equals("PLAY")) {
					int player = Integer.parseInt arr[5]
					JButton[][] buttonGrid = player == gameData.playerIndex ? opponentButtons : buttons;
					
					int x = Integer.parseInt arr[3]
					int y = Integer.parseInt arr[4]
					JButton button = buttonGrid[y][x];
					boolean hit = arr[6].equals("HIT")
					button.text = hit ? "X" : "."
					button.enabled = false
				}
				if (arr[2].equals("TURN")) {
					int player = Integer.parseInt arr[3]
					gameData.currentPlayer = player
				}
			}
		}
	
		borderLayout(vgap: 5)
		
		panel(constraints: BorderLayout.NORTH, id: 'namePanel',
		border: compoundBorder([emptyBorder(10), titledBorder('Enter your name:')])) {
			vbox {
				textField gameData.name, id: 'name', columns: 20
				button id: 'connectButton', text: 'Save', actionPerformed: {
					if (gameData.name.contains(" ")) {
						return;
					}
					lobbyPanel.visible = true
					client = new Client(name: gameData.name);
					new Thread({ client.listen(listener) }).start();
					name.enabled = false
					connectButton.visible = false
					
				}
				button id: 'placeShipButton', visible: false, text: 'Apply', actionPerformed: {
					if (!shipsToPlace.empty) {
						// place a ship
						if (currentShip.isValid()) {
							placedShips++
							if (placedShips < shipsToPlace.size()) {
								currentShip = shipsToPlace[placedShips]
							} else {
								currentShip = null
								str = new StringBuilder("MOVE ${gameData.gameid} SHIP ")
								for (Ship ship in shipsToPlace) {
									str.append(ship.confString())
								}
								client.send(str.toString().trim())
								shipsToPlace.clear()
								placeShipButton.visible = false
							}
						} else {
							def sb = new SwingBuilder()
							def pane = sb.optionPane(message: 'Invalid Ship Placement: ' + currentShip, options: ['OK'])
							def dialog = pane.createDialog(null, 'Error!')
							dialog.visible = true
						}
					}
				}
			}
		}
		panel(constraints: BorderLayout.CENTER, id: 'gamePanel', visible: false) {
			hbox {
				panel {
					borderLayout()
					label 'Your Board', constraints: BorderLayout.NORTH
					panel(id: 'panelGrid', constraints: BorderLayout.CENTER) {
						gridLayout() { }
					}
				}
				
				panel()
				
				panel {
					borderLayout()
					label 'Opponent Board', constraints: BorderLayout.NORTH
					panel(id: 'opponentGrid', constraints: BorderLayout.CENTER) {
						gridLayout() { }
					}
				}
			}
		}
		panel(constraints: BorderLayout.SOUTH, id: 'lobbyPanel', visible: false) {
			borderLayout()
			panel(constraints: BorderLayout.CENTER) {
				textArea id: 'chat', rows: 20, columns: 50
			}
			panel(constraints: BorderLayout.SOUTH) {
				textField gameData.chatSend, id: 'chatSend', columns: 20
				button text: 'Send', actionPerformed: {
					if (gameData.chatSend.trim().equals("")) {
						return;
					}
					client.send "CHAT 0 " + gameData.chatSend
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
		bean gameData,
			name: bind { name.text },
			chatSend: bind { chatSend.text }
	}

}
