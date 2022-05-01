/*cliente qu ese conectara al servidor.
 */
package Parte_2;
import java.io.*;
import java.net.*;
/**
 *
 * @author blanf
 */
public class Cliente {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Socket cliente;
        DataInputStream entrada;
        DataOutputStream salida;
        String mensaje, respuesta;
        try
        {
            cliente = new Socket(InetAddress.getLocalHost(),5000);   //Creamos el socket para conectarnos al puerto 5000 del servidor
            cliente.close();                                          // Cerramos la conexión
        }
        catch (IOException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
   