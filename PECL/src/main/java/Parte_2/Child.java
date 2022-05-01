/* Clase hilo niños cuyos atributos son identificador, campamento,contador de actividades y clase detener
 */
package Parte_2;

import Parte_1.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author blanf
 */
public class Child extends Thread{
    //Inicializo variables
    private String id;
    private Campamento camp;
    private int contActividades;
    private Detener detener;
    

    //Constructor
    public Child(String id, Campamento co,int c, Detener deten) {
        this.id = id;
        this.camp = co;
        this.contActividades = c;
        this.detener = deten;
        
    }
    //Método para devolver valor del identificador del niños
    public String getCId() {
        return id;
    }
    //Método que establece valor del identificador
    public void setId(String id) {
        this.id = id;
    }
    //Método que devuelve valor del contador de actividades
    public  int getContActividades() {
        return contActividades;
    }
    // Método para establecer valor del contador
    public void setContActividades( int contActividades) {
        this.contActividades = contActividades;
    }
    //Método que suma valor pasado al contador de actividades
    public int sumar(int n)
    {
        contActividades = contActividades + n; //Sumar valor indicado
        return contActividades; //Deveuleve nuevo valor del contador
    }
    //Se ejecurta hilo
    public void run()
    {
        detener.comprobar();    //Punto de detención del programa
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
    }
}
