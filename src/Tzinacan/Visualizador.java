package Tzinacan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.zeromq.ZMQ;//biblioteca de mensajeria asincrona

import Tzinacan.PPI.PPI;
import Tzinacan.PPI.ArregloMatrices.ArregloMatrices;
import Tzinacan.PPI.ArregloPies.ArregloPies;
import Tzinacan.PPI.ScanConverter.ScanConverter;
import Tzinacan.PPI.ScanConverter.ThreadScanConverter;
import Tzinacan.PPI.ZMQ.AdapZmq;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;//componente para mostrar el worldWindModel
import gov.nasa.worldwind.geom.Angle;//representa el angulo geometrico, las instancias de angle son inmutables
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.EarthFlat;//Define un modelo de la Tierra proyectado sobre un plano
import gov.nasa.worldwind.globes.FlatGlobe;//define un globo representado como una proyección sobre un plano. El tipo de proyección es modificable
import gov.nasa.worldwind.layers.RenderableLayer;//administra una colección de objetos Renderable para su procesamiento, selección y eliminación
import gov.nasa.worldwindx.examples.util.LayerManagerLayer;//Muestra la lista de capas en una visualización frontal en la ventana gráfica

public class Visualizador extends JFrame {

	private WorldWindowGLCanvas Mundo;//mundo es la variable que almacena el grafico (golobo tierra)
	public PPI _myPpi;//obj tipo PPI class
	public ThreadScanConverter _threadScanConvSimulador;
	public RenderableLayer _MyLayerPpi;
	public ScanConverter _scanConv;
	public ArregloPies _arregloPies;
	public ArregloMatrices _arregloMatrices;
	public AdapZmq _adapRecibir;

	private JButton btnMTR, btnRun, btnStdby, btnCorto, btnMedio, btnLargo, btnSo, btnCa, btnGo, btnCefar, btnMu,
			btnMuestreo, btnVector;
	private JComboBox cbPulse, cbAZ, cbDACB;// cbDACA,cbDACB;
	private JSpinner jsGain, jsGanancia, jsTune, jsChannel, jsWeather, jsOBJ, jsCptVA;//label con flechas
	private JLabel jlGain, jlPulse, jltune, jlChanel, jlEtiqueta;

	private SpinnerModel value;//??
	
	// Add the layer manager layer to the model layer list
	LayerManagerLayer capaManejadoraCapas;

	// crea un modelo del mundo en un plano
	FlatGlobe flatGlobe = new EarthFlat();

	// representa el punto central y la elevacion actual a la que se encuentra el
	// mapa
	public Position puntoCentralMapa = null;

	// Variables para hacer la conexion con la aplicacion por medio del 0MQ
	ZMQ.Context contextoSocket0MQ;
	ZMQ.Socket socket0MQEnviaFPGA;//

	// variables iniciales que se leen del archivo de configuracion.xml

	static double LATITUD_INICIAL = 19.044177;
	static double LONGITUD_INICIAL = -95.971202;
	static double ALTURA_INICIAL = 21400;

	static double ALTURA_RADAR = 21400;

	static double ALTURA_64_Millas = 409600;
	static double ALTURA_32_Millas = 204800;
	static double ALTURA_16_Millas = 102400;
	static double ALTURA_8_Millas = 51200;
	static double ALTURA_4_Millas = 25600;
	static double ALTURA_2_Millas = 12800;
	static double ALTURA_1_Millas = 6400;
	static double ALTURA_05_Millas = 3200;
	static double ALTURA_025_Millas = 1600;
	static double ALTURA_0125_Millas = 800;

	// double alturaActual = 0;

