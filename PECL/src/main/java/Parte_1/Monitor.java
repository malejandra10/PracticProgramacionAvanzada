/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Parte_1;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author blanf
 */
public class Monitor extends Thread{
    private String id;
    private Campamento camp;
    AtomicInteger contActividades = new AtomicInteger(0);
    
    /*Constructor*/
    public Monitor(String i, Campamento c, AtomicInteger cont)
    {
        this.id = i;
        this.camp = c;
        this.contActividades = cont;
    }

    //Método devuelve identificador del monitor
    public String getMId() {
        return id;
    }

    //Método establece identificador del monitor
    public void setId(String id) {
        this.id = id;
    }
    
    public  AtomicInteger getContActividades() {
        return contActividades;
    }

    public void setContActividades( AtomicInteger contActividades) {
        this.contActividades = contActividades;
    }
    
    //Ejecuta hilo
    public void run()
    {
        try
        {
            sleep((int)(3000*Math.random()));
        } catch (InterruptedException e){ }
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
    }
    
}

