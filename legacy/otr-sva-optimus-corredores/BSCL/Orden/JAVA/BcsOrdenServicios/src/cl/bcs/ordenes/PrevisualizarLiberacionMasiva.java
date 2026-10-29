package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class PrevisualizarLiberacionMasiva implements JavaCall {

	private final Logger logger = LogManager.getLogger(PrevisualizarLiberacionMasiva.class.getName());

	private final String SQL_PREVISUALIZAR_LIBERACION = loadResourceToString("cl/bcs/ordenes/sql/PrevisualizarLiberacionMasiva_ConsultarTransaccionesReservadas.sql");

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		PrevisualizarLiberacionMasivaEntrada in = gson.fromJson(jin.toString(), PrevisualizarLiberacionMasivaEntrada.class);
		PrevisualizarLiberacionMasivaSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static class PrevisualizarLiberacionMasivaEntrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public Long p_IdLibroOrdenes;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoInstrumento;
		public Long p_IdInstrumento;
		public String p_AccionOrden;
		public String p_IdBolsa;
		public String p_IdMoneda;
		public String p_IdCondicionDeLiquidacion;
	}

	public static class PrevisualizarLiberacionMasivaSalida extends BcsPojo {
		public List<Transaccion_Proceso> p_out_Transacciones;
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;

		public PrevisualizarLiberacionMasivaSalida() {
			p_out_Transacciones = new ArrayList<>();
			p_out_Mensajes = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}

	public PrevisualizarLiberacionMasivaSalida logica(Connection conn, PrevisualizarLiberacionMasivaEntrada entrada) throws Exception {
		PrevisualizarLiberacionMasivaSalida salida = new PrevisualizarLiberacionMasivaSalida();

		try {
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida requisitosBasicosSalida = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);

			if (MensajeSalida.getNumeroErrores(requisitosBasicosSalida.p_out_Mensajes) > 0) {
				salida.p_out_Mensajes.addAll(requisitosBasicosSalida.p_out_Mensajes);
				return salida;
			}

			if (entrada.p_FechaDesde == null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FECHA_DESDE_VACIA", ""));
				return salida;
			}
			if (entrada.p_FechaHasta == null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FECHA_HASTA_VACIA", ""));
				return salida;
			}

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
				sdf.parse(entrada.p_FechaDesde);
			} catch (ParseException e) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FORMATO_FECHA_DESDE_INCORRECTO", ""));
				return salida;
			}

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
				sdf.parse(entrada.p_FechaHasta);
			} catch (ParseException e) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FORMATO_FECHA_HASTA_INCORRECTO", ""));
				return salida;
			}

			SQL sql = new SQL();
			HashMap<String, Object> parms = new HashMap<>(2);
			parms.put("FechaDesde", entrada.p_FechaDesde);
			parms.put("FechaHasta", entrada.p_FechaHasta);

			List<Transaccion_Proceso> transacciones = sql.query(conn, SQL_PREVISUALIZAR_LIBERACION, parms, Transaccion_Proceso.class);

			for (Transaccion_Proceso tran : transacciones) {
				if (entrada.p_AccionOrden != null && (tran.AccionOrden == null || !tran.AccionOrden.equalsIgnoreCase(entrada.p_AccionOrden))) {
					continue;
				}
				if (entrada.p_IdBolsa != null && (tran.IdBolsa == null || !tran.IdBolsa.equalsIgnoreCase(entrada.p_IdBolsa))) {
					continue;
				}
				if (entrada.p_IdInstrumento != null && (tran.IdInstrumento == null || tran.IdInstrumento.longValue() != entrada.p_IdInstrumento.longValue())) {
					continue;
				}
				if (entrada.p_IdLibroOrdenes != null && (tran.IdLibroOrdenes == null || tran.IdLibroOrdenes.longValue() != entrada.p_IdLibroOrdenes.longValue())) {
					continue;
				}
				if (entrada.p_IdClasificacionTipoInstrumento != null && (tran.IdClasificacionTipoInstrumento == null || !tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(entrada.p_IdClasificacionTipoInstrumento))) {
					continue;
				}
				if (entrada.p_IdTipoInstrumento != null && (tran.IdTipoInstrumento == null || !tran.IdTipoInstrumento.equalsIgnoreCase(entrada.p_IdTipoInstrumento))) {
					continue;
				}
				if (entrada.p_IdMoneda != null && (tran.IdMoneda == null || !tran.IdMoneda.equalsIgnoreCase(entrada.p_IdMoneda))) {
					continue;
				}
				if (entrada.p_IdCondicionDeLiquidacion != null && (tran.IdCondicionDeLiquidacion == null || !tran.IdCondicionDeLiquidacion.equalsIgnoreCase(entrada.p_IdCondicionDeLiquidacion))) {
					continue;
				}
				salida.p_out_Transacciones.add(tran);
			}
			return salida;
		} catch (Exception e) {
			logger.error("ERROR::PrevisualizarLiberacionMasiva", e);
			salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_INESPERADO", ""));
			return salida;
		} finally {
			for (MensajeSalida msje : salida.p_out_Mensajes) {
				if (msje.descripcion == null || msje.descripcion.isEmpty()) {
					msje.descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, msje.codigo, new HashMap());
				}
			}
		}
	}

	public static class Transaccion_Proceso extends BcsPojo {
		public Long IdTransaccion;
		public String FolioTransaccion;
		public String FechaSistemaCreacion;
		public String HoraSistemaCreacion;
		public String Nemotecnico;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String AccionOrden;
		public String IdBolsa;
		public String IdMoneda;
		public String IdCondicionDeLiquidacion;
		public String NombreCondicionDeLiquidacion;
		public Long IdLibroOrdenes;
		public String NombreLibroOrdenes;
		public String Reservado;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			PrevisualizarLiberacionMasivaEntrada ent = new PrevisualizarLiberacionMasivaEntrada();
			ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 97L;
			ent.p_IdModulo = "ORD";
			ent.p_FechaDesde = "2026-08-01 00:00:00";
			ent.p_FechaHasta = "2026-08-31 23:59:59";
			ent.p_IdBolsa = "BCS";
			PrevisualizarLiberacionMasiva d = new PrevisualizarLiberacionMasiva();
			PrevisualizarLiberacionMasivaSalida salida = d.logica(conn, ent);
			for (Transaccion_Proceso tran : salida.p_out_Transacciones) {
				System.out.println(tran.toString());
			}
		}
	}
}
