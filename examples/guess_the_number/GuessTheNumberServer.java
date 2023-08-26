package guess_the_number;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import lu.pcy113.jb.codec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.events.Event;
import lu.pcy113.p4j.events.Listener;
import lu.pcy113.p4j.socket.P4JClientInstance;
import lu.pcy113.p4j.socket.P4JServerInstance;
import lu.pcy113.p4j.socket.events.ClientInstanceConnectedEvent;
import lu.pcy113.p4j.socket.server.P4JServer;
import lu.pcy113.p4j.socket.server.ServerClient;

public class GuessTheNumberServer {
	
	private Range range;
	
	private ServerClient client;
	private P4JServer server;
	
    private int randomNumber;
    private int attemptsLeft;
    private boolean gameActive;

    public GuessTheNumberServer(P4JClientInstance c, P4JServerInstance s) {
    	server = (P4JServer) s;
    	client = (ServerClient) c;
    	
        this.randomNumber = 0;
        this.attemptsLeft = 0;
        this.gameActive = false;
        
        Random random = new Random();
        range = new Range(random.nextInt(50), random.nextInt(100));
    }
    
    public void requestDisconnection(String obj) {
    	System.out.println("Disconnection requested for: "+obj);
    	gameStop();
	}
    
    public void gameStart() {
    	Range answer = null;
    	
        if (!gameActive) {
            Random random = new Random();
            randomNumber = random.nextInt(range.getMax()+1)+ range.getMin();
            attemptsLeft = 5;
            gameActive = true;

            System.out.println("Game started");
            
            answer = range;
        } else {
        	System.out.println("Game already started");
        	
        	answer = null;
        }
        
        client.write(new StartGamePacket(answer));
    }

    public void gameGuess(int guessedNumber) {
    	String answer = null;
    	
        if (gameActive) {
            attemptsLeft--;

            if (guessedNumber == randomNumber) {
                answer = "Correct guess! You won the game.";
                gameActive = false;
            } else if (attemptsLeft == 0) {
            	client.write(new StopGamePacket("Out of attempts! You lost the game. The number was: " + randomNumber));
                gameActive = false;
            } else if (guessedNumber < randomNumber) {
            	answer = "Too low! Try again. Attempts left: " + attemptsLeft;
            } else {
            	answer = "Too high! Try again. Attempts left: " + attemptsLeft;
            }
        } else {
        	answer = "No active game. Start a new game.";
        }
        
        if(answer != null) {
        	client.write(new FeedbackPacket(answer));
        	if(guessedNumber == randomNumber) {
        		client.write(new StopGamePacket("You won the game."));
        	}
        }
    }

    public void gameStop() {
        gameActive = false;
        System.out.println("Game stopped.");
    }

    public static HashMap<UUID, GuessTheNumberServer> games = new HashMap<>();
    
    public static void main(String[] args) throws IOException {
    	P4JServer server = new P4JServer(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
        
        server.registerPacket(GuessPacket.class, 1);
        server.registerPacket(StartGamePacket.class, 2);
        server.registerPacket(StopGamePacket.class, 3);
        
        server.getCodec().register(new RangeEncoder(), new RangeDecoder(), (short) 12);
        
        server.listenersConnected.add(new Listener() {
			@Override
			public void handle(Event event) {
				ClientInstanceConnectedEvent e = (ClientInstanceConnectedEvent) event;
				System.out.println("Client connected: "+((ServerClient) e.getClient()).getUUID());
				games.put(((ServerClient) e.getClient()).getUUID(), new GuessTheNumberServer(e.getClient(), e.getServer()));
			}
		});
        
        server.bind(new InetSocketAddress(5000));
        server.setAccepting();

        /*P4JServer.setPacketReceivedHandler(packet -> {
            if (packet instanceof C2SPacket) {
                C2SPacket c2sPacket = (C2SPacket) packet;
                if (c2sPacket instanceof GuessPacket) {
                    GuessPacket guessPacket = (GuessPacket) c2sPacket;
                    server.guess(guessPacket.getGuessedNumber());
                } else if (c2sPacket instanceof StartGamePacket) {
                    server.start();
                } else if (c2sPacket instanceof StopGamePacket) {
                    server.stop();
                }
            }
        });*/
    }

}
