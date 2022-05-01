/*esta clase servirá para detener y reanudar parando los hilos en la primera sentencia que se permita la ejecución del programa
 */
package Parte_2;

import Parte_1.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author blanf
 */
public class Detener {
    private boolean detenido=false;      //Determina si se cierra campamento
    private Lock cerrojo = new ReentrantLock(); //variable cerrojo 
    private Condition parar = cerrojo.newCondition();   //Varuable condition asociada al cerrojo

    /*Clase para hacer esperar los hilos cuando se detenga ejecución correctamente*/
    public void comprobar()
    {
        try
        {
            cerrojo.lock();
            while(detenido)  //Mientras la ejecución este detenida
            {
                try
                {
                    parar.await();  //Hilo espera hasta que se reanude ejecución
                } catch(InterruptedException ie){ }
            }
        }
        finally
        {
            cerrojo.unlock();
        }
    }

    //Método para reanudar ejecución del programa
    public void reanudar()
    {
        try
        {
            cerrojo.lock();     //Cierra cerrojo
            detenido = false;      //Actualiza estado de ejecucióna no detenido
            parar.signalAll();  //Libera hilos que estuviesen esperando
        }
        finally
        {
            cerrojo.unlock();   //Abre cerrojo
        }
    }
    
    //Método para detener la ejecución del programa
    public void detener()
    {
        try
        {
            cerrojo.lock();     //Bloquea cerrojo
            detenido = true;       //Indica que se ha detenido la ejeución
        }
        finally
        {
            cerrojo.unlock();   //Desbloquea cerrojo
        }
    }
}
