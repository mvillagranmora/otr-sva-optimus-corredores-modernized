package cl.bcs.normativa.cartola.utils;

import java.util.ArrayList;
import java.util.List;

public class ReporteMaster {
	
	List<ReporteDef> reportes;
	
	public static class ReporteDef {
		public String nombrePagina;
		public String nombreJasper;
		public String nombreBinarioJasper;
		public String nombreProcedimiento;
		public String filtroSoloMonedaSistema;
		public String filtroUbicacionBoveda;  //filtro del servicio
		public String nombreParametroReporte;
		public String nombreParametroDataSource;
	    public String nombreParametroInclusion;
	    public String tipoSubReport;
	    public String reportePadre;
	    public String reporteConsolidado;
	    public String poseeSubReporteCabecera;
	    public String poseeSubReporteDetalle;
	    
	    
	    public String nombreGrafico= "";
	    public String procedimientoGrafico = "";
	};
	
	public List<ReporteDef> getReporteMaster () {
		return reportes;
	};
	
	public ReporteMaster (String SessionLocale) {
		reportes = new ArrayList<ReporteDef>();
		
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
			ReporteDef InfoCarteraMasterPag0 = new ReporteDef();
			InfoCarteraMasterPag0.nombrePagina				= "InfoCarteraMasterPag0";
			InfoCarteraMasterPag0.nombreJasper 				= "InfoCarteraMasterPag0";
			InfoCarteraMasterPag0.nombreBinarioJasper 		= "InfoCarteraMasterPag0Jasper";
			InfoCarteraMasterPag0.nombreProcedimiento 		= "InfoCarteraMaster";
		    InfoCarteraMasterPag0.nombreParametroInclusion 	= "showCarteraConsolidada";
		    InfoCarteraMasterPag0.tipoSubReport 			= "N";
		    InfoCarteraMasterPag0.reporteConsolidado 		= "S";
		    InfoCarteraMasterPag0.poseeSubReporteCabecera 	= "S";
		    InfoCarteraMasterPag0.poseeSubReporteDetalle 	= "N";
		    
		   // InfoCarteraMasterPag0.nombreGrafico  = "GraficoConsolidadoPorProducto";
		   // InfoCarteraMasterPag0.procedimientoGrafico  = "GraficoConsolidadoPorProducto";
		    
		    reportes.add(InfoCarteraMasterPag0);
		    
		    ReporteDef InfoCarteraMasterPag1 = new ReporteDef();
		    InfoCarteraMasterPag1.nombrePagina				= "InfoCarteraMasterPag1";
		    InfoCarteraMasterPag1.nombreJasper				= "InfoCarteraMasterPag1";
		    InfoCarteraMasterPag1.nombreBinarioJasper		= "InfoCarteraMasterPag1Jasper";
		    InfoCarteraMasterPag1.nombreProcedimiento		= "InfoCarteraMaster";
		    InfoCarteraMasterPag1.tipoSubReport				= "N";
		    InfoCarteraMasterPag1.reporteConsolidado		= "N";
		    InfoCarteraMasterPag1.poseeSubReporteCabecera	= "S";
		    InfoCarteraMasterPag1.poseeSubReporteDetalle	= "N";
		   
		    InfoCarteraMasterPag1.nombreGrafico  = "GraficoConsolidadoPorProducto";
		    InfoCarteraMasterPag1.procedimientoGrafico  = "GraficoConsolidadoPorProducto";
		    
		    reportes.add(InfoCarteraMasterPag1);
		    
		    ReporteDef InfoCarteraMasterPag2 = new ReporteDef();
		    InfoCarteraMasterPag2.nombrePagina				= "InfoCarteraMasterPag2";
		    InfoCarteraMasterPag2.nombreJasper				= "InfoCarteraMasterPag4";
		    InfoCarteraMasterPag2.nombreBinarioJasper		= "InfoCarteraMasterPag4Jasper";
		    InfoCarteraMasterPag2.nombreProcedimiento		= "InfoCarteraMaster";
		    InfoCarteraMasterPag2.tipoSubReport				= "N";
		    InfoCarteraMasterPag2.reporteConsolidado		= "N";
		    InfoCarteraMasterPag2.poseeSubReporteCabecera	= "N";
		    InfoCarteraMasterPag2.poseeSubReporteDetalle	= "S";
		    reportes.add(InfoCarteraMasterPag2);
		    
		    ////////////////////////////SUBREPORT INFO PORTADA ////////////////////////////////////		    
		    ReporteDef ResumenInversionConsolidado = new ReporteDef();
		    ResumenInversionConsolidado.nombrePagina				= "ResumenInversionConsolidado";
		    ResumenInversionConsolidado.nombreJasper 				= "itemResumenInversionConsolidado";
		    ResumenInversionConsolidado.nombreBinarioJasper 		= "ResumenInversionConsolidadoJasper";
		    ResumenInversionConsolidado.nombreProcedimiento 		= "ResumenInversionConsolidado";
		    ResumenInversionConsolidado.nombreParametroReporte 		= "resumenInversionConsolidado";
		    ResumenInversionConsolidado.nombreParametroDataSource 	= "DataResumenInversionConsolidado";
		    ResumenInversionConsolidado.tipoSubReport 				= "S";
		    ResumenInversionConsolidado.reportePadre 				= "InfoCarteraMasterPag0";
		    ResumenInversionConsolidado.poseeSubReporteCabecera 	= "S";
		    reportes.add(ResumenInversionConsolidado);
		    
		    ReporteDef ResumenFlujosProyectadosConsolidado = new ReporteDef();
		    ResumenFlujosProyectadosConsolidado.nombrePagina				= "ResumenFlujosProyectadosConsolidado";
		    ResumenFlujosProyectadosConsolidado.nombreJasper 				= "itemResumenFlujosProyectadosConsolidado";
		    ResumenFlujosProyectadosConsolidado.nombreBinarioJasper 		= "ResumenFlujosProyectadosConsolidadoJasper";
		    ResumenFlujosProyectadosConsolidado.nombreProcedimiento 		= "ResumenFlujosProyectadosConsolidado";
		    ResumenFlujosProyectadosConsolidado.nombreParametroReporte 		= "resumenFlujosProyectadosConsolidado";
		    ResumenFlujosProyectadosConsolidado.nombreParametroDataSource 	= "DataResumenFlujosProyectadosConsolidado";
		    ResumenFlujosProyectadosConsolidado.tipoSubReport 				= "S";
		    ResumenFlujosProyectadosConsolidado.reportePadre 				= "InfoCarteraMasterPag0";
		    ResumenFlujosProyectadosConsolidado.poseeSubReporteCabecera 	= "S";
		    reportes.add(ResumenFlujosProyectadosConsolidado);
		    
		    ReporteDef ResumenInversion = new ReporteDef();
		    ResumenInversion.nombrePagina				= "ResumenInversion";
		    ResumenInversion.nombreJasper 				= "itemResInversionClasico";
		    ResumenInversion.nombreBinarioJasper 		= "ResumenInversionJasper";
		    ResumenInversion.nombreProcedimiento 		= "ResumenInversion";
		    ResumenInversion.nombreParametroReporte 	= "resumenInversion";
		    ResumenInversion.nombreParametroDataSource 	= "DataResumenInversion";
		    ResumenInversion.tipoSubReport 				= "S";
		    ResumenInversion.reportePadre 				= "InfoCarteraMasterPag1";
		    ResumenInversion.poseeSubReporteCabecera 	= "S";
		    reportes.add(ResumenInversion);
		    
		    ReporteDef ResumenMovimientos = new ReporteDef();
		    ResumenMovimientos.nombrePagina					= "ResumenMovimientos";
		    ResumenMovimientos.nombreJasper 				= "resumenMovimientos";
		    ResumenMovimientos.nombreBinarioJasper 			= "ResumenMovimientosJasper";
		    ResumenMovimientos.nombreProcedimiento 			= "ResumenMovimientos";
		    ResumenMovimientos.nombreParametroReporte 		= "resumenMovimientos";
		    ResumenMovimientos.nombreParametroDataSource 	= "DataResumenMovimientos";
		    ResumenMovimientos.tipoSubReport 				= "S";
		    ResumenMovimientos.reportePadre 				= "InfoCarteraMasterPag1";
		    ResumenMovimientos.poseeSubReporteCabecera 		= "S";
		    reportes.add(ResumenMovimientos);
		    
		    ReporteDef FlujoPatrimonial = new ReporteDef();
		    FlujoPatrimonial.nombrePagina				= "FlujoPatrimonial";
		    FlujoPatrimonial.nombreJasper 				= "flujoPatrimonial";
		    FlujoPatrimonial.nombreBinarioJasper 		= "FlujoPatrimonialJasper";
		    FlujoPatrimonial.nombreProcedimiento 		= "FlujoPatrimonial";
		    FlujoPatrimonial.nombreParametroReporte 	= "flujoPatrimonial";
		    FlujoPatrimonial.nombreParametroDataSource 	= "DataFlujoPatrimonial";
		    FlujoPatrimonial.tipoSubReport 				= "S";
		    FlujoPatrimonial.reportePadre 				= "InfoCarteraMasterPag1";
		    FlujoPatrimonial.poseeSubReporteCabecera 	= "S";
		    reportes.add(FlujoPatrimonial);
		    
		    ReporteDef FlujoProyectado = new ReporteDef();
		    FlujoProyectado.nombrePagina				= "FlujoProyectado";
		    FlujoProyectado.nombreJasper 				= "flujoProyectado";
		    FlujoProyectado.nombreBinarioJasper 		= "FlujoProyectadoJasper";
		    FlujoProyectado.nombreProcedimiento 		= "FlujoProyectado";		    
		    FlujoProyectado.nombreParametroReporte 		= "flujoProyectado";
		    FlujoProyectado.nombreParametroDataSource 	= "DataFlujoProyectado";
		    FlujoProyectado.tipoSubReport 				= "S";
		    FlujoProyectado.reportePadre 				= "InfoCarteraMasterPag1";
		    FlujoProyectado.poseeSubReporteCabecera 	= "S";
		    reportes.add(FlujoProyectado);
		    
		    ReporteDef PatrimonioValorizado = new ReporteDef();
		    PatrimonioValorizado.nombrePagina				= "PatrimonioValorizado";
		    PatrimonioValorizado.nombreJasper 				= "patrimonioValorizado_H";
		    PatrimonioValorizado.nombreBinarioJasper 		= "PatrimonioValorizadoJasper";
		    PatrimonioValorizado.nombreProcedimiento 		= "PatrimonioValorizado";
		    PatrimonioValorizado.nombreParametroReporte 	= "patrimonioVal";
		    PatrimonioValorizado.nombreParametroDataSource 	= "DataPatrimonioVal";
		    PatrimonioValorizado.tipoSubReport 				= "S";
		    PatrimonioValorizado.reportePadre 				= "InfoCarteraMasterPag1";
		    PatrimonioValorizado.poseeSubReporteCabecera 	= "S";
		    reportes.add(PatrimonioValorizado);
		    
		    ReporteDef CodigoPDF417 = new ReporteDef();
		    CodigoPDF417.nombrePagina				= "CodigoPDF417";
		    CodigoPDF417.nombreJasper 				= "graficoPDF417Cartola";
		    CodigoPDF417.nombreProcedimiento 		= "CodigoPDF417";
		    CodigoPDF417.nombreParametroReporte 	= "codigoPDF417";
		    CodigoPDF417.nombreParametroDataSource 	= "DataCodigoPDF417";
		    CodigoPDF417.tipoSubReport 				= "S";
		    CodigoPDF417.reportePadre 				= "InfoCarteraMasterPag1";
		    CodigoPDF417.poseeSubReporteCabecera 	= "S";
		    reportes.add(CodigoPDF417);
		    
		    ReporteDef ItemDerivados = new ReporteDef();
		    ItemDerivados.nombrePagina				= "ItemDerivados";
		    ItemDerivados.nombreJasper 				= "itemDerivados";
		    ItemDerivados.nombreBinarioJasper 		= "ItemDerivadosJasper";
		    ItemDerivados.nombreProcedimiento 		= "ItemDerivados";
		    ItemDerivados.nombreParametroReporte 	= "resumenDerivados";
		    ItemDerivados.nombreParametroDataSource = "DataResumenDerivados";
		    ItemDerivados.nombreParametroInclusion	= "PFU_showReport";
		    ItemDerivados.tipoSubReport 			= "S";
		    ItemDerivados.reportePadre 				= "InfoCarteraMasterPag1";
		    ItemDerivados.poseeSubReporteCabecera 	= "S";
		    reportes.add(ItemDerivados);
		    
		    ReporteDef ItemDerivadosForward = new ReporteDef();
		    ItemDerivadosForward.nombrePagina				= "ItemDerivadosFWD";
		    ItemDerivadosForward.nombreJasper 				= "itemDerivadosFWD";
		    ItemDerivadosForward.nombreBinarioJasper 		= "ItemDerivadosFWDJasper";
		    ItemDerivadosForward.nombreProcedimiento 		= "ItemDerivadosFWD";
		    ItemDerivadosForward.nombreParametroReporte 	= "resumenForward";
		    ItemDerivadosForward.nombreParametroDataSource 	= "dataResumenForward";
		    ItemDerivadosForward.nombreParametroInclusion	= "PFU_showReport";
		    ItemDerivadosForward.tipoSubReport 				= "S";
		    ItemDerivadosForward.reportePadre 				= "InfoCarteraMasterPag1";
		    ItemDerivadosForward.poseeSubReporteCabecera 	= "S";
		    reportes.add(ItemDerivadosForward);
		    
		    ////////////////////////////SUBREPORT INFO DETALLE ///////////////////////////		    
		    ReporteDef CarteraRV = new ReporteDef();
		    CarteraRV.nombrePagina				= "CarteraRV";
		    CarteraRV.nombreJasper 				= "carteraRv";
		    CarteraRV.nombreBinarioJasper 		= "CarteraRVJasper";
		    CarteraRV.nombreProcedimiento 		= "CarteraRV";//--
		    CarteraRV.nombreParametroReporte 	= "subGrilla";//--
		    CarteraRV.nombreParametroDataSource = "DataGrilla";//--
		    CarteraRV.nombreParametroInclusion	= "PRV_incCartera";//----
		    CarteraRV.tipoSubReport 			= "S";
		    CarteraRV.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraRV.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraRV);
		    
