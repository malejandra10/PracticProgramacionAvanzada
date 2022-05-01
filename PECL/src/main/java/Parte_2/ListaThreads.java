/*Clase contiene y actualiza listas que se mostrarán en la interfaz
 */
package Parte_2;


import java.util.ArrayList;
import javax.swing.JTextField;

/**
 *
 * @author blanf
 */
public class ListaThreads {
    //Inicializo variables
    ArrayList<String> lista;    //Array que contiene elementos a mostrar
    JTextField tf;  //Espacio dónde se mostrará la información de la lista
    
    //Constructir
    public ListaThreads(JTextField tf)
    {
        lista=new ArrayList<String>();
        this.tf=tf;
    }
    
    //Método para introducir elemento en a lista
    public synchronized void meter(String id)
    {
        lista.add(id);  //Añade string a la lista
        //imprimir(); //Muestra nuevo valor por interfaz
    }
    
    //Método para sacar string de la lista
    public synchronized void sacar(String id)
    {
        lista.remove(id);   //Elimina string indicado de la lista
        //imprimir(); //Imprime nueva lista por pantalla
    }
    
    //Método saca todos los elementos de la lista
    public synchronized void sacarTodos ( )
    {
        lista.clear();  //Elimina todos los elementos que contenia
        //imprimir(); //Imprime lista vacia
    }
    
    //Método determina cuántos elementos hay en la lista
    public synchronized int getTamaño()
    {
        return lista.size();    //Devuelve tamaño de la lista
    }
    
    
    //Método imprime por panatalla el tamaño de la lista
    public void imprimir()
    {
        String tamaño="";    //String contiene tamaño de la lista
        int tam;
        tam = lista.size();     //Contiene tamaño de la lista
        tamaño = " " + tam;  //Comvierto entero en cadena
        tf.setText(tamaño);  //Se escribe tamaño  en hueco correspondiente en interfaz
    }
    
    //Método imprime por panatalla el contenido de la lista
    public void imprimir2()
    {
        String contenido="";    //String contiene contenidod e la lista
        for(int i = 0; i < lista.size(); i++)   //Bucle recorre toda la lsita
        {
           contenido = contenido + lista.get(i) + " ";    //Se guarda contenido de la lista a mostrar
        }
        tf.setText(contenido);  //Se escribe contenido en hueco correspondiente en interfaz
    }
}