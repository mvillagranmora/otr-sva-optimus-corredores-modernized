package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.PrevisualizarLiberacionMasiva.Transaccion_Proceso;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EjecutarLiberacionMasiva implements JavaCall {
	private final Logger logger = LogManager.getLogger(EjecutarLiberacionMasivaSalida.class.getName());

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EjecutarLiberacionMasivaEntrada in = gson.fromJson(jin.toString(), EjecutarLiberacionMasivaEntrada.class);
		EjecutarLiberacionMasivaSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static class EjecutarLiberacionMasivaEntrada {
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

	public static class EjecutarLiberacionMasivaSalida {
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;

		public EjecutarLiberacionMasivaSalida() {
			this.p_out_Mensajes = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}

	public EjecutarLiberacionMasivaSalida logica(Connection conn, EjecutarLiberacionMasivaEntrada entrada) throws Exception {
		logger.info("INICIO::EjecutarLiberacionMasiva");
		long inicio = System.currentTimeMillis();

		EjecutarLiberacionMasivaSalida salida = new EjecutarLiberacionMasivaSalida();

		try {
			PrevisualizarLiberacionMasiva cons = new PrevisualizarLiberacionMasiva();
			PrevisualizarLiberacionMasiva.PrevisualizarLiberacionMasivaEntrada ent = new PrevisualizarLiberacionMasiva.PrevisualizarLiberacionMasivaEntrada();

			ent.p_IdEmpresa = entrada.p_IdEmpresa;
			ent.p_IdUsuario = entrada.p_IdUsuario;
			ent.p_IdModulo = entrada.p_IdModulo;
			ent.p_FechaDesde = entrada.p_FechaDesde;
			ent.p_FechaHasta = entrada.p_FechaHasta;
			ent.p_IdLibroOrdenes = entrada.p_IdLibroOrdenes;
			ent.p_IdClasificacionTipoInstrumento = entrada.p_IdClasificacionTipoInstrumento;
			ent.p_IdTipoInstrumento = entrada.p_IdTipoInstrumento;
			ent.p_IdInstrumento = entrada.p_IdInstrumento;
			ent.p_AccionOrden = entrada.p_AccionOrden;
			ent.p_IdBolsa = entrada.p_IdBolsa;
			ent.p_IdMoneda = entrada.p_IdMoneda;
			ent.p_IdCondicionDeLiquidacion = entrada.p_IdCondicionDeLiquidacion;

			PrevisualizarLiberacionMasiva.PrevisualizarLiberacionMasivaSalida sal = cons.logica(conn, ent);

			if (MensajeSalida.getNumeroErrores(sal.p_out_Mensajes) > 0) {
				salida.p_out_Mensajes.addAll(sal.p_out_Mensajes);
				return salida;
			}

			ReservarLiberarTransacciones servicioReservarLiberar = new ReservarLiberarTransacciones();
			ReservarLiberarTransacciones.ReservarLiberarTransacciones_Entrada reservarLiberarEntrada = new ReservarLiberarTransacciones.ReservarLiberarTransacciones_Entrada();
			reservarLiberarEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			reservarLiberarEntrada.p_IdUsuario = entrada.p_IdUsuario;
			reservarLiberarEntrada.p_IdModulo = entrada.p_IdModulo;
			reservarLiberarEntrada.p_Accion = "L";
			reservarLiberarEntrada.p_Motivo = "";
			reservarLiberarEntrada.p_Origen = "TRANSACCION";

			for (Transaccion_Proceso t : sal.p_out_Transacciones) {
				ReservarLiberarTransacciones.Transaccion_Entrada tx = new ReservarLiberarTransacciones.Transaccion_Entrada();
				tx.IdTransaccion = t.IdTransaccion;
				reservarLiberarEntrada.p_Transacciones.add(tx);
			}

			ReservarLiberarTransacciones.ReservarLiberarTransacciones_Salida reservarLiberarSalida = servicioReservarLiberar.logica(conn, reservarLiberarEntrada);

			salida.p_out_Mensajes.addAll(reservarLiberarSalida.P_OUT_MENSAJES);

			long fin = System.currentTimeMillis();
			logger.debug("FIN::EjecutarLiberacionMasiva tiempo: " + (fin - inicio));

			return salida;
		} catch (Exception e) {
			logger.error("ERROR::EjecutarLiberacionMasiva", e);
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

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			EjecutarLiberacionMasivaEntrada ent = new EjecutarLiberacionMasivaEntrada();
			ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 97L;
			ent.p_IdModulo = "ORD";
			ent.p_FechaDesde = "2026-08-01 00:00:00";
			ent.p_FechaHasta = "2026-08-31 23:59:59";
			ent.p_IdBolsa = "BCS";
			EjecutarLiberacionMasiva d = new EjecutarLiberacionMasiva();
			EjecutarLiberacionMasivaSalida salida = d.logica(conn, ent);
			for (MensajeSalida m : salida.p_out_Mensajes) {
				System.out.println(m.toString());
			}
			conn.rollback();
		}
	}
}
