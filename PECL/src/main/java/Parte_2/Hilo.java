/*Clase hilo para poder gestionar conexiones de varios clientes uy que muestra intefaz una vez conectados*/
package Parte_2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author blanf
 */
public class Hilo extends Thread {
    private int num;
    private Socket conexion;
    private DataOutputStream salida;
    private DataInputStream entrada;

    public Hilo(Socket conexion) {
        this.conexion = conexion;
    }
    
    public void run()
    {
        num++;
        System.out.println("Conexión nº "+ num +" desde: "+ conexion.getInetAddress().getHostName());
        try
        {
            //Muestra la interfaz que se usará para que cliente haga consultas
                java.awt.EventQueue.invokeLater(new Runnable() 
                {
                    public void run() 
                    {
                       Interfaz in = new Interfaz();
                       in.setVisible(true);
                    }
                 });
            conexion.close(); // Y cerramos la conexión
        } catch (IOException e) {}
    }
}
