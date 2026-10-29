package cl.bcs.apv.application.recaudacion.colabg4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Verbatim legacy contract (field names = JSON names) of a collaborator outside APV; copied from the legacy source through the oracle shim. */
public final class TesoreriaComprobanteG4 {

    private TesoreriaComprobanteG4() {
    }

    /** Legacy PlataformaUtils.MensajeSalida as it travels in the JSON. */
    public static class Msg {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
    }

    public static class CrearComprobante_Entrada
    	{
    		public Long		p_IdEmpresa; 									//	BIGINT  
    		public Long		p_IdUsuario; 									//	BIGINT 
    		public String 	p_IdModulo; 									//	NVARCHAR(3)    
    		public String 	p_SimularOperacion = "N"; 						//	NVARCHAR(1)
    		public String 	p_UsaReglasNegocio = "S"; 						//	NVARCHAR(1)      
    		public String 	p_FechaMovimiento;								//  NVARCHAR(10)  
    		public Long		p_IdCaja = 0L;   								//	BIGINT			-- Caja donde se procesaran los comprobantes		
    		public String 	p_GenerarComprobanteDesdeMovimientos  = "N";	//	NVARCHAR(1)		-- S: debe llenar p_IngresoEgresoGenerar	
    		public String 	p_GenerarComprobanteDesdeComprobantes = "N";	//  NVARCHAR(1)		-- S: debe llenar p_ComprobanteGenerar	
    		public String 	p_EsIngresoAutomatico = "N";					//  NVARCHAR(1) 	-- S: Es ingreso automatico, N: Es ingreso Manual
    		
    		public List<IngresoEgresoGenerar_Entrada>	p_IngresoEgresoGenerar; //Listado de movimientos para generar comprobantes
    		public List<ComprobanteGenerar_Entrada>		p_ComprobanteGenerar;   //Listado de comprobantes para generar comprobantes
    		public List<FormaDePago_Entrada>			p_FormaDePago;			//Formas de pago para PARING/PAREGR, para comprobantes normales los toma desde el origen
    		
    		public CrearComprobante_Entrada()
    		{
    			this.p_SimularOperacion = "N";
    			this.p_UsaReglasNegocio = "S";
    			this.p_IdCaja = 0L;		
    			this.p_GenerarComprobanteDesdeMovimientos  = "N";	// S: debe llenar p_IngresoEgresoGenerar
    			this.p_GenerarComprobanteDesdeComprobantes = "N";	// S: debe llenar p_ComprobanteGenerar
    			this.p_EsIngresoAutomatico = "N";
    			this.p_IngresoEgresoGenerar = new ArrayList<IngresoEgresoGenerar_Entrada>();
    			this.p_ComprobanteGenerar = new ArrayList<ComprobanteGenerar_Entrada>();
    			this.p_FormaDePago = new ArrayList<FormaDePago_Entrada>();
    		}
    }

    public static class IngresoEgresoGenerar_Entrada
    	{
    		public Long 	NumeroReferencia;   // BIGINT 		-- Correlativo para relacionar los Mov que van a formar el comprobante
    		public Long 	IdIngresoEgreso;	// BIGINT		-- Movimientos que conformarán el comprobante
    		public String 	TipoComprobante;	// NVARCHAR(6) 	-- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
    	}

    public static class ComprobanteGenerar_Entrada
    	{
    		public Long 	NumeroReferencia;   // BIGINT 		 -- Correlativo para relacionar los comprobantes que van a formar un nuevo comprobante
    		public Long 	IdComprobante;   	// BIGINT        -- Comprobantes que conformará el comprobante 
    		public String 	TipoComprobante;   	// NVARVCHAR(6)  -- PARING: Partida Ingreso / PAREGR: Partida Egreso / '': Otro
    	}

    public static class FormaDePago_Entrada  
    	{
    		public Long 		NumeroReferencia;   			// BIGINT
    		public String 		IdMedioDePago;  				// NVARCHAR(3)
    		public String 		IdModalidadDePago;  			// NVARCHAR(6)
    		public String 		IdMoneda;  						// NVARCHAR(3)
    		public BigDecimal	Monto;   						// DECIMAL(26,8)
    		public Long 		IdCuentaBancariaCorredora;   	// BIGINT
    		public Long 		IdTarjetaBancariaCorredora;   	// BIGINT
    		public Long 		IdChequera;   					// BIGINT
    		public Long 		NumeroDocumentoCorredora;   	// BIGINT
    		public String 		FechaDocumentoPagoCorredora;	// NVARCHAR(40)
    		public Long 		IdCuentaBancariaSocio;   		// BIGINT
    		public Long 		IdBancoSocio;   				// BIGINT
    		public String 		NombreCuentaBancariaSocio; 		// NVARCHAR(100)
    		public String 		NumeroCuentaBancariaSocio; 		// NVARCHAR(100)
    		public String 		IdTipoCuentaBancariaSocio; 		// NVARCHAR(4)
    		public String 		NombreTipoCuentaBancariaSocio; 	// NVARCHAR(100)
    		public Long 		IdTarjetaBancariaSocio;  		// BIGINT
    		public String 		IdTipoTarjetaBancariaSocio; 	// NVARCHAR(40)
    		public String 		NombreTipoTarjetaBancariaSocio; // NVARCHAR(100)
    		public String 		NumeroTarjetaBancariaSocio; 	// NVARCHAR(100)
    		public String 		NumeroSerieSocio; 				// NVARCHAR(40)
    		public Long 		NumeroDocumentoSocio;   		// BIGINT
    		public String 		FechaDocumentoPagoSocio; 		// NVARCHAR(40)
    		
    	}

    public static class ResultadoProceso_Salida
    	{
    		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdComprobante"
    		public Long   	Id;           // BIGINT        -- IdComprobante generado  Ej: 9999
    		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearComprobante"
    		public String	Resultado;    // VARCHAR(40)   -- Ej: "OK"
    		public Long NumeroReferencia;
    		
    		public ResultadoProceso_Salida(){
    			this.NumeroReferencia = 0L;
    		}
    	}

    public static class CrearComprobante_Salida
    	{
    		public List<Msg>			 P_OUT_MENSAJES;
    		public Long							 P_OUT_ERRORESCONTADOR;
    		public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;
    		
    		public CrearComprobante_Salida()
    		{
    			P_OUT_MENSAJES = new ArrayList<Msg>();
    			P_OUT_RESULTADOPROCESO =  new ArrayList<ResultadoProceso_Salida>();
    		}
    		
    	}


}
