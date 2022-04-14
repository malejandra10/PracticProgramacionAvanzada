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
    
    ArrayList<String> lista;
    JTextField tf;
    
    public ListaThreads(JTextField tf)
    {
        lista=new ArrayList<String>();
        this.tf=tf;
    }
    
    public synchronized void meter(String id)
    {
        lista.add(id);
        imprimir();
    }
    
    public synchronized void sacar(String id)
    {
        lista.remove(id);
        imprimir();
    }
    
    public void imprimir()
    {
        String contenido="";
        for(int i=0; i<lista.size(); i++)
        {
           contenido=contenido+lista.get(i)+" ";
        }
        tf.setText(contenido);
    }
}
