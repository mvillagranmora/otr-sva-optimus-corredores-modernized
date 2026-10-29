package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearEnrolamiento implements JavaCall{
	public String SQL_INSERT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearEnrolamiento_Insert.sql");
	
	private Logger logger = LogManager.getLogger(CrearEnrolamiento.class);
	
	public static class EntradaCrearEnrolamiento{
		public Enrolamiento enrolamiento;
	}
	public static class SalidaCrearEnrolamiento{
		public SalidaCrearEnrolamiento(){
			this.p_out_AdvertenciasContador = 0;
			this.p_out_ErroresContador = 0;
			this.p_out_AdvertenciasDetalle = new ArrayList<AdvertenciaDetalle>();
			this.p_out_ErroresDetalle = new ArrayList<ErrorDetalle>();
		}
		
		public Enrolamiento enrolamiento;
		
		public long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaCrearEnrolamiento logica(Connection conn, Enrolamiento in) throws Exception {
		SQL sql = new SQL();
		SalidaCrearEnrolamiento salida = new SalidaCrearEnrolamiento();
		
		List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Enrolamiento", new Long(1));
		in.IdEnrolamiento = secuencia.get(0);
		
		List<Enrolamiento> enrolamientos = Arrays.asList(in);
		logger.debug("INICIO::SQL_INSERT_ENROLAMIENTO");
		int[] resultado = sql.batch(conn, SQL_INSERT_ENROLAMIENTO, enrolamientos, Enrolamiento.class);
		logger.debug("FIN::SQL_INSERT_ENROLAMIENTO");
		if(resultado!=null && resultado.length>0 && resultado[0]>0){
			salida.enrolamiento = in;
		}else{
			salida.p_out_ErroresContador++;
			salida.p_out_ErroresDetalle.add(new ErrorDetalle("UI_ERROR_AL_ENROLAR_SOCIO", "UI_ERROR_AL_ENROLAR_SOCIO"));
		}
		
		return salida;
	}
	
}
