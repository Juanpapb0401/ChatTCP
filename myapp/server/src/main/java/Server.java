import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private static Map<String, Set<PrintWriter>> groups = new HashMap<>();
    private static Chatters clientes = new Chatters();
    private static final int PORT = 6565;
    private static final int FILE_PORT = 7000;
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) {
        try (ServerSocket messageServerSocket = new ServerSocket(PORT);
             ServerSocket fileServerSocket = new ServerSocket(FILE_PORT)) {

            System.out.println("Servidor iniciado. Esperando clientes...");
            System.out.println("Servidor de mensajes en puerto: " + PORT);
            System.out.println("Servidor de archivos en puerto: " + FILE_PORT);

            Thread messageThread = new Thread(() -> handleConnections(messageServerSocket, "mensaje", running));
            Thread fileThread = new Thread(() -> handleConnections(fileServerSocket, "archivo", running));

            messageThread.start();
            fileThread.start();

            // Esperar a que el usuario presione Enter para detener el servidor
            System.out.println("Presione Enter para detener el servidor");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

            running.set(false);
            messageServerSocket.close();
            fileServerSocket.close();

            messageThread.join();
            fileThread.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Servidor detenido.");
    }

    private static void handleConnections(ServerSocket serverSocket, String type, AtomicBoolean running) {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión de " + type + ": " + clientSocket);
                if (type.equals("mensaje")) {
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientes, groups);
                    new Thread(clientHandler).start();
                } else {
                    new Thread(() -> handleFileTransfer(clientSocket)).start();
                }
            } catch (SocketException e) {
                if (running.get()) {
                    System.out.println("Error al aceptar conexión de " + type + ": " + e.getMessage());
                }
            } catch (IOException e) {
                System.out.println("Error al aceptar conexión de " + type + ": " + e.getMessage());
            }
        }
    }

    private static void handleFileTransfer(Socket fileSocket) {
        try (DataInputStream dis = new DataInputStream(fileSocket.getInputStream());
             FileOutputStream fos = new FileOutputStream("server_received_file.tmp")) {

            long fileSize = dis.readLong();
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.printf("Recibiendo archivo: %.2f%%\r", (totalBytesRead * 100.0) / fileSize);
            }
            fos.flush();
            System.out.println("\nArchivo recibido correctamente.");
        } catch (IOException e) {
            System.out.println("Error al recibir archivo: " + e.getMessage());
        }
    }
}