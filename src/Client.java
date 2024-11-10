//import java.io.IOException;
//import java.net.*;
//import java.util.Scanner;
//
//public class Client {
//    private DatagramSocket datagramSocket;
//    private InetAddress serverAddress;
//    private int serverPort;
//    private byte[] buffer = new byte[1024];
//
//    public Client(String serverIp, int serverPort) throws UnknownHostException, SocketException {
//        this.serverAddress = InetAddress.getByName(serverIp);
//        this.serverPort = serverPort;
//        this.datagramSocket = new DatagramSocket();
//    }
//
//    public void sendMessage(String message) throws IOException {
//        byte[] messageBytes = message.getBytes();
//        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, serverAddress, serverPort);
//        datagramSocket.send(packet);
//
//        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
//        datagramSocket.receive(responsePacket);
//        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
//        System.out.println("Server response: " + response);
//    }
//
//    public void displayAvailableCommands() throws IOException {
//        sendMessage("AVAILABLE_COMMANDS");
//    }
//
//    public void start() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Client started. Ensure the server is running before sending commands.");
//
//        try {
//            while (true) {
//                System.out.println("\nRequesting available commands from server...");
//                displayAvailableCommands();
//
//                System.out.println("\nEnter a command (or type 'EXIT' to close the client):");
//                String command = scanner.nextLine().trim();
//
//                if (command.equalsIgnoreCase("EXIT")) {
//                    System.out.println("Exiting client.");
//                    break;
//                }
//
//                sendMessage(command);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            datagramSocket.close();
//        }
//    }
//
//    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.out.println("Usage: java Client <Server IP> <Server Port>");
//            return;
//        }
//
//        try {
//            String serverIp = args[0];
//            int serverPort = Integer.parseInt(args[1]);
//            Client client = new Client(serverIp, serverPort);
//            client.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
import java.io.IOException;
import java.net.*;
import java.util.Base64;
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

    // Encode with Base64
    public static String encodeBase64(String message) {
        return Base64.getEncoder().encodeToString(message.getBytes());
    }

    // Decode with Base64
    public static String decodeBase64(String encodedMessage) {
        return new String(Base64.getDecoder().decode(encodedMessage));
    }

    public void sendMessage(String message) throws IOException {
        String encodedMessage = encodeBase64(message); // enkripti i mesazhit
        byte[] messageBytes = encodedMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, serverAddress, serverPort);
        datagramSocket.send(packet);

        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(responsePacket);

        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        String decodedResponse = decodeBase64(response); // dekodimi i mesazheve
        System.out.println("Mesazhi nga Serveri: " + decodedResponse);
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
