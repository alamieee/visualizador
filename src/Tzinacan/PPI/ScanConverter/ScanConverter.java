package Tzinacan.PPI.ScanConverter;

//import org.apache.commons.math3.linear.Array2DRowRealMatrix;
//import org.apache.commons.math3.linear.RealMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import Tzinacan.Visualizador;
import Tzinacan.PPI.ArregloMatrices.ArregloMatrices;
import Tzinacan.PPI.ArregloPies.ArregloPies;
import static org.jocl.CL.*;//?

import org.jocl.*;//?
import java.util.HashMap;


public class ScanConverter {
	private static cl_context context = null;//OpenCL context
	private static cl_command_queue commandQueue = null; //OpenCL command queue
	Visualizador objSim;
	// int var=1;

	ArregloPies _arregloPiesScanConv = null;
	ArregloMatrices _arregloMatricesScanConv = null;

	/*
	 * ! \brief matrizPuntosGlobal Matriz que almacena los valores resultantes de la
	 * descompresion de los datos.
	 */
	public static int[][] matrizPuntosPie = null;
	/*
	 * ! \brief matrizPuntosRadarGlobal Matriz que almacena los valores resultantes
	 * del ScanConverter.
	 * 
	 */
	public static double[][] matrizScanConverter = null;
	public static double[][] matrizScanConverterAux = null;

	public static int[][] matrizCfar = null;
	/*
	 * ! \brief iPosicionPie Variable que se encarga de llevar la posicion leida del
	 * pie a procesar.
	 */
	static int iPosicionPie;
	static int iPosicionScanConv;
	static int numVuelta;
	static boolean primeraVuelta;
	/*
	 * ! \brief azimutInicial Variable que indica el primer azimut del pie a
	 * procesar.
	 */
	int azimutInicial;
	/*
	 * ! \brief Delta V .
	 */
	double Delta;
//---------------------8 Bits---------------------	
//	static int pies=512;//16//32//64//128----32768
//	static int pies=256;//16//32//64//128----65536
//	static int pies=128;//16//32//64//128----131072
//	static int pies=64;//16//32//64//128----262144
//	static int pies=32;//16//32//64//128----524288
//	static int pies=16;//16//32//64//128----1048576
	
//	static int iTamPie=16;//32/64/128/----32768
//	static int iTamPie=16;//32/64/128/----65536
//	static int iTamPie=32;//32/64/128/----131072
//	static int iTamPie=64;//32/64/128/----262144
//	static int iTamPie=128;//32/64/128/----524288
//	static int iTamPie=256;//32/64/128/----1048576
	
//---------------------4 Bits---------------------	
	static int pies=512;//16//32//64//128----32768
//	static int pies=128;//16//32//64//128----65536
//	static int pies=64;//16//32//64//128----131072
//	static int pies=32;//16//32//64//128----262144
//	static int pies=16;//16//32//64//128----524288
//	static int pies=8;//16//32//64//128----1048576
	
	static int iTamPie=8;//32/64/128/----32768
//	static int iTamPie=32;//32/64/128/----65536
//	static int iTamPie=64;//32/64/128/----131072
//	static int iTamPie=128;//32/64/128/----262144
//	static int iTamPie=256;//32/64/128/----524288
//	static int iTamPie=512;//32/64/128/----1048576
	static int iTamRangeCustom=4096;//2048/4096/8192
	//4096 , same rangeBeamCustom

	byte[] arregloIntervalo = null;

	byte[] bArregloAux = null;
	byte[] cadenaMensaje = null;

	double rangeBeanCustom;

	int erosionCustom;
	int dilatacionCustom;

	int iTv;
	int iCg;

	int iAnguloRangoRotacionPpi;
	// double iAnguloRangoRotacionPpi;

	boolean banderaVueltas = false;
	private HashMap<String, Integer> map = new HashMap();
	// TERMINA VARIABLES PARA REALIZAR LA EROSION Y DILATACION

	
	public ScanConverter(Visualizador siccam) {
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		inicializarOpenCL();
		//------------------------------------------2018 cambios
		iPosicionPie = 0;
		iPosicionScanConv = 0;
		numVuelta = 0;
		primeraVuelta = true;
		matrizPuntosPie = new int[iTamPie][iTamRangeCustom];//valores resultantes del scan
		matrizScanConverter = new double[1000][1000];//resolucion de la pantalla
		matrizScanConverterAux = new double[1000][1000];

		matrizCfar = new int[iTamPie][iTamRangeCustom];
		Delta=0.087890625;//?
		//Delta = 0.088888889;

		rangeBeanCustom = 4096.0;// 4080
		//rangeBeanCustom = 4000.0;//4080
		erosionCustom = 0;//?
		dilatacionCustom = 0;//?

		objSim = siccam;
		iTv = 24;//?
		iCg = 2;//?
        	//System.out.println("ver"+ matrizPuntosPie.length);


		iAnguloRangoRotacionPpi = 2;// 3
		
		inicializaMatriz();
	}
	

