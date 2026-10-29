package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.custodia.EjecutarVencimiento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;

public class PautaVencimientosReportes implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaVencimientosReportes");

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaVencimientosReportes *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			EjecutarVencimiento ejevenc = new EjecutarVencimiento();
			EjecutarVencimiento.Entrada inejevenc = new EjecutarVencimiento.Entrada();
			inejevenc.p_IdEmpresa = (Long)parameters.get("IdEmpresa");;
			inejevenc.p_IdModulo = "PLA";
			inejevenc.p_IdUsuario =  (Long)parameters.get("IdUsuario");;
			inejevenc.p_Fecha = confFechaSistema;
			inejevenc.p_IdPais = empresa.IdPais;
			inejevenc.p_SimularOperacion = "N";
			inejevenc.p_IdTipoVencimiento = "RE";
			
			EjecutarVencimiento.Salida outejevenc = ejevenc.logica(con, inejevenc);
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(outejevenc.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = outejevenc.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaVencimientosReportes *****");
			throw e;
		}
		log.write("***** Fin PautaVencimientosReportes *****");
		return salida;
	}

}
