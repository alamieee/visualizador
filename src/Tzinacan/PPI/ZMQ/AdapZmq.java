
/** @file AdapZmq.java
 * @brief Declaración de la clase AdapZmq
 *
 *  Clase de tipo Thread que crea un objeto de tipo AdapRecibir, para poder realizar la lectura
 *  del socket independiente del proceso principal.
 *  
 *  \version 1.0.0
 *  \date 21-Julio-2017
 *  @author Miguel Angel
*/


/*--  Packages  --*/
package Tzinacan.PPI.ZMQ;


/*--  Import  --*/
import java.lang.*;
import java.util.*;
import Tzinacan.PPI.ZMQ.AdapRecibir;
import Tzinacan.PPI.ArregloPies.ArregloPies;
import java.util.Timer;
import java.util.TimerTask;
import zmq.*;
import javax.crypto.spec.PSource;


/*!
 * \class AdapZmq
 * \brief Clase que extiende de Thread para ejecutarse como un hilo y recibir de los sockets del ZMQ.
 */
public class AdapZmq extends Thread{
	
	/*!
     * \brief Objeto de tipo ArregloPies donde se almacenan los mensajes recibidos por los sockets .
     */
	ArregloPies _piesAdapZmq = null;
	/*!
     * \brief Objeto de tipo AdapRecibir, la recepción de los sockets.
     */
	AdapRecibir _recibirAdapZmq = null;
	/*!
     * \brief Arreglo de bytes donde se almacena el mensaje recibido.
     */
	public byte[] mensaje = null;
	
	
	/*!
     * \brief Arreglo de bytes donde se almacena el mensaje recibido.
     */
	public byte[] mensaje1 = null;
	/*!
     * \brief Arreglo de bytes donde se almacena el mensaje recibido.
     */
	public byte[] mensaje2 = null;
	public byte[] mensaje3 = null;
	public byte[] mensaje4 = null;
	

	/*!
     * \brief Variable de tipo string, la cual contiene el mensaje en formato de cadena.
     */
	String cadenaMensaje = null;
	/*!
     * \brief Variable de tipo entero, la cual indica la posición donde se almacenará el mensaje recibido.
     */
	static int posicionAdapZmq;
	/*!
     * \brief Variable donde se guarda el azimut.
     */
	int azimut;
	int azimut2;
	/*!
     * \brief Variable que indica la posición del Azimut.
     */
	int posAzimut;
	int posAzimut2;
	int posAzimut3;
	/*!
     * \brief Variable donde se almacena el tamaño del mensaje.
     */
	int itamMensaje;
	int itamVector;
	int itamVecEPR;
	
	int iIniVec,iFinVec;
	

	int numPies = 512;
	
	/*!
     * \brief Contructor
     */
	public AdapZmq(){
		posicionAdapZmq = 0;
		_recibirAdapZmq = new AdapRecibir();
		conectarZMQ();
		azimut = 0;
    	posAzimut = 0;
    	itamMensaje =0;
    	itamVector=0;
    	itamVecEPR=32;
    	iIniVec=0;
    	iFinVec=0;

	}
	
	/*!
     * \fn setPiesAdapZmq
     * \brief Función que permite colocar el objeto de tipo ArregloPies a una
     * variable local.
     * \param p Apuntador de tipo ArregloPies
     */
	public void setPiesAdapZmq( ArregloPies p){
		_piesAdapZmq = p;
	}

	/*!
     * \fn getPiesAdapZmq
     * \brief Función que permite obtener el objeto local de tipo ArregloPies
     * \return El apuntador de tipo ArregloPies
     */
	public ArregloPies getPiesAdapZmq(){
		return _piesAdapZmq;
	}
	
	/*!
     * \fn conectarZMQ
     * \brief Función que invoca a las funciones necesarias para iniciar las conexiones
     * con el ZMQ
     */
    public void conectarZMQ(){
    	//recvAlgo.inicializaPaquete();
	    //recvAlgo.ejecutarZMQRecibir("127.0.0.1","5001");
    	_recibirAdapZmq.ejecutarZMQRecibir("*","3010");
    	//_recibirAdapZmq.ejecutarZMQRecibir("*","3010");
    	
    	//_recibirAdapZmq.ejecutarZMQRecibir("*","5031");
    }
	
    /*!
     * \fn buscaAZ
     * \brief Función que permite buscar las variables A y Z en un arreglo dado.
     * \param arreglo Arreglo de bytes.
     * \param posInicial Entero que indica la posicion Inicial a buscar.
     * \param tamArreglo Tamaño del arreglo.
     * \return La posición donde se localiza la variable A seguida de la variable Z.
     */
	public int buscaAZ(byte[] arreglo, int posInicial,  int tamArreglo) 
 	{
//		int A = 0x41;
//		int Z = 0x5a;
//		
//		for(int i=posInicial; i < tamArreglo; i++)
//		{
//			if( A == arreglo[i])
//				if( Z == arreglo[i+1])
//					return i;
//		}
		int A = 0x41;
		int Z = 0x5a;
		int M = 0x4D;
		int T = 0x54;

		for (int i = posInicial; i < tamArreglo-4; i++) {
			if (A == arreglo[i])
				if (Z == arreglo[i + 1])
					if (M == arreglo[i + 4])
						if (T == arreglo[i + 5])
					return i;
		}
		
		return tamArreglo;
	}//Fin buscaAZ
	
	/*!
     * \fn azimutIni
     * \brief Función que permite formar el valor del azimut con dos bytes que representar los bits más y menos significativos.
     * \param alta byte con la parte alta del valor.
     * \param baja byte con la parte baja del valor.
     * \return El valor en entero que corresponde al azimut.
     */
	public int azimutIni(byte alta, byte baja) 
 	{ 
		  int Azimut;
		  
		  Azimut=alta & 0x0F;
		  Azimut=Azimut<<8;
		  
		  Azimut+=(baja & 0XFF);
		  
	  
		  return Azimut;
 	}//Fin AzimutIni
	
