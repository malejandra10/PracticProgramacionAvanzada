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
    private int contActividades;
    private Detener detener;
    


    public Child(String id, Campamento co,int c, Detener deten) {
        this.id = id;
        this.camp = co;
        this.contActividades = c;
        this.detener = deten;
        
    }

    public String getCId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public  int getContActividades() {
        return contActividades;
    }

    public void setContActividades( int contActividades) {
        this.contActividades = contActividades;
    }
    
    public int sumar(int n)
    {
        this.contActividades = contActividades + n;
        return contActividades;
    }
    
    public void run()
    {
        detener.entrar();
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
    }
}