	//OPENCL
	public void inicializarOpenCL(){

        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device}, 
            null, null, null);
        
        // Create a command-queue for the selected device
        System.out.println("Creando cola ...");
        cl_queue_properties properties = new cl_queue_properties();
        commandQueue = clCreateCommandQueueWithProperties(
            context, device, properties, null);
       
	}//FIN-OPENCL
	
	// Esta es la que se ejecuta
	public void ScanConverter2() {//readfile
		
		
		StringBuilder sb = new StringBuilder();//objeto mutrable, debe crearse con el constructor, no se ṕuede instanciar directamente
		
		File srckernel = new File("/home/radar/scan_converter.cl");//Cursor Library
		try {
			Scanner reader = new Scanner(srckernel);//produce valores escaneados del archivo especificado
			while(reader.hasNextLine())
			{
				String line = reader.nextLine();
				sb.append(line);//se agrega al strinbuilder el arcivo .cl
			}
			reader.close();//porque se cierra?
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //clReleaseKernel(kernel);
        //clReleaseProgram(program);
       
        /*
        for (int i=0; i<n*n; i++)
        {
           System.out.println(rdevice[i]);
        }
        */

		/*
		 * System.out.println("++++++++posicionAdapZmq1 = "+azimutInicial);
		 * if(azimutInicial<45) { azimutInicial = 4096 - azimutInicial-1;
		 * System.out.println("++++++++360 = "+azimutInicial); } else { azimutInicial =
		 * azimutInicial - 45; System.out.println("++++++++45 = "+azimutInicial); }
		 * System.out.println("++++++++posicionAdapZmq2 = "+azimutInicial);
		 */

		/**/int iAuxCalc = 0;		// iARPpi = 2
		iAuxCalc = azimutInicial - iAnguloRangoRotacionPpi;//Aqui iAuxCal empieza en -2
				//System.out.println(iAnguloRangoRotacionPpi);
		//System.out.println(iAuxCalc);
		 //System.out.println("azimutI " + azimutInicial);
		if (iAuxCalc < 0) {
			iAuxCalc = iAuxCalc + 1;
			azimutInicial = 4096 + iAuxCalc;//azumutinicial = 4095
		} else {
			azimutInicial = iAuxCalc;
		}
	
		double dAuxAzimut = (double) azimutInicial;//4095  la primera vez 
		// double Begin = azimutInicial*Delta;
		double Begin = dAuxAzimut * Delta;//4095 * 0.087890625 = 359.912109375
		double SizePpi = 1000.0;
		// int range = 3 ;//millas nauticas //eqvale a 5.556km
		double AzimutBeans = 4096.0;
		// double rangeBeans = 4086.0; //500
		double MaxAzimut = (360.0 * Math.PI) / 180.0;// radianes //6.283185307179586
		double InitAzimut = (Begin * Math.PI) / 180.0;// radianes   initazimut = 5.048330772931645
		// EndAzmiut no se usa
		double DeltaAzimut = MaxAzimut / AzimutBeans;  //0.0015339807878856412
		// double maxRange = range*1825;
		// Deltarange no se usa
		double Center = SizePpi / 2.0;
		double Escala = Center / rangeBeanCustom; //0.1220703125
		double Azimut = InitAzimut; //5.048330772931645

		int x = 0;
		int y;
		int Range = 1;

		double val = 0;

		 //System.out.println(" Escala==........."+Escala);
		 //System.out.println(" Center==........."+Center);

		//System.out.println("iNIT  "+(double)InitAzimut);
		
		//System.out.println("deltaAZ  "+(double)DeltaAzimut);
		//System.out.print(matrizPuntosPie.length);
		
/*matrizPuntosPie hasta el momento
    	int cc=0;
		for (int i = 0; i<matrizPuntosPie.length; i++) {
			for (int j = 0; j<matrizPuntosPie[i].length; j++) {
				
				//matrizPuntosPie[i][j]=0;
				System.out.print(matrizPuntosPie[i][j]);
				cc=cc+1;
			}
			System.out.println(cc);
		}
*/
		/*
		 * 	int hostMatrizPuntosPie[] = new int[matrizPuntosPie.length * matrizPuntosPie[0].length];
		double hostMatrizScanConverter[] = new double[matrizScanConverter.length * matrizScanConverter[0].length];
		double hostAzimuts[] = new double[iTamPie];
	
		for (int i = 0; i < matrizPuntosPie.length; i++) {
			
			for (int j = 0; j < matrizPuntosPie[0].length; j++)
			{
				hostMatrizPuntosPie[j + i * matrizPuntosPie[0].length] = matrizPuntosPie[i][j];
			}
		
		}
		for (int i = 0; i < matrizScanConverter.length; i++) {
			
			for (int j = 0; j < matrizScanConverter[0].length; j++)
			{
				hostMatrizScanConverter[j + i * matrizScanConverter[0].length] = matrizScanConverter[i][j];
			}
		
		}
		hostAzimuts[0] = Azimut;
		for (int i = 1; i < iTamPie; i++)
		{
			hostAzimuts[i] += hostAzimuts[i - 1] + DeltaAzimut;
		}
		Pointer srcMpp = Pointer.to(hostMatrizPuntosPie);
		Pointer srcMsc = Pointer.to(hostMatrizScanConverter);
		Pointer srcAzimut = Pointer.to(hostAzimuts);
		cl_mem memMpp = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * hostMatrizPuntosPie.length, srcMpp, null);
		cl_mem memMsc = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double * hostMatrizScanConverter.length, srcMsc, null);
		cl_mem memAzmt = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_double * hostAzimuts.length, srcAzimut, null);
		
		
		
		cl_program program = clCreateProgramWithSource(context, 1, new String[]{sb.toString()}, null, null);
	 	clBuildProgram(program, 0, null, null, null, null);
	 	cl_kernel kernel = clCreateKernel(program, "scan_converter", null);
	 	clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memAzmt));
	 	clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memMpp));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memMsc));
	 	clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{matrizScanConverter[0].length}));
	 	clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{matrizPuntosPie[0].length}));
	 	clSetKernelArg(kernel, 5, Sizeof.cl_double, Pointer.to(new double[]{Center}));
	 	clSetKernelArg(kernel, 6, Sizeof.cl_double, Pointer.to(new double[]{Escala}));
	 	long global_work_size [] = new long[]{iTamPie, (long)rangeBeanCustom};
	 	clEnqueueNDRangeKernel(commandQueue, kernel, 2, null, global_work_size, null, 0, null, null);
	 	clEnqueueReadBuffer(commandQueue, memMsc, CL_TRUE, 0, hostMatrizScanConverter.length * Sizeof.cl_double, srcMsc, 0, null, null);
        System.out.println(hostMatrizScanConverter[0]);
	 	clReleaseMemObject(memAzmt);
        clReleaseMemObject(memMpp);
        clReleaseMemObject(memMsc);
		 */
 //same of scan_converter.cl
        for (int i = 0; i < iTamPie; i++) {
			Range = 1;
			for (int j = 0; j < rangeBeanCustom; j++)// 4080
			{
				
				y = (int) (Center + (j+1) * Math.sin(Azimut) * Escala);
				x = (int) (Center - (j+1) * Math.cos(Azimut) * Escala);
				// x=(int)(Center+(Range*(Math.sin(Azimut))*Escala));
				// y=(int)(Center-(Range*(Math.cos(Azimut))*Escala));
				
			    //si x y y son negativos los vuelve positivos
				if (x < 0)
					x *= -1;

				if (y < 0)
					y *= -1;
				//no ser mayor a 999
				if (y > 999)
					y = 999;

				if (x > 999)
					x = 999;

				val = matrizPuntosPie[i][j];
				if (matrizScanConverter[x][y] <= val) {
					matrizScanConverter[x][y] = val;
					

				}
				/* x, y son valores que se pueden repetir, esto hace que no se pueda paralelizar este bucle
				 * por ejemplo, en la iteracion 10 se puede cumplir (matrizScanConverter[x][y] = 9) <= val, por lo tanto se actualiza el valor
				 * digamos val = 11, luego en la iteracion 20 x, y se repiten y matrizPuntosPie[i][j] = 12, por lo tanto se cumple la condicion y
				 * matrizScanConverter[x][y] termina valiendo 12, pero usando OpenCL para paralelizar este bucle, todas las iteraciones se ejecutan
				 * al mismo tiempo y en orden aleatorio, por lo que matrizScanConverter[x][y] nunca tendra un valor fijo
				 * 
				 */
				
				Range++;
			}
			val = 0;
			Azimut = Azimut + DeltaAzimut ;
			//System.out.println(Azimut);

		}//FIN-SECOND
      
        /*
         *for (int i = 0; i < matrizScanConverter.length; i++) {
				
				for (int j = 0; j < matrizScanConverter[0].length; j++)
				{
					if(matrizScanConverter[i][j] != hostMatrizScanConverter[j + i * matrizScanConverter[0].length]) System.out.println("ewarning");
					
				}
		
			}
		 System.out.println("--------------------------------------"); 
         */
		 
        
	//(R,Thetha) => (X,Y)	
		for (int i = 0; i < iTamPie; i++) {
			Range = 1;

			for (int j = 0; j < rangeBeanCustom; j++)// 4080
			{

				y = (int) (Center + (Range * (Math.sin(Azimut)) * Escala));//azimut es theta
				x = (int) (Center - (Range * (Math.cos(Azimut)) * Escala));
				// x=(int)(Center+(Range*(Math.sin(Azimut))*Escala));
				// y=(int)(Center-(Range*(Math.cos(Azimut))*Escala));

				if (x < 0)
					x *= -1;

				if (y < 0)
					y *= -1;

				if (y > 999)
					y = 999;

				if (x > 999)
					x = 999;

				matrizScanConverter[x][y] = 0;// 5
				//matrizScanConverterOpenCV.row(x).col(y).setTo(new Scalar(0));
				// matrizScanConverter[x][y]=matrizPuntosPie[i][j];

				Range++;
			}
			Azimut = Azimut + DeltaAzimut;
		}

	}// Fin class ScanConverter2


	

	public void inicializaMatriz() {
		for (int i = 0; i < iTamPie; i++) {//8
			for (int j = 0; j < iTamRangeCustom; j++) {//4096
				matrizPuntosPie[i][j] = 0;
				matrizCfar[i][j] = 0;
			}
		}

		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				matrizScanConverter[i][j] = 0;
				matrizScanConverterAux[i][j] = 0;
			}
		}
	}// fin inicializaMatriz

	
	public void setBandera(boolean val) {
		banderaVueltas = val;
	}

	public boolean getBandera() {
		return banderaVueltas;
	}

	public void setArregloPiesScanConv(ArregloPies arr) {
		_arregloPiesScanConv = arr;
	}

	public ArregloPies getArregloPiesScanConv() {
		return _arregloPiesScanConv;
	}

	public void setArregloMatricesScanConv(ArregloMatrices arr) {
		_arregloMatricesScanConv = arr;
	}

	public ArregloMatrices getArregloMatricesScanConv() {
		return _arregloMatricesScanConv;
	}

	public void setRangeBeansCustom(double val) {
		rangeBeanCustom = val;
	}

	public double getRangeBeansCustom() {
		return rangeBeanCustom;
	}

	public void setErosionCustom(int val) {
		erosionCustom = val;
	}

	public int getErosionCustom() {
		return erosionCustom;
	}

	public void setDilatacionCustom(int val) {
		dilatacionCustom = val;
	}

	public int getDilatacionCustom() {
		return dilatacionCustom;
	}

	public void setAnguloRotacionPpi(int val) {
		iAnguloRangoRotacionPpi = val;
	}

	public int getAnguloRotacionPpi() {
		return iAnguloRangoRotacionPpi;
	}