	/*!
     * \fn run
     * \brief Función run del hilo, esta función se realiza:
     *  - El proceso de recepción del mensajes por el ZMQ.
     *  - La obtención del azimut inicial.
     *  - El calculo de la posición a almacenar el el arregloPies.
     */
	public void run()
	{
		
		cadenaMensaje="";
		itamMensaje =0;
		itamVector=0;
		
		System.out.println("ejecutando?????!!!!!!");
		
		while (true)
		{
			
			mensaje = _recibirAdapZmq.recibir1();
			_recibirAdapZmq.enviar.send(mensaje, ZMQ.ZMQ_NOBLOCK);
	    	itamMensaje = mensaje.length;

//	    	if (posicionAdapZmq==numPies-6){ //Tamaño de Pies = 16
//    			posicionAdapZmq=0;
//	    	}
	    	
	    	
//	    	System.out.println("Valor de mensaje "+itamMensaje);
	    
	    	itamVector=itamMensaje/itamVecEPR;
	    	
	    	iIniVec=0;
	    	
	    	iFinVec=itamVector;
	    	
//	    	System.out.println("!!!!!!!!!!!!!!!!!!!!!!Valores inicial "+ iIniVec+ " Valor final " + iFinVec);
	    	
	    	mensaje1 =new byte [itamVector];

	    	
	    	//Se agrega segmentación del mensaje 
	    	
	    	
	    	for(int i=0;i<itamVecEPR;i++)
	    	{
	    		mensaje1 =new byte [itamVector];
		    	System.arraycopy(mensaje, iIniVec, mensaje1, 0, itamVector);
		    	_piesAdapZmq.setMensajeArregloFpga(mensaje1, posicionAdapZmq);
				posicionAdapZmq++;
				iIniVec=iFinVec;
				iFinVec=iIniVec+itamVector;
				//System.out.println("Posición inicial "+iIniVec+" Posición Final "+iFinVec+ " Posicion de azimut "+ posicionAdapZmq);
//				System.out.println("Posición inicial "+iIniVec+" Posición Final "+iFinVec);
	    	}
	    	

	    	if (posicionAdapZmq==numPies){ //Tamaño de Pies = 16
			
	    		posicionAdapZmq=0;
			}
	    	
	    	
//	    	posAzimut3 = buscaAZ(mensaje, 0, itamMensaje);	    	
//			while ((posAzimut3 < itamMensaje))// <128 <256
//			{
////
//				posAzimut3 = buscaAZ(mensaje, posAzimut3, itamMensaje);
//				posAzimut2 = buscaAZ(mensaje, posAzimut3 + 4, itamMensaje);
//				azimut2 = azimutIni(mensaje[posAzimut3 + 3],
//						mensaje[posAzimut3 + 2]);
//				System.out.println(+azimut2);
////				//if (azimut < 10 | azimut > 4080) {
////				//	azimut = azimut3;
////				//}
////				//azimut2 = azimut + azimut2;
//				posAzimut3 = posAzimut2;
////				//azimut3 = azimut;
////
//			} // Fin while +++++++++++++++++++++++++
			
	    	
//	    	System.out.println("Valor de mensaje 1 "+  mensaje1.length);
//	    	System.out.println("Valor de mensaje 2 "+  mensaje2.length);
	    	
	    	
////	    	System.out.println("++++++++Tamaño del mensaje = "+itamMensaje);
//
//	    	posAzimut = buscaAZ(mensaje, 0, itamMensaje);
//	    	//posAzimut2 = buscaAZ(mensaje,2048*32, itamMensaje);
//	    	//System.out.println("++++++++Tamaño del mensaje = "+posAzimut2);
//	    	azimut = azimutIni(mensaje[posAzimut+3], mensaje[posAzimut+2]);
//	    	
//	    	//System.out.println("++++++++Tamaño del mensaje1 = "+mensaje[posAzimut+2]);
//	    	//System.out.println("++++++++Tamaño del mensaje2 = "+mensaje[posAzimut+3]);
//	    	//System.out.println("++++++++Tamaño del mensaje3 = "+mensaje[posAzimut2+2]);
//	    	//System.out.println("++++++++Tamaño del mensaje4 = "+mensaje[posAzimut2+3]);
//	    	//System.out.println("++++++++Tamaño del mensaje = "+azimut);
//	    	//System.out.println("++++++++Tamaño del mensaje = "+itamMensaje);
//	    	
//	    	if((posAzimut+2<itamMensaje) && (posAzimut+3<itamMensaje))
//	    	{
//	    		posicionAdapZmq = (int)(azimut/32);
////	    		AZ=tempAZ;
////	    		tempAZ = (int)(azimut/256); //524288
////	    		if((tempAZ==AZ) && (tempAZ<15))//524288
////	    	    //tempAZ = (int)(azimut/8); //32768
////	    		//if((tempAZ==AZ) && (tempAZ<511))//32768
////		    	{
////	    			tempAZ = tempAZ+1;
////		    	}
////	    		posicionAdapZmq=(int)tempAZ;
//		    	
////		    	System.out.println("++++++++posicionAdapZmq = "+posicionAdapZmq);
//				
//				_piesAdapZmq.setMensajeArregloFpga(mensaje, posicionAdapZmq);
//	    	}
//	    	/**/
//	    	//mensaje = null;
//			//cadenaMensaje="";
	    	
	    	


		}//fin while
	}// fin run
	
}
