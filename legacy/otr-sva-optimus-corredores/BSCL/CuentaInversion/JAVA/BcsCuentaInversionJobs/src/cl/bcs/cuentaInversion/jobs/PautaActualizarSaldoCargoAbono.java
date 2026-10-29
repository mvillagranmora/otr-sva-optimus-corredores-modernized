package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoEntrada;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoSalida;

import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.utils.Log;


public class PautaActualizarSaldoCargoAbono implements BcsProcesoPauta {
	private final Log log = new Log("CTA.PautaActualizarSaldoCargoAbono");

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio CTA.PautaActualizarSaldoCargoAbono *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");

		    //Invoca servicio de actualizacion de saldos cta inversion
		    ActualizarSaldoCargoAbono actualizarSaldoCargoAbono = new ActualizarSaldoCargoAbono();
		    ActualizarSaldoCargoAbonoEntrada entradaActualizarSaldoCargoAbonoEntrada = new ActualizarSaldoCargoAbonoEntrada(); 
		    entradaActualizarSaldoCargoAbonoEntrada.p_IdEmpresa 				= (Long)parameters.get("IdEmpresa");
		    entradaActualizarSaldoCargoAbonoEntrada.p_IdUsuario 				= (Long)parameters.get("IdUsuario");
		    entradaActualizarSaldoCargoAbonoEntrada.p_FechaMovimiento 			= fechaSistema;  
		    entradaActualizarSaldoCargoAbonoEntrada.p_ActualizarTodosLosSaldos	= "S";
		    ActualizarSaldoCargoAbonoSalida salidaActualizarSaldoCargoAbono = actualizarSaldoCargoAbono.logica(con, entradaActualizarSaldoCargoAbonoEntrada);
		    
			
			
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaActualizarSaldoCargoAbono.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = salidaActualizarSaldoCargoAbono.P_OUT_ERRORESCONTADOR;			
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			

		} catch (Exception e) {
			log.write(e);
			log.write("***** Error PautaActualizarSaldoCargoAbono *****");
			throw e;
		}
		log.write("***** Fin PautaActualizarSaldoCargoAbono *****");
		return salida;
	}

	
    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getDESAConnection()){
			PautaActualizarSaldoCargoAbono servicio = new PautaActualizarSaldoCargoAbono();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa",1L);
		    params.put("IdUsuario",0L);
		    servicio.transaction(con,params);
			
		    
		    con.rollback();
		  }
		  
	}

    
}