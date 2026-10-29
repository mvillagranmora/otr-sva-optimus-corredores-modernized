package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.ModificarAsignacionesPorDetallePerfilCobro.EntradaModificarAsignacionesPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarAsignacionesPorDetallePerfilCobro.SalidaModificarAsignacionesPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarComisionMovimientosPorDetallePerfilCobro.DetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarComisionMovimientosPorDetallePerfilCobro.EntradaModificarComisionMovimientosPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarComisionMovimientosPorDetallePerfilCobro.SalidaModificarComisionMovimientosPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarOrdenesPorDetallePerfilCobro.EntradaModificarOrdenesPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarOrdenesPorDetallePerfilCobro.SalidaModificarOrdenesPorDetallePerfilCobro;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro  implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.class);
	
	public static class EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Long> p_IdDetallePerfilCobros;
		
		public EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro() {
			this.p_IdDetallePerfilCobros = new ArrayList<>();
		}
	}
	
	public static class SalidaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro in = gson.fromJson(jin.toString(), EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.class);
		SalidaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro logica(Connection conn, EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro entrada) throws Exception {
		logger.debug("INICIO::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro");
		long inicio = System.currentTimeMillis();

		SalidaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro salida = new SalidaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro();
		
		
		
		SalidaModificarOrdenesPorDetallePerfilCobro SalidaOrdenes= new SalidaModificarOrdenesPorDetallePerfilCobro();
		SalidaOrdenes = ModificarOrdenes(conn, entrada);
		
		salida.P_OUT_MENSAJES = SalidaOrdenes.P_OUT_MENSAJES;
		salida.P_OUT_ERRORESCONTADOR += SalidaOrdenes.P_OUT_ERRORESCONTADOR;
		
		SalidaModificarAsignacionesPorDetallePerfilCobro SalidaAsignaciones= new SalidaModificarAsignacionesPorDetallePerfilCobro();
		SalidaAsignaciones = ModificarAsignaciones(conn, entrada);
		
		salida.P_OUT_MENSAJES = SalidaAsignaciones.P_OUT_MENSAJES;
		salida.P_OUT_ERRORESCONTADOR += SalidaAsignaciones.P_OUT_ERRORESCONTADOR;
		
		
		
		SalidaModificarComisionMovimientosPorDetallePerfilCobro salidaMovimientosFacturar = new SalidaModificarComisionMovimientosPorDetallePerfilCobro();
		salidaMovimientosFacturar = modificarMovimientosPorFacturar(conn, entrada);
		
		salida.P_OUT_MENSAJES = salidaMovimientosFacturar.P_OUT_MENSAJES;
		salida.P_OUT_ERRORESCONTADOR += salidaMovimientosFacturar.P_OUT_ERRORESCONTADOR;
		
		
		if(salida.P_OUT_ERRORESCONTADOR == 0 ) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_MODIFICACION_INGRESO_EXITO", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro", fin - inicio);
		return salida;
	}

	public SalidaModificarComisionMovimientosPorDetallePerfilCobro modificarMovimientosPorFacturar(Connection conn, EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro entrada)  throws Exception {
		logger.debug("INICIO::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.modificarMovimientosPorFacturar");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarComisionMovimientosPorDetallePerfilCobro salidaMovimientosFacturar = new SalidaModificarComisionMovimientosPorDetallePerfilCobro();

		EntradaModificarComisionMovimientosPorDetallePerfilCobro entradaMovimientosFacturar = new EntradaModificarComisionMovimientosPorDetallePerfilCobro();
		entradaMovimientosFacturar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaMovimientosFacturar.p_IdUsuario = entrada.p_IdUsuario;
		entradaMovimientosFacturar.p_IdModulo = entrada.p_IdModulo;
		entradaMovimientosFacturar.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		
		List<DetallePerfilCobro> detallesPerfilCobros = entrada.p_IdDetallePerfilCobros.stream().map(detalle ->{
			DetallePerfilCobro detalleEntrada = new DetallePerfilCobro();
			detalleEntrada.IdDetallePerfilCobros = detalle;
			return detalleEntrada;
		}).collect(Collectors.toList());
		
		entradaMovimientosFacturar.p_IdDetallePerfilCobros = detallesPerfilCobros;
		
		ModificarComisionMovimientosPorDetallePerfilCobro movimientosFacturar = new ModificarComisionMovimientosPorDetallePerfilCobro();
		
		salidaMovimientosFacturar = movimientosFacturar.logica(conn, entradaMovimientosFacturar);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.modificarMovimientosPorFacturar", fin - inicio);
		
		return salidaMovimientosFacturar;
	}
	public SalidaModificarOrdenesPorDetallePerfilCobro ModificarOrdenes(Connection conn, EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro entrada)  throws Exception {
		logger.debug("INICIO::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.modificarOrdenes");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarOrdenesPorDetallePerfilCobro salidaMovimientosOrdenes = new SalidaModificarOrdenesPorDetallePerfilCobro();

		EntradaModificarOrdenesPorDetallePerfilCobro entradaMovimientosOrdenes = new EntradaModificarOrdenesPorDetallePerfilCobro();
		entradaMovimientosOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaMovimientosOrdenes.p_IdUsuario = entrada.p_IdUsuario;
		entradaMovimientosOrdenes.p_IdModulo = entrada.p_IdModulo;
		entradaMovimientosOrdenes.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaMovimientosOrdenes.p_IdDetallePerfilCobros = entrada.p_IdDetallePerfilCobros;
		
		ModificarOrdenesPorDetallePerfilCobro movimientosOrdenes = new ModificarOrdenesPorDetallePerfilCobro();
		
		salidaMovimientosOrdenes = movimientosOrdenes.logica(conn, entradaMovimientosOrdenes);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.modificarOrdenes", fin - inicio);
		
		return salidaMovimientosOrdenes;
	}
	
	public SalidaModificarAsignacionesPorDetallePerfilCobro ModificarAsignaciones(Connection conn, EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro entrada)  throws Exception {
		logger.debug("INICIO::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.ModificarAsignaciones");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarAsignacionesPorDetallePerfilCobro salidaMovimientosAsignaciones = new SalidaModificarAsignacionesPorDetallePerfilCobro();

		EntradaModificarAsignacionesPorDetallePerfilCobro entradaMovimientosAsignaciones = new EntradaModificarAsignacionesPorDetallePerfilCobro();
		entradaMovimientosAsignaciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaMovimientosAsignaciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaMovimientosAsignaciones.p_IdModulo = entrada.p_IdModulo;
		entradaMovimientosAsignaciones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaMovimientosAsignaciones.p_IdDetallePerfilCobros = entrada.p_IdDetallePerfilCobros;
		
		ModificarAsignacionesPorDetallePerfilCobro movimientosAsignaciones = new ModificarAsignacionesPorDetallePerfilCobro();
		
		salidaMovimientosAsignaciones = movimientosAsignaciones.logica(conn, entradaMovimientosAsignaciones);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.ModificarAsignaciones", fin - inicio);
		
		return salidaMovimientosAsignaciones;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();					
			    				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDetallePerfilCobros\":[2890]}";

				EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro Objin = gson.fromJson(json, EntradaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro.class);
				System.out.println(Objin.toString());
				ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro modificarMovimientoComprobante = new ModificarOrdenesAsignacionesYMovimientosPorDetalleCobro();
				SalidaModificarOrdenesAsignacionesYMovimientosPorDetalleCobro Objout = modificarMovimientoComprobante.logica(con, Objin);			

				System.out.println("____Salida_____");
				System.out.println(Objout);
				con.rollback();
				//con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		}
	}
}
