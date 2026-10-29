-- CrearComprobante
SET SCHEMA "BSCL";

-- Entrada
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"   cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar"     cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago"     		cascade;

-- Salida
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso"     	cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle"     	cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle"     cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle"     		cascade;

-- Procesos internos
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes"      		cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"      				cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle"			    cascade;				  
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen"      	cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"      	cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"      		cascade;





CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"
(	
	"NumeroReferencia"			BIGINT,			--Numero correlativo para relacionar los Mov de tesoreria que van a conformar un comprobante
	"IdIngresoEgreso" 			BIGINT,			--Movimientos que conforman comprobante
	"TipoComprobante"			NVARCHAR(6)     --PARING: Partida Ingreso, PAREGR: Partida Egreso, '': Otro	
    										  		  
    										  		  	
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar"
(	
	"NumeroReferencia"			BIGINT,			-- Numero correlativo para relacionar los comprobantes que van a conformar un nuevo comprobante
	"IdComprobante" 			BIGINT,			-- Comprobantes que conforman comprobante
	"TipoComprobante"			NVARCHAR(6)	    -- PARING: Partida Ingreso, PAREGR: Partida Egreso, '': Otro	
)  ON COMMIT DELETE ROWS;
	
	
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago"
   LIKE "BSCL.Tesoreria.BD.Tipos::TES.FormaDePago"  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso"
(	
	"Campo" 					VARCHAR(40),
	"Id" 						BIGINT,
	"Servicio" 					VARCHAR(250),
	"Resultado" 				VARCHAR(40)
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle"
(
	"Codigo" 					NVARCHAR(100),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle"
(	
	"Codigo" 					NVARCHAR(100),
	"RequiereAutorizacion" 		NVARCHAR(40),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle"
(	
	"Codigo" 					NVARCHAR(100),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;
	



-- Procesos Internos
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes"
(
	"Grupo" 						BIGINT, 
	"IdComprobante" 				BIGINT,
	"NumeroReferencia"				BIGINT,
	"TipoComprobante"				NVARCHAR(6),
	"IdPersonaTitularPago"			BIGINT,
	"IdPortafolioTitularPago"		BIGINT,
	"IdMoneda"						NVARCHAR(3),
	"IdCaja"						BIGINT,
	"FechaLiquidacion"				DATE,
	"SecuenciaAgrupacion"			NVARCHAR(40),
	"MueveCuentaInversion"			NVARCHAR(1),
	"NeteaComprobante"				NVARCHAR(1),
	"IdTipoMovimiento"				NVARCHAR(6),
	"AgrupaComprobante"				NVARCHAR(1),
	"IdInternoTes"					BIGINT,
	"IdConcepto"					NVARCHAR(6),
	"IdEmpleadoDigitador"			BIGINT,
	"IdSucursalEmpleadoDigitador"	BIGINT,
	"IdTipoComprobanteOrigen"		NVARCHAR(3),
	"IdTipoProducto"				NVARCHAR(4),
	"IdTipoOperacion"				NVARCHAR(6),
	"IdBolsa"						NVARCHAR(3)
)  ON COMMIT DELETE ROWS;





CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"
(
	"NumeroReferencia"			BIGINT,
	"IdComprobante"				BIGINT,
	"TipoComprobante"			NVARCHAR(6),
	"IdTipoMovimiento"			NVARCHAR(6),
	"IdConcepto"				NVARCHAR(6),
	"IdCaja"					BIGINT,
	"IdMoneda"					NVARCHAR(3),
	"Monto"						DECIMAL(26,8),
	"FechaLiquidacion"			SECONDDATE,
	"IdTipoPersona"				NVARCHAR(3),
	"IdPersona"					BIGINT,
	"NombrePersona"				NVARCHAR(400),
	"IdTipoDocumentoIdentidad"	NVARCHAR(1),
	"DocumentoIdentidad"		NVARCHAR(40),
	"IdTipoPersonaTitularPago"	NVARCHAR(3),
	"IdPersonaTitularPago"		BIGINT,
	"NombrePersonaTitularPago"	NVARCHAR(400),
	"IdPortafolioTitularPago"	BIGINT,
	"TitularPago"				NVARCHAR(1),
	"SecuenciaAgrupacion"		NVARCHAR(40),
	"EsBilateral"				NVARCHAR(1),
	"ComprobanteAgrupado"		NVARCHAR(1),
	"ComprobanteNeteado"		NVARCHAR(1),
	"SeparaPorConcepto"			NVARCHAR(1),
	"GeneradoDesdeMovimientos"	NVARCHAR(1),
	"MueveCuentaInversion"		NVARCHAR(1)
)  ON COMMIT DELETE ROWS;




CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle"
(
	"IdComprobanteDetalle"				BIGINT,
	"IdComprobante"						BIGINT,
	"IdTipoMovimiento"					NVARCHAR(6),
	"IdConcepto"						NVARCHAR(6),
	"NroLinea"							BIGINT,
	"IdMoneda"							NVARCHAR(3),
	"Monto"								DECIMAL(26,8),
	"SecuenciaAgrupacion"				NVARCHAR(40),
	"SecuenciaRelacionada"				BIGINT,
	"IdEmpleadoDigitador"				BIGINT,
	"IdSucursalEmpleadoDigitador"		BIGINT,
	"IdTipoComprobanteOrigen"			NVARCHAR(3),
	"IdComprobanteOrigen"				BIGINT,
	"FechaComprobanteOrigen"			TIMESTAMP,
	"FechaLiquidacionComprobanteOrigen"	SECONDDATE,
	"IdTipoProducto"					NVARCHAR(4),
	"IdTipoOperacion"					NVARCHAR(6),
	"IdBolsa"							NVARCHAR(3),
	"IdMonedaOperacion"					NVARCHAR(3),
	"MontoOperacion"					DECIMAL(26,8),
	"FechaParidad"						SECONDDATE,
	"IdTipoReajuste"					NVARCHAR(3),
	"ValorParidad"						DECIMAL(26,8),
	"FolioCartera"						BIGINT,
	"IdInstrumento"						BIGINT,
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdClasificacionTipoInstrumento"	NVARCHAR(3)
)  ON COMMIT DELETE ROWS;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen"
(
	"IdComprobanteDetalle"	BIGINT,
	"IdIngresoEgreso"		BIGINT,
	"IdComprobante"			BIGINT
)  ON COMMIT DELETE ROWS;


-- Es ROW para poder updatear contenido
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"  
(
	"IdComprobante"							BIGINT,
	"NumeroReferencia"						BIGINT,
	"IdTipoMovimiento"						NVARCHAR(6),
	"IdMedioDePago"							NVARCHAR(3),
	"NombreMedioDePago"						NVARCHAR(100),
	"IdModalidadDePago"						NVARCHAR(6),
	"NombreModalidadDePago"					NVARCHAR(100),
	"IdCuentaBancariaCorredora"				BIGINT,
	"NombreCuentaBancariaCorredora"			NVARCHAR(100),
	"NumeroCuentaBancariaCorredora"			NVARCHAR(100),
	"IdTipoCuentaBancariaCorredora"			NVARCHAR(4),
	"NombreTipoCuentaBancariaCorredora"		NVARCHAR(100),
	"IdTarjetaBancariaCorredora"			BIGINT,
	"IdTipoTarjetaBancariaCorredora"		NVARCHAR(40),
	"NombreTipoTarjetaBancariaCorredora"	NVARCHAR(100),
	"NumeroTarjetaBancariaCorredora"		NVARCHAR(100),
	"IdChequera"							BIGINT,
	"NumeroDocumentoCorredora"				BIGINT,
	"FechaDocumentoPagoCorredora"			NVARCHAR(40),
	"IdCuentaBancariaSocio"					BIGINT,
	"IdBancoSocio"							BIGINT,
	"NombreCuentaBancariaSocio"				NVARCHAR(100),
	"NumeroCuentaBancariaSocio"				NVARCHAR(100),
	"IdTipoCuentaBancariaSocio"				NVARCHAR(4),
	"NombreTipoCuentaBancariaSocio"			NVARCHAR(100),
	"IdTarjetaBancariaSocio"				BIGINT,
	"IdTipoTarjetaBancariaSocio"			NVARCHAR(40),
	"NombreTipoTarjetaBancariaSocio"		NVARCHAR(100),
	"NumeroTarjetaBancariaSocio"			NVARCHAR(100),
	"NumeroSerieSocio"						NVARCHAR(40),
	"NumeroDocumentoSocio"					BIGINT,
	"FechaDocumentoPagoSocio"				NVARCHAR(40),
	"IdMoneda"								NVARCHAR(3),
	"Monto"									DECIMAL(26,8),
	"IdPlaza"								BIGINT,
	"DiasRetencion"							BIGINT,
	"FechaAfectaSaldo"						TIMESTAMP
)  ON COMMIT DELETE ROWS;




CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"CodigoDato1" 	NVARCHAR(100),
	"ValorDato1" 	NVARCHAR(520),
	"CodigoDato2" 	NVARCHAR(100),
	"ValorDato2" 	NVARCHAR(520),
	"Resultado"		NVARCHAR(4)
)  ON COMMIT DELETE ROWS;




----------------------------------------------------------- ServicioCumpleRequisitosCrearComprobantes

DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar"  	cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar"(
  "NumeroReferencia"					BIGINT,			--Numero correlativo para relacionar movimientos con formas de pago
  "IdIngresoEgresoInformado" 			BIGINT,			--Movimientos que conforman comprobante
  "TipoComprobante"						NVARCHAR(6),	--PARING: Partida Ingreso, PAREGR: Partida Egreso, '': Otro														  
  "IdIngresoEgreso"						BIGINT,
  "IdTipoMovimiento"                 	NVARCHAR(6),
  "IdConcepto" 							NVARCHAR(6),
  "IdCaja"								BIGINT,
  "IdMoneda" 							NVARCHAR(3),
  "Monto" 								DECIMAL(26,8),
  "FechaLiquidacion" 					NVARCHAR(10),
  "FechaIngresoRegistro"             	NVARCHAR(40),
  "FechaMovimiento"                 	NVARCHAR(10),
  "IdTipoPersona"      					NVARCHAR(3),
  "IdPersona"							BIGINT,
  "IdTipoDocumentoIdentidad" 	    	NVARCHAR(1),													  
  "IdTipoPersonaTitularPago"      		NVARCHAR(3),
  "IdPersonaTitularPago"				BIGINT,
  "IdPortafolioTitularPago"				BIGINT,
  "SecuenciaAgrupacion" 				NVARCHAR(40),
  "SecuenciaRelacionada"				BIGINT,
  "Glosa" 								NVARCHAR(260),
  "IdEmpleadoDigitador" 				BIGINT,
  "IdSucursalEmpleadoDigitador" 		BIGINT,
  "IdTipoComprobanteOrigen"				NVARCHAR(3),
  "IdComprobanteOrigen"					BIGINT,
  "FechaComprobanteOrigen"				NVARCHAR(40),
  "FechaLiquidacionComprobanteOrigen"	NVARCHAR(40),
  "IdTipoProducto" 						NVARCHAR(4),
  "IdTipoOperacion" 					NVARCHAR(6),
  "IdBolsa" 							NVARCHAR(3),
  "IdMonedaOperacion" 					NVARCHAR(3),
  "MontoOperacion" 						DECIMAL(26,8),
  "FechaParidad" 						NVARCHAR(40),
  "IdTipoReajuste" 						NVARCHAR(3),
  "ValorParidad" 						DECIMAL(26,8),
  "FolioCartera" 						BIGINT,
  "IdInstrumento" 						BIGINT,
  "IdTipoInstrumento" 					NVARCHAR(3),
  "IdClasificacionTipoInstrumento" 		NVARCHAR(3),
  "EsBilateral"							NVARCHAR(1),
  "TitularPago"							NVARCHAR(1),
  "EsIngresoAutomatico"					NVARCHAR(1),
  "NeteaComprobante"					NVARCHAR(1),
  "AgrupaComprobante"             		NVARCHAR(1),
  "MueveCuentaInversion"				NVARCHAR(1),
  "IdEstado"							NVARCHAR(3)
) ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteGenerar"    	cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteGenerar"(
	"NumeroReferencia"					BIGINT,			--Numero correlativo para relacionar comprobantes con formas de pago
	"IdComprobanteInformado" 			BIGINT,			--Comprobantes que conforman comprobante
  	"TipoComprobanteInformado"			NVARCHAR(6),	--PARING: Partida Ingreso, PAREGR: Partida Egreso, '': Otro	
	"IdComprobante"						BIGINT,
	"TipoComprobante"					NVARCHAR(6),
	"IdEmpresa"							BIGINT,
	"IdTipoMovimiento"					NVARCHAR(6),
	"IdConcepto"						NVARCHAR(6),
	"IdCaja"							BIGINT,
	"IdMoneda"							NVARCHAR(3),
	"FechaLiquidacion"					NVARCHAR(10),
  	"FechaMovimiento"					NVARCHAR(10),
  	"IdTipoPersona"						NVARCHAR(3),
  	"IdPersona"							BIGINT,
  	"IdTipoDocumentoIdentidad"			NVARCHAR(1),
  	"TitularPago"						NVARCHAR(1),
  	"IdTipoPersonaTitularPago"			NVARCHAR(3),
  	"IdPersonaTitularPago"				BIGINT,
  	"IdPortafolioTitularPago"			BIGINT,
  	"EsBilateral"						NVARCHAR(1),
  	"MueveCuentaInversion"				NVARCHAR(1),
  	"ComprobanteAgrupado"				NVARCHAR(1),
  	"ComprobanteNeteado"				NVARCHAR(1),
  	"SeparadoPorConcepto"				NVARCHAR(1),
  	"Monto"								DECIMAL(26,8),
  	"IdEstado"							NVARCHAR(3)
) ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_FormasDePagoGenerar"    	cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_FormasDePagoGenerar"(
  "NumeroReferencia"			        BIGINT,        -- IdIngresoEgreso / IdComprobante /Id incremental
  "IdMedioDePago"						NVARCHAR(3),   -- CHE, EFE, TRA, VVI, CIN
  "IdModalidadDePago"					NVARCHAR(6),   -- MODCAJ: Por Caja   MODDEP: Depósito
  "IdMoneda"							NVARCHAR(3),
  "Monto"								DECIMAL(26,8),									
  "IdCuentaBancariaCorredora"			BIGINT,
  "IdTarjetaBancariaCorredora"			BIGINT,
  "IdChequera"							BIGINT,
  "NumeroDocumentoCorredora"			BIGINT,
  "FechaDocumentoPagoCorredora"			NVARCHAR(40),
  "IdCuentaBancariaSocio" 				BIGINT,
  "IdBancoSocio"						BIGINT,
  "NombreCuentaBancariaSocio"			NVARCHAR(100),
  "NumeroCuentaBancariaSocio"			NVARCHAR(100),
  "IdTipoCuentaBancariaSocio"			NVARCHAR(4),
  "NombreTipoCuentaBancariaSocio"		NVARCHAR(100),
  "IdTarjetaBancariaSocio"				BIGINT,
  "IdTipoTarjetaBancariaSocio"			NVARCHAR(40),
  "NombreTipoTarjetaBancariaSocio"		NVARCHAR(100),
  "NumeroTarjetaBancariaSocio"			NVARCHAR(100),
  "NumeroSerieSocio"					NVARCHAR(40),
  "NumeroDocumentoSocio"				BIGINT,
  "FechaDocumentoPagoSocio"				NVARCHAR(40),
  "NombreMedioDePago"					NVARCHAR(100),
  "NombreModalidadDePago"				NVARCHAR(100),
  "IdPersona"							BIGINT,
  "NombreCompleto"						NVARCHAR(300),
  "DocumentoIdentidadDefecto"			NVARCHAR(40),
  "CodigoTipoPersona"					NVARCHAR(3),
  "IdCuentaBanco"						BIGINT,
  "IdBanco"								BIGINT,
  "IdPlaza"								BIGINT,
  "PlazoRetencionMismoBanco"			BIGINT,
  "PlazoRetencionOtroBanco"				BIGINT,
  "NombreCuentaBancaria"				NVARCHAR(100),
  "NumeroCuentaBancaria"				NVARCHAR(100),
  "IdTipoCuentaBancaria"				NVARCHAR(4),
  "NombreTipoCuentaBancaria"			NVARCHAR(100),
  "IdTipoTarjetaBancaria"				NVARCHAR(40),
  "NombreTipoTarjetaBancaria"			NVARCHAR(100),
  "NumeroTarjetaBancaria"				NVARCHAR(100)
) ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_Comprobante"    			cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_Comprobante"(
	"NumeroReferencia"			        BIGINT,
	"IdComprobante"						BIGINT,
  	"TipoComprobante"					NVARCHAR(6),
	"IdTipoMovimiento"					NVARCHAR(6),
	"IdConcepto"						NVARCHAR(6),
	"IdCaja"							BIGINT,
	"IdMoneda"							NVARCHAR(3),
	"Monto"								DECIMAL(26,8),
	"FechaLiquidacion"					NVARCHAR(10), 
	"FechaMovimiento"					NVARCHAR(10),
	"IdTipoPersona"						NVARCHAR(3),
) ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteFormaDePago"    cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteFormaDePago"(
	"NumeroReferencia"			        BIGINT,
	"IdComprobante"						BIGINT,
	"IdMoneda"							NVARCHAR(3),
	"Monto"								DECIMAL(26,8),
	"IdModalidadDePago"   				NVARCHAR(6)
) ON COMMIT DELETE ROWS;


/*
-- falta llevar a script inicial
ALTER TABLE BSCL."BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteFormaDePago"
ADD("IdModalidadDePago"   NVARCHAR(6)  NOT NULL   DEFAULT ''); 
* */


DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"    	 		cascade;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"(
	"NumeroReferencia"			BIGINT,
	"ValidaComprobanteGenerado"	NVARCHAR(1),						   	   									   
	"Codigo"					NVARCHAR(100),
	"Resultado"					NVARCHAR(4),
	"CodigoDato1"				NVARCHAR(100),
	"ValorDato1"				NVARCHAR(520),
	"CodigoDato2"				NVARCHAR(100),
	"ValorDato2"				NVARCHAR(520)	
) ON COMMIT DELETE ROWS;
									
commit;
