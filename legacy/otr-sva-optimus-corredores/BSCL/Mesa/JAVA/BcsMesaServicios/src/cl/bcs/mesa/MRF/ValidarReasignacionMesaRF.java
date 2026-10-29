package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidarReasignacionMesaRF implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ValidarReasignacionMesaRF.class);
	
	private final String SQL_SELECT_OPERACION = loadResourceToString("cl/bcs/mesa/MRF/sql/ValidarReasignacionMesaRF_SelectConsultarOperacionMesa.sql");
	private final String SQL_SELECT_CARTERA = loadResourceToString("cl/bcs/mesa/MRF/sql/ValidarReasignacionMesaRF_SelectConsultarCarteraOperacionMesa.sql");
	
	
	public static class EntradaValidarReasignacionMesaRF extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_FolioOperacion;
		public Long p_FolioCartera;
		public BigDecimal p_CantidadAsignada;
		public Boolean p_Procesar;
		public Long p_CantidadAProcesar;
		public Long p_IdInstrumento;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_TipoOperacion;
		public Boolean p_AfectaCustodia;
	
		public EntradaValidarReasignacionMesaRF(Long p_IdEmpresa , Long p_IdUsuario, String p_IdModulo, Long p_FolioOperacion, Long p_FolioCartera, BigDecimal p_CantidadAsignada, Boolean p_Procesar, Long p_CantidadAProcesar, Long p_IdInstrumento, Long p_IdCliente, Long p_IdPortafolio, String p_TipoOperacion, Boolean p_AfectaCustodia) {
			this.p_IdEmpresa = p_IdEmpresa;
			this.p_IdUsuario = p_IdUsuario;
			this.p_IdModulo = p_IdModulo;
			this.p_FolioOperacion = p_FolioOperacion;
			this.p_FolioCartera = p_FolioCartera;
			this.p_CantidadAsignada = p_CantidadAsignada;
			this.p_Procesar = p_Procesar;
			this.p_CantidadAProcesar = p_CantidadAProcesar;
			this.p_IdInstrumento = p_IdInstrumento;
			this.p_IdCliente = p_IdCliente;
			this.p_IdPortafolio = p_IdPortafolio;
			this.p_TipoOperacion = p_TipoOperacion;
			this.p_AfectaCustodia = p_AfectaCustodia;			
		}
		
	}
	
	public static class Archivo extends BcsPojo{
		public Long IdEmpresa;
		public Long IdUsuario;
		public String p_IdModulo;
		public Long p_FolioOperacion;
		public Long p_FolioCartera;
		public BigDecimal CantidadAsignada;
		public Boolean p_Procesar;
		public Long p_CantidadAProcesar;
	}
	
	public static class SalidaValidarReasignacionMesaRF extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidarReasignacionMesaRF() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class DatosOperacion{
		
		// DATOS OPERACION
		public Long FolioOperacion;
		public String NombreOperacion;
		public Long IdOperacion;
		public Date FechaOperacion;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public BigDecimal MontoOperacion;
		public BigDecimal Tasa;
		public Date FechaVencimiento;
		public String Glosa = "";
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosCartera{
		
		// DATOS CARTERA
		public Long FolioCarteraCustodiaDetalle;
		public String NemotecnicoDetalle;
		public String IdClasificacionTipoInstrumentoDetalle;
		public BigDecimal TasaCompraDetalle;
		public BigDecimal CantidadReservadaDetalle;
		public BigDecimal ValorRescateDetalle;
		public BigDecimal CorteMinimoDetalle;
		public Date FechaOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaValidarReasignacionMesaRF jin = gson.fromJson(in.toString(), EntradaValidarReasignacionMesaRF.class);
		SalidaValidarReasignacionMesaRF out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	// ------------------------------------------------------------
	// LOGICA
	// ------------------------------------------------------------
	
	public SalidaValidarReasignacionMesaRF logica(Connection conn, EntradaValidarReasignacionMesaRF in) throws Exception {
		logger.info("INICIO::ValidarReasignacionMesaRF");
		
		SalidaValidarReasignacionMesaRF out = new SalidaValidarReasignacionMesaRF();
		String v_FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.info("INICIO - VALIDAR CUSTODIA");
		if (in.p_AfectaCustodia && in.p_TipoOperacion == "COMTER") {
			
			ConsultarCustodiaPortafolio consultarCustodia = new ConsultarCustodiaPortafolio();
			ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio paramsCustodia = new ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio();
			paramsCustodia.p_AgrupaPorBoveda = "N";
			paramsCustodia.p_AgrupaPorRelacion = "S";
			paramsCustodia.p_FechaConsulta = v_FechaSistema;
			paramsCustodia.p_IdBoveda = "";
			paramsCustodia.p_IdClasificacionTipoInstrumento = "";
			paramsCustodia.p_IdCliente = in.p_IdCliente;
			paramsCustodia.p_IdEmpresa = in.p_IdEmpresa;
			paramsCustodia.p_IdInstrumento = in.p_IdInstrumento;
			paramsCustodia.p_IdModulo = "CUS";
			paramsCustodia.p_IdPortafolio = in.p_IdPortafolio;
			paramsCustodia.p_IdProductoAgrupador = null;
			paramsCustodia.p_IdTipoInstrumento = "";
			paramsCustodia.p_IdUsuario = in.p_IdUsuario;
			paramsCustodia.p_SaldosNegativos = "N";
			paramsCustodia.p_SaldosNegativosPresuntos = "N";
			paramsCustodia.p_SolicitaDividendo = "S";
			paramsCustodia.p_TipoFechaConsulta = "ADQ";
			ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio salidaCustodia = consultarCustodia.logica(conn, paramsCustodia);	
		
			if(salidaCustodia.P_OUT_TBL.get(0).CantidadCustodia.compareTo(in.p_CantidadAsignada) > 0  ) {
				out.P_OUT_ERRORESCONTADOR = 1L;	
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CUSTODIA_INSUFICIENTE","Cliente no tiene custodia"));				
				return out;
			}
		} 

		logger.info("INICIO - VALIDAR FOLIOS DISTINTOS DE 0");
		if (in.p_FolioCartera == 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;	
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTEN_FOLIOS","La operacion tiene movimientos en transito."));				
			return out;
		} else {
		
			// CONSULTA SQL:
			SQL _sql = new SQL();
			
			HashMap<String,Object> paramsOpe = new HashMap<>();
			paramsOpe.put("FolioOperacion", in.p_FolioOperacion);
			
			// CONSULTA OPERACIONES
			logger.debug("INICIO - Select Obtener datos operacion");
			DatosOperacion datosOperacion = new DatosOperacion();
			List<DatosOperacion> operacion = _sql.query(conn, SQL_SELECT_OPERACION, paramsOpe, DatosOperacion.class);
			for (DatosOperacion ope: operacion) {
				datosOperacion = ope;
			}
			logger.debug("FIN - Select Obtener datos operacion");
			
			HashMap<String,Object> paramsCart = new HashMap<>();
			paramsCart.put("FolioOperacion", in.p_FolioOperacion);
			paramsCart.put("FolioCartera", in.p_FolioCartera);
			
			// CONSULTA CARTERAS
			logger.debug("INICIO - Select Obtener datos operacion");
			DatosCartera datosCartera = new DatosCartera();
			List<DatosCartera> cartera = _sql.query(conn, SQL_SELECT_CARTERA, paramsCart, DatosCartera.class);
			for (DatosCartera cart: cartera) {
				datosCartera = cart;
			}
			logger.debug("FIN - Select Obtener datos operacion");
			
			
			// AQUI EMPIEZAN LAS VALIDACIONES:
				
			if(operacion.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;	
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCUENTRA_OPERACION","No se encontro la operacion"));				
				return out;
			}
			
			if(cartera.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;		
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCUENTRA_OPERACION","No se encontro la cartera"));				
				return out;
			}
			
			// VALIDACION PARA QUE LA CANTIDAD ASIGNADA NO SUPERE LA CANTIDAD DEL INSTRUMENTO DE LA OPERACION ORIGINAL.
			if(datosCartera.IdClasificacionTipoInstrumentoDetalle.equals("RF")) {
				if(in.p_CantidadAsignada.compareTo(datosCartera.CantidadReservadaDetalle) > 0) {
					out.P_OUT_ERRORESCONTADOR = 1L;	
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CANTIDAD_MAYOR_TOTAL","La cantidad asignada es mayor que el total del instrumento de la operacion original."));				
					return out;
				} else {
					
					if (in.p_CantidadAsignada.compareTo(datosCartera.CantidadReservadaDetalle) != 0 ) {
											
						if (datosCartera.CorteMinimoDetalle.compareTo(new BigDecimal(0)) == 0) {
							datosCartera.CorteMinimoDetalle = new BigDecimal(1);
						}
						
						if (in.p_CantidadAsignada.compareTo(datosCartera.CorteMinimoDetalle) < 0) {
							out.P_OUT_ERRORESCONTADOR = 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
									"CANTIDAD_MENOR_CORTEMINIMO",
									"La cantidad asignada es menor que el corte minimo posible para la reasignacion."));
							return out;
						} else if(in.p_CantidadAsignada.remainder(datosCartera.CorteMinimoDetalle).compareTo(new BigDecimal(0)) != 0) {
							out.P_OUT_ERRORESCONTADOR = 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
									"CANTIDAD_NO_MULTIPLO",
									"La cantidad asignada no es multiplo del corte minimo posible para la reasignacion."));
						}
					}				
				}
			} else if(datosCartera.IdClasificacionTipoInstrumentoDetalle.equals("IF")) {
				if(in.p_CantidadAsignada.compareTo(datosCartera.ValorRescateDetalle) > 0) {
					out.P_OUT_ERRORESCONTADOR = 1L;	
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CANTIDAD_MAYOR_TOTAL","La cantidad asignada es mayor que el total del instrumento de la operacion original."));				
					return out;
				} else {
					
					if (in.p_CantidadAsignada.compareTo(datosCartera.ValorRescateDetalle) != 0 ) {

						if (datosCartera.CorteMinimoDetalle.compareTo(new BigDecimal(0)) == 0) {
							datosCartera.CorteMinimoDetalle = new BigDecimal(1);
						}

						if (in.p_CantidadAsignada.compareTo(datosCartera.CorteMinimoDetalle) < 0) {
							out.P_OUT_ERRORESCONTADOR = 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
									"CANTIDAD_MENOR_CORTEMINIMO",
									"La cantidad asignada es menor que el corte minimo posible para la reasignacion."));
							return out;
						} else if (in.p_CantidadAsignada.remainder(datosCartera.CorteMinimoDetalle).compareTo(new BigDecimal(0)) != 0) {
							out.P_OUT_ERRORESCONTADOR = 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
									"CANTIDAD_NO_MULTIPLO",
									"La cantidad asignada no es multiplo del corte minimo posible para la reasignacion."));
						}
					}			
				}
			}
		}
		
		// AQUI SE RECOPILAN LOS ERRORES Y MENSAJES:
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		logger.info("FIN::ValidarReasignacionMesaRF");
		return out;
	
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"MRF\",\"p_FolioOperacion\":20148,\"p_FolioCartera\":20704534,\"p_CantidadAsignada\":100,\"p_Procesar\":true,\"p_CantidadAProcesar\":2}";
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":164,\"p_IdModulo\":\"MRF\",\"p_FolioOperacion\":21973,\"p_FolioCartera\":20706309,\"p_CantidadAsignada\":107933333,\"p_AfectaCustodia\":true,\"p_IdInstrumento\":330184,\"p_Procesar\":false,\"p_IdCliente\":1423325,\"p_IdPortafolio\":1450341,\"p_TipoOperacion\":\"COMTER\"}";
		EntradaValidarReasignacionMesaRF Objin = gson.fromJson(json, EntradaValidarReasignacionMesaRF.class);
		ValidarReasignacionMesaRF demo = new ValidarReasignacionMesaRF();				
		try {
			SalidaValidarReasignacionMesaRF ObjOut = demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}			
    	};
		System.out.println("== END ==");
	}
	
}
