package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

public class CrearComision implements JavaCall{

	private final String SQL_INSERT_COMISION = loadResourceToString("cl/bcs/fondomutuo/sql/CrearComision_InsertComision.sql");
	private final String SQL_INSERT_TRAMO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearTramoComision_InsertTramo.sql");
	static final Logger logger = LogManager.getLogger(CrearComision.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaAdministrarComision logica(Connection conn, EntradaAdministrarComision entrada)  throws Exception{
		logger.info("INICIO::CrearComision");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salida = new SalidaAdministrarComision();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String, String> paramsMensaje = new HashMap<>(2);
		
		if(entrada.p_Comision.isEmpty()) {
			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE", paramsMensaje, conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.stream().count();
			return salida;
		}
		
		List<Long> secuenciaComisiones = Utils.getSecuencia(conn, "BSCL.FondoMutuo.BD.Secuencias::Comision", Long.valueOf(entrada.p_Comision.size()));
		
		Long cantidadTramos = entrada.p_Comision.stream().map(com -> {
			return com.TramosComision;
		}).count();
		
		List<Long> secuenciaTramos = Utils.getSecuencia(conn, "BSCL.FondoMutuo.BD.Secuencias::TramoComision", cantidadTramos);
		AtomicInteger indiceComisiones = new AtomicInteger();
		AtomicInteger indiceTramos = new AtomicInteger();
		List<TramoComision> tramosACrear = new ArrayList<>();
		
		for(Comision com : entrada.p_Comision) {
			com.IdEmpresa = entrada.p_IdEmpresa;
			com.IdComision = secuenciaComisiones.get(indiceComisiones.getAndIncrement());
			com.FechaSistema = fechaSistema;
			tramosACrear.addAll( com.TramosComision.stream().map(tramo ->{
				tramo.Estado = "ACT";
				tramo.FechaSistema = fechaSistema;
				tramo.IdComision = com.IdComision;
				tramo.IdTramo = tramo.IdTramo.equals(0L) ? secuenciaTramos.get(indiceTramos.getAndIncrement()) : tramo.IdTramo;
				return tramo;
			}).collect(Collectors.toList()));
		}
		
		insertarComisiones(conn, entrada.p_Comision);
			
		if(!tramosACrear.isEmpty()) {
			SalidaAdministrarComision salidaCrear = crearTramos(conn, tramosACrear);
			salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaCrear.P_OUT_ADVERTENCIASDETALLE);
			salida.P_OUT_ERRORESDETALLE.addAll(salidaCrear.P_OUT_ERRORESDETALLE);
		}
		
		salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.stream().count();
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.stream().count();
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearComision", finServicio - inicioServicio);
		return salida;
	}

	private void insertarComisiones(Connection conn, List<Comision> comisiones) throws Exception {
		logger.info("INICIO::CrearComision.insertarComisiones");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_INSERT_COMISION, comisiones, Comision.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearComision.insertarComisiones", finServicio - inicioServicio);
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
