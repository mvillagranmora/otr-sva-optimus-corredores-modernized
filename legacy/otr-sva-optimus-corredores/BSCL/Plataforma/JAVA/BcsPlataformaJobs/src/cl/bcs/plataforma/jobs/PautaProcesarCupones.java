package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon.EntradaProcesarEventoCapitalCorteCupon;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon.SalidaProcesarEventoCapitalCorteCupon;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaProcesarCupones implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaProcesarCupones");

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaProcesarCupones *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
			ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
			InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
			fechasIn.fechaInicial = confFechaSistema;
			fechasIn.numeroDias = 1L;
			inObtenerDiaAnterior.fechas.add(fechasIn);
			inObtenerDiaAnterior.idPais = empresa.IdEmpresa;
			inObtenerDiaAnterior.diaHabil = "S";
			
			OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
			String fechaAnterior = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
			
			ProcesarEventoCapitalCorteCupon procesarCorteCupon = new ProcesarEventoCapitalCorteCupon();
			EntradaProcesarEventoCapitalCorteCupon entrada = new EntradaProcesarEventoCapitalCorteCupon();
			entrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			entrada.p_IdUsuario = (Long)parameters.get("IdUsuario");
			entrada.p_IdModulo = "CUS";
			entrada.p_FechaCorteCuponDesde = fechaAnterior;
			entrada.p_FechaCorteCuponHasta = confFechaSistema;
			SalidaProcesarEventoCapitalCorteCupon salidaProcesarCorteCupon = procesarCorteCupon.logica(con, entrada);
			salida.P_OUT_ERRORESCONTADOR = salidaProcesarCorteCupon.P_OUT_ERRORESCONTADOR;
			if (salidaProcesarCorteCupon.P_OUT_ERRORESCONTADOR > 0) {
				long ocurrencia = salidaProcesarCorteCupon.P_OUT_MENSAJES.stream().filter(s -> "UI_FECHA_SIN_CORTES_DE_CUPON".equalsIgnoreCase(s.codigo)).count();
				if (ocurrencia > 0) salida.P_OUT_ERRORESCONTADOR = 0L;
			}
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaProcesarCorteCupon.P_OUT_MENSAJES);
			
		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaProcesarCupones *****");
			throw e;
		}
		log.write("***** Fin PautaProcesarCupones *****");
		return salida;
	}

}
