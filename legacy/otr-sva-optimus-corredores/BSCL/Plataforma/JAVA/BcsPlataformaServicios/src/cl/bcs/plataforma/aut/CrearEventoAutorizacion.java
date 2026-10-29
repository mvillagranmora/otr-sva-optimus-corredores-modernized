package cl.bcs.plataforma.aut;

/*
 * Wrapper para llamar al stored procedure CrearEventoAutorizacion
 * Patrón similar a CrearEventoAutorizacionMasivo
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearEventoAutorizacion implements JavaCall {

	/*
	 * El log
	 */
	static final Logger logger = LogManager.getLogger(CrearEventoAutorizacion.class);

	/*
	 * Clases utilitarias que representan objetos IN/OUT
	 */
	public static class ServiceEntrada {

		// Parámetros comunes
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public String	p_simularOperacion = "N";
		public String	p_UsaReglasNegocio = "N";
		
		// Parámetros específicos del servicio
		public String		p_IdTipoEvento;
		public String		p_IdModuloSolicita;
		public Long			p_IdUsuarioSolicita;
		public String		p_Fecha;
		public String		p_IdTipoOperacion = "";
		public String		p_IdConcepto = "";
		public String		p_NumeroOperacionConcepto;
		public String		p_FechaOperacionConcepto;
		public Long			p_IdCliente = 0L;
		public Long			p_IdFuncionario = 0L;
		public Long			p_IdProveedor = 0L;
		public String		p_IdTipoProducto = "";
		public String		p_IdClasificacionTipoInstrumento = "";
		public String		p_IdTipoInstrumento = "";
		public Long			p_IdInstrumento = 0L;
		public String		p_Nemo = "";
		public Long			p_Cantidad = 0L;
		public BigDecimal	p_Monto = BigDecimal.ZERO;
		public BigDecimal	p_Precio = BigDecimal.ZERO;
		public BigDecimal	p_Tasa = BigDecimal.ZERO;
		public String		p_IdMoneda = "";
		public String		p_Glosa = "";
		public String		p_IdCanal = "OPT";

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServiceSalida {

		public Long							p_out_IdEvento;
		public Long							p_out_AdvertenciasContador;
		public List<ObjADVERTENCIASDETALLE>	p_out_AdvertenciasDetalle;
		public Long							p_out_ErroresContador;
		public List<ObjERRORESDETALLE>		p_out_ErroresDetalle;
		
		public ServiceSalida() {
			this.p_out_IdEvento = 0L;
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_ErroresContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<ObjADVERTENCIASDETALLE>();
			this.p_out_ErroresDetalle = new ArrayList<ObjERRORESDETALLE>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*
	 * Clases utilitarias que representan objetos de procesamiento
	 */
	public static class ObjADVERTENCIASDETALLE {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}

	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}

	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ServiceEntrada in = gson.fromJson(jin.toString(), ServiceEntrada.class);
		ServiceSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/*
	 * Logica Servicio
	 */
	@Trace
	public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception {
		SQL sql = new SQL();
		ServiceSalida out = new ServiceSalida();
		logger.info("INICIO CrearEventoAutorizacion");
		
		try {
			// Ejecuta el SP
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();

			parmsIN.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIN.put("p_IdUsuario", in.p_IdUsuario);
			parmsIN.put("p_IdModulo", in.p_IdModulo);
			parmsIN.put("p_simularOperacion", in.p_simularOperacion);
			parmsIN.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			parmsIN.put("p_IdTipoEvento", in.p_IdTipoEvento);
			parmsIN.put("p_IdModuloSolicita", in.p_IdModuloSolicita);
			parmsIN.put("p_IdUsuarioSolicita", in.p_IdUsuarioSolicita);
			parmsIN.put("p_Fecha", in.p_Fecha);
			parmsIN.put("p_IdTipoOperacion", in.p_IdTipoOperacion);
			parmsIN.put("p_IdConcepto", in.p_IdConcepto);
			parmsIN.put("p_NumeroOperacionConcepto", in.p_NumeroOperacionConcepto);
			parmsIN.put("p_FechaOperacionConcepto", in.p_FechaOperacionConcepto);
			parmsIN.put("p_IdCliente", in.p_IdCliente);
			parmsIN.put("p_IdFuncionario", in.p_IdFuncionario);
			parmsIN.put("p_IdProveedor", in.p_IdProveedor);
			parmsIN.put("p_IdTipoProducto", in.p_IdTipoProducto);
			parmsIN.put("p_IdClasificacionTipoInstrumento", in.p_IdClasificacionTipoInstrumento);
			parmsIN.put("p_IdTipoInstrumento", in.p_IdTipoInstrumento);
			parmsIN.put("p_IdInstrumento", in.p_IdInstrumento);
			parmsIN.put("p_Nemo", in.p_Nemo);
			parmsIN.put("p_Cantidad", in.p_Cantidad);
			parmsIN.put("p_Monto", in.p_Monto);
			parmsIN.put("p_Precio", in.p_Precio);
			parmsIN.put("p_Tasa", in.p_Tasa);
			parmsIN.put("p_IdMoneda", in.p_IdMoneda);
			parmsIN.put("p_Glosa", in.p_Glosa);
			parmsIN.put("p_IdCanal", in.p_IdCanal);

			HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

			parmsOUT.put("p_out_IdEvento", Long.class);
			parmsOUT.put("p_out_AdvertenciasContador", Long.class);
			parmsOUT.put("p_out_AdvertenciasDetalle", ObjADVERTENCIASDETALLE.class);
			parmsOUT.put("p_out_ErroresContador", Long.class);
			parmsOUT.put("p_out_ErroresDetalle", ObjERRORESDETALLE.class);
			
			logger.debug("INICIO::Call CrearEventoAutorizacion SP");
			String queryCall = "call \"BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion\" ("
					+ ":p_IdEmpresa,"
					+ ":p_IdUsuario,"
					+ ":p_IdModulo,"
					+ ":p_simularOperacion,"
					+ ":p_UsaReglasNegocio,"
					+ ":p_IdTipoEvento,"
					+ ":p_IdModuloSolicita,"
					+ ":p_IdUsuarioSolicita,"
					+ ":p_Fecha,"
					+ ":p_IdTipoOperacion,"
					+ ":p_IdConcepto,"
					+ ":p_NumeroOperacionConcepto,"
					+ ":p_FechaOperacionConcepto,"
					+ ":p_IdCliente,"
					+ ":p_IdFuncionario,"
					+ ":p_IdProveedor,"
					+ ":p_IdTipoProducto,"
					+ ":p_IdClasificacionTipoInstrumento,"
					+ ":p_IdTipoInstrumento,"
					+ ":p_IdInstrumento,"
					+ ":p_Nemo,"
					+ ":p_Cantidad,"
					+ ":p_Monto,"
					+ ":p_Precio,"
					+ ":p_Tasa,"
					+ ":p_IdMoneda,"
					+ ":p_Glosa,"
					+ ":p_IdCanal,"
					+ "@p_out_IdEvento,"
					+ "@p_out_AdvertenciasContador,"
					+ "$p_out_AdvertenciasDetalle,"
					+ "@p_out_ErroresContador,"
					+ "$p_out_ErroresDetalle)";
			
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIN, parmsOUT);
			logger.debug("FIN::Call CrearEventoAutorizacion SP");

			// Lee scalar
			out.p_out_IdEvento = (Long) rpt.get("p_out_IdEvento");
			out.p_out_AdvertenciasContador = (Long) rpt.get("p_out_AdvertenciasContador");
			out.p_out_ErroresContador = (Long) rpt.get("p_out_ErroresContador");

			// Lee tablas
			out.p_out_AdvertenciasDetalle = (List<ObjADVERTENCIASDETALLE>) rpt.get("p_out_AdvertenciasDetalle");
			out.p_out_ErroresDetalle = (List<ObjERRORESDETALLE>) rpt.get("p_out_ErroresDetalle");

		} catch (Exception e) {
			logger.error("Error en CrearEventoAutorizacion", e);
			throw e;
		}
		
		logger.info("TERMINO CrearEventoAutorizacion");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties");
			
			CrearEventoAutorizacion c = new CrearEventoAutorizacion();
			ServiceEntrada in = new ServiceEntrada();
			in.p_IdEmpresa = 1L;
			in.p_IdUsuario = 25L;
			in.p_IdModulo = "AUT";
			in.p_simularOperacion = "N";
			in.p_UsaReglasNegocio = "N";
			
			// Datos del evento
			in.p_IdTipoEvento = "EJEMPL";
			in.p_IdModuloSolicita = "ORD";
			in.p_IdUsuarioSolicita = 25L;
			in.p_Fecha = "2020-03-17";
			in.p_IdTipoOperacion = "";
			in.p_IdConcepto = "INGORD";
			in.p_NumeroOperacionConcepto = "12345";
			in.p_FechaOperacionConcepto = "2020-03-17";
			in.p_IdCliente = 0L;
			in.p_IdFuncionario = 0L;
			in.p_IdProveedor = 0L;
			in.p_IdTipoProducto = "";
			in.p_IdClasificacionTipoInstrumento = "";
			in.p_IdTipoInstrumento = "";
			in.p_IdInstrumento = 0L;
			in.p_Nemo = "";
			in.p_Cantidad = 0L;
			in.p_Monto = BigDecimal.ZERO;
			in.p_Precio = BigDecimal.ZERO;
			in.p_Tasa = BigDecimal.ZERO;
			in.p_IdMoneda = "";
			in.p_Glosa = "Prueba de evento de autorización";
			in.p_IdCanal = "OPT";
			
			ServiceSalida out = c.logica(con, in);
			
			System.out.println("Resultado:");
			System.out.println("IdEvento: " + out.p_out_IdEvento);
			System.out.println("Errores: " + out.p_out_ErroresContador);
			System.out.println("Advertencias: " + out.p_out_AdvertenciasContador);
			
			if (out.p_out_ErroresContador > 0) {
				System.out.println("Detalle errores:");
				for (ObjERRORESDETALLE error : out.p_out_ErroresDetalle) {
					System.out.println("  - " + error.Codigo + ": " + error.Descripcion);
				}
			}
			
			con.rollback();			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
