import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private static int PORT;
    private static InetAddress ipAddress;

    // Map to store permissions for each client based on IP
    private static final Map<String, String> clientPermissions = new HashMap<>();

    public Server(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;

        // Define client permissions
        clientPermissions.put("127.0.0.1", "read_write_execute"); // Client with full permissions
        clientPermissions.put("192.168.1.4", "read");              // Client with read-only permissions
        clientPermissions.put("192.168.1.5", "read");              // Client with read-only permissions
    }

    public void receiveThenRespond() {
        System.out.println("Server listening on IP " + ipAddress + " and port " + PORT);

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                String clientIp = clientAddress.getHostAddress();

                String messageFromClient = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("Message from client (" + clientIp + "): " + messageFromClient);

                // Process request based on client permissions
                String response = handleRequest(clientIp, messageFromClient);
                byte[] responseBytes = response.getBytes();

                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
                datagramSocket.send(responsePacket);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private String handleRequest(String clientIp, String request) {
        String permission = clientPermissions.getOrDefault(clientIp, "none");

        if (request.startsWith("READ ")) {
            if (permission.contains("read")) {
                String filePath = request.substring(5).trim();
                return readFile(filePath);
            } else {
                return "Permission denied for reading.";
            }
        } else if (request.startsWith("WRITE ")) {
            if (permission.contains("write")) {
                String[] parts = request.split(" ", 3);
                if (parts.length == 3) {
                    return writeFile(parts[1], parts[2]);
                } else {
                    return "Invalid write request format.";
                }
            } else {
                return "Permission denied for writing.";
            }
        } else if (request.equals("EXECUTE")) {
            if (permission.contains("execute")) {
                return "Execute action simulated.";
            } else {
                return "Permission denied for executing.";
            }
        } else if (request.equals("LIST")) {
            if (permission.contains("read")) {
                return listFiles("src/File/");
            } else {
                return "Permission denied for listing files.";
            }
        }
        return "Invalid request.";
    }

    private String readFile(String filePath) {
        try {
            Path path = Paths.get("src/File/" + filePath);
            System.out.println("Attempting to read file: " + path.toString());
            return Files.readString(path);
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    private String writeFile(String filePath, String content) {
        try (FileOutputStream out = new FileOutputStream(new File("src/" + filePath))) {
            out.write(content.getBytes());
            return "Write successful.";
        } catch (IOException e) {
            return "Error writing to file: " + e.getMessage();
        }
    }

    private String listFiles(String directoryPath) {
        File folder = new File(directoryPath);
        if (folder.isDirectory()) {
            StringBuilder fileList = new StringBuilder("Files:\n");
            for (File file : folder.listFiles()) {
                fileList.append(file.getName()).append("\n");
            }
            return fileList.toString();
        } else {
            return "Directory not found.";
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java Server <IP> <Port>");
            return;
        }

        ipAddress = InetAddress.getByName(args[0]);
        PORT = Integer.parseInt(args[1]);

        DatagramSocket datagramSocket = new DatagramSocket(PORT, ipAddress);
        Server server = new Server(datagramSocket);
        server.receiveThenRespond();
    }
}