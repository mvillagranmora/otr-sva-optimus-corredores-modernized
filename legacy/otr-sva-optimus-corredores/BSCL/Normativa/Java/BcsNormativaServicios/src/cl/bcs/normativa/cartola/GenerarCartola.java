package cl.bcs.normativa.cartola;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.SystemOutLogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.GenerarCartolaPDF.GenerarCartolaPDF_Salida;
//import cl.bcs.normativa.cartola.jobs.GenerarCartolasJob;
//import cl.bcs.normativa.cartola.jobs.GenerarCartolasJob.ObjNomina;
import cl.bcs.normativa.cartola.utils.GraficadorPastel;
import cl.bcs.normativa.cartola.utils.GraficoMaster;
import cl.bcs.normativa.cartola.utils.GraficoMaster.GraficoDef;
import cl.bcs.normativa.cartola.utils.JasperUtils;
import cl.bcs.normativa.cartola.utils.Opcion;
import cl.bcs.normativa.cartola.utils.ReporteMaster;
import cl.bcs.normativa.cartola.utils.ReporteMaster.ReporteDef;
import cl.bcs.normativa.cartola.utils.TipoRecurso;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;


public class GenerarCartola implements JavaCall{
	static final Logger logger = LogManager.getLogger(GenerarCartola.class.getName());

	//Configuracion del virtualizer de Jasper (paginado a disco del modelo de impresion).
	//Paginas que se mantienen en heap por cartola antes de volcar a disco.
	private static final int VIRTUALIZER_PAGINAS_EN_MEMORIA  = 50;
	//Tamano del bloque del swap file, en bytes.
	private static final int VIRTUALIZER_BLOQUE_BYTES        = 4096;
	//Bloques que se agregan al swap file cada vez que necesita crecer.
	private static final int VIRTUALIZER_CRECIMIENTO_BLOQUES = 100;
	private final String SQL_Select_Plantilla = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Plantilla.sql");
	private final String SQL_Select_Recurso = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Recurso.sql");
	private final String SQL_Select_Portafolios = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Portafolios.sql");
	private final String SQL_Select_Ultima_Plantilla = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_Select_Ultima_Plantilla.sql");
	private final String SQL_Insert_Token = loadResourceToString("cl/bcs/normativa/cartola/sql/SQL_Insert_Token.sql");
	private final String SQL_INSERT_CLIENTES_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertClientesLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	private final String SQL_INSERT_NOMINA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertNominaPaso.sql");
	private final String SQL_DELETE_NOMINA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeleteNominaPaso.sql");
	
	private final String SQL_DELETE_PROCESOS_EJECUTADOS_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_DeleteProcesoEjecutado.sql");
	private final String SQL_DELETE_RESUMEN_INVERSION_BASE_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_DeleteOutServicioEjecutado.sql");
	private final String SQL_DELETE_CARTERA_RV_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRV_DeleteOutServicioEjecutado.sql");
	private final String SQL_DELETE_INFO_CARTERA_MASTER_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/InfoCarteraMaster_DeleteOutServicioEjecutado.sql");
	private final String SQL_DELETE_CARTERA_RF_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRF_DeleteOutServicioEjecutado.sql");
	private final String SQL_DELETE_CARTERA_IF_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraIF_DeleteOutServicioEjecutado.sql");
	
	private final String SQL_SELECT_ORDEN_ITEM_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/GenerarCartola_SelectItemOrdenCartola.sql");
			

	
	
	public static class GenerarCartolaEntrada {
	    public Long						p_IdEmpresa;
	    public Long						p_IdUsuario;
	    public String					p_IdModulo;
	    public String 					p_FechaDesde;
	    public String 					p_FechaHasta;
	    public String 				    p_FechaHastaOrigen;
	    public Long						p_IdNomina;
	    public String					p_TipoProceso;
	    public String					p_CartolaConsolidada;
	    public List<objDatosEntrada>	p_Clientes;
	    
	    public GenerarCartolaEntrada() {
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
	    public byte[]  	CartolaEncodedByte;
		public String 	NombreArchivo;
	}
	
