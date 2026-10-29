package cl.bcs.plataforma.evt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLADETALLE;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import datadog.trace.api.Trace;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class CrearSolicitud implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearSolicitud.class);

	public static class CrearSolicitudEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Solicitud> p_Solicitud;

		public CrearSolicitudEntrada() {
			p_Solicitud = new ArrayList<Solicitud>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Solicitud {
		public Long IdCliente;
		public String CorredoraRut;
		public String CorredoraDireccion;
		public String CorredoraTelefono;
		public String FechaSolicitud;
		public String ClienteRut;
		public String ClientePortafolio;
		public String ClienteDireccion;
		public String ClienteNombre;
		public String solicitud;
		public String SolicitudProductos;
		public String SolicitudInstrumentos;
		public String SolicitudAgregar;
		public String SolicitudMotivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CrearSolicitudSalida {
		public String P_OUT;
		public Long P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearSolicitudEntrada in = gson.fromJson(jin.toString(), CrearSolicitudEntrada.class);
		CrearSolicitudSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/*
	 * Logica Servicio
	 * 
	 */
	@Trace
	public CrearSolicitudSalida logica(Connection con, CrearSolicitudEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		CrearSolicitudSalida out = new CrearSolicitudSalida();
		logger.info("Inicio servicio AgregarEvento");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA",
				"PLA.FechaSistema");

		for (Solicitud solicitud : in.p_Solicitud) {
			CrearEventoAutorizacionMasivo.ServiceEntrada inCrearAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
			List<ObjTABLAEVENTOS> tablaEventos = new ArrayList<ObjTABLAEVENTOS>();
			List<ObjTABLADETALLE> tablaEventoDetalle = new ArrayList<ObjTABLADETALLE>();
			ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();

			evento.IdUnion = 0L;
			evento.IdTipoEvento = "SOLPRO";
			evento.IdModuloSolicita = in.p_IdModulo;
			evento.IdUsuarioSolicita = in.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdTipoOperacion = "";
			evento.IdConcepto = "SOLPRO";
			evento.NumeroOperacionConcepto = "";
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = solicitud.IdCliente;
			evento.IdFuncionario = 0L;
			evento.IdProveedor = 0L;
			evento.IdTipoProducto = "";
			evento.IdClasificacionTipoInstrumento = "";
			evento.IdTipoInstrumento = "";
			evento.IdInstrumento = 0L;
			evento.Nemo = "";
			evento.Cantidad = 0L;
			evento.Monto = BigDecimal.ZERO;
			evento.Precio = BigDecimal.ZERO;
			evento.Tasa = BigDecimal.ZERO;
			evento.IdMoneda = "";
			evento.Glosa = "Solicitud Agregar Productos y/o Instrumentos " + solicitud.SolicitudAgregar + " "
					+ solicitud.SolicitudMotivo;

			tablaEventos.add(evento);

			ObjTABLADETALLE ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1"; // indica que este valor ira a nivel de cabecera
			ItemEventoDetalle.NombreCampo = "CorredoraRut";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.CorredoraRut;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "CorredoraDireccion";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.CorredoraDireccion;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "CorredoraTelefono";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.CorredoraTelefono;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "SolicitudFecha";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.FechaSolicitud;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "ClienteRut";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.ClienteRut;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "ClientePortafolio";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.ClientePortafolio;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "ClienteNombre";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.ClienteNombre;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "ClienteDireccion";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.ClienteDireccion;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "Productos";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.SolicitudProductos;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "Instrumentos";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.SolicitudInstrumentos;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "Agregar";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.SolicitudAgregar;
			tablaEventoDetalle.add(ItemEventoDetalle);

			ItemEventoDetalle = new ObjTABLADETALLE();
			ItemEventoDetalle.IdUnion = evento.IdUnion;
			ItemEventoDetalle.Clasificacion = "1";
			ItemEventoDetalle.NombreCampo = "Motivos";
			ItemEventoDetalle.TipoDato = "STRING";
			ItemEventoDetalle.Valor = solicitud.SolicitudMotivo;
			tablaEventoDetalle.add(ItemEventoDetalle);

			CrearEventoAutorizacionMasivo crearAutorizacionService = new CrearEventoAutorizacionMasivo();
			inCrearAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
			inCrearAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
			inCrearAutorizacion.P_IDMODULO = in.p_IdModulo;
			inCrearAutorizacion.P_SIMULAROPERACION = "N";
			inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";
			inCrearAutorizacion.P_TABLAEVENTOS = tablaEventos;
			inCrearAutorizacion.P_DETALLE = tablaEventoDetalle;

			CrearEventoAutorizacionMasivo.ServiceSalida autoriza = crearAutorizacionService.logica(con,
					inCrearAutorizacion);
			if (autoriza.P_OUT_ERRORESCONTADOR != 0) {
				out.P_OUT = autoriza.P_OUT_ERRORESDETALLE.get(0).Descripcion;
				out.P_OUT_ERRORESCONTADOR = autoriza.P_OUT_ERRORESCONTADOR;
			} else {
				out.P_OUT = "OK";
				out.P_OUT_ERRORESCONTADOR = autoriza.P_OUT_ERRORESCONTADOR;
			}
		}
		logger.info("Termino del servicio CrearSolicitud, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":164,\"p_IdModulo\":\"PLA\", \"p_Solicitud\":[{\"IdCliente\":20338, \"FechaSolicitud\":\"2019-08-02\",\"CorredoraRut\":\"96489000-5\", \"CorredoraDireccion\":\"APOQUINDO 3000 PISO 16\", \"CorredoraTelefono\":\"+56952562542\", \"ClienteRut\":\"333-6\", \"ClientePortafolio\":\"1\", \"ClienteNombre\":\"PABLO PROJO MERINO\", \"ClienteDireccion\":\"MONEDA 1025\", \"SolicitudProductos\":\"Productos\", \"SolicitudInstrumentos\":\"Instrumentos\", \"SolicitudAgregar\":\"sdfsdfsf\", \"SolicitudMotivo\":\"sdfsfsfdf\"}]}";

			CrearSolicitudEntrada Objin = gson.fromJson(json, CrearSolicitudEntrada.class);
			CrearSolicitud demo = new CrearSolicitud();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				CrearSolicitudSalida ObjOut = demo.logica(con, Objin);
				 //con.rollback();
				con.commit();
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				// er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}
