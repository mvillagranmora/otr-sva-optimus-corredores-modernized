package cl.bcs.custodia.jobs;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import cl.bcs.custodia.evc.ProcesarEventoCapital;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaProcesoEventoCapital implements BcsProcesoPauta {
	private final Log log = new Log("CUS.PautaProcesoEventoCapital");
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaProcesoEventoCapital *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			String paisSistema = ConfiguracionSistema.getParametroConfiguracionString(con,
					(Long) parameters.get("IdEmpresa"), "PLATAFORMA", "PLA.PaisSistema");
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,
					(Long) parameters.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");

			ProcesarEventoCapital.ProcesarEventoCapitalEntrada parametros = new ProcesarEventoCapital.ProcesarEventoCapitalEntrada();
			parametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametros.p_IdModulo = "CUS";
			parametros.p_Origen = "AUT";
			parametros.p_UsaReglasNegocio = "N";
			parametros.p_IdEventoCapital = new ArrayList<Long>(); // se va vacio
			

			ObtenerDiaSiguiente diaHabilSig = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente entradaObtenerDiaSiguiente = new InObtenerDiaSiguiente();

			List<InObtenerDiaSiguiente.FechasIn> lstFechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
			InObtenerDiaSiguiente.FechasIn objFecha = new InObtenerDiaSiguiente.FechasIn();

			objFecha.fechaInicial = confFechaSistema.toString(); // format.format(addDays(myDate, Calendar.DATE,
																	// -1).getTime());
			objFecha.numeroDias = 1L;
			lstFechas.add(objFecha);

			entradaObtenerDiaSiguiente.p_idPais = Long.parseLong(paisSistema);
			entradaObtenerDiaSiguiente.p_diaHabil = "S";
			entradaObtenerDiaSiguiente.p_fechas = lstFechas;

			OutObtenerDiaSiguiente salidaObtenerDiaSiguiente = diaHabilSig.logica(con, entradaObtenerDiaSiguiente);
			FechasOut fechaHabil = salidaObtenerDiaSiguiente.p_out_fechas.get(0);
			Date fechaCalculada = new SimpleDateFormat("yyyy-MM-dd").parse(fechaHabil.fechaCalculada);
			Date fechaInicial = new SimpleDateFormat("yyyy-MM-dd").parse(fechaHabil.fechaInicial);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			if (fechaHabil.numeroDias > 1) {
				while (fechaInicial.before(fechaCalculada)) {
					String fechaProceso = df.format(fechaInicial);
					parametros.p_FechaLimite.add(fechaProceso);
					fechaInicial = addDay(fechaInicial, 1);
				}
			}else {
				parametros.p_FechaLimite.add(confFechaSistema);
			}

			
			//Agregar una linea por cada tipo de Evento de Capital
			parametros.p_IdTipoEventoCapital.add("DA");	//DIVIDENDO EN ACCIONES *
			parametros.p_IdTipoEventoCapital.add("DV");	//DIVIDENDO EN DINERO *
//			parametros.p_IdTipoEventoCapital.add("EP");	//EMISION PAGADA
//			parametros.p_IdTipoEventoCapital.add("CN");	//CAMBIO NEMO
//			parametros.p_IdTipoEventoCapital.add("CC");	//CORTE CUPON
//			parametros.p_IdTipoEventoCapital.add("DS");	//DIVISION DE SOCIEDAD
//			parametros.p_IdTipoEventoCapital.add("CA");	//CANJE
//			parametros.p_IdTipoEventoCapital.add("SU");	//EMISION SUSCRIPCION
//			parametros.p_IdTipoEventoCapital.add("PB");	//PREPAGO DE BONO
//			parametros.p_IdTipoEventoCapital.add("DF");	//DIVIDENDO EN DINERO FFMM:
//			parametros.p_IdTipoEventoCapital.add("EL");	//EMISION LIBERADA
			parametros.p_IdTipoEventoCapital.add("RC"); //REPARTO CAPITAL
			log.write("==========================================================================");
			log.write("Inicio llamada por pauta procesar evento de capital, parametros llamada:");
			log.write(parametros.toString());
			
			ProcesarEventoCapital proceso = new ProcesarEventoCapital();
			ProcesarEventoCapital.ProcesarEventoCapitalSalida resultadoProceso = proceso.logica(con, parametros);

			salida.P_OUT_ERRORESCONTADOR = resultadoProceso.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES = resultadoProceso.P_OUT_MENSAJES;
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(resultadoProceso.P_OUT_MENSAJES);

			// String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			// log.write("Salida: ");
			// for (String mensaje: mensajes) log.write(mensaje);
		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaProcesoEventoCapital *****");
			throw e;
		}
		log.write("***** Fin PautaProcesoEventoCapital *****");
		
		
		return salida;
	}

	public static Date addDay(Date fecha, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.add(Calendar.DATE, day);
		return calendar.getTime();
	}

}
