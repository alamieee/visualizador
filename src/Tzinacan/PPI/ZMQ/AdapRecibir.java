
/** @file AdapRecibir.java
 * @brief Declaración de la clase AdapRecibir
 *
 *  Clase que se utiliza para la conexión ZMQ utilizando a la clase Recibir
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
import Tzinacan.PPI.ArregloPies.ArregloPies;
import Tzinacan.PPI.ZMQ.Recibir;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.*;
import java.text.SimpleDateFormat;



/*!
 * \class AdapRecibir
 * \brief Clase que extiende de Recibir para conectarse a los sockets del ZMQ
 */
public class AdapRecibir extends Recibir {

	/*!
     * \brief Arreglo de bytes donde se guardará el mensajes leido por el socket.
     */
	byte[] mensaje = null;
	int cPaquetes=0;

	String Datos;
	
	ObjectOutputStream escribiendoFichero=null;
	/*!
     * \brief Contructor
     */
//	public AdapRecibir(){
//		
//		
//		java.util.Date fecha = new Date();
//		SimpleDateFormat formateador = new SimpleDateFormat("hh_mm_ss");
//		Datos="Objeto"+formateador.format(fecha)+".bin";
//		
//		try {
//			escribiendoFichero = new ObjectOutputStream(new FileOutputStream(Datos) );
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public void setArregloPiesToZmq(ArregloPies arr)
	{
		//_recibirZmq.setPiesAdapZmq(arr);
	}
	
	/*!
     * \fn recibir1
     * \brief Declaración de la función virtual de MyObserverRecibir
     * \param value Paquete recibido
     */
	
	
    public byte[] recibir1(){
    	
    	//cadenaMensaje="";
    	mensaje = null;
    	
        //long inicio = System.currentTimeMillis();
      
    	//System.out.println("fdsfds");
		try
		{
		   mensaje = recibir.recv(0);
		   String cadenaMensaje = new String(mensaje);
		  // System.out.println("En el test =="+mensaje.length);

		 /*  if(cPaquetes<=50) {
			   
			   System.out.println("Guardando ArrayList en el fichero ");
			   
			   escribiendoFichero.writeObject(mensaje);
		   }*/
			   

		   
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Error CorreLeeOrdenSocket: :O :"+ex.toString());
		}
		cPaquetes++;
		
        //long fin = System.currentTimeMillis();
        //long tiempo = (fin - inicio);
        //System.out.println(tiempo +" Milis");
        
		return mensaje;
    }
}
