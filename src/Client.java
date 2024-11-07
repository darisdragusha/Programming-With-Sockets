import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    private DatagramSocket datagramSocket;
    private InetAddress serverAddress;
    private int serverPort;
    private byte[] buffer;

    public Client(DatagramSocket datagramSocket, InetAddress serverAddress, int serverPort) {
        this.datagramSocket = datagramSocket;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void sendThenReceive() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("Enter command (REQUEST_FULL_ACCESS, RELEASE_FULL_ACCESS, READ <file>, WRITE <file> <content>, EXECUTE, LIST):");
                String command = scanner.nextLine();
                buffer = command.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
                datagramSocket.send(sendPacket);

                byte[] responseBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                datagramSocket.receive(receivePacket);

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Response from server: " + response);

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        scanner.close();
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        if (args.length < 2) {
            System.out.println("Usage: java Client <Server IP> <Server Port>");
            return;
        }

        InetAddress serverAddress = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);
        DatagramSocket datagramSocket = new DatagramSocket();

        Client client = new Client(datagramSocket, serverAddress, serverPort);
        System.out.println("Client started. Ensure the server is running before sending commands.");
        client.sendThenReceive();
    }
}