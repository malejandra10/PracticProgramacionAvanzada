/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Parte_1;;

/**
 *
 * @author blanf
 */
public class Monitor extends Thread{
    private String id;
    private Campamento camp;
    private int contActividades;
    private Detener detener;
    
    /*Constructor*/
    public Monitor(String i, Campamento c,int cont, Detener deten)
    {
        this.id = i;
        this.camp = c;
        this.contActividades = cont;
        this.detener = deten;
    }

    //Método devuelve identificador del monitor
    public String getMId() {
        return id;
    }

    //Método establece identificador del monitor
    public void setId(String id) {
        this.id = id;
    }
    
    public  int getContActividades() {
        return contActividades;
    }

    public void setContActividades(int contActividades) {
        this.contActividades = contActividades;
    }
     public int sumar(int n)
    {
        this.contActividades = contActividades + n;
        return contActividades;
    }
    //Ejecuta hilo
    public void run()
    {
        detener.entrar();
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
    }
    
}
