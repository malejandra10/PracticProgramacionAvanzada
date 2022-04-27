/*Clase que crearfichero log donde escirbiremos los estados de los hilos del sistema.
 */
package Parte_1;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 *
 * @author blanf
 */
public class Log 
{
    private FileWriter log; //Objeto FileWriter usado para escribir en el archivo
    private DateTimeFormatter fecha = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");   //Formato que usaremos para escribir la fecha y el tiempo

    /*Constructor de la clase*/
    public Log(FileWriter l) {
        this.log = l;
        try
        {
            this.log = new FileWriter("evolucionCampamento.txt",true); //Crea objeto file(true es para no reescribir si no añadir)
        }catch(IOException ioe){}
    }
    
    //Método para escribir en archivo log
    public synchronized void escribir(String texto) 
    {
        try
        {
            log.write(fecha.format(LocalDateTime.now()) + texto);   //Escribe fecha y hora actual y el texto especificado 
            log.flush();    //Guardo lo escrito en el fichero
            //log.close();
        }catch(IOException ioe){}
    }
    
    //Método para cerrar archivo
    public synchronized void cerrar()
    {
       try
        {
            log.close();  //Cierro el archivo log
        }catch(IOException ioe){} 
    }
}
