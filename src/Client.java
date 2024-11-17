
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    private DatagramSocket datagramSocket;
    private InetAddress serverAddress;
    private int serverPort;
    private byte[] buffer = new byte[1024];

    public Client(String serverIp, int serverPort) throws UnknownHostException, SocketException {
        this.serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.datagramSocket = new DatagramSocket();
    }



    public void sendMessage(String message) throws IOException {
        byte[] messageBytes = message.getBytes();  //
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, serverAddress, serverPort);
        datagramSocket.send(packet);

        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(responsePacket);

        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        System.out.println("Mesazhi nga Serveri: " + response);
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Kujdes!!\nLidhja me Server behet ne kete menyre: java Client <Server IP> <Server Port>");
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            System.out.println("Jeni lidhur me Server. Komandat e lejuara: \n  1. REQUEST_FULL_ACCESS \n  2. READ `emri i fajllit` \n  3. LIST");

            while (true) {
                String message = scanner.nextLine().trim();
                client.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
