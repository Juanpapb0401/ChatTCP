import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Chatters {
    private Map<String, PrintWriter> clientes = new HashMap<>(); // Estructura HashMap para almacenar usuarios y sus PrintWriter

    // Agregar un usuario al HashMap
    public synchronized void addUsr(String userName, PrintWriter out) {
        clientes.put(userName, out); // Se agrega el usuario y su PrintWriter
    }

    // Remover un usuario del HashMap
    public synchronized void removeUsr(String userName) {
        clientes.remove(userName); // Remover por nombre de usuario
    }

    // Enviar un mensaje a todos los usuarios
    public synchronized void broadcastMessage(String message) {
        for (PrintWriter writer : clientes.values()) {
            writer.println(message); // Envía el mensaje a todos los clientes conectados
        }
    }

    // Enviar un mensaje privado a un usuario específico
    public synchronized void privateMessage(String user, String message) {
        PrintWriter out = clientes.get(user); // Obtener el PrintWriter del usuario destino
        if (out != null) {
            out.println(message); // Enviar el mensaje privado si el usuario existe
        } else {
            System.out.println("Usuario no encontrado: " + user); // Manejar caso de usuario no encontrado
        }
    }

    // Comprobar si un usuario ya existe
    public synchronized boolean existeUsr(String userName) {
        return clientes.containsKey(userName); // Verificar si el usuario ya está registrado en el sistema
    }
}

