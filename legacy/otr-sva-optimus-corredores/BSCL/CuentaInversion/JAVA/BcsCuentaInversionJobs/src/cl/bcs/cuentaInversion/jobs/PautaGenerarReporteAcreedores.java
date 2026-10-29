package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchSalida;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class PautaGenerarReporteAcreedores implements BcsProcesoPauta {
	
	private final static Logger logger = LogManager.getLogger(PautaGenerarReporteAcreedores.class);
	private final String cadena = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private final Integer longuitud = 30;

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		logger.info("INICIO::PautaGenerarReporteAcreedores");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
		
		InscribirProcesoBatch agregarProgramaReporte = new InscribirProcesoBatch();
		InscribirProcesoBatch.InscribirProcesoBatchEntrada entradaAgregarProgramaReporte = new InscribirProcesoBatch.InscribirProcesoBatchEntrada();
		entradaAgregarProgramaReporte.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
		entradaAgregarProgramaReporte.p_IdUsuario = 0L;
		entradaAgregarProgramaReporte.p_IdModulo = "CTA";
		entradaAgregarProgramaReporte.p_TipoPrograma = new ArrayList<>();
		InscribirProcesoBatch.InTipoPrograma tipoPrograma = new InscribirProcesoBatch.InTipoPrograma();
		tipoPrograma.IdInterno = 0L;
		tipoPrograma.IdTipoPrograma = "GenerarReporteAcreedores";
		tipoPrograma.SegundosDesfase = 0L;
		entradaAgregarProgramaReporte.p_TipoPrograma.add(tipoPrograma);
		entradaAgregarProgramaReporte.p_Parametro = new ArrayList<>();
		entradaAgregarProgramaReporte.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "IdEmpresa", String.valueOf(parameters.get("IdEmpresa")), null));
		entradaAgregarProgramaReporte.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "IdUsuario", String.valueOf(0L), null));
		entradaAgregarProgramaReporte.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "P_FechaSolicitud", fechaSistema, null));
		entradaAgregarProgramaReporte.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "P_Moneda", "CLP", null));
		entradaAgregarProgramaReporte.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "P_Consolidar", "S", null));
		entradaAgregarProgramaReporte.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "NombreInternoArchivo", generarNombreRandom(cadena,longuitud), null));
		InscribirProcesoBatchSalida salidaAgregarProgramaReporte = agregarProgramaReporte.logica(con, entradaAgregarProgramaReporte);
		
		salida.P_OUT_ERRORESCONTADOR = salidaAgregarProgramaReporte.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaAgregarProgramaReporte.P_OUT_MENSAJES);

		
		logger.info("FIN::PautaGenerarReporteAcreedores");
		return salida;
	}
	
	private String generarNombreRandom(String cadena, Integer longuitud) {
		logger.trace("INICIO::PautaGenerarReporteAcreedores.generarNombreRandom");
		long inicio = System.currentTimeMillis();
		
		String result = "";
		Integer tamanoCadena = cadena.length();
		while(result.length() < longuitud) {
			Integer posicion = (int)(Math.random()*tamanoCadena);
			char letra = cadena.charAt(posicion);
			if(!result.contains(String.valueOf(letra))) {
				result = result + letra;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::PautaGenerarReporteAcreedores.generarNombreRandom tiempo: " + (fin - inicio));
		return result;
	}

}
