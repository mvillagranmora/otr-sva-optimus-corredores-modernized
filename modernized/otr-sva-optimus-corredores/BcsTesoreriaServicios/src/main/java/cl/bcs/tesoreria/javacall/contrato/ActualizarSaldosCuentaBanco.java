package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class ActualizarSaldosCuentaBanco {

	private ActualizarSaldosCuentaBanco() {
	}


	// legacy Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/ban/ActualizarSaldosCuentaBanco.java:46-60 (verbatim)
	public static class ActualizarSaldosCuentaBanco_Entrada
	{	
		public String 						p_FechaMovimiento;
		public List<CuentaBanco_Entrada>	p_CuentaBanco;
		
		public ActualizarSaldosCuentaBanco_Entrada()
		{
			this.p_CuentaBanco = new ArrayList<CuentaBanco_Entrada>();
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	// legacy Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/ban/ActualizarSaldosCuentaBanco.java:63-71 (verbatim)
	public static class CuentaBanco_Entrada
	{	
		public Long 	IdCuentaBanco;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}		
	}

	// legacy Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/ban/ActualizarSaldosCuentaBanco.java:132-147 (verbatim)
	public static class ActualizarSaldosCuentaBanco_Salida
	{
		public List<MensajeSalida>			 P_OUT_MENSAJES;
		public Long							 P_OUT_ERRORESCONTADOR;
		
		
		public ActualizarSaldosCuentaBanco_Salida()
		{
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
}
