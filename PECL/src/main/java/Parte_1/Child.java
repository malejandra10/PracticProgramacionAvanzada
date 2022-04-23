/*Niños que entraran al campamento
 */
package Parte_1;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author blanf
 */
public class Child extends Thread{
    private String id;
    private Campamento camp;
    AtomicInteger contActividades = new AtomicInteger(0);
    private CyclicBarrier barreraIni;
    private CyclicBarrier barreraFin;


    public Child(String id, Campamento c, AtomicInteger cont) {
        this.id = id;
        this.camp = c;
        this.contActividades = cont;
        
    }

    public String getCId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public  AtomicInteger getContActividades() {
        return contActividades;
    }

    public void setContActividades( AtomicInteger contActividades) {
        this.contActividades = contActividades;
    }
    
    public void run()
    {
        /*try {
            barreraIni.await();
            barreraFin.await();
            } catch (Exception e) { }*/
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
    }
}
