package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;

import cl.bcs.plataforma.acc.BloqueoUsuariosPorVigencia;
import cl.bcs.plataforma.acc.BloqueoUsuariosPorVigencia.BloqueoUsuariosPorVigenciaEntrada;
import cl.bcs.plataforma.acc.BloqueoUsuariosPorVigencia.BloqueoUsuariosPorVigenciaSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaBloqueoUsuariosPorVigencia implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaBloqueoUsuariosPorVigencia");

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	log.write("***** Inicio PautaBloqueoUsuariosPorVigencia *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		BloqueoUsuariosPorVigencia bloqueo = new BloqueoUsuariosPorVigencia();
			BloqueoUsuariosPorVigenciaEntrada entrada = new BloqueoUsuariosPorVigenciaEntrada();
			entrada.p_IdEmpresa = (Long) parameters.get("IdEmpresa");
			entrada.p_IdUsuario = (Long) ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.IdUsuarioBolsa");

			BloqueoUsuariosPorVigenciaSalida salidaBloqueo = bloqueo.logica(con, entrada);

			salida.P_OUT_MENSAJE = salidaBloqueo.P_OUT_MENSAJES.get(0).descripcion;
			salida.P_OUT_ERRORESCONTADOR = salidaBloqueo.P_OUT_ERRORESCONTADOR;

			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida:");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje : mensajes)
				log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaBloqueoUsuariosPorVigencia *****");
    		throw e;
    	}
    	log.write("***** Fin PautaBloqueoUsuariosPorVigencia *****");
		return salida;
    } 
    
    public static void main(String[] args) throws Throwable {
		System.out.println("== START ==");
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			PautaBloqueoUsuariosPorVigencia pautaBloqueoUsuariosPorVigencia = new PautaBloqueoUsuariosPorVigencia();
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa",1L);
			params.put("IdUsuario",0L);
			
			pautaBloqueoUsuariosPorVigencia.transaction(conn,params );

			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}