package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar.MovimientoComprobante;
import cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar.MovimientoComprobanteCobrosModificar;
import cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.Cobro;
import cl.bcs.facturacion.ModificarMovimientoComprobanteCobrosMasivo.SalidaModificarMovimientoComprobanteCobrosMasivo;
import cl.bcs.plataforma.com.ModificarComisionMovimientosPorDetallePerfilCobro.DetallePerfilCobros;
import cl.bcs.plataforma.com.ModificarComisionMovimientosPorDetallePerfilCobro.EntradaModificarComisionMovimientosPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarComisionMovimientosPorDetallePerfilCobro.MovimientoComprobanteCobro;
import cl.bcs.plataforma.com.QuitarDetallePerfilCobro.DetallePerfilCobro_Entrada;
import cl.bcs.plataforma.com.QuitarDetallePerfilCobro.QuitarDetallePerfilCobro_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class EliminarComisionMovimientosPorDetallePerfilCobro implements JavaCall {
	static final Logger logger = LogManager.getLogger(EliminarComisionOrdenesAsignacionesYMovimientosPorDetalle.class);
	
	private final String SQL_SELECT_DETALLE_PERFIL_COBROS_ELIMINADOS = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarComisionMovimientosPorDetallePerfilCobro_SelectDetallePerfilCobros.sql");
	private final String SQL_SELECT_DETALLE_PERFIL_COBROS_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarComisionMovimientosPorDetallePerfilCobro_SelectDetallePerfilCobrosBase.sql");
	private final String SQL_SELECT_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarComisionMovimientosPorDetallePerfilCobro_SelectMovimientosComprobantesCobros.sql");
	private final String SQL_DELETE_COBROS = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarComisionMovimientosPorDetallePerfilCobro_DeleteMovimientosComprobantesCobros.sql");
	
	public static class SalidaEliminarComisionMovimientosPorDetallePerfilCobro extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaEliminarComisionMovimientosPorDetallePerfilCobro() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public SalidaEliminarComisionMovimientosPorDetallePerfilCobro logica(Connection conn, QuitarDetallePerfilCobro_Entrada entrada) throws Exception {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro");
		long inicio = System.currentTimeMillis();
		
		SalidaEliminarComisionMovimientosPorDetallePerfilCobro salida = new SalidaEliminarComisionMovimientosPorDetallePerfilCobro();
		
		SalidaModificarMovimientoComprobanteCobrosMasivo salidaModificar = new SalidaModificarMovimientoComprobanteCobrosMasivo();
		ModificarComisionMovimientosPorDetallePerfilCobro modificar = new ModificarComisionMovimientosPorDetallePerfilCobro();
		
		List<DetallePerfilCobros> detallesPerfilCobrosBorrados = obtenerDatosDetallePerfilCobros(conn, entrada.p_DetallesPerfilCobro);
		
		List<DetallePerfilCobros> detallesPerfilCobrosBase = obtenerDatosDetallePerfilCobrosBase(conn, detallesPerfilCobrosBorrados);
		
		if(detallesPerfilCobrosBorrados.isEmpty()) {
//			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PERFIL_COBROS_NO_EXISTE", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
//			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_MENSAJES.stream().filter( m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count(); 
			return salida; 
		}
		
		modificar.generarTablaTemporal(conn, detallesPerfilCobrosBorrados);
		
		List<MovimientoComprobanteCobro> movimientosComprobanteCobrosAnteriores = obtenerMovimientosComprobanteCobros(conn, detallesPerfilCobrosBorrados);
		modificar.dropTablaTemporal(conn);
		
		for( DetallePerfilCobros detalle : detallesPerfilCobrosBorrados) {
			
			List<MovimientoComprobanteCobro> cobrosAnterioresFiltrados = modificar.filtrarMovimientosPorCanalIngresoYTipoOperacion(movimientosComprobanteCobrosAnteriores, detalle);
			
			List<DetallePerfilCobros> perfilCobroBase = obtenerPerfilCobrosBaseParaModificarComision(detallesPerfilCobrosBase, detalle);
			
			if(perfilCobroBase.isEmpty()) {
				borrarCobros(conn,cobrosAnterioresFiltrados);
			}else {
				List<MovimientoComprobante> comprobantes = cobrosAnterioresFiltrados.parallelStream().map(mov -> {
					MovimientoComprobante movimientoComprobante = new MovimientoComprobante();
					movimientoComprobante.IdMovimientoComprobante = mov.IdMovimientoComprobante;
					return movimientoComprobante;
				}).collect(Collectors.toList());
				
				DetallePerfilCobros perfilBase = perfilCobroBase.get(0);
				
				EntradaModificarComisionMovimientosPorDetallePerfilCobro entradaModificar =new EntradaModificarComisionMovimientosPorDetallePerfilCobro();
				entradaModificar.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaModificar.p_IdModulo = entrada.p_IdModulo;
				entradaModificar.p_IdUsuario = entrada.p_IdUsuario;
				entradaModificar.p_UsaReglasNegocio = "N";
				
				List<MovimientoComprobanteCobrosModificar> movimientosComprobanteCobrosModificar = modificar.obtenerConsultarMovimientoComprobanteCobrosModificar(conn, entradaModificar, perfilBase.IdCobro, comprobantes);
				
				Long cantidadComprobantes = comprobantes.parallelStream().count();
				BigDecimal montoCobroDetalle = perfilBase.ValorCobro.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : perfilBase.ValorCobro;
				List<Cobro> cobros = new ArrayList<Cobro>();
				if(detalle.IdCobro.equals("COVA")) {
					cobros = modificar.obtenerCobrosAModificarComisionVariable(conn, movimientosComprobanteCobrosModificar, perfilBase.ValorCobro);				
				}
				if(detalle.IdCobro.equals("COFI")) {
					cobros = modificar.obtenerCobrosAModificarComisionFija(conn, movimientosComprobanteCobrosModificar, cantidadComprobantes, montoCobroDetalle);
				}
				salidaModificar = modificar.modificarMovimientoComprobanteCobrosMasivo(conn, entradaModificar, comprobantes, cobros, detalle.IdCobro);
				
				if(salidaModificar.P_OUT_ERRORESCONTADOR>0) {
					salida.P_OUT_MENSAJES = salidaModificar.P_OUT_MENSAJES;
					salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_MENSAJES.stream().filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count(); 
					return salida;
				}
			}
		}
		
		if(salida.P_OUT_ERRORESCONTADOR==0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_MODIFICACION_INGRESO_EXITO", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro", fin - inicio);
		return salida;
	}

	public void borrarCobros(Connection conn, List<MovimientoComprobanteCobro> comprobantesCobros) throws Exception {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro.borrarCobros");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_DELETE_COBROS, comprobantesCobros, MovimientoComprobanteCobro.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro.borrarCobros", fin - inicio);
	}

	private List<DetallePerfilCobros> obtenerPerfilCobrosBaseParaModificarComision(List<DetallePerfilCobros> detallesPerfilCobrosBase, DetallePerfilCobros detalle) {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerPerfilCobrosBaseParaModificarComision");
		long inicio = System.currentTimeMillis();
		
		List<DetallePerfilCobros> detallePerfilcobroBase = detallesPerfilCobrosBase;
		
		if(!detalle.IdTipoCanalIngreso.equals("0")) {
			detallePerfilcobroBase = detallePerfilcobroBase.stream().filter(det -> det.IdTipoCanalIngreso.equals(detalle.IdTipoCanalIngreso) || det.IdTipoCanalIngreso.equals("0") ).collect(Collectors.toList());	
		}

		if(!detalle.IdTipoOperacion.equals("0")) {
			detallePerfilcobroBase = detallePerfilcobroBase.stream().filter(det -> det.IdTipoOperacion.equals(detalle.IdTipoOperacion) || det.IdTipoOperacion.equals("0") ).collect(Collectors.toList());	
		}

		if(!detalle.IdBolsa.equals("0")) {
			detallePerfilcobroBase = detallePerfilcobroBase.stream().filter(det -> det.IdBolsa.equals(detalle.IdBolsa) || det.IdBolsa.equals("0") ).collect(Collectors.toList());	
		}
		
		detallePerfilcobroBase = detallePerfilcobroBase.stream().filter(det -> det.IdCobro.equals(detalle.IdCobro)).collect(Collectors.toList());	

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerPerfilCobrosBaseParaModificarComision", fin - inicio);
		return detallePerfilcobroBase;
	}

	private List<MovimientoComprobanteCobro> obtenerMovimientosComprobanteCobros(Connection conn, List<DetallePerfilCobros> detallesPerfilCobrosBorrados) throws Exception {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerMovimientosComprobanteCobros");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		
		SQL sql = new SQL();
		List<MovimientoComprobanteCobro> movimientosComprobantesCobros = sql.query(conn, SQL_SELECT_COBROS, parametros, MovimientoComprobanteCobro.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerMovimientosComprobanteCobros", fin - inicio);
		return movimientosComprobantesCobros;
	}

	private List<DetallePerfilCobros> obtenerDatosDetallePerfilCobros(Connection conn, List<DetallePerfilCobro_Entrada> detallesPerfilCobro) throws Exception {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		List<Long> idDetallePerfilCobros = detallesPerfilCobro.stream()
				.map(detalle ->detalle.IdDetallePerfilCobros)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("IdDetallePerfilCobros", idDetallePerfilCobros);
		
		SQL sql = new SQL();
		List<DetallePerfilCobros> detallesBorrados = sql.query(conn, SQL_SELECT_DETALLE_PERFIL_COBROS_ELIMINADOS, parametros, DetallePerfilCobros.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros", fin - inicio);
		return detallesBorrados;
	}

	private List<DetallePerfilCobros> obtenerDatosDetallePerfilCobrosBase(Connection conn, List<DetallePerfilCobros> detallesPerfilCobrosBorrados) throws Exception {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		List<Long> idPortafolios = detallesPerfilCobrosBorrados.stream()
				.map(detalle ->detalle.IdPortafolio)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("IdPortafolio", idPortafolios);
		
		SQL sql = new SQL();
		List<DetallePerfilCobros> detallesBorrados = sql.query(conn, SQL_SELECT_DETALLE_PERFIL_COBROS_BASE, parametros, DetallePerfilCobros.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro.obtenerDatosDetallePerfilCobros", fin - inicio);
		return detallesBorrados;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		
		return null;
	}
	
	public static void main(String[] args){
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getDESAConnection())  {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\r\n"
					+ "\"p_IdDetallePerfilCobros\":[ { \"IdDetallePerfilCobros\" : 	132261	} ,\r\n"
					+ "{ \"IdDetallePerfilCobros\" : 	131087	} ,\r\n"
					+ "{ \"IdDetallePerfilCobros\" : 	131632	} ,\r\n"
					+ "{ \"IdDetallePerfilCobros\" : 	131633	} ,\r\n"
					+ "{ \"IdDetallePerfilCobros\" : 	132299	} ,\r\n"
					+ "{ \"IdDetallePerfilCobros\" : 	132298	} ]}";
			String entrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":208,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_DetallesPerfilCobro\":[{ \"IdDetallePerfilCobros\" : 	131624	} ]}";
			
			QuitarDetallePerfilCobro_Entrada Objin = gson.fromJson(entrada, QuitarDetallePerfilCobro_Entrada.class);
			
			EliminarComisionMovimientosPorDetallePerfilCobro demo = new EliminarComisionMovimientosPorDetallePerfilCobro();

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaEliminarComisionMovimientosPorDetallePerfilCobro ObjOut  = demo.logica(con, Objin);
				con.rollback();
//				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
		
}
