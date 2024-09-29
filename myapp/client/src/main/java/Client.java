import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "localhost"; // Dirección IP del servidor
    private static final int PORT = 6565; // Puerto del servidor

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            System.out.println("Conectado al servidor.");

            String message;
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Solicitar el nombre de usuario y manejar la respuesta del servidor
            while ((message = in.readLine()) != null) {
                if (message.startsWith("SUBMITNAME")) {
                    System.out.print("Ingrese nombre de usuario: ");
                    String name = userInput.readLine();
                    out.println(name);
                } else if (message.startsWith("NAMEACCEPTED")) {
                    System.out.println("Nombre aceptado!!");
                    break;
                }
            }

            // Iniciar un hilo para escuchar mensajes del servidor
            Lector lector = new Lector(in);
            new Thread(lector).start();

            // Manejar la entrada del usuario para enviar mensajes y notas de voz
            while ((message = userInput.readLine()) != null) {
                if (message.startsWith("/sendvoicenote")) {
                    // Formato: /sendvoicenote <usuario|grupo> <nombreArchivo>
                    String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        String fileName = parts[2];
                        sendVoiceNote(fileName, socket); // Enviar la nota de voz
                        out.println(message); // Enviar el comando al servidor
                    } else {
                        System.out.println("Uso incorrecto del comando. Formato: /sendvoicenote <usuario|grupo> <nombreArchivo>");
                    }
                } else {
                    out.println(message); // Enviar mensajes regulares
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para enviar una nota de voz desde el cliente
    private static void sendVoiceNote(String fileName, Socket socket) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Archivo no encontrado: " + fileName);
            return;
        }

        // Enviar el archivo de audio al servidor
        try (FileInputStream fis = new FileInputStream(file);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }
            System.out.println("Nota de voz enviada: " + fileName);
        } catch (IOException e) {
            System.out.println("Error al enviar la nota de voz: " + e.getMessage());
        }
    }
}
