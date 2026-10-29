SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Type:                                                 */
/*==============================================================*/
DROP TYPE "BSCL.Tesoreria.BD.Tipos::TES.FormaDePago"         	CASCADE;
DROP TYPE "BSCL.Tesoreria.BD.Tipos::TES.CartolaCuentaBanco"  	CASCADE;
DROP TYPE "BSCL.Tesoreria.BD.Tipos::TES.IngresoEgresoEditar" 	CASCADE;
DROP TYPE "BSCL.Tesoreria.BD.Tipos::TES.TablaCodigosMensajes" 	CASCADE;
DROP TYPE "BSCL.Tesoreria.BD.Tipos::TES.ResultadoReglas" 		CASCADE;


/*==============================================================*/
/* Create Type:                                                 */
/*==============================================================*/

CREATE TYPE "BSCL.Tesoreria.BD.Tipos::TES.FormaDePago"  AS TABLE  ( 
	"NumeroReferencia"			        BIGINT,        -- IdIngresoEgreso / IdComprobante /Id incremental
	"IdMedioDePago"						NVARCHAR(3),   -- CHE, EFE, TRA, VVI, CIN
	"IdModalidadDePago"					NVARCHAR(6),   -- MODCAJ: Por Caja   MODDEP: Depósito
	"IdMoneda"							NVARCHAR(3),
	"Monto"								DECIMAL(26,8),									
	"IdCuentaBancariaCorredora"			BIGINT,
	"IdTarjetaBancariaCorredora"		BIGINT,
	"IdChequera"						BIGINT,
	"NumeroDocumentoCorredora"			BIGINT,
	"FechaDocumentoPagoCorredora"		NVARCHAR(40),
	"IdCuentaBancariaSocio" 			BIGINT,
	"IdBancoSocio"						BIGINT,
	"NombreCuentaBancariaSocio"			NVARCHAR(100),
	"NumeroCuentaBancariaSocio"			NVARCHAR(100),
	"IdTipoCuentaBancariaSocio"			NVARCHAR(4),
	"NombreTipoCuentaBancariaSocio"		NVARCHAR(100),
	"IdTarjetaBancariaSocio"			BIGINT,
	"IdTipoTarjetaBancariaSocio"		NVARCHAR(40),
	"NombreTipoTarjetaBancariaSocio"	NVARCHAR(100),
	"NumeroTarjetaBancariaSocio"		NVARCHAR(100),
	"NumeroSerieSocio"					NVARCHAR(40),
	"NumeroDocumentoSocio"				BIGINT,
	"FechaDocumentoPagoSocio"			NVARCHAR(40)
);


CREATE TYPE "BSCL.Tesoreria.BD.Tipos::TES.CartolaCuentaBanco" AS TABLE  ( 
	 "IdComprobanteFormaDePago"				BIGINT
	,"IdComprobante"						BIGINT
	,"IdTipoMovimiento"						NVARCHAR(6)
	,"NombreTipoMovimiento"					NVARCHAR(100)
	,"IdConcepto"							NVARCHAR(6)
	,"NombreConcepto"						NVARCHAR(100)
	,"IdMedioDePago"						NVARCHAR(3)
	,"NombreMedioDePago"					NVARCHAR(100)
	,"IdModalidadPago"						NVARCHAR(6)
	,"NombreModalidadDePago"				NVARCHAR(100)
	,"IdMoneda"								NVARCHAR(3)
	,"NombreMoneda"							NVARCHAR(100)
	,"Monto"								DECIMAL(26,8)
	,"MontoIngreso"							DECIMAL(26,8)
	,"MontoEgreso"							DECIMAL(26,8)
	,"SaldoContable"						DECIMAL(26,8)
	,"SaldoProyectado"						DECIMAL(26,8)
	,"FechaSaldo"							DATE
	,"IdEstado"								NVARCHAR(3)
	,"NombreEstado"							NVARCHAR(100)
	
--INFO CORREDORA
	,"IdPersonaCorredora"					BIGINT
	,"NombreCorredora"						NVARCHAR(300)
	,"TipoPersonaCorredora"					NVARCHAR(3)
	,"DocumentoIdentidadCorredora"			NVARCHAR(40)
	,"IdCuentaBancariaCorredora"			BIGINT
	,"IdBancoCorredora"						BIGINT
	,"NombreBancoCorredora"					NVARCHAR(100)
	,"NombreTipoCuentaBancariaCorredora"	NVARCHAR(100)
	,"NombreCuentaBancariaCorredora"		NVARCHAR(300)
	,"NumeroCuentaBancariaCorredora"		NVARCHAR(100)
	,"IdChequera"							BIGINT
	,"NumeroSerieCorredora"					NVARCHAR(100)
	,"NumeroDocumentoCorredora"				BIGINT
	,"FechaDocumentoPagoCorredora"			TIMESTAMP
	
	
--INFO SOCIO DE NEGOCIO
	,"IdPersonaSocio"						BIGINT
	,"NombreSocio"							NVARCHAR(300)
	,"TipoPersonaSocio"						NVARCHAR(3)
	,"DocumentoIdentidadSocio"				NVARCHAR(40)
	,"IdPersonaRelacionadoSocio"			BIGINT
	,"IdPortafolioRelacionadoSocio"			BIGINT
	,"NombrePortafolioRelacionadoSocio"		NVARCHAR(300)					
	,"IdCuentaBancariaSocio"				BIGINT
	,"IdBancoSocio"							BIGINT
	,"NombreBancoSocio"						NVARCHAR(100)
	,"NombreTipoCuentaBancariaSocio"		NVARCHAR(100)
	,"NombreCuentaBancariaSocio"			NVARCHAR(100)
	,"NumeroCuentaBancariaSocio"			NVARCHAR(100)
	,"NumeroSerieSocio"						NVARCHAR(100)
	,"NumeroDocumentoSocio"					BIGINT
	,"FechaDocumentoPagoSocio"				TIMESTAMP
	
--INFO ADICIONAL
	,"FechaIngresoRegistro"					TIMESTAMP	
	,"FechaContable"						TIMESTAMP
	,"FechaLiquidacion"						TIMESTAMP
	,"FechaGiro"							TIMESTAMP
	,"IdComprobantePartida"					BIGINT
	
);