	public Visualizador() {//constr

		CreaAjustaMundo();
		AjustaPropiedadesVentana();
		CreaConexionesZeroMQ();

		//se crean los sig instancias
		_adapRecibir = new AdapZmq();
		_arregloPies = new ArregloPies();
		_scanConv = new ScanConverter(this);//this hace referencia a el obj de tipo Visualizador
		_threadScanConvSimulador = new ThreadScanConverter();
		_arregloMatrices = new ArregloMatrices();
		
		
		_adapRecibir.setPiesAdapZmq(_arregloPies);//le pasa el obj ArregloPies

		_scanConv.setArregloPiesScanConv(_arregloPies);
		_scanConv.setArregloMatricesScanConv(_arregloMatrices);
		_threadScanConvSimulador.setScanConverterToHilo(_scanConv);//se le pasa el obj scanConv
//se inician los hilos
		_adapRecibir.start();
		_threadScanConvSimulador.start();

		_myPpi = new PPI();
		_myPpi.setObjSim(this);//se usa como parametro el visualizador
		_MyLayerPpi = new RenderableLayer();
		_myPpi.setArregloMatrices(_arregloMatrices);
		 _myPpi.ejecutaTimer();//comentttt
		_MyLayerPpi.setName("PPI");
		_MyLayerPpi.addRenderable(_myPpi);
		Mundo.getModel().getLayers().add(_MyLayerPpi);
		Mundo.redraw();

		_myPpi.setOpcionPintado(1);

		capaManejadoraCapas = new LayerManagerLayer(Mundo);

		AgregaListenerTeclado();
		AgregaPanelBotones();
		AgregaListeners();

		// Mundo en 2D
		Mundo.getModel().setGlobe(flatGlobe);
		Mundo.getView().setPitch(Angle.fromDegrees(0));

		puntoCentralMapa = new Position(Angle.fromDegrees(LATITUD_INICIAL), Angle.fromDegrees(LONGITUD_INICIAL),
				ALTURA_INICIAL);

		Mundo.getView().setEyePosition(puntoCentralMapa);

	}

	private void AgregaListeners() {

		// Accion a realizar cuando el botón MTR
		btnMTR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sCmd = "MT01";

				System.out.println("Click en botón MTR");
				enviaComandoFPGA(sCmd);

			}
		});

		// Accion a realizar cuando el botón RUN
		btnRun.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String sCmd = "TX01";
				System.out.println("Click en botón RUN");
				_myPpi.setOpcionPintado(3);
				enviaComandoFPGA(sCmd);

			}
		});

		// Accion a realizar cuando el botón STDBY
		btnStdby.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String sCmd = "TX02";

				System.out.println("Click en botón STDBY");
				_myPpi.setOpcionPintado(3);
				enviaComandoFPGA(sCmd);
			}
		});

		// Accion a realizar cuando el cambio de Pulso/Rango PL01
		cbPulse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sCmd = "Rn";

//				System.out.println(cbPulse.getSelectedItem().toString());
				System.out.println("Click en combo Pulso " + cbPulse.getSelectedIndex());
				Alcance(cbPulse.getSelectedIndex());

				sCmd += cbPulse.getSelectedIndex();

				enviaComandoFPGA(sCmd);

			}
		});
		// Accion a realizar cuando el cambio de Ganancia Cl01
		jsGain.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				String sCmd = "Ga";
				System.out.println("GAIN " + jsGain.getValue().toString());
				sCmd += jsGain.getValue().toString();
				enviaComandoFPGA(sCmd);
			}
		});
		// Accion a realizar cuando el cambio de level Cfar Cl01
		jsGanancia.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				String sCmd = "Cl";
				System.out.println("GAIN " + jsGanancia.getValue().toString());
				sCmd += jsGanancia.getValue().toString();
				enviaComandoFPGA(sCmd);
			}
		});
		// Accion a realizar cuando el cambio de Valores del CPT
		jsCptVA.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				String sCmd = "CA";
				System.out.println("CPTVA " + jsCptVA.getValue().toString());
				sCmd += jsCptVA.getValue().toString();
				enviaComandoFPGA(sCmd);
			}
		});

		// Accion a realizar cuando el botón CA CfCA
		btnCa.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String sCmd = "Cf01";

				System.out.println("CFAR CA");
				enviaComandoFPGA(sCmd);
			}
		});
		// Accion a realizar cuando el botón SO CfSO
		btnSo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String sCmd = "Cf02";
				System.out.println("CFAR GO");
				enviaComandoFPGA(sCmd);
			}
		});
		// Accion a realizar cuando el botón GO CfGO
		btnGo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String sCmd = "Cf03";
				System.out.println("CFAR SO");
				enviaComandoFPGA(sCmd);
			}
		});
		// Accion a realizar cuando el Selector de AZ simulado o Encoder
		cbAZ.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sCmd = "As";

