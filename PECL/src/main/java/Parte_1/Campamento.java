/*Esta clase contendrá todas las entradas y actividades.*/
package Parte_1;

import static java.lang.Thread.sleep;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CyclicBarrier;
import javax.swing.JTextField;

/**
 *
 * @author blanf
 */
public class Campamento {
    private boolean entradaIzq = false, entradaDer= false; //Variables almacenan si entradas estan abiertas
    private int aforo,num, capTir = 1, capMer = 20, capSoga = 10, numLimpias = 0, numSucias = 25, numJugadores = 0, numA = 0, numB = 0;     //Aforo maximo del campamento y parte numérica del identificador
    private ArrayList<Child> equiA, equiB; 
    private ListaThreads colaEntradaIzq, colaEntradaDer, colaTirolina, colaMerienda, dentro, monEnTirolina,monEnMerienda,monEnZonaComun, monEnSoga, childEnSoga, childEnMer,limpias,sucias,childEnZc,childEnTirPrep,childEnTir,childEnFinTir,equipoA,equipoB; //Colas de espera y niños dentro de cada actividad y de entrada
    private Semaphore semaforoAforo, semaforoCapTir, semaforoCapMer, semaforoCapSoga; //Variable semaforo para proteger variable aforo
    private Lock cerrojoIzq = new ReentrantLock(); //variable cerrojo para cuando puertaIzq esta cerrada 
    private Lock cerrojoDer = new ReentrantLock(); //variable cerrojo para cuando puerta derecha esta cerrada
    private Lock cerrojoLimpias = new ReentrantLock(); //variable cerrojo para bandejas limpias
    private Lock cerrojoSucias = new ReentrantLock(); //variable cerrojo para bandejas sucias
    private Lock cerrojoSoga = new ReentrantLock();     //Cerrojo para numero de jugadores en soga
    private Condition cerradaIzq = cerrojoIzq.newCondition();   //Varuable condition asociada al cerrojo de la puerta izq
    private Condition cerradaDer = cerrojoDer.newCondition();   //Varuable condition asociada al cerrojo de la puerta derecha
    private Condition bandSucias = cerrojoSucias.newCondition();
    private Condition bandLimpias = cerrojoLimpias.newCondition();
    private Condition jugadores = cerrojoSoga.newCondition();
    private int maxJugadores = 10;  //Numero de jugadores necesarios para jugar a actividad soga
    private CyclicBarrier barreraIni = new CyclicBarrier(maxJugadores); //Paara esperar a que haya 10 jugadores en actividad soga


    /*Constructor de la clase*/
    public Campamento(int aforo,JTextField espEn1,JTextField espEn2,JTextField espTir,JTextField espMer, JTextField den, JTextField monEnTir,JTextField monEnMer,JTextField monEnZC, JTextField monEnSo,JTextField colaMer,JTextField colaTir, JTextField enSoga, JTextField enMer, JTextField limp, JTextField suc, JTextField zc, JTextField tirPrep,JTextField enTir, JTextField finTir, JTextField a,JTextField b)
    {
        this.aforo = aforo;
        semaforoAforo = new Semaphore (aforo,true);
        semaforoCapTir = new Semaphore (capTir, true);
        semaforoCapMer = new Semaphore (capMer, true);
        semaforoCapSoga = new Semaphore (capSoga, true);
        colaEntradaIzq = new ListaThreads (espEn1);
        colaEntradaDer = new ListaThreads (espEn2);
        colaTirolina = new ListaThreads (colaTir);
        colaMerienda = new ListaThreads (colaMer);
        dentro = new ListaThreads (den);
        monEnTirolina = new ListaThreads (monEnTir);
        monEnMerienda = new ListaThreads (monEnMer);
        monEnZonaComun = new ListaThreads (monEnZC);
        monEnSoga = new ListaThreads (monEnSo);
        childEnSoga = new ListaThreads (enSoga);
        childEnMer = new ListaThreads (enMer);
        limpias = new ListaThreads (limp);
        sucias = new ListaThreads (suc);
        childEnZc = new ListaThreads (zc);
        childEnTirPrep = new ListaThreads (tirPrep);
        childEnTir = new ListaThreads (enTir);
        childEnFinTir = new ListaThreads (finTir);
        equipoA = new ListaThreads (a);
        equipoB = new ListaThreads (b);
        equiA = new ArrayList<Child>();
        equiB = new ArrayList<Child>();
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
                    cerrojoIzq.lock();     //Cierra cerrojo puertaIzq
                    sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                    entradaIzq = true;          //Monitor abre entrada
                    cerradaIzq.signalAll();     //Libera hilos que estuviesen esperando en la cola de la puerta izquierda
                } catch (InterruptedException e){ }
                
