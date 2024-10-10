import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Client {
    private static final String SERVER_IP = "localhost"; // Asegúrate de cambiar esto a la IP correcta de tu servidor
    private static final int PORT = 6565;
    private static final int FILE_PORT = 7000;

    public static void main(String[] args) {
        try {
            // Intentar conectar al servidor de mensajes
            Socket messageSocket = new Socket(SERVER_IP, PORT);
            System.out.println("Conectado al servidor para mensajes.");

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(messageSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));

            // Manejar la autenticación del usuario
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("SUBMITNAME")) {
                    System.out.print("Ingrese nombre de usuario: ");
                    String name = userInput.readLine();
                    out.println(name);
                } else if (message.startsWith("NAMEACCEPTED")) {
                    System.out.println("Nombre aceptado. ¡Bienvenido al chat!");
                    break;
                }
            }

            // Iniciar un hilo para escuchar mensajes del servidor
            Lector lector = new Lector(in);
            new Thread(lector).start();

            // Manejar la entrada del usuario para enviar mensajes y notas de voz
            while (true) {
                String input = userInput.readLine();
                if (input.startsWith("/sendvoicenote")) {
                    String[] parts = input.split(" ", 3);
                    if (parts.length == 3) {
                        String target = parts[1];
                        String fileName = parts[2];
                        sendVoiceNote(target, fileName, out);
                    } else {
                        System.out.println("Uso: /sendvoicenote <usuario|grupo> <nombreArchivo>");
                    }
                } else {
                    out.println(input);
                }
            }
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendVoiceNote(String target, String fileName, PrintWriter out) {
        Path filePath = Paths.get(fileName);
        if (!Files.exists(filePath)) {
            System.out.println("El archivo no existe: " + fileName);
            return;
        }

        try (Socket fileSocket = new Socket(SERVER_IP, FILE_PORT);
             DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
             FileInputStream fis = new FileInputStream(filePath.toFile())) {

            // Enviar comando al servidor de mensajes
            out.println("/sendvoicenote " + target + " " + fileName);

            // Enviar tamaño del archivo
            long fileSize = Files.size(filePath);
            dos.writeLong(fileSize);

            // Enviar contenido del archivo
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.printf("Enviando: %.2f%%\r", (totalBytesRead * 100.0) / fileSize);
            }
            dos.flush();
            System.out.println("\nNota de voz enviada: " + fileName);
        } catch (IOException e) {
            System.out.println("Error al enviar la nota de voz: " + e.getMessage());
        }
    }

    
}