//						System.out.println(cbPulse.getSelectedItem().toString());
				System.out.println("Click en combo Pulso " + cbAZ.getSelectedIndex());
				Alcance(cbAZ.getSelectedIndex());

				sCmd += cbAZ.getSelectedIndex();

				enviaComandoFPGA(sCmd);

			}
		});
//		// Accion a realizar cuando el cambio de Señal de DAC A
//		cbDACA.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				String sCmd="St";
//
////				System.out.println(cbPulse.getSelectedItem().toString());
//				System.out.println("Click en combo Pulso " +cbDACA.getSelectedIndex());
//				Alcance(cbDACA.getSelectedIndex());
//				
//				sCmd+=cbDACA.getSelectedIndex();
//
//				enviaComandoFPGA(sCmd);
//
//			}
//		});
		// Accion a realizar cuando el cambio de Señal de DAC B
		cbDACB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sCmd = "Ft";

//						System.out.println(cbPulse.getSelectedItem().toString());
				System.out.println("Click en combo Pulso " + cbDACB.getSelectedIndex());
				Alcance(cbDACB.getSelectedIndex());

				sCmd += cbDACB.getSelectedIndex();

				enviaComandoFPGA(sCmd);

			}
		});

		// Accion a realizar cuando el cambio de Weather
		jsWeather.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				String sCmd = "WT";
				System.out.println("WEATHER " + jsWeather.getValue().toString());
				sCmd += jsWeather.getValue().toString();
				enviaComandoFPGA(sCmd);
			}
		});

		// Accion a realizar cuando el cambio de Tune TN01
		jsTune.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				String sCmd = "TN";
				System.out.println("TUNE  " + jsTune.getValue().toString());
				sCmd += jsTune.getValue().toString();
				enviaComandoFPGA(sCmd);
			}
		});

		// Accion a realizar cuando el cambio de Cannel CH01
		jsChannel.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				String sCmd = "CH";
				System.out.println("CHANNEL " + jsChannel.getValue().toString());
				sCmd += jsChannel.getValue().toString();
				enviaComandoFPGA(sCmd);

			}
		});

		// Accion a realizar cuando el cambio de Velocidad de Objetivos OB01
		jsOBJ.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				String sCmd = "OB";
				System.out.println("OBJ " + jsOBJ.getValue().toString());
				sCmd += jsOBJ.getValue().toString();
				enviaComandoFPGA(sCmd);

			}
		});

		// Accion a realizar cuando el botón MUTE MT01
		// Accion a realizar cuando el botón STC St01
		// Accion a realizar cuando el botón FTC Ft01

	}

	private JButton crearBoton(String nombre) {
		JButton boton = new JButton(nombre);
		boton.setPreferredSize(new Dimension(150, 30));
		boton.setFont(new Font("Arial", Font.BOLD, 18));
		return boton;
	}

	private JLabel crearLabel(String nombre) {
		JLabel label = new JLabel(nombre);
		label.setPreferredSize(new Dimension(150, 30));
		label.setFont(new Font("Arial", Font.BOLD, 18));

		label.setForeground(Color.red);
		label.setBackground(Color.lightGray);

		return label;
	}

	private void AgregaPanelBotones() {
		JPanel selectionPanel = new JPanel();
		{
			JPanel gridPanel = new JPanel(new GridLayout(0, 1, 10, 10)); // rows, cols, hgap, vgap

			btnMTR = crearBoton("MTR");
			gridPanel.add(btnMTR);

			btnRun = crearBoton("RUN");
			gridPanel.add(btnRun);
			btnStdby = crearBoton("STDBY");
			gridPanel.add(btnStdby);

			jlEtiqueta = crearLabel("RANGE");
			gridPanel.add(jlEtiqueta);
			cbPulse = new JComboBox();
			cbPulse.addItem("0.125MN");
			cbPulse.addItem("0.25 MN");
			cbPulse.addItem("0.5  MN");
//			cbPulse.addItem("0.75 MN");
			cbPulse.addItem("  1  MN");
//			cbPulse.addItem("1.5  MN");
			cbPulse.addItem("  2  MN");
//			cbPulse.addItem("  3  MN");
			cbPulse.addItem("  4  MN");
//			cbPulse.addItem("  6  MN");
			cbPulse.addItem("  8  MN");
//			cbPulse.addItem(" 12  MN");
			cbPulse.addItem(" 16  MN");
//			cbPulse.addItem(" 24  MN");
			cbPulse.addItem(" 32  MN");
			cbPulse.addItem(" 64  MN");
			gridPanel.add(cbPulse);

			jlEtiqueta = crearLabel("CFAR");
			gridPanel.add(jlEtiqueta);
			btnCa = crearBoton("CA");
			gridPanel.add(btnCa);
			btnSo = crearBoton("GO");
			gridPanel.add(btnSo);
			btnGo = crearBoton("SO");
			gridPanel.add(btnGo);

			jlEtiqueta = crearLabel("GAIN");
			gridPanel.add(jlEtiqueta);
			value = new SpinnerNumberModel(0, 0, 127, 1); // initial value, minimum value,maximum value,step
			jsGain = new JSpinner(value);
			gridPanel.add(jsGain);

			jlEtiqueta = crearLabel("LEVEL");
			gridPanel.add(jlEtiqueta);
			value = new SpinnerNumberModel(0, 0, 254, 1); // initial value, minimum value,maximum value,step
			jsGanancia = new JSpinner(value);
			gridPanel.add(jsGanancia);

			jlEtiqueta = crearLabel("CPT VAL");
			gridPanel.add(jlEtiqueta);
			value = new SpinnerNumberModel(0, 0, 63, 1); // initial value, minimum value,maximum value,step
			jsCptVA = new JSpinner(value);
			gridPanel.add(jsCptVA);

			jlEtiqueta = crearLabel("AZIMUT");
			gridPanel.add(jlEtiqueta);
			cbAZ = new JComboBox();
			cbAZ.addItem("ENCODER");
			cbAZ.addItem("SIMULADO");
			gridPanel.add(cbAZ);

//			jlEtiqueta = crearLabel("DAC A");
//			gridPanel.add(jlEtiqueta);
//			cbDACA = new JComboBox();
//			cbDACA.addItem("XCORR");
//			cbDACA.addItem("FILTER-INT");
//			cbDACA.addItem("CFAR");
//			cbDACA.addItem("RANGO");
//			cbDACA.addItem("BLANCOS");
//			cbDACA.addItem("COMPR 8 BITS");
//			gridPanel.add(cbDACA);
//			
//			jlEtiqueta = crearLabel("DAC B");
//			gridPanel.add(jlEtiqueta);
//			cbDACB = new JComboBox();
//			cbDACB.addItem("XCORR");
//			cbDACB.addItem("FILTER-INT");
//			cbDACB.addItem("CFAR");
//			cbDACB.addItem("RANGO");
//			cbDACB.addItem("BLANCOS");
//			cbDACB.addItem("COMPR 8 BITS");
//			gridPanel.add(cbDACB);

// -----------------8 Bits--------------------------			
			jlEtiqueta = crearLabel("COMPRESION");
			gridPanel.add(jlEtiqueta);
			cbDACB = new JComboBox();
			cbDACB.addItem("1-2-2-2-2-2-2-2");
			cbDACB.addItem("9-1-1-1-1-1-1-1");
			cbDACB.addItem("8-1-1-1-1-1-1-1 <<1");
			cbDACB.addItem("7-1-1-1-1-1-1-1 <<2");
			cbDACB.addItem("6-1-1-1-1-1-1-1 <<3");
			cbDACB.addItem("5-1-1-1-1-1-1-1 <<4");
			cbDACB.addItem("4-1-1-1-1-1-1-1 <<5");
			cbDACB.addItem("3-1-1-1-1-1-1-1 <<6");
			cbDACB.addItem("2-1-1-1-1-1-1-1 <<7");
			cbDACB.addItem("1-1-1-1-1-1-1-1 <<8");
			cbDACB.addItem("LOG");
			gridPanel.add(cbDACB);
// -----------------4 Bits--------------------------			
//			jlEtiqueta = crearLabel("COMPRESION");
//			gridPanel.add(jlEtiqueta);
//			cbDACB = new JComboBox();
//			cbDACB.addItem("3-4-4-4");
//			cbDACB.addItem("9-2-2-2");
//			cbDACB.addItem("8-2-2-2 <<1");
//			cbDACB.addItem("7-2-2-2 <<2");
//			cbDACB.addItem("6-2-2-2 <<3");
//			cbDACB.addItem("5-2-2-2 <<4");
//			cbDACB.addItem("4-2-2-2 <<5");
//			cbDACB.addItem("3-2-2-2 <<6");
//			cbDACB.addItem("2-2-2-2 <<7");
//			cbDACB.addItem("1-2-2-2 <<8");
//			gridPanel.add(cbDACB);

			jlEtiqueta = crearLabel("WEATHER");
			gridPanel.add(jlEtiqueta);
			value = new SpinnerNumberModel(0, 0, 7, 1); // initial value, minimum value,maximum value,step
			jsWeather = new JSpinner(value);
			gridPanel.add(jsWeather);

			jlEtiqueta = crearLabel("TUNE");
			gridPanel.add(jlEtiqueta);
			value = new SpinnerNumberModel(10, 0, 63, 1); // initial value, minimum value,maximum value,step
			jsTune = new JSpinner(value);
			gridPanel.add(jsTune);

			jlEtiqueta = crearLabel("CHANNEL");
			gridPanel.add(jlEtiqueta);
			value = new SpinnerNumberModel(6, 0, 20, 1); // initial value, minimum value,maximum value,step
			jsChannel = new JSpinner(value);
			gridPanel.add(jsChannel);

			jlEtiqueta = crearLabel("OBJ");
			gridPanel.add(jlEtiqueta);
			value = new SpinnerNumberModel(0, 0, 254, 1); // initial value, minimum value,maximum value,step
			jsOBJ = new JSpinner(value);
			gridPanel.add(jsOBJ);

			// btnCorto = crearBoton("Libre");
			// gridPanel.add(btnCorto);
			// btnMedio = crearBoton("Libre");
			// gridPanel.add(btnMedio);
			// btnLargo = crearBoton("Libre");
			// gridPanel.add(btnLargo);

			selectionPanel.setLayout(new BorderLayout());
			selectionPanel.add(gridPanel, BorderLayout.NORTH);
			gridPanel.setBackground(Color.BLACK);
			add(selectionPanel, BorderLayout.EAST);
			selectionPanel.setBackground(Color.BLACK);
		}
	}

	// activa o desactiva una capa dada de alta en el mapa
	public void ActivarCapa(String nombreCapa, boolean activar) {
		for (gov.nasa.worldwind.layers.Layer l : Mundo.getModel().getLayers()) {
			System.out.println(l.getName());
			if (l.getName().equals(nombreCapa)) {
				l.setEnabled(activar);
				break;
			}
		}
	}

	private void AgregaListenerTeclado() {
		Mundo.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
				System.out.println(e.getKeyChar());
				switch(e.getKeyChar()) {
				case 'r':
					_myPpi.setOpcionPintado(3);
					System.out.println("RUN");
					break;
				case 's':
					_myPpi.setOpcionPintado(2);
					System.out.println("STBY");
					break;
				case 'o':
					ActivarCapa("Scale bar", false);
					
					ActivarCapa("Stars", false);
					ActivarCapa("Atmosphere", false);
					ActivarCapa("NASA Blue Marble Image", false);
					ActivarCapa("Blue Marble May 2004", false);//
					ActivarCapa("i-cubed Landsat", false);//
					ActivarCapa("USDA NAIP", false);
					ActivarCapa("USDA NAIP USGS", false);
					ActivarCapa("MS Virtual Earth Aerial", false);//
					ActivarCapa("Bing Imagery", false);//
					ActivarCapa("USGS Topographic Maps 1:250K", false); 
					ActivarCapa("USGS Topographic Maps 1:100K", false);
					ActivarCapa("USGS Topographic Maps 1:24K", false);
					ActivarCapa("USGS Urban Area Ortho", false);
					ActivarCapa("Political Boundaries", false);
					ActivarCapa("Open Street Map", false);
					ActivarCapa("Earth at Night", false);
					ActivarCapa("Place Names", false);
					ActivarCapa("World Map", false);
					ActivarCapa("Scale bar", false);

					
					
					break;
				case 'd':
					


					ActivarCapa("NASA Blue Marble Image", true);
					ActivarCapa("Blue Marble May 2004", true);
					ActivarCapa("i-cubed Landsat", true);
					ActivarCapa("USDA NAIP", true);
					ActivarCapa("USDA NAIP USGS", true);
					ActivarCapa("MS Virtual Earth Aerial", true);
					ActivarCapa("Bing Imagery", true);

					break;
				}
			}
			
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}
			
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub	
			}
		});
	}

	public void CreaAjustaMundo() {
		Mundo = new WorldWindowGLCanvas();
		this.getContentPane().add(Mundo, java.awt.BorderLayout.CENTER);
		Mundo.setModel(new BasicModel());
		// Mundo.getModel().getLayers().removeAll();//comentar para mapa
		add(Mundo);
	}

	public WorldWindowGLCanvas getMundo() {
		return Mundo;
	}

	public void AjustaPropiedadesVentana() {
		Mundo.setPreferredSize(new java.awt.Dimension(1300, 1000));
		setAlwaysOnTop(true);
		setUndecorated(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("TZINCAN-200");
	}

	private void CreaConexionesZeroMQ() {

		contextoSocket0MQ = ZMQ.context(3);
		// mediante este equipo se envian las posiciones del mouse a la
		// aplicacion gui
		socket0MQEnviaFPGA = contextoSocket0MQ.socket(ZMQ.PUSH);

		int hwm = 25;
		socket0MQEnviaFPGA.setHWM(hwm);

		try { // si la aplicacion es stand-alone no debe de conectarse este socket
//			socket0MQEnviaFPGA.bind("tcp://localhost:5004");
//			socket0MQEnviaFPGA.bind("tcp://192.168.100.39:5004");
			socket0MQEnviaFPGA.bind("tcp://192.168.200.140:5100"); //cambio de puerto para pruebas
			System.out.println("Conecto al servidor ");

		} catch (Exception ex) {
			
			System.out.println(ex.toString());

			System.out.println("Error de conexion ");
		}

	}

	public void enviaComandoFPGA(String cmd) {
		try {
			String mensaje = cmd;
			System.out.println("Mensaje enviado " + mensaje);
			socket0MQEnviaFPGA.send(mensaje.getBytes(), ZMQ.NOBLOCK);
		} catch (Exception e) {
			System.out.println("Error al enviar comando");
		}
	}

	public void Alcance(int alc) {
		switch (alc) {
case 1:
			
			ALTURA_RADAR = ALTURA_025_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 2:
			
			ALTURA_RADAR = ALTURA_0125_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 3:
			
			ALTURA_RADAR = ALTURA_05_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 4:
			
			ALTURA_RADAR = ALTURA_1_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 5:
			
			ALTURA_RADAR = ALTURA_2_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 6:
			
			ALTURA_RADAR = ALTURA_4_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 7:
			
			ALTURA_RADAR = ALTURA_8_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 8:
			
			ALTURA_RADAR = ALTURA_16_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 9:
			
			ALTURA_RADAR = ALTURA_32_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		case 10:
			
			ALTURA_RADAR = ALTURA_64_Millas;
			CentraMapaAlturaActual(LATITUD_INICIAL,LONGITUD_INICIAL);
			break;
		default:
			break;

		}
	}

	/*
	 * public void CentraMapa(double latitud, double longitud) {
	 * System.out.println("ALTURA_RADAR:" + ALTURA_RADAR); puntoCentralMapa = new
	 * Position(Angle.fromDegrees(latitud), Angle.fromDegrees(longitud),
	 * Mundo.getView().getEyePosition().getAltitude());
	 * Mundo.getView().setEyePosition(puntoCentralMapa); }
	 */

	// centra el mapa a una posicion y altura dada
	public void CentraMapaAlturaActual(double latitud, double longitud) {
		// alturaActual = Mundo.getView().getEyePosition().getAltitude();
		puntoCentralMapa = new Position(Angle.fromDegrees(latitud), Angle.fromDegrees(longitud), ALTURA_RADAR);
		Mundo.getView().setEyePosition(puntoCentralMapa);
		Mundo.redraw();
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {
				Visualizador vis = new Visualizador();
				vis.pack();
				vis.setVisible(true);
			}
		});
	}

}