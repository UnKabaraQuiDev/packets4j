package guess_the_number;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

import lu.pcy113.jb.codec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.socket.client.P4JClient;

public class GuessTheNumberClient extends P4JClient {
	
	public GuessTheNumberClient() {
		super(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
		
		registerPacket(GuessPacket.class, 1);
		registerPacket(StartGamePacket.class, 2);
		registerPacket(StopGamePacket.class, 3);
		
		super.getCodec().register(new RangeEncoder(), new RangeDecoder(), (short) 12);
	}
	
	public void gameDisconnect() {
		write(new StopGamePacket("Disconnected by client."));
		super.close();
	}
	
	public void gameStart(Range obj) {
		System.out.println("Game started by server, guessing range: "+obj.getMin()+"-"+obj.getMax());
	}
	
	public void gameFeedback(String message) {
		System.out.println(message);
	}
	
	public static void main(String[] args) throws IOException {
		GuessTheNumberClient client = new GuessTheNumberClient();
		client.bind();
		client.connect(InetAddress.getLocalHost(), 5000);

		/*P4JClient.setPacketReceivedHandler(packet -> {
			if (packet instanceof S2CPacket) {
				S2CPacket s2cPacket = (S2CPacket) packet;
				if (s2cPacket instanceof FeedbackPacket) {
					FeedbackPacket feedbackPacket = (FeedbackPacket) s2cPacket;
					System.out.println(feedbackPacket.getFeedbackMessage());
				} else if (s2cPacket instanceof GameEndPacket) {
					GameEndPacket gameEndPacket = (GameEndPacket) s2cPacket;
					System.out.println(gameEndPacket.getEndMessage());
				}
			}
		});*/

		Scanner scanner = new Scanner(System.in);
		while (true) {
			String input = scanner.nextLine();

			if (input.equals("quit")) {
				client.gameDisconnect();
				break;
			}else if (input.equals("start")) {
				client.write(new StartGamePacket());
				continue;
			}

			int guessedNumber;
			try {
				guessedNumber = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a number or 'quit' to exit.");
				continue;
			}

			GuessPacket guessPacket = new GuessPacket(guessedNumber);
			client.write(guessPacket);
		}
	}

}