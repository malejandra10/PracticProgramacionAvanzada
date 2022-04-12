/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Parte_1;

/**
 *
 * @author blanf
 */
public class Monitor extends Thread{
    private String id;
    private Campamento camp;
    
    /*Constructor*/
    public Monitor(String i, Campamento c)
    {
        this.id = i;
        this.camp = c;
    }

    //Método devuelve identificador del monitor
    public String getMId() {
        return id;
    }

    //Método establece identificador del monitor
    public void setId(String id) {
        this.id = id;
    }
    
    //Ejecuta hilo
    public void run()
    {
        try
        {
            sleep((int)(3000*Math.random()));
        } catch (InterruptedException e){ }
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
        //camp.accederActividad(this); //Entra  actividad correspondiente
    }
    
}

