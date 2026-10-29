SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE   "BSCL.Normativa.BD::NOR.GLPROCESO_SALIDA_OPERACIONBASE";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/


CREATE GLOBAL TEMPORARY TABLE "BSCL.Normativa.BD::NOR.GLPROCESO_SALIDA_OPERACIONBASE"
	(
				---CAMPOS COMUNES
			"FechaMovimiento"			DATETIME DEFAULT NULL,
			"FolioTransacion"			NVARCHAR(40) DEFAULT '' NULL,
			"SecuenciaOrden"			NVARCHAR(40) DEFAULT '' NULL,
			"IdOperacion"				BIGINT DEFAULT 0 NULL,
			"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL,
			"IdCliente"					BIGINT DEFAULT 0 NULL,
			"IdPortafolio"				BIGINT DEFAULT 0 NULL,
			"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL,
			"Mercado"					NVARCHAR(6) DEFAULT '' NULL,
			"IdEmisor"					NVARCHAR(40)  DEFAULT '' NULL,
			"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL,
			"Cantidad"					DECIMAL(27,8) DEFAULT 0 NULL,
			"Precio"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Tasa"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdMoneda"					NVARCHAR(3)		DEFAULT '' NULL,
			"IdInstrumento"				BIGINT DEFAULT 0 NULL,
			"IdTipoInstrumento"  		NVARCHAR(3)	 DEFAULT '' NULL,	
			"IdTipoProducto" 			NVARCHAR(4)  DEFAULT '' NULL,
				---ESPECIFICOS: FACTURACION
			"FechaTransaccion"        	SECONDDATE DEFAULT '0001-01-01 00:00:00',
			"TasaCompra"				DECIMAL(26,8) DEFAULT 0 NULL,			
			"RelacionInstrumento"		NVARCHAR(40) DEFAULT '' NULL,
			"IdContraparte"				BIGINT DEFAULT 0 NULL,
			"IdOperadorContraparte"		BIGINT DEFAULT 0 NULL,
			"IdOperadorCorredor"		BIGINT DEFAULT 0 NULL,			
			"IdTipoComprobante"			NVARCHAR(6) DEFAULT '' NULL,
			"FolioComprobante"			BIGINT DEFAULT 0 NULL,
			"ValorPresenteCompraInst"	DECIMAL(27,8) DEFAULT 0 NULL,
			"FechaVencimiento"			DATETIME DEFAULT NULL,
			"MontoCompra"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"MontoVenta"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"Comision"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Derecho"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"CargoFijo"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"IVA"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalCompra"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalVenta"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdCondicionLiquidacion" 	NVARCHAR(3) DEFAULT '' NULL,
			"IdDocumentoRelacion"		BIGINT DEFAULT 0 NULL,
			"FechaVencimientoPacto"		DATETIME DEFAULT NULL,
			"PrecioPlazo"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TasaPlazo"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdMonedaPlazo"				NVARCHAR(3)		DEFAULT ''  NULL,
			"TotalCompraVencimiento"	DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalVentaVencimiento"		DECIMAL(27,8)	DEFAULT 0 NULL,
			"Custodia"					NVARCHAR(3) DEFAULT '' NULL,
			"IdBolsa"					NVARCHAR(3) DEFAULT '' NULL,
			"IdBoveda"					NVARCHAR(6) DEFAULT '' NULL,
			"CantidadGarantia"			DECIMAL(27,8)	DEFAULT 0 NULL,
			"NemoSVS"					NVARCHAR(10) DEFAULT '' NULL,
			"TasaVenta"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"ValorPresente"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"UtilidadPerdida"			DECIMAL(27,8)	DEFAULT 0 NULL,
			"FechaOrigenPacto"			DATETIME DEFAULT NULL,
			"DescripcionOperacion"		NVARCHAR(100) DEFAULT '' NULL,
			"IdEstadoFactura"			NVARCHAR(3) DEFAULT '' NULL,
			"Canal"						NVARCHAR(3) DEFAULT '' NULL,
			"ValorIVA"					DECIMAL(11, 4) DEFAULT 0 NULL,
			"FolioComprobanteAsociado" 	BIGINT DEFAULT 0 NULL,
			"ModalidadEjecucion"		NVARCHAR(3) DEFAULT '' NULL,
			"EsFungible"				CHAR(1) DEFAULT '' NULL,
			"IdMovimientoRelacionado"   BIGINT DEFAULT 0 NULL,
			"CodigoOperadorContraparte" NVARCHAR(40) DEFAULT '' NULL,
			"CodigoOperadorCorredor" NVARCHAR(40) DEFAULT '' NULL,
			---ESPECIFICOS: MESA RF	
			"TipoOperacionSecundaria"	NVARCHAR(6) DEFAULT '' NULL,
			"ValorRescateInicial"		DECIMAL(26,8) DEFAULT 0 NULL,
			---ESPECIFICOS: SPOT
			"IdMonedaSecundaria"		NVARCHAR(3)		DEFAULT '' NULL,
			--NORMATIVA
			"IdNormativa"              		 NVARCHAR(15)    DEFAULT '' NULL,
			"IdTipoDocumento" 				 NVARCHAR(10) 	 NOT NULL DEFAULT '',
			"HIdTipoProducto" 				 NVARCHAR(4) 	 DEFAULT '' NULL,
			"HIdClasificacionTipoInstrumento" NVARCHAR(6) 	 DEFAULT '' NULL,
			"HIdTipoInstrumento"  			 NVARCHAR(3)	 DEFAULT '' NULL,
			"HIdTipoOperacion"				 NVARCHAR(6)     DEFAULT '' NULL,
			"EsEnRueda"				 		 NVARCHAR(2)     DEFAULT '' NULL
			
	);								
	



									    								     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit





