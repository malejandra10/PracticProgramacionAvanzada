/*Clase hilo monitor cuyso atributos son identificador, campamento, contador de actividades y clase detener 
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
    
    //Devuelve valor de la variable contador de actividades
    public  int getContActividades() {
        return contActividades;
    }

    //Establece valor de contActividades
    public void setContActividades(int contActividades) {
        this.contActividades = contActividades;
    }
    //Método para sumar valor al contador de actividades
    public int sumar(int n)
    {
        this.contActividades = contActividades + n; //Suma valor indicador
        return contActividades; //Devuelve nuevo valor del contador
    }
    //Ejecuta hilo
    public void run()
    {
        detener.comprobar();    //Punto de detención del programa
        camp.entrar(this); //Entra en el campamento si hay hueco; y sino espera en la cola
    }
    
}
