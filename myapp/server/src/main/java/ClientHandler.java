import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;
    private Chatters clientes;
    private Map<String, Set<PrintWriter>> groups;
    private static List<String> messageHistory = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        String joinMessage = clientName + " se ha unido al chat.";
        clientes.broadcastMessage(joinMessage);
        addToHistory(joinMessage);

        // Procesar mensajes del cliente
        String message;
        while ((message = in.readLine()) != null) {
            if (message.equals("/history")) {
                sendHistory();
            } else if (message.startsWith("/creategroup")) {
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
                    // Ejecutar la recepción de la nota de voz en un hilo separado
                    Thread voiceNoteThread = new Thread(() -> {
                        try {
                            receiveVoiceNote(parts[2], parts[1]);
                        } catch (IOException e) {
                            System.out.println("Error al recibir la nota de voz: " + e.getMessage());
                        }
                    });
                    voiceNoteThread.start(); // Iniciar el hilo para recibir la nota de voz
                }
            } else if (message.startsWith("@")) {
                int idx = message.indexOf(':');
                if (idx != -1) {
                    String targetUser = message.substring(1, idx);
                    String privateMessage = message.substring(idx + 1).trim();
                    clientes.privateMessage(targetUser, clientName + " (privado): " + privateMessage);
                    addToHistory(clientName + " (privado a " + targetUser + "): " + privateMessage);
                }
            } else if (message.equals("/listvoicenotes")) {
                listVoiceNoteHistory();
            }
            else if (message.equals("/listuservoicenotes")) {
                listUserVoiceNotes();
            }
            else {
                clientes.broadcastMessage(clientName + ": " + message);
                addToHistory(clientName + ": " + message); 
            }
        }
    } catch (IOException e) {
        System.out.println("Error en ClientHandler: " + e.getMessage());
    } finally {
        if (clientName != null) {
            clientes.removeUsr(clientName);
            String leaveMessage = clientName + " ha abandonado el chat.";
            clientes.broadcastMessage(leaveMessage);
            addToHistory(leaveMessage);
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


    private static synchronized void addToHistory(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        messageHistory.add(timestamp + " " + message);
    }

    private void sendHistory() {
        out.println("=== Historial de mensajes ===");
        for (String msg : messageHistory) {
            out.println(msg);
        }
        out.println("=== Fin del historial ===");
    }

    private void createGroup(String groupName) {
        if (!groups.containsKey(groupName)) {
            groups.put(groupName, new HashSet<>());
            String message = "Grupo '" + groupName + "' creado por " + clientName;
            out.println(message);
            addToHistory(message);
        } else {
            out.println("El grupo '" + groupName + "' ya existe.");
        }
    }

    private void joinGroup(String groupName) {
        if (groups.containsKey(groupName)) {
            groups.get(groupName).add(out);
            String message = clientName + " se ha unido al grupo '" + groupName + "'";
            out.println("Te has unido al grupo '" + groupName + "'.");
            addToHistory(message);
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
            String fullMessage = "Grupo " + groupName + ": " + clientName + ": " + message;
            for (PrintWriter writer : groups.get(groupName)) {
                writer.println(fullMessage);
            }
            addToHistory(fullMessage);
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
        
        String sanitizedFileName = new File(fileName).getName().replaceAll("[\\\\/:*?\"<>|]", "_");
        Path backupDir = Paths.get("historial/" + clientName);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }
    
        Path filePath = backupDir.resolve(sanitizedFileName);
        String confirmationMessage = clientName + " ha enviado una nota de voz con el nombre " + fileName + " y ha sido recibida correctamente.";
        clientes.privateMessage(target, confirmationMessage);
    
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
    
            long fileSize = dis.readLong();
            System.out.println("Tamaño del archivo a recibir: " + fileSize + " bytes");
    
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
    
            while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.printf("Recibiendo: %.2f%%\r", (totalBytesRead * 100.0) / fileSize);
            }
    
            fos.flush();
            System.out.println("Recepción completada. Total de bytes recibidos: " + totalBytesRead);
    
            if (totalBytesRead == fileSize) {
                System.out.println("\nNota de voz recibida correctamente: " + sanitizedFileName);
            } else {
                System.out.println("\nError: No se recibieron todos los bytes esperados. Se recibieron " + totalBytesRead + " de " + fileSize + " bytes.");
            }
    
            // Enviar la confirmación al destinatario
            //sendVoiceNoteToTarget(sanitizedFileName, target);
        } catch (IOException e) {
            System.out.println("Error al recibir la nota de voz: " + e.getMessage());
            throw e;
        }
    }
    
    

    private void listVoiceNoteHistory() {
        try {
            Path baseDir = Paths.get("historial"); // Directorio base que contiene todas las carpetas de los clientes
            if (Files.exists(baseDir)) {
                out.println("=== Lista de todas las notas de voz guardadas ===");
    
                // Recorrer todas las carpetas de los diferentes clientes
                try (DirectoryStream<Path> clientDirs = Files.newDirectoryStream(baseDir)) {
                    for (Path clientDir : clientDirs) {
                        if (Files.isDirectory(clientDir)) {
                            String clientName = clientDir.getFileName().toString(); // Obtener el nombre del cliente
                            out.println("Notas de voz de " + clientName + ":");
    
                            // Listar los archivos dentro de la carpeta del cliente
                            try (DirectoryStream<Path> voiceNotes = Files.newDirectoryStream(clientDir)) {
                                for (Path voiceNote : voiceNotes) {
                                    out.println("  - " + voiceNote.getFileName().toString());
                                }
                            } catch (IOException e) {
                                out.println("Error al listar notas de voz de " + clientName);
                            }
                        }
                    }
                }
    
                out.println("=== Fin del historial de todas las notas de voz ===");
            } else {
                out.println("No se han guardado notas de voz para ningún usuario.");
            }
        } catch (IOException e) {
            System.out.println("Error al listar notas de voz: " + e.getMessage());
        }
    }

    private void listUserVoiceNotes() throws IOException {
        // Directorio base del historial del usuario actual
        Path userDir = Paths.get("historial/" + clientName);
        if (Files.exists(userDir)) {
            out.println("=== Notas de voz enviadas por " + clientName + " ===");
   
            // Recorrer y listar todos los archivos en la carpeta del usuario
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(userDir)) {
                boolean found = false;
                for (Path path : stream) {
                    out.println(path.getFileName().toString());
                    found = true;
                }
   
                if (!found) {
                    out.println("No se han encontrado notas de voz enviadas.");
                }
            } catch (IOException e) {
                out.println("Error al listar las notas de voz de " + clientName + ": " + e.getMessage());
            }
            out.println("=== Fin de la lista de notas de voz ===");
        } else {
            out.println("No se han encontrado notas de voz para el usuario " + clientName + ".");
        }
    }
    
    
    
    
    
    /* 
    private void sendVoiceNoteToTarget(String fileName, String target) throws IOException {
        Path filePath = Paths.get("server_" + fileName);
        if (Files.exists(filePath)) {
            if (clientes.existeUsr(target)) {
                out.println("Enviando nota de voz.. skibidi.");
                sendVoiceNoteToUser(filePath, target);
            } else if (groups.containsKey(target)) {
                out.println("Enviando nota de voz.. skibidi.");
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
            userWriter.println("Recibiendo nota de voz de " + clientName);  // Enviar mensaje al destinatario
            userWriter.flush();  // Asegurarse de que el mensaje llegue inmediatamente
    
            // Enviar el archivo
            sendFile(filePath, userWriter);
    
            // Enviar la confirmación utilizando broadcastMessage
            String confirmationMessage = "CONFIRMACION: " + clientName + " ha enviado una nota de voz a " + user + " y ha sido recibida correctamente.";
            clientes.broadcastMessage(confirmationMessage);
            System.out.println("Nota de voz enviada a " + user + " con confirmación.");
        } else {
            System.out.println("Error: El usuario " + user + " no está disponible para recibir la nota de voz.");
        }
    }
    
    
    
    

    private void sendVoiceNoteToGroup(Path filePath, String group) throws IOException {
        for (PrintWriter writer : groups.get(group)) {
            writer.println("Recibiendo nota de voz de " + clientName + " en el grupo " + group);  // Enviar mensaje al grupo
            sendFile(filePath, writer);  // Enviar el archivo
            writer.flush();  // Asegurarse de que el mensaje y el archivo se envíen completamente
        }
        System.out.println("Nota de voz enviada al grupo " + group + " correctamente.");
    }
    

    private void sendFile(Path filePath, PrintWriter writer) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
    
            long fileSize = Files.size(filePath);
            dos.writeLong(fileSize);  // Enviar el tamaño del archivo
            dos.flush();  // Asegurarse de que el tamaño se envíe inmediatamente
    
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
    
            // Enviar contenido del archivo
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.printf("Enviando: %.2f%%\r", (totalBytesRead * 100.0) / fileSize);
            }
    
            dos.flush();  // Asegurarse de que todos los datos se envíen
            System.out.println("Nota de voz enviada completamente.");
    
            // Confirmación adicional utilizando broadcastMessage
            String broadcastConfirmation = "CONFIRMACION: Archivo enviado completamente y recibido por el destinatario.";
            clientes.broadcastMessage(broadcastConfirmation);
            System.out.println(broadcastConfirmation);
        } catch (IOException e) {
            System.out.println("Error al enviar el archivo: " + e.getMessage());
            throw e;
        }
    }
         */
    
    
    
    
}