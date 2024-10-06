import java.io.*;
import java.net.*;

public class Lector implements Runnable{
    String message;
    BufferedReader in;
    public Lector(BufferedReader in){
        this.in=in;
    }

    @Override
    public void run() {
        // Leer la línea que envía el servidor e imprimir en pantalla
        try {
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (SocketException e) {
            System.err.println("Socket cerrado: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de entrada/salida: " + e.getMessage());
        } finally {
            try {
                in.close();  // Cerrar BufferedReader al finalizar
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}