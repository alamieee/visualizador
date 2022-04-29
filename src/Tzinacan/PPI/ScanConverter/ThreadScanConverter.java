package Tzinacan.PPI.ScanConverter;

import Tzinacan.PPI.ScanConverter.ScanConverter;

public class ThreadScanConverter extends Thread{

	ScanConverter _scanConverterHilo = null;//instancia tipo scanconverter
	
	public ThreadScanConverter()//const vacio
	{
		
	}
	
	public void setScanConverterToHilo(ScanConverter sca)//
	{
		_scanConverterHilo = sca;
	}
	
	public void run()
	{
		while(true)
		{
			try {
				_scanConverterHilo.procesoObtenerScan();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}//fin while
	
}//fin class
