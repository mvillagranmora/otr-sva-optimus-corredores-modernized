package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.AdministrarComisiones.Comision;
import cl.bcs.fondomutuo.AdministrarComisiones.EntradaAdministrarComision;
import cl.bcs.fondomutuo.AdministrarComisiones.SalidaAdministrarComision;
import cl.bcs.fondomutuo.AdministrarComisiones.TramoComision;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarComision implements JavaCall{

	private final String SQL_UPDATE_COMISION = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarComision_UpdateComision.sql");
	static final Logger logger = LogManager.getLogger(ModificarComision.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaAdministrarComision logica(Connection conn, EntradaAdministrarComision entrada)  throws Exception{
		logger.info("INICIO::ModificarComision");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salida = new SalidaAdministrarComision();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String, String> paramsMensaje = new HashMap<>(2);
		
		if(entrada.p_Comision.isEmpty()) {
			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE", paramsMensaje, conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.stream().count();
			return salida;
		}
		
		Long cantidadTramosNuevos = entrada.p_Comision.stream().map(com -> {
			return com.TramosComision.stream().filter(tramo -> tramo.IdTramo == 0L);
		}).count();
		
		List<Long> secuenciaTramos = Utils.getSecuencia(conn, "BSCL.FondoMutuo.BD.Secuencias::TramoComision", cantidadTramosNuevos);
		AtomicInteger indiceTramos = new AtomicInteger();

		List<TramoComision> tramosAModificar = new ArrayList<>();
		List<TramoComision> tramosACrear = new ArrayList<>();
		List<TramoComision> tramosAQuitar = new ArrayList<>();
		
		for(Comision com : entrada.p_Comision) {
			com.FechaSistema = fechaSistema;
			
			com.TramosComision.stream().forEach(tramo ->{
				tramo.FechaSistema = fechaSistema;
				tramo.IdComision = com.IdComision;
				if(tramo.IdTramo.equals(0L)) {
					tramo.Estado = "ACT";
					tramo.IdTramo = secuenciaTramos.get(indiceTramos.getAndIncrement());
					tramosACrear.add(tramo);
				}else {
					tramosAModificar.add(tramo);
				}
			});
			
			if(!com.TramosQuitados.isEmpty()) {
				tramosAQuitar.addAll(com.TramosQuitados);
			}
		}
		
		modificarComisiones(conn, entrada.p_Comision);
		
		if(!tramosACrear.isEmpty()) {
			SalidaAdministrarComision salidaCrear = crearTramos(conn, tramosACrear);
			salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaCrear.P_OUT_ADVERTENCIASDETALLE);
			salida.P_OUT_ERRORESDETALLE.addAll(salidaCrear.P_OUT_ERRORESDETALLE);
		}
		if(!tramosAModificar.isEmpty()) {
			SalidaAdministrarComision salidaModificar = modificarTramos(conn, tramosAModificar);
			salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaModificar.P_OUT_ADVERTENCIASDETALLE);
			salida.P_OUT_ERRORESDETALLE.addAll(salidaModificar.P_OUT_ERRORESDETALLE);
		}
		
		if(!tramosAQuitar.isEmpty()) {
			SalidaAdministrarComision salidaModificar = quitarTramos(conn, tramosAQuitar);
			salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaModificar.P_OUT_ADVERTENCIASDETALLE);
			salida.P_OUT_ERRORESDETALLE.addAll(salidaModificar.P_OUT_ERRORESDETALLE);
		}
		
		salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.stream().count();
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.stream().count();
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarComision", finServicio - inicioServicio);
		return salida;
	}

	private SalidaAdministrarComision quitarTramos(Connection conn, List<TramoComision> tramosAQuitar) throws Exception {
		logger.info("INICIO::ModificarComision.quitarTramos");
		long inicioServicio = System.currentTimeMillis();
		
		QuitarTramoComision quitar = new QuitarTramoComision();
		SalidaAdministrarComision salidaQuitar = new SalidaAdministrarComision();
		
		salidaQuitar = quitar.logica(conn, tramosAQuitar);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarComision.quitarTramos", finServicio - inicioServicio);
		return salidaQuitar;
	}

	private void modificarComisiones(Connection conn, List<Comision> comisiones) throws Exception {
		logger.info("INICIO::ModificarComision.modificarComisiones");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_COMISION, comisiones, Comision.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarComision.modificarComisiones", finServicio - inicioServicio);
	}

	private SalidaAdministrarComision modificarTramos(Connection conn, List<TramoComision> tramosAModificar) throws Exception {
		logger.info("INICIO::ModificarComision.modificarTramos");
		long inicioServicio = System.currentTimeMillis();
		
		ModificarTramoComision modificar = new ModificarTramoComision();
		SalidaAdministrarComision salidaModificar = new SalidaAdministrarComision();
		
		salidaModificar = modificar.logica(conn, tramosAModificar);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarComision.modificarTramos", finServicio - inicioServicio);
		return salidaModificar;
	}
	
	private SalidaAdministrarComision crearTramos(Connection conn, List<TramoComision> tramosACrear) throws Exception {
		logger.info("INICIO::ModificarComision.crearTramos");
		long inicioServicio = System.currentTimeMillis();
		
		CrearTramoComision crear = new CrearTramoComision();
		SalidaAdministrarComision salidaCrear = new SalidaAdministrarComision();
		
		salidaCrear = crear.logica(conn, tramosACrear);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarComision.crearTramos", finServicio - inicioServicio);
		return salidaCrear;
	}
}
