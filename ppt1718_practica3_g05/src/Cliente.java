/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Clase que modela un cliente genérico TCP
 * Prácticas de Protocolos de Transporte
 * Grado en Ingeniería Telemática
 * Universidad de Jaén
 * 
 * @author Juan Carlos Cuevas Martínez , Antonio Osuna y Fernando Cabrera
 */

/* 
 * Hay dos modos de conseguir hilos de ejecución (threads) en Java. Una es implementando el interfaz Runnable,
 * la otra es extender la clase Thread.
 * La clase cliente implementa el interfaz Runnable. La implementación del interfaz Runnable es la forma habitual de crear hilos.
 * En dicho interfaz podemos destacar el método run() que constituye el cuerpo de un hilo en ejecución. 
 */
public class Cliente implements Runnable{
    //Variables
    private String mId="";
    
    //Contructor de cliente
    public Cliente(String id){
        mId=id;
    }
    
    //Método run
    public synchronized void run() {
   
        try{       
            //Determinamos la dirección IP de un host dando un nombre.
            InetAddress destination = InetAddress.getByName("www10.ujaen.es");
            System.out.println("-------------------\r\nIniciando cliente "+mId+"\r\n--------------------");
            
            System.out.println("Conectando con socket "+destination.toString());
            //Creamos el socket
            Socket socket = new Socket(destination,80);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.write("GET / HTTP/1.1\r\nhost:www10.ujaen.es\r\nConnection:close\r\n\r\n".getBytes());
            String line="";
            int i=0;
            while((line=input.readLine())!=null) {
                if(i==0)
                    System.out.println("<"+mId+"> "+line);
                i++;
            }   
            
        }catch (UnknownHostException e) {
            System.out.println("\tUnabletofindaddressfor");
        } catch(IOException ex){
        
            System.out.println("\tError: " +ex.getMessage()+"\r\n"+ex.getStackTrace());
        }
    }
    
}
