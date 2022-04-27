/*Esta clase contendrá todas las entradas y actividades.*/
package Parte_1;

import java.io.FileWriter;
import static java.lang.Thread.sleep;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JTextField;

/**
 *
 * @author blanf
 */
public class Campamento {
    private boolean entradaIzq = false, entradaDer= false, finSoga = true; //Variables almacenan si entradas estan abiertas
    private int aforo,num, capTir = 1, capMer = 20, capSoga = 10, numLimpias = 0, numSucias = 25, numJugadores = 0, numA = 0, numB = 0,contSoga = 0, contTir = 0,contMer = 0,contMer2 = 0;     //Aforo maximo del campamento y parte numérica del identificador
    private ArrayList<Child> equiA, equiB;  //Arrays que contendrán miembros de cada equipo en actividad soga
    private ListaThreads colaEntradaIzq, colaEntradaDer, colaTirolina, colaMerienda, dentro, monEnTirolina,monEnMerienda,monEnZonaComun, monEnSoga, childEnSoga, childEnMer,limpias,sucias,childEnZc,childEnTirPrep,childEnTir,childEnFinTir,equipoA,equipoB; //Colas de espera y niños dentro de cada actividad y de entrada
    private Semaphore semaforoAforo, semaforoCapTir, semaforoCapMer, semaforoCapSoga; //Variable semaforo para proteger variable aforo
    private Lock cerrojoIzq = new ReentrantLock(); //variable cerrojo para cuando puertaIzq esta cerrada 
    private Lock cerrojoDer = new ReentrantLock(); //variable cerrojo para cuando puerta derecha esta cerrada
    private Lock cerrojoLimpias = new ReentrantLock(); //variable cerrojo para bandejas limpias
    private Lock cerrojoSucias = new ReentrantLock(); //variable cerrojo para bandejas sucias
    private Lock cerrojoBandejas = new ReentrantLock(); //cerrojo para exclusión mutua de actividad merienda
    private Condition cerradaIzq = cerrojoIzq.newCondition();   //Variable condition asociada al cerrojo de la puerta izq
    private Condition cerradaDer = cerrojoDer.newCondition();   //Variable condition asociada al cerrojo de la puerta derecha
    private Condition limp = cerrojoBandejas.newCondition();    //Para esperar si no hay bandejas limpias
    private Condition suc = cerrojoBandejas.newCondition(); //Para esperar si no hay bandejas sucias
    private int maxJugadores = 10;  //Numero de jugadores necesarios para jugar a actividad soga
    private CyclicBarrier barreraSoga = new CyclicBarrier(maxJugadores); //Para esperar a que haya 10 jugadores en actividad soga
    private CyclicBarrier des = new CyclicBarrier(2);   //Controla que ambos monitores vayan a la vez al descanso
    private CountDownLatch contadorSoga = new CountDownLatch (100);  //Para bloquear monitor mientras se realiza actividad soga
    private CountDownLatch contadorTirolina = new CountDownLatch (10);  //Para bloquear monitor mientras se realiza actividad tirolina
    private CountDownLatch contadorMerienda = new CountDownLatch (10);  //Para bloquear monitor mientras se realiza actividad merienda
    private Semaphore esperaTir = new Semaphore (0, true);
    private Semaphore esperaSoga = new Semaphore (100, true);   //Para que niños esperen a la vuelta del monitor del descanso
    private Semaphore esperaMer = new Semaphore (10, true); //Niños esperen a la vuelta del monitor del descanso en actividad merienda
    private Semaphore siguiente = new Semaphore (1, true);  //Controla que salgan niños de uno en uno de la actividad soga
    private Semaphore descanso = new Semaphore (1, true);   //Solo puedan ir monitores de merienda de uno a uno a descanso
    private Semaphore servir = new Semaphore (numLimpias, true);    //Controla que niños no cojan bandeja si no hay limpias disponibles
    private Semaphore  limpiar= new Semaphore (numSucias, true); //Controla que monitores se bloqueen si no hay bandejas sucias que limpiar
    private Detener detener;    //Para detener y reanudad ejecución del programa
    private Log log;    //Para escribir en archivo evolción de la ejecución del programa
    private FileWriter file;    //Archivo dónde se escribirá evolución del programa
    private CountDownLatch señal = new CountDownLatch (1);  //Para niño se tire cuando monitor de señal en actividad tirolina
    private CountDownLatch empezarTir = new CountDownLatch (1); //Niño espere hasta que monitor este en actividad (la primera vez que llega) 
    
    
    
