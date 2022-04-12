/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Parte_1;

import java.util.ArrayList;
import javax.swing.JTextField;

/**
 *
 * @author blanf
 */
public class ListaThreads {
    
    ArrayList<Monitor> listEnIzq, listEnDer, listaMonEnMer,listaMonSoga,listaMonTir; //Guarda lista de monitores que entran los entradas
    JTextField text;     //TestField donde mostraremos monitores que entran en las distintas entradas y actividades
    /*Constructor*/
    public ListaThreads(JTextField t)
    {
        listEnIzq = new ArrayList<Monitor>();
        listEnDer = new ArrayList<Monitor>();
        listaMonEnMer = new ArrayList<Monitor>();
        listaMonSoga = new ArrayList<Monitor>();
        listaMonTir = new ArrayList<Monitor>();
        this.text = t;
        
    }
    
    /*Método introduce monitor en la lista de la entrada correspondiente segun el id*/
    public synchronized void meter(Monitor m,int id)
    {
        switch (id) {
            case 1:
                listEnIzq.add(m);
                break;
            case 2:
                listEnDer.add(m);;
    } 
        imprimir(id);
    }
    
    //Método saca monitor de la lista de entrada correspondiente a id dado
    public synchronized void sacar(Monitor m, int id)
    {
        switch (id) {
            case 1:
            {
                listEnIzq.remove(m);
                break;
            }
            case 2:
            {
                listEnDer.remove(m);
                break;
            }
         }    
        imprimir(id);
    }
    
    /*Método introduce monitor en la lista de la actividad correspondiente segun el id*/
    public synchronized void accederAct(Monitor m,int id)
    {
        switch (id) {
            case 1:
                listaMonSoga.add(m);
                break;
            case 2:
                listaMonTir.add(m);
                break;
            case 3:
                listaMonEnMer.add(m);
                break;
            case 4:
                listaMonEnMer.add(m);
                break;
                
        //Falta cuando pasan a la zona comun   
    } 
        imprimir(id);
    }
    
    /*Método imprime en pantalla lista de niños y monitores que entran por entrada correspondiente*/
    public void imprimir(int id)
    {
        String contenido1 = "", contenido2 = "";
         switch (id) {
            case 1:
                for(int i=0; i<listEnIzq.size(); i++)   //Recorre lista que contiene quien entra puerta izq
                {
                   contenido1=contenido1+listEnIzq.get(i).getMId(); //guarda en contenido elementos de la lista
                }
                for(int i=0; i<listaMonSoga.size(); i++)   //Recorre lista que contiene monitores encargados de la actividad soga
                {
                   contenido2=contenido2+listaMonSoga.get(i).getMId();  //guarda en contenido elementos de la lista
                }
                //Escribe texto correspondiente en la interfaz
                System.out.println(contenido1);
                System.out.println(contenido2);
                text.setText(contenido1);
                text.setText(contenido2);
                break;
                
            case 2:
                for(int i=0; i<listEnDer.size(); i++)
                {
                   contenido1=contenido1+listEnDer.get(i).getMId();
                }
                for(int i=0; i<listaMonTir.size(); i++)   //Recorre lista que contiene monitores encargados de la actividad soga
                {
                   contenido2=contenido2+listaMonTir.get(i).getMId();  //guarda en contenido elementos de la lista
                }
                //Escribe texto correspondiente en la interfaz
                text.setText(contenido1);
                text.setText(contenido2);
                break;
                
            case 3:    //default corresponderá a caso 3 y 4
                for(int i=0; i<listaMonEnMer.size(); i++)
                {
                   contenido1=contenido1+listaMonEnMer.get(i).getMId();
                }
                text.setText(contenido1);
                break;
            case 4:    //default corresponderá a caso 3 y 4
                for(int i=0; i<listaMonEnMer.size(); i++)
                {
                   contenido1=contenido1+listaMonEnMer.get(i).getMId();
                }
                text.setText(contenido1);
                break;
         }
    }
    
}