	public static class Token{
		public String IdTokenSesion;
		public Long   IdUsuario;
		public String TokenValido;
		public Date FechaUltimoUso;
		public int UsoToken;
		public Date FechaIngreso;
		public Date FechaSalida;
	}

	public static class GenerarCartolaSalida {		
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public List<ObjCartolaSalida>	P_OUT_CARTOLAS;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public GenerarCartolaSalida() {
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
	    public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class OrdenItem {
		public String ItemCartola;
		public Long Orden;
	}
	
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
	    public String toString() {
			return Utils.printJson(this, false);
		}
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
		public String  CodigoPortafolio;
		public String  DocumentoIdentidad;
	};
	
	public static class ObjPortafolios {
		public Long		IdCliente;
		public Long		IdPortafolio;
		public String	CodigoPortafolio;
		public String	Proposito;
		public String DocumentoIdentidad;
	};
	
	public static class Cliente {
		public Long IdCliente;
		public Long IdTrabajo;
	}
	
	public static class NominaPaso {
		public Long IdNomina;
		
		public NominaPaso() {
			this.IdNomina = 0L;
		}
	}
	
	public static class ServicioEjecutado{
		public Long IdNomina;
		public Long IdTrabajo;
		public String Recurso;
		public String Entrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public GenerarCartolaSalida logica(Connection con, GenerarCartolaEntrada in) throws Exception {
		// Modo de salida: define que representaciones del PDF se retienen en memoria.
		//   ONLINE                    -> la UI consume CartolaEncoded (Base64); es siempre 1 cliente.
		//   Otro TipoProceso no nulo  -> se arma el .zip, hay que retener los PDF en 'pdfs'.
		//   TipoProceso nulo (batch)  -> ninguna de las dos: GenerarCartolasJob solo lee
		//                                CartolaEncodedByte para comprimir y persistir en BD.
		// Retener las tres representaciones a la vez es lo que agota el heap en nominas grandes.
		final boolean esOnline  = "ONLINE".equals(in.p_TipoProceso);
		final boolean generaZip = in.p_TipoProceso != null && !esOnline;

		Set<Long> clientesProcesados = new HashSet<Long>();
        HashMap<String,byte[]> pdfs = new HashMap<String,byte[]>();
        HashMap<String,Object> paramsNomina = new HashMap<String,Object>();	
		GenerarCartolaSalida out = new GenerarCartolaSalida();
		GenerarCartolaPDF cartolaconsolidada = new GenerarCartolaPDF();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		
		if(in.p_FechaHastaOrigen == null) {
			in.p_FechaHastaOrigen = in.p_FechaHasta;
			
		}
		
		Long idTrabajo =  Double.valueOf(Math.random()*10000000000L).longValue();
		
		NominaPaso nominaPaso = new NominaPaso();
		nominaPaso.IdNomina = in.p_IdNomina;
		List<NominaPaso> nominasPaso = new ArrayList();
        nominasPaso.add(nominaPaso);
        
		try {
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);	
			//Obtención de Plantilla
			HashMap<String,Object> paramsPlantilla = new HashMap<>();
			paramsPlantilla.put("IdEmpresa",in.p_IdEmpresa);
			paramsPlantilla.put("FechaDesde",in.p_FechaHasta);
			List<ObjPlantilla> Plantilla = sql.query(con, SQL_Select_Plantilla,paramsPlantilla,ObjPlantilla.class);

			if(null == Plantilla || Plantilla.isEmpty()) {
				paramsPlantilla = new HashMap<>();
				paramsPlantilla.put("IdEmpresa",in.p_IdEmpresa);
				Plantilla = sql.query(con, SQL_Select_Ultima_Plantilla,paramsPlantilla,ObjPlantilla.class);
			}
			//Obtención de Recurso
			HashMap<String,Object> paramsRecurso = new HashMap<>();
			for (ObjPlantilla det: Plantilla){				
				paramsRecurso.put("IdPlantilla",det.IdPlantilla);
			};
			List<ObjRecurso> Recursos = sql.query(con, SQL_Select_Recurso,paramsRecurso,ObjRecurso.class);
			HashMap<String,Object> LHRecurso = Utils.List2Hash(Recursos, "Nombre");
			//Determina si la configuración indica que es plantilla consolidada	        
	        Boolean showCarteraConsolidada = false;
	        for (ObjRecurso p : Recursos){
	        	if (p.Nombre.equals("showCarteraConsolidada") && p.Valor.equals("SI") ){
	        		showCarteraConsolidada = true;
	        	}
	        };
	         
	        // ORDEN DE ITEMS DE DETALLE
	        List<OrdenItem> ordenItem = new ArrayList<OrdenItem>();
	        try {
	        	ordenItem = sql.query(con, SQL_SELECT_ORDEN_ITEM_CARTOLA,new HashMap<String,Object>(),OrdenItem.class);
	        }catch(Exception e) {
	        	logger.info("SQL_SELECT_ORDEN_ITEM_CARTOLA "+e);
	        }
	        
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
	        
	       

	        
	        try {
	        	sql.batch(con, SQL_INSERT_NOMINA_PASO, nominasPaso, NominaPaso.class);
			} catch (Exception e) {
				logger.info("SQL_INSERT_NOMINA_PASO "+e);
			}
	        
	        
	        //Obtiene los portafolios a buscar
	        List<ObjDatosBusqueda> datosBusquedaServicio = new ArrayList<ObjDatosBusqueda>();
	        if (showCarteraConsolidada){	        	
	        	HashMap<String,Object> parametrosPortafolios = new HashMap<>(2);
	        	parametrosPortafolios.put("IdTrabajo", idTrabajo);
	        	
				List<ObjPortafolios> Portafolios = sql.query(con, SQL_Select_Portafolios,parametrosPortafolios,ObjPortafolios.class);
	        	
				for (ObjPortafolios pf : Portafolios){
					ObjDatosBusqueda p = new ObjDatosBusqueda();
		        	p.IdCliente = pf.IdCliente;
		        	p.IdPortafolio = pf.IdPortafolio;
		        	p.DocumentoIdentidad = pf.DocumentoIdentidad;
		        	p.CodigoPortafolio = pf.CodigoPortafolio;
		        	datosBusquedaServicio.add(p);
				};
	        } else {
	        	
	        	List<Long> Ports = new ArrayList<Long>();
	        	
	        	for (GenerarCartolaEntrada.objDatosEntrada CLI : in.p_Clientes){	        		
	        		Ports.add(CLI.IdPortafolio);
	        	};
	        	
	        	HashMap<String,Object> parametrosPortafolios = new HashMap<>(2);
	        	parametrosPortafolios.put("IdTrabajo", idTrabajo);
	        	
				List<ObjPortafolios> Portafolios = sql.query(con, SQL_Select_Portafolios,parametrosPortafolios,ObjPortafolios.class);
				
				for (ObjPortafolios pf : Portafolios){
					if (!Ports.contains(pf.IdPortafolio)) continue;
					ObjDatosBusqueda p = new ObjDatosBusqueda();
		        	p.IdCliente = pf.IdCliente;
		        	p.IdPortafolio = pf.IdPortafolio;
		        	p.DocumentoIdentidad = pf.DocumentoIdentidad;
		        	p.CodigoPortafolio = pf.CodigoPortafolio;
		        	datosBusquedaServicio.add(p);
				};
	        };
	    	if(in.p_CartolaConsolidada!= null && in.p_CartolaConsolidada.equals("s")) {
				GenerarCartolaPDF.GenerarCartolaPDF_Entrada cartolaentrada = new GenerarCartolaPDF.GenerarCartolaPDF_Entrada();
		        Token t = new Token();
		        try {
		        	String timestamp = getISO8601StringForCurrentDate();
					List<Token> params = new ArrayList<Token>();
		        	t.IdTokenSesion = new String(Base64.encodeBase64(timestamp.getBytes()));
					t.IdUsuario = in.p_IdUsuario;
					t.TokenValido = "S";
					t.FechaUltimoUso = new Date();
					t.UsoToken = 0;
					t.FechaIngreso = new Date();
					t.FechaSalida = new Date();
					params.add(t);		        			        	
					sql.batch(con,SQL_Insert_Token,params,Token.class); 
					con.commit();
				}catch(Exception e) {
					throw e;
				}

	    	
		        in.p_Clientes.parallelStream().forEach(CLI -> {
	        		GenerarCartolaPDF.Dato datosCartolaPdf = new GenerarCartolaPDF.Dato();
			        List<GenerarCartolaPDF.Dato> clientes = new ArrayList<GenerarCartolaPDF.Dato>();
		        	datosCartolaPdf.IdCliente 	= CLI.IdCliente;
		        	datosCartolaPdf.IdPortafolio 	= CLI.IdPortafolio;
		       	    clientes.add(datosCartolaPdf);    	     			
		       	  
					ObjCartolaSalida cartolaSalida = new ObjCartolaSalida();
					
					cartolaentrada.p_IdEmpresa = in.p_IdEmpresa;
					cartolaentrada.p_IdModulo = in.p_IdModulo;
					cartolaentrada.p_IdUsuario = in.p_IdUsuario;
					cartolaentrada.p_FechaDesde = in.p_FechaDesde;
					cartolaentrada.p_FechaHasta = in.p_FechaHasta;
					cartolaentrada.p_Datos = clientes;
					cartolaentrada.p_token = t.IdTokenSesion;
					
					try {
						GenerarCartolaPDF_Salida cartolasalida = cartolaconsolidada.logica(con, cartolaentrada);
						cartolaSalida.FechaDesde = in.p_FechaDesde;
						cartolaSalida.FechaHasta = in.p_FechaHasta;
			        	cartolaSalida.IdCliente 	= CLI.IdCliente;
						cartolaSalida.IdPortafolio 	= CLI.IdPortafolio;
						cartolaSalida.CartolaEncoded = cartolasalida.CartolaEncoded;
						cartolaSalida.NombreArchivo = cartolasalida.nombrearchivo;
						if(cartolasalida.CartolaEncoded != null) {
							out.P_OUT_CARTOLAS.add(cartolaSalida);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
		        });        	
				
			} else {
	        
	        
	        //LLama a los servicios configurados en base a los portafolios obtenidos
	        if (datosBusquedaServicio.size() > 0){
	        	ReporteMaster reporteMaster = new ReporteMaster(localizacionSistema);
				GraficoMaster graficoMaster = new GraficoMaster(localizacionSistema);
	        	
				HashMap<String,ArrayList<HashMap>> ServiceResultSet = ResultSet (in.p_IdEmpresa,in.p_IdUsuario,in.p_IdModulo,in.p_FechaDesde,in.p_FechaHasta,in.p_FechaHastaOrigen
																				 ,datosBusquedaServicio,Recursos,reporteMaster,graficoMaster,con);
				
			
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
		        
		        
		        paramsNomina.put("IdNomina", in.p_IdNomina );
//				List<GenerarCartolasJob.ObjNomina> sqlNomina = sql.query(con, SQL_Select_Nomina_TipoCartola, paramsNomina, ObjNomina.class);
		
//				System.out.println(sqlNomina.size() > 0);
				
						
		        

				//Portafolios pendientes por cliente: permite liberar de ServiceResultSet las filas
				//consolidadas (clave "...-IdCliente-0") recien cuando el cliente ya no tiene mas portafolios.
				HashMap<Long,Integer> portafoliosPendientesPorCliente = new HashMap<Long,Integer>();
				for (ObjDatosBusqueda dbs : datosBusquedaServicio){
					Integer pendientes = portafoliosPendientesPorCliente.get(dbs.IdCliente);
					portafoliosPendientesPorCliente.put(dbs.IdCliente, pendientes == null ? 1 : pendientes + 1);
				};

				for (ObjDatosBusqueda  dbs : datosBusquedaServicio){
					List<JasperPrint> jpList = new ArrayList<JasperPrint>();
					HashMap<String, JasperPrint> sub = new HashMap<String, JasperPrint>();

					//Paginacion a disco del modelo de impresion de Jasper: mantiene en heap solo las
					//ultimas VIRTUALIZER_PAGINAS_EN_MEMORIA paginas y vuelca el resto a un swap file.
					//Sin esto el JasperPrint completo de la cartola (con todos sus JRPrintElement) vive
					//en memoria hasta el export, que es donde reventaba el fill de los subreportes.
					final JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(
							VIRTUALIZER_PAGINAS_EN_MEMORIA,
							new JRSwapFile(System.getProperty("java.io.tmpdir"),
									VIRTUALIZER_BLOQUE_BYTES, VIRTUALIZER_CRECIMIENTO_BLOQUES),
							true);
					try {

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
								
								if (!existsData)
									existsData = ServiceResultSet.containsKey(ParametroSubReporte.get(RM.nombrePagina)+"-"+ParametroSubReporte.get(RM.nombreProcedimiento)+"-" +dbs.IdCliente+"-"+IdPortafolio);								
								List<DynaBean> datos_ = formatData(ServiceResultSet.get(ParametroSubReporte.get(RM.nombrePagina)+"-"+ParametroSubReporte.get(RM.nombreProcedimiento)+"-"+dbs.IdCliente+"-"+IdPortafolio));
						        JRDataSource DS = new JRBeanCollectionDataSource(datos_);
						        ParametroSubReporte.put(RM.nombreParametroDataSource, DS);
							};
							for (GraficoDef GM : graficoMaster.getGraficoMaster()) {
								if (JBO.nombreGrafico == null || JBO.nombreGrafico.equals(""))  continue; 
								List<DynaBean> datos_ = formatData(ServiceResultSet.get(JBO.nombreGrafico+"-"+JBO.procedimientoGrafico+"-" + dbs.IdCliente+"-"+IdPortafolio));
								ParametroSubReporte.put(GM.nombreParametroReporte, createGraficoPorProducto(GM.nombreGrafico, datos_, ""));
								
								
								/*
								if ( !ParametroSubReporte.containsKey( GM.nombreGrafico) )
									continue;
								if (!KeySubReport.equals(JBO.nombrePagina)&& !GM.reportePadre.equals(KeySubReport)  )
									continue;
								System.out.println("ServiceResultSet::" + ServiceResultSet);
								System.out.println("ParametroSubReporte::" + ParametroSubReporte);
								List<DynaBean> datos_ = formatData(ServiceResultSet.get(ParametroSubReporte.get(GM.nombreGrafico)+"-"+ParametroSubReporte.get(GM.nombreProcedimiento)+"-" + dbs.IdCliente+"-"+IdPortafolio));
								ParametroSubReporte.put(GM.nombreParametroReporte, createGraficoPorProducto(GM.nombreGrafico, datos_, ""));
								
								*/
							};
							
							//Agrega paginas a la cartola
							if (existsData && ((JBO.reporteConsolidado.equals("S") && !clientesProcesados.contains(dbs.IdCliente)) ||  JBO.reporteConsolidado.equals("N")) ){
								ParametroSubReporte.put("parametros", Param);
								
								//Setea resultSet con la informacion del cliente
								List<DynaBean> _datos = formatData(ServiceResultSet.get(JBO.nombrePagina+"-"+"InfoCarteraMaster"+"-"+dbs.IdCliente+"-"+dbs.IdPortafolio));
								JRDataSource datos = new JRBeanCollectionDataSource(_datos);
								
								java.util.Locale locale = new Locale( "es", "CL" );
								ParametroSubReporte.put( JRParameter.REPORT_LOCALE, locale );
								ParametroSubReporte.put( JRParameter.REPORT_VIRTUALIZER, virtualizer );
								
								if("InfoCarteraMasterPag2".equals(JBO.nombrePagina)){
									JasperPrint pagina = JasperFillManager.fillReport(jasper, ParametroSubReporte, datos);
									if(!ordenItem.isEmpty()) {
										sub.put(KeySubReport, pagina);
									}
									else {
										jpList.add(pagina);
									}
									
								}else {
									JasperPrint pagina = JasperFillManager.fillReport(jasper, ParametroSubReporte, datos);
					                jpList.add(pagina);
								}
								
								
							};
						};// END FOR JasperParametros
						
						if("InfoCarteraMasterPag2".equals(JBO.nombrePagina) && !sub.isEmpty()){							
							List<JasperPrint> ordenados = ItemOrdenados(sub,ordenItem);
							jpList.addAll(ordenados);
							
						}
						
					}; // END FOR JasperBinariosOrdenados
					
					if ((showCarteraConsolidada && !clientesProcesados.contains(dbs.IdCliente)) ||  !showCarteraConsolidada) {
						clientesProcesados.add(dbs.IdCliente);
						
						//Paginación pdf
						if (Param.get("Title_FormatoPaginacion") != null)
							JasperUtils.setPageNumbers(jpList, "paginacion",Param.get("Title_FormatoPaginacion").toString() );
									
						//Generación archivo pdf
						try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
							//tFechaHoraSistema.replace("-", "").substring(0, 8);				
							//String namePDF = showCarteraConsolidada ? localizacionSistema+tFechaHoraSistema.replace("-", "").substring(0, 8) +"_"+ dbs.IdCliente +".pdf" : localizacionSistema+tFechaHoraSistema.replace("-", "").substring(0, 8) +"_"+ dbs.IdCliente + "_" + dbs.IdPortafolio +".pdf";
							String namePDF = dbs.DocumentoIdentidad + " " + dbs.CodigoPortafolio+".pdf";
							
							if (jpList.size() > 0){
								final JRPdfExporter exporter = new JRPdfExporter();
								exporter.setExporterInput(SimpleExporterInput.getInstance(jpList));
								exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os));
								exporter.exportReport();
								
								final byte[] bytes = os.toByteArray();
								if (generaZip)
									pdfs.put(namePDF, bytes);
								//IOUtils.write(bytes, new FileOutputStream(new File(namePDF)));	//Solo para pruebas internas

								//UI - solo si fuera ONLINE
								ObjCartolaSalida cartolaSalida = new ObjCartolaSalida();
								//El Base64 pesa 1.33x el PDF y solo lo consume la UI: fuera de ONLINE es memoria retenida sin uso.
								if (esOnline)
									cartolaSalida.CartolaEncoded = new String(Base64.encodeBase64(bytes));
								cartolaSalida.CartolaEncodedByte = bytes;
								cartolaSalida.FechaDesde = in.p_FechaDesde;
								cartolaSalida.FechaHasta = in.p_FechaHasta;
								cartolaSalida.IdCliente = dbs.IdCliente;
								cartolaSalida.IdPortafolio = dbs.IdPortafolio;
								cartolaSalida.NombreArchivo = namePDF;
								out.P_OUT_CARTOLAS.add(cartolaSalida);
							} else {
								//out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "CARTOLA", "Sin Informacion para el cliente "+dbs.IdCliente.toString()));
								out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "CLIENTE_NO_GENERA_CARTOLA", "CLIENTE_NO_GENERA_CARTOLA"));
							};
						};
			        };