    /*Constructor de la clase*/
    public Campamento(int aforo,JTextField espEn1,JTextField espEn2,JTextField espTir,JTextField espMer, JTextField den, JTextField monEnTir,JTextField monEnMer,JTextField monEnZC, JTextField monEnSo,JTextField colaMer,JTextField colaTir, JTextField enSoga, JTextField enMer, JTextField limp, JTextField suc, JTextField zc, JTextField tirPrep,JTextField enTir, JTextField finTir, JTextField a,JTextField b,Detener deten)
    {
        this.aforo = aforo;
        this.detener = deten;
        this.log = new Log(file);
        semaforoAforo = new Semaphore (aforo,true); //aforo es el número de èrmisos y true para indicar salida FIFO de la cola
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
        limpias.meter(" " + 0);
        sucias.meter(" " + 25);
        
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
                cerrojoIzq.lock();     //Cierra cerrojo puertaIzq
                try
                {
                    if(!entradaIzq)
                    {
                        sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                        entradaIzq = true;          //Monitor abre entrada
                        log.escribir(" Monitor " + m.getMId() + " abre puerta izquierda\n");    //Escribe estado del monitor en archivo
                        cerradaIzq.signalAll();     //Libera hilos que estuviesen esperando en la cola de la puerta izquierda
                        colaEntradaIzq.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
                        dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento 
                    }
                    else
                    {
                       colaEntradaIzq.sacar(m.getMId());            //Sacamos a monitor de la cola de espera    
                       log.escribir(" Monitor " + m.getMId() + " entra por la puerta izquierda\n"); //Escribe estado del monitor en archivo
                       dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento 
                    }
                } catch (InterruptedException e){ }
                
                finally
                {        
                    cerrojoIzq.unlock();   //Abre cerrojo
                }    
            }
            else
            {
                colaEntradaIzq.sacar(m.getMId());            //Sacamos a monitor de la cola de espera   
                log.escribir(" Monitor " + m.getMId() + " entra por la puerta izquierda\n"); //Escribe estado del monitor en archivo
                dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento         
            }
            
            }   
        else        //Si id del monitor es impar entra por la puerta derecha
        {
            colaEntradaDer.meter(m.getMId());  //Introduce monitor en la cola (a efectos practicos no espera se hace para observar)
            if(!entradaDer)    /*Si puerta de la entrada derecha esta abierta*/
            {
                cerrojoDer.lock();     //Cierra cerrojo puertaDer
                try
                {
                   if(!entradaDer)
                   {
                        sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                        entradaDer = true;          //Monitor abre entrada
                        log.escribir(" Monitor " + m.getMId() + " abre puerta derecha\n"); //Escribe estado del monitor en archivo
                        cerradaDer.signalAll();     //Libera hilos que estuviesen esperando en la cola de la puerta derecha
                        colaEntradaDer.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
                        dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento 
                   }
                   else
                   {
                       colaEntradaDer.sacar(m.getMId());            //Sacamos a monitor de la cola de espera     
                       log.escribir(" Monitor " + m.getMId() + " entra por la puerta derecha\n"); //Escribe estado del monitor en archivo
                       dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento      
                   }
                } 
                catch (InterruptedException e){ }
                
                finally
                {
                    cerrojoDer.unlock();   //Abre cerrojo    
                }    
            }
            else
            {
                colaEntradaDer.sacar(m.getMId());            //Sacamos a monitor de la cola de espera
                log.escribir(" Monitor " + m.getMId() + " entra en por la puerta derecha\n"); //Escribe estado del monitor en archivo
                dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento         
            }
        }
        num = numId(m);   //guarda parte numérica del identificador del monitor
        accederActividad(m,num);  //Llama a función acceder a actividad 
    }
    
    //Método por el que monitores salen del campamento
    public void salir(Monitor m)
    {
        dentro.sacar(m.getMId()); //Saca a monitor del campamento
        log.escribir("Monitor " + m.getMId() + "sale del campamento\n"); //Escribe estado del monitor en archivo
    }
    
    //Método para que cada monitor accede a actividad correspondiente segun su id
    public void accederActividad(Monitor m, int num)
    {
      detener.comprobar();  //Punto de detención de la ejecución
      switch(num)
      {
        case 1:   //Monitor a actividad soga
            log.escribir(" Monitor " + m.getMId() + " accede a actividad soga\n");  //Escribe estado del monitor en archivo
            monEnSoga.meter(m.getMId());    //Monitor accede a actividad soga
            soga(m,num);    //Llama a función soga accediendo monitor
            break;
        case 2:     //Monitor entra en actividad tirolina
            log.escribir(" Monitor " + m.getMId() + " accede a actividad tirolina\n");  //Escribe estado del monitor en archivo
            monEnTirolina.meter(m.getMId());    //Monitor accede a actividad tirolina
            tirolina(m,num);    //Llama a función tirolina accediendo monitor
            break;
        case 3:     //Monitor entra en actividad merienda
            log.escribir(" Monitor " + m.getMId() + " accede a actividad merienda\n");  //Escribe estado del monitor en archivo
            monEnMerienda.meter(m.getMId());    //Accede monitor a actividad merienda
            try
            {
                merienda(m,num);    //Se llama a función merienda
            }
            catch(Exception e){}
            break;
        case 4:     //Monitor entra en actividad merienda
            log.escribir(" Monitor " + m.getMId() + " accede a actividad merienda\n"); //Escribe estado del monitor en archivo
            monEnMerienda.meter(m.getMId());    //Accede monitor a función merienda
            try
            {
                merienda(m,num);    //Se llama a función merienda 
            }
            catch(Exception e){}
            break;    
      }     
    }
    
    //Método para simular el funcionamiento de la merienda por parte de los monitores
    public void merienda (Monitor m, int n) throws InterruptedException
    {
        
        contMer = 0;    //Contador guarda numero de platos limpios servidos reseteandose csda vez que monitores vuelven del descanso
        esperaMer.release(5);  //libera a los niños que estuviesen bloqueados esperando a que el monitor volviese
        
        while(true)
        {
            detener.comprobar();   //Se detiene ejecución del programa en este punto
            if(contMer == 10 || contMer == 11)   //Si sirven 10 comidas monitores se van a descanso
            {
                esperaMer.drainPermits();   //Bloquea a los niños que esten en la actividad
                try
                {
                    des.await();    //Espera a que ambos monitores lleguen
                    descanso.acquire(); //permite pasar a los monitores de uno en uno al descanso
                }catch(Exception e){}
                log.escribir(" Monitor " + m.getMId() + " se va al descanso\n"); //Escribe estado del monitor en archivo
                monEnMerienda.sacar(m.getMId());    //Saca a monitor de la actividad
                zonaComun(m,n); //Manda monitor a zona comun
            }
            limpiar.acquire();  //monitor se bloquea si no hay platos que lavar
            sleep(3000 + (int) (2000 * Math.random())); //Tarda entre 3 y 5 segundos en limpiar y servir
            log.escribir(" Monitor " + m.getMId() + " limpia bandeja\n"); //Escribe estado del monitor en archivo
            contMer++;  //Aumenta número de meriendas llevadas a cabo por ambos monitores
            limpias.sacar(" " + numLimpias);    //Actualiza el número de bandejas limpias sacando anterior valor
            numLimpias++;   //Aumenta número de bandejas limpias disponibles
            limpias.meter(" " + numLimpias);    //Muestra nuevo número de bandejas limpias en interfaz
            sucias.sacar(" " + numSucias);      //Saca antiguo valor de bandejas sucias
            numSucias--;    //Disminuye el número de bandejas sucias
            sucias.meter(" " + numSucias);  //Introduce nuevo número de bandejas sucias
            servir.release();   //avisa de que hay un plato mas limpio disponible
        }
    }
    
    
    //Método simula actividad soga para los monitores
    public void soga(Monitor m, int n)
    {
        esperaSoga.release(100);    //libera permisos para que puedan entrar niños cuando monitor vuelve del descanso
        while(true) //Monitor realiza esta actividad continuamente
        {
         detener.comprobar();  //Ejecución se detendrá en este punto si se indica
            try
            {
                contadorSoga.await();   //espera hasta que actividad se ha realizado 10 veces (es decir han pasado 100 niños)
                esperaSoga.drainPermits();  //bloquea a los niños que entran mientras no haya monitor
                contadorSoga = new CountDownLatch(100); //Reinicializa barrera
                log.escribir(" Monitor " + m.getMId() + " se va al descanso\n");    //Escribe estado del monitor en archivo
                monEnSoga.sacar(m.getMId());    //Se saca monitor de la actividad
                m.sumar(10); //Se suma las veces que s eha realizado actividad
                zonaComun(m,n); //Se envia monitor a la zona comun para que descanse 
            }catch(Exception e){}
        }
    }
    
    //Método simula actividad tirolina para los monitores
    public void tirolina(Monitor m, int n)
    {;
        while(true)
        {
            detener.comprobar();   //Ejecución se detendrá en este punto si se indica
            try
            {
                esperaTir.release(10); //Libera permisos para que niños puede entrar en actividad
                /*sleep(1500 + (int)(500 *Math.random())); //Espera entre 1,5 segundos y 2 segundos para dar la señal a niño de que se tire (no especificado en enunciado)
                señal.countDown(); //Avisa a niño de que puede tirarse
                señal = new CountDownLatch(1);*/
                contadorTirolina.await();   //Espera a que actividad se realice 10 veces
                m.sumar(10);    //Introduce las 10 veces que se ha realizado actividad en contador
                esperaTir.drainPermits();   //Bloquea a niños hasta que vuelva monitor
                contadorTirolina = new CountDownLatch(10);  //reinicializa countdownlatch
                log.escribir(" Monitor " + m.getMId() + " se va al descanso\n");    //Escribe estado del monitor en archivo
                monEnTirolina.sacar(m.getMId());    //Se saca monitor de la actividad
                zonaComun(m,n); //Se envia monitor a la zona comun para que descanse 
            }
            catch(Exception e){}
        }
    }
    
    //Método simula funcionamiento de la zona comun para los monitores
    public void zonaComun(Monitor m, int n)
    {
        detener.comprobar();   //Ejecución se detendrá en este punto
        try 
        {
            if(n == 3 || n == 4)    //Si monitores de merienda se libera al siguiente para que entre
            {
                descanso.release(); //Libera semáforo descanso
            }
            monEnZonaComun.meter(m.getMId());   //Monitor entra en zona comun
            sleep(1000 + (int) (1000* Math.random()));  //Monitor descansa entre 1 y 2 segundos enona comun
            monEnZonaComun.sacar(m.getMId());   //Monitor sale de la zona comun
            log.escribir(" Monitor " + m.getMId() + " vuelve a su actividad\n"); //Escribe estado del monitor en archivo
            accederActividad(m,n);   //Llama a función para volver a su actividad
        }catch (InterruptedException e){ } 
    }

    
    //Método para que niños entren en el campamento(una puerta u otra segun si id es par)
    public void entrar(Child c)
    {
        num = numId(c); //Convierte en entero el identificador del niño
        /*Si identificador del niño es par se va a la primera puerta (asignamos puertas segun id)*/
        if(paridadId(num))
        {
            colaEntradaIzq.meter(c.getCId());  //Introduce niño en la cola hasta que se abra puerta 
            log.escribir(" Niño " + c.getCId() + " intenta entrar al campamento por la puerta izquierda \n"); //Escribe estado del niño en archivo
            if(!entradaIzq) //si la puerta izquierda esta cerrada
            {
                cerrojoIzq.lock();
                try
                {
                    while(!entradaIzq)  //Mientras este cerrada la puerta izquierda
                    {
                        try
                        {
                            log.escribir(" Niño " + c.getCId() + " espera en la cola de la puerta izquierda\n"); //Escribe estado del niño en archivo
                            cerradaIzq.await();     //niño se bloquea hasta que se abra puerta    
                        } catch(InterruptedException ie){ }
                    }
                    log.escribir(" Niño " + c.getCId() + " entra al campamento por la puerta izquierda \n"); //Escribe estado del niño en archivo
                    colaEntradaIzq.sacar(c.getCId());            //Sacamos a monitor de la cola de espera          
                    dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento    
                     
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
                   semaforoAforo.acquire();          //Adquiere semáforo (-1 permiso disponible)
                   colaEntradaIzq.sacar(c.getCId());            //Sacamos a niño de la cola de espera
                   log.escribir(" Niño " + c.getCId() + " entra al campamento por la puerta izquierda \n"); //Escribe estado del niño en archivo
                   dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado niño en el campamento
                } catch(InterruptedException e){ }  
            }
        }    
        else        //Si id del monitor es impar entra por la puerta derecha
        {
           colaEntradaDer.meter(c.getCId());  //Introduce niño en la cola (a efectos practicos no espera se hace para observar)
           log.escribir(" Niño " + c.getCId() + " intenta entrar al campamento por la puerta derecha \n"); //Escribe estado del niño en archivo
           if(!entradaDer)    /*Si puerta de la entrada derecha esta abierta*/
            {
                cerrojoDer.lock();
                try
                {
                    while(!entradaDer)  //Mientras entrada derecha este cerrada
                    {
                        try
                        {
                            log.escribir(" Niño " + c.getCId() + " espera en la cola de la puerta derecha\n"); //Escribe estado del niño en archivo
                            cerradaDer.await();     //hilo se bloquea hasta que se abra puerta
                        } catch(InterruptedException ie){ }
                    }
                    colaEntradaDer.sacar(c.getCId());            //Sacamos a monitor de la cola de espera 
                    log.escribir(" Niño " + c.getCId() + " entra al campamento por la puerta derecha \n"); //Escribe estado del niño en archivo
                    dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento         
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
                    semaforoAforo.acquire();    //Sisminuye un permiso del aforo
                } catch(InterruptedException e){ }
                colaEntradaDer.sacar(c.getCId());            //Sacamos a niño de la cola de espera  
                log.escribir(" Niño " + c.getCId() + " entra al campamento por la puerta derecha \n"); //Escribe estado del niño en archivo
                dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado niño en el campamento    
           }
        }
        num = numId(c);   //Guarda parte numérica del identificador
        int n = moduloId(num);    //Saca número del 0 - 2 del identificador
        accederActividad(c,n);  //Llama a función acceder a actividad (la primera vez siempre mismo orden)
    }
    
    //Método niños accedan a actividad correspondiente
     public void accederActividad(Child c, int n)
    {
      detener.comprobar(); //Ejecución se detendrá en este punto si se indica
      n++; //Sumamos uno para conseguin numeros del 1 al
      if(c.getContActividades() == 15)    //Si niños realiza 15 actividades sale del campamento
      {
          log.escribir(" Niño " + c.getCId() + " sale del campamento\n");   //Escribe estado del niño en archivo
          dentro.sacar(c.getCId()); //Se saca niño del campamento
      }
      else  //Al niño todavia le faltan actividades por hacer
      {
        switch(n)
        {
          case 1:   //Niño a actividad soga
              log.escribir(" Niño " + c.getCId() + " accede a la actividad soga\n");    //Escribe estado del niño en archivo
              childEnSoga.meter(c.getCId());    //Niño entra en actividad soga
              soga(c, n);     //Llama a función que simula actividad soga
              break;
          case 2:     //Niño entra en actividad tirolina
              colaTirolina.meter(c.getCId());  //Niño accede a la cola de espera de la tirolina
              log.escribir(" Niño " + c.getCId() + " accede a la cola de espera de la actividad tirolina\n"); //Escribe estado del niño en archivo
              try
              {  
                  semaforoCapTir.acquire();   //Adquiere permiso de la acpacidad de actividad tirolina
                  childEnTirPrep.meter(c.getCId());  //Accede a parte de preparción de la actividad
                  colaTirolina.sacar(c.getCId());    //Niño deja cola de espera
                  log.escribir(" Niño " + c.getCId() + " accede a la actividad tirolina\n"); //Escribe estado del niño en archivo
                  tirolina(c , n);  //Llama a función que simula la actividad de tirolina
              }catch(InterruptedException e){ }
              break;
          case 3:     //Niño entra en actividad merienda
              if(c.getContActividades() >= 3) //Para merendar debe haber realizado mas o tres actividades
              {
                  try
                  {
                    colaMerienda.meter(c.getCId()); //Entra en la cola para la merienda
                    log.escribir(" Niño " + c.getCId() + " accede a la cola de la la actividad merienda\n"); //Escribe estado del niño en archivo
                    semaforoCapMer.acquire();       //Disminuye en uno permisos del semaforo asociado a la merienda
                    colaMerienda.sacar(c.getCId()); //Saca al nió de la cola de espera
                    childEnMer.meter(c.getCId());         //Mete a niño en lista de niños que estan merendando
                    log.escribir(" Niño " + c.getCId() + " accede a la actividad merienda\n"); //Escribe estado del niño en archivo
                    merienda(c, n);                 //Llama a función merienda para simular actividad
                  } catch(InterruptedException e){ }   
              }
              else  //Si ha realizado menos de tres actividades
              {
                log.escribir(" Niño " + c.getCId() + " selecciona siguiente actividad\n"); //Escribe estado del niño en archivo
                accederActividad(c , n++ % 3);  //Accede a actividad distinta
              } //Si ha hecho menos de tres actividades vuelve a llamar a la función paar elegir otra
              break;
        }   
      }
    }
     
    //Método simula actividad merienda para los niños
    public void merienda(Child c, int n) throws InterruptedException
    {
        detener.comprobar();   //Ejecución se detendrá en este punto
        esperaMer.acquire();    //Adquiere permiso si monitor en actividad
        log.escribir(" Niño " + c.getCId() + " coge plato limpio\n"); //Escribe estado del niño en archivo
        servir.acquire();   //Si no hay platos limpios se bloquea
        sleep(7000);        //Tarda 7 segundos en merendar
        log.escribir(" Niño " + c.getCId() + " merienda\n"); //Escribe estado del niño en archivo
        limpias.sacar(" " + numLimpias);    //Quita el anterior número de bandejas limpias
        numLimpias--;       //Disminuye numero de bandejas limpias y aumenta de sucias
        limpias.meter(" " + numLimpias);    //Introduce nuevo valor de bandejas limpias
        sucias.sacar(" " + numSucias);  //Saca antiguo valor de bandejas sucias
        numSucias++;    //Aumenta el número de bandejas sucias
        sucias.meter(" " + numSucias);  //Introduce nuevo número de bandejas sucias
        limpiar.release();  //Avisa de que hay bandejas que limpiar
        log.escribir(" Niño " + c.getCId() + " deja plato sucio en su pila\n"); //Escribe estado del niño en archivo
        childEnMer.sacar(c.getCId());    //Saca a niño del merendero
        semaforoCapMer.release();   //avisa de que hay un plato sucio 
        c.sumar(1);   //Aumenta en uno el contador de actividades del niño
        log.escribir(" Niño " + c.getCId() + " se va a la zona comun\n"); //Escribe estado del niño en archivo
        zonaComun(c , n);   //Tras terminar actividad niño va a zona comun  
    }
    
    //Método simula funcionamiento de la zona común para los niños
    public void zonaComun(Child c, int n)
    {
        detener.comprobar();   //Punto dónde se detendrá ejecución del programa
        log.escribir(" Niño " + c.getCId() + " accede a la zona comun\n"); //Escribe estado del niño en archivo
        try 
        {
            if(n == 1) //Si niños que llegaba habia estado en actividad soga
            {
                siguiente.release(); //libera hilo estaba esperando a que niño saliese de actividad soga para salir el siguiente
            } 
            childEnZc.meter(c.getCId());    //Mete a niño en la zona comun
            sleep(2000 + (int) (2000* Math.random()));  //Niño descansa entre 2 y 4 segundos entre cada actividad
            childEnZc.sacar(c.getCId());    //Saca a niño de la zona comun
            log.escribir(" Niño " + c.getCId() + " selecciona siguiente actividad\n"); //Escribe estado del niño en archivo
            accederActividad(c, (n+ 1) % 3);   //Llama a función para realizar nueva actividad
        }catch (InterruptedException e){ }
    }
     
    //Método simula funcionamiento de la actividad tirolina para los niños
    public void tirolina(Child c, int n)
    {
        detener.comprobar();   //Punto donde se detendrá funcionaientod el programa
        try
        {
            esperaTir.acquire();    //Niño se bloquea si monitor esta en descanso
            log.escribir(" Niño " + c.getCId() + " se prepara para tirarse en la tirolina\n"); //Escribe estado del niño en archivo
            sleep(1000);    //Monitor prepara al niño para tirarse
            childEnTirPrep.sacar(c.getCId()); //Niño termina de prepararse
            log.escribir(" Niño " + c.getCId() + " se tira por la tirolina\n"); //Escribe estado del niño en archivo
            childEnTir.meter(c.getCId());    //Pasa a estado de tirarse
            //señal.await();                            //Espera señal del monitor para tirarse
            sleep(3000);    //Tarda 3 segundos en llegar al final
            childEnTir.sacar(c.getCId());   //Sca a niño de estado de tirarse
            log.escribir(" Niño " + c.getCId() + " llega al final de la tirolina\n"); //Escribe estado del niño en archivo
            childEnFinTir.meter(c.getCId()); //Cabia a estado final
            sleep(500); //Niño tarda 0,5 segundos en bajarse
            childEnFinTir.sacar(c.getCId());    //Sca a niño de tirolina
            log.escribir(" Niño " + c.getCId() + " se baja de la tirolina\n"); //Escribe estado del niño en archivo
            semaforoCapTir.release();       //Libera permiso del semaforo de la tirolina
        }catch (InterruptedException e){ }
        c.sumar(1);   //Aumenta en uno el contador de actividades del niño
        contTir++;      //Se incremente el numero de veces que se ha realizado esta actividad
        contadorTirolina.countDown();   //Resta uno al contador 
        log.escribir(" Niño " + c.getCId() + " se va a la zona comun\n");   //Escribe estado del niño en archivo
        zonaComun(c, n);    //Accede a zona comun para descansar tras actividad
    }
    
    //Método simula funcionamiento de la actividad soga para los niños
    public void soga(Child c, int n) 
    {
        detener.comprobar();   //Punto donde se detendrá ejecución del programa
        try
        {
            esperaSoga.acquire();   //Si monitor no esta niño se queda bloqueado
        }
        catch(Exception e){}
        childEnSoga.sacar(c.getCId());  //Saca a niño de la lista
        if(!finSoga || contadorSoga.getCount() == 0)  //Si hilo llega y ya hay 10 jugadores
        {
            log.escribir(" Niño " + c.getCId() + " selecciona otra actividad ya que no hay hueco en actividad soga\n"); //Escribe estado del niño en archivo
            accederActividad(c , n++ % 3);  //No espera y se va a otra actividad
        }
        else        //Introduce al niño ene quipo correspondiente
        {   
                if(paridadId(numId(c)) && numA < 5 || numB == 5)    //si identificador del niño es par y equipo A no esta acompleto
                {
                    log.escribir(" Niño " + c.getCId() + " se mete en el quipo A\n"); //Escribe estado del niño en archivo
                    equipoA.meter(c.getCId());  //Introducimos niño en equipo A
                    equiA.add(c);   //Añade a niño al equipo
                    numA++;         //Aumentamos número de jugadores en equipo
                }
                else    //Si identificador del niño es impar o equipo A esta completo
                {
                    log.escribir(" Niño " + c.getCId() + " se mete en el equipo B\n"); //Escribe estado del niño en archivo
                    equipoB.meter(c.getCId());  //Introducimos niño en equipo B
                    equiB.add(c);   //Añade niño al array correspondiente
                    numB++;             //Aumentamos número de jugadores en equipo B
                }
                numJugadores++; //Aumenta le número de jugadores
                try
                {
                    barreraSoga.await();    //Espera a que lleguen 10 niños
                    finSoga = false;    //Indica que se esta realizando la actividad
                    log.escribir(" Niño " + c.getCId() + " empieza competición de soga\n"); //Escribe estado del niño en archivo
                    sleep(7000);    //Tardan 7 segundos en realizar actividad
                    log.escribir(" Niño " + c.getCId() + " termina competición soga\n"); //Escribe estado del niño en archivo
                    siguiente.acquire();    //Hacen que se vayan niños de la actividad de uno en uno
                    if(equiA.contains(c))   //Si niño esta en el equipo A
                    {
                        log.escribir(" Niño " + c.getCId() + " ha ganado competición soga\n"); //Escribe estado del niño en archivo
                        equipoA.sacar(c.getCId());  //sacamos  niño de equipo A
                        equiA.remove(c);    //Sacamos al niño de su respectivoa array
                        if(equiA.isEmpty() && equiB.isEmpty())  //Si equipos estan vacios ha terminado la actividad
                        {
                            finSoga = true; //Indica que ha terminado actividad
                        }
                        numA--;         //Disminuimos número de jugadores en equipo
                        c.sumar(2); //Equipo a siempre gana porque juego esta amañado
                        numJugadores--; //Disminuye numero de jugadores
                        contadorSoga.countDown();   //Decrementa cada vez que acaba actividad
                        log.escribir(" Niño " + c.getCId() + " se va a la zona comun\n");   //Escribe estado del niño en archivo
                        zonaComun(c,n); //Tras terminar actividad niños van a zona comun
                    }
                    else    //Si niño estaba en el equipo B
                    {
                        log.escribir(" Niño " + c.getCId() + " ha perdido competición soga\n"); //Escribe estado del niño en archivo
                        equipoB.sacar(c.getCId());  //sacamos niño de equipo B
                        equiB.remove(c);    //Saca a niño del array correspondiente
                        if(equiA.isEmpty() && equiB.isEmpty())  //Si equipos estan vacios ha terminado la actividad
                        {
                            finSoga = true; //Indica que ha terminado la actividad
                        }
                        numB--;             //Disminuimos número de jugadores en equipo B
                        c.sumar(1); //Suma uno al contador de actividades del niño
                        numJugadores--; //Disminuye numero de jugadores
                        contadorSoga.countDown();   //Decrementa cada vez que acaba actividad
                        log.escribir(" Niño " + c.getCId() + " se va a la zona comun\n"); //Escribe estado del niño en archivo
                        zonaComun(c,n); //Tras terminar actividad niños van a zona comun
                    }   
                }catch(Exception e){}
        }
    }
    
    
    //Método para cerrar TODO el campamento
    public boolean cerrar() {
        if(entradaIzq || entradaDer)    //Si una de las dos entradas estan abiertas las cierra
        {
            log.escribir(" se cierra campamento\n"); //Escribe estado del campamento
            semaforoAforo.drainPermits();   //Vacia permisos para bloquear a nuevos hilos que intenten acceder al campamento
            entradaIzq = false; //Actualiza estado de las entradas
            entradaDer = false;
        }   
        return false;   //Devuelve que el campamento est acerrado
    }
    
    //Método para abrir TODO el campamento
    public boolean abrir()
    {
      if(!entradaIzq && !entradaDer)    // Si ambas entradas estan cerradas
      {
        semaforoAforo.release(aforo);   //Libera todos los permisos
        entradaDer = true;  //Actualiza estados de las entradas
        entradaIzq = true;
      }
      return true;
    }
    
    /*Método determina si número es par*/
    public boolean paridadId(int n)
    {
        boolean par = false;    //Variable almacena si parte numérica del id es par
        if(n%2 == 0)       
        {
            par = true; //Si parte numérica es par cambia variable a true
        }   
        
        return par; //Devuelve si es par o no
    }
    
    //Método separa parte numérica del id (para monitores)
    public int numId(Monitor m)
    {
        String id = m.getMId();  //Guarda identificador del monitor
        String[] a = id.split("M");  //Convierte identificador en array
        int n = Integer.valueOf(a[1]);  //Convierte segunda parte del identificador (parte numérica) a int
        return n;   //Devuelve parte numérica dl identificador
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
        return n;   //Devuelve parte numérica del identificador 
    }
    
    //Método saca un solo número del 1 al 4 de la parte numérica del identificador de un niño
    public int moduloId(int n)
    {
        return n % 3;       //Módulo 3 de parte numérica
    }
}
