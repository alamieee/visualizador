package Tzinacan.PPI.ArregloPies;

import java.util.*;

public class ArregloPies {

	/*!
     * \brief recibidoMensaje Vector que almacena arreglos de tipo byte.
     */
	Vector <byte[]> arregloPiesRecibido = null;
	byte[] auxByte = null;
	
	//Tamaño de cadena  8 EPR
	int tamCadena=16384;//65536/4;
//---------------------8 Bits---------------------
//	int pies=512;//32768
//	int pies=256;//65536
//	int pies=128;//131072
//	int pies=64;//262144
//	int pies=32;//524288
//	int pies=16;//1048576
	
//---------------------4 Bits---------------------	
	//Numero de pies a pintar

	int pies=512;//16384
//	int pies=256;//32768
//	int pies=128;//65536
//	int pies=64;//131072
//	int pies=32;//262144
//	int pies=16;//524288
//	int pies=8;//1048576
	
	public ArregloPies()
	{
//---------------------8 Bits---------------------	
//		arregloPiesRecibido = new Vector <byte[]>(512);//32768
//		arregloPiesRecibido = new Vector <byte[]>(256);//65536
//		arregloPiesRecibido = new Vector <byte[]>(128);//131072
//		arregloPiesRecibido = new Vector <byte[]>(64);//262144
//		arregloPiesRecibido = new Vector <byte[]>(32);//524288
//		arregloPiesRecibido = new Vector <byte[]>(16);//1048576
		
//---------------------4 Bits---------------------	
		arregloPiesRecibido = new Vector <byte[]>(pies);//32768
//		arregloPiesRecibido = new Vector <byte[]>(128);//65536
//		arregloPiesRecibido = new Vector <byte[]>(64);//131072
//		arregloPiesRecibido = new Vector <byte[]>(32);//262144
//		arregloPiesRecibido = new Vector <byte[]>(16);//524288
//		arregloPiesRecibido = new Vector <byte[]>(8);//1048576

		inicializaArreglo();
	}

	public byte[] getAuxByte()
	{
		return auxByte;
	}
	
	public void inicializaArreglo()
	{
		auxByte = new byte [tamCadena];//32768
//		auxByte = new byte [32768];//32768
//		auxByte = new byte [65536];//65536
//		auxByte = new byte [131072];//131072
//		auxByte = new byte [262144];//262144
//		auxByte = new byte [524288];//524288
//		auxByte = new byte [1048576];//1048576
		
		 Arrays.fill(auxByte, (byte) 0);
		 
		 for (int i=0; i<pies; i++)
		 {
			 arregloPiesRecibido.addElement(auxByte);
		 }
		 auxByte = null;
	}
	
	public synchronized byte[] getMensajeArregloFpga(int index){
    	return arregloPiesRecibido.elementAt(index);
    }
	
	public synchronized void setMensajeArregloFpga(byte[] arr, int index){
    	arregloPiesRecibido.insertElementAt(arr, index);
    	arregloPiesRecibido.removeElementAt(index+1);
    	//System.out.println("++++++++++++++++++Agregado en el modelo  index Pies = "+index);
    	//System.out.println("++++++++++++++++++tamaño del arreglo Pies = "+arregloPiesRecibido.size());
    }
	
	
}//fin class
