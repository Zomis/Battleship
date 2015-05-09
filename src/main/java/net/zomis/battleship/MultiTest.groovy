package net.zomis.battleship


def scanner = new Scanner(System.in);

print "Enter name: "
def nameA = scanner.nextLine();
def listenerA = {mess -> System.out.println nameA + " Incoming: " + mess}
def clientA = new Client(name: nameA)
new Thread({ clientA.listen(listenerA) }).start();

print "Enter name: "
def nameB = scanner.nextLine();
def listenerB = {mess -> System.out.println nameB + " Incoming: " + mess}
def clientB = new Client(name: nameB)
new Thread({ clientB.listen(listenerB) }).start();

Thread.sleep 200
clientA.send("INVT Battleship $nameB")
Thread.sleep 200
clientB.send("INVY 0")
Thread.sleep 200
clientA.send("MOVE 0 SHIP Air_Carrier 5 1 0 0 Battleship 4 1 0 1 Submarine 3 1 0 2 Submarine 3 1 0 3 Patrol 2 1 0 4")
Thread.sleep 200
clientB.send("MOVE 0 SHIP Air_Carrier 5 1 0 0 Battleship 4 1 0 1 Submarine 3 1 0 2 Submarine 3 1 0 3 Patrol 2 1 0 4")
Thread.sleep 200

clientA.send("MOVE 0 PLAY 0 0")
clientA.send("MOVE 0 PLAY 0 1")
clientA.send("MOVE 0 PLAY 0 2")
clientA.send("MOVE 0 PLAY 0 3")
clientA.send("MOVE 0 PLAY 0 4")
clientA.send("MOVE 0 PLAY 0 5")
