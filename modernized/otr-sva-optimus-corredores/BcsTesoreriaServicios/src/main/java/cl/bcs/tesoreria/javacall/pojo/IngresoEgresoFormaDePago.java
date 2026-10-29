package cl.bcs.tesoreria.javacall.pojo;

import java.math.BigDecimal;
import cl.bcs.utils.Utils;

public class IngresoEgresoFormaDePago {
	
	public Long 				IdIngresoEgresoFormaDePago; 		//	BIGINT
	public Long 				IdIngresoEgreso; 					//	BIGINT
	public String				IdTipoMovimiento;					//	NVARCHAR(6)
	public java.sql.Timestamp	FechaIngresoRegistro;				//  SECONDATE(19)
	public String				IdMedioDePago;          			//	NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
	public String				NombreMedioDePago;              	//	NVARCHAR(100)
	public String				IdModalidadDePago;      			//	NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
	public String				NombreModalidadDePago;          	//	NVARCHAR(100)  
	public String				IdMoneda;               			//	NVARCHAR(3)
	public BigDecimal   		Monto; 								//	DECIMAL(26,8)
	public Long 				IdCuentaBancariaCorredora; 			//	BIGINT
	public String				IdTipoCuentaBancariaCorredora; 		//	NVARCHAR(4)
	public String				NombreTipoCuentaBancariaCorredora;	//	NVARCHAR(100)
	public String				NombreCuentaBancariaCorredora;		//	NVARCHAR(100)
	public String				NumeroCuentaBancariaCorredora; 		//	NVARCHAR(100)
	public String				IdTipoTarjetaBancariaCorredora; 	//	NVARCHAR(4)
	public String				NombreTipoTarjetaBancariaCorredora; //	NVARCHAR(100)
	public String				NumeroTarjetaBancariaCorredora; 	//	NVARCHAR(100)
	public Long 				IdTarjetaBancariaCorredora; 		//	BIGINT
	public Long 				IdChequera; 						//	BIGINT
	public Long 				NumeroDocumentoCorredora; 			//	BIGINT	
	public java.sql.Timestamp	FechaDocumentoPagoCorredora;		//	SECONDATE(19)
	public Long 				IdCuentaBancariaSocio; 				//	BIGINT
	public Long 				IdBancoSocio; 						//	BIGINT
	public String				NombreCuentaBancariaSocio;			//	NVARCHAR(100)
	public String				NumeroCuentaBancariaSocio;			//	NVARCHAR(100)
	public String				IdTipoCuentaBancariaSocio;			//	NVARCHAR(4)
	public String				NombreTipoCuentaBancariaSocio; 		//  NVARCHAR(100)
	public Long 				IdTarjetaBancariaSocio;				//	BIGINT,
	public String				IdTipoTarjetaBancariaSocio;  		//	NVARCHAR(40)
	public java.sql.Timestamp	FechaVctoTarjetaBancariaSocio;		//  SECONDATE
	public String				NombreTipoTarjetaBancariaSocio; 	//	NVARCHAR(100)
	public String				NumeroTarjetaBancariaSocio;  		//	NVARCHAR(100)
	public String				NumeroSerieSocio;  					//	NVARCHAR(40)
	public Long 				NumeroDocumentoSocio;				//	BIGINT
	public java.sql.Timestamp	FechaDocumentoPagoSocio;  			//	SECONDATE(19)	
	public String				IdEstado; 							//	NVARCHAR(3)	
	
	public String toString() {
		return Utils.printJson(this, false);
	}		
}