                finally
                {
                    cerrojoIzq.unlock();   //Abre cerrojo
                }    
            }
            colaEntradaIzq.meter(m.getMId());  //Introduce monitor en la cola (a efectos practicos no espera se hace para observar)
            /*try
            {
               semaforoAforo.acquire();
            } catch(InterruptedException e){ }*/
            colaEntradaIzq.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
            dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento
            }   
        else        //Si id del monitor es impar entra por la puerta derecha
        {
            colaEntradaDer.meter(m.getMId());  //Introduce monitor en la cola (a efectos practicos no espera se hace para observar)
            if(!entradaDer)    /*Si puerta de la entrada derecha esta abierta*/
            {
                try
                {
                    cerrojoDer.lock();     //Cierra cerrojo puertaDer
                    sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                    entradaDer = true;          //Monitor abre entrada
                    cerradaDer.signalAll();     //Libera hilos que estuviesen esperando en la cola de la puerta derecha
                } catch (InterruptedException e){ }
                
                finally
                {
                    cerrojoDer.unlock();   //Abre cerrojo
                }    
            }
            else
            {
                /*try
                {
                    semaforoAforo.acquire();
                } catch(InterruptedException e){ }*/
                colaEntradaDer.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
                dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento         
            }
        }
        num = numId(m);   //guarda parte numérica del identificador del monitor
        accederActividad(m,num);  //Llama a función acceder a actividad 
    }
    
    //Método por el que monitores salen del campamento
    public void salir(Monitor m)
    {
        num = numId(m);
        if (paridadId(num)) 
        {
            dentro.sacar(m.getMId());
        }
        else  
        {
            dentro.sacar(m.getMId());
        }
        if(entradaDer || entradaIzq) semaforoAforo.release();
    }
    
    //Método para que cada monitor accede a actividad correspondiente segun su id
    public void accederActividad(Monitor m, int num)
    {
      switch(num)
      {
        case 1:   //Monitor a actividad soga
            monEnSoga.meter(m.getMId());
            break;
        case 2:     //Monitor entra en actividad tirolina
            monEnTirolina.meter(m.getMId());
            break;
        case 3:     //Monitor entra en actividad merienda
            monEnMerienda.meter(m.getMId());
            break;
        case 4:     //Monitor entra en actividad merienda
            monEnMerienda.meter(m.getMId());
            break;    
      }     
    }
    
      //Método simula actividad soga para los monitores
    public void soga(Monitor m, int n)
    {
        m.contActividades.incrementAndGet();
        if(m.contActividades.intValue() == 10)  //Si monitor ha realizado 10 actividades
        {
            zonaComun(m,n);
            monEnSoga.sacar(m.getMId());
        }
    }
    
    //Método simula funcionamiento de la zona comun para los monitores
    public void zonaComun(Monitor m, int n)
    {
        try 
        {
            monEnZonaComun.meter(m.getMId());
            sleep(1000 + (int) (1000* Math.random()));  //Monitor descansa entre 1 y 2 segundos enona comun
            monEnZonaComun.sacar(m.getMId());
            accederActividad(m,n);   //Llama a función para volver a su actividad
        }catch (InterruptedException e){ } 
    }

    
    //Método para que niños entren en el campamento(una puerta u otra segun si id es par)
    public void entrar(Child c)
    {
        num = numId(c);
        /*Si identificador del niño es par se va a la primera puerta (asignamos puertas segun id)*/
        if(paridadId(num))
        {
            colaEntradaIzq.meter(c.getCId());  //Introduce niño en la cola hasta que se abra puerta 
            if(!entradaIzq) //si la puerta izquierda esta cerrada
            {
                try
                {
                    cerrojoIzq.lock();
                    while(!entradaIzq)
                    {
                        try
                        {
                            cerradaIzq.await();     //hilo se bloquea hasta que se abra puerta
                        } catch(InterruptedException ie){ }
                    }
                }
                finally
                {
                    cerrojoIzq.unlock();
                }
            }
            
            else    /*Si puerta de la entrada izquierda esta abierta*/
            {
                try
                {
                   semaforoAforo.acquire();          //adquiere semáforo (-1 permiso disponible)
                } catch(InterruptedException e){ }
                colaEntradaIzq.sacar(c.getCId());            //Sacamos a niño de la cola de espera          
                dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado niño en el campamento
            }
        }    
        else        //Si id del monitor es impar entra por la puerta derecha
        {
           colaEntradaDer.meter(c.getCId());  //Introduce niño en la cola (a efectos practicos no espera se hace para observar)
           if(!entradaDer)    /*Si puerta de la entrada derecha esta abierta*/
            {
                try
                {
                    cerrojoDer.lock();
                    while(!entradaDer)
                    {
                        try
                        {
                            cerradaDer.await();     //hilo se bloquea hasta que se abra puerta
                        } catch(InterruptedException ie){ }
                    }
                }
                finally
                {
                    cerrojoDer.unlock();
                }
            }
           else 
           {
                try
                {
                    semaforoAforo.acquire();
                } catch(InterruptedException e){ }
                colaEntradaDer.sacar(c.getCId());            //Sacamos a niño de la cola de espera          
                dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado niño en el campamento    
           }
        }
        num = numId(c);   //guarda parte numérica del identificador
        int n = moduloId(num);    //Saca número del 0 - 2 del identificador
        accederActividad(c,n);  //Llama a función acceder a actividad (la primera vez siempre mismo orden)
    }
    
    //Método niños accedan a actividad correspondiente
     public void accederActividad(Child c, int n)
    {
      n++; //Sumamos uno para conseguin numeros del 1 al
      if(c.contActividades.intValue() == 15)    //Si niños realiza 15 actividades sale del campamento
      {
          dentro.sacar(c.getCId()); //Se saca niño del campamento
      }
      else  //Al niño todavia le faltan actividades por hacer
      {
        switch(n)
        {
          case 1:   //Niño a actividad soga
              childEnSoga.meter(c.getCId());
              try 
              {
                  soga(c, n);     //Llama a función que simula actividad soga
              }catch(InterruptedException e){ }
              break;
          case 2:     //Niño entra en actividad tirolina
              colaTirolina.meter(c.getCId());  //Niño accede a la cola de espera de la tirolina
              try
              {
                  semaforoCapTir.acquire();   //Adquiere permiso
                  childEnTirPrep.meter(c.getCId());  //Accede a parte de preparción de la actividad
                  colaTirolina.sacar(c.getCId());    //Niño deja cola de espera
                  tirolina(c , n);  //Llama a función que simula la actividad de tirolina
              }catch(InterruptedException e){ }
              break;
          case 3:     //Niño entra en actividad merienda
              colaMerienda.meter(c.getCId()); //Entra en la cola para la merienda
              colaMerienda.sacar(c.getCId());
              if(c.contActividades.intValue() >= 3) //Para merendar debe haber realizado mas o tres actividades
              {
                  try
                  {
                      semaforoCapMer.acquire();       //Disminuye en uno permisos del semaforo asociado a la merienda
                      childEnMer.meter(c.getCId());         //Mete a niño en lista de niños que estan merendando
                      merienda(c, n);                 //Llama a función merienda para simular actividad
                  } catch(InterruptedException e){ }   
              }
              else 
              {
                  accederActividad(c , n++ % 3);
              } //Si ha hecho menos de tres actividades vuelve a llamar a la función paar elegir otra
              break;
        }   
      }
    }
     
    //Método simula actividad merienda para los niños
    public void merienda(Child c, int n) throws InterruptedException
    {
        cerrojoLimpias.lock();
        while (numLimpias == 0)     //No hay bandejas limpias disponibles
        { 
            bandLimpias.await();    //Espera hasta que haya bandejas limpias
        }
        try 
        { 
            sleep(7000);        //Tarda 7 segundos en merendar
            numLimpias--;       //Disminuye numero de bandejas limpias y aumenta de sucias
            numSucias++;
            bandSucias.signal(); //Hay bandejas sucias
        } 
        finally 
        { 
            cerrojoLimpias.unlock(); 
        }
        childEnMer.sacar(c.getCId());    //Saca a niño del merendero
        semaforoCapMer.release();
        c.contActividades.incrementAndGet();   //Aumenta en uno el contador de actividades del niño
        zonaComun(c , n);   //Tras terminar actividad niño va a zona comun
    }
    
    //Método simula funcionamiento de la zona común para los niños
    public void zonaComun(Child c, int n)
    {
        try 
        {
            childEnZc.meter(c.getCId());
            sleep(2000 + (int) (2000* Math.random()));  //Niño descansa entre 2 y 4 segundos entre cada actividad
            childEnZc.sacar(c.getCId());
            accederActividad(c, n++ % 3);   //Llama a función para realizar nueva actividad
        }catch (InterruptedException e){ }
    }
     
    //Método simula funcionamiento de la actividad tirolina para los niños
    public void tirolina(Child c, int n)
    {
        try
        {
            sleep(1000);    //Monitor prepara al niño para tirarse
            childEnTirPrep.sacar(c.getCId()); //Niño termina de prepararse
            childEnTir.meter(c.getCId());    //Pasa a estado de tirarse
                                            //Espera señal del monitor para tirarse
            sleep(3000);    //Tarda 3 segundos en llegar al final
            childEnTir.sacar(c.getCId());
            childEnFinTir.meter(c.getCId()); //Cabia a estado final
            sleep(500); //Niño tarda 0,5 segundos en bajarse
            childEnFinTir.sacar(c.getCId());
            semaforoCapTir.release();       //Libera permiso del semaforo
        }catch (InterruptedException e){ }
        c.contActividades.incrementAndGet();   //Aumenta en uno el contador de actividades del niño
        zonaComun(c, n);    //Accede a zona comun para descansar tras actividad
    }
    
    //Método simula funcionamiento de la actividad soga para los niños
    public void soga(Child c, int n) throws InterruptedException
    {
        childEnSoga.sacar(c.getCId());
        if(numJugadores == 10)  //Si hilo llega y ya hay 10 jugadores
        {
            accederActividad(c , n++ % 3);  //No espera y se va a otra actividad
        }
        else
        {   
            //Espera a que hay 10 jugadores
            try {
                if(paridadId(numId(c)) && numA < 5)
                {
                    equipoA.meter(c.getCId());  //Introducimos niño en equipo A
                    equiA.add(c);
                    numA++;         //Aumentamos número de jugadores en equipo
                }
                else
                {
                    equipoB.meter(c.getCId());  //Introducimos niño en equipo B
                    equiB.add(c);
                    numB++;             //Aumentamos número de jugadores en equipo B
                }
                numJugadores++;
                barreraIni.await();
                sleep(7000);    //Tardan 7 segundos en realizar actividad
                numJugadores--;     //Disminuye numero de jugadores en actividad
                contSoga++;
                //Vacia los equipos y da puntos correspondientes
                if(paridadId(numId(c)))
                {
                    equipoA.sacar(c.getCId());  //sacamos  niño de equipo A
                    equiA.remove(c);
                    numA--;         //Disminuimos número de jugadores en equipo
                    c.contActividades.addAndGet(2); //Equipo a siempre gana porque juego esta amañado
                    zonaComun(c,n); //Tras terminar actividad niños van a zona comun
                }
                else
                {
                    equipoB.sacar(c.getCId());  //sacamos niño de equipo B
                    equiB.remove(c);
                    numB--;             //Disminuimos número de jugadores en equipo B
                    c.contActividades.incrementAndGet();
                    zonaComun(c,n); //Tras terminar actividad niños van a zona comun
                }
            } catch (Exception e) { }  
        }
    }
    
    
    //Método para cerrar TODO el campamento
    public boolean cerrar() {
        if(entradaIzq || entradaDer)    //Si una de las dos entradas estan abiertas las cierra
        {
            semaforoAforo.drainPermits();
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
        semaforoAforo.release(aforo);
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
    
    //Método separa parte numérica del id (para monitores)
    public int numId(Monitor m)
    {
        String id = m.getMId();  //Guarda identificador del monitor
        String[] a = id.split("M");  //Convierte identificador en array
        int n = Integer.valueOf(a[1]);  //Convierte segunda parte del identificador (parte numérica) a int
        return n;
    }
    
    //Método separa parte numérica del id(para los niños)
    public int numId(Child c)
    {
        String id = c.getCId();  //Guarda identificador del niño
        String[] a = id.split("N");  //Convierte identificador en array sepando letra del número
        int n = 0; //Almacena parte numérica
        for(int i = 1; i < a.length; i++ )      //Bucle recorre parte numérica
        {
            n += Integer.valueOf(a[i]);  //Convierte segunda parte del identificador (parte numérica) a int
        }
        return n;
    }
    
    //Método saca un solo número del 1 al 4 de la parte numérica del identificador de un niño
    public int moduloId(int n)
    {
        return n % 3;       //Módulo 3 de parte numérica
    }
}

