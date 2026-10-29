package cl.bcs.plataforma.ins;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import java.text.SimpleDateFormat;

public class ConsultarInstrumentoWS implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ConsultarInstrumentoWS.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	private String SQL_SELECT_CONSULTAR_INSTRUMENTO_WS =loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultaInstrumentoWS.sql");
	private String SQL_SELECT_CONSULTAR_PRECIOS =loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoWS_ConsultaPrecioCierre.sql");
	private String SQL_SELECT_ATRIBUTO_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarInstrumentoWS_ConsultaAtributoInstrumento.sql");
	private final SimpleDateFormat ANO_FORMAT = new SimpleDateFormat("yyyy");
	private final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	public static class ConsultarInstrumentoWSEntrada extends BcsPojo{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_IncluyeNemoNegociacion;
	    public ArrayList<DatosIn> p_Datos; 
		
	    public ConsultarInstrumentoWSEntrada() {
			this.p_Datos = new ArrayList<DatosIn>();

		}
	}
    
    public static class DatosIn extends BcsPojo{
		public Long IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public String EstaInscrito;
		public String EsAutorizado;
		public String EsInstrumentoCalificado;
		public String IdEstado;
	}
		
	public static class ConsultarInstrumentoWSSalida extends BcsPojo{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<InstrumentosWS>  		P_OUT_INSTRUMENTO;
		
		public ConsultarInstrumentoWSSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_INSTRUMENTO = new ArrayList<InstrumentosWS>();
		}
	}
	
	public static class InstrumentosWS extends BcsPojo{
		public Long			IdInstrumento;
		public String		Nemotecnico;
		public String		NombreCompletoEmisor;
		public String 		RazonSocial;
		public String 		IdClasificacionTipoInstrumento; 
		public String 		IdTipoInstrumento;
		public String 		IdEstado;
		public BigDecimal	Rentabilidad7Dias;
		public BigDecimal	Rentabilidad30Dias;
		public BigDecimal	Rentabilidad52Sem;
		public BigDecimal	RentabilidadAnoCurso;
		public String		EstaInscrito;
		public String		EsAutorizado;
		public String 		EsInstrumentoCalificado;
		public String 		MonedaDeReajuste;
		public String 		MonedaDePago;	
	}
	
	public static class PreciosCierre extends BcsPojo{
		public String     UltimaFechaCierre;
		public String     PrimeraFechaInicio;
		public Long       IdInstrumento;
		public String	  Nemotecnico;
		public BigDecimal UltimoPrecioCierre;
		public BigDecimal PrimerPrecioCierre;
		public BigDecimal Rentabilidad;
	}
	
	public static class AtributoInstrumento extends BcsPojo {
		public String IdAtributoInstrumento;
		public String TipoAtributoInstrumento;
		
		public AtributoInstrumento() {
			this.IdAtributoInstrumento = "";
			this.TipoAtributoInstrumento = "";
		}
	}
		
	@Trace
	public ConsultarInstrumentoWSSalida logica(Connection con, ConsultarInstrumentoWSEntrada in) throws Exception {
		logger.info("INICIO::ConsultarInstrumentoWS");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		Long Id = 0L;
		ConsultarInstrumentoWSSalida salida = new ConsultarInstrumentoWSSalida();
		HashMap<String,Object> params = new HashMap<String,Object>(15);
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		List<Long> IdInstrumento = new ArrayList<Long>();
		List<String> IdClasificacionTipoInstrumento = new ArrayList<String>();
		List<String> IdTipoInstrumento = new ArrayList<String>();
		List<String> Nemotecnico = new ArrayList<String>();
		List<String> EstaInscrito = new ArrayList<String>();
		List<String> EsAutorizado = new ArrayList<String>();
		List<String> EsInstrumentoCalificado = new ArrayList<String>();
		List<String> IdEstado = new ArrayList<String>();

		for(DatosIn instrumentos : in.p_Datos){
			if(instrumentos.IdInstrumento != null) IdInstrumento.add(instrumentos.IdInstrumento);
			if(instrumentos.IdClasificacionTipoInstrumento != null) IdClasificacionTipoInstrumento.add(instrumentos.IdClasificacionTipoInstrumento);
			if(instrumentos.IdTipoInstrumento != null) IdTipoInstrumento.add(instrumentos.IdTipoInstrumento);
			if(instrumentos.Nemotecnico != null)  Nemotecnico.add(instrumentos.Nemotecnico);
			if(instrumentos.EstaInscrito != null) EstaInscrito.add(instrumentos.EstaInscrito);
			if(instrumentos.EsAutorizado != null) EsAutorizado.add(instrumentos.EsAutorizado);
			if(instrumentos.EsInstrumentoCalificado != null) EsInstrumentoCalificado.add(instrumentos.EsInstrumentoCalificado);
			if(instrumentos.IdEstado != null) IdEstado.add(instrumentos.IdEstado);
	    }
		
		List<AtributoInstrumento> atributos = obtenerAtributos(con, in.p_IdEmpresa);
		
		salida.P_OUT_MENSAJES.addAll(verificarAtributosEntrada(con, in.p_IdEmpresa, atributos, IdClasificacionTipoInstrumento, IdTipoInstrumento, EsAutorizado, EstaInscrito, EsInstrumentoCalificado));
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0L) {
			long fin = System.currentTimeMillis();
			logger.info("FIN::ConsultarInstrumentoWS", fin - inicio);
			return salida;
		}
		  
		String FiltraIdInstrumento = IdInstrumento.isEmpty() ? _c.NO : _c.SI;
		String FiltraIdClasificacionTipoInstrumento = IdClasificacionTipoInstrumento.isEmpty() ?   _c.NO : _c.SI;
		String FiltraIdTipoInstrumento = IdTipoInstrumento.isEmpty()  ? _c.NO : _c.SI;
		String FiltraNemotecnico = Nemotecnico.isEmpty() ? _c.NO : _c.SI;
		String FiltraEsInscrito = EstaInscrito.isEmpty() ? _c.NO : _c.SI;
		String FiltraEsAutorizado = EsAutorizado.isEmpty() ? _c.NO : _c.SI;
		String FiltraEsInstrumentoCalificado = EsInstrumentoCalificado.isEmpty() ? _c.NO : _c.SI;
		String FiltraIdEstado = IdEstado.isEmpty() ? _c.NO : _c.SI;
		/* MODIFICACION PARA DIA 0 RENTA VARIABLE*/
		// Restringe la consulta a instrumentos raiz de RV (omitiendo derivados de negociacion) cuando no se busca con los filtros IdInstrumento o Nemotecnico específico.
		String IncluyeNemoNegociacion = (in.p_IncluyeNemoNegociacion == null || in.p_IncluyeNemoNegociacion.trim().isEmpty()) ? "N" : in.p_IncluyeNemoNegociacion;
		
		params.put("IdInstrumento", IdInstrumento);
		params.put("IdClasificacionTipoInstrumento", IdClasificacionTipoInstrumento);
		params.put("IdTipoInstrumento", IdTipoInstrumento);
		params.put("Nemotecnico", Nemotecnico);
		params.put("EstaInscrito", EstaInscrito);
		params.put("EsAutorizado", EsAutorizado);
		params.put("EsInstrumentoCalificado", EsInstrumentoCalificado);
		params.put("IdEstado", IdEstado);
		params.put("FiltraIdInstrumento", FiltraIdInstrumento);
		params.put("FiltraIdClasificacionTipoInstrumento", FiltraIdClasificacionTipoInstrumento);
		params.put("FiltraIdTipoInstrumento", FiltraIdTipoInstrumento);
		params.put("FiltraNemotecnico", FiltraNemotecnico);
		params.put("FiltraEsInscrito", FiltraEsInscrito);
		params.put("FiltraEsAutorizado", FiltraEsAutorizado);
		params.put("FiltraEsInstrumentoCalificado", FiltraEsInstrumentoCalificado);
		params.put("FiltraIdEstado", FiltraIdEstado);
		params.put("IncluyeNemoNegociacion", IncluyeNemoNegociacion);
		
		List<InstrumentosWS> Instrumentos = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO_WS, params, InstrumentosWS.class);
		salida.P_OUT_INSTRUMENTO.addAll(Instrumentos);
		
		if(salida.P_OUT_INSTRUMENTO.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_SIN_INSTRUMENTOS",new HashMap<String,String>(0),con, in.p_IdEmpresa));
		} else {
			java.util.Date fecha = FORMAT.parse(fechaSistema);
			String ano = "";
			
			ano = ANO_FORMAT.format(fecha);
			
			String FechaInicioAno= ano+"-01-02";
			
			boolean EsDiaHabil = Feriados.esDiaHabil(con, FechaInicioAno,idPaisEmpresa);
			
			if(EsDiaHabil== false) {
				ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
			    InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
				inObtenerDiaSiguiente.p_idPais =idPaisEmpresa;
				inObtenerDiaSiguiente.p_diaHabil = "S";
				inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
				FechasIn f = new FechasIn();
				f.fechaInicial = FechaInicioAno;
				f.numeroDias = 1L;
				inObtenerDiaSiguiente.p_fechas.add(f);
				
				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(con, inObtenerDiaSiguiente);	
				FechaInicioAno = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;	
			}
			
			HashMap<String,Object> params2 = new HashMap<String,Object>(3);
			List<String> Nemotecnicos = new ArrayList<String>();
			
			String FechaCierre = fechaSistema;
			
			for(InstrumentosWS ConsultaInstrumento : Instrumentos){
				 Nemotecnicos.add(ConsultaInstrumento.Nemotecnico);
		     }
			
			params2.put("FechaInicioAno",FechaInicioAno);
			params2.put("FechaCierre",FechaCierre);
			params2.put("Nemotecnicos",Nemotecnicos);
			
			List<PreciosCierre> PreciosCierreInstrumento = sql.query(con, SQL_SELECT_CONSULTAR_PRECIOS, params2, PreciosCierre.class);
			for (InstrumentosWS s: salida.P_OUT_INSTRUMENTO) {
				for (PreciosCierre Rentab : PreciosCierreInstrumento) {
					if(s.Nemotecnico.equalsIgnoreCase(Rentab.Nemotecnico)) {
						s.RentabilidadAnoCurso=Rentab.Rentabilidad;
					}	
				}
			}
			
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CONSULTA_INSTRUMENTOS_OK",new HashMap<String,String>(0),con, in.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarInstrumentoWS", fin - inicio);
		return salida;
	}
	
	public List<AtributoInstrumento> obtenerAtributos(Connection conn, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ConsultarInstrumentoWS.obtenerAtributos");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
        
        List<AtributoInstrumento> atributos = sql.query(conn, SQL_SELECT_ATRIBUTO_INSTRUMENTO, new HashMap<>(0), AtributoInstrumento.class);
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarInstrumentoWS.obtenerAtributos", fin - inicio);
		return atributos;
	}
	
	public List<MensajeSalida> verificarAtributosEntrada(Connection conn, Long idEmpresa, List<AtributoInstrumento> atributos, List<String> clasificacionInstrumentos, List<String> tipoInstrumentos,List<String> autorizados, List<String> inscritos, List<String> calificados) throws Exception {
		logger.debug("INICIO::ConsultarInstrumentoWS.verificarAtributosEntrada");
        long inicio = System.currentTimeMillis();
        
        List<MensajeSalida> mensajes = new ArrayList<>();
        HashMap<String,String> params = new HashMap<String,String>(1);
       
        for(String clasificacion : clasificacionInstrumentos) {
        	if(atributos.parallelStream().filter(atr -> "CLASIFICACION".equals(atr.TipoAtributoInstrumento) && clasificacion.equalsIgnoreCase(atr.IdAtributoInstrumento)).count() == 0 ) {
        		params.put("ClasificacionInstrumento", clasificacion);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CLASIFICACION_INSTRUMENTO_NO_EXISTE",params,conn, idEmpresa));
        		params.clear();
        	}
        }
        
        for(String tipo : tipoInstrumentos) {
        	if(atributos.parallelStream().filter(atr -> "TIPO".equals(atr.TipoAtributoInstrumento) && tipo.equalsIgnoreCase(atr.IdAtributoInstrumento)).count() == 0 ) {
        		params.put("TipoInstrumento", tipo);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_TIPO_INSTRUMENTO_NO_EXISTE",params,conn, idEmpresa));
        		params.clear();
        	}
        }
        
        for(String esAutorizado : autorizados) {
        	if(!("S".equalsIgnoreCase(esAutorizado) || "N".equalsIgnoreCase(esAutorizado))) {
        		params.put("EsAutorizado",esAutorizado);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ES_AUTORIZADO_NO_ES_VALIDO",params,conn, idEmpresa));
        		params.clear();
        	}
        }
        
        for(String estaInscrito : inscritos) {
        	if(!("S".equalsIgnoreCase(estaInscrito) || "N".equalsIgnoreCase(estaInscrito))) {
        		params.put("EstaInscrito",estaInscrito);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ESTA_INSCRITO_NO_ES_VALIDO",params,conn, idEmpresa));
        		params.clear();
        	}
        }
        
        for(String esInstrumentoCalificado : calificados) {
        	if(!("S".equalsIgnoreCase(esInstrumentoCalificado) || "N".equalsIgnoreCase(esInstrumentoCalificado))) {
        		params.put("EsInstrumentoCalificado",esInstrumentoCalificado);
        		mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ES_INSTRUMENTO_CALIFICADO_NO_ES_VALIDO",params,conn, idEmpresa));
        		params.clear();
        	}
        }
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarInstrumentoWS.verificarAtributosEntrada", fin - inicio);
		return mensajes;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = getGsonBuilder();
		ConsultarInstrumentoWSEntrada inn = gson.fromJson(in.toString(), ConsultarInstrumentoWSEntrada.class);
		ConsultarInstrumentoWSSalida out = logica(conn, inn);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try (Connection con = PlataformaUtils.getPropertiesConnection("becbpro1", "connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				//String jsonDE = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_Datos\":[{\"IdTipoInstrumento\":\"ACX\"}]}";
				//String jsonDE = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_Datos\":[{\"IdTipoInstrumento\":\"ACX\"}]}";
				String jsonDE = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_Datos\":[{\"IdTipoInstrumento\":\"ACN\"},{\"IdEstado\":\"ACT\"}]}";
				ConsultarInstrumentoWSEntrada Objin = gson.fromJson(jsonDE, ConsultarInstrumentoWSEntrada.class);
							
				ConsultarInstrumentoWS kk = new ConsultarInstrumentoWS();
				ConsultarInstrumentoWSSalida Objout = kk.logica(con, Objin);
				System.out.println(Objout.toString());
				con.rollback();
				
	    	}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
}
