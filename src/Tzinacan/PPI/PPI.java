package Tzinacan.PPI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;//transform 2D, asignacion lineal de coordenadas 2D, conservando rectitud y paralelismo
import java.awt.image.BufferedImage; //bufer accesible a datos de un img
import java.util.Timer;//se podria utilizar para ejecutar un hilo cada sierto tiempo
import java.util.TimerTask;

//        "joamp" libreria for 3D graphics, media and processing
import com.jogamp.opengl.util.awt.TextureRenderer;

import Tzinacan.Visualizador;
import Tzinacan.PPI.ArregloMatrices.ArregloMatrices;
import gov.nasa.worldwind.render.DrawContext; //worldwind api open source
import gov.nasa.worldwind.render.Renderable;


public class PPI implements Renderable{

	Visualizador objSim = null;
	int contadorFrames=0;
	ArregloMatrices _arregloMatrizPpi = null;
	public BufferedImage buffRenderImage2 = null;
	BufferedImage buffRenderImage3 = null;
	private int iOpcionPintado; 
	private int iPosicionPiePintar;
	public double[][] matrizPpiPintar = null;
	public int[][] matrizPpiPintarCeros = null;

	BufferedImage buffRenderImage = null; 
	Graphics2D flatGraphic = null; 
//Graphics2D; abstract class; proporciona un control d geometría, las transformaciones de coordenadas, la gestión del color y el diseño del texto. fundamental para representar formas, texto e imágenes bidimensionales
	
	int posX;
	int posY;
	int contInc;

	double rangeBeamCustomPPi;
	int colorCustomPPi;
	Color c;


	TextureRenderer textureRenderer = null;
	
	//Nuevo codigo Licea
	BufferedImage buffRenderKalman = null;
	AffineTransform tx;
	
	public PPI()//constructor
	{
		
		posX = 400;
		posY = 200;
		contInc = 0;

		iPosicionPiePintar = 0;
		matrizPpiPintar = new double[1000][1000];
		matrizPpiPintarCeros = new int[1000][1000];

		for (int i = 0; i < 1000; i++) 
		{
			for (int j = 0; j < 1000; j++) 
			{
				matrizPpiPintar[i][j]=0;
				matrizPpiPintarCeros[i][j]=0;
			}//porque las llena de 0's?
		}
		//Este valor es el Azimuth Reset Pulse?
		//4096 pulsos por 360 grados
		rangeBeamCustomPPi = 4096.0;//4086.0
		colorCustomPPi = 2;

	}

	public void setObjSim(Visualizador s)//recibe el objeto Visualizador
	{
		objSim = s; //le asigna los parametros para visualizar??
	}

	public void setOpcionPintado( int op)
	{
		iOpcionPintado = op;//numero que indica...
	}
	
	public int getOpcionPintado() {//obtiene el valor de la opcion
		return this.iOpcionPintado;
	}

	public void setArregloMatrices(ArregloMatrices arr)//recibe el parametro arr de la class ArregloMatrices
	{
		_arregloMatrizPpi = arr;
	}

	public ArregloMatrices getArregloMatrices()
	{//metd, retorma un obj type ArregloMatrices
		return _arregloMatrizPpi;
	}
//cuando se usan estos metodos?
	public void incX()
	{
		posX++;
		if(posX > 900)
			posX = 400;
	}

	public void incY()
	{
		posY++;
		if(posY > 900)
			posY = 200;
	}
// getyset del rango del haz ppi
	public void setRangeBeansCustomPPi(double val)
	{
		rangeBeamCustomPPi = val;
	}

	public double getRangeBeansCustomPPi()
	{
		return rangeBeamCustomPPi;
	}

//getyset del color personalizado del ppi
	public void setColorPPI(int val)
	{
		colorCustomPPi = val;
	}