		    /*
		    ReporteDef CarteraFU = new ReporteDef();
		    CarteraFU.nombrePagina				= "CarteraFU";
		    CarteraFU.nombreJasper 				= "carteraFU";
		    CarteraFU.nombreBinarioJasper 		= "CarteraFUJasper";
		    CarteraFU.nombreProcedimiento 		= "CarteraFU";//--
		    CarteraFU.nombreParametroReporte 	= "subGrilla";//--
		    CarteraFU.nombreParametroDataSource = "DataGrilla";//--
		    CarteraFU.nombreParametroInclusion	= "FU_incCartera";//----
		    CarteraFU.tipoSubReport 			= "S";
		    CarteraFU.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraFU.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraFU);
		    
		    ReporteDef CarteraOP = new ReporteDef();
		    CarteraOP.nombrePagina				= "CarteraOP";
		    CarteraOP.nombreJasper 				= "carteraOP";
		    CarteraOP.nombreBinarioJasper 		= "CarteraOPJasper";
		    CarteraOP.nombreProcedimiento 		= "CarteraOP";//--
		    CarteraOP.nombreParametroReporte 	= "subGrilla";//--
		    CarteraOP.nombreParametroDataSource = "DataGrilla";//--
		    CarteraOP.nombreParametroInclusion	= "OP_incCartera";//----
		    CarteraOP.tipoSubReport 			= "S";
		    CarteraOP.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraOP.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraOP);

		    ReporteDef CarteraSE = new ReporteDef();
		    CarteraSE.nombrePagina				= "CarteraSE";
		    CarteraSE.nombreJasper 				= "carteraSE";
		    CarteraSE.nombreBinarioJasper 		= "CarteraSEJasper";
		    CarteraSE.nombreProcedimiento 		= "CarteraSE";//--
		    CarteraSE.nombreParametroReporte 	= "subGrilla";//--
		    CarteraSE.nombreParametroDataSource = "DataGrilla";//--
		    CarteraSE.nombreParametroInclusion	= "SE_incCartera";//----
		    CarteraSE.tipoSubReport 			= "S";
		    CarteraSE.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraSE.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraSE);
			*/
		    
