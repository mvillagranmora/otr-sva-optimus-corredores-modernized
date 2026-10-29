package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;
import java.util.Date;

/**
 * ORACLE FAKE of legacy cl.bcs.tesoreria.LiquidarComprobante (cl/bcs/tesoreria/LiquidarComprobante.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by RecaudacionBCP and GestionDeDepositos.
 */
public class LiquidarComprobante implements JavaCall {

	// legacy cl/bcs/tesoreria/LiquidarComprobante.java:101-128 (verbatim)
	public static class LiquidarComprobante_Entrada 
	{	
		public Long		p_IdEmpresa;  				 
		public Long		p_IdUsuario; 				   
		public String 	p_IdModulo; 				
		public String 	p_SimularOperacion = "N"; 	 
		public String 	p_UsaReglasNegocio;			
	    public String 	p_FechaMovimiento; 	// Corresponde a Fecha Sistema	
	    
	    //Uso exclusivo Aporte CSC
	    public String	FolioCSC = "";
	    // En SQL se llama "p_TablaComprobante"
	    public List<ComprobanteLiquidar_Entrada> p_ComprobanteLiquidar;  // Comprobantes a Liquidar
	     
	    // En SQL se llama "p_TablaComprobanteFormaDePago" 
	    public List<FormaDePagoLiquidar_Entrada> p_FormaDePagoLiquidar;  // Formas de Pago a Liquidar > Si no especifica la FP a liquidar, se asume que se deben liquidar todas las FP de ese comprobante 
	    		
	    public LiquidarComprobante_Entrada()
	    {
	    	p_SimularOperacion    = "N";
	    	p_ComprobanteLiquidar = new ArrayList<ComprobanteLiquidar_Entrada>();
	    	p_FormaDePagoLiquidar = new ArrayList<FormaDePagoLiquidar_Entrada>();
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// legacy cl/bcs/tesoreria/LiquidarComprobante.java:131-136 (verbatim)
	public static class ComprobanteLiquidar_Entrada
	{
		public Long 	IdComprobante;  		
		public String 	IdTipoMovimiento;  		// MOVING / MOVEGR
		public String 	AfectaCuentaInversion;  // CLI: Cliente / COR: Corredora / VACIO: Ambos
	}

	// legacy cl/bcs/tesoreria/LiquidarComprobante.java:139-143 (verbatim)
	public static class FormaDePagoLiquidar_Entrada
	{
		public Long 	IdComprobante;
		public Long 	IdComprobanteFormaDePago;
	}	

	// legacy cl/bcs/tesoreria/LiquidarComprobante.java:150-164 (verbatim)
	public static class LiquidarComprobante_Salida 
	{	
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>	P_OUT_RESULTADOPROCESO;
		
		public LiquidarComprobante_Salida (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO 		= new ArrayList<ResultadoProceso_Salida>();
		};
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/tesoreria/LiquidarComprobante.java:167-177 (verbatim)
	public static class ResultadoProceso_Salida
	{
		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
		public Long   	Id;           // BIGINT        -- IdComprobante liquidado  Ej: 9999
		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes"
		public String	Resultado;    // VARCHAR(40)   -- Ej: "OK"
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/** FAKE of the legacy logica: records its input (Hooks.registrar) and answers the script colaboradores.<Servicio> of the case (see oracle/Hooks.java); nothing of the legacy behaviour is reproduced. */
	public LiquidarComprobante_Salida logica(Connection con, LiquidarComprobante_Entrada in) throws Exception {
		Hooks.registrar("LiquidarComprobante", in);
		return Hooks.salida("LiquidarComprobante", LiquidarComprobante_Salida.class);
	}

}
