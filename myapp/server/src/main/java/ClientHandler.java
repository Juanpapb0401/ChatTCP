import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;
    private Chatters clientes;
    private Map<String, Set<PrintWriter>> groups;

    public ClientHandler(Socket socket, Chatters clientes, Map<String, Set<PrintWriter>> groups) {
        this.clientSocket = socket;
        this.clientes = clientes;
        this.groups = groups;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Autenticación del usuario
            while (true) {
                out.println("SUBMITNAME");
                clientName = in.readLine();
                if (clientName == null) {
                    return;
                }
                synchronized (clientes) {
                    if (!clientName.isBlank() && !clientes.existeUsr(clientName)) {
                        clientes.addUsr(clientName, out);
                        break;
                    }
                }
            }

            out.println("NAMEACCEPTED " + clientName);
            clientes.broadcastMessage(clientName + " se ha unido al chat.");

            // Procesar mensajes del cliente
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/creategroup")) {
                    createGroup(message.split(" ")[1]);
                } else if (message.startsWith("/joingroup")) {
                    joinGroup(message.split(" ")[1]);
                } else if (message.startsWith("/groups")) {
                    listGroups();
                } else if (message.startsWith("/msggroup")) {
                    String[] parts = message.split(" ", 3);
                    sendMessageToGroup(parts[1], parts[2]);
                } else if (message.startsWith("/listclients")) {
                    listClients();
                } else if (message.startsWith("/sendvoicenote")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length == 3) {
                        receiveVoiceNote(parts[2], parts[1]);
                    }
                } else if (message.startsWith("@")) {
                    int idx = message.indexOf(':');
                    if (idx != -1) {
                        String targetUser = message.substring(1, idx);
                        String privateMessage = message.substring(idx + 1).trim();
                        clientes.privateMessage(targetUser, clientName + " (privado): " + privateMessage);
                    }
                } else {
                    clientes.broadcastMessage(clientName + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Error en ClientHandler: " + e.getMessage());
        } finally {
            if (clientName != null) {
                clientes.removeUsr(clientName);
                clientes.broadcastMessage(clientName + " ha abandonado el chat.");
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createGroup(String groupName) {
        if (!groups.containsKey(groupName)) {
            groups.put(groupName, new HashSet<>());
            out.println("Grupo '" + groupName + "' creado exitosamente.");
        } else {
            out.println("El grupo '" + groupName + "' ya existe.");
        }
    }

    private void joinGroup(String groupName) {
        if (groups.containsKey(groupName)) {
            groups.get(groupName).add(out);
            out.println("Te has unido al grupo '" + groupName + "'.");
        } else {
            out.println("El grupo '" + groupName + "' no existe.");
        }
    }

    private void listGroups() {
        if (groups.isEmpty()) {
            out.println("No hay grupos disponibles.");
        } else {
            out.println("Grupos disponibles: " + String.join(", ", groups.keySet()));
        }
    }

    private void sendMessageToGroup(String groupName, String message) {
        if (groups.containsKey(groupName)) {
            for (PrintWriter writer : groups.get(groupName)) {
                writer.println("Grupo " + groupName + ": " + clientName + ": " + message);
            }
        } else {
            out.println("El grupo '" + groupName + "' no existe.");
        }
    }

    private void listClients() {
        Set<String> connectedUsers = clientes.getConnectedUsers();
        if (connectedUsers.isEmpty()) {
            out.println("No hay clientes conectados.");
        } else {
            out.println("Clientes conectados: " + String.join(", ", connectedUsers));
        }
    }

    private void receiveVoiceNote(String fileName, String target) throws IOException {
        Path filePath = Paths.get("server_" + fileName);
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             FileOutputStream fos = new FileOutputStream(filePath.toFile())) {

            long fileSize = dis.readLong();
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.printf("Recibiendo: %.2f%%\r", (totalBytesRead * 100.0) / fileSize);
            }
            fos.flush();
            System.out.println("\nNota de voz recibida: " + fileName);

            sendVoiceNoteToTarget(fileName, target);
        } catch (IOException e) {
            System.out.println("Error al recibir la nota de voz: " + e.getMessage());
            throw e;
        }
    }

    private void sendVoiceNoteToTarget(String fileName, String target) throws IOException {
        Path filePath = Paths.get("server_" + fileName);
        if (Files.exists(filePath)) {
            if (clientes.existeUsr(target)) {
                sendVoiceNoteToUser(filePath, target);
            } else if (groups.containsKey(target)) {
                sendVoiceNoteToGroup(filePath, target);
            } else {
                out.println("Usuario o grupo no encontrado: " + target);
            }
        } else {
            out.println("Archivo no encontrado en el servidor: " + fileName);
        }
    }

    private void sendVoiceNoteToUser(Path filePath, String user) throws IOException {
        PrintWriter userWriter = clientes.getWriter(user);
        if (userWriter != null) {
            userWriter.println("Recibiendo nota de voz de " + clientName);
            sendFile(filePath, userWriter);
        }
    }

    private void sendVoiceNoteToGroup(Path filePath, String group) throws IOException {
        for (PrintWriter writer : groups.get(group)) {
            writer.println("Recibiendo nota de voz de " + clientName + " en el grupo " + group);
            sendFile(filePath, writer);
        }
    }

    private void sendFile(Path filePath, PrintWriter writer) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            long fileSize = Files.size(filePath);
            dos.writeLong(fileSize);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.printf("Enviando: %.2f%%\r", (totalBytesRead * 100.0) / fileSize);
            }
            dos.flush();
            System.out.println("\nNota de voz enviada correctamente.");
            writer.println("Nota de voz recibida correctamente.");
        }
    }
}