//ver donde se implementa este metodo
	public int buscaAZ(byte[] arreglo, int posInicial, int tamArreglo) {
		int A = 0x41;//HEXADEC
		int Z = 0x5a;//90
		int M = 0x4D;//77
		int T = 0x54;//84

		for (int i = posInicial; i < tamArreglo-4; i++) {//porque -4?
			if (A == arreglo[i])
				if (Z == arreglo[i + 1])
					if (M == arreglo[i + 4])
						if (T == arreglo[i + 5])
					return i;
		}

		return tamArreglo;
	}// Fin buscaAZ

	public int obtenerIntervaloAZ(int posInicial, int posFinal) {
		int tamIntervalo = 0;

		tamIntervalo = posFinal - posInicial;

		return tamIntervalo;
	}// Fin obtenerIntervaloAZ

	public byte[] obtenerValores(byte[] arreglo, int posInicial, int posFinal, int tamIntervalo) {
		arregloIntervalo = null;
		int j = 0;

		arregloIntervalo = new byte[tamIntervalo];

		for (int i = posInicial; i < posFinal; i++) {
			arregloIntervalo[j] = arreglo[i];//[i]=posicion inicial
			j++;
		}//arreglo intervalo almacenara en un vector byte[] los valores empezando de posInicial +1 hasta posFinal
		return arregloIntervalo;
	}// Fin obtenerValores

	public int meterArregloMatriz(int[][] matriz, byte[] arregloIntervalo, int k, int tamIntervalo) {
		int lFuncion = 0;// cuando se usa este metodo?
		int iAuxValor = 0;

		for (int i = 0; i < tamIntervalo; i++) {

			if (lFuncion < 4096) {

				try {
//					iAuxValor = arregloIntervalo[i]; 				//8Bits
					iAuxValor = manejoBitsMas(arregloIntervalo[i]); //4Bits
					// matriz[k][lFuncion] = iAuxValor;
					matrizPuntosPie[k][lFuncion] = iAuxValor;
					lFuncion++;

					iAuxValor = manejoBitsMenos(arregloIntervalo[i]); // 4Bits
					// matriz[k][lFuncion] = iAuxValor;
					matrizPuntosPie[k][lFuncion] = iAuxValor;
					lFuncion++;
					
				} catch (Exception ex) {
					System.out.println("Exception-----------------------");
					System.out.println("k == " + k);
					System.out.println("tamIntervalo == " + tamIntervalo);
					System.out.println("i == " + i);
					System.out.println("lFuncion == " + lFuncion);
					System.out.println("iAuxValor == " + iAuxValor);
					System.out.println("Exception");
					ex.printStackTrace();
					break;
				}
			} // FIN IF
			else
				break;
		}
		
		k++;
		return k;
	}// Fin meterArregloMatriz

	
	
	public int manejoBitsMas(byte x) {//recibe un num byte de la matriz [k][lfunction] del metodo meterArregloMatriz()
		byte valor = x;
		byte valorAux = valor;
		Byte aux = new Byte(valorAux);
		int c;

		// corrimiento
		valorAux = (byte) (valorAux >>> 4);
		// mascara
		valorAux = (byte) (valorAux & 0x0F);

		aux = new Byte(valorAux);
		c = aux.intValue();
		// Mascara para cambiar a 0 bits mas significativos
		c = c & 0x000000FF;

		// Imprime valor bits mas significativos int
		// System.out.println(c);
		return c;
	}// Fin manejoBitsMas

	
	
	public int manejoBitsMenos(byte x) {

		byte valor = x;
		byte valorAux = valor;
		Byte aux = new Byte(valorAux);
		int c;

		valorAux = valor;

		//
		valorAux = (byte) (valorAux & 0x0F);
		aux = new Byte(valorAux);

		// Convierte valor a entero
		c = aux.intValue();

		c = c & 0x000000FF;

		// Imprime valor bits menos significativos en Int
		// System.out.println(c);
		return c;
	}// Fin manejoBitsMenos

	public int azimutIni(byte alta, byte baja) {
		int Azimut;

		Azimut = alta & 0x0F;
		Azimut = Azimut << 8;

		Azimut += (baja & 0XFF);

		return Azimut;
	}// Fin AzimutIni

	public void obtenerMatriz() throws InterruptedException {

		int iPosInicial = 0;
		int iPosFinal = 1;
		int i_iMatriz = 0;
		int iTamIntervalo = 0;
		// byte[] bArregloAux = null;
		// byte[] cadenaMensaje = null;
		int iTamMensaje = 0;

//		--------8 Bits-------------
//		Thread.sleep(2);//32768
//		Thread.sleep(5);//65536
//		Thread.sleep(10);//131072
//		Thread.sleep(20);//262144
//		Thread.sleep(40);//524288
//		Thread.sleep(80);//1048576
		
//		--------8 Bits-------------
		Thread.sleep(1);//32768
//		Thread.sleep(5);//32768
//		Thread.sleep(10);//65536
//		Thread.sleep(20);//131072
//		Thread.sleep(40);//262144
//		Thread.sleep(80);//524288
//		Thread.sleep(160);//1048576
		
		// System.out.println("Obteniendo pie "+ iPosicionPie);
		cadenaMensaje = _arregloPiesScanConv.getMensajeArregloFpga(iPosicionPie);
		iTamMensaje = cadenaMensaje.length;

		int iAux1 = 0;

		// if(iTamMensaje!=262144) //---------------------------------------agosto
		// System.out.println("Tamaño del mensaje SCAN "+ iTamMensaje);

		iAux1 = buscaAZ(cadenaMensaje, iPosInicial, iTamMensaje);

//		if ((iAux1 + 2 < iTamMensaje) && (iAux1 + 3 < iTamMensaje)) {
		if ((iAux1 + 8 < iTamMensaje) && (iAux1 + 9 < iTamMensaje)) {	

//			azimutInicial = azimutIni(cadenaMensaje[iAux1 + 3], cadenaMensaje[iAux1 + 2]);
			azimutInicial = azimutIni(cadenaMensaje[iAux1 + 9], cadenaMensaje[iAux1 + 8]);
//			 System.out.println("Procesando pie SCAAAAAAAAAAAAAAAAAAAAAAANNN=="+
//			 azimutInicial);

			while ((iPosInicial < iTamMensaje) && (i_iMatriz < iTamPie))// <128 <256 <512
			{
				
				//System.out.println("Exception-----------------------");
				//System.out.println("i_iMatriz == " + i_iMatriz);
				//System.out.println("iPosInicial == " + iPosInicial);
				//System.out.println("iPosFinal == " + iPosFinal);
				//System.out.println("iTamIntervalo == " + iTamIntervalo);
				
				iPosInicial = buscaAZ(cadenaMensaje, iPosInicial, iTamMensaje);
				iPosFinal = buscaAZ(cadenaMensaje, iPosInicial + 4, iTamMensaje);

				iTamIntervalo = obtenerIntervaloAZ(iPosInicial + 4, iPosFinal);

				try {
					bArregloAux = new byte[iTamIntervalo];
				} catch (Exception ex) {
					System.out.println("Exception-----------------------");
					System.out.println("i_iMatriz == " + i_iMatriz);
					System.out.println("iPosInicial == " + iPosInicial);
					System.out.println("iPosFinal == " + iPosFinal);
					System.out.println("iTamIntervalo == " + iTamIntervalo);
					System.out.println("Exception");
					ex.printStackTrace();
				}
				bArregloAux = obtenerValores(cadenaMensaje, iPosInicial + 4, iPosFinal, iTamIntervalo);
//				if ((iPosFinal + 2 < iTamMensaje) && (iPosFinal + 3 < iTamMensaje)) {
//				System.out.println(
//				azimutIni(cadenaMensaje[iPosFinal + 3], cadenaMensaje[iPosFinal + 2]));
//				}

				System.out.println("K "+ i_iMatriz);
				i_iMatriz = meterArregloMatriz(matrizPuntosPie, bArregloAux, i_iMatriz, iTamIntervalo);

				iPosInicial = iPosFinal;

			} // Fin while +++++++++++++++++++++++++
		} // FIN IF
		else {
			// _arregloPiesScanConv.setMensajeArregloFpga(_arregloPiesScanConv.getAuxByte(),iPosicionPie);
			/*
			 * for (int i = 0; i < 128; i++) { for (int j = 0; j < 4096; j++) {
			 * matrizPuntosPie[i][j]=0; } }
			 */
		} // fin else

		iPosicionPie++;
		if (iPosicionPie >= pies)
			iPosicionPie = 0;

		bArregloAux = null;
		cadenaMensaje = null;
	}// Fin obtenerMatriz

	public void sobelHMatriz() {
		int[][] matrizAuxiliar = null;
		matrizAuxiliar = new int[128][4096];

		for (int a = 0; a < 128; a++) {
			for (int b = 0; b < 4096; b++) {
				matrizAuxiliar[a][b] = 0;
			}
		}

		int[][] mascara = null;
		mascara = new int[3][3];
		mascara[0][0] = 1;
		mascara[0][1] = 0;
		mascara[0][2] = 1;
		mascara[1][0] = 0;
		mascara[1][1] = -4;
		mascara[1][2] = 0;
		mascara[2][0] = 1;
		mascara[2][1] = 0;
		mascara[2][2] = 1;

		for (int i = 1; i < 128 - 1; i++) {
			for (int j = 1; j < 4096 - 1; j++) {
				matrizAuxiliar[i][j] = (matrizPuntosPie[i - 1][j - 1] * mascara[0][0]
						+ matrizPuntosPie[i - 1][j] * mascara[0][1] + matrizPuntosPie[i - 1][j + 1] * mascara[0][2]
								+ matrizPuntosPie[i][j - 1] * mascara[1][0] + matrizPuntosPie[i][j] * mascara[1][1]
										+ matrizPuntosPie[i][j + 1] * mascara[1][2] + matrizPuntosPie[i + 1][j - 1] * mascara[2][0]
												+ matrizPuntosPie[i + 1][j] * mascara[2][1] + matrizPuntosPie[i + 1][j + 1] * mascara[2][2])
						/ 9;

				if (matrizAuxiliar[i][j] > 0)
					matrizAuxiliar[i][j] = matrizPuntosPie[i][j];
			}
		}

		for (int a = 0; a < 128; a++) {
			for (int b = 0; b < 4096; b++) {
				matrizPuntosPie[a][b] = matrizAuxiliar[a][b];
			}
		}
	}

	public void erosionMatriz(int opcion) {

		int[][] matrizAuxiliar = null;
		matrizAuxiliar = new int[128][4096];

		for (int a = 0; a < 128; a++) {
			for (int b = 0; b < 4096; b++) {
				matrizAuxiliar[a][b] = 0;
			}
		}

		int mascara1[] = { 1, 1, 1 };

		int mascara2[][] = { { 1, 1 }, { 1, 1 } };

		int mascara3[][] = { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } };

		switch (opcion) {
		case 1:
			for (int i = 0; i < 128 - 1; i++) {
				for (int j = 1; j < 4096 - 1; j++) {
					matrizAuxiliar[i][j] = (matrizPuntosPie[i][j - 1] * mascara1[0] * matrizPuntosPie[i][j]
							* mascara1[1] * matrizPuntosPie[i][j + 1] * mascara1[2]);

					if (matrizAuxiliar[i][j] > 0)
						matrizAuxiliar[i][j] = matrizPuntosPie[i][j];
				}
			}
			break;
		case 2:
			for (int i = 1; i < 128 - 1; i++) {
				for (int j = 1; j < 4096 - 1; j++) {
					matrizAuxiliar[i][j] = (matrizPuntosPie[i - 1][j - 1] * mascara2[0][0] * matrizPuntosPie[i - 1][j]
							* mascara2[0][1] * matrizPuntosPie[i][j - 1] * mascara2[1][0] * matrizPuntosPie[i][j]
									* mascara2[1][1]);

					if (matrizAuxiliar[i][j] > 0)
						matrizAuxiliar[i][j] = matrizPuntosPie[i][j];
				}
			}
			break;
		case 3:
			for (int i = 1; i < 128 - 1; i++) {
				for (int j = 1; j < 4096 - 1; j++) {
					matrizAuxiliar[i][j] = (matrizPuntosPie[i - 1][j - 1] * mascara3[0][0] * matrizPuntosPie[i - 1][j]
							* mascara3[0][1] * matrizPuntosPie[i - 1][j + 1] * mascara3[0][2]
									* matrizPuntosPie[i][j - 1] * mascara3[1][0] * matrizPuntosPie[i][j] * mascara3[1][1]
											* matrizPuntosPie[i][j + 1] * mascara3[1][2] * matrizPuntosPie[i + 1][j - 1]
													* mascara3[2][0] * matrizPuntosPie[i + 1][j] * mascara3[2][1]
															* matrizPuntosPie[i + 1][j + 1] * mascara3[2][2]);

					if (matrizAuxiliar[i][j] > 0)
						matrizAuxiliar[i][j] = matrizPuntosPie[i][j];
				}
			}
			break;
		default:
			break;
		}

		for (int a = 0; a < 128; a++) {
			for (int b = 0; b < 4096; b++) {
				matrizPuntosPie[a][b] = matrizAuxiliar[a][b];
			}
		}

	}

	public void dilatacionMatriz(int opcion) {

		int[][] matrizAuxiliar = null;
		matrizAuxiliar = new int[128][4096];

		for (int a = 0; a < 128; a++) {
			for (int b = 0; b < 4096; b++) {
				matrizAuxiliar[a][b] = 0;
			}
		}

		int mascara3[][] = { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } };

		int mascara5[][] = { { 0, 0, 1, 0, 0 }, { 0, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 0 },
				{ 0, 0, 1, 0, 0 } };

		int mascara7[][] = { { 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 0, 0 }, { 0, 1, 1, 1, 1, 1, 0 },
				{ 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 0 }, { 0, 0, 1, 1, 1, 0, 0 }, { 0, 0, 0, 1, 0, 0, 0 } };

		int mascara9[][] = { { 0, 0, 0, 0, 1, 0, 0, 0, 0 }, { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
				{ 0, 0, 1, 1, 1, 1, 1, 0, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				{ 0, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 0, 1, 1, 1, 1, 1, 0, 0 }, { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
				{ 0, 0, 0, 0, 1, 0, 0, 0, 0 } };

		switch (opcion) {
		case 1:
			for (int i = 1; i < 128 - 1; i++) {
				for (int j = 1; j < 4096 - 1; j++) {
					matrizAuxiliar[i][j] = (matrizPuntosPie[i - 1][j - 1] * mascara3[0][0]
							+ matrizPuntosPie[i - 1][j] * mascara3[0][1]
									+ matrizPuntosPie[i - 1][j + 1] * mascara3[0][2]
											+ matrizPuntosPie[i][j - 1] * mascara3[1][0] + matrizPuntosPie[i][j] * mascara3[1][1]
													+ matrizPuntosPie[i][j + 1] * mascara3[1][2]
															+ matrizPuntosPie[i + 1][j - 1] * mascara3[2][0]
																	+ matrizPuntosPie[i + 1][j] * mascara3[2][1]
																			+ matrizPuntosPie[i + 1][j + 1] * mascara3[2][2]);

					if (matrizAuxiliar[i][j] > 15)
						matrizAuxiliar[i][j] = 15;
				}
			}
			break;
		case 2:
			for (int i = 2; i < 128 - 2; i++) {
				for (int j = 2; j < 4096 - 2; j++) {
					matrizAuxiliar[i][j] = (matrizPuntosPie[i - 2][j - 2] * mascara5[0][0]
							+ matrizPuntosPie[i - 2][j - 1] * mascara5[0][1]
									+ matrizPuntosPie[i - 2][j] * mascara5[0][2]
											+ matrizPuntosPie[i - 2][j + 1] * mascara5[0][3]
													+ matrizPuntosPie[i - 2][j + 2] * mascara5[0][4]
															+ matrizPuntosPie[i - 1][j - 2] * mascara5[1][0]
																	+ matrizPuntosPie[i - 1][j - 1] * mascara5[1][1]
																			+ matrizPuntosPie[i - 1][j] * mascara5[1][2]
																					+ matrizPuntosPie[i - 1][j + 1] * mascara5[1][3]
																							+ matrizPuntosPie[i - 1][j + 2] * mascara5[1][4]
																									+ matrizPuntosPie[i][j - 2] * mascara5[2][0] + matrizPuntosPie[i][j - 1] * mascara5[2][1]
																											+ matrizPuntosPie[i][j] * mascara5[2][2] + matrizPuntosPie[i][j + 1] * mascara5[2][3]
																													+ matrizPuntosPie[i][j + 2] * mascara5[2][4]
																															+ matrizPuntosPie[i + 1][j - 2] * mascara5[3][0]
																																	+ matrizPuntosPie[i + 1][j - 1] * mascara5[3][1]
																																			+ matrizPuntosPie[i + 1][j] * mascara5[3][2]
																																					+ matrizPuntosPie[i + 1][j + 1] * mascara5[3][3]
																																							+ matrizPuntosPie[i + 1][j + 2] * mascara5[3][4]
																																									+ matrizPuntosPie[i + 2][j - 2] * mascara5[4][0]
																																											+ matrizPuntosPie[i + 2][j - 1] * mascara5[4][1]
																																													+ matrizPuntosPie[i + 2][j] * mascara5[4][2]
																																															+ matrizPuntosPie[i + 2][j + 1] * mascara5[4][3]
																																																	+ matrizPuntosPie[i + 2][j + 2] * mascara5[4][4]);

					if (matrizAuxiliar[i][j] > 15)
						matrizAuxiliar[i][j] = 15;
				}
			}
			break;
		case 3:
			for (int i = 3; i < 128 - 3; i++) {
				for (int j = 3; j < 4096 - 3; j++) {
					matrizAuxiliar[i][j] = (matrizPuntosPie[i - 3][j - 3] * mascara7[0][0]
							+ matrizPuntosPie[i - 3][j - 2] * mascara7[0][1]
									+ matrizPuntosPie[i - 3][j - 1] * mascara7[0][2]
											+ matrizPuntosPie[i - 3][j] * mascara7[0][3]
													+ matrizPuntosPie[i - 3][j + 1] * mascara7[0][4]
															+ matrizPuntosPie[i - 3][j + 2] * mascara7[0][5]
																	+ matrizPuntosPie[i - 3][j + 3] * mascara7[0][6]
																			+ matrizPuntosPie[i - 2][j - 3] * mascara7[1][0]
																					+ matrizPuntosPie[i - 2][j - 2] * mascara7[1][1]
																							+ matrizPuntosPie[i - 2][j - 1] * mascara7[1][2]
																									+ matrizPuntosPie[i - 2][j] * mascara7[1][3]
																											+ matrizPuntosPie[i - 2][j + 1] * mascara7[1][4]
																													+ matrizPuntosPie[i - 2][j + 2] * mascara7[1][5]
																															+ matrizPuntosPie[i - 2][j + 3] * mascara7[1][6]
																																	+ matrizPuntosPie[i - 1][j - 3] * mascara7[2][0]
																																			+ matrizPuntosPie[i - 1][j - 2] * mascara7[2][1]
																																					+ matrizPuntosPie[i - 1][j - 1] * mascara7[2][2]
																																							+ matrizPuntosPie[i - 1][j] * mascara7[2][3]
																																									+ matrizPuntosPie[i - 1][j + 1] * mascara7[2][4]
																																											+ matrizPuntosPie[i - 1][j + 2] * mascara7[2][5]
																																													+ matrizPuntosPie[i - 1][j + 3] * mascara7[2][6]
																																															+ matrizPuntosPie[i][j - 3] * mascara7[3][0] + matrizPuntosPie[i][j - 2] * mascara7[3][1]
																																																	+ matrizPuntosPie[i][j - 1] * mascara7[3][2] + matrizPuntosPie[i][j] * mascara7[3][3]
																																																			+ matrizPuntosPie[i][j + 1] * mascara7[3][4] + matrizPuntosPie[i][j + 2] * mascara7[3][5]
																																																					+ matrizPuntosPie[i][j + 3] * mascara7[3][6]
																																																							+ matrizPuntosPie[i + 1][j - 3] * mascara7[4][0]
																																																									+ matrizPuntosPie[i + 1][j - 2] * mascara7[4][1]
																																																											+ matrizPuntosPie[i + 1][j - 1] * mascara7[4][2]
																																																													+ matrizPuntosPie[i + 1][j] * mascara7[4][3]
																																																															+ matrizPuntosPie[i + 1][j + 1] * mascara7[4][4]
																																																																	+ matrizPuntosPie[i + 1][j + 2] * mascara7[4][5]
																																																																			+ matrizPuntosPie[i + 1][j + 3] * mascara7[4][6]
																																																																					+ matrizPuntosPie[i + 2][j - 3] * mascara7[5][0]
																																																																							+ matrizPuntosPie[i + 2][j - 2] * mascara7[5][1]
																																																																									+ matrizPuntosPie[i + 2][j - 1] * mascara7[5][2]
																																																																											+ matrizPuntosPie[i + 2][j] * mascara7[5][3]
																																																																													+ matrizPuntosPie[i + 2][j + 1] * mascara7[5][4]
																																																																															+ matrizPuntosPie[i + 2][j + 2] * mascara7[5][5]
																																																																																	+ matrizPuntosPie[i + 2][j + 3] * mascara7[5][6]
																																																																																			+ matrizPuntosPie[i + 3][j - 3] * mascara7[6][0]
																																																																																					+ matrizPuntosPie[i + 3][j - 2] * mascara7[6][1]
																																																																																							+ matrizPuntosPie[i + 3][j - 1] * mascara7[6][2]
																																																																																									+ matrizPuntosPie[i + 3][j] * mascara7[6][3]
																																																																																											+ matrizPuntosPie[i + 3][j + 1] * mascara7[6][4]
																																																																																													+ matrizPuntosPie[i + 3][j + 2] * mascara7[6][5]
																																																																																															+ matrizPuntosPie[i + 3][j + 3] * mascara7[6][6]);

					if (matrizAuxiliar[i][j] > 15)
						matrizAuxiliar[i][j] = 15;
				}
			}
			break;
		case 4:
			for (int i = 4; i < 128 - 4; i++) {
				for (int j = 4; j < 4096 - 4; j++) {
					matrizAuxiliar[i][j] = (matrizPuntosPie[i - 4][j - 4] * mascara9[0][0]
							+ matrizPuntosPie[i - 4][j - 3] * mascara9[0][1]
									+ matrizPuntosPie[i - 4][j - 2] * mascara9[0][2]
											+ matrizPuntosPie[i - 4][j - 1] * mascara9[0][3]
													+ matrizPuntosPie[i - 4][j] * mascara9[0][4]
															+ matrizPuntosPie[i - 4][j + 1] * mascara9[0][5]
																	+ matrizPuntosPie[i - 4][j + 2] * mascara9[0][6]
																			+ matrizPuntosPie[i - 4][j + 3] * mascara9[0][7]
																					+ matrizPuntosPie[i - 4][j + 4] * mascara9[0][8]
																							+ matrizPuntosPie[i - 3][j - 4] * mascara9[1][0]
																									+ matrizPuntosPie[i - 3][j - 3] * mascara9[1][1]
																											+ matrizPuntosPie[i - 3][j - 2] * mascara9[1][2]
																													+ matrizPuntosPie[i - 3][j - 1] * mascara9[1][3]
																															+ matrizPuntosPie[i - 3][j] * mascara9[1][4]
																																	+ matrizPuntosPie[i - 3][j + 1] * mascara9[1][5]
																																			+ matrizPuntosPie[i - 3][j + 2] * mascara9[1][6]
																																					+ matrizPuntosPie[i - 3][j + 3] * mascara9[1][7]
																																							+ matrizPuntosPie[i - 3][j + 4] * mascara9[1][8]
																																									+ matrizPuntosPie[i - 3][j - 4] * mascara9[2][0]
																																											+ matrizPuntosPie[i - 2][j - 3] * mascara9[2][1]
																																													+ matrizPuntosPie[i - 2][j - 2] * mascara9[2][2]
																																															+ matrizPuntosPie[i - 2][j - 1] * mascara9[2][3]
																																																	+ matrizPuntosPie[i - 2][j] * mascara9[2][4]
																																																			+ matrizPuntosPie[i - 2][j + 1] * mascara9[2][5]
																																																					+ matrizPuntosPie[i - 2][j + 2] * mascara9[2][6]
																																																							+ matrizPuntosPie[i - 2][j + 3] * mascara9[2][7]
																																																									+ matrizPuntosPie[i - 2][j + 4] * mascara9[2][8]
																																																											+ matrizPuntosPie[i - 1][j - 4] * mascara9[3][0]
																																																													+ matrizPuntosPie[i - 1][j - 3] * mascara9[3][1]
																																																															+ matrizPuntosPie[i - 1][j - 2] * mascara9[3][2]
																																																																	+ matrizPuntosPie[i - 1][j - 1] * mascara9[3][3]
																																																																			+ matrizPuntosPie[i - 1][j] * mascara9[3][4]
																																																																					+ matrizPuntosPie[i - 1][j + 1] * mascara9[3][5]
																																																																							+ matrizPuntosPie[i - 1][j + 2] * mascara9[3][6]
																																																																									+ matrizPuntosPie[i - 1][j + 3] * mascara9[3][7]
																																																																											+ matrizPuntosPie[i - 1][j + 4] * mascara9[3][8]
																																																																													+ matrizPuntosPie[i][j - 4] * mascara9[4][0] + matrizPuntosPie[i][j - 3] * mascara9[4][1]
																																																																															+ matrizPuntosPie[i][j - 2] * mascara9[4][2] + matrizPuntosPie[i][j - 1] * mascara9[4][3]
																																																																																	+ matrizPuntosPie[i][j] * mascara9[4][4] + matrizPuntosPie[i][j + 1] * mascara9[4][5]
																																																																																			+ matrizPuntosPie[i][j + 2] * mascara9[4][6] + matrizPuntosPie[i][j + 3] * mascara9[4][7]
																																																																																					+ matrizPuntosPie[i][j + 4] * mascara9[4][8]
																																																																																							+ matrizPuntosPie[i + 1][j - 4] * mascara9[5][0]
																																																																																									+ matrizPuntosPie[i + 1][j - 3] * mascara9[5][1]
																																																																																											+ matrizPuntosPie[i + 1][j - 2] * mascara9[5][2]
																																																																																													+ matrizPuntosPie[i + 1][j - 1] * mascara9[5][3]
																																																																																															+ matrizPuntosPie[i + 1][j] * mascara9[5][4]
																																																																																																	+ matrizPuntosPie[i + 1][j + 1] * mascara9[5][5]
																																																																																																			+ matrizPuntosPie[i + 1][j + 2] * mascara9[5][6]
																																																																																																					+ matrizPuntosPie[i + 1][j + 3] * mascara9[5][7]
																																																																																																							+ matrizPuntosPie[i + 1][j + 4] * mascara9[5][8]
																																																																																																									+ matrizPuntosPie[i + 2][j - 4] * mascara9[6][0]
																																																																																																											+ matrizPuntosPie[i + 2][j - 3] * mascara9[6][1]
																																																																																																													+ matrizPuntosPie[i + 2][j - 2] * mascara9[6][2]
																																																																																																															+ matrizPuntosPie[i + 2][j - 1] * mascara9[6][3]
																																																																																																																	+ matrizPuntosPie[i + 2][j] * mascara9[6][4]
																																																																																																																			+ matrizPuntosPie[i + 2][j + 1] * mascara9[6][5]
																																																																																																																					+ matrizPuntosPie[i + 2][j + 2] * mascara9[6][6]
																																																																																																																							+ matrizPuntosPie[i + 2][j + 3] * mascara9[6][7]
																																																																																																																									+ matrizPuntosPie[i + 2][j + 4] * mascara9[6][8]
																																																																																																																											+ matrizPuntosPie[i + 3][j - 4] * mascara9[7][0]
																																																																																																																													+ matrizPuntosPie[i + 3][j - 3] * mascara9[7][1]
																																																																																																																															+ matrizPuntosPie[i + 3][j - 2] * mascara9[7][2]
																																																																																																																																	+ matrizPuntosPie[i + 3][j - 1] * mascara9[7][3]
																																																																																																																																			+ matrizPuntosPie[i + 3][j] * mascara9[7][4]
																																																																																																																																					+ matrizPuntosPie[i + 3][j + 1] * mascara9[7][5]
																																																																																																																																							+ matrizPuntosPie[i + 3][j + 2] * mascara9[7][6]
																																																																																																																																									+ matrizPuntosPie[i + 3][j + 3] * mascara9[7][7]
																																																																																																																																											+ matrizPuntosPie[i + 3][j + 4] * mascara9[7][8]
																																																																																																																																													+ matrizPuntosPie[i + 4][j - 4] * mascara9[8][0]
																																																																																																																																															+ matrizPuntosPie[i + 4][j - 3] * mascara9[8][1]
																																																																																																																																																	+ matrizPuntosPie[i + 4][j - 2] * mascara9[8][2]
																																																																																																																																																			+ matrizPuntosPie[i + 4][j - 1] * mascara9[8][3]
																																																																																																																																																					+ matrizPuntosPie[i + 4][j] * mascara9[8][4]
																																																																																																																																																							+ matrizPuntosPie[i + 4][j + 1] * mascara9[8][5]
																																																																																																																																																									+ matrizPuntosPie[i + 4][j + 2] * mascara9[8][6]
																																																																																																																																																											+ matrizPuntosPie[i + 4][j + 3] * mascara9[8][7]
																																																																																																																																																													+ matrizPuntosPie[i + 4][j + 4] * mascara9[8][8]);

					if (matrizAuxiliar[i][j] > 15)
						matrizAuxiliar[i][j] = 15;
				}
			}
			break;
		default:
			break;
		}

		for (int a = 0; a < 128; a++) {
			for (int b = 0; b < 4096; b++) {
				matrizPuntosPie[a][b] = matrizAuxiliar[a][b];
			}
		}

	}

	public void pasarMatrizScanConv() {
		_arregloMatricesScanConv.setMatrizMostrar(matrizScanConverter, iPosicionScanConv);

		iPosicionScanConv++;
		if (iPosicionScanConv >= pies) {
			iPosicionScanConv = 0;

			recolectarBasura();

		}

	}


	public void procesoObtenerScan() throws InterruptedException {

		obtenerMatriz();

		pasarMatrizScanConv();
		ScanConverter2();

		//recolectarBasura();
	}

	public void recolectarBasura() {
		Runtime garbage = Runtime.getRuntime();
		garbage.gc();
	}

}