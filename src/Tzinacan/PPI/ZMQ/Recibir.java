
/** @file Recibir.java
 * @brief Declaración de la clase Recibir
 *
 *  Clase que se utiliza para la conexión ZMQ
 *  
 *  \version 1.0.0
 *  \date 21-Julio-2017
 *  @author Miguel Angel
*/

/*--  Packages  --*/
package Tzinacan.PPI.ZMQ;

/*--  Imports  --*/
import java.util.*;
import org.zeromq.ZMQ;
import java.lang.*;


/*!
 * \class Recibir
 * \brief Clase utilizada para la conexión con sockets usando el ZMQ
 */
public abstract class Recibir {

	/*!
    * \brief contextZMQ Apuntador para el context del socket
    */
    	ZMQ.Context contextZMQ = null;
	/*!
     * \brief recibir Apuntador para el socket del ZMQ
     */
    	public ZMQ.Socket recibir = null;
    	public ZMQ.Socket enviar = null;
    /*!
     * \brief ipRecibir Dirección ip para recibir los datos
     */
    	String ipRecibir = null;
    	String ipEnviar = null;
    /*!
     * \brief puertoRecibir Puerto para recibir los datos
     */
    	String puertoRecibir = null;
    	String puertoEnviar = null;
    /*!
     * \brief direccionRecibir Cadena formada por el ip y puerto de la forma
     * "tcp://ip:puerto"
     */
    	String direccionRecibir = null;
    	String direccionEnviar = null;
	/*!
     * \brief Contructor
     */
    public Recibir(){
    	
    }
    
    /*!
     * \fn recibir1
     * \brief Función virtual que se declarará en otra instancia
     * \param value Valor que contiene la informaión recibida
     */
	public abstract byte[] recibir1();
    	
	/*!
     * \fn ejecutarZMQRecibir
     * \brief Función que se encarga de ejecutar las funciones de creaSocketRecibir() y conectaZMQ()
     * \param ipO ip destino
     * \param in puerto
     */
	public void ejecutarZMQRecibir(String ipO, String out){
		ipRecibir = ipO;
        puertoRecibir = out;
        direccionRecibir = "tcp://192.168.200.140:" + puertoRecibir;
        System.out.println("a====  "+direccionRecibir);
        //ipEnviar = ipO;
        puertoEnviar = "3333";
        direccionEnviar =  "tcp://192.168.200.140:" + puertoEnviar;
        
        
        creaSocketRecibir();
        conectaZMQ();
	}
    /*!
     * \fn creaSocketRecibir
     * \brief Función que se encarga de crear el socket PULL
     */
	public void creaSocketRecibir(){
		//Iniciando apuntadores
        contextZMQ = ZMQ.context(3);
        
        if(contextZMQ != null)
        {
        	recibir = contextZMQ.socket(ZMQ.PULL);
        	enviar = contextZMQ.socket(ZMQ.PUSH);
      
        }
	}
    	
    /*!
     * \fn conectaZMQ
     * \brief Función que se encarga de conectar el socket creado a la dirección destino (incluyendo el puerto)
     */
	public void conectaZMQ(){
		try
        {
            if(contextZMQ != null)
            {
                if(recibir != null)
                {
                    //recibir.connect(direccionRecibir);
                	recibir.bind(direccionRecibir);
                	recibir.setHWM(100);
                    System.out.println("creado recibir");
                    System.out.println("conexion establecida");
                }
                else
                {
                	System.out.println("No creado recibir");
                }
                
                if(enviar != null)
                {
                	enviar.bind(direccionEnviar);
                	enviar.setHWM(100);
                }
            }
        }
        catch(Exception ex)
        {
        	System.out.println("Error: " + ex.getMessage());
        }
	}
	    	
}
