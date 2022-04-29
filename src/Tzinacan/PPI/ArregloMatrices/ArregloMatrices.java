package Tzinacan.PPI.ArregloMatrices;

import java.util.Vector;

public class ArregloMatrices {

	
	/*!
     * \brief recibidoMensaje Vector que almacena arreglos de tipo byte.
     */
	Vector <double[][]> arregloMatrizRecibido = null;
	//static BufferedImage buffRenderImage;
	//BufferedImage buffRenderImage;
	
	double[][] iAuxMat = null;
	
	//---------------------8 Bits---------------------
//	int pies=512;//32768
//	int pies=256;//65536
//	int pies=128;//131072
//	int pies=64;//262144
//	int pies=32;//524288
//	int pies=16;//1048576
	
//---------------------4 Bits---------------------	
	int pies=512;//32768
//	int pies=256;//32768
//	int pies=128;//65536
//	int pies=64;//131072
//	int pies=32;//262144
//	int pies=16;//524288
//	int pies=8;//1048576

	public ArregloMatrices()
	{
//		---------------------------8 Bits---------------------------
//		arregloMatrizRecibido = new Vector <double[][]>(512);//32768
//		arregloMatrizRecibido = new Vector <double[][]>(256);//65536
//		arregloMatrizRecibido = new Vector <double[][]>(128);//131072
//		arregloMatrizRecibido = new Vector <double[][]>(64);//262144
//		arregloMatrizRecibido = new Vector <double[][]>(32);//524288
//		arregloMatrizRecibido = new Vector <double[][]>(16);//1048576
		
//		---------------------------4 Bits---------------------------
		arregloMatrizRecibido = new Vector <double[][]>(pies);//32768
		
//		arregloMatrizRecibido = new Vector <double[][]>(128);//65536
//		arregloMatrizRecibido = new Vector <double[][]>(64);//131072
//		arregloMatrizRecibido = new Vector <double[][]>(32);//262144
//		arregloMatrizRecibido = new Vector <double[][]>(16);//524288
//		arregloMatrizRecibido = new Vector <double[][]>(8);//1048576
		//buffRenderImage = new BufferedImage(1300, 1000, BufferedImage.TYPE_INT_ARGB);
		inicializaMatriz();
	}
	
	public void inicializaMatriz()
	{
		iAuxMat = new double[1000][1000];
		
		for (int i = 0; i < 1000; i++) 
		{
			 for (int j = 0; j < 1000; j++) 
			 {
				 iAuxMat[i][j]=0;
			 }
		}
		
		for (int i=0; i<pies; i++)
		 {
			arregloMatrizRecibido.addElement(iAuxMat);
		 }
		iAuxMat = null;
		
	}
	
	public synchronized double[][] getMatrizMostrar(int index){
    	return arregloMatrizRecibido.elementAt(index);
    }
	
	public synchronized void setMatrizMostrar(double[][] arr, int index){
    	arregloMatrizRecibido.insertElementAt(arr, index);
    	arregloMatrizRecibido.removeElementAt(index+1);
    	//System.out.println("Agregado en el modelo  index = "+index);
    	//System.out.println("++++++++++++++++++Agregado en el modelo  index Matriz = "+index);
    	//System.out.println("++++++++++++++++++tamaño del arreglo Matriz = "+arregloMatrizRecibido.size());
    	
    }
	

	
}//fin class
