import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.awt.Desktop;

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
//Metoda per logim te komunikimit Klient-Server
    public void logToFile(String logMessage) {
        try (FileWriter fileWriter = new FileWriter("communication_log.txt", true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Enkriptim me Base64
    public static String encodeBase64(String message) {
        return Base64.getEncoder().encodeToString(message.getBytes());
    }

    // Dekriptimi me Base64
    public static String decodeBase64(String encodedMessage) {
        return new String(Base64.getDecoder().decode(encodedMessage));
    }

    public void receiveThenRespond() {
        System.out.println("Server filloi. Po n'degjohen te gjitha mesazhet ne ip adresen: " + ipAddress + " dhe port-in: " + PORT);

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);

                InetAddress clientAddress = packet.getAddress();
                String clientIp = clientAddress.getHostAddress();
                int clientPort = packet.getPort();

                String messageFromClient = new String(packet.getData(), 0, packet.getLength()).trim();
                String decodedMessage = decodeBase64(messageFromClient); //Dekriptimi i mesazhit
                System.out.println("\nMesazhi i dekriptuar nga Klienti (" + clientIp + "): " + decodedMessage);
                logToFile("Mesazhi nga Klienti (" + clientIp + "): " + decodedMessage); // Logimi i mesazhit

                String response;
                switch (decodedMessage) {
                    case "REQUEST_FULL_ACCESS":
                        response = handleAccessRequest(clientIp);
                        break;
                    case "RELEASE_FULL_ACCESS":
                        response = handleAccessRelease(clientIp);
                        break;
                    default:
                        response = handleRequest(clientIp, decodedMessage);
                        break;
                }

                String encodedResponse = encodeBase64(response); // Enkriptimi i pergjigjes
                byte[] responseBytes = encodedResponse.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
                datagramSocket.send(responsePacket);
                logToFile("Pergjigjia per Klientin (" + clientIp + "): " + response); // Logimi i pergjigjes

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private String handleAccessRequest(String clientIp) {
        Scanner scanner = new Scanner(System.in);

        if (fullAccessClient == null) {
            System.out.println("Klienti " + clientIp + " Po kerkon leje per te shkruar dhe ekzekutuar. \nA lejohet? (yes/no)");
            String approval = scanner.nextLine().trim().toLowerCase();

            if (approval.equals("yes")) {
                fullAccessClient = clientIp;
                hasAccess.put(clientIp, true);
                return "Serveri aprovoi kerkesen e juaj";
            } else {
                return "Serveri nuk e aprovoi kerkesen e juaj.";
            }
        } else if (fullAccessClient.equals(clientIp)) {
            return "Ke arritur akses maksimal.";
        } else {
            return "Nje klient tjeter ka akses maksimal. Ju lutemi pritni.";
        }
    }

    private String handleAccessRelease(String clientIp) {
        if (clientIp.equals(fullAccessClient)) {
            System.out.println("Klienti " + clientIp + " nuk ka me akses maksimal.");
            fullAccessClient = null;
            hasAccess.put(clientIp, false);
            return "Aksesi maksimal u leshua.";
        } else {
            return "Komande e panjohur";
        }
    }

    private String handleRequest(String clientIp, String request) {
        boolean hasFullAccess = hasAccess.getOrDefault(clientIp, false);

        switch (request) {
            case "AVAILABLE_COMMANDS":
                return hasFullAccess ? "Komandat: RELEASE_FULL_ACCESS, READ, WRITE, EXECUTE, LIST, CREATE, DELETE"
                        : "Komandat: REQUEST_FULL_ACCESS, READ, LIST";
            case "LIST":
                return listFiles("File/");
            case "EXECUTE":
                return hasFullAccess ? "File python eshte duke u ekzekutar." : "Nuk keni akses per kete veprim. Kerkoni akses te plote.";
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
                            return "Forma e panjohur e komandes.";
                        }
                    } else {
                        return "Nuk lejoheni te shkruani. Kerko akses maksimal.";
                    }
                } else if (request.startsWith("CREATE ")) {
                    if (hasFullAccess) {
                        String filePath = request.substring(7).trim();
                        return createFile(filePath);
                    } else {
                        return "Nuk ju lejohet te krijoni nje file te ri. Kerko akses maksimal.";
                    }
                } else if (request.startsWith("DELETE ")) {
                    if (hasFullAccess) {
                        String filePath = request.substring(7).trim();
                        return deleteFile(filePath);
                    } else {
                        return "Nuk ju lejohet te fshini files. Kerko akses maksimal.";
                    }
                }
                else if(request.startsWith("EXECUTE ")){
                    if(hasFullAccess){
                        String filePath = request.substring(8).trim();
                        return executeFile(filePath);
                    } else {
                        return "Nuk keni askes per kete veprim. Kerkoni akses te plote.";
                    }
                }
                return "Iu dergua mesazhi serverit.\nNese keni menduar te dergoni komand, atehere shiko edhe njehere formatin e duhur te komandave permes komandes `AVAILABLE_COMMANDS`.";
        }
    }

    private String readFile(String filePath) {
        try {
            Path path = Paths.get("File/" + filePath);
            System.out.println("Tentim per te lexuar file-in: " + path.toString());
            return Files.readString(path);
        } catch (IOException e) {
            return "Gabim gjate leximit te file-it: " + e.getMessage();
        }
    }

    private String writeFile(String filePath, String content) {
        try (FileOutputStream out = new FileOutputStream(new File("File/" + filePath), true)) {
            out.write((content + "\n").getBytes());
            return "Teksti u shkrua ne menyre te sukseshme.";
        } catch (IOException e) {
            return "Gabim gjate shkrimit ne file: " + e.getMessage();
        }
    }

    private String createFile(String filePath) {
        try {
            File file = new File("File/" + filePath);
            if (file.createNewFile()) {
                return "File-i u krijua: " + filePath;
            } else {
                return "File-i ekziston.";
            }
        } catch (IOException e) {
            return "Gabim gjate krijimit te file-it: " + e.getMessage();
        }
    }

    private String deleteFile(String filePath) {
        File file = new File("File/" + filePath);
        if (file.delete()) {
            return "File-i u fshi: " + filePath;
        } else {
            return "Deshtoi fshirja e file-it";
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
            return "Folder-i nuk u gjete.";
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
            System.out.println("Perdorimi: java Server <IP> <Port>");
            return;
        }

        ipAddress = InetAddress.getByName(args[0]);
        PORT = Integer.parseInt(args[1]);

        DatagramSocket datagramSocket = new DatagramSocket(PORT, ipAddress);
        Server server = new Server(datagramSocket);
        server.receiveThenRespond();
    }
}
