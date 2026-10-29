package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.EliminarComisionMovimientosPorDetallePerfilCobro.SalidaEliminarComisionMovimientosPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarAsignacionesPorDetallePerfilCobro.EntradaModificarAsignacionesPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarAsignacionesPorDetallePerfilCobro.SalidaModificarAsignacionesPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarOrdenesPorDetallePerfilCobro.EntradaModificarOrdenesPorDetallePerfilCobro;
import cl.bcs.plataforma.com.ModificarOrdenesPorDetallePerfilCobro.SalidaModificarOrdenesPorDetallePerfilCobro;
import cl.bcs.plataforma.com.QuitarDetallePerfilCobro.QuitarDetallePerfilCobro_Entrada;
import cl.bcs.plataforma.com.QuitarDetallePerfilCobro.QuitarDetallePerfilCobro_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCallExtra;
import cl.bcs.utils.Utils;

public class EliminarComisionOrdenesAsignacionesYMovimientosPorDetalle implements JavaCallExtra {
	static final Logger logger = LogManager.getLogger(EliminarComisionOrdenesAsignacionesYMovimientosPorDetalle.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin, JsonObject jout) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		QuitarDetallePerfilCobro_Entrada in = gson.fromJson(jin.toString(), QuitarDetallePerfilCobro_Entrada.class);
		QuitarDetallePerfilCobro_Salida out = gson.fromJson(jin.toString(), QuitarDetallePerfilCobro_Salida.class);
		
