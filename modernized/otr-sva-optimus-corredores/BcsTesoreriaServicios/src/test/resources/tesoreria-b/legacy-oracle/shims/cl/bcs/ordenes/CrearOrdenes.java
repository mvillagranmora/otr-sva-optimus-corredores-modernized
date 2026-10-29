package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

/**
 * ORACLE FAKE (hand-written, minimal) of cl.bcs.ordenes.CrearOrdenes: only the members ProcesarDistribucion touches
 * (ProcesarDistribucion.java:1240-1241,1292-1347,1385-1440). The assigned fields of ObjOrdenesEntrada are listed with the types of the assignments.
 * No case exercises this path.
 */
public class CrearOrdenes {

	public static class ObjOrdenesEntrada {
		public Long IdInterno;
		public String FechaRecepcion, FechaVigencia, FechaEmision, FechaOperacion, IdCondicionDeLiquidacion, IdTipoOperacion, IdTipoVigencia, Glosa;
		public BigDecimal MontoNeto, Cantidad, ValorRescate, PrecioLimite, TipoCambio, TasaVentaCorta, TasaSimultanea, TasaRFIF;
		public String EsPorMonto, IdBoveda, IdFormaCustodia, EsFacturaInmediata, IdTipoPrecio, IdTipoCanalIngreso, IdTipoReajuste, IdEmisor, IdBolsa, EspecificaBolsa;
		public String IdMonedaLiquidacionOrden, IdCondicionDeLiquidacionPlazo, EsVentaCorta, EsOD, EsFungible, IdTipoLibroOrden, IdTipoCanalDistribucion, EsOrdenEspecial;
		public String PagoCobroCuentaInversion, TipoInteres, IdMecanismoCaptacion;
		public Long IdPorcentajeVisibilidad, IdPortafolio, IdInstrumento, IdInstrumentoSubyacente, PlazoVentaCorta, PlazoSimultanea, PlazoIF, IdLibroOrdenes;
		public Long IdCuentaCustodioPortafolio, IdCorredorCorresponsal, IdPersonaAutorizadoDarOrdenes, IdPreferenciaPagoPortafolio, BaseTasa;
	}

	public static class ObjRESULTADO {
		public Long IdInterno;
		public Long IdOrden;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Timestamp FechaOperacionOrden;
		public BigDecimal Cantidad;
	}

	public static class CrearOrdenesEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public List<ObjOrdenesEntrada> p_Ordenes = new ArrayList<>();
	}

	public static class CrearOrdenesSalida {
		public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<ObjRESULTADO> P_OUT_RESULTADO = new ArrayList<>();
	}

	/** FAKE: records its input and answers "colaboradores.crearOrdenes": {"mensajes":[{id,tipo,codigo,descripcion}], "idOrdenBase": n (one result per order, IdOrden = base + index), "lanza": "text"}. */
	public CrearOrdenesSalida logica(Connection con, CrearOrdenesEntrada in) throws Exception {
		Hooks.registrar("CrearOrdenes", in);
		com.google.gson.JsonObject script = Hooks.objeto("crearOrdenes");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		CrearOrdenesSalida out = new CrearOrdenesSalida();
		if (script.has("mensajes")) {
			for (com.google.gson.JsonElement e : script.getAsJsonArray("mensajes")) out.P_OUT_MENSAJES.add(Hooks.mensaje(e.getAsJsonObject()));
		}
		long base = script.has("idOrdenBase") ? script.get("idOrdenBase").getAsLong() : 8000L;
		long i = 0;
		for (ObjOrdenesEntrada o : in.p_Ordenes) {
			ObjRESULTADO r = new ObjRESULTADO();
			r.IdInterno = o.IdInterno;
			r.IdOrden = base + (i++);
			out.P_OUT_RESULTADO.add(r);
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;
	}
}
