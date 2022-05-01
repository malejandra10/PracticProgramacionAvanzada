/*Servidor al que se conectarán los clientes para relaizar métodos de consulta
 */
package Parte_2;
import java.io.*;
import java.net.*;

 
/**
 *
 * @author blanf
 */
public class Servidor {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerSocket servidor;
        Socket conexion;
        DataOutputStream salida;
        DataInputStream entrada;
        int num = 0;
        try
        {
            servidor = new ServerSocket(5000); // Creamos un ServerSocket en el puerto 5000
            System.out.println("Servidor Arrancado....");
            while (true)
            {
                conexion = servidor.accept();     // Esperamos una conexión
                Hilo h = new Hilo (conexion);   //Crea hilo para poder recibir conexiones de varios clientes
                h.start();
                servidor.close();                          // Y cerramos la conexión
            }
        } catch (IOException e) {}
    }             
}
 
