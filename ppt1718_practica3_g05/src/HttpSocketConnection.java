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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;


/**
 * Clase de atención de un servidor TCP sencillo
 * Prácticas de Protocolos de Transporte
 * Grado en Ingeniería Telemática
 * Universidad de Jaén
 * 
 * NOTA:
 * 
 * @author Juan Carlos Cuevas Martínez, Antonio Osuna y Fernando Cabrera
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
    
    @Override
    public void run() {
        //Variables
        String request_line="";
        BufferedReader input;
        DataOutputStream output;
        String contentType="";
        String contentLength="";
        String connection="";
        String allow="";
        String server="";
        String resourceFile="";
        try {
            byte[] outdata=null;
            byte[] outdataRecurso=null;
            String outmesg="";
            input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            output = new DataOutputStream(mSocket.getOutputStream());
           
            
            
            request_line= input.readLine();  
                 String parts[]=request_line.split(" ");
                
                if(request_line.startsWith("GET ")){
                  
               
                    
                    if(parts.length==3){
                        //COmprobar la versión
                        String[] respuesta;
                        respuesta=parts[2].split("/");
                        
                        //Juan Carlos: Si es 1 o 1.1 es valido
                        if(respuesta[1].equals("1") || respuesta[1].equals("1.1")){
                           
                        /* ***MAL
                            No podemos obtener el codigo de error de esta manera, ya que una vez que entraba
                            en el if(request_line.startsWith("GET ")), el bucle if(!parts[0].equals("GET ")) no tenía ningún 
                            sentido, ya que nunca entraría en él.
                            
                            if(!parts[0].equals("GET ")){
                           
                            outmesg="HTTP/1.1 405\r\n\r\n<html><body><h1>Método no permitido</h1></body></html>";
                       outdata=outmesg.getBytes(); 
                        }
                       */
                        
                        //equalsIgnoreCase compara la cadena con el objeto ignorando mayúsculas y minúsculas
                        if(parts[1].equalsIgnoreCase("/")){
                            resourceFile="index.html";
                        }else{
                            //parts[1] --> contiene el tipo de contenido
                            resourceFile=parts[1];
                        }
                        //Content-type
                        //Para ello, necesitamos separar parts1 y obtener el tipo de contenido.
                        //Como el tipo de contenido presenta un punto, separaremos por ahí
                        String[] separacionContentType;
                    
                        //Hemos tenido que poner un if para comparar si la part[1] era distinto de "/"
                        //Ya que cuando haciamos un get por defecto no nos mostraba el index
                        if(!parts[1].equals("/")){
                        //Para poder separar con punto necesitamos poner una barra \\. Ya que el punto en split significa
                        //cualquier caracter
                        separacionContentType= parts[1].split("\\.");
                        if(separacionContentType[1].equals("jpg")){
                            contentType="\r\nContent-type: image/jpeg\r\n";
                        }else if(separacionContentType[1].equals("txt")){
                            contentType="\r\nContent-type: text/plain\r\n";
                        }else{
                            //Como solo podremos disponibles 3, elegimos que si no es una imagen o texto plano, sea:
                            contentType="\r\nContent-type: text/html\r\n";
                        }
                        }else{
                            contentType="\r\nContent-type: text/html\r\n";
                        }
                          //Hemos tenido que crear una nueva variable porque nos sobreescribía outdata
                        outdataRecurso=leerRecurso(resourceFile);
                        
                        /* **************************************MAL
                        No podiamos hallar aquí la cabecera contentLength ya que si era null nos daba error, por lo tanto
                        lo hallamos en la comprobación siguiente
                         contentLength="Content-Length: "+ outdataRecurso.length;  
                        **********************************************/
                        
                        if(outdataRecurso==null)    {
                            outmesg="HTTP/1.1 404\r\n\r\n<html><body><h1>No encontrado</h1></body></html>";
                            outdata=outmesg.getBytes();    
                        }else{
                            //200 OK
                            outmesg="HTTP/1.1 200";
                            outdata=outmesg.getBytes();
                        }
             
                    }else{
                        outmesg="HTTP/1.1 505\r\n\r\n<html><body><h1>HTTP Version Not Supported</h1></body></html>";
                        outdata=outmesg.getBytes();
                        }
                    }else{
                      outmesg="HTTP/1.1 400\r\n\r\n<html><body><h1>Problema en el cliente</h1></body></html>";
                      outdata=outmesg.getBytes();
                    }
                }else{
                    outmesg="HTTP/1.1 405\r\n\r\n<html><body><h1>Método no permitido</h1></body></html>";
                    outdata=outmesg.getBytes(); 
                }
                    
                //No podíamos dejar el do{}while junto con el if como lo teniamos en el codigo que nos dejó
                //Juan Carlos, porque nos provocaba que siempre se diese el error 405 ya que en la siguiente iteración se metía en el
                //else, donde nosotros hemos puesto que el método no sea permitido
                //Por lo que separamos el do del if(request_line.startsWith("GET "))
                   do{
                request_line= input.readLine();        
                //Escribe host...
                System.out.println(request_line);
            }while(request_line.compareTo("")!=0);
            //CABECERAS.
            //Juan Carlos nos ha dicho que las cabeceras se mandan después de la línea de estado.
            
            //Necesitamos poner este bucle porque nos salian excepciones a la hora de 
            //mandar las cabeceras de un error 404 por ejemplo
            if(outmesg.equals("HTTP/1.1 200")){
            //Cabecera content-length  -->El tamaño del contenido de la petición en bytes
            //Para esta cabecera deberemos obtener el valor en bytes de nuestro outdataRecurso, para ello utilizaremos length
            contentLength="Content-Length: "+ String.valueOf(outdataRecurso.length) + " \r\n";  
           
            //Connection controla si la conexión de red permanece abierta después de que finalice la transacción actual.
            //Si el valor enviado es keep-alive, la conexión es persistente y no se cierra, lo que permite 
            //realizar solicitudes posteriores al mismo servidor.
            //Juan Carlos --> El cliente al ser http 1.1 intenta usar conexiones persistentes, pero el servidor (el nuestro) tiene que enviarle connection:close
            //para que sepa que tiene que hacer conexión por recurso.
            connection= "Connection: close \r\n";
            
            //Fecha
            Date fecha = Fecha();
            //Es de tipo date, creamos una variable de tipo string para que podamos utilizar el método getBytes()
            String cabeceraFecha= fecha + " \r\n";
            
            //Allow --> Allow= #Methods 
            //El propósito es informar al destinatario de los métodos de solicitud válidos asociados con el recurso.
            //En la práctica solo permitimos el método get.
            allow="Allow: #GET\r\n"; 
            
           //Cabecera Server
           //Muestra el tipo de servidor HTTP empleado
           server="Server: Servidor HTTP 1.1\r\n\r\n"; //--> Se pone \r\n\r\n porque es fin de cabeceras
            
           //output.write necesita bytes [escribe byte]
           //Este output se puede ver en el cliente telnet
           //Linea de estado
               output.write(outdata);
               
               //Escribimos cabeceras
               output.write(contentType.getBytes()); //Necesita bytes, por eso getBytes
               output.write(contentLength.getBytes());
               output.write(connection.getBytes());
               output.write(cabeceraFecha.getBytes());
               output.write(allow.getBytes());
               output.write(server.getBytes());
               
               
               //Recurso
               if(outdataRecurso!=null){
               output.write(outdataRecurso);
               }
           }else{
               
                //Linea de estado
               output.write(outdata);
               //Tenemos que quitar el Recurso ya que aquí aparece nulo y nos sale excepcion
               if(outdataRecurso!=null){
               output.write(outdataRecurso);
               }
           }
            
            
            input.close();
            output.close();
            mSocket.close();
    
        } catch (IOException e) {
            System.err.println("Exception" + e.getMessage());
        }
        }

    
    /**
     * Método para obtener la fecha
     * @return la fecha actual 
     */
    
    private Date Fecha(){
        java.util.Date fecha = new Date();
        return fecha;
    }
    
    /**
     * Método para leer un recurso del disco
     * @param resourceFile
     * @return los bytes del archivo o null si éste no existe
     */
    
    /*
    private byte[] leerRecurso(String resourceFile){        
        //./ es para el directorio
        File f= new File("./"+resourceFile);
        byte[] bytesArray = null;
        FileInputStream fis=null;
        try{
       fis = new FileInputStream (f);
       bytesArray = new byte[(int) f.length()];
       fis.read(bytesArray);
       BufferedInputStream bis = new BufferedInputStream(fis);
       bis.read(bytesArray, 0 , bytesArray.length);

        }catch(IOException e) {
            e.printStackTrace();
        }  
       return bytesArray;
    }*/
    
     private byte[] leerRecurso(String resourceFile){        
        //./ es para el directorio
        File f= new File("./"+resourceFile);
        byte[] bytesArray = null;
        FileInputStream fis=null;
        try{
       fis = new FileInputStream (f);
       bytesArray = new byte[(int) f.length()];
       fis.read(bytesArray);
      // BufferedInputStream bis = new BufferedInputStream(fis);
      // bis.read(bytesArray, 0 , bytesArray.length);

        }catch(IOException e) {
        }  finally{
            if(fis !=null){
                try{
                    fis.close();
                }catch(IOException e){
                    e.printStackTrace();
                    
                }
               
            }
        }
       return bytesArray;
    }
    
    
    
}