package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarComisionesNN implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ProcesarComisionesNN.class);
	
	private final String SQL_SELECT_COMISIONES_NN_PROCESADAS = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_SelectComisionesNNProcesadas.sql");
	private final String SQL_SELECT_CUSTODIA = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_SelectConsultarCartera.sql");
	private final String SQL_SELECT_CUSTODIA_SELECTIVO = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_SelectConsultarCarteraSelectivo.sql");
	private final String SQL_INSERT_COMISIONES_NN_PROCESADAS = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_InsertComisionesNNProcesadas.sql");
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaProcesarComisionesNN extends BcsPojo{
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
	    public String p_IdModulo;
		public String p_FechaConsultaDesde;
		public String p_FechaConsultaHasta;
		public List<Cartera> p_Solicitudes;
		public String p_EsMasivo;
		
		public EntradaProcesarComisionesNN() {
			this.p_FechaConsultaDesde = "0001-01-01";
			this.p_FechaConsultaHasta = "0001-01-01";
			this.p_EsMasivo = "N";
		}
	}
	
	public static class SalidaProcesarComisionesNN extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		public List<DocumentoPorFolio> P_OUT_DOCUMENTOS_POR_FOLIO;
		
		public SalidaProcesarComisionesNN(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ADVERTENCIASCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
			this.P_OUT_DOCUMENTOS_POR_FOLIO = new ArrayList<DocumentoPorFolio>();
		}
	}
	
	public static class SalidaCustodia extends BcsPojo{
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String CodigoPortafolio;
		public String IdSerieFondo;
		public Long FolioCartera;
		public String FechaHasta;
		public java.sql.Date FechaCompra;
		public int DiasPermanencia;
		public BigDecimal ComisionAnual;
		public BigDecimal ComisionDiaria;
		public BigDecimal CuotasCliente;
		public BigDecimal ComisionNN;
		public BigDecimal CuotasFinalesCliente;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
	   	public String IdRegimenTributario;
	   	public BigDecimal MontoOriginal;
	   	public Long IdCartera;
	   	public String NombreCliente;
	   	
	   	public SalidaCustodia () {
	   		this.DocumentoIdentidad = "";
	   	}
	}
	
	public static class Cartera extends BcsPojo{
		public Long IdCartera;
	}
	
	public static class ComisionProcesada extends BcsPojo{
		public Long IdEmpresa;
		public Long IdCartera;
		public Long FolioCartera;
    	public Date FechaDesde;
    	public Date FechaHasta;
    	public java.sql.Timestamp FechaProcesamiento;
    	public String Estado;
    	public String IdSecuenciaRescateNN;
		public Long IdCliente;
		public String DocumentoIdentidad;
		
		public ComisionProcesada() {
			this.IdEmpresa = 0L;
			this.IdCartera = 0L;
			this.FolioCartera = 0L;
			this.FechaDesde = Fecha.toDate("0001-01-01");
			this.FechaHasta = Fecha.toDate("0001-01-01");
			this.FechaProcesamiento = Fecha.toDateTime("0001-01-01 00:00:00");
			this.Estado = "ANU";
			this.IdSecuenciaRescateNN = "";
			this.IdCliente = 0L;
			this.DocumentoIdentidad = "";
		}
	}
	
	public static class DocumentoPorFolio extends BcsPojo {
		public String DocumentoIdentidad;
		public Long FolioCartera;
		
		public DocumentoPorFolio() {
			this.DocumentoIdentidad = "";
			this.FolioCartera = 0L;
		}
		
		public DocumentoPorFolio (String documentoIdentidad, Long FolioCartera) {
			this.DocumentoIdentidad = documentoIdentidad;
			this.FolioCartera = FolioCartera;
		}
	}
	
	private List<Long> obtenerIdCartera(List<Cartera> carteras){
		logger.trace("INICIO::ProcesarComisionesNN.obtenerIdCartera");
		List<Long> ids = carteras.parallelStream().map(solicitud->solicitud.IdCartera).distinct().collect(Collectors.toList());
		logger.trace("FIN::ProcesarComisionesNN.obtenerIdCartera");
		return ids;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaProcesarComisionesNN jin = gson.fromJson(in.toString(), EntradaProcesarComisionesNN.class);
		SalidaProcesarComisionesNN out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarComisionesNN logica(Connection con, EntradaProcesarComisionesNN entrada) throws Exception{
		logger.info("INICIO::ProcesarComisionesNN");
		long inicioServicio = System.currentTimeMillis();
		SQL sql=new SQL();
		
		SalidaProcesarComisionesNN out = new SalidaProcesarComisionesNN();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		ConsultarComisionesNNFondoMutuo consultarComisionesNNFondoMutuo = new ConsultarComisionesNNFondoMutuo();
		String fechaHastaUltimoDiaHabil = consultarComisionesNNFondoMutuo.obtenerDiaHabilAnterior(entrada.p_FechaConsultaHasta);
		
		List<SalidaCustodia> listCustodias = new ArrayList<SalidaCustodia>();
		
		HashMap<String, Object> params = new HashMap<String, Object>(5);
		HashMap<String, Object> paramsProcesadas = new HashMap<String, Object>(4);
		List<ComisionProcesada> comisionesProcesadas = new ArrayList<ComisionProcesada>();
		
		if(entrada.p_Solicitudes != null && entrada.p_Solicitudes.size() > 0) 
		{
			List<Long> idCarteras = obtenerIdCartera(entrada.p_Solicitudes);
			paramsProcesadas.put("IdCartera", idCarteras);
			paramsProcesadas.put("FechaDesde", entrada.p_FechaConsultaDesde);
			paramsProcesadas.put("FechaHasta", fechaHastaUltimoDiaHabil);
			paramsProcesadas.put("IdEmpresa", entrada.p_IdEmpresa);
			comisionesProcesadas = sql.query(con, SQL_SELECT_COMISIONES_NN_PROCESADAS, paramsProcesadas, ComisionProcesada.class);
			
			if(comisionesProcesadas.size() > 0) {
				out.P_OUT_MENSAJES.addAll(obtenerErroresAdvertenciasComisionesProcesadas(con, comisionesProcesadas, entrada.p_IdEmpresa, entrada.p_EsMasivo));
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream().filter(men -> MensajeSalida.MENSAJE_ERROR.equals(men.tipo)).count();
				if(out.P_OUT_ERRORESCONTADOR > 0 && _c.NO.equals(entrada.p_EsMasivo) ){
					return out;					
				}
			}
			
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdCartera", idCarteras);
			params.put("FechaConsultaDesde", entrada.p_FechaConsultaDesde);
			params.put("FechaConsultaHasta", entrada.p_FechaConsultaHasta);
			params.put("FechaHastaUltimoDiaHabil", fechaHastaUltimoDiaHabil);
			listCustodias = sql.query(con, SQL_SELECT_CUSTODIA_SELECTIVO, params, SalidaCustodia.class);
		}
		else
		{
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FechaConsultaDesde", entrada.p_FechaConsultaDesde);
			params.put("FechaConsultaHasta", entrada.p_FechaConsultaHasta);
			params.put("FechaHastaUltimoDiaHabil", fechaHastaUltimoDiaHabil);
			
			listCustodias = sql.query(con, SQL_SELECT_CUSTODIA, params, SalidaCustodia.class);
		}
		
		
		CrearSolicitudRescateComisionNN crearSolicitudRescateComisionNN = new CrearSolicitudRescateComisionNN();
		CrearSolicitudRescateComisionNN.EntradaCrearSolicitudRescateComisionNN entradaCrearSolicitudRescateComisionNN = new CrearSolicitudRescateComisionNN.EntradaCrearSolicitudRescateComisionNN();
		Long i = 1L;
		
		entradaCrearSolicitudRescateComisionNN.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearSolicitudRescateComisionNN.p_IdModulo = entrada.p_IdModulo;
		entradaCrearSolicitudRescateComisionNN.p_IdTipoCanalIngreso = "";
		entradaCrearSolicitudRescateComisionNN.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearSolicitudRescateComisionNN.p_SimularOperacion = "N";
		entradaCrearSolicitudRescateComisionNN.p_UsaReglasNegocio = "S";
		
		for (SalidaCustodia sal : listCustodias) {
			CrearSolicitudRescateComisionNN.InSolicitudesRescate inSolicitudesRescate = new CrearSolicitudRescateComisionNN.InSolicitudesRescate();
			
			inSolicitudesRescate.FolioContrato = 0L;
			inSolicitudesRescate.IdCliente = sal.IdCliente;
			inSolicitudesRescate.IdInterno = i++;
			inSolicitudesRescate.IdMoneda = sal.IdMoneda;
			inSolicitudesRescate.IdPortafolio = sal.IdPortafolio;
			inSolicitudesRescate.IdRegimenTributario = sal.IdRegimenTributario;
			inSolicitudesRescate.IdSerieFondo = sal.IdSerieFondo;
			inSolicitudesRescate.IngresoAutomatico = "N";
			inSolicitudesRescate.Monto = new BigDecimal(1); //sal.MontoOriginal.multiply(sal.ComisionNN).divide(sal.CuotasCliente);
			inSolicitudesRescate.NumeroCuotas = sal.ComisionNN;
			inSolicitudesRescate.FechaValorCuota = Fecha.toDate(fechaSistema);
			inSolicitudesRescate.FolioCartera = sal.FolioCartera;
			inSolicitudesRescate.IdMonedaPago = monedaSistema;
			inSolicitudesRescate.IdCartera = sal.IdCartera;
			entradaCrearSolicitudRescateComisionNN.p_SolicitudesRescate.add(inSolicitudesRescate);
		}
		
		CrearSolicitudRescateComisionNN.SalidaCrearSolicitudRescateComisionNN salida = crearSolicitudRescateComisionNN.logica(con, entradaCrearSolicitudRescateComisionNN);
		
		if(salida.P_OUT_COMISIONES_INSERTADAS.size() > 0) {
			insertarComisionesNNProcesadas(con, entrada.p_FechaConsultaDesde, entrada.p_FechaConsultaHasta , salida.P_OUT_COMISIONES_INSERTADAS);
		}
		
		long finServicio = System.currentTimeMillis();
		
		logger.info("FIN::ProcesarComisionesNN", finServicio - inicioServicio);
		//con.rollback();
		out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
		out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_MENSAJES.stream().filter(men -> MensajeSalida.MENSAJE_ADVERTENCIA.equals(men.tipo)).count();
		out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream().filter(men -> MensajeSalida.MENSAJE_ERROR.equals(men.tipo)).count();
		out.P_OUT_RESULTADO = salida.P_OUT_RESULTADO;
		out.P_OUT_DOCUMENTOS_POR_FOLIO = obtenerDocumentosIdentidadPorFolioCartera(out.P_OUT_MENSAJES, listCustodias,comisionesProcesadas);
		return out;
	}
	
	public List<MensajeSalida> obtenerErroresAdvertenciasComisionesProcesadas(Connection con, List<ComisionProcesada> comisionesProcesadas, Long p_IdEmpresa, String p_EsMasivo) throws Exception{
		
		HashMap<String,String> params = new HashMap<String,String>(2);
		List<MensajeSalida> errores = comisionesProcesadas.stream().map(com -> {
			MensajeSalida mensaje2 = new MensajeSalida();
			params.put("IdSecuenciaRescateNN", com.IdSecuenciaRescateNN);
			params.put("DocumentoIdentidad", com.DocumentoIdentidad);
			try {
				mensaje2 = new MensajeSalida(com.FolioCartera,_c.SI.equals(p_EsMasivo) ? MensajeSalida.MENSAJE_ADVERTENCIA : MensajeSalida.MENSAJE_ERROR,"UI_ERROR_COMISION_NN_PROCESADA",params,con, p_IdEmpresa);
			} catch (Exception e) {
				e.printStackTrace();
			}
			params.clear();
			return mensaje2;
		}).collect(Collectors.toList());
		return errores;
	}

	public void insertarComisionesNNProcesadas(Connection con, String p_FechaConsultaDesde, String p_FechaConsultaHasta, List<SolicitudAporteRescate> solicitudesInsertadas) throws Exception {
		logger.info("INICIO::ProcesarComisionesNN.insertarComisionesNNProcesadas");
		long inicioServicio = System.currentTimeMillis();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, solicitudesInsertadas.get(0).IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<ComisionProcesada> comisionesProcesadas = solicitudesInsertadas.parallelStream().map( comisionInsertada -> {
			ComisionProcesada comision = new ComisionProcesada();
			comision.IdCartera = comisionInsertada.IdCartera;
			comision.FolioCartera = comisionInsertada.FolioCartera;
			comision.IdEmpresa = comisionInsertada.IdEmpresa;
			comision.FechaDesde = Fecha.toDate(p_FechaConsultaDesde);
			comision.FechaHasta = Fecha.toDate(p_FechaConsultaHasta);
			comision.FechaProcesamiento = Fecha.toDateTime(fechaSistema);
			comision.Estado = "PRO";
			comision.IdSecuenciaRescateNN = comisionInsertada.IdSolicitudAporteRescate.toString();
			comision.IdCliente = comisionInsertada.IdCliente;
			return comision;
		}).collect(Collectors.toList());
		
		SQL sql=new SQL();
		sql.batch(con, SQL_INSERT_COMISIONES_NN_PROCESADAS, comisionesProcesadas, ComisionProcesada.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarComisionesNN.insertarComisionesNNProcesadas", finServicio - inicioServicio);
	}

	private List<DocumentoPorFolio> obtenerDocumentosIdentidadPorFolioCartera(List<MensajeSalida> p_OUT_MENSAJES, List<SalidaCustodia> listCustodias, List<ComisionProcesada> comisionesProcesadas) {
		logger.info("INICIO::ProcesarComisionesNN.obtenerDocumentosIdentidadPorFolioCartera");
		long inicioServicio = System.currentTimeMillis();
		
		List<DocumentoPorFolio> listadoDocumentos = new ArrayList<DocumentoPorFolio>();
		
		listadoDocumentos.addAll( listCustodias.stream().map( cus -> {
			return new DocumentoPorFolio(cus.DocumentoIdentidad ,cus.FolioCartera);
		}).collect(Collectors.toList()));
		
		listadoDocumentos.addAll( comisionesProcesadas.stream().map( cus -> {
			return new DocumentoPorFolio(cus.DocumentoIdentidad ,cus.FolioCartera);
		}).collect(Collectors.toList()));
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarComisionesNN.obtenerDocumentosIdentidadPorFolioCartera", finServicio - inicioServicio);
		return listadoDocumentos;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FechaConsultaDesde\":\"2021-10-01\",\"p_FechaConsultaHasta\":\"2021-12-31\",\"p_Solicitudes\":[]}\r\n";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FechaConsultaDesde\":\"2021-10-01\",\"p_FechaConsultaHasta\":\"2021-12-31\",\"p_Solicitudes\":[{\"IdCartera\":1536905}]}";
			EntradaProcesarComisionesNN Objin = gson.fromJson(json, EntradaProcesarComisionesNN.class);
			ProcesarComisionesNN  demo=new ProcesarComisionesNN();				
			try {
				SalidaProcesarComisionesNN ObjOut=demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
				con.commit();
			}
			
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
				con.rollback(); 			
	    	}
			System.out.println("== END ==");
	}

}
