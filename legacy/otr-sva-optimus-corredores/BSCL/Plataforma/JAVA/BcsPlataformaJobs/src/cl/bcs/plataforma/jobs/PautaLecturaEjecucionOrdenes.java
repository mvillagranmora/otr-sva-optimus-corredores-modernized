package cl.bcs.plataforma.jobs;
  

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

import cl.bcs.normativa.circulares.NotificarCIRCULAR1985;
import cl.bcs.normativa.circulares.NotificarCIRCULAR1985.NotificarCIRCULAR1985_Entrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.pla.ExtraerPreciosBECRuebasDB;
import cl.bcs.plataforma.pla.ExtraerPreciosBECRuebasDB.ExtraerPreciosBECRuebasDB_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.Log;

public class PautaLecturaEjecucionOrdenes implements BcsProcesoPauta {
	
	private final Log log = new Log("PLA.PautaLecturaEjecucionOrdenes");
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaLecturaEjecucionOrdenes *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		ExtraerPreciosBECRuebasDB preciosBEC = new ExtraerPreciosBECRuebasDB();
    		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, (Long)parameters.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");
    		ExtraerPreciosBECRuebasDB_Salida outPrecioBEC = preciosBEC.logica(con, fechaSistema);
    		
			CallableStatement sp = null;
			final String callSp = "CALL \"BSCL.Normativa.BD.Servicios::NOR.ProcesarTareaPauta\"(?, ?, ?)";
			final String IdTipoTarea = "PROCESOLECTURAEJECUCIONORDENES";		
			final long idEmpresa = (Long)parameters.get("IdEmpresa");
			sp = con.prepareCall(callSp);
			sp.setLong(1, idEmpresa);
			sp.setString(2, IdTipoTarea);
			sp.setLong(3, (Long)parameters.get("IdEjecucion"));
			long milis = System.currentTimeMillis();
			sp.execute();
			
			//Inscribir proceso batch para el almacenamiento de cartas
			InscribirProcesoBatch inscribir = new InscribirProcesoBatch();
			InscribirProcesoBatch.InscribirProcesoBatchEntrada entradaBatch = new InscribirProcesoBatch.InscribirProcesoBatchEntrada();
			entradaBatch.p_IdEmpresa = Long.valueOf(idEmpresa);
			entradaBatch.p_IdUsuario  = 1L;
			entradaBatch.p_IdModulo   = "NOR";

			entradaBatch.p_TipoPrograma = new ArrayList<>();
			entradaBatch.p_TipoPrograma.add(new InscribirProcesoBatch.InTipoPrograma(
			    0L, "GenerarCartasCIR1985", 0L, "Generar Cartas Circular 1985"
			));

			entradaBatch.p_Parametro = new ArrayList<>();
			entradaBatch.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "IdEmpresa",    String.valueOf(idEmpresa), null));
			entradaBatch.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "IdUsuario",    "1",                       null));
			entradaBatch.p_Parametro.add(new InscribirProcesoBatch.Parametro(0L, "FechaSistema", fechaSistema,              null));

			InscribirProcesoBatch.InscribirProcesoBatchSalida salidaBatch = inscribir.logica(con, entradaBatch);
			log.write("GenerarCartasCIR1985 inscrito - Errores: " + salidaBatch.P_OUT_ERRORESCONTADOR);

			NotificarCIRCULAR1985 circular1985 = new NotificarCIRCULAR1985();
			NotificarCIRCULAR1985_Entrada in = new NotificarCIRCULAR1985_Entrada();
			in.p_IdEmpresa 			= 1L;
			in.p_IdModulo 			= "NOR";
			in.p_IdUsuario 			= Long.parseLong("1");
			in.p_FechaSistema = fechaSistema;
			circular1985.logica(con, in);
			
			if(outPrecioBEC.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_ERRORESCONTADOR = outPrecioBEC.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_MENSAJE = outPrecioBEC.P_OUT_MENSAJE;
			}
			else
			{
				salida.P_OUT_ERRORESCONTADOR = 0L;
				salida.P_OUT_MENSAJE = "El proceso PautaLecturaEjecucionOrdenes se ha ejecutado correctamente";
			}
			
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaLecturaEjecucionOrdenes *****");
    		throw e;
    	}
		log.write("***** Fin PautaLecturaEjecucionOrdenes *****");
		return salida;
        	
	}
}