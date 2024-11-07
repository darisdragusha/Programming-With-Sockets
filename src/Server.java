import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import  java.awt.Desktop;

public class Server {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private static int PORT;
    private static InetAddress ipAddress;

    // Map to store each client's permissions
    private final Map<String, Boolean> hasAccess = new HashMap<>();
    private String fullAccessClient = null; // IP of the client with full access

    public Server(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void receiveThenRespond() {
        System.out.println("Server started. Listening on IP " + ipAddress + " and port " + PORT);

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);

                InetAddress clientAddress = packet.getAddress();
                String clientIp = clientAddress.getHostAddress();
                int clientPort = packet.getPort();

                String messageFromClient = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("\nMessage from client (" + clientIp + "): " + messageFromClient);

                String response;
                switch (messageFromClient) {
                    case "REQUEST_FULL_ACCESS":
                        response = handleAccessRequest(clientIp);
                        break;
                    case "RELEASE_FULL_ACCESS":
                        response = handleAccessRelease(clientIp);
                        break;
                    default:
                        response = handleRequest(clientIp, messageFromClient);
                        break;
                }

                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
                datagramSocket.send(responsePacket);

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private String handleAccessRequest(String clientIp) {
        Scanner scanner = new Scanner(System.in);

        if (fullAccessClient == null) {
            System.out.println("Client " + clientIp + " is requesting WRITE and EXECUTE access. Approve? (yes/no)");
            String approval = scanner.nextLine().trim().toLowerCase();

            if (approval.equals("yes")) {
                fullAccessClient = clientIp;
                hasAccess.put(clientIp, true);
                return "Access granted for WRITE and EXECUTE.";
            } else {
                return "Access denied.";
            }
        } else if (fullAccessClient.equals(clientIp)) {
            return "You already have full access.";
        } else {
            return "Another client currently has full access. Please wait.";
        }
    }

    private String handleAccessRelease(String clientIp) {
        if (clientIp.equals(fullAccessClient)) {
            System.out.println("Client " + clientIp + " is releasing WRITE and EXECUTE access.");
            fullAccessClient = null;
            hasAccess.put(clientIp, false);
            return "Full access released.";
        } else {
            return "You do not have full access to release.";
        }
    }

    private String handleRequest(String clientIp, String request) {
        boolean hasFullAccess = hasAccess.getOrDefault(clientIp, false);

        switch (request) {
            case "AVAILABLE_COMMANDS":
                return hasFullAccess ? "Commands: RELEASE_FULL_ACCESS, READ, WRITE, EXECUTE, LIST"
                        : "Commands: REQUEST_FULL_ACCESS, READ, LIST";
            case "LIST":
                return listFiles("File/");
            case "EXECUTE":
                return hasFullAccess ? "Execute action simulated." : "Permission denied for executing. Request full access first.";
            default:
                if (request.startsWith("READ ")) {
                    String filePath = request.substring(5).trim();
                    return readFile(filePath);
                } else if (request.startsWith("WRITE ")) {
                    if (hasFullAccess) {
                        String[] parts = request.split(" ", 3);
                        if (parts.length == 3) {
                            return writeFile(parts[1], parts[2]);
                        } else {
                            return "Invalid write request format.";
                        }
                    } else {
                        return "Permission denied for writing. Request full access first.";
                    }

                }else if (request.startsWith("EXECUTE ")) {
                    if (hasFullAccess) {
                        String[] parts = request.split(" ", 2);
                        if (parts.length == 2) {
                            return executeFile(parts[1]);
                        } else {
                            return "Invalid execute request format.";
                        }
                    } else {
                        return "Permission denied for executing. Request full access first.";
                    }
                }
                return "Invalid request.";
        }
    }

    private String readFile(String filePath) {
        try {
            Path path = Paths.get("File/" + filePath);
            System.out.println("Attempting to read file: " + path.toString());
            return Files.readString(path);
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    private String writeFile(String filePath, String content) {
        try (FileOutputStream out = new FileOutputStream(new File("File/" + filePath), true)) {
            out.write((content + "\n").getBytes());
            return "Write successful (appended).";
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
    private String executeFile(String filePath) {
        try {
            Path path = Paths.get("File/" + filePath);
            System.out.println("Attempting to execute file: " + path.toString());

            File file = path.toFile();

            // Check if Desktop is supported and the file exists
            if (Desktop.isDesktopSupported() && file.exists()) {
                Desktop.getDesktop().open(file); // Open the file with the default application
                return "File executed successfully: " + file.getAbsolutePath();
            } else if (!file.exists()) {
                return "File does not exist: " + file.getAbsolutePath();
            } else {
                return "Desktop operations are not supported on this platform.";
            }
        } catch (IOException e) {
            return "Error executing file: " + e.getMessage();
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