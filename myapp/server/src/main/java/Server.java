import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static Map<String, Set<PrintWriter>> groups = new HashMap<>(); // Mapa para gestionar grupos de chat
    private static Chatters clientes = new Chatters();

    public static void main(String[] args) {
        int PORT = 6565;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado. Esperando clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket);
                // Crear el objeto ClientHandler y pasarle el mapa de grupos
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientes, groups);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
