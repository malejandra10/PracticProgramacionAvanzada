/*esta clase servirá para detener y reanudar parando los hilos en la primera sentencia que se permita la ejecución del programa
 */
package Parte_1;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author blanf
 */
public class Detener {
    private boolean cerrado=false;      //Determina si se cierra campamento
    private Lock cerrojo = new ReentrantLock(); //variable cerrojo 
    private Condition parar = cerrojo.newCondition();   //Varuable condition asociada al cerrojo

    /*
    public void mirar()
    {
        try
        {
            cerrojo.lock();
            while(cerrado)
            {
                try
                {
                    parar.await();
                } catch(InterruptedException ie){ }
            }
        }
        finally
        {
            cerrojo.unlock();
        }
    }
*/
    //Método para reanudar ejecución del programa
    public void abrir()
    {
        try
        {
            cerrojo.lock();     //Cierra cerrojo
            cerrado=false;
            parar.signalAll();  //Libera hilos que estuviesen esperando
        }
        finally
        {
            cerrojo.unlock();   //Abre cerrojo
        }
    }
    
    //Método para deterner la ejecución del programa
    public void cerrar()
    {
        try
        {
            cerrojo.lock();     //Bloquea cerrojo
            cerrado=true;       //Cierra campamento
        }
        finally
        {
            cerrojo.unlock();   //Desbloquea cerrojo
        }
    }
}
