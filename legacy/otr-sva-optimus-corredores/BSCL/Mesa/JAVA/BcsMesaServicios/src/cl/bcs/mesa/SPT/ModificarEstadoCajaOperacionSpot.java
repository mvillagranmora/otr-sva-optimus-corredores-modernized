package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;

public class ModificarEstadoCajaOperacionSpot implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ModificarEstadoCajaOperacionSpot.class);
	
	private final String SQL_SELECT_OPERACIONES_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ConfirmarLiquidacionSpot_ConsultarOperacionesSpot.sql");
	private final String SQL_UPDATE_ESTADO_CAJA_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/ModificarEstadoCajaOperacionSpot_UpdateEstadoCaja.sql");


	public static class ModificarEstadoCajaOperacionSpotEntrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String IdCanal;
		public List<EstadoCajaEntrada> EstadoCajaModificar;

		public ModificarEstadoCajaOperacionSpotEntrada() {
			this.EstadoCajaModificar = new ArrayList<>();
			this.p_IdModulo = "MSP";
		}

	}
	
	public static class EstadoCajaEntrada extends BcsPojo {
		public Long IdOperacion;
		public String NuevoEstado;
		
		public EstadoCajaEntrada() {
			this.IdOperacion = 0L;
			this.NuevoEstado = "";
		}
		
		public EstadoCajaEntrada(Long IdOperacion, String NuevoEstado) {
			this.IdOperacion = IdOperacion;
			this.NuevoEstado = NuevoEstado;
		}
	}

	public static class ModificarEstadoCajaOperacionSpotSalida extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public ModificarEstadoCajaOperacionSpotSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public enum EstadosCaja {
		PEN,
		CNF,
		NRE;
		
		private static final Set<String> ESTADOS_VALIDOS = Arrays.stream(values()).map(Enum::name).collect(Collectors.toSet());
		
		public static boolean esValido(String estado) {
			return ESTADOS_VALIDOS.contains(estado);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ModificarEstadoCajaOperacionSpotEntrada entrada = gson.fromJson(in.toString(), ModificarEstadoCajaOperacionSpotEntrada.class);
		ModificarEstadoCajaOperacionSpotSalida out = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ModificarEstadoCajaOperacionSpotSalida logica(Connection conn,ModificarEstadoCajaOperacionSpotEntrada entrada) throws Exception {
		logger.info("Inicio servicio ModificarEstadoCajaOperacionSpot");
		ModificarEstadoCajaOperacionSpotSalida out = new ModificarEstadoCajaOperacionSpotSalida();
		HashMap<String,String> mapMsg = new HashMap<>();
		Long id = 0L;

		logger.info("Validar Operaciones entrada " + entrada);
		if (entrada.EstadoCajaModificar == null || entrada.EstadoCajaModificar.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(++out.P_OUT_ERRORESCONTADOR, MensajeSalida.MENSAJE_ERROR,"NO_HAY_OPERACIONES_PARA_MODIFICAR", mapMsg, conn, entrada.p_IdEmpresa));
			return out;
		}
		
		List<EstadoCajaEntrada> estadosValidos = new ArrayList<>();
		
		for (EstadoCajaEntrada estado : entrada.EstadoCajaModificar) {
		    if (!EstadosCaja.esValido(estado.NuevoEstado)) {
		    	mapMsg.put("IdOperacion", String.valueOf(estado.IdOperacion));
		        mapMsg.put("IdEstado", estado.NuevoEstado);
		        out.P_OUT_MENSAJES.add(new MensajeSalida(++id,MensajeSalida.MENSAJE_ADVERTENCIA,"SPOT_MODIFICAR_ESTADO_CAJA_ERROR",mapMsg,conn,entrada.p_IdEmpresa));
		        mapMsg.clear();
		        continue;
		    } 
		    estadosValidos.add(estado);
		}
		
		if(estadosValidos.isEmpty()) {
			return out;
		}
		
		logger.info("Consultar info registros validos, " + estadosValidos);
		SQL _sql = new SQL();
		List<Long> NumeroOperacion = estadosValidos.stream().map(data -> data.IdOperacion).collect(Collectors.toList());
		LocalDate  fechaSistema = LocalDate.parse(ConfiguracionSistema.getParametroConfiguracionString(conn,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"));
		
		HashMap<String,Object> params = new HashMap<>();
		params.put("NumeroOperacion", NumeroOperacion);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<OperacionSpot> selectOpeSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT, params, OperacionSpot.class);
		
		if(selectOpeSpot.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(++out.P_OUT_ERRORESCONTADOR, MensajeSalida.MENSAJE_ERROR,"NO_HAY_OPERACIONES_PARA_MODIFICAR", mapMsg, conn, entrada.p_IdEmpresa));
			return out;
		}
		
		HashMap<Long, Object> HashSelectOpeSpot = Utils.List2Hash(selectOpeSpot, "IdOperacionSpot");
		HashMap<Long, Object> HashDatosValidos = Utils.List2Hash(estadosValidos, "IdOperacion");
		
		for (EstadoCajaEntrada estado : estadosValidos) {

		    if (!HashSelectOpeSpot.containsKey(estado.IdOperacion)) {
		        mapMsg.put("IdOperacion", String.valueOf(estado.IdOperacion));
		        out.P_OUT_MENSAJES.add(new MensajeSalida(++id,MensajeSalida.MENSAJE_ADVERTENCIA,"SPOT_OPERACION_NO_EXISTE",mapMsg,conn,entrada.p_IdEmpresa));
		        mapMsg.clear();
		    }
		}
		
		List<EstadoCajaEntrada> estadoCajaActualizar = new ArrayList<>();
		
		for(OperacionSpot opSp : selectOpeSpot) {
			LocalDate  fechaOperacion = LocalDate.parse(Fecha.toStringDate(opSp.FechaOperacion));
			
			if(fechaSistema.isBefore(fechaOperacion)) {
				mapMsg.put("IdOperacion", String.valueOf(opSp.IdOperacionSpot));
				mapMsg.put("FechaSistema", fechaSistema.toString());
				mapMsg.put("FechaOperacion", fechaOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(++id, MensajeSalida.MENSAJE_ADVERTENCIA, "SPOT_ESTADO_CAJA_ERROR_FECHA_MODIFICACION", mapMsg, conn, entrada.p_IdEmpresa));
				mapMsg.clear();
				continue;
			}
			
			if("CNF".equalsIgnoreCase(opSp.EstadoConfirmacion)) {
				mapMsg.put("IdOperacion", String.valueOf(opSp.IdOperacionSpot));
				out.P_OUT_MENSAJES.add(new MensajeSalida(++id, MensajeSalida.MENSAJE_ADVERTENCIA, "SPOT_MODIFICAR_ESTADO_CAJA_OPERACION_CONFIRMADA", mapMsg, conn, entrada.p_IdEmpresa));
				mapMsg.clear();
				continue;
			}
			
			EstadoCajaEntrada estadoActualizar = (EstadoCajaEntrada) HashDatosValidos.get(opSp.IdOperacionSpot);
			
			if(estadoActualizar.NuevoEstado.equalsIgnoreCase(opSp.EstadoCaja)) {
				mapMsg.put("IdOperacion", String.valueOf(estadoActualizar.IdOperacion));
				mapMsg.put("OldEstado", String.valueOf(opSp.EstadoCaja));
				mapMsg.put("NewEstado", String.valueOf(estadoActualizar.NuevoEstado));
				out.P_OUT_MENSAJES.add(new MensajeSalida(++id, MensajeSalida.MENSAJE_ADVERTENCIA, "SPOT_ESTADO_CAJA_MISMO_ESTADO", mapMsg, conn, entrada.p_IdEmpresa));
				mapMsg.clear();
				continue;
			}
			
			estadoCajaActualizar.add(new EstadoCajaEntrada(opSp.IdOperacionSpot, estadoActualizar.NuevoEstado));
		}
		
		if(estadoCajaActualizar.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(++id, MensajeSalida.MENSAJE_INFORMACION,"NO_HAY_OPERACIONES_PARA_MODIFICAR", mapMsg, conn, entrada.p_IdEmpresa));
			return out;
		}
		
		logger.info("actualizar estado caja registros validos, " + estadosValidos.toString());
		
		_sql.batch(conn, SQL_UPDATE_ESTADO_CAJA_SPOT, estadoCajaActualizar, EstadoCajaEntrada.class);
		
		for(EstadoCajaEntrada estadoCaja : estadoCajaActualizar) {
			OperacionSpot opeSp = (OperacionSpot) HashSelectOpeSpot.get(estadoCaja.IdOperacion);
			
			mapMsg.put("IdOperacion", String.valueOf(estadoCaja.IdOperacion));
			mapMsg.put("OldEstado", String.valueOf(opeSp.EstadoCaja));
			mapMsg.put("NewEstado", String.valueOf(estadoCaja.NuevoEstado));
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(++id, MensajeSalida.MENSAJE_OK, "SPOT_ESTADO_CAJA_MODIFICADO", mapMsg, conn, entrada.p_IdEmpresa));
			
			// AGREGAR LOG DE CAMBIO DE ESTADO:
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAgregarLog.p_IdModulo = entrada.p_IdModulo;
			entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = opeSp.IdOperacionSpot;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema.toString();
			log.IdUsuarioAtendido = 0L;
			log.IdCliente = opeSp.IdCliente;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "CAMBIOESTADOCAJASPOT";
			log.EstadoRegistro = opeSp.Estado;
			log.IdTipoOperacion = opeSp.IdTipoOperacion;
			log.Folio = opeSp.IdOperacionSpot;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = opeSp.MontoMonedaSecundaria;
			log.IdMonedaOrigen = opeSp.IdMonedaPrincipal;
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = opeSp.TipoCambioCierre != null ? opeSp.TipoCambioCierre.toString() : "";
			log.Nemo = opeSp.Nemotecnico;
			log.IdProceso = 0L;
			log.EstadoProceso = opeSp.Estado;
			log.NombreProceso = "ModificarEstadoCajaOperacionSpot";						
			entradaAgregarLog.p_Log.add(log);
			
			InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = opeSp.IdOperacionSpot;
			detLog.IdTrabajo=0L;
			detLog.SubRegistro = "Operacion: " + estadoCaja.IdOperacion;
			detLog.CampoRegistro = "ESTADO CAJA";
			detLog.ValorAntiguo = opeSp.EstadoCaja;
			detLog.ValorNuevo = estadoCaja.NuevoEstado;
			entradaAgregarLog.p_LogDetalle.add(detLog);
			
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
			
			mapMsg.clear();
		}
		
		logger.info("Consultar info registros validos, " + out);
		return out;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"MSP\",\"IdCanal\":\"WBS\",\"EstadoCajaModificar\":[{\"IdOperacion\":83492236,\"NuevoEstado\":\"CNF\"}]}";
			ModificarEstadoCajaOperacionSpotEntrada Objin = gson.fromJson(json, ModificarEstadoCajaOperacionSpotEntrada.class);
			ModificarEstadoCajaOperacionSpot demo = new ModificarEstadoCajaOperacionSpot();
			ModificarEstadoCajaOperacionSpotSalida out = demo.logica(con, Objin);
			System.out.println(out);
			con.rollback();
		}
	}

}