		SalidaEliminarComisionOrdenesAsignacionesYMovimientosPorDetalle outExtra = logica(conn,in,out);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(outExtra)).getAsJsonObject();
		return dataout;
	}
	
	public static class SalidaEliminarComisionOrdenesAsignacionesYMovimientosPorDetalle extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaEliminarComisionOrdenesAsignacionesYMovimientosPorDetalle() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public SalidaEliminarComisionOrdenesAsignacionesYMovimientosPorDetalle logica(Connection conn, QuitarDetallePerfilCobro_Entrada entrada, QuitarDetallePerfilCobro_Salida salida) throws Exception {
		logger.debug("INICIO::EliminarComisionOrdenesAsignacionesYMovimientosPorDetalle");
		long inicio = System.currentTimeMillis();
		
		EliminarComisionMovimientosPorDetallePerfilCobro movimientosFacturar = new EliminarComisionMovimientosPorDetallePerfilCobro();
		SalidaEliminarComisionOrdenesAsignacionesYMovimientosPorDetalle salidaExtra = new SalidaEliminarComisionOrdenesAsignacionesYMovimientosPorDetalle();
		
		SalidaEliminarComisionMovimientosPorDetallePerfilCobro salidaMovimientosFacturar = movimientosFacturar.logica(conn, entrada);
		
		salidaExtra.P_OUT_MENSAJES.addAll(salidaMovimientosFacturar.P_OUT_MENSAJES);
		
		SalidaModificarOrdenesPorDetallePerfilCobro SalidaOrdenes= new SalidaModificarOrdenesPorDetallePerfilCobro();
		SalidaOrdenes = ModificarOrdenes(conn, entrada);
		
		salidaExtra.P_OUT_MENSAJES.addAll(SalidaOrdenes.P_OUT_MENSAJES);
		
		SalidaModificarAsignacionesPorDetallePerfilCobro SalidaAsignaciones= new SalidaModificarAsignacionesPorDetallePerfilCobro();
		SalidaAsignaciones = ModificarAsignaciones(conn, entrada);
		
		salidaExtra.P_OUT_MENSAJES.addAll(SalidaAsignaciones.P_OUT_MENSAJES);
		
	//	salidaExtra.P_OUT_ERRORESCONTADOR = salidaExtra.P_OUT_MENSAJES.stream().filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count(); 
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionOrdenesAsignacionesYMovimientosPorDetalle", fin - inicio);
		return salidaExtra;
	}
	

	public SalidaModificarOrdenesPorDetallePerfilCobro ModificarOrdenes(Connection conn, QuitarDetallePerfilCobro_Entrada entrada)  throws Exception {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro.modificarOrdenes");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarOrdenesPorDetallePerfilCobro salidaMovimientosOrdenes = new SalidaModificarOrdenesPorDetallePerfilCobro();

		EntradaModificarOrdenesPorDetallePerfilCobro entradaMovimientosOrdenes = new EntradaModificarOrdenesPorDetallePerfilCobro();
		entradaMovimientosOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaMovimientosOrdenes.p_IdUsuario = entrada.p_IdUsuario;
		entradaMovimientosOrdenes.p_IdModulo = entrada.p_IdModulo;
		entradaMovimientosOrdenes.p_UsaReglasNegocio = "N";
		
		List<Long>Aux = new ArrayList<>();
		entrada.p_DetallesPerfilCobro.stream().forEach(DetallesPerfilCobro -> {
		Aux.add(	DetallesPerfilCobro.IdDetallePerfilCobros);		});
		
	    entradaMovimientosOrdenes.p_IdDetallePerfilCobros = Aux;
		
		ModificarOrdenesPorDetallePerfilCobro movimientosOrdenes = new ModificarOrdenesPorDetallePerfilCobro();
		
		salidaMovimientosOrdenes = movimientosOrdenes.logica(conn, entradaMovimientosOrdenes);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro.modificarOrdenes", fin - inicio);
		
		return salidaMovimientosOrdenes;
	}
	
	public SalidaModificarAsignacionesPorDetallePerfilCobro ModificarAsignaciones(Connection conn, QuitarDetallePerfilCobro_Entrada entrada)  throws Exception {
		logger.debug("INICIO::EliminarComisionMovimientosPorDetallePerfilCobro.ModificarAsignaciones");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarAsignacionesPorDetallePerfilCobro salidaMovimientosAsignaciones = new SalidaModificarAsignacionesPorDetallePerfilCobro();

		EntradaModificarAsignacionesPorDetallePerfilCobro entradaMovimientosAsignaciones = new EntradaModificarAsignacionesPorDetallePerfilCobro();
		entradaMovimientosAsignaciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaMovimientosAsignaciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaMovimientosAsignaciones.p_IdModulo = entrada.p_IdModulo;
		entradaMovimientosAsignaciones.p_UsaReglasNegocio = "N";
		
		List<Long>Aux = new ArrayList<>();
		entrada.p_DetallesPerfilCobro.stream().forEach(DetallesPerfilCobro -> {
		Aux.add(	DetallesPerfilCobro.IdDetallePerfilCobros);		});
		
		entradaMovimientosAsignaciones.p_IdDetallePerfilCobros = Aux;
		
		ModificarAsignacionesPorDetallePerfilCobro movimientosAsignaciones = new ModificarAsignacionesPorDetallePerfilCobro();
		
		salidaMovimientosAsignaciones = movimientosAsignaciones.logica(conn, entradaMovimientosAsignaciones);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarComisionMovimientosPorDetallePerfilCobro.ModificarAsignaciones", fin - inicio);
		
		return salidaMovimientosAsignaciones;
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
			
			String salida= "{\"P_OUT_ERRORESCONTADOR\":0,\"p_out_Mensajes\":[]}";
			
			QuitarDetallePerfilCobro_Entrada Objin = gson.fromJson(entrada, QuitarDetallePerfilCobro_Entrada.class);
			QuitarDetallePerfilCobro_Salida Objout = gson.fromJson(salida, QuitarDetallePerfilCobro_Salida.class);
			
			EliminarComisionOrdenesAsignacionesYMovimientosPorDetalle demo = new EliminarComisionOrdenesAsignacionesYMovimientosPorDetalle();

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaEliminarComisionOrdenesAsignacionesYMovimientosPorDetalle ObjOut  = demo.logica(con, Objin, Objout);
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