					//Libera del acumulador las filas ya consumidas por este portafolio. Sus claves son
					//"pagina-procedimiento-IdCliente-IdPortafolio" y no se vuelven a leer en el resto del loop.
					//Sin esto ServiceResultSet retiene la nomina completa hasta el return (mayor consumidor de heap).
					final String sufijoPortafolio = "-" + dbs.IdCliente + "-" + dbs.IdPortafolio;
					ServiceResultSet.keySet().removeIf(k -> k.endsWith(sufijoPortafolio));

					//Las filas de reportes consolidados usan IdPortafolio 0 y las comparten todos los
					//portafolios del cliente: se liberan al procesar el ultimo portafolio de ese cliente.
					Integer pendientes = portafoliosPendientesPorCliente.get(dbs.IdCliente);
					if (pendientes != null && pendientes <= 1) {
						portafoliosPendientesPorCliente.remove(dbs.IdCliente);
						final String sufijoConsolidado = "-" + dbs.IdCliente + "-0";
						ServiceResultSet.keySet().removeIf(k -> k.endsWith(sufijoConsolidado));
					} else if (pendientes != null) {
						portafoliosPendientesPorCliente.put(dbs.IdCliente, pendientes - 1);
					}

					} finally {
						//Libera las paginas virtualizadas y borra el swap file de esta cartola.
						virtualizer.cleanup();
					}
				};
				}

				//Zipear Total de PDF's
				final String currentPath = Utils.getConfiguracion(con, "BSCL", in.p_IdEmpresa, "PLA.RutaBaseArchivos");
				//final String currentPath = "NOR/";//prueba local
				String nameCarpeta = localizacionSistema+tFechaHoraSistema.replace("-", "").substring(0, 8);
				try {
					if(in.p_TipoProceso != null) {
				if (pdfs.size() > 0 && !in.p_TipoProceso.equals("ONLINE")) {
					zipArchive(currentPath, nameCarpeta, pdfs);
				}else if(pdfs.size() == 0 && in.p_TipoProceso.equals("ONLINE") && out.P_OUT_CARTOLAS == null) {
					//out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "CARTOLA", "Sin Informacion para el cliente "+datosBusquedaServicio.get(0).toString()));
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ADVERTENCIA, "CLIENTE_NO_GENERA_CARTOLA", "CLIENTE_NO_GENERA_CARTOLA"));
				}
					}
				}catch(Exception e) {
					throw e;
				}
	        };
		}
		catch (Exception e) {
			e.printStackTrace();
			out.P_OUT_ERRORESCONTADOR = 1L;
			throw e;
		} finally {
			Cliente clienteBusqueda = new Cliente();
			clienteBusqueda.IdTrabajo = idTrabajo;
			List<Cliente> clientesBusqueda = Arrays.asList(clienteBusqueda);
			sql.batch(con, SQL_DELETE_PORTAFOLIOS_LEGACY, clientesBusqueda, Cliente.class);
			
			try {
				
				sql.batch(con, SQL_DELETE_NOMINA_PASO, nominasPaso, NominaPaso.class);
				sql.batch(con, SQL_DELETE_PROCESOS_EJECUTADOS_PASO, nominasPaso, NominaPaso.class);
				sql.batch(con, SQL_DELETE_RESUMEN_INVERSION_BASE_OUT, nominasPaso, NominaPaso.class);
				sql.batch(con, SQL_DELETE_CARTERA_RV_OUT, nominasPaso, NominaPaso.class); 
				sql.batch(con, SQL_DELETE_INFO_CARTERA_MASTER_OUT, nominasPaso, NominaPaso.class);
				sql.batch(con, SQL_DELETE_CARTERA_RF_OUT, nominasPaso, NominaPaso.class);
				sql.batch(con, SQL_DELETE_CARTERA_IF_OUT, nominasPaso, NominaPaso.class);
			} catch (Exception e) {
				logger.info("SQL_DELETE_NOMINA_PASO "+e);
			}
			
			
		}
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
	
	public List<JasperPrint> ItemOrdenados(HashMap<String, JasperPrint> sub, List<OrdenItem> ord) {
	    List<JasperPrint> jpListDet = new ArrayList<>();
	    Set <String> ItemsAgregados = new HashSet<>();

	    for (OrdenItem item : ord) {
	        JasperPrint jp = sub.get(item.ItemCartola);
	        if (jp != null) {
	            jpListDet.add(jp);
	            ItemsAgregados.add(item.ItemCartola);
	        }
	    }
	    
	    // EN CASO DE NO EXISTIR CONFIGURACION DE ELEMENTO QUEDA AL FINAL
	    
	    if (sub.size() != ItemsAgregados.size()) {
	    	for (Map.Entry <String, JasperPrint> obj : sub.entrySet()) {
	    		if(!ItemsAgregados.contains(obj.getKey())) {
	    			logger.info("ELEMENTO CON ORDEN NO CONFIGURADO: " + obj.getKey());
	    			 JasperPrint jp = obj.getValue();
	    			if(jp != null) {
	    				jpListDet.add(jp);
	    			}	
	    		}
	    	}
	    }

	    return jpListDet;
	}

	
	public HashMap<String,ArrayList<HashMap>> ResultSet (Long IdEmpresa, Long IdUsuario, String IdModulo, String FechaDesde, String FechaHasta,String FechaHastaOrigen,
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
				parmsInSP.put("FechaHastaOrigen", FechaHastaOrigen);
				//Filtros propios de servicio
				if (Servicios.get("filtroUbicacionBoveda") != null)
					parmsInSP.put("p_FiltroUbicacionBoveda", Servicios.get("filtroUbicacionBoveda"));
				if (Servicios.get("filtroSoloMonedaSistema") != null)
					parmsInSP.put("p_FiltroSoloMonedaSistema", Servicios.get("filtroSoloMonedaSistema"));
				
		    	
				for (ObjRecurso R : Recursos) {
					if(R.Nombre.equals("FM_showRent") && R.Valor.equals("NO")){
						R.Valor = "SI";
					}
					HashMap<String, Object> rpt = new HashMap<String, Object>();
					if(R.Tipo.equals("java") && R.Nombre.equals(Servicios.get("nombreProcedimiento"))){
						Class<?> tclass = null;
						try {
							tclass=Class.forName(R.Valor);
						}
						catch(Exception e) {
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
						entradaCartola.p_AgrupaMovimientos = "S";
						entradaCartola.p_FechaHastaOrigen = Fecha.toDate((String) parmsInSP.get("FechaHastaOrigen"));
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
						String FiltroUbicacion = "";
						String FiltroMoneda = "";
						if (Servicios.get("filtroUbicacionBoveda") != null)
							FiltroUbicacion = "p_FiltroUbicacionBoveda=>:p_FiltroUbicacionBoveda,";
						if (Servicios.get("filtroSoloMonedaSistema") != null)
							FiltroMoneda = "p_FiltroSoloMonedaSistema=>:p_FiltroSoloMonedaSistema,";
						
						String queryCall = "CALL \""+ R.Valor +"\" "
				        		+ "(p_IdModulo=>:IdModulo,"
				        		+ "p_IdUsuario=>:IdUsuario,"
				        		+ "p_IdEmpresa=>:IdEmpresa,"
				        		+ "p_FechaDesde=>:FechaDesde,"
				        		+ "p_FechaHasta=>:FechaHasta,"
				        		+ FiltroUbicacion
				        		+ FiltroMoneda
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
	
		//System.out.println(ServiceResultSet);
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
    
    public void zipArchive(final String currentPath, final String nameZip, HashMap<String,byte[]> pdfs) throws IOException {
    	String zipFile=currentPath+nameZip+".zip";
    	try (final ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
	    	for (Map.Entry<String, byte[]> entry : pdfs.entrySet()) {
	    		zout.putNextEntry(new ZipEntry(entry.getKey()));
	    		zout.write(entry.getValue(),0, entry.getValue().length );
	    		zout.closeEntry();
	    	};
    	}
    };
	/**
	 * Return an ISO 8601 combined date and time string for current date/time
	 * 
	 * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
	 */
	public String getISO8601StringForCurrentDate() {
		Date now = new Date();
		return getISO8601StringForDate(now);
	}

	/**
	 * Return an ISO 8601 combined date and time string for specified date/time
	 * 
	 * @param date
	 *            Date
	 * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
	 */
	private String getISO8601StringForDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
		return dateFormat.format(date);
	}

	/**
	 * Private constructor: class cannot be instantiated
	 */

	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		GenerarCartolaEntrada jin=gson.fromJson(in.toString() ,GenerarCartolaEntrada.class);
		GenerarCartolaSalida out=logica(con,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("Master02", "connections.properties")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				SQL sql = new SQL();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":109,\"p_AccionNomina\":\"ONLINE\",\"p_TipoProceso\":\"ONLINE\",\"p_FechaDesde\":\"2019-08-01\",\"p_FechaHasta\":\"2019-08-23\",\"p_Clientes\":[{\"IdCliente\":395174,\"IdPortafolio\":343710}]}";
				GenerarCartolaEntrada Objin = gson.fromJson(json, GenerarCartolaEntrada.class);		
				GenerarCartola  kk = new GenerarCartola();
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