	public int getColorPPI()
	{
		return	colorCustomPPi;
	}

	
	//metodo retorna un obj tipo BufferedImage
	public BufferedImage writeImage() 
	{
		//esto es un ?...
		buffRenderImage = new BufferedImage(1300, 1000, BufferedImage.TYPE_INT_ARGB);
										// width, height, imageType
		flatGraphic = buffRenderImage.createGraphics(); //Creates a Graphics2D, which can be used to draw into this BufferedImage.
//se crea un objimg con 1300,1000, despues al obj flatGraphic se le asigna dicho objetoimg creado
		
		//retorna un valor int, 
		matrizPpiPintar = _arregloMatrizPpi.getMatrizMostrar(iPosicionPiePintar);
		iPosicionPiePintar++;
		//System.out.println("iPosicionPiePintar===="+"msg"+iPosicionPiePintar);//comentar
		if(iPosicionPiePintar>=512) //65536-524288 = 32 //1048576 = 16  -----------------8 Bits
//		if(iPosicionPiePintar>=16) //65536-262144 = 32 //524288 = 16 //1048576 = 8 -----4 Bits
			iPosicionPiePintar = 0;
		//en que situacion iPosicionPiePinrtar es mayor o igual que 512, y porque 512?
		Color rojo, verde, azul, amarillo, blanco;
		
		//???
		int coorX = (300/2);
		int coorY =(0/2);
		//???
		
		int valorColor = 0;
		
		for (int i=0; i< 1000; i++)
			for (int j=0; j< 1000; j++)
			{

				valorColor = (int) matrizPpiPintar[i][j];
//				valorColor = valorColor * 2;		//8 Bits
				valorColor = valorColor * 16 ; 		//4 bits

				if(valorColor > 255 )
					valorColor=255;
				
				c = new Color(0, valorColor, 0); //crea un color RGB  con los valores de rojo, verde y azul especificados en el rango (0 - 255).
//				c = new Color(0, 255, 0, valorColor);
				
				if(valorColor > 0 && valorColor < 256)
				{//se crean todos estos colores para poder pintar sobre la imagen?
					rojo = new Color(valorColor, 0, 0);
					verde = new Color(0, valorColor, 0);
//					verde = new Color(0, 255, 0, valorColor);
					azul = new Color(0, 0, valorColor);
					amarillo = new Color(valorColor, valorColor, 0);
					blanco = new Color(valorColor, valorColor, valorColor);					
					

					switch(colorCustomPPi)
					{
					case 1: c=rojo;	  break;
					case 2:	c=verde;  break;
					case 3:	c=azul;   break;
					case 4:	c=amarillo;	break;
					case 5:	c=blanco; break;
					default:c=verde;  break;
					}

					flatGraphic.setColor(c);//se establece el color seleccionado c, para usarlo posterior
					flatGraphic.fillRect(coorX+j,coorY+i, 1, 1);//rellena el area especificada del color actual de c.
				}//se pinta pixel a pixel, ya que recorre la matriz de 1000x1000
			}//como esta dentro del cilo se pintan todos los pixeles??
		
//		for(int i=0; i<1000; i++) {
//			c = new Color(0, 255, 0);
//			
//			flatGraphic.setColor(c);
//			flatGraphic.fillRect(coorX+i, i, 1, 1);
//		}


		// don't use drawn graphic anymore.
		flatGraphic.dispose();//libera el obj graphic

		return buffRenderImage;
	}

	
	
	//Api nasa
	public void render(DrawContext dc)
	{						    //constr int width, int heigth, boolean alpha
		textureRenderer = new TextureRenderer(1300, 1000, true); // Crea un nuevo renderizador con almacenamiento de respaldo del ancho y alto especificados
		Graphics2D  grafico = textureRenderer.createGraphics();
		//Al obj2D grafico se le asigna el objeto creado textureRedner 
		
		Font myFont = new Font("Courier New",1,50);

		//System.out.println("Pintando!!!!!!!!!!!!!!!!!!!CUBO");


		switch (iOpcionPintado) {
		case 1:	//NotReady
			grafico.setColor(Color.BLACK);
			grafico.fillRect(0, 0, 1300, 1000);
			grafico.setColor(Color.RED);
			grafico.setFont(myFont);
			grafico.drawString("----NOT READY, HELP PLS!----",205, 520);
			break;
		case 2:	//STDBY
			grafico.setColor(Color.BLACK);
			grafico.fillRect(0, 0, 1300, 1000);
			grafico.setFont(myFont);
			grafico.setColor(Color.GREEN);
			grafico.drawString("----STAND BY----",425, 520);
			break;
		case 3:	//RUN
			//			  grafico.drawString("Valor del Range Beam",10, 50);
			//			  grafico.drawString(String.valueOf(rangeBeamCustomPPi),10, 70);
			writeImage();
			grafico.drawImage(buffRenderImage, null, null);
			//pinta la img, con el formato de la img, 
			
			
//			buffRenderImage3 = writeImage2();
//			grafico.drawImage(buffRenderImage3, null, null);
			
			flatGraphic.dispose();
			buffRenderImage.flush();//??
			//buffRenderImage2.flush();
			break;
		}

		objSim.getMundo().redraw();//comunucacion con visualizador

										//Alto,ancho,bool disable (deben ser los mismos del diseño actual)
		textureRenderer.beginOrthoRendering(1300, 1000, true);//Metodo que representa partes de la textura Opengl,dibujando como superposicion plana en pantalla
						 //int screen x,int screen y
		textureRenderer.drawOrthoRect(0, 0);//Dibuja un rectángulo proyectado ortográficamente que contiene la textura subyacente en la ubicación especificada en la pantalla. Todas las coordenadas (x, y) se especifican en relación con la esquina inferior izquierda de la imagen de textura o del elemento de diseño OpenGL actual
					
		textureRenderer.endOrthoRendering();//representar partes de la textura OpenGL en la pantalla, si la aplicación tiene la intención de dibujarlas como una superposición plana en la pantalla.
		textureRenderer.dispose();//Elimina todos los recursos asociados con este renderizador

		incX();
		incY();

		grafico.dispose();
	}//fin render

	TimerTask timerTask = new TimerTask() 
	{ 
		public void run()  
		{ 

			//System.out.println("Ejecutando cada 66 milisegundos");
			matrizPpiPintar = _arregloMatrizPpi.getMatrizMostrar(iPosicionPiePintar);
        	//_arregloMatrizPpi.setMatrizMostrar(matrizPpiPintarCeros,iPosicionPiePintar);
        	iPosicionPiePintar++;
          	 if(iPosicionPiePintar>=512) //32
          		 iPosicionPiePintar = 0;
			//System.out.println("iPosicionPiePintar===="+iPosicionPiePintar);
			//simuladorActualizaDraw();
			//objSim.getMundo().redraw();

		} 
	}; 

	public void ejecutaTimer()
	{
		// Aquí se pone en marcha el timer cada segundo. 
		Timer timer = new Timer(); 
		// Dentro de 0 milisegundos avísame cada 1000 milisegundos 
		timer.scheduleAtFixedRate(timerTask, 0, 5);
	}

}