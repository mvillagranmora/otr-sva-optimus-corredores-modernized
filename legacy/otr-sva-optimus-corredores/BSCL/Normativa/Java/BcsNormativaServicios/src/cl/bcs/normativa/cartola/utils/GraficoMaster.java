package cl.bcs.normativa.cartola.utils;

import java.util.ArrayList;
import java.util.List;

public class GraficoMaster {
	
	List<GraficoDef> graficos;
	
	public static class GraficoDef {
		public String nombreGrafico;
	    public String nombreParametroReporte;
	    public String nombreProcedimiento;
	    public String reportePadre;
	};
	
	public List<GraficoDef> getGraficoMaster () {
		return graficos;
	};
	
	public GraficoMaster (String SessionLocale) {
		graficos = new ArrayList<GraficoDef>();
		
		//---------------------------------------------
		/*
			 .d8888b.  888    888 8888888 888      8888888888 
			d88P  Y88b 888    888   888   888      888        
			888    888 888    888   888   888      888        
			888        8888888888   888   888      8888888    
			888        888    888   888   888      888        
			888    888 888    888   888   888      888        
			Y88b  d88P 888    888   888   888      888        
			 "Y8888P"  888    888 8888888 88888888 8888888888 
		*/
		//---------------------------------------------
		
		if (SessionLocale.equals("CL")) {
			GraficoDef GraficoConsolidadoPorProducto = new GraficoDef();
			GraficoConsolidadoPorProducto.nombreGrafico				= "GraficoConsolidadoPorProducto";
			GraficoConsolidadoPorProducto.nombreParametroReporte	= "imgGrafico1";
			GraficoConsolidadoPorProducto.nombreProcedimiento 		= "GraficoConsolidadoPorProducto";
			GraficoConsolidadoPorProducto.reportePadre 				= "InfoCarteraMasterPag0";
		    graficos.add(GraficoConsolidadoPorProducto);
		    
		    /*GraficoDef GraficoActivoPorProducto = new GraficoDef();
		    GraficoActivoPorProducto.nombreGrafico				= "GraficoActivoPorProducto";
		    GraficoActivoPorProducto.nombreParametroReporte		= "imgGrafico1";
		    GraficoActivoPorProducto.nombreProcedimiento 		= "GraficoActivoPorProducto";
		    GraficoActivoPorProducto.reportePadre 				= "InfoCarteraMasterPag1";
		    graficos.add(GraficoActivoPorProducto);*/
		};
		
	
		//---------------------------------------------
		/*
			8888888b.  8888888888 8888888b.  888     888 
			888   Y88b 888        888   Y88b 888     888 
			888    888 888        888    888 888     888 
			888   d88P 8888888    888   d88P 888     888 
			8888888P"  888        8888888P"  888     888 
			888        888        888 T88b   888     888 
			888        888        888  T88b  Y88b. .d88P 
			888        8888888888 888   T88b  "Y88888P"  
		*/
		//---------------------------------------------
		
		
		if (SessionLocale.equals("PE")) {
			GraficoDef GraficoActivoPorProducto = new GraficoDef();
		    GraficoActivoPorProducto.nombreGrafico				= "GraficoActivoPorProducto";
		    GraficoActivoPorProducto.nombreParametroReporte		= "ImgGraficoPorProducto";
		    GraficoActivoPorProducto.nombreProcedimiento 		= "GraficoActivoPorProducto";
		    GraficoActivoPorProducto.reportePadre 				= "InfoCarteraMasterPag1";
		    graficos.add(GraficoActivoPorProducto);
		};
	};

	
};