CREATE TYPE "BSCL.Tesoreria.BD.Tipos::TES.IngresoEgresoEditar" AS TABLE  ( 
	"NumeroReferencia"							BIGINT, 
	"IdTipoMovimiento"  						NVARCHAR(6),  -- MOVING, MOVEGR
   	"IdTipoPersona"								NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
   	"IdPersona"    								BIGINT,
   	"IdPortafolio"								BIGINT, 
   	"IdMoneda"									NVARCHAR(3),
   	"Monto" 									DECIMAL(26,8),
   	"FechaLiquidacion"           				NVARCHAR(10),                                                                      
   	"IdConcepto" 								NVARCHAR(6),
   	"IdTipoPersonaTitularPago"					NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
   	"IdPersonaTitularPago"    					BIGINT,
   	"IdPortafolioTitularPago"					BIGINT,
   	"IdCaja"   									BIGINT,
   	"SecuenciaAgrupacion" 						NVARCHAR(40), --Aquellos mov con la misma secuencia no son mezclados en distintos comprobante
   	"SecuenciaRelacionada"						BIGINT,		  --Permite relacionar distintos mov y generar un orden de liquidación entre ellos.
   	"Glosa" 									NVARCHAR(260),
									   	
   	-- Parámetros Opcionales(Referenciales)  
   	"IdEmpleadoDigitador" 						BIGINT,
   	"IdSucursalEmpleadoDigitador" 				BIGINT,
   	"IdTipoProducto"   							NVARCHAR(4),
    "IdTipoOperacion" 							NVARCHAR(6),									    
    "IdTipoComprobante"							NVARCHAR(3),
   	"IdComprobante"								BIGINT,
   	"FechaComprobante"							NVARCHAR(40),
    "FechaLiquidacionComprobante"				NVARCHAR(40),
									    
    "IdOperacion" 								BIGINT,			
    "FechaOperacion"							NVARCHAR(40),
    "IdCondicionLiquidacion"					NVARCHAR(2),
    "IdBolsa" 									NVARCHAR(3),
    "IdMonedaOperacion" 						NVARCHAR(3),
    "MontoOperacion" 							DECIMAL(26,8),
    "IdTipoReajuste" 							NVARCHAR(3),
    "FechaParidad" 								NVARCHAR(40),
    "ValorParidad" 								DECIMAL(26,8),
    "FolioCartera" 								BIGINT,
    "IdInstrumento" 							BIGINT,
    "IdTipoInstrumento" 						NVARCHAR(3),
    "IdClasificacionTipoInstrumento" 			NVARCHAR(3),
									  
    -- Indicarores
    "Bilateral"									NVARCHAR(1), --Cleanimg
    "GeneraComprobante"							NVARCHAR(1), --S: genera comprobante automático									    
    "AgruparEnComprobante"						NVARCHAR(1), --S: permite agrupar varios mov. en un comprobante
	"MueveCuentaInversion"						NVARCHAR(1), --S: mueve cuenta inversion N: No mueve cuenta inversion
    "TitularPago"								NVARCHAR(1)  --S: Socio que genera movimiento es quién cobra o paga
);

CREATE TYPE "BSCL.Tesoreria.BD.Tipos::TES.TablaCodigosMensajes" AS TABLE  ( 
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"CodigoDato1" 	NVARCHAR(100),
	"ValorDato1" 	NVARCHAR(520),
	"CodigoDato2" 	NVARCHAR(100),
	"ValorDato2" 	NVARCHAR(520),
	"Resultado" 	NVARCHAR(4)
);

CREATE TYPE "BSCL.Tesoreria.BD.Tipos::TES.CTA.ResultadoReglas" AS TABLE  ( 
	"IdInterno"			BIGINT,   									   
   	"Codigo"			NVARCHAR(100),
   	"Resultado"			NVARCHAR(4),
   	"CodigoDato1"		NVARCHAR(100),
	"ValorDato1"		NVARCHAR(520),
	"CodigoDato2"		NVARCHAR(100),
	"ValorDato2"		NVARCHAR(520)	--OK   = Cumple todos los requisitos,		
										--ADVE = 
		                                --RECH = Alguna de lAS reglAS detienen la operacion
										--RAUT = Requiere autorizacion
);

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
COMMIT;
