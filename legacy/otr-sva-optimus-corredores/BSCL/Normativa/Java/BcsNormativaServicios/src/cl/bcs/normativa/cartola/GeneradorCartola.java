package cl.bcs.normativa.cartola;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.codec.binary.Base64;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.utils.GraficadorPastel;
import cl.bcs.normativa.cartola.utils.GraficoMaster;
import cl.bcs.normativa.cartola.utils.GraficoMaster.GraficoDef;
import cl.bcs.normativa.cartola.utils.JasperUtils;
import cl.bcs.normativa.cartola.utils.Opcion;
import cl.bcs.normativa.cartola.utils.ReporteMaster;
import cl.bcs.normativa.cartola.utils.ReporteMaster.ReporteDef;
import cl.bcs.normativa.cartola.utils.TipoRecurso;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;


public class GeneradorCartola implements JavaCall{
	
	private final Log log = new Log("NOR.Cartola");
	
	//private final String SQL_Select_Configuracion = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Configuracion.sql");
	private final String SQL_Select_Plantilla = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Plantilla.sql");
	private final String SQL_Select_Recurso = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Recurso.sql");
	private final String SQL_Select_Portafolios = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Portafolios.sql");
	private final String SQL_CALL_GrabarNomina = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Call_GrabarNomina.sql");
	private final String SQL_INSERT_CLIENTES_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertClientesLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class GeneradorCartolaEntrada {
	    public Long						p_IdEmpresa;
	    public Long						p_IdUsuario;
	    public String					p_IdModulo;
	    public String 					p_FechaDesde;
	    public String 					p_FechaHasta;
	    public String					p_TipoProceso;
	    public List<objDatosEntrada>	p_Clientes;
	    
	    public GeneradorCartolaEntrada() {
			this.p_Clientes = new ArrayList<objDatosEntrada>();
		}
	    public static class objDatosEntrada {
		    public Long	 	IdCliente;
		    public Long	 	IdPortafolio;		    
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjCartolaSalida {
		public String 	FechaDesde;
	    public String 	FechaHasta;
	    public Long	 	IdCliente;
	    public Long	 	IdPortafolio;
	    public String	CartolaEncoded;
		public String 	NombreArchivo;
	}
	
	public static class GeneradorCartolaSalida {		
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public List<ObjCartolaSalida>	P_OUT_CARTOLAS;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public GeneradorCartolaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_CARTOLAS = new ArrayList<>();
		}
		
	};
	
	public static class ObjConfiguracion {
		public Long		IdConfig;
		public Long		IdEmpresa;
		public String	Descripcion;
		public String	Tipo;
		public String	Nombre;
		public String	Valor;
	};
	
	public static class ObjPlantilla {
		public Long					IdPlantilla;
	    public Long					IdEmpresa;
	    public String				Nombre;
	    public String				Vigente;
	    public java.sql.Timestamp	VigenteDesde;
	};
	
	public static class ObjRecurso {
		public Long		IdRecurso;
	    public Long		IdPlantilla;
	    public String	Editable;
	    public String	Descripcion;
	    public String	Grupo;
	    public String	Nombre;
	    public String	Tipo;
	    public String	IdEstado;
	    public String	Valor; 
	};
	
	public static class ObjParametros {
		public Long		IdParametro;
	    public Long		IdNomina;
	    public String	Nombre;
	    public String	Valor;
	};
	
	public static class ObjAdventenciasDetalle {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	};
	
	public static class ObjErroresDetalle {
		public String	Codigo;
		public String	Descripcion;
	};
	
	public static class  ObjDatosBusqueda{
		public Long		IdCliente;
		public Long		IdPortafolio;
	};
	
	public static class ObjPortafolios {
		public Long		IdCliente;
		public Long		IdPortafolio;
		public String	CodigoPortafolio;
		public String	Proposito;
	};
	
	public static class Cliente {
		public Long IdCliente;
		public Long IdTrabajo;
	}
		
	public GeneradorCartolaSalida logica(Connection con, GeneradorCartolaEntrada in) throws Exception {
		GeneradorCartolaSalida out = new GeneradorCartolaSalida();		
		SQL sql = new SQL();		
		log.write("== INICIO LOGICA ==");
		
		Long idTrabajo =  Double.valueOf(Math.random()*10000000000L).longValue();
		
		try {
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");			
			
			//Obtención de Configuración
			//HashMap<String,Object> paramsConfiguracion = new HashMap<>();
			//paramsConfiguracion.put("IdEmpresa",in.p_IdEmpresa);
			//List<ObjConfiguracion> Configuracion = sql.query(con, SQL_Select_Configuracion,paramsConfiguracion,ObjConfiguracion.class);
			
			//Obtención de Plantilla
			HashMap<String,Object> paramsPlantilla = new HashMap<>();
			paramsPlantilla.put("IdEmpresa",in.p_IdEmpresa);
			paramsPlantilla.put("FechaDesde",in.p_FechaDesde);
			List<ObjPlantilla> Plantilla = sql.query(con, SQL_Select_Plantilla,paramsPlantilla,ObjPlantilla.class);			
			
			//Obtención de Recurso
			HashMap<String,Object> paramsRecurso = new HashMap<>();
			for (ObjPlantilla det: Plantilla){				
				paramsRecurso.put("IdPlantilla",det.IdPlantilla);
				log.write("Plantilla "+det.IdPlantilla);
			};
			List<ObjRecurso> Recursos = sql.query(con, SQL_Select_Recurso,paramsRecurso,ObjRecurso.class);
			HashMap<String,Object> LHRecurso = Utils.List2Hash(Recursos, "Nombre");
			
			//Grabar Nomina del proceso
			Long IdNomina = GrabarNomina(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaDesde, in.p_FechaHasta, 0L, "EN_PROCESO",in.p_TipoProceso, con);
			
			//Determina si la configuración indica que es plantilla consolidada	        
	        Boolean showCarteraConsolidada = false;
	        for (ObjRecurso p : Recursos){
	        	if (p.Nombre.equals("showCarteraConsolidada") && p.Valor.equals("SI") ){
	        		showCarteraConsolidada = true;
	        	}
	        };
	        
	        List<Long> idsCliente = in.p_Clientes.stream().map(cliente->cliente.IdCliente).distinct().collect(Collectors.toList());
	        
	        List<Cliente> clientesBusqueda = idsCliente.stream()
	        		.map(cliente->{
	        			Cliente clienteBusqueda = new Cliente();
	        			clienteBusqueda.IdCliente = cliente;
	        			clienteBusqueda.IdTrabajo = idTrabajo;
	        			return clienteBusqueda;
	        		})
	        		.collect(Collectors.toList());
	        
	        sql.batch(con, SQL_INSERT_CLIENTES_LEGACY, clientesBusqueda, Cliente.class);
	        
	        //Obtiene los portafolios a buscar
	        List<ObjDatosBusqueda> datosBusquedaServicio = new ArrayList<ObjDatosBusqueda>();
	        if (showCarteraConsolidada){	        	
	        	HashMap<String,Object> paramsPortafolio = new HashMap<>();
	        	paramsPortafolio.put("IdTrabajo", idTrabajo);
				List<ObjPortafolios> Portafolios = sql.query(con, SQL_Select_Portafolios,paramsPortafolio,ObjPortafolios.class);
	        	
				for (ObjPortafolios pf : Portafolios){
					ObjDatosBusqueda p = new ObjDatosBusqueda();
		        	p.IdCliente = pf.IdCliente;
		        	p.IdPortafolio = pf.IdPortafolio; 
		        	datosBusquedaServicio.add(p);
				};
	        } else {
	        	for (GeneradorCartolaEntrada.objDatosEntrada CLI : in.p_Clientes){
	        		ObjDatosBusqueda p = new ObjDatosBusqueda();
		        	p.IdCliente 	= CLI.IdCliente;
		        	p.IdPortafolio 	= CLI.IdPortafolio;
		        	datosBusquedaServicio.add(p);
	        	};
	        };
				        
	        //LLama a los servicios configurados en base a los portafolios obtenidos
	        if (datosBusquedaServicio.size() > 0){
	        	ReporteMaster reporteMaster = new ReporteMaster(localizacionSistema);
				GraficoMaster graficoMaster = new GraficoMaster(localizacionSistema);
	        	
				HashMap<String,ArrayList<HashMap>> ServiceResultSet = ResultSet (in.p_IdEmpresa,in.p_IdUsuario,in.p_IdModulo,in.p_FechaDesde,in.p_FechaHasta,
																				 datosBusquedaServicio,Recursos,reporteMaster,graficoMaster,con);
				
				
				//Estructurar parametros para impresión en jasper
				HashMap<String, Object> Param = new HashMap<String, Object>();				
				for (ObjRecurso R : Recursos) {
					if (!R.Tipo.equals("jasper") && !R.Tipo.equals("sp")){
						Param.put(R.Nombre, getValorAsObject(R.Valor, R.Tipo));
					};					
				};
				
				
				//Obtencion configuracion Subreport				
				HashMap<String, List<String>> SubReportes = SubReportes(localizacionSistema);
				HashMap<String,HashMap<String, Object>> SubReport = SubReports(localizacionSistema);
				for (ReporteDef RM_ : reporteMaster.getReporteMaster()){							
					ObjRecurso Jasper = (ObjRecurso) LHRecurso.get( RM_.nombreBinarioJasper );
					ObjRecurso ParametroInclusion = (ObjRecurso) LHRecurso.get( RM_.nombreParametroInclusion );
					String Inclusion = "";
					
					if (RM_.reportePadre == null || RM_.tipoSubReport.equalsIgnoreCase("N"))
						continue;
					
					if (ParametroInclusion != null){
						Inclusion = ParametroInclusion.Valor;
					};				
					
					HashMap<String, Object> SR = SubReport.get( KeyReporte(RM_.poseeSubReporteCabecera,RM_.reportePadre,RM_.nombrePagina) );
					SR.put(RM_.nombrePagina, RM_.nombrePagina);
					SR.put(RM_.nombreProcedimiento, RM_.nombreProcedimiento);
					SR.put(RM_.nombreParametroInclusion, Inclusion);
					if (Jasper != null){
						SR.put(RM_.nombreParametroReporte, loadJasperReport(Jasper.Valor));
					};
					SR.put(RM_.nombreParametroDataSource, new HashMap<>());
				};
				for (GraficoDef GM_ : graficoMaster.getGraficoMaster()){
					if (GM_.reportePadre == null)
						continue;
										
					HashMap<String, Object> SR = SubReport.get( KeyReporte("S",GM_.reportePadre,GM_.nombreGrafico) );					
					SR.put(GM_.nombreProcedimiento, GM_.nombreProcedimiento);
					SR.put(GM_.nombreParametroReporte, new HashMap<>());
				};
				
				
				//Agregar Binarios y parametros por cabeceras
				HashMap<String,JasperReport> JasperBinarios = new HashMap<String,JasperReport>();				
				HashMap<String,HashMap<String, HashMap<String, Object>>> JasperParametros = new HashMap<String,HashMap<String, HashMap<String, Object>>>();
				ArrayList<ReporteDef> JasperBinariosOrdenados = new ArrayList<ReporteDef>();
				List<JasperPrint> jpList = new ArrayList<JasperPrint>();
				for (ReporteDef RM : reporteMaster.getReporteMaster()) {
					HashMap<String, HashMap<String, Object>> Parameters = new HashMap<String, HashMap<String, Object>>();
					ObjRecurso Inclusion = (ObjRecurso) LHRecurso.get( RM.nombreParametroInclusion );
					
					if (RM.tipoSubReport.equals("S"))
						continue;
					
					if (Inclusion != null && Inclusion.Valor.equals("NO"))
						continue;
					
					for (ObjRecurso R : Recursos) {
						if (R.Tipo.equals("jasper")){
							if (R.Nombre.equalsIgnoreCase( RM.nombreBinarioJasper )){
								JasperBinarios.put(RM.nombrePagina , loadJasperReport(R.Valor));								
							};
						};
					};
									
					if (SubReportes.containsKey(RM.nombrePagina)){
						List<String> SRM = SubReportes.get(RM.nombrePagina);
						for (String K : SRM) {							
							String Key = KeyReporte(RM.poseeSubReporteCabecera,RM.nombrePagina,K);
							Parameters.put(Key, SubReport.get(Key) );							
						};
					};

					JasperParametros.put(RM.nombrePagina, Parameters);
					JasperBinariosOrdenados.add(RM);
		        };
		        
		        
		        //Se agregan páginas segun cliente y portafolio
		        ArrayList<Long> clientesProcesados = new ArrayList<>();
		        String namePDF = "";
				for (ObjDatosBusqueda  dbs : datosBusquedaServicio){
					String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
					namePDF = localizacionSistema+tFechaHoraSistema.replace("-", "").substring(0, 8) +"_"+ dbs.IdCliente +".pdf";
					
					for (ReporteDef JBO : JasperBinariosOrdenados) {
						JasperReport jasper = JasperBinarios.get(JBO.nombrePagina);
						HashMap<String, HashMap<String, Object>> jParam =  JasperParametros.get(JBO.nombrePagina);						
						
						for (String key : jParam.keySet()) {
							HashMap<String, Object> ParametroSubReporte = new HashMap<>(jParam.get(key)); 
							Boolean existsData = false;
							Long IdPortafolio = 0L;
							
							if (!JBO.reporteConsolidado.equals("S"))
								IdPortafolio = dbs.IdPortafolio;
							
							String KeySubReport = key;
							if ( KeySubReport.contains("_") )
								KeySubReport = key.substring(key.indexOf("_")+1,key.length());
														
							for (ReporteDef RM : reporteMaster.getReporteMaster()) {
								if ( !ParametroSubReporte.containsKey( RM.nombrePagina) )
									continue;
								if (!KeySubReport.equals(JBO.nombrePagina) && !RM.nombrePagina.equals(KeySubReport) )
									continue;
								
								existsData = ServiceResultSet.containsKey(ParametroSubReporte.get(RM.nombrePagina)+"-"+ParametroSubReporte.get(RM.nombreProcedimiento)+"-" +dbs.IdCliente+"-"+IdPortafolio);								
								List<DynaBean> datos_ = formatData(ServiceResultSet.get(ParametroSubReporte.get(RM.nombrePagina)+"-"+ParametroSubReporte.get(RM.nombreProcedimiento)+"-"+dbs.IdCliente+"-"+IdPortafolio));
						        JRDataSource DS = new JRBeanCollectionDataSource(datos_);
						        ParametroSubReporte.put(RM.nombreParametroDataSource, DS);
							};
							for (GraficoDef GM : graficoMaster.getGraficoMaster()) {
								if ( !ParametroSubReporte.containsKey( GM.nombreGrafico) )
									continue;
								if (!KeySubReport.equals(JBO.nombrePagina) && !GM.nombreGrafico.equals(KeySubReport) )
									continue;
								
								List<DynaBean> datos_ = formatData(ServiceResultSet.get(ParametroSubReporte.get(GM.nombreGrafico)+"-"+ParametroSubReporte.get(GM.nombreProcedimiento)+"-" + dbs.IdCliente+"-"+IdPortafolio));
								ParametroSubReporte.put(GM.nombreParametroReporte, createGraficoPorProducto(GM.nombreGrafico, datos_, ""));
							};
							
							//Agrega paginas a la cartola
							if (existsData && ((JBO.reporteConsolidado.equals("S") && !clientesProcesados.contains(dbs.IdCliente)) ||  JBO.reporteConsolidado.equals("N")) ){
								ParametroSubReporte.put("parametros", Param);
								
								//Setea resultSet con la informacion del cliente
								List<DynaBean> _datos = formatData(ServiceResultSet.get(JBO.nombrePagina+"-"+"InfoCarteraMaster"+"-"+dbs.IdCliente+"-"+dbs.IdPortafolio));
								JRDataSource datos = new JRBeanCollectionDataSource(_datos);
								JasperPrint pagina = JasperFillManager.fillReport(jasper, ParametroSubReporte, datos);
				                jpList.add(pagina);
							};

						};// END FOR JasperParametros						
					}; // END FOR JasperBinariosOrdenados
					
					if (!clientesProcesados.contains(dbs.IdCliente)) {
			        	clientesProcesados.add(dbs.IdCliente);
			        };
				};				
				
				//Paginación pdf
				if (Param.get("Title_FormatoPaginacion") != null)
					JasperUtils.setPageNumbers(jpList, "paginacion",Param.get("Title_FormatoPaginacion").toString() );
							
				//Generación archivo pdf
				try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
					String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
					tFechaHoraSistema.replace("-", "").substring(0, 8);
					
					System.out.println("== GENERAR PDF ==");
					
					for (ObjDatosBusqueda  dbs : datosBusquedaServicio){
						if (jpList.size() > 0){
							final JRPdfExporter exporter = new JRPdfExporter();
							exporter.setExporterInput(SimpleExporterInput.getInstance(jpList));
							exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os));
							exporter.exportReport();
							final byte[] bytes = os.toByteArray();
							//IOUtils.write(bytes, new FileOutputStream(new File(namePDF)));//Solo ejecucion local
							
							ObjCartolaSalida cartolaSalida = new ObjCartolaSalida();
							cartolaSalida.CartolaEncoded = new String(Base64.encodeBase64(bytes));
							cartolaSalida.FechaDesde = in.p_FechaDesde;
							cartolaSalida.FechaHasta = in.p_FechaHasta;
							cartolaSalida.IdCliente = dbs.IdCliente;
							cartolaSalida.IdPortafolio = dbs.IdPortafolio;
							cartolaSalida.NombreArchivo = namePDF;
							out.P_OUT_CARTOLAS.add(cartolaSalida);
						} else {
							out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "CARTOLA", "Sin Informacion para el cliente "+dbs.IdCliente.toString()));
						};
					};
				};
				
				
				//Actualizar estado de la nomina
				GrabarNomina(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaDesde, in.p_FechaHasta, IdNomina, "TERMINADO", in.p_TipoProceso, con);
				
	        } else {
	        	GrabarNomina(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaDesde, in.p_FechaHasta, IdNomina, "ERROR", in.p_TipoProceso, con);
	        };
		}
		finally {
			Cliente clienteBusqueda = new Cliente();
			clienteBusqueda.IdTrabajo = idTrabajo;
			List<Cliente> clientesBusqueda = Arrays.asList(clienteBusqueda);
			sql.batch(con, SQL_DELETE_PORTAFOLIOS_LEGACY, clientesBusqueda, Cliente.class);
		}		
		log.write("== FIN LOGICA ==");
		return out;		
	};
	
	
	public HashMap<String, List<String>> SubReportes(String localizacionSistema){
		HashMap<String, List<String> > SubReport = new HashMap<String, List<String> >();
		ReporteMaster reporteMaster = new ReporteMaster(localizacionSistema);
		GraficoMaster graficoMaster = new GraficoMaster(localizacionSistema);
		
		for (ReporteDef RM : reporteMaster.getReporteMaster()){
			List<String> Report = new ArrayList<String>();
			
			if (RM.tipoSubReport.equalsIgnoreCase("S"))
				continue;
			
			for (ReporteDef SR : reporteMaster.getReporteMaster()){
				if (SR.reportePadre == null || SR.tipoSubReport.equalsIgnoreCase("N"))
					continue;
				
				if (SR.reportePadre.equals(RM.nombrePagina) ){
					Report.add(SR.nombrePagina);
				}				
			};
			
			for (GraficoDef GM : graficoMaster.getGraficoMaster()) {
				if (GM.reportePadre == null)
					continue;
				
				if (GM.reportePadre.equals(RM.nombrePagina) ){
					Report.add(GM.nombreGrafico);
				}
			};
			
			SubReport.put(RM.nombrePagina, Report);
		};
			
		return SubReport;
	};
	
	public HashMap<String,HashMap<String, Object>> SubReports(String localizacionSistema) {
		HashMap<String,HashMap<String, Object>> SubReport = new HashMap<String,HashMap<String, Object>>();
		HashMap<String, List<String>> SubReportes = SubReportes(localizacionSistema);
		ReporteMaster reporteMaster = new ReporteMaster(localizacionSistema);
		
		for (ReporteDef RM_ : reporteMaster.getReporteMaster()){					
			if (RM_.tipoSubReport.equalsIgnoreCase("S"))
				continue;
			
			HashMap<String, Object> SR = SubReport.get(RM_.nombrePagina);
			if (SR == null) {				
				if (SubReportes.containsKey(RM_.nombrePagina)){
					List<String> SR_ = SubReportes.get(RM_.nombrePagina);
					for (String K : SR_) {						
						SubReport.put( KeyReporte(RM_.poseeSubReporteCabecera,RM_.nombrePagina,K) , new HashMap<String, Object>());
					};
				};
			};
		};
		
		return SubReport;
	};
	
	public String KeyReporte(String EsCabecera, String NameReporte, String NameSubReporte){
		String Key = "";
		
		if (EsCabecera.equals("S") ){
			Key = NameReporte;
		} else {
			Key = NameReporte +"_"+ NameSubReporte;
		};
		
		return Key;		
	};
	
	public Long GrabarNomina(Long IdEmpresa, Long IdUsuario, String IdModulo, String FechaDesde, String FechaHasta, Long IdNomina, String Estado, String TipoProceso, Connection con) throws SQLException{
		SQL sql = new SQL();
		
		HashMap<String,Object> parmsInGrabraNomina =new HashMap<String,Object>();
		parmsInGrabraNomina.put("p_IdEmpresa", IdEmpresa);
		parmsInGrabraNomina.put("p_IdUsuario", IdUsuario);
		parmsInGrabraNomina.put("p_IdModulo", IdModulo);
		parmsInGrabraNomina.put("p_IdNomina", 0L);
		parmsInGrabraNomina.put("p_Periodo", null);
		parmsInGrabraNomina.put("p_FechaDesde", FechaDesde);
		parmsInGrabraNomina.put("p_FechaHasta", FechaHasta);
		parmsInGrabraNomina.put("p_Comentario", null);
		parmsInGrabraNomina.put("p_Certificada", "NO");
		parmsInGrabraNomina.put("p_FechaCertificacion", null);
		parmsInGrabraNomina.put("p_IdUsuarioCertificador", null);
		parmsInGrabraNomina.put("p_NombreUsuarioCertificador", null);
		parmsInGrabraNomina.put("p_NotaCertificacion", null);			
		parmsInGrabraNomina.put("p_TipoProceso", TipoProceso);
		parmsInGrabraNomina.put("p_EstadoProceso", Estado);
		parmsInGrabraNomina.put("p_SegundosEsperaProceso", null);
		parmsInGrabraNomina.put("p_MensajeProceso", null);
		
		HashMap<String,Class> parmsOutGrabraNomina =new HashMap<String,Class>();
		parmsOutGrabraNomina.put("p_out_id", Long.class);
		parmsOutGrabraNomina.put("p_out_AdvertenciasContador", Long.class);
		parmsOutGrabraNomina.put("p_out_AdvertenciasDetalle", ObjAdventenciasDetalle.class);
		parmsOutGrabraNomina.put("p_out_ErroresContador", Long.class);
		parmsOutGrabraNomina.put("p_out_ErroresDetalle", ObjErroresDetalle.class);			
		HashMap<String,Object> rptGrabarNomina = sql.call(con, SQL_CALL_GrabarNomina, parmsInGrabraNomina, parmsOutGrabraNomina);
		
		return (Long) rptGrabarNomina.get("p_out_id");
	};
	
	public HashMap<String,ArrayList<HashMap>> ResultSet (Long IdEmpresa, Long IdUsuario, String IdModulo, String FechaDesde, String FechaHasta,
			List<ObjDatosBusqueda> datosBusquedaServicio, List<ObjRecurso> Recursos, ReporteMaster reporteMaster,GraficoMaster graficoMaster,Connection con)
					throws Exception{
		SQL sql = new SQL();
		HashMap<String,ArrayList<HashMap>> ServiceResultSet = new HashMap<>();
		
		
		HashMap<String, HashMap<String, Object>> procedimientos =new HashMap<String, HashMap<String, Object>>();
		for (ReporteDef RM : reporteMaster.getReporteMaster()){
			HashMap<String, Object> Datos =new HashMap<String, Object>();
			Datos.put("nombreProcedimiento", RM.nombreProcedimiento);
			Datos.put("filtroUbicacionBoveda", RM.filtroUbicacionBoveda);
			Datos.put("filtroSoloMonedaSistema", RM.filtroSoloMonedaSistema);
			procedimientos.put(RM.nombrePagina, Datos);
		};		
		for (GraficoDef GM : graficoMaster.getGraficoMaster()) {
			HashMap<String, Object> Datos =new HashMap<String, Object>();
			Datos.put("nombreProcedimiento", GM.nombreProcedimiento);
			procedimientos.put(GM.nombreGrafico, Datos);
		};
		
		try{
			SQL.exec(con,
					"CREATE LOCAL TEMPORARY TABLE #CallServicio_DatosBusqueda (\"IdCliente\" BIGINT,\"IdPortafolio\" BIGINT)");

			sql.batch(con,
					"INSERT INTO #CallServicio_DatosBusqueda (\"IdCliente\",\"IdPortafolio\") VALUES (:IdCliente,:IdPortafolio)", datosBusquedaServicio, ObjDatosBusqueda.class);
			
			
			for (String key : procedimientos.keySet()){
				HashMap<String, Object> Servicios = new HashMap<>(procedimientos.get(key));
				
				HashMap<String,Object> parmsInSP =new HashMap<String,Object>();
				parmsInSP.put("IdEmpresa", IdEmpresa);
				parmsInSP.put("IdUsuario", IdUsuario);
				parmsInSP.put("IdModulo", IdModulo);				
				parmsInSP.put("FechaDesde", FechaDesde);
				parmsInSP.put("FechaHasta", FechaHasta);
				parmsInSP.put("p_Datos", datosBusquedaServicio);
				
				//Filtros propios de servicio
				if (Servicios.get("filtroUbicacionBoveda") != null)
					parmsInSP.put("p_FiltroUbicacionBoveda", Servicios.get("filtroUbicacionBoveda"));
				if (Servicios.get("filtroSoloMonedaSistema") != null)
					parmsInSP.put("p_FiltroSoloMonedaSistema", Servicios.get("filtroSoloMonedaSistema"));
				
		    	
				for (ObjRecurso R : Recursos) {
					HashMap<String, Object> rpt = new HashMap<String, Object>();
					if(R.Tipo.equals("java") && R.Nombre.equals(Servicios.get("nombreProcedimiento"))){
						log.write(R.Valor+",  Reporte-"+key+",  Servicio-"+R.Nombre+",  filtroUbicacionBoveda-"+procedimientos.get(key).get("filtroUbicacionBoveda")+",  filtroSoloMonedaSistema-"+procedimientos.get(key).get("filtroSoloMonedaSistema"));
						Class<?> tclass = null;
						try {
							tclass=Class.forName(R.Valor);
						}
						catch(Exception e) {
							log.write("Error al llamar clase::",e);
							continue;
						}
						BcsCartola servicio = (BcsCartola) tclass.newInstance();
						EntradaCartola entradaCartola = new EntradaCartola();
						entradaCartola.p_IdModulo = (String) parmsInSP.get("IdModulo");
						entradaCartola.p_IdEmpresa = (Long) parmsInSP.get("IdEmpresa");
						entradaCartola.p_IdUsuario = (Long) parmsInSP.get("IdUsuario");
						if (Servicios.get("filtroSoloMonedaSistema") != null)
							entradaCartola.p_FiltroSoloMonedaSistema = (String) parmsInSP.get("p_FiltroSoloMonedaSistema");
						entradaCartola.p_FechaDesde = Fecha.toDate((String) parmsInSP.get("FechaDesde"));
						entradaCartola.p_FechaHasta = Fecha.toDate((String) parmsInSP.get("FechaHasta"));
						for(ObjDatosBusqueda busqueda : datosBusquedaServicio){
							BcsCartola.Dato dato = new BcsCartola.Dato();
							dato.IdCliente = busqueda.IdCliente;
							dato.IdPortafolio = busqueda.IdPortafolio;
							entradaCartola.p_Datos.add(dato);
						}
						SalidaCartola salidaCartola = servicio.procesar(con, entradaCartola);
						rpt = salidaCartola.P_OUT_SALIDA;
					}
					else if (R.Tipo.equals("sp") && R.Nombre.equals(Servicios.get("nombreProcedimiento")) ){
						System.out.println(R.Valor+",  Reporte-"+key+",  Servicio-"+R.Nombre);
						String FiltroUbicacion = "";
						if (Servicios.get("filtroUbicacionBoveda") != null)
							FiltroUbicacion = "p_FiltroUbicacionBoveda=>:p_FiltroUbicacionBoveda,";
						
						String queryCall = "CALL \""+ R.Valor +"\" "
				        		+ "(p_IdModulo=>:IdModulo,"
				        		+ "p_IdUsuario=>:IdUsuario,"
				        		+ "p_IdEmpresa=>:IdEmpresa,"
				        		+ "p_FechaDesde=>:FechaDesde,"
				        		+ "p_FechaHasta=>:FechaHasta,"
				        		+ FiltroUbicacion
				        		+ "p_Datos=>#CallServicio_DatosBusqueda)";						
						
						
						rpt = sql.call(con, queryCall, parmsInSP, new HashMap<>());
					}
					else{
						continue;
					}
					for (Object rst : rpt.values()) {								
						ArrayList rs = (ArrayList) rst;
						for (Object _fila : rs) {
							HashMap fila = (HashMap) _fila;
							
							Long idCliente = (Long) fila.get("IdCliente");
							Long idPortafolio = (Long) fila.get("IdPortafolio");
							
							if (idCliente == null)
								continue;
							
							ArrayList _lista = ServiceResultSet.get(key+"-"+R.Nombre+"-"+idCliente+"-"+idPortafolio);
							if (_lista == null) {
								_lista = new ArrayList<>();
								ServiceResultSet.put(key+"-"+R.Nombre+"-"+idCliente+"-"+idPortafolio, _lista);
							}
							_lista.add(fila);
						}
					};
				};
			};
		} finally {
			SQL.exec(con, "DROP TABLE #CallServicio_DatosBusqueda");
		};

		return ServiceResultSet;
	};
	
	public List<DynaBean> formatData(List<HashMap> data) {
	    List<DynaBean> resultado = new ArrayList<>();
	    if (data != null) {
		    for (HashMap h : data) {
		      LazyDynaBean d = new LazyDynaBean();
		      Object[] nombres = h.keySet().toArray();
		      Object[] fila = h.values().toArray();
		      
		      for (int i = 0; i < nombres.length; i++) {
		        d.set(nombres[i].toString(), fila[i]);
		      }		      
		      resultado.add(d);		      
		    }
	    }
	    
	    return resultado;
	};
	
	public Object getValorAsObject(String valor, String tipo) {
        if (valor == null) {
            return null;
        }

        if (TipoRecurso.OPCION.equals(tipo)) {
            return Opcion.SI.equals(valor);
        }

        if (TipoRecurso.NUMERO.equals(tipo)) {
            return Integer.parseInt(valor);
        }

        if (TipoRecurso.IMAGEN.equals(tipo)) {
            try {
                return getBufferedImage(valor);
            } catch (Exception ex) {
                return null;
            }
        }
        return valor;
    };
    
    public BufferedImage createGraficoPorProducto(String nombreGrafico,List<DynaBean> data, String titulo){
    	final Map<String, BigDecimal> datosGrafico = new ConcurrentHashMap();
        for (final DynaBean bean : data) {
            final String key = (String) bean.get("grupo");
            final BigDecimal value = (BigDecimal) bean.get("totPorcActivo");
            if (key != null) {
            	datosGrafico.put(key, value);	
            }
            else {
            	System.out.println("aasdfasdf");
            }
            
        }

        return GraficadorPastel.graficar(datosGrafico, titulo);
    };    
    
    public static JasperReport loadJasperReport(
            final String base64String)
                    throws IOException,
                    JRException {
        final byte[] bytes = Base64.decodeBase64(base64String);
        try (final InputStream is = new ByteArrayInputStream(bytes)) {
            return (JasperReport) JRLoader.loadObject(is);
        }
    }
    
    public static BufferedImage getBufferedImage(
            final String valor)
                    throws IOException {
        final byte[] bytes = Base64.decodeBase64(valor);
        try (final InputStream in = new ByteArrayInputStream(bytes)) {
            // ADVERTENCIA: Esto no soporta JPG.
            return ImageIO.read(in);
        }
    };	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		GeneradorCartolaEntrada jin=gson.fromJson(in.toString() ,GeneradorCartolaEntrada.class);
		GeneradorCartolaSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		//try (Connection con = PlataformaUtils.getDESAConnection()) {
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
		//try (Connection con = PlataformaUtils.getQAPEConnection()) {
		//try (Connection con = PlataformaUtils.getMBConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":69,\"p_FechaDesde\":\"2017-11-01\",\"p_FechaHasta\":\"2017-11-08\", "
						+ "\"p_Clientes\": [ {\"IdCliente\":174994,\"IdPortafolio\":40739} ], "
						+ "\"p_TipoProceso\":\"ONLINE\"}";
				
				
				
				GeneradorCartolaEntrada Objin = gson.fromJson(json, GeneradorCartolaEntrada.class);
								
				GeneradorCartola  kk = new GeneradorCartola();
				kk.logica(con, Objin);
				con.commit();
				con.close();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END MAIN ==");
	};
}
