import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;
    Chatters clientes;
    private Map<String, Set<PrintWriter>> groups; // Mapa de grupos para manejar la comunicación grupal

    public ClientHandler(Socket socket, Chatters clientes, Map<String, Set<PrintWriter>> groups) {
        this.clientes = clientes;
        this.clientSocket = socket;
        this.groups = groups;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Pedir el nombre del cliente hasta que sea válido
            while (true) {
                out.println("SUBMITNAME");
                clientName = in.readLine();
                if (clientName == null) {
                    return;
                }
                synchronized (clientes) {
                    if (!clientName.isBlank() && !clientes.existeUsr(clientName)) {
                        clientes.broadcastMessage(clientName + " se ha unido al chat.");
                        out.println("NAMEACCEPTED " + clientName);
                        clientes.addUsr(clientName, out);
                        break;
                    }
                }
            }

            // Ahora que el nombre está establecido, el usuario puede usar la funcionalidad de grupos
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
                    listClients(); // Nuevo comando para listar los clientes conectados
                } else if (message.startsWith("/sendvoicenote")) {
                    // Formato: /sendvoicenote <usuario|grupo> <nombreArchivo>
                    String[] parts = message.split(" ");
                    String target = parts[1];
                    String fileName = parts[2];
                    receiveVoiceNote(fileName, target);
                } else if (message.startsWith("@")) {
                    // Mensaje privado: formato @nombreUsuario: mensaje
                    int idx = message.indexOf(':');
                    if (idx != -1) {
                        String targetUser = message.substring(1, idx);
                        String newMessage = message.substring(idx + 1).trim();
                        if (!newMessage.isEmpty()) {
                            clientes.privateMessage(targetUser, clientName + " (privado): " + newMessage);
                        }
                    }
                } else {
                    // Mensaje público
                    clientes.broadcastMessage(clientName + ": " + message);
                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println(clientName + " ha abandonado el chat.");
                clientes.broadcastMessage(clientName + " ha abandonado el chat.");
                synchronized (clientes) {
                    clientes.removeUsr(clientName);
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
    }

    // Método para crear un grupo
    private void createGroup(String groupName) {
        if (!groups.containsKey(groupName)) {
            groups.put(groupName, new HashSet<>());
            out.println("Group '" + groupName + "' created successfully.");
        } else {
            out.println("Group '" + groupName + "' already exists.");
        }
    }

    // Método para unirse a un grupo
    private void joinGroup(String groupName) {
        if (groups.containsKey(groupName)) {
            groups.get(groupName).add(out);
            out.println("Joined group '" + groupName + "' successfully.");
        } else {
            out.println("Group '" + groupName + "' does not exist.");
        }
    }

    // Método para listar los grupos disponibles
    private void listGroups() {
        if (groups.isEmpty()) {
            out.println("No groups available.");
        } else {
            out.println("Available groups: " + String.join(", ", groups.keySet()));
        }
    }

    // Método para enviar un mensaje a un grupo específico
    private void sendMessageToGroup(String groupName, String message) {
        if (groups.containsKey(groupName)) {
            for (PrintWriter writer : groups.get(groupName)) {
                writer.println("Group " + groupName + ": " + message);
            }
        } else {
            out.println("Group '" + groupName + "' does not exist.");
        }
    }

    // Nuevo método para listar todos los clientes conectados
    private void listClients() {
        Set<String> connectedUsers = clientes.getConnectedUsers();
        System.out.println("Clientes conectados: " + connectedUsers); // Mensaje de depuración para el servidor
        if (connectedUsers.isEmpty()) {
            out.println("No hay clientes conectados.");
        } else {
            out.println("Clientes conectados: " + String.join(", ", connectedUsers));
        }
    }

    // Método para recibir una nota de voz desde el cliente
    private void receiveVoiceNote(String fileName, String target) throws IOException {
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        FileOutputStream fos = new FileOutputStream("server_" + fileName);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = dis.read(buffer)) > 0) {
            fos.write(buffer, 0, bytesRead);
        }
        fos.close();
        sendVoiceNoteToTarget(fileName, target);
    }

    // Método para enviar una nota de voz a un usuario o grupo
    private void sendVoiceNoteToTarget(String fileName, String target) throws IOException {
        File file = new File("server_" + fileName);
        if (clientes.existeUsr(target)) {
            sendVoiceNoteToUser(file, target);
        } else if (groups.containsKey(target)) {
            sendVoiceNoteToGroup(file, target);
        } else {
            out.println("Usuario o grupo no encontrado: " + target);
        }
    }

    // Enviar una nota de voz a un usuario específico
    private void sendVoiceNoteToUser(File file, String user) throws IOException {
        PrintWriter userWriter = clientes.getWriter(user);
        if (userWriter != null) {
            userWriter.println("Recibiendo nota de voz de " + clientName);
            sendFile(file, userWriter);
        }
    }

    // Enviar una nota de voz a un grupo
    private void sendVoiceNoteToGroup(File file, String group) throws IOException {
        for (PrintWriter writer : groups.get(group)) {
            writer.println("Recibiendo nota de voz de " + clientName + " en el grupo " + group);
            sendFile(file, writer);
        }
    }

    // Método auxiliar para enviar el archivo a través del socket
    private void sendFile(File file, PrintWriter writer) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) > 0) {
            dos.write(buffer, 0, bytesRead);
        }
        fis.close();
        writer.println("Nota de voz enviada correctamente.");
    }
}
