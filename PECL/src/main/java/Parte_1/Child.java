/*Niños que entraran al campamento
 */
package Parte_1;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author blanf
 */
public class Child extends Thread{
    private String id;
    private Campamento camp;
    AtomicInteger contActividades = new AtomicInteger(0);


    public Child(String id, Campamento c, AtomicInteger cont) {
        this.id = id;
        camp = c;
        contActividades = cont;
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
        try
        {
            sleep((int)(3000*Math.random()));
        } catch (InterruptedException e){ }
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
    }
}