		    ReporteDef CarteraFM = new ReporteDef();
		    CarteraFM.nombrePagina				= "CarteraFM";
		    CarteraFM.nombreJasper 				= "carteraFM";
		    CarteraFM.nombreBinarioJasper 		= "CarteraFMJasper";
		    CarteraFM.nombreProcedimiento 		= "CarteraFM";//--
		    CarteraFM.nombreParametroReporte 	= "subGrilla";//--
		    CarteraFM.nombreParametroDataSource = "DataGrilla";//--
		    CarteraFM.nombreParametroInclusion	= "FM_incCartera";//----
		    CarteraFM.tipoSubReport 			= "S";
		    CarteraFM.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraFM.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraFM);
		    
		    
		    
		    ReporteDef CarteraRF = new ReporteDef();
		    CarteraRF.nombrePagina				= "CarteraRF";
		    CarteraRF.nombreJasper 				= "carteraRF";
		    CarteraRF.nombreBinarioJasper 		= "CarteraRFJasper";
		    CarteraRF.nombreProcedimiento 		= "CarteraRF";
		    CarteraRF.nombreParametroReporte 	= "subGrilla";
		    CarteraRF.nombreParametroDataSource = "DataGrilla";
		    CarteraRF.nombreParametroInclusion	= "IRF_incCartera";
		    CarteraRF.tipoSubReport 			= "S";
		    CarteraRF.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraRF.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraRF);
		    
		    ReporteDef CarteraIF = new ReporteDef();
		    CarteraIF.nombrePagina				= "CarteraIF";
		    CarteraIF.nombreJasper 				= "carteraIIF";
		    CarteraIF.nombreBinarioJasper 		= "CarteraIFJasper";
		    CarteraIF.nombreProcedimiento 		= "CarteraIF";
		    CarteraIF.nombreParametroReporte 	= "subGrilla";
		    CarteraIF.nombreParametroDataSource = "DataGrilla";
		    CarteraIF.nombreParametroInclusion	= "IIF_incCartera";
		    CarteraIF.tipoSubReport 			= "S";
		    CarteraIF.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraIF.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraIF);
		    
		    ReporteDef CarteraSimultaneas = new ReporteDef();
		    CarteraSimultaneas.nombrePagina					= "CarteraSimultaneas";
		    CarteraSimultaneas.nombreJasper 				= "carteraSimultanea";
		    CarteraSimultaneas.nombreBinarioJasper 			= "CarteraSimultaneaJasper";
		    CarteraSimultaneas.nombreProcedimiento 			= "CarteraSimultaneas";
		    CarteraSimultaneas.nombreParametroReporte 		= "subGrilla";
		    CarteraSimultaneas.nombreParametroDataSource	= "DataGrilla";
		    CarteraSimultaneas.nombreParametroInclusion		= "SIM_incCartera";
		    CarteraSimultaneas.tipoSubReport 				= "S";
		    CarteraSimultaneas.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraSimultaneas.poseeSubReporteCabecera 		= "N";
		    reportes.add(CarteraSimultaneas);
		    
		    ReporteDef CarteraPactos = new ReporteDef();
		    CarteraPactos.nombrePagina				= "CarteraPc";
		    CarteraPactos.nombreJasper 				= "carteraPc";
		    CarteraPactos.nombreBinarioJasper 		= "CarteraPcJasper";
		    CarteraPactos.nombreProcedimiento 		= "CarteraPc";
		    CarteraPactos.nombreParametroReporte 	= "subGrilla";
		    CarteraPactos.nombreParametroDataSource = "DataGrilla";
		    CarteraPactos.nombreParametroInclusion	= "IPC_incCartera";
		    CarteraPactos.tipoSubReport 			= "S";
		    CarteraPactos.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraPactos.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraPactos);
		    
		    ReporteDef CarteraForward = new ReporteDef();
		    CarteraForward.nombrePagina				= "CarteraForward";
		    CarteraForward.nombreJasper 			= "carteraForward";
		    CarteraForward.nombreBinarioJasper 		= "CarteraForwardJasper";
		    CarteraForward.nombreProcedimiento 		= "CarteraForward";
		    CarteraForward.nombreParametroReporte 	= "subGrilla";
		    CarteraForward.nombreParametroDataSource = "DataGrilla";
		    CarteraForward.nombreParametroInclusion	= "FWD_incCartera";
		    CarteraForward.tipoSubReport 			= "S";
		    CarteraForward.reportePadre 			= "InfoCarteraMasterPag2";
		    CarteraForward.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraForward);
		    
		    ReporteDef CarteraVC = new ReporteDef();
		    CarteraVC.nombrePagina				= "CarteraVC";
		    CarteraVC.nombreJasper 				= "carteraVC";
		    CarteraVC.nombreBinarioJasper 		= "CarteraVCJasper";
		    CarteraVC.nombreProcedimiento 		= "CarteraVC";//--
		    CarteraVC.nombreParametroReporte 	= "subGrilla";//--
		    CarteraVC.nombreParametroDataSource = "DataGrilla";//--
		    CarteraVC.nombreParametroInclusion	= "PVC_incCartera";//----
		    CarteraVC.tipoSubReport 			= "S";
		    CarteraVC.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraVC.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraVC);
		    
		    /*
		    ReporteDef CarteraFwd = new ReporteDef();
		    CarteraFwd.nombrePagina				= "CarteraFwd";
		    CarteraFwd.nombreJasper 			= "carteraFwd";
		    CarteraFwd.nombreBinarioJasper 		= "CarteraFwdJasper";
		    CarteraFwd.nombreProcedimiento 		= "CarteraFwd";
		    CarteraFwd.nombreParametroReporte 	= "subGrilla";
		    CarteraFwd.nombreParametroDataSource = "DataGrilla";
		    CarteraFwd.nombreParametroInclusion	= "IFW_incCartera";
		    CarteraFwd.tipoSubReport 			= "S";
		    CarteraFwd.reportePadre 			= "InfoCarteraMasterPag2";
		    CarteraFwd.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraFwd);
		    */
		    
		    ReporteDef MovimientosTitulos = new ReporteDef();
		    MovimientosTitulos.nombrePagina					= "MovimientosTitulos";
		    MovimientosTitulos.nombreJasper 				= "MovTitulos";
		    MovimientosTitulos.nombreBinarioJasper 			= "MovimientosTitulosJasper";
		    MovimientosTitulos.nombreProcedimiento 			= "MovimientosTitulos";
		    MovimientosTitulos.nombreParametroReporte 		= "subGrilla";
		    MovimientosTitulos.nombreParametroDataSource	= "DataGrilla";
		    MovimientosTitulos.nombreParametroInclusion		= "MTI_incCartera";
		    MovimientosTitulos.tipoSubReport 				= "S";
		    MovimientosTitulos.reportePadre 				= "InfoCarteraMasterPag2";
		    MovimientosTitulos.poseeSubReporteCabecera 		= "N";
		    reportes.add(MovimientosTitulos);
		    
		    ReporteDef MovimientosCaja = new ReporteDef();
		    MovimientosCaja.nombrePagina				= "MovimientosCaja";
		    MovimientosCaja.nombreJasper 				= "movimientosCaja";
		    MovimientosCaja.nombreBinarioJasper 		= "MovimientosCajaJasper";
		    MovimientosCaja.nombreProcedimiento 		= "MovimientosCaja";
		    MovimientosCaja.nombreParametroReporte 		= "subGrilla";
		    MovimientosCaja.nombreParametroDataSource	= "DataGrilla";
		    MovimientosCaja.nombreParametroInclusion	= "MCJ_incCartera";
		    MovimientosCaja.tipoSubReport 				= "S";
		    MovimientosCaja.reportePadre 				= "InfoCarteraMasterPag2";
		    MovimientosCaja.poseeSubReporteCabecera 	= "N";
		    reportes.add(MovimientosCaja);
		    
		    ReporteDef MovTitulosFWD = new ReporteDef();
		    MovTitulosFWD.nombrePagina					= "MovTitulosFWD";
		    MovTitulosFWD.nombreJasper 					= "MovTitulosFWD";
		    MovTitulosFWD.nombreBinarioJasper 			= "MovTitulosFWDJasper";
		    MovTitulosFWD.nombreProcedimiento 			= "MovimientosTitulosForward";
		    MovTitulosFWD.nombreParametroReporte 		= "subGrilla";
		    MovTitulosFWD.nombreParametroDataSource		= "DataGrilla";
		    MovTitulosFWD.nombreParametroInclusion		= "MFW_incCartera";
		    MovTitulosFWD.tipoSubReport 				= "S";
		    MovTitulosFWD.reportePadre 					= "InfoCarteraMasterPag2";
		    MovTitulosFWD.poseeSubReporteCabecera 		= "N";
		    reportes.add(MovTitulosFWD);
		    
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
			ReporteDef InfoCarteraMasterPag1 = new ReporteDef();
		    InfoCarteraMasterPag1.nombrePagina				= "InfoCarteraMasterPag1";
		    InfoCarteraMasterPag1.nombreJasper				= "InfoCarteraMasterPeruPag1";
		    InfoCarteraMasterPag1.nombreBinarioJasper		= "InfoCarteraMasterPeruPag1Jasper";
		    InfoCarteraMasterPag1.nombreProcedimiento		= "InfoCarteraMaster";
		    InfoCarteraMasterPag1.tipoSubReport				= "N";
		    InfoCarteraMasterPag1.reporteConsolidado		= "N";
		    InfoCarteraMasterPag1.poseeSubReporteCabecera	= "S";
		    InfoCarteraMasterPag1.poseeSubReporteDetalle	= "N";
		    
		    InfoCarteraMasterPag1.nombreGrafico  = "GraficoActivoPorProducto";
		    InfoCarteraMasterPag1.procedimientoGrafico  = "GraficoActivoPorProducto";
		    
		    reportes.add(InfoCarteraMasterPag1);
		    
		    ReporteDef InfoCarteraMasterPag2 = new ReporteDef();
		    InfoCarteraMasterPag2.nombrePagina				= "InfoCarteraMasterPag2";
		    InfoCarteraMasterPag2.nombreJasper				= "InfoCarteraMasterPeruPag2";
		    InfoCarteraMasterPag2.nombreBinarioJasper		= "InfoCarteraMasterPeruPag2Jasper";
		    InfoCarteraMasterPag2.nombreProcedimiento		= "InfoCarteraMaster";
		    InfoCarteraMasterPag2.tipoSubReport				= "N";
		    InfoCarteraMasterPag2.reporteConsolidado		= "N";
		    InfoCarteraMasterPag2.poseeSubReporteCabecera	= "N";
		    InfoCarteraMasterPag2.poseeSubReporteDetalle	= "S";
		    reportes.add(InfoCarteraMasterPag2);
		    
		    ////////////////////////////SUBREPORT INFO PORTADA ////////////////////////////////////
		    ReporteDef FlujoProyectado = new ReporteDef();
		    FlujoProyectado.nombrePagina				= "FlujoProyectado";
		    FlujoProyectado.nombreJasper 				= "flujoProyectadoPeru";
		    FlujoProyectado.nombreBinarioJasper 		= "FlujoProyectadoPeruJasper";
		    FlujoProyectado.nombreProcedimiento 		= "FlujoProyectado";
		    FlujoProyectado.filtroSoloMonedaSistema		= "N";
		    FlujoProyectado.nombreParametroReporte 		= "flujoProyectado";
		    FlujoProyectado.nombreParametroDataSource 	= "DataFlujoProyectado";
		    FlujoProyectado.tipoSubReport 				= "S";
		    FlujoProyectado.reportePadre 				= "InfoCarteraMasterPag1";
		    FlujoProyectado.poseeSubReporteCabecera 	= "S";
		    reportes.add(FlujoProyectado);

		    ReporteDef ResumenInversion = new ReporteDef();
		    ResumenInversion.nombrePagina				= "ResumenInversion";
		    ResumenInversion.nombreJasper 				= "resumenInversionPeru";
		    ResumenInversion.nombreBinarioJasper 		= "ResumenInversionPeruJasper";
		    ResumenInversion.nombreProcedimiento 		= "ResumenInversion";
		    ResumenInversion.nombreParametroReporte 	= "resumenInversionPeru";
		    ResumenInversion.nombreParametroDataSource 	= "DataResumenInversion";
		    ResumenInversion.tipoSubReport 				= "S";
		    ResumenInversion.reportePadre 				= "InfoCarteraMasterPag1";
		    ResumenInversion.poseeSubReporteCabecera 	= "S";
		    reportes.add(ResumenInversion);
		    
		    ReporteDef resumenMovimientosCaja = new ReporteDef();
		    resumenMovimientosCaja.nombrePagina					= "ResumenMovimientosCaja";
		    resumenMovimientosCaja.nombreJasper 				= "resumenMovimientosCajaPeru";
		    resumenMovimientosCaja.nombreBinarioJasper 			= "ResumenMovimientosCajaPeruJasper";
		    resumenMovimientosCaja.nombreProcedimiento 			= "ResumenMovimientos";
		    resumenMovimientosCaja.nombreParametroReporte 		= "resumenMovimientosCajaPeru";
		    resumenMovimientosCaja.nombreParametroDataSource 	= "DataMovimientosCaja";
		    resumenMovimientosCaja.tipoSubReport 				= "S";
		    resumenMovimientosCaja.reportePadre 				= "InfoCarteraMasterPag1";
		    resumenMovimientosCaja.poseeSubReporteCabecera 		= "S";
		    reportes.add(resumenMovimientosCaja);
		    
			////////////////////////////SUBREPORT INFO DETALLE ///////////////////////////		    
			ReporteDef CarteraRVLocal = new ReporteDef();
			CarteraRVLocal.nombrePagina					= "CarteraRVLocal";
			CarteraRVLocal.nombreJasper 				= "carteraPeruRVLocal";
			CarteraRVLocal.nombreBinarioJasper 			= "CarteraPeruRVLocalJasper";
			CarteraRVLocal.nombreProcedimiento 			= "CarteraRV";
			CarteraRVLocal.filtroUbicacionBoveda		= "LOCAL";
			CarteraRVLocal.filtroSoloMonedaSistema		= "N";
			CarteraRVLocal.nombreParametroReporte 		= "subGrilla";
			CarteraRVLocal.nombreParametroDataSource	= "DataGrilla";
			CarteraRVLocal.nombreParametroInclusion		= "PRV_incCartera";
			CarteraRVLocal.tipoSubReport 				= "S";
			CarteraRVLocal.reportePadre 				= "InfoCarteraMasterPag2";
			CarteraRVLocal.poseeSubReporteCabecera 		= "N";
			reportes.add(CarteraRVLocal);
			
			ReporteDef CarteraRVExterior = new ReporteDef();
			CarteraRVExterior.nombrePagina					= "CarteraRVExterior";
			CarteraRVExterior.nombreJasper 					= "carteraPeruRVExterior";
			CarteraRVExterior.nombreBinarioJasper 			= "CarteraPeruRVExteriorJasper";
			CarteraRVExterior.nombreProcedimiento 			= "CarteraRV";
			CarteraRVExterior.filtroSoloMonedaSistema		= "N";
			CarteraRVExterior.filtroUbicacionBoveda			= "EXTERIOR";
			CarteraRVExterior.nombreParametroReporte 		= "subGrilla";
			CarteraRVExterior.nombreParametroDataSource 	= "DataGrilla";
			CarteraRVExterior.nombreParametroInclusion		= "PRV_incCartera";
			CarteraRVExterior.tipoSubReport 				= "S";
			CarteraRVExterior.reportePadre 					= "InfoCarteraMasterPag2";
			CarteraRVExterior.poseeSubReporteCabecera 		= "N";
			reportes.add(CarteraRVExterior);
			
			ReporteDef CarteraRFLocal = new ReporteDef();
			CarteraRFLocal.nombrePagina				= "CarteraRFLocal";
			CarteraRFLocal.nombreJasper 			= "carteraPeruRFLocal";
			CarteraRFLocal.nombreBinarioJasper 		= "CarteraPeruRFLocalJasper";
			CarteraRFLocal.nombreProcedimiento 		= "CarteraRF";
			CarteraRFLocal.filtroUbicacionBoveda	= "LOCAL";
			CarteraRFLocal.filtroSoloMonedaSistema	= "N";
			CarteraRFLocal.nombreParametroReporte 	= "subGrilla";
			CarteraRFLocal.nombreParametroDataSource = "DataGrilla";
		    CarteraRFLocal.nombreParametroInclusion	= "IRF_incCartera";
		    CarteraRFLocal.tipoSubReport 			= "S";
		    CarteraRFLocal.reportePadre 			= "InfoCarteraMasterPag2";
		    CarteraRFLocal.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraRFLocal);
		    
		    ReporteDef CarteraRFExterior = new ReporteDef();
		    CarteraRFExterior.nombrePagina				= "CarteraRFExterior";
		    CarteraRFExterior.nombreJasper 				= "carteraPeruRFExterior";
		    CarteraRFExterior.nombreBinarioJasper 		= "CarteraPeruRFExteriorJasper";
		    CarteraRFExterior.nombreProcedimiento 		= "CarteraRF";
		    CarteraRFExterior.filtroSoloMonedaSistema	= "N";
		    CarteraRFExterior.filtroUbicacionBoveda		= "EXTERIOR";
		    CarteraRFExterior.nombreParametroReporte 	= "subGrilla";
		    CarteraRFExterior.nombreParametroDataSource = "DataGrilla";
		    CarteraRFExterior.nombreParametroInclusion	= "IRF_incCartera";
		    CarteraRFExterior.tipoSubReport 			= "S";
		    CarteraRFExterior.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraRFExterior.poseeSubReporteCabecera 	= "N";
		    reportes.add(CarteraRFExterior);
		    
		    ReporteDef CarteraOpciones = new ReporteDef();
		    CarteraOpciones.nombrePagina				= "CarteraOpciones";
		    CarteraOpciones.nombreJasper 				= "carteraPeruOpciones";
		    CarteraOpciones.nombreBinarioJasper 		= "CarteraPeruOpcionesJasper";
		    CarteraOpciones.nombreProcedimiento 		= "CarteraOpciones";
		    CarteraOpciones.nombreParametroReporte 		= "subGrilla";
		    CarteraOpciones.nombreParametroDataSource	= "DataGrilla";
		    CarteraOpciones.nombreParametroInclusion	= "OPC_incCartera";
		    CarteraOpciones.tipoSubReport 				= "S";
		    CarteraOpciones.reportePadre 				= "InfoCarteraMasterPag2";
		    CarteraOpciones.poseeSubReporteCabecera 	= "N";
		    CarteraOpciones.filtroSoloMonedaSistema		= "N";
			reportes.add(CarteraOpciones);
		    
		    ReporteDef Reporte = new ReporteDef();
		    Reporte.nombrePagina				= "Reporte";
		    Reporte.nombreJasper 				= "carteraReporte";
		    Reporte.nombreBinarioJasper 		= "ReportePeruJasper";
		    Reporte.nombreProcedimiento 		= "CarteraSimultaneas";
		    Reporte.nombreParametroReporte 		= "subGrilla";
		    Reporte.nombreParametroDataSource	= "DataGrilla";
		    Reporte.nombreParametroInclusion	= "SIM_incCartera";
		    Reporte.tipoSubReport 				= "S";
		    Reporte.reportePadre 				= "InfoCarteraMasterPag2";
		    Reporte.poseeSubReporteCabecera 	= "N";
		    reportes.add(Reporte);
		    
		    ReporteDef MovimientosTitulos = new ReporteDef();
		    MovimientosTitulos.nombrePagina					= "MovimientosTitulos";
		    MovimientosTitulos.nombreJasper 				= "movimientoTitulosPeru";
		    MovimientosTitulos.nombreBinarioJasper 			= "MovimientoTitulosPeruJasper";
		    MovimientosTitulos.nombreProcedimiento 			= "MovimientosTitulos";
		    MovimientosTitulos.nombreParametroReporte 		= "subGrilla";
		    MovimientosTitulos.nombreParametroDataSource	= "DataGrilla";
		    MovimientosTitulos.nombreParametroInclusion		= "MTI_incCartera";
		    MovimientosTitulos.tipoSubReport 				= "S";
		    MovimientosTitulos.reportePadre 				= "InfoCarteraMasterPag2";
		    MovimientosTitulos.poseeSubReporteCabecera 		= "N";
		    reportes.add(MovimientosTitulos);
		    
		    ReporteDef MovimientosCaja = new ReporteDef();
		    MovimientosCaja.nombrePagina				= "MovimientosCaja";
		    MovimientosCaja.nombreJasper 				= "movimientosCajaPeru";
		    MovimientosCaja.nombreBinarioJasper 		= "MovimientoCajaPeruJasper";
		    MovimientosCaja.nombreProcedimiento 		= "MovimientosCaja";
		    MovimientosCaja.nombreParametroReporte 		= "subGrilla";
		    MovimientosCaja.nombreParametroDataSource	= "DataGrilla";
		    MovimientosCaja.nombreParametroInclusion	= "MCJ_incCartera";
		    MovimientosCaja.tipoSubReport 				= "S";
		    MovimientosCaja.reportePadre 				= "InfoCarteraMasterPag2";
		    MovimientosCaja.poseeSubReporteCabecera 	= "N";
		    reportes.add(MovimientosCaja);
		};
	};
};
