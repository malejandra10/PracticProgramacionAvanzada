/*Esta clase contendrá todas las entradas y actividades.*/
package Parte_1;

import static java.lang.Thread.sleep;
import java.lang.reflect.Array;
import java.util.concurrent.Semaphore;
import javax.swing.JTextField;

/**
 *
 * @author blanf
 */
public class Campamento {
    private boolean entradaIzq = false, entradaDer= false; //Variables almacenan si entradas estan abiertas
    private int aforo, num ;     //Aforo maximo del campamento y parte numérica del identificador
    private ListaThreads colaEntradaIzq, colaEntradaDer, colaTirolina, colaMerienda, dentro, enTirolina,enMerienda,enZonaComun, enSoga; //Colas de espera y niños dentro de cada actividad y de entrada
    private Semaphore semaforo; //Variable semaforo para proteger variable aforo

    /*Constructor de la clase*/
    public Campamento(int aforo,JTextField espEn1,JTextField espEn2,JTextField espTir,JTextField espMer, JTextField den, JTextField enTir,JTextField enMer,JTextField enZC, JTextField enSo,JTextField colaMer,JTextField colaTir)
    {
        this.aforo=aforo;
        semaforo=new Semaphore(aforo,true);
        colaEntradaIzq=new ListaThreads(espEn1);
        colaEntradaDer=new ListaThreads(espEn2);
        colaTirolina=new ListaThreads(colaTir);
        colaMerienda=new ListaThreads(colaMer);
        dentro=new ListaThreads(den);
        enTirolina=new ListaThreads(enTir);
        enMerienda=new ListaThreads(enMer);
        enZonaComun=new ListaThreads(enZC);
        enSoga=new ListaThreads(enSo);
    }
    
    /*Método override que permite entrar en campamento a monitores*/
    public void entrar(Monitor m)
    {
        num = numId(m);
        /*Si identificador de monitor es par se va a la primera puerta (asignamos puertas segun id)*/
        if(paridadId(num))
        {
            if(!entradaIzq)    /*Si puerta de la entrada izquierda esta cerrada*/
            {
                try
               {
                    sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                    entradaIzq = true;  //Monitor abre entrada
               } catch (InterruptedException e){ }
            }
            colaEntradaIzq.meter(m,1);  //Introduce monitor en la cola (a efectos practicos no espera se hace para observar)
            try
            {
               semaforo.acquire();
            } catch(InterruptedException e){ }
            colaEntradaIzq.sacar(m,1);            //Sacamos a monitor de la cola de espera          
            //dentro.meter(m,1);                  //Llamamos a meter para indicar que ha entrado monitor en el campamento
            }   
        else        //Si id del monitor es impar entra por la puerta derecha
        {
           if(!entradaDer)    /*Si puerta de la entrada derecha esta abierta*/
            {
                try
                {
                    sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                    entradaDer = true;  //Monitor abre entrada
                } catch (InterruptedException e){ }
            }
            colaEntradaDer.meter(m,2);  //Introduce monitor en la cola (a efectos practicos no espera se hace para observar)
            try
            {
                semaforo.acquire();
            } catch(InterruptedException e){ }
            colaEntradaDer.sacar(m,2);            //Sacamos a monitor de la cola de espera          
            //dentro.meter(m,2);                  //Llamamos a meter para indicar que ha entrado monitor en el campamento         
        }
        accederActividad(m);
    }
    
    //Método por el que monitores salen del campamento
    public void salir(Monitor m)
    {
        num = numId(m);
        if (paridadId(num)) 
        {
            dentro.sacar(m,1);
        }
        else  
        {
            dentro.sacar(m,2);
        }
        if(entradaDer || entradaIzq) semaforo.release();
    }

    //Método para que cada monitor accede a actividad correspondiente segun su id
    public void accederActividad(Monitor m)
    {
      num = numId(m);  
      switch(num)
      {
        case 1:   //Monitor a actividad soga
            enSoga.accederAct(m, num);
            break;
        case 2:     //Monitor entra en actividad tirolina
            enTirolina.accederAct(m, num);
            break;
        case 3:     //Monitor entra en actividad merienda
            enMerienda.accederAct(m, num);
            break;
        case 4:     //Monitor entra en actividad merienda
            enMerienda.accederAct(m, num);
            break;    
      }     
    }
    //Método para cerrar TODO el campamento
    public boolean cerrar() {
        if(entradaIzq || entradaDer)    //Si una de las dos entradas estan abiertas las cierra
        {
            semaforo.drainPermits();
            entradaIzq = false;
            entradaDer = false;
        }   
        return false;
    }
    
    //Método para abrir TODO el campamento
    public boolean abrir()
    {
      if(!entradaIzq && !entradaDer)
      {
        semaforo.release(aforo);
        entradaDer = true;
        entradaIzq = true;
      }
      return true;
    }
    
    /*Método determina si número es par*/
    public boolean paridadId(int n)
    {
        boolean par = false;    //Variable almacena si parte numérica del id es par
        if(n%2 == 0)        par = true;   //Si parte numérica es par cambia variable a true
        
        return par;
    }
    
    //Método separa parte numérica del id
    public int numId(Monitor m)
    {
        String id = m.getMId();  //Guarda identificador del monitor
        String[] a = id.split("M");  //Convierte identificador en array
        int n = Integer.valueOf(a[1]);  //Convierte segunda parte del identificador (parte numérica) a int
        return n;
    }
}
