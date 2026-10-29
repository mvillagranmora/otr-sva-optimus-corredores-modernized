package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaReinversionesFondoMutuo implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CargaReinversionesFondoMutuo.class);
	
	private final String SQL_SELECT_REINVERSIONES = loadResourceToString("cl/bcs/fondomutuo/sql/CargaReinversionesFondoMutuo_SelectReinversiones.sql");
	private final String SQL_INSERT_REINVERSIONES = loadResourceToString("cl/bcs/fondomutuo/sql/CargaReinversionesFondoMutuo_InsertReinversiones.sql");
	
	public static class EntradaCargaReinversionesFondoMutuo{
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario = 0L;
		public Long p_IdCarga;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaCargaReinversionesFondoMutuo{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_DETALLE_CUADRATURA;
		
		public SalidaCargaReinversionesFondoMutuo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ReinversionSerieFondoDestino{
		public Long IdReinversionSerieFondo;
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondoDestino;
		public BigDecimal Porcentaje;
		public String IdRegimenTributario;
		public Long IdCarga;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaCargaReinversionesFondoMutuo logica(Connection con, EntradaCargaReinversionesFondoMutuo entrada) throws Exception {
		logger.info("INICIO::CargaReinversionesFondoMutuo");
		SalidaCargaReinversionesFondoMutuo out = new SalidaCargaReinversionesFondoMutuo();
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<ReinversionSerieFondoDestino> reinversionesCarga = sql.query(con, SQL_SELECT_REINVERSIONES, params, ReinversionSerieFondoDestino.class);
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::IdReinversionSerieFondo", new Long(reinversionesCarga.size()));
		
		AtomicInteger i = new AtomicInteger(0);
		List<ReinversionSerieFondoDestino> reinversiones = reinversionesCarga.stream().map(reinversionCarga -> {
			ReinversionSerieFondoDestino reinversion = new ReinversionSerieFondoDestino();
			reinversion.IdReinversionSerieFondo = secuencias.get(i.getAndIncrement());
			reinversion.IdSolicitudAporteRescate = reinversionCarga.IdSolicitudAporteRescate;
			reinversion.IdSerieFondoDestino = reinversionCarga.IdSerieFondoDestino.trim();
			reinversion.Porcentaje = reinversionCarga.Porcentaje;
			reinversion.IdRegimenTributario = reinversionCarga.IdRegimenTributario;
			reinversion.IdCarga = entrada.p_IdCarga;
			return reinversion;
		}).collect(Collectors.toList());
		
		sql.batch(con, SQL_INSERT_REINVERSIONES, reinversiones, ReinversionSerieFondoDestino.class);
		
		logger.info("FIN::CargaReinversionesFondoMutuo");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		//System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			
			EntradaCargaReinversionesFondoMutuo cIn = new EntradaCargaReinversionesFondoMutuo();
			cIn.p_IdEmpresa = 1L;
			cIn.p_IdUsuario = 69L;
			cIn.p_IdCarga = 67L;
			
			CargaReinversionesFondoMutuo demo=new CargaReinversionesFondoMutuo();				

 			try {
 				SalidaCargaReinversionesFondoMutuo ObjOut=demo.logica(con, cIn);
 				System.out.println(ObjOut);
 				//con.commit();
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
	}
	
}
