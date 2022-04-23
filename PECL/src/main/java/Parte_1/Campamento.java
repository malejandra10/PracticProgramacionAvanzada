/*Esta clase contendrá todas las entradas y actividades.*/
package Parte_1;

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
    private boolean entradaIzq = false, entradaDer= false; //Variables almacenan si entradas estan abiertas
    private int aforo,num, capTir = 1, capMer = 20, capSoga = 10, numLimpias = 0, numSucias = 25, numJugadores = 0, numA = 0, numB = 0,contSoga = 0, contTir = 0,contMer1 = 0, contMer2 = 0;     //Aforo maximo del campamento y parte numérica del identificador
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
    private Condition jugadores = cerrojoSoga.newCondition();
    private int maxJugadores = 10;  //Numero de jugadores necesarios para jugar a actividad soga
    private CyclicBarrier barreraIni = new CyclicBarrier(maxJugadores); //Para esperar a que haya 10 jugadores en actividad soga
    private CountDownLatch contadorSoga = new CountDownLatch (10);  //Para bloquear monitor mientras se realiza actividad soga
    private CountDownLatch contadorTirolina = new CountDownLatch (10);  //Para bloquear monitor mientras se realiza actividad tirolina
    private CountDownLatch contadorMerienda = new CountDownLatch (10);  //Para bloquear monitor mientras se realiza actividad merienda
    private Semaphore espera = new Semaphore (1, true);
    private Queue<Child> eA = new ConcurrentLinkedQueue<Child>();
    private Queue<Child> eB = new ConcurrentLinkedQueue<Child>();
    private Queue<Child> todos = new ConcurrentLinkedQueue<Child>();
    /*Constructor de la clase*/
    public Campamento(int aforo,JTextField espEn1,JTextField espEn2,JTextField espTir,JTextField espMer, JTextField den, JTextField monEnTir,JTextField monEnMer,JTextField monEnZC, JTextField monEnSo,JTextField colaMer,JTextField colaTir, JTextField enSoga, JTextField enMer, JTextField limp, JTextField suc, JTextField zc, JTextField tirPrep,JTextField enTir, JTextField finTir, JTextField a,JTextField b)
    {
        this.aforo = aforo;
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
                    //if(!entradaIzq)
                    //{
                        sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                        entradaIzq = true;          //Monitor abre entrada
                        cerradaIzq.signalAll();     //Libera hilos que estuviesen esperando en la cola de la puerta izquierda
                        colaEntradaIzq.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
                        dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento 
                    //}
                    /*else
                    {
                       colaEntradaIzq.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
                       dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento 
                    }*/
                } catch (InterruptedException e){ }
                
                finally
                {        
                    cerrojoIzq.unlock();   //Abre cerrojo
                }    
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
                   //if(!entradaDer)
                   //{
                        sleep(500 + (int) (1000* Math.random()));      //Monitor tarda entre 0.5 y 1 segundo en abrir la puerta
                        entradaDer = true;          //Monitor abre entrada
                        cerradaDer.signalAll();     //Libera hilos que estuviesen esperando en la cola de la puerta derecha
                        colaEntradaDer.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
                        dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento 
                  /* }
                   else
                   {
                       colaEntradaDer.sacar(m.getMId());            //Sacamos a monitor de la cola de espera          
                       dentro.meter(m.getMId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento      
                   }*/
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
            soga(m,num);
            break;
        case 2:     //Monitor entra en actividad tirolina
            monEnTirolina.meter(m.getMId());
            tirolina(m,num);
            break;
        case 3:     //Monitor entra en actividad merienda
            monEnMerienda.meter(m.getMId());
            try
            {
                merienda(m,num);
            }
            catch(Exception e){}
            break;
        case 4:     //Monitor entra en actividad merienda
            monEnMerienda.meter(m.getMId());
            try
            {
                merienda(m,num);
            }
            catch(Exception e){}
            break;    
      }     
    }
    
    //Método para simular el funcionamiento de la merienda por parte de los monitores
    public synchronized void merienda (Monitor m, int n) throws InterruptedException
    {
        while(true)
        {
            while(numSucias == 0)
            {
                wait();
            }
            sleep(3000 + (int) (2000 * Math.random()));
            limpias.sacar(" " + numLimpias);
            numLimpias++;
            limpias.meter(" " + numLimpias);
            sucias.sacar(" " + numSucias);
            numSucias--;
            sucias.meter(" " + numSucias);
            notifyAll();
        }
        //contadorMerienda.await();
        //contadorMerienda = new CountDownLatch (10);  //Para bloquear monitor mientras se realiza actividad merienda
        //zonaComun(m, n);
    }
    
    //Método simula actividad soga para los monitores
    public void soga(Monitor m, int n)
    {
        while(true)
        {
        //bucle con countDownLunch await con numero de veces que debe realizar actividad wait
            try
            {
                contadorSoga.await();
                contadorSoga = new CountDownLatch(10);
                monEnSoga.sacar(m.getMId());    //Se saca monitor de la actividad
                m.contActividades.addAndGet(10); //Se resetea contador para la actividad soga
                zonaComun(m,n); //Se envia monitor a la zona comun para que descanse 
            }catch(Exception e){}
        }
    }
    
    //Método simula actividad tirolina para los monitores
    public void tirolina(Monitor m, int n)
    {
        while(true)
        {
            try
            {
                contadorTirolina.await();
                m.contActividades.addAndGet(10);
                contadorTirolina = new CountDownLatch(10);
                monEnTirolina.sacar(m.getMId());    //Se saca monitor de la actividad
                zonaComun(m,n); //Se envia monitor a la zona comun para que descanse 
            }
            catch(Exception e){}
        }
    }
    
    //Método simula funcionamiento de la zona comun para los monitores
    public void zonaComun(Monitor m, int n)
    {
        try 
        {
            monEnZonaComun.meter(m.getMId());   //Monitor entra en zona comun
            sleep(1000 + (int) (1000* Math.random()));  //Monitor descansa entre 1 y 2 segundos enona comun
            monEnZonaComun.sacar(m.getMId());   //Monitor sale de la zona comun
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
                cerrojoIzq.lock();
                try
                {
                    while(!entradaIzq)
                    {
                        try
                        {
                            cerradaIzq.await();     //hilo se bloquea hasta que se abra puerta
                            colaEntradaIzq.sacar(c.getCId());            //Sacamos a monitor de la cola de espera          
                            dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento        
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
                   colaEntradaIzq.sacar(c.getCId());            //Sacamos a niño de la cola de espera          
                    dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado niño en el campamento
                } catch(InterruptedException e){ }  
            }
        }    
        else        //Si id del monitor es impar entra por la puerta derecha
        {
           colaEntradaDer.meter(c.getCId());  //Introduce niño en la cola (a efectos practicos no espera se hace para observar)
           if(!entradaDer)    /*Si puerta de la entrada derecha esta abierta*/
            {
                cerrojoDer.lock();
                try
                {
                    while(!entradaDer)
                    {
                        try
                        {
                            cerradaDer.await();     //hilo se bloquea hasta que se abra puerta
                            colaEntradaDer.sacar(c.getCId());            //Sacamos a monitor de la cola de espera          
                            dentro.meter(c.getCId());                  //Llamamos a meter para indicar que ha entrado monitor en el campamento         
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
                      System.out.println("caca");
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
    public synchronized void merienda(Child c, int n) throws InterruptedException
    {
        System.out.println("hollaaaaaaa");
        while (numLimpias == 0)     //No hay bandejas limpias disponibles
        { 
            wait();    //Bloquea hilo hasta que haya bandejas limpias
        }
        try 
        { 
            sleep(7000);        //Tarda 7 segundos en merendar
            limpias.sacar(" " + numLimpias);
            numLimpias--;       //Disminuye numero de bandejas limpias y aumenta de sucias
            limpias.meter(" " + numLimpias);
            sucias.sacar(" " + numSucias);
            numSucias++;
            sucias.meter(" " + numSucias);
            System.out.println("patata");
            notifyAll(); //Despierta a monitor que estuviese bloqueado esperando que hibiese bandejas sucias
            System.out.println("patata2");
            childEnMer.sacar(c.getCId());    //Saca a niño del merendero
            semaforoCapMer.release();
            c.contActividades.incrementAndGet();   //Aumenta en uno el contador de actividades del niño
            contadorMerienda.countDown();
            zonaComun(c , n);   //Tras terminar actividad niño va a zona comun
        } 
        catch(Exception e){}
        
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
        contTir++;      //Se incremente el numero de veces que se ha realizado esta actividad
        contadorTirolina.countDown();   //Resta uno al contador 
        zonaComun(c, n);    //Accede a zona comun para descansar tras actividad
    }
    
    //Método simula funcionamiento de la actividad soga para los niños
    public void soga(Child c, int n) throws InterruptedException
    {
        childEnSoga.sacar(c.getCId());
        if(numJugadores == 10 || contadorSoga.getCount() == 0)  //Si hilo llega y ya hay 10 jugadores
        {
            accederActividad(c , n++ % 3);  //No espera y se va a otra actividad
        }
        else
        {   
            //Espera a que hay 10 jugadores
            try {
                if(paridadId(numId(c)) && numA < 5 || numB == 5)
                {
                    equipoA.meter(c.getCId());  //Introducimos niño en equipo A
                    equiA.add(c);
                    eA.add(c);
                    numA++;         //Aumentamos número de jugadores en equipo
                    //c.contActividades.addAndGet(2);
                    todos.add(c);
                }
                else
                {
                    equipoB.meter(c.getCId());  //Introducimos niño en equipo B
                    equiB.add(c);
                    eB.add(c);
                    numB++;             //Aumentamos número de jugadores en equipo B
                    //c.contActividades.incrementAndGet();
                    todos.add(c);
                }
                numJugadores++;
                barreraIni.await();
                sleep(7000);    //Tardan 7 segundos en realizar actividad
                numJugadores = 0;     //Disminuye numero de jugadores en actividad
                numA = 0;
                numB = 0;
                contSoga++;
                
                Child ch = todos.poll();
                Child c1 = eA.poll();
                c1.contActividades.addAndGet(2);
                equipoA.sacar(c1.getCId());
                System.out.println(eB.peek().getCId());
                Child c2 = eB.poll();
                c2.contActividades.incrementAndGet();
                equipoB.sacar(c2.getCId());
                zonaComun(ch, n);
                
               /* numA = 0;
                numB = 0;
                equiA.clear();
                equiB.clear();
                equipoA.sacarTodos();
                equipoB.sacarTodos();*/
                //Vacia los equipos y da puntos correspondientes
                /*
                if(paridadId(numId(c)) || numB == 0)
                {
                    System.out.println(numA);
                    System.out.println("hola");
                    equipoA.sacar(c.getCId());  //sacamos  niño de equipo A
                    equiA.remove(c);
                    numA--;         //Disminuimos número de jugadores en equipo
                    c.contActividades.addAndGet(2); //Equipo a siempre gana porque juego esta amañado
                    zonaComun(c,n); //Tras terminar actividad niños van a zona comun
                }
                else
                {
                    System.out.println(numB);
                    System.out.println("hola2");
                    equipoB.sacar(c.getCId());  //sacamos niño de equipo B
                    equiB.remove(c);
                    numB--;             //Disminuimos número de jugadores en equipo B
                    c.contActividades.incrementAndGet();
                    zonaComun(c,n); //Tras terminar actividad niños van a zona comun
                }*/
                contadorSoga.countDown();   //Decrementa cada vez que acaba actividad
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
