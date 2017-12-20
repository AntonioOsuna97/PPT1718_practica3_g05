/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Random;
import sun.misc.IOUtils;


/**
 * Clase de atención de un servidor TCP sencillo
 * Prácticas de Protocolos de Transporte
 * Grado en Ingeniería Telemática
 * Universidad de Jaén
 * 
 * NOTA:
 * 
 * @author Juan Carlos Cuevas Martínez y Antonio Osuna y Fernando 
 */
public class HttpSocketConnection implements Runnable {
    
    public static final String HTTP_Ok="200";
    public static final String HTTP_Bad_Request="400";
    public static final String HTTP_Not_Found="404";
    public static final String HTTP_Method_Not_Allowed="405";
    public static final String HTTP_Version_Not_Supported="505";
    
    
    private Socket mSocket=null;
    
    /**
     * Se recibe el socket conectado con el cliente
     * @param s Socket conectado con el cliente
     */
    public HttpSocketConnection(Socket s){
        mSocket = s;
    }
    
    public void run() {
        //Variables
        Random r = new Random(System.currentTimeMillis());
        int n=r.nextInt();
        String request_line="";
        BufferedReader input;
        DataOutputStream output;
        FileInputStream input_file;
        try {
            byte[] outdata=null;
            byte[] outdataRecurso=null;
            String outmesg="";
            input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            output = new DataOutputStream(mSocket.getOutputStream());
            do{
               
                request_line= input.readLine();
                //Esto se debería sacar de este bucle
                String parts[]=request_line.split(" ");
                if(request_line.startsWith("GET ")){
                    String resourceFile="";
               
                    
                    if(parts.length==3){
                        //COmprobar la versión
                        String[] respuesta;
                        respuesta=parts[2].split("/");
                        
                        //Juan Carlos: Si es 1 o 1.1 es valido
                        if(respuesta[1].equals("1") || respuesta[1].equals("1.1")){
                            
                        if(!parts[0].equals("GET ")){
                            outmesg="HTTP/1.1 405\r\nContent-type:text/html\r\n\r\n<html><body><h1>Método no permitido</h1></body></html>";
                       outdata=outmesg.getBytes(); 
                        }
                        //equalsIgnoreCase compara la cadena con el objeto ignorando mayúsculas y minúsculas
                        if(parts[1].equalsIgnoreCase("/")){
                            resourceFile="index.html";
                        }else
                            resourceFile=parts[1];
                        //Content-type
                          
                        outdataRecurso=leerRecurso(resourceFile);
                        //Cabecera content-length
                        if(outdata==null)    {
                            outmesg="HTTP/1.1 404\r\nContent-type:text/html\r\n\r\n<html><body><h1>No encontrado</h1></body></html>";
                            outdata=outmesg.getBytes();    
                        }else{
                            //200 OK
                            outmesg="HTTP/1.1 200\r\nContent-type:text/html\r\n\r\n";
                            outdata=outmesg.getBytes();
                        }
                   // }
                    
                   
                    }else{
                        outmesg="HTTP/1.1 505\r\nContent-type:text/html\r\n\r\n<html><body><h1>HTTP Version Not Supported</h1></body></html>";
                        outdata=outmesg.getBytes();
                        }
                    }
                           // }else
                        //outmesg="HTTP/1.1 400\r\nContent-type:text/html\r\n\r\n<html><body><h1>Problema en el cliente</h1></body></html>";
                        //outdata=outmesg.getBytes();
                    
            }
                    
                
                
                
                System.out.println(request_line);
            }while(request_line.compareTo("")!=0);
            //CABECERAS....
            
            //Recurso
           output.write(outdata);
           output.write(outdataRecurso);
            input.close();
            output.close();
            mSocket.close();
    
        } catch (IOException e) {
            System.err.println("Exception" + e.getMessage());
        }
        }

    /**
     * Método para leer un recurso del disco
     * @param resourceFile
     * @return los bytes del archio o null si éste no existe
     */
    
    
    
    private byte[] leerRecurso(String resourceFile){
        //Se debe comprobar que existe
        
    File f= new File("./"+resourceFile);
    byte[] bytesArray = null;
        try{
       FileInputStream fis = new FileInputStream (f);
       bytesArray = new byte[(int) f.length()];
       fis.read(bytesArray);
       BufferedInputStream bis = new BufferedInputStream(fis);
       bis.read(bytesArray, 0 , bytesArray.length);

        }catch(IOException e) {
            e.printStackTrace();
        }  
       return bytesArray;
    }
    
    /*
    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }
    */
    
}