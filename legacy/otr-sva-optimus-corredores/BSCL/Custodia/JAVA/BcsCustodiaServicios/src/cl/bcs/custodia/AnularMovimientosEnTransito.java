package cl.bcs.custodia;

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

import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.InformacionConfiguracionCustodia;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularMovimientosEnTransito implements JavaCall{	
	static final Logger logger	= LogManager.getLogger(AnularMovimientosEnTransito.class);
	
	private final String SQL_SELECT_MOVIMIENTO_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/AnularMovimientosEnTransito_SelectMovimientoEnTransito.sql");
	
	public static class ObjMovimientos {
		public Long		IdMovimientoCarteraTransito;
		public String	IdTipoOperacion;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class MovimientoEnTransito {
		public Long			IdMovimientoCarteraEnTransito;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		Nemotecnico;
		public String		IdTipoOperacion;
		public String		IdEstado;
		public BigDecimal	Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class AnularMovimientosEnTransitoEntrada {
	    public Long					p_IdEmpresa;
	    public Long					p_IdUsuario;
	    public String				p_IdModulo;
	    public String				p_FechaOperacion;
	    public List<ObjMovimientos>	P_MOVIMIENTOSANULAR;

	    public AnularMovimientosEnTransitoEntrada (){
	    	P_MOVIMIENTOSANULAR = new ArrayList<ObjMovimientos>();
	    }
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class AnularMovimientosEnTransitoSalida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_TICKETATENCION;
		
		public AnularMovimientosEnTransitoSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
		
	public AnularMovimientosEnTransitoSalida logica(Connection con, AnularMovimientosEnTransitoEntrada in) throws Exception {
		AnularMovimientosEnTransitoSalida out = new AnularMovimientosEnTransitoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_TICKETATENCION = 0L;
		
		try {
			//******************************************************************************************************************
			//												VALIDACIONES INICIALES
			//******************************************************************************************************************			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();			
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			};
			if (in.P_MOVIMIENTOSANULAR.size() == 0 ){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("", "No hay movimientos para procesar");
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_VIENEN_MOVIMIENTOS",valores,con,in.p_IdEmpresa));
			};
		
			if (out.P_OUT_MENSAJES.size() > 0){
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			};
			
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************
			List<InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia> ConfiguracionCUS = InformacionConfiguracionCustodia.informacionConfiguracion(con, in.p_IdEmpresa);
			HashMap<String,Object> ConfigCUS = Utils.List2Hash(ConfiguracionCUS, "IdTipoOperacion");
			
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> movimientos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();			
			
			for (ObjMovimientos mov: in.P_MOVIMIENTOSANULAR){				
				InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia ConfiguracionCustodia = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUS.get(mov.IdTipoOperacion);

				if (mov.IdEstado.equalsIgnoreCase("CRT") && ConfiguracionCustodia.PermiteAnularOperacionesCertificadas.equalsIgnoreCase("S")){
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_SOLO_PERMITIDO_ANULAR_CERTIFICADO",new HashMap<>(),con,in.p_IdEmpresa));						
				} else {
					GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
					MOV.IdMovimientoCarteraEnTransito = mov.IdMovimientoCarteraTransito;
					MOV.EsAnulacion = "S";
					MOV.IdUsuario = in.p_IdUsuario;
					movimientos.add(MOV);
				};		
			};
			
			if (movimientos.size()>0){
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, movimientos);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_ANULACION_REALIZADA",new HashMap<>(),con,in.p_IdEmpresa));
				agregarLog(con, in, movimientos);
			};
		}
		catch(Exception er) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error("Error al anular",er);
		};
		return out;
	}
	
	private void agregarLog(Connection con, AnularMovimientosEnTransitoEntrada in, List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> listaMovimientos) throws Exception {
		SQL _sql = new SQL();
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	
		List<AgregarLog.InAgregarLog.Log> listaLog = new ArrayList<>();
		List<Long> idsMovimientosCarteraEnTransito = new ArrayList<Long>();
		
		for (GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar movimiento : listaMovimientos) {
			idsMovimientosCarteraEnTransito.add(movimiento.IdMovimientoCarteraEnTransito);
		}
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdMovimientoCarteraEnTransito", idsMovimientosCarteraEnTransito);
		List<MovimientoEnTransito> listaDatosMovimiento = _sql.query(con, SQL_SELECT_MOVIMIENTO_EN_TRANSITO, params, MovimientoEnTransito.class);
		HashMap<Long, Object> hDatosMovimiento = Utils.List2Hash(listaDatosMovimiento, "IdMovimientoCarteraEnTransito");
		
		for (GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar movimiento : listaMovimientos) {
			//CABECERA
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			MovimientoEnTransito datosMovimiento = (MovimientoEnTransito) hDatosMovimiento.get(movimiento.IdMovimientoCarteraEnTransito);
			
			log.IdTrabajo = 1L; 
			log.IdCabeceraLog = datosMovimiento.IdMovimientoCarteraEnTransito; //
			log.IdTipoLog = "ANULACION";
			log.IdModulo = in.p_IdModulo;
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = FechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdCliente = datosMovimiento.IdCliente; //
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = datosMovimiento.IdPortafolio;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "ANULARMOVIMIENTOENTRANSITO";
			log.IdRegistro = datosMovimiento.IdMovimientoCarteraEnTransito.toString();
			log.EstadoRegistro = datosMovimiento.IdEstado;
			log.IdTipoOperacion = datosMovimiento.IdTipoOperacion;
			log.Folio = 0L;
			log.Cantidad = datosMovimiento.Cantidad;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = datosMovimiento.Nemotecnico; //
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "cl.bcs.custodia.AnularMovimientosEnTransito";
					
			listaLog.add(log);
		}
		
		//REGISTRO
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;		
		entradaAgregarLog.p_Log = listaLog;	
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		AnularMovimientosEnTransitoEntrada jin=gson.fromJson(in.toString() ,AnularMovimientosEnTransitoEntrada.class);
		AnularMovimientosEnTransitoSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76765,\"p_IdModulo\":\"CUS\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"P_MOVIMIENTOSANULAR\":[{\"IdMovimientoCarteraTransito\":10086274,\"IdTipoOperacion\":\"RETTIT\",\"IdEstado\":\"PEN\"}],\"p_GlosaAnulacion\":\"\",\"p_FechaProceso\":\"2026-05-28\",\"p_IdSucursal\":0}";
				AnularMovimientosEnTransitoEntrada Objin = gson.fromJson(json, AnularMovimientosEnTransitoEntrada.class);
								
				AnularMovimientosEnTransito  kk = new AnularMovimientosEnTransito();
				kk.logica(con, Objin);				
				con.rollback();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
}